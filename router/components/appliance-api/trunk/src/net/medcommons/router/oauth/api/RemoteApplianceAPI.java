/*
 * $Id$
 * Created on 16/06/2008
 */
package net.medcommons.router.oauth.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.router.api.ApplianceAPI;
import net.medcommons.router.api.AuthenticationResult;
import net.medcommons.router.api.InvalidCredentialsException;
import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.json.JSONObject;

/**
 * OAuth implementation of Appliance API.
 * <p>
 * This implementation uses OAuth for most calls but not for all calls: some calls
 * do not require signing and in that case the API can be instantiated
 * without a consumer key and calls can be made using only an
 * access token.
 * 
 * @author ssadedin
 */
public class RemoteApplianceAPI implements ApplianceAPI {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RemoteApplianceAPI.class);
    
    private OAuthConsumer consumer = null;
    
    private OAuthAccessor accessor = null;
    
    private String applianceURL;
    
    private OAuthClient client = new OAuthClient(new URLConnectionClient());
    
    /**
     * Creates instance of api for given consumer and accessor
     * 
     * @param consumer
     * @param accessor
     */
    public RemoteApplianceAPI(String consumerKey, String consumerSecret, String accessToken, String tokenSecret, String applianceURL) {
        OAuthServiceProvider provider = new OAuthServiceProvider(null, null, null);
        this.consumer = new OAuthConsumer(null, consumerKey, consumerSecret, provider);
        this.accessor = new OAuthAccessor(consumer);
        accessor.accessToken = accessToken;
        accessor.tokenSecret = tokenSecret;
        this.applianceURL = applianceURL;
        if(!this.applianceURL.endsWith("/"))
            this.applianceURL += "/";
    }
    
    /**
     * Create an instance of ApplianceAPI without a consumer token.
     * <p>
     * Such instances will be limited to calling APIs that do not require
     * OAuth signatures.
     * 
     * @param accessor
     * @param applianceURL
     */
    public RemoteApplianceAPI(OAuthAccessor accessor, String applianceURL) {
        this(null, null, accessor.accessToken, accessor.tokenSecret, applianceURL);
    }

    /**
     * Resolve storage for the given account.  Executed as a non-oauth call, does
     * not require consumer token.
     */
    public String findStorage(String accid) throws APIException {
        try { 
            JSONObject result = null;
            if(consumer == null || consumer.consumerKey==null) {
                RESTUtil.RestCall call = new RESTUtil.RestCall(this.applianceURL+"acct/ws/resolveGateway.php", this.accessor.accessToken,"accid", accid);
                result = call.fetchJSONResponse();
            }
            else {
	            String baseUrl = this.applianceURL+"api/find_storage.php";
	            
	            Map<String, String> params = new HashMap<String, String>();
	            params.put("accid",accid);
	            
	            OAuthMessage response = client.invoke(accessor, baseUrl, params.entrySet());
	            String json = readResponse(response);
	            
	            result = new JSONObject(json.toString());
            }
            
            String gwUrl = result.getString("result");
	            
            log.info("Resolved result " + gwUrl + " for account " + accid + " on appliance " + applianceURL);
            
            return gwUrl;
        }
        catch (Exception e) {
            throw new APIException("Failed to resolve gateway for account " + accid, e);
        } 
    }
    /**
     * Retrieve Current CCR for given account.
     * <p>
     * Does not require consumer token.
     */
    public String getCCR(String accid, String format) throws APIException {
        
        String gw = this.findStorage(accid);
        String baseUrl = gw + "/ccrs/"+accid;
        try {
            if(consumer == null) {
                RESTUtil.RestCall call = new RESTUtil.RestCall(baseUrl, 
                        this.accessor.accessToken,"accid", accid, "fmt",format, "at", this.accessor.accessToken);
                return RESTUtil.fetchUrlResponse(call.url).toString();
            }
            else {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accid",accid);
                params.put("fmt",format);
                params.put("at",this.accessor.accessToken);
                
                OAuthMessage response = client.invoke(accessor, baseUrl, params.entrySet());
                return readResponse(response);
            }
        } 
        catch (Exception e) {
            throw new APIException("Failed to retrieve CCR for account " + accid, e);
       }
    }

    public AuthenticationResult authenticate(String accid, String password) throws APIException {
        try {
            if(accid.matches("[0-9]{12}")) { // Treat as tracking number
                try { 
                    String pinHash = PIN.hash(password);
                    RESTUtil.RestCall call = 
                        new RESTUtil.RestCall(this.applianceURL+"secure/ws/validateDocument.php", this.accessor.accessToken, "trackingNumber", accid, "pinHash",pinHash);
                    Document d = call.execute();
                    if(!"success".equals(d.getRootElement().getChildText("summary_status"))) 
                        throw new InvalidCredentialsException("Failed to authenticate using provided credentials");
                    Element outputs = d.getRootElement().getChild("outputs");
                    return new AuthenticationResult(outputs.getChildText("mcid"), outputs.getChildText("auth"));
                }
                catch (JDOMException e) {
                    throw new APIException("Unable to validate tracking number / pin for tn="+ accid);
                }
                catch (IOException e) {
                    throw new APIException("Unable to validate tracking number / pin for tn="+ accid);
                }
                catch (NoSuchAlgorithmException e) {
                    throw new APIException("Unable to validate tracking number / pin for tn="+ accid);
                }
            }
            else { // Treat as medcommons id
                String verifyURL = null;
	            if(accid.matches("^[A-Z]{7}$"))  // Treat as voucher 
	                verifyURL = this.applianceURL+"mod/authenticate_voucher.php?voucherId="+URLEncoder.encode(accid,"UTF-8");
	            else
	                verifyURL = this.applianceURL+"acct/ws/wsAuthenticate.php";
	            
                RESTUtil.RestCall call = new RESTUtil.RestCall(verifyURL, this.accessor.accessToken,"accid", accid, "pwd",password);
                JSONObject result = call.fetchJSONResponse();
                JSONObject obj = result.getJSONObject("result");
                if("valid".equals(obj.getString("status"))) {
                    String token = obj.getString("token");
                    log.info("Resolved token " + token + " for account " + accid + " on appliance " + applianceURL);
                    return new AuthenticationResult(obj.getString("mcid"),token); 
                }
                else {
                    log.info("Verification of credentials for account " + accid + " failed");
                    throw new InvalidCredentialsException("Failed to authenticate using provided credentials");
                }
            }
        }
        catch (RESTException e) { 
            throw new APIException("Failed to authenticate account " + accid + ": " + e.getMessage(), e);
        }
        catch (UnsupportedEncodingException e) {
            throw new APIException("Failed to authenticate account " + accid + ": " + e.getMessage(), e);
        }     }
    

    /**
     * Utility method to read the body of the given response as a String
     */
    private String readResponse(OAuthMessage response) throws IOException, UnsupportedEncodingException {
        InputStream s = response.getBodyAsStream();
        StringBuilder b = new StringBuilder();
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read = s.read(buffer)) > 0) {
           b.append(new String(buffer,0,read,"UTF-8"));
        }
        return b.toString();
    }
    
}


