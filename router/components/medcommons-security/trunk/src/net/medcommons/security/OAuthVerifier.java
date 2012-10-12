/*
 * $Id: OAuthVerifier.java 3321 2009-04-22 05:42:30Z ssadedin $
 * Created on 17/01/2008
 */
package net.medcommons.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.JDOMException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.utils.Str;

/**
 * Implements logic for checking requests for OAuth parameters / headers
 * and if found, forwarding them to the central OAuth service for verification.
 * 
 * @author ssadedin
 */
public class OAuthVerifier {
    
    /**
     * Token parsed from request by verify
     */
    private String token;

    /**
     * Regex for parsing OAuth parameters
     */
    static Pattern COMMA_PATTERN = Pattern.compile(",");
    
    /**
     * Utility class for holding OAuth parameters
     */
    static class OAuthParam {
        String name;
        String value;
    }
    
    /**
     * Used by unit tests
     */
    protected static boolean disableValidation = false;
    
    /**
     * Attempts to parse OAuth parameters from 
     * the given request and returns true if request
     * is verified.
     * 
     * @return true - iff the request has valid OAUTH parameters
     * @throws OAuthException 
     */
    public boolean verify(ServletRequest req) throws OAuthException {
        
        HttpServletRequest r = (HttpServletRequest) req;
        
        Map<String, String> params = getOAuthParameters(r);
        
        if(params == null)
            return false;
        
        try {
            String url = getVerificationUrl(r.getRequestURL().toString() + "?"+URLEncoder.encode(r.getQueryString(),"ASCII"), params);
            
            if(!disableValidation) {
                Document result = RESTUtil.executeUrl(url);
            }
            
            this.token = params.get("oauth_token");
            
            return true;
        }
        catch (ConfigurationException e) {
            throw new OAuthException("Unable to verify oauth parameters for URL " + r.getRequestURL(),e);
        }
        catch (JDOMException e) {
            throw new OAuthException("Unable to verify oauth parameters for URL " + r.getRequestURL(),e);
        }
        catch (IOException e) {
            throw new OAuthException("Unable to verify oauth parameters for URL " + r.getRequestURL(),e);
        }
    }

    /**
     * Generate a service URL for the OAuth service for verifying the given OAuth url
     * @param targetUrl - URL to be verified
     * @param params - OAuth parameters
     * @return
     * @throws ConfigurationException
     * @throws UnsupportedEncodingException
     */
    protected String getVerificationUrl(String targetUrl, Map<String, String> params) throws ConfigurationException,
                    UnsupportedEncodingException {
        String url = Configuration.getProperty("OAuthService.verifyOAuthRequest.url");
        StringBuilder qs = new StringBuilder();
        for(String p : params.keySet()) {
            if(qs.length()!=0)
                qs.append("&");
            qs.append(p).append("=").append(URLEncoder.encode(Str.nvl(params.get(p), ""),"ASCII"));
        }
        qs.append("&url=").append(targetUrl);
        
        url += "?";
        url += qs;
        return url;
    }

    /**
     * Search the given request for OAuth parameters as either headers or 
     * query string parameters.
     * @return
     * @throws OAuthException 
     */
    protected Map<String, String> getOAuthParameters(HttpServletRequest r) throws OAuthException {
        String auth = r.getHeader("Authorization");
        
        // Check for headers
        if(hasOAuthHeaders(r)) {
            return parseOAuthHeader(auth);
        }
        else // no headers, look for parameters
        if(r.getParameter("oauth_token") != null)  { 
            Map<String,String> params = new HashMap<String, String>();
            params.put("oauth_consumer_key",r.getParameter("oauth_consumer_key"));
            params.put("oauth_token",r.getParameter("oauth_token"));
            params.put("oauth_signature_method",r.getParameter("oauth_signature_method"));
            params.put("oauth_signature",r.getParameter("oauth_signature"));
            params.put("oauth_timestamp",r.getParameter("oauth_timestamp"));
            params.put("oauth_nonce",r.getParameter("oauth_nonce"));
            params.put("oauth_version",r.getParameter("oauth_version"));
            return params;
        }
        else
            return null;
    }

    /**
     * Extracts all OAuth parameters from the OAuth header
     */
    protected Map<String, String> parseOAuthHeader(String auth) throws OAuthException {
        Map<String,String> params = new HashMap<String, String>();
        String [] rawParams = COMMA_PATTERN.split(auth);
        boolean first = true;
        for(String p : rawParams) {
            // First parameter has Authorization header itself, ignore
            if(first) {
                first = false;
                continue;
            }
                
            OAuthParam param = decodeOAuthHeader(p);
            params.put(param.name, param.value);
        }
        return params;
    }

    /**
     * Do a quick check for OAuth headers / parameters 
     * 
     * @return true iff the request appears to contain OAuth headers / parameters
     */
    protected boolean hasOAuthHeaders(HttpServletRequest r) {
        String auth = r.getHeader("Authorization");
        if(auth == null)
            return false;
        
        auth = auth.trim();
        
        return auth.startsWith("OAuth");
    }
    
    /**
     * Decodes an individual OAuth header of the form 
     * 
     * foo="bar"
     * 
     * @param p - parameter from OAuth header
     * @return OAuthParam object with name and value
     * @throws OAuthException 
     */
    protected OAuthParam decodeOAuthHeader(String p) throws OAuthException {
        try {
            OAuthParam result = new OAuthParam();
            int equalsIndex = p.indexOf('=');
            if(equalsIndex<0)
                throw new OAuthException("Invalid header parameter format for parameter " + p);
            
            if(p.charAt(equalsIndex+1)!='"')
                throw new OAuthException("Invalid header parameter format for parameter " + p + ": expected '\"\'");
            
            int startIndex = p.indexOf("oauth_");
            if(startIndex<0)
                throw new OAuthException("Invalid header parameter format for parameter " + p + ": expected parameter beginning with 'oauth_'");
                
            result.name = p.substring(startIndex,equalsIndex);
            result.value = URLDecoder.decode(p.substring(equalsIndex+2, p.lastIndexOf('"')),"UTF-8");                        
            return result;
        }
        catch (UnsupportedEncodingException e) {
            throw new OAuthException("Failed to decode header parameter: " + p,e);
        }
    }

    public String getToken() {
        return token;
    }

}
