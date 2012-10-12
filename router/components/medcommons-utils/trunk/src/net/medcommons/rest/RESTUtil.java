/*
 * $Id$
 * Created on 11/05/2005
 */
package net.medcommons.rest;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.join;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import javax.net.ssl.*;

import net.medcommons.modules.utils.Str;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;

/**
 * Class to assist in performing simple REST queries
 * 
 * @author ssadedin
 */
@SuppressWarnings("unchecked")
public class RESTUtil {
    
    static class CacheEntry {
        public CacheEntry(long expiry, String response) {
            this.expiry = expiry;
            this.response = response;
        }
        public long expiry;
        public String response;
    }

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RESTUtil.class);
    
    private static ListOrderedMap internalCache = new ListOrderedMap();
    
    /**
     * Cache of requests that are sent back with max-age headers
     */
    private static Map<String,CacheEntry> cache = Collections.synchronizedMap(internalCache);
    
    /**
     * Source for configuration
     */
    private static RESTConfiguration config;
    
    /**
     * Key used to sign URLs
     */
    private static ThreadLocal<String> key = new ThreadLocal<String>();
    
    /**
     * Set to true to run this class in test mode - ie. it will
     * not actually make the calls, only log them, allowing you 
     * to look at the calls made in the call log
     */
    public static boolean testMode = false;
    
    /**
     * list of results for tests
     */
    public static List<String> testCallResults = new ArrayList<String>();
    
    /**
     * If set then will be used for the next response in place of doing
     * a real call.  USED BY TESTS ONLY.
     */
    public static String testResponse;
    
    /**
     * A simple object to capture calls
     * @author ssadedin
     */
    public static class RestCall {
        public RestCall(String serviceUrl,String authToken, String... params) throws RESTException {
            try {
                StringBuffer paramLog = new StringBuffer();
                timeStamp = System.currentTimeMillis();
                StringBuilder url = new StringBuilder(serviceUrl);
                String name = null;
                StringBuilder inferred = null;
                boolean questionMarkAdded = false;
                boolean nonInferred = false;
                for (String param : params) {
                    if(name == null) {
                        name = param;
                    }
                    else {
                        if (param == null){
                            log.debug("Parameter " + name + " has value null, resetting to empty string");
                            param = "";
                        }
                        String paramName = "#" + name + "#";
                        int pos = url.indexOf(paramName);
                        // log.info(paramName + ":" + param);
                        if(url.indexOf(paramName)>=0) {
                            String encoded = (param == null) ? "" : URLEncoder.encode(param, "UTF-8");
                            url = url.replace(pos, pos+paramName.length(),encoded);
                            if(paramLog.length()>0) 
                                paramLog.append(", ");
                            paramLog.append(name + "=" + param);
                            
                            questionMarkAdded=true; // we don't really know, but ASSUME it must be, since we found a value
                            nonInferred = true; // hack: force & to be added below - if we found a config'd param then it will need one.
                        }
                        else { // not found in config'd url - infer name
                            if(inferred == null) {
                                inferred = new StringBuilder();
                            }
                            else
                            if(!nonInferred)
                                inferred.append("&");
                            
                            if(nonInferred)
                              inferred.append("&");
                            
                            inferred.append(name);
                            inferred.append("=");
                            inferred.append(URLEncoder.encode(param, "ASCII"));
                        }
                        name = null;
                    }
                }
                
                // If it is provided, add the clientId
                if(!questionMarkAdded) {
                    questionMarkAdded = url.indexOf("?")>=0;
                }           
                
                if(authToken != null) { // Add client id
                    if(questionMarkAdded)
                        url.append("&auth="+authToken);
                    else {
                        url.append("?auth="+authToken);
                        questionMarkAdded = true;
                    }
                }
                
                if(inferred != null) { // if any parameters were inferred, add them too
                    if(!questionMarkAdded)
                        url.append('?');
                    else 
                        url.append('&');
                    url.append(inferred);
                }
                
                this.url = url.toString();            
                this.params = paramLog.toString();
            }
            catch (UnsupportedEncodingException e) {
                throw new RESTException("Failed to encode URL with parameters " + join(params,","),e);
            }            
        }
        
        public void setExceptionResult(Exception e) {
            PrintWriter s = new PrintWriter(new StringWriter());
            e.printStackTrace(s);
            s.flush();
            result = s.toString();
        }
        
        public long timeStamp;
        public String url;
        public String result;
        public String service;
        public String params;
        public String getResult() {
            return result;
        }
        public long getTimeStamp() {
            return timeStamp;
        }
        public String getUrl() {
            return url;
        }
        public String getService() {
            return service;
        }
        public String getParams() {
            return params;
        }
        
        public Document execute() throws JDOMException, IOException {
            return RESTUtil.executeUrl(this.url);
        }
        
        /**
         * Execute the URL encapsulated by this RESTCall and return the result
         * as a JSON object.
         * <p>
         * The returned response is expected to have a (non-REST-like) status
         * field called "status".  If the status field is not present or is not
         * equal to "ok" then an exception is thrown.
         * 
         * @return JSON Object containing parsed result of returned result.
         * @throws RESTException
         */
        public JSONObject fetchJSONResponse() throws RESTException {
            if(RESTUtil.log.isInfoEnabled())
                RESTUtil.log.info("Sending JSON Request: " + this.getUrl());
            
            try {
                StringWriter sw = RESTUtil.fetchUrlResponse(this.getUrl());
                JSONObject obj = new JSONObject(sw.toString());
                // Decode result
                if("ok".equals(obj.getString("status"))) {
                    return obj;
                }
                else {
                    String error = "REST JSON call returned non-success status: " + obj.getString("status");
                    if(!obj.isNull("message")) 
                        error = obj.getString("message");
                    else
                    if(!obj.isNull("error")) 
                        error = obj.getString("error");
                    throw new RESTException(error); 
                }
            }
            catch (MalformedURLException e) {
                throw new RESTException("JSON call to URL " + this.url + " failed",e);
            }
            catch (NoSuchElementException e) {
                throw new RESTException("JSON call to URL " + this.url + " failed",e);
            }
            catch (IOException e) {
                throw new RESTException("JSON call to URL " + this.url + " failed",e);
            }
            catch (ParseException e) {
                throw new RESTException("JSON call to URL " + this.url + " failed",e);
            }
        }
    }
    
    /**
     * Log kept of outgoing calls
     */
    public static List<RestCall> callLog = new ArrayList<RestCall>();
    
    /**
     * Whether SSL has been initialized.  Initializing SSL disables CERT checking
     * if that option has been configured.
     */
    static boolean sslInitialized = false;

    public static void initSSL() {
        if(sslInitialized)
            return;
        
        if(testMode)
            return;
        
        sslInitialized = true;
        
        // If flag set, install a dummy SSL manager that will believe anything.
        if(config.getProperty("DisableSSLCertValidation", false)) {
            log.warn("Disabling SSL Cert Validation.  THIS GATEWAY IS INSECURE.");
            try {
                SSLContext sslCtx = SSLContext.getInstance("SSL");
                HostnameVerifier hv = new HostnameVerifier() {
                    public boolean verify(String urlHostName,SSLSession session) {
                        return true;
                    }
                };
                
                TrustManager[] trustAllCerts = new TrustManager[] {
                                new X509TrustManager() {
                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                        return null;
                                    }
                                    public void checkClientTrusted(
                                                    java.security.cert.X509Certificate[] certs, String authType) {
                                    }
                                    public void checkServerTrusted(
                                                    java.security.cert.X509Certificate[] certs, String authType) {
                                    }
                                }
                };
                sslCtx.init(null, trustAllCerts, new java.security.SecureRandom());
                SSLSocketFactory sslSocketFactory = sslCtx.getSocketFactory();            
                HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            } 
            catch (Exception e) {
                log.error("Could not create SSLSocketFactory",e);
                throw new RuntimeException(e);
            }
        }
        else
            log.info("SSL Cert validation is enabled.");
    }
    
           
    public RESTUtil() {
        super();
    }
    
    public static void init(RESTConfiguration cfg) {
        if(config == null)
            RESTUtil.config = cfg;
    }

     /**
     * Accepts a service name and url params as arguments and makes a REST call
     * to the corresponding service, returning the results as a JDOM document.
     * <p>
     * An optional ability to map parameter names is supported, so that parameter
     * names in the actual URL used can differ to those supplied to this
     * function call.
     * <p>
     * The service name should correspond to a configured URL in the configuration 
     * file.  This is retrieved from the configuration file as a property with
     * name "<ServiceName>.url".
     * <p>
     * The parameters supplied should be name value pairs in the form
     * <code>name1,value1,name2,value2,...</code>
     * Parameters will be substituted into the configured URL using a simple
     * template mechanism, whereby each parameter should appear in the configured
     * URL in the form #<param name>.
     * <p>
     * An example of a parameterised url:
     * 
     * ConfigServiceUrl=https://my.config.host.net/config?configValue=#configValue#
     * <p>Alternatively, a global configuration value for the service can be specified
     * in the configuration file of the form:  "<ServiceServer>.baseUrl".  In that case
     * if there is no specific configuration for a given service call then a URL will
     * be computed by combining the base URL value with the name of the service,
     * and parameters will be assumed to be named exactly as passed in the "params"
     * argument.
     * 
     * @param authToken - the authToken received by central to authorize the transaction
     * @param service - the name of the service.  Must correspond to configuration property "&lt;service&gt;Url"
     * @param params - parameters containing name,value pairs for arguments
     * 
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws RESTException 
     */
    @SuppressWarnings("unchecked")
    public static Document call(String authToken, String service, String... params) throws RESTException {
        initSSL();
        
        RestCall call = null;
        try {
            String serviceUrl = getServiceUrl(service);

            call = new RestCall(serviceUrl,authToken, params);
            call.service = service;
            
            Document doc = call.execute();            
            
            String status = doc.getRootElement().getChildText("summary_status");
            call.result = status;
            if(!"success".equals(status)) {
                // Billing API uses different convention for returned status (argh!)
                Iterator<Element> i = doc.getDescendants(new ElementFilter("status"));
                if(i.hasNext() &&  Pattern.matches("[10]",(status = i.next().getTextTrim()))) {
                    call.result = status;
                }
                else 
                    throw new RESTException("REST call returned summary status " + status, doc);
            }
            
            return doc; 
        }
        catch (UnsupportedEncodingException e) {
            if(call !=null)
                call.setExceptionResult(e);
            throw new RESTException("Call to service " + service + " with parameters " + params + " failed", e);
        }
        catch (RESTConfigurationException e) {
            if(call !=null)call.setExceptionResult(e);
            throw new RESTException("Call to service " + service + " with parameters " + params + " failed", e);
        }
        catch (JDOMException e) {
            if(call !=null)call.setExceptionResult(e);
            throw new RESTException("Call to service " + service + " with parameters " + params + " failed", e);
        }
        catch (IOException e) {
            if(call !=null)call.setExceptionResult(e);
            throw new RESTException("Call to service " + service + " with parameters " + params + " failed", e);
        }
        catch (RuntimeException e) {
            if(call !=null)call.setExceptionResult(e);
            throw e;
        }        
        finally {
            if(config.getProperty("EnableRestLog",false)) {
	            callLog.add(call);
	            if(callLog.size()>100) { // Max size 100
	                callLog.remove(0);
	            }
	        }
        }
        
    }

    /**
     * Attempt to locate a configured URL for the specified service.
     * <p/>
     * First checks if an explicit URL is defined via <service name>.url in the configuration file.
     * If no such URL is defined, attempts to construct a URL using <service base>.baseUrl property. 
     * 
     * @param service
     * @return
     * @throws RESTConfigurationException
     * @throws RESTException
     */
    private static String getServiceUrl(String service) throws RESTConfigurationException, RESTException {
        if(testMode)
            return service;
        
        String serviceUrl = config.getProperty(service+".url");
        if(serviceUrl == null) {
            // See if we can find the "base url"
            String [] serviceUrlParts = service.split("\\.");
            if(serviceUrlParts.length == 2) {
                String baseUrl = config.getProperty(serviceUrlParts[0]+".baseUrl");
                if(baseUrl!=null) {
                    serviceUrl = baseUrl + serviceUrlParts[1]+ ".php";
                }
            }
        }
        if(serviceUrl == null) 
            throw new RESTException("Configuration for service " 
                            + service + " could not be found.  Expected configuration value " + service + ".url or <Service>.baseUrl");
        return serviceUrl;
    }   
    
    
    /**
     * Calls a service that returns JSON
     * @param authToken
     * @param service
     * @param params
     * @return
     * @throws RESTException
     */
    public static JSONObject callJSON(String authToken, String service, String... params) throws RESTException {
        initSSL();
         
        RestCall call = null;
        try {
            String serviceUrl = getServiceUrl(service);

            call = new RestCall(serviceUrl,authToken, params);
            call.service = service;
            return call.fetchJSONResponse();
        }
        catch (RESTConfigurationException e) {
            if(call !=null)call.setExceptionResult(e);
            throw new RESTException("Call to service " + service + " with parameters " + join(params,",") + " failed", e);
        }
        catch (RuntimeException e) {
            if(call !=null)call.setExceptionResult(e);
            throw e;
        }
        finally {
            if(config.getProperty("EnableRestLog",false)) {
                callLog.add(call);
                if(callLog.size()>100) { // Max size 100
                    callLog.remove(0);
                }
            }
        }
        
    }

    /**
     * Attempts to resolve the URL for the given service from the configuration file.
     * 
     * @param serviceName
     * @return
     * @throws RESTConfigurationException
     * @throws RESTException
     */
    public static String resolveServiceUrl(String serviceName) throws RESTConfigurationException, RESTException {
        String serviceUrl = config.getProperty(serviceName+".url");
        if(serviceUrl == null) {
            throw new RESTException("Configuration for service " 
                            + serviceName + " could not be found.  Expected configuration value " + serviceName + ".url");
        }
        return serviceUrl;
    }  
    /**
     * Executes the given url and returns the result as parsed XML in the
     * form of a JDOM Document
     * 
     * @param url
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static Document executeUrl(String url) throws JDOMException, IOException {
        
        String resultXML = null;
        if(testMode) {
            resultXML = testCallResults.remove(0).toString();
        }
        else {
            long startTime = System.currentTimeMillis();
            log.info("Sending REST Request: " + url);
            
            initSSL();
            
            StringWriter result = fetchUrlResponse(url);      
            
            if(log.isDebugEnabled()) {
                log.debug("Received from Central: executeUrl elaspsed " + (System.currentTimeMillis() - startTime) + 
                                "msec\n" + result.toString());
            }
            resultXML = result.toString();
        }
        
        Document document = null;
        try {
            document = new SAXBuilder().build(new StringReader(resultXML));
        }
        catch (JDOMException e) {
            log.error("Invalid XML returned to REST call: " + resultXML);
            throw e;
        }
        
        // Parse the result XML 
        return document;
    }

    /**
     * @param url
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    public static StringWriter fetchUrlResponse(String url) throws IOException, MalformedURLException {
        
        StringWriter result = new StringWriter(200);
        
        if(testResponse != null) {
            result = new StringWriter(testResponse.length());
            result.append(testResponse);
            testResponse = null;
            return result;
        }
        
        // Check cache
        CacheEntry entry = cache.get(url);
        if(entry != null) {
            if(entry.expiry > System.currentTimeMillis()) {
                log.info("Returning cached response");
                result.write(entry.response);
                return result;
            }
        }
        
        byte [] buffer = new byte[1024];
        URLConnection urlConn = new URL(url).openConnection();
        String nodeKey = getKey();
        if(!blank(nodeKey))
	        urlConn.setRequestProperty("X-MedCommons-Key", nodeKey);
        
        try {
            InputStream in = urlConn.getInputStream();
            int bytes = 0;
            while((bytes = in.read(buffer)) > 0) {
                result.write(new String(buffer, 0, bytes));
            }         
            in.close();
            
            if(config.getProperty("LogRESTResponses", false))
                log.info("Received response: " + result.toString());
            
            if(urlConn instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                if(httpConn.getHeaderField("Cache-Control") != null) {
                    long ageSeconds = parseMaxAge(httpConn.getHeaderField("Cache-Control"));
                    long expiry = System.currentTimeMillis()+ageSeconds*1000;
                    cacheEntry(url, new CacheEntry(expiry, result.toString()));
                    log.debug("Cached response for URL " + url + " with max-age " + ageSeconds);
                }
                if(httpConn.getResponseCode() >= 400)
                    throw new IOException("Call to URL " + url + " returned response code " + httpConn.getResponseCode());
            } 
        }
        catch(IOException ex) {
            // Annoyingly, the underlying code throws exceptions that do not contain the response message
            // which can be invaluable in figuring out what went wrong
            // So - we "fix" it
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            throw new IOException(ex.getMessage() + " Message = " + httpConn.getResponseMessage(), ex);
        }
        
        return result;
    }    
    
    private synchronized static void cacheEntry(String url, CacheEntry entry) {
        while(cache.size() > 200) {
            cache.remove(internalCache.lastKey());
        }
        cache.put(url, entry);
    }


    /**
     * Parses a HTTP 1.1 header field of the form:
     * <p>
     * Cache-Control: max-age=3600
     * <p>
     * to return the maximum age of the header field.
     */
    protected static long parseMaxAge(String headerField) {
        
        String [] directives = headerField.trim().split(",");
        for(String dir : directives) {
            String [] parts = dir.trim().split("=");
            if(parts.length<2) {
                continue;
            }
            
            if(!"max-age".equals(parts[0]))
                continue;
            
            try {
                return Long.parseLong(parts[1].trim());
            } 
            catch (NumberFormatException e) {
                log.warn("Unable to parse cache control age: " + parts[1].trim());
                return -1L;
            }
        }
        return -1L;
    }


    public static String getSummmaryStatus(Document doc){
        if(doc == null)
            return "unknown";
        
    	String status = doc.getRootElement().getChildText("summary_status");
    	return(status);
    }
    
    public static String getKey() {
        if(key.get() == null) {
            File f = new File("data/node_key.properties");
            if(f.exists()) {
                Properties p = new Properties();
                FileInputStream in  = null;
                try {
                    in = new FileInputStream(f);
                    p.load(in);
	                key.set(p.getProperty("key"));
                } 
                catch (IOException e) {
                    key.set("");
                    throw new IllegalStateException("Key file " + f + " exists but not able to be read / parsed", e);
                }
                finally {
                    if(in != null)
                        try { in.close(); } catch (IOException e) { e.printStackTrace(); }
                }
            }
        }
        return key.get();
    }
    
}
