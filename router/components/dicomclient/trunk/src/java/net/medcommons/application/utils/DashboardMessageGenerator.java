package net.medcommons.application.utils;

import java.net.URLEncoder;
import java.text.DecimalFormat;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.transactions.ContextState;

public class DashboardMessageGenerator {
	private static String ddlIdentifier=null;
	
	public enum MessageType{ERROR, INFO, PROMPT};
	

	static{
		ddlIdentifier = ContextManager.get().getConfigurations().getDDLIdentity();
	}
	 
	public static boolean validStateForMessageGeneration(ContextState contextState){
		boolean valid = false;
		if ((contextState != null) && (contextState.getCxpProtocol() != null) && (contextState.getAuth() != null)){
			valid = true;
		}
		return (valid);
	}
	/**
	 * Creates a base URL for status messages
	 * @param contextState
	 * @return
	 */
    public static String createBaseStatusURL(ContextState contextState) {
		StringBuilder buff = new StringBuilder();
		if (contextState.getCxpProtocol().equals("https")) {
			buff.append(JSONSimpleGET.HTTPS);
		} else {
			buff.append(contextState.getCxpProtocol());
		}
		buff.append("://");
		buff.append(contextState.getCxpHost());
		if (contextState.getCxpPort() != null) {
			buff.append(":").append(contextState.getCxpPort());
		}
		buff.append("/router/TransferState.action?put");
		buff.append("&status.accountId=");
		buff.append(contextState.getStorageId());
		buff.append("&auth=");
		buff.append(contextState.getAuth());
		buff.append("&status.ddlIdentifier=").append(ddlIdentifier);
		
		return (buff.toString());
	}
    
    /**
     * Creates a base URL for messages
     * @param contextState
     * @return
     */
    public static String createBaseMessageURL(ContextState contextState) {
		StringBuilder buff = new StringBuilder();
	
		if (contextState.getCxpProtocol().equals("https")) {
			buff.append(JSONSimpleGET.HTTPS);
		} else {
			buff.append(contextState.getCxpProtocol());
		}
		buff.append("://");
		buff.append(contextState.getCxpHost());
		if (contextState.getCxpPort() != null) {
			buff.append(":").append(contextState.getCxpPort());
		}
		buff.append("/router/TransferState.action?addMessage");
		buff.append("&message.accountId=");
		buff.append(contextState.getStorageId());
		buff.append("&auth=");
		buff.append(contextState.getAuth());
		buff.append("&message.ddlIdentifier=").append(ddlIdentifier);
		return (buff.toString());
	}
	/**
     * 
     * https://ci.myhealthespace.com/router/TransferState.action?message&message.transferKey=bfbbdf71f6f8fc6b56cd40a5859313ac4fac1be8&message.message=Hello+World%21&message.ddlIdentifier=bfbbdf71f6f8fc6b56cd40a5859313ac4fac1be8&message.category=INFO&auth=9e9596cc8117d36327370a219e5bb7692ca23137
     * @param baseURL
     * @param key
     * @param status
     * @param progress
     * @return
     */
    public static String makeStatusURL(String baseURL, String key, String status, double progress, int version){
    	DecimalFormat df = new DecimalFormat("#.##");

    	StringBuilder buff = new StringBuilder(baseURL);
    	buff.append("&put");
    	buff.append("&status.key=").append(key);
    	buff.append("&status.status=").append(status);	
    	buff.append("&status.progress=").append(df.format(progress));
    	buff.append("&status.version=").append(version);
    	
    	return(buff.toString());
    }
    
    public static String makeGetStatusURL(String baseURL, String key, String accountId) {
        return new StringBuilder(baseURL).append("put")
                                          .append("&key=").append("key")
                                          .append("&accountId=").append(accountId)
                                          .toString();
    }
    
    public static String makeMessageURL(String baseURL, MessageType messageType, String key,String message){
   	 DecimalFormat df = new DecimalFormat("#.##");

   	StringBuilder buff = new StringBuilder(baseURL);
   	if (key != null)
   		buff.append("&message.transferKey=").append(key);
   	
	buff.append("&message.category="+messageType.name());
   	buff.append("&message.message=").append(URLEncoder.encode(message));
   	
   	return(buff.toString());

   }
}
