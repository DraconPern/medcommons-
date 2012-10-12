package net.medcommons.application.dicomclient.utils;

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.medcommons.application.dicomclient.transactions.ContextState;

/**
 * A CommandBlock is a request with a block of parameters typically used
 * 
 * TODO: Perhaps should merge CommandBlock parameters and ContextState?
 * @author mesozoic
 *
 */
public class CommandBlock {
	private String command;
	private Properties properties = new Properties();
	
	public CommandBlock(String command){
		this.command = command;
	}
	public CommandBlock(Map<String, String[]> parameters){
		String[] c = parameters.get("command");
		if (c!= null){
			command = c[0];
		}
		else{
			throw new IllegalArgumentException("Missing command in URL");
		}
		Set<String> parameterKeys = parameters.keySet();
		Iterator<String> keys = parameterKeys.iterator();
		while(keys.hasNext()){
			String key = keys.next();
			String values[] = parameters.get(key);
			if (!"command".equals(key)){
				if (values == null)
					properties.put(key, null);
				else
					properties.put(key, values[0]);
			}
		}
	}
	public String getCommand(){
		return(this.command);
	}
	public void addProperty(String name, String value){
		properties.put(name, value);
	}
	public String getProperty(String name){
		return((String) properties.get(name));
	}
	public String urlRequest(){
		StringBuffer buff = new StringBuffer("http://localhost:16092/CommandServlet?command=");
		buff.append(command);
		Enumeration names = properties.propertyNames();
		while(names.hasMoreElements()){
			String name = (String) names.nextElement();
			buff.append("&").append(name).append("=").append(URLEncoder.encode((String)properties.get(name)));
		}
		return(buff.toString());
	}
	public String toString(){
		StringBuffer buff = new StringBuffer("Command:").append(command);
		
		Enumeration names = properties.propertyNames();
		buff.append("[");
		boolean first = true;
		while(names.hasMoreElements()){
			if (first)
				first=false;
			else
				buff.append(",");
			
			String name = (String) names.nextElement();
			buff.append(name).append("=").append(properties.get(name));
		}
		buff.append("]");
		return(buff.toString());
	}
	
	/**
	 * Attempts to create a ContextState from the parameters in this CommandBlock
	 */
	public ContextState toContextState() {
	    ContextState contextState = new ContextState();
		contextState.setStorageId(this.getProperty("storageid"));
		contextState.setAuth(this.getProperty("auth"));
		contextState.setCxpHost(this.getProperty("cxphost"));
		contextState.setCxpPath(this.getProperty("cxppath"));
		contextState.setCxpPort(this.getProperty("cxpport"));
		contextState.setCxpProtocol(this.getProperty("cxpprotocol"));
		contextState.setAccountId(this.getProperty("accountid"));
		contextState.setGuid(this.getProperty("guid"));
		contextState.setGroupName(this.getProperty("groupname"));
		return contextState;
	}
}
