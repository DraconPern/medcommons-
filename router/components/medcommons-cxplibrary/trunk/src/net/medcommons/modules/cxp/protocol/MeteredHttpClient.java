package net.medcommons.modules.cxp.protocol;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;
import org.codehaus.xfire.transport.http.HttpChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MeteredHttpClient extends HttpClient {

	 private static final String GZIP_CONTENT_ENCODING = "gzip";

	    public static final String DISABLE_KEEP_ALIVE = "disable-keep-alive";
	    public static final String DISABLE_EXPECT_CONTINUE = "disable.expect-continue";
	    public static final String HTTP_CLIENT_PARAMS = "httpClient.params";
	    public static final String USER_AGENT =  
	        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; XFire Client +http://xfire.codehaus.org)";
	    public static final String HTTP_PROXY_HOST = "http.proxyHost";
	    public static final String HTTP_PROXY_PORT = "http.proxyPort";
	    public static final String HTTP_PROXY_USER = "http.proxy.user";
	    public static final String HTTP_PROXY_PASS = "http.proxy.password";
	    public static final String HTTP_STATE = "httpClient.httpstate";
	    public static final String HTTP_CLIENT = "httpClient";
	    public static final String HTTP_TIMEOUT = "http.timeout";
	    public static final String GZIP_REQUEST_ENABLED = "gzip.request.enabled";

	    private static final int DEFAULT_MAX_CONN_PER_HOST = 6;

	    public  static final String MAX_CONN_PER_HOST = "max.connections.per.host";

	    public  static final String MAX_TOTAL_CONNECTIONS = "max.total.connections";

	    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS;

	    private static final Log log = LogFactory.getLog(CommonsHttpMessageSender.class);

	        public static final String HTTP_HEADERS = "http.custom.headers.map";
	    
	        
	        public static final String DISABLE_PROXY_UTILS = "http.disable.proxy.utils";

	        public static final String PROXY_UTILS_CLASS = "proxy.utils.class";
	        
	        private static final String DEFAULT_PROXY_UTILS_CLASS = "org.codehaus.xfire.transport.http.ProxyUtils";

	private long bytesIn = 0;
	private long bytesOut = 0;
	public MeteredHttpClient(MultiThreadedHttpConnectionManager manager){
		super(manager);
	}
	public MeteredHttpClient(){
		super();
	
	}
	
	public void resetBytesIn(){
		bytesIn = 0;
	}
	public void resetBytesOut(){
		bytesOut = 0;
	}
	public long getBytesIn(){
		
		return(bytesIn);
	}
	public long getBytesOut(){
		return(bytesOut);
	}
	/*
	public MeteredHttpClient createClient(){
		 MeteredHttpClient client = new MeteredHttpClient();
         MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
         HttpConnectionManagerParams conParams = new HttpConnectionManagerParams (); 
         manager.setParams(conParams);
         int maxConnPerHost = getIntValue(MAX_CONN_PER_HOST, DEFAULT_MAX_CONN_PER_HOST);
         conParams.setDefaultMaxConnectionsPerHost(maxConnPerHost );
         int maxTotalConn  = getIntValue(MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS);
         conParams.setMaxTotalConnections(maxTotalConn);
         client.setHttpConnectionManager(manager);
         ((HttpChannel) getMessage().getChannel()).setProperty(HTTP_CLIENT, client);
	}
	*/
}
