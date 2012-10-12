package net.medcommons.security;

/*
 * net/medcommons/crypto/SessionFilter.java
 * Copyright(c) 2006, Medcommons, Inc.
 */


import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;

/**
 * A SessionFilter intercepts Servlet HTTP requests,
 * verifying if encrypted, timestamped, and signed query strings are valid.
 *
 * If a timestamp is not present, the attribute ts_verified is set to False.
 * If a timestamp is present, but old, an exception is thrown.
 * If the timestamp is current, the attribute ts_verified is set to True.
 * <p>
 * If a signature isn't present, the attribute hmac_verified is set to True.
 * If a signature is present, but invalid, an exception is thrown.
 * If the signature is valid, the attribute hmac_verified is set to True.
 * <p>
 * Any encrypted query strings are decrypted, and inserted into the
 * request to be available to the servlet.
 * <p>This class also handles OAuth signatures.   When OAuth parameters are present,
 * they are checked for validity and if valid, the request is allowed to pass
 * and the auth token from the request becomes active as the security credential
 * for the session.
 *
 * @author <a href='email:tway@medcommons.net'>Terence Way</a>
 * @author <a href='email:ssadedin@medcommons.net'>Simon Sadedin</a>
 */
public class SessionFilter implements Filter {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SessionFilter.class);
    
    /**
     * Regex used to parse parameter strings
     */
    private static final Pattern PARAMETER_PARSE_PATTERN = Pattern.compile("&");

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest req, ServletResponse resp,
                    FilterChain chain)
    throws IOException, ServletException {
        try {
            // If HMAC found then it augments the request and forwards to the chain itself,
            // so we should just return
            if(verifyHMAC(req, resp, chain))
                return;
            
            verifyOAUTH(req,resp,chain);
            
            
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            
            // clear session if session id in URL
            if (httpRequest.isRequestedSessionIdFromURL())
            {
                HttpSession session = httpRequest.getSession();
                if (session != null) session.invalidate();
            }
                    
            // wrap response to remove URL encoding
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpResponse)
            {
                @Override
                public String encodeRedirectUrl(String url) { return url; }

                @Override
                public String encodeRedirectURL(String url) { return url; }

                @Override
                public String encodeUrl(String url) { return url; }

                @Override
                public String encodeURL(String url) { return url; }
            };
            
            chain.doFilter(req, wrappedResponse);
        }
        catch (OAuthException e) {
            throw new ServletException("Failure while validating OAuth parameters",e);
        }
    }
    
    /**
     * Check for OAUTH headers or parameters and if found, attempt to verify them and
     * translate to a standard auth parameter
     * @throws OAuthException 
     */
    private boolean verifyOAUTH(ServletRequest req, ServletResponse resp, FilterChain chain) throws OAuthException {
        
        OAuthVerifier oauth = new OAuthVerifier();
        if(oauth.verify(req)) {
            log.info("Verified OAuth token " + oauth.getToken());
            req.setAttribute("oauth_token", oauth.getToken());
            
            if(!blank(req.getParameter("identity_type"))) {
                
                String firstName = "";
                String lastName = "";
                if(!blank(req.getParameter("identity_name"))) {
                    String [] names = req.getParameter("identity_name").split(" ");
                    firstName = names[0];
                    if(names.length>1) 
                        lastName = names[1];
                } 
                
                AccountSpec accountSpec = new AccountSpec(req.getParameter("identity"), req.getParameter("identity_type"), firstName, lastName);
                req.setAttribute("oauth_principal", accountSpec);
            }
            return true;
        }
        else
            return false;
    }
    
    /**
     * Checks for the proprietary  
     * 
     * @param req
     * @param resp
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    private boolean verifyHMAC(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
                    ServletException {
        HttpServletRequest hreq = (HttpServletRequest) req;
        String queryString = hreq.getQueryString();

        if (queryString == null) {
            return false;
        }

        if (queryString.indexOf("&ts=") >= 0) {
            if (!UrlSession.IsQueryStringCurrent(queryString))
                throw new ServletException("bad timestamp");

            hreq.setAttribute("ts_verified", Boolean.TRUE);
        }
        else
            hreq.setAttribute("ts_verified", Boolean.FALSE);

        if (queryString.indexOf("hmac=") >= 0) {
            if (!UrlSession.IsSignedQueryStringValid("secret", queryString))
                throw new ServletException("bad hmac signature");

            hreq.setAttribute("hmac_verified", Boolean.TRUE);
        }
        else
            hreq.setAttribute("hmac_verified", Boolean.FALSE);

        if (queryString.indexOf("enc=") >= 0) {
            Hashtable parameters = new Hashtable();
            String enc = UrlSession.GetEncryptedQueryString(queryString,
            "secret");

            parameters.putAll(hreq.getParameterMap());
            parseParameters(enc, parameters);
            System.out.println("enc=" + enc);

            hreq.setAttribute("enc_verified", Boolean.TRUE);

            chain.doFilter(new SessionRequestWrapper(hreq,
                            enc + '&' + queryString,
                            parameters), resp);
            return true;
        }
        else {
            hreq.setAttribute("enc_verified", Boolean.FALSE);
            return false;
        }
    }

    static void parseParameters(String queryString, Hashtable map) {
        String [] pieces = PARAMETER_PARSE_PATTERN.split(queryString);
        for(String pair : pieces) {
            if(pair.isEmpty())
                continue;
            int equalsIndex = pair.indexOf('=');
            if(equalsIndex>=0)
                addParameter(map, pair.substring(0,equalsIndex), pair.substring(equalsIndex+1));
            else 
                addParameter(map, pair, "");
        }
    }

    static final void addParameter(Hashtable map, String key, String nextValue) {
        String[] values = (String[]) map.get(key);

        if (values == null) {
            values = new String[1];
            values[0] = nextValue;
        }
        else {
            String[] newValues = new String[values.length + 1];

            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = nextValue;

            values = newValues;
        }

        map.put(key, values);
    }

    public void destroy() {
    }
}

/**
 * Override parameter methods so encrypted parameters can be retrieved.
 */
class SessionRequestWrapper extends HttpServletRequestWrapper {
    private String queryString;
    private Hashtable parameters;

    SessionRequestWrapper(HttpServletRequest req, String queryString,
                    Hashtable parameters) {
        super(req);

        this.queryString = queryString;
        this.parameters = parameters;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getParameter(String name) {
        String[] values = (String[]) this.parameters.get(name);

        return values != null ? values[0] : null;
    }

    public String[] getParameterValues(String name) {
        return (String[]) this.parameters.get(name);
    }

    public Enumeration getParameterNames() {
        return this.parameters.keys();
    }

    public Map getParameterMap() {
        return this.parameters;
    }

}
