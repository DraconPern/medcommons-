package net.medcommons.application.dicomclient.test;

/**
 * Placeholder class for information used in the setAccountFocus call
 * to the context manager.
 * @author mesozoic
 *
 */
public class AccountFocusParameters {
	String accountId = "";
	String auth = "";
	String groupAccountId= "";
	String groupName ="";
	String cxpProtocol ="";
	String cxpHost="";
	String cxpPort ="";
	String cxpPath="";
	/*
	 *  setContextURL(baseURL + "setAccountFocus?"+
    "accountId=" + accountId +
		"&auth=" + auth +
		"&groupAccountId=" + groupAccountId +
		"&groupName=" + groupName +
		"&cxpprotocol=" + protocol +
		"&cxphost=" + host +
		"&cxpport=" + port +
		"&cxppath=" + path
	 */
	public String toURLArguments(){
		StringBuffer buff = new StringBuffer("?");
		buff.append("accountId=");
		buff.append(accountId);
		buff.append("&auth=");
		buff.append(auth);
		buff.append("&groupAccountId=");
		buff.append(groupAccountId);
		buff.append("&groupName=");
		buff.append(groupName);
		buff.append("&cxpProtocol=");
		buff.append(cxpProtocol);
		buff.append("&cxpHost=");
		buff.append(cxpHost);
		buff.append("&cxpPort=");
		buff.append(cxpPort);
		buff.append("&cxpPath=");
		buff.append(cxpPath);


		return(buff.toString());
	}
}