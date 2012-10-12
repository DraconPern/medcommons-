package net.medcommons.application.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import net.medcommons.application.dicomclient.transactions.ContextState;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.HttpSecureProtocol;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Wrapper around HttpClient for GET with low latency; returns
 * a JSON object of the results.
 * 
 * Uses "insecure-https" as the protocol instead of https to avoid
 * cert issues. The protocol handler for this is initialized in
 * DICOMClient.
 * 
 * @author mesozoic
 *
 */
public class JSONSimpleGET {
	
	private static Logger log = Logger.getLogger(JSONSimpleGET.class);
	
	public final static String HTTPS = "insecure-https";
	//private final static int connectionTimeoutMsec = 1000;
	//private final static int responseTimeoutMsec = 1000;
	ContextState contextState = null;
	
    /**
     * Turns off almost all SSL security checks for HTTPS so
     * that we can deal with self-signed and self-signed expired
     * certificates.
     * 
     * To use this - simply replace "https" with the value of the
     * string HTTP (now "insecure-https") in the URL before 
     * making the call.
     * 
     * @param port
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void initInsecureSocketLayer(int port) throws IOException, GeneralSecurityException{

		HttpSecureProtocol f = new HttpSecureProtocol();
		f.setCheckExpiry(false);
		f.setCheckCRL(false);
		f.setCheckHostname(false);
		f.setTrustMaterial(TrustMaterial.TRUST_ALL);

		// To avoid deprecation warnings:
		ProtocolSocketFactory psf = f;
		
		Protocol trustHttps = new Protocol(HTTPS, psf, port);
		Protocol.registerProtocol(HTTPS, trustHttps);
		
    }

	
	public JSONSimpleGET(ContextState contextState) {
		this.contextState = contextState;

	}

	/**
	 * Returns the response body of from URL
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws GeneralSecurityException
	 */
	public JSONObject executeMethod(String url) throws IOException, HttpException,
			GeneralSecurityException, ParseException {
		String responseBody = null;
		HttpClient client = new HttpClient();
		HttpMethod method = null;

		try {
			

			method = new GetMethod(url);

			client.executeMethod(method);
			responseBody = method.getResponseBodyAsString();
			int code = method.getStatusCode();

			if (code > 299) {
				log.error("HttpStatus for service '" + url
						+ "' returned HTTP status " + code + ", responseBody ="
						+ responseBody);
				throw new HttpException("GET returned HTTP code " + code + ", "
						+ responseBody);
			}
			if (log.isDebugEnabled())
				log.debug("GET url:" + url + ", returned \n" + responseBody);
			

		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
		JSONObject obj = new JSONObject(responseBody);
		return (obj);
	}
	public static void runLater(String url){
		final String u = url;
		 new Thread("JSONSimpleGET" + System.currentTimeMillis()) {
			    public void run() {
			      try {
			    	  JSONSimpleGET async = new JSONSimpleGET(null);
			    	  JSONObject response = async.executeMethod(u);
			    	  Object responseStatus = (String) response.get("status");
			    	  if (!responseStatus.equals("ok")){
			    		  log.error("Error sending message " + u + ", response is " + response.toString());
			    	  }
			    	  else{
			    		  log.info("URL " + u + " returned with status OK");
			    	  }
			      }
			      catch(Exception ex) {
			    	  log.error("Error calling url " + u + " asynchronously");
			      }
			    }
			  }.start();
	}
}
