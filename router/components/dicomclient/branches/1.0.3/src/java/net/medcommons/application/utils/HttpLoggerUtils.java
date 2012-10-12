package net.medcommons.application.utils;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.net.HTTPAppender;


/**
 * Class provides API to Apache log4j appender for network logging. The network target can be reset
 * dynamically as the DDL accesses different appliances.
 * 
 * There are two alternative mechanisms here.
 * <ol>
 * <li>The setURL method can be used to set the appender's target  </li>
 * <li>The error methods can set the URL temporarily and then the URL target is
 *     reset back to the earlier one.
 * </ol>
 * 
 * This class has some issues:
 * <ul>
 * <li> There are race conditions. Even if the log.error() message were synchronous it's certainly possible 
 * for a setURL or error() call can set the URL while another message is going out. In this 
 * case - it's difficult to know which server it would go to. </li>
 * <li> Introducing synchronization at this level is probably a bad idea - a deadlock could
 * easily occur if there were error messages embedded in other code with synchronized blocks.
 * </li>
 * <li>We could introduce a local variable to synch on - but since the methods are static the variable 
 * would have to be static as well. Synchronization on static variables can lock access to the entire class.
 * </li>
 * </ul>
 * So - leaving synch out here and accepting the risk that some messages might be sent to the wrong server
 * seems best for the moment. 
 * 
 * <p><em>Modified</em>:  it is now permissible to call setURL(null) which disables the HTTP logging
 * altogether.  This is the default state, if no context state is available to give us a logging target.
 * 
 * @author mesozoic, ssadedin@medcommons.net
 */
public class HttpLoggerUtils {
    
	private final static String logname = "ServerHTTPLog";
	
	private static HttpLoggerUtils loggerUtils = new HttpLoggerUtils();
	
	private static Logger logger = Logger.getLogger(HttpLoggerUtils.class);
	
	/**
	 * This is the log threshold originally configured in Log4J.  We disable
	 * logging under some situations and then wish to switch it back to the original
	 * level afterwards.  To make this possible, the original level is cached
	 * here.
	 */
	private static Priority configuredLogThreshold = Priority.INFO;
	
	protected HttpLoggerUtils(){
		super();
	}
	
	public static HttpLoggerUtils getHttpLoggerUtils(){
		
		return(loggerUtils);
	}
	
	/**
	 * Sets the URL target for the HTTPAppender
	 * @param url
	 */
	public static void setURL(String url){ 
	    
		HTTPAppender httpAppender = (HTTPAppender) Logger.getRootLogger().getAppender(logname);
		if (httpAppender != null){
		    
		    if(configuredLogThreshold == null)
		        configuredLogThreshold = httpAppender.getThreshold();
	            
		    httpAppender.setLogURL(url);
		    
		    // If the URL is being set to null then deconfigure the log so that it doesn't
		    // try and send log statements up to the null URL
	        if(url == null) {
	            httpAppender.setThreshold(Priority.FATAL);
	        }
	        else {
	            httpAppender.setThreshold(configuredLogThreshold);
	            logger.info("httpAppender " + httpAppender.getName() + " set to target " + url);
	            httpAppender.activateOptions();
	            logger.info("appender target now set to " + httpAppender.getLogURL());
	        }
		}
		else{
			logger.error("No httpAppender with name '" + logname + "'");
			dumpAppenders();
		}
	}
	
	/**
	 * Gets the current URL target for the HTTPAppender
	 * @return
	 */
	private static String getURL(){
		String url = null;
		HTTPAppender httpAppender = (HTTPAppender) Logger.getRootLogger().getAppender(logname);
		if (httpAppender != null){
			url = httpAppender.getLogURL();
		}
		return(url);
		
	}
	
	public static void error(Logger log, String url, String msg, Throwable t){
		String oldURL = getURL();
		setURL(url);
		log.error(msg, t);
		setURL(oldURL);
	}
	
	public static void error(Logger log, String url, String msg){
		String oldURL = getURL();
		setURL(url);
		log.error(msg);
		setURL(oldURL);
	}
	
	private static void dumpAppenders(){
		StringBuffer buff = new StringBuffer("Appenders:");
		Enumeration<Appender> appenders = logger.getAllAppenders();
		while (appenders.hasMoreElements()){
			Appender a = appenders.nextElement();
			buff.append("\n").append(a.getName()).append(":").append(a.getClass());
			
		}
		logger.info(buff.toString());
	}
}
