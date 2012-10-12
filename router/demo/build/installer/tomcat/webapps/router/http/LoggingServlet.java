/*
 * $Id$
 * Created on Dec 15, 2004
 */
package net.medcommons.router.web.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.ccr.CCRChangeElement;
import net.medcommons.router.util.CustomJDOMFactory;

import org.apache.commons.httpclient.HttpConstants;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * Receives Atom feed of log messages from log4j HTTPAppender and places
 * them in the system log.
 * 
 * INFO messages are logged via log.info(), ERROR messages are logged via log.error(), WARN
 * are logged as warn(). DEBUG messages are logged as log.info() because we don't want to have 
 * to turn on logging for the gateway appliance if we're debugging a remote application.
 * 
 * @author sean
 */
public class LoggingServlet extends HttpServlet {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(LoggingServlet.class);

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig arg0) throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        this.doPost(arg0, arg1);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        handleLogEntry(request, response);

    }

    private void handleLogEntry(HttpServletRequest request, HttpServletResponse response) throws IOException{
       InputStream in = request.getInputStream();
       StringBuffer buff = new StringBuffer();
       PrintWriter out = new PrintWriter(response.getWriter());
       try{
           byte [] b = new byte[4096];
           
           int i=in.read(b, 0, 4096);
      
           int nBytes = i;
           while (i != -1){
               
               if (nBytes > (16*1024)){
                   log.error("Max length of log message exceeded " + nBytes + "\n" +
                           buff.toString());
                  
                   return;
               }
               String s = new String(b,0,i);
               buff.append(s);
               //log.info(" Read " + i + " bytes, '" + s +"'");
               i = in.read(b, 0, 4096);
               nBytes += i;
           }
           
           
           Enumeration<String> params = request.getParameterNames();
           while(params.hasMoreElements()){
               String pName = params.nextElement();
               log.info("Parameter:" + pName + ", value:" + request.getParameter(pName));
           }
           logMessage(buff.toString());
           response.setStatus(200);
           out.print("OK");
       }
       catch(Exception e){
           log.error("Error handling log entry " + e.getLocalizedMessage(), e);
           response.setStatus(500);
           out.print("Server error: " + e);
       }
       finally{
           in.close();
           out.close();
       }
      
     
    }
    private void logMessage(String s) throws IOException, JDOMException{
        Namespace atom = Namespace.getNamespace("http://www.w3.org/2005/Atom");
        Namespace logNs = Namespace.getNamespace("log","http://log4j.apache.org/2007/Log"); // local ns
        Reader reader = new StringReader(s);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(reader);
        Element entry = doc.getRootElement();
        Element title = entry.getChild("title", atom);
        Element id = entry.getChild("id", atom);
        Element loggerName = entry.getChild("loggerName", logNs);
        Element message = entry.getChild("msg", logNs);
        Element stackTrace = entry.getChild("stackTrace", logNs);
        Element level = entry.getChild("level", logNs);
        Element methodName = entry.getChild("methodName", logNs);
        Element className = entry.getChild("className", logNs);
        Element lineNumber = entry.getChild("lineNumber", logNs);
        Element fileName = entry.getChild("fileName", logNs);
        Element timeStamp = entry.getChild("timeStamp", logNs);
        Element hostName = entry.getChild("hostName", logNs);
        
        if (entry != null){
           
            String msg = "UNKNOWN";
            
            String lev = "UNKNOWN";

            if (message != null){
                msg = message.getText();
            }
           
            if (level != null){
                lev = level.getText();
            }
            StringBuffer logMessage = new StringBuffer();
            if (hostName != null){
                logMessage.append("Remote log entry from " + hostName.getText());
                if (timeStamp != null){
                    logMessage.append(" at localTime ").append(timeStamp.getText());
                }
                logMessage.append("\n");
            }
            logMessage.append(msg);
            if (className != null){
                logMessage.append(className.getText());
            }
            if (methodName != null){
                logMessage.append(".").append(methodName.getText());
            }
            if (lineNumber != null){
                logMessage.append("(line ").append(lineNumber.getText()).append(")");
            }
            if (fileName != null){
                logMessage.append("\n File:").append(fileName.getText());
            }
            
            if (stackTrace != null){
                logMessage.append("\n");
                logMessage.append(stackTrace.getText());
            }
            if ("INFO".equals(lev)){
                log.info(logMessage.toString());
            }
            else if ("ERROR".equals(lev)){
                log.error(logMessage.toString());
            }
            else if ("WARN".equals(lev)){
                log.warn(logMessage.toString());
            }
            else if ("FATAL".equals(lev)){
                log.fatal(logMessage.toString());
            }
            else if ("DEBUG".equals(lev)){
                log.info("Remote DEBUG message\n" + logMessage.toString());
            }
            else {
                log.error("Unknown log level + '" + lev + "'," + logMessage.toString());
            }
        }
        else{
            log.error("No entry element in " + s);
        }
        

    }
}
