package net.medcommons.application.utils;

import static net.medcommons.application.utils.Str.blank;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import net.medcommons.application.dicomclient.ContextManager;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
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
	
	private static Logger log = Logger.getLogger("JSONSimpleGET");
	
	/**
	 * Timeout for connections - defaults to 0 which means, no timeout
	 */
	private int timeoutMs = 0;
	
	public final static String HTTPS = "insecure-https";
	
	//private final static int connectionTimeoutMsec = 1000;
	//private final static int responseTimeoutMsec = 1000;
	
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

	
	public JSONSimpleGET(int timeoutMs) {
        super();
        this.timeoutMs = timeoutMs;
    }

    public JSONSimpleGET() {
	}

    /**
     * Posts the specified data to the specified URL.
     * <p>
     * The https protocol in the URL is automatically replaced with our 
     * customized protocol (see {@link #HTTPS}).
     */
	public JSONObject post(String url, byte[] postData) throws IOException {
	    String https = "https://";
        if(url.startsWith(https))
	        url = JSONSimpleGET.HTTPS + "://" + url.substring(https.length());
	            
	    PostMethod post = new PostMethod(url);
	    post.setRequestEntity(new ByteArrayRequestEntity(postData, "application/x-www-form-urlencoded"));
	    return executeMethod(post);
	}
	
	public JSONObject post(String url, File file) throws IOException {
	    PostMethod post = new PostMethod(url);
	    Part[] parts = {
                new FilePart("imageFile", file)
            };
	    post.setRequestEntity(
	            new MultipartRequestEntity(parts, post.getParams())
	    );	    
	    return executeMethod(post);
	}
	
	public static final long [] retryDelays = { 3000, 10000, 30000, 60000, 120000, 120000 };
	
	public JSONObject get(String url, int retries) throws IOException {
	    
	    if(retries > 4) {
	        log.warn("Retries " + retries + " exceeds maximum.  Resetting to 4");
	        retries = 4;
	    }
	    
	    int count = 0;
	    IOException lastError = null;
	    while(count < retries) {
	        try {
	            return get(url);
	        }
	        catch(IOException e) {
	            ++count;
	            log.warn("IO Failure while sending to URL " + url + " Retry " + count + " / " + retries);
	            try { Thread.sleep(retryDelays[count]); } catch (InterruptedException e1) { }
	            lastError = e;
	        }
	    }
	    
	    // Exceeded max attempts
	    throw lastError;
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
	public JSONObject get(String url) throws IOException {
	    return executeMethod(new GetMethod(url));
	}
	
	public JSONObject executeMethod(HttpMethodBase method) throws IOException {
	    String url = method.getURI().toString();
		try {
            String responseBody = executeRaw(method, url);
            JSONObject obj = new JSONObject(responseBody);
            return (obj);
        } catch (ParseException e) {
            throw new IOException("Unable to execute simple JSON transaction to url " + url,e);
        }
	}


    public String rawGet(String url) throws IOException, HttpException {
        String https = "https://";
        if(url.startsWith(https))
            url = JSONSimpleGET.HTTPS + "://" + url.substring(https.length());
	    return executeRaw(new GetMethod(url),url);
    }
    
    private String executeRaw(HttpMethodBase method, String url) throws IOException, HttpException {
        String responseBody = null;
        
        // url = url.replaceAll("^https://", HTTPS + "://");
        
        HttpClient client = new HttpClient();
        if(timeoutMs > 0) {
            client.getParams().setSoTimeout(timeoutMs);
        }
        if(!blank(ContextManager.get().getConfigurations().getProxyAddress())) {
            String[] proxyParts = ContextManager.get().getConfigurations().getProxyAddress().split(":");
            client.getHostConfiguration().setProxy(proxyParts[0], Integer.parseInt(proxyParts[1]));
            if(!blank(ContextManager.get().getConfigurations().getProxyAuthUserName())) {
                HttpState state = new HttpState();
                state.setProxyCredentials(AuthScope.ANY, 
                        new UsernamePasswordCredentials(
	                      ContextManager.get().getConfigurations().getProxyAuthUserName(),
	                      ContextManager.get().getConfigurations().getProxyAuthPassword()));
            }
        }
        
        try {
        	if(log.isInfoEnabled())
        	    log.info("Executing URL: " + url);
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
        	// if (log.isDebugEnabled())
        	//	log.debug("Fetch of url:" + url + ", returned \n" + responseBody);
        	

        } finally {
        	if (method != null) {
        		method.releaseConnection();
        	}
        }
        return responseBody;
    }
	
	public static void runLater(String url){
		final String u = url;
		 new Thread("JSONSimpleGET" + System.currentTimeMillis()) {
			    public void run() {
			      try {
			    	  JSONSimpleGET async = new JSONSimpleGET();
			    	  JSONObject response = async.get(u);
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
	
	public static byte [] createParcel(byte[] src, int start, int length) {
	    byte[] parcel = new byte[ length ];
	    
	    System.arraycopy(src, start, parcel, 0, length);
	    
	    return parcel;
	}


    public int getTimeoutMs() {
        return timeoutMs;
    }


    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
