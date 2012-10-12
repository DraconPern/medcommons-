package net.medcommons.application.dicomclient;

import static net.medcommons.application.utils.Str.blank;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.utils.JSONSimpleGET;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Checks if network connectivity to the gateway is working and if not, 
 * and tries to find a network configuration that works.
 * 
 * @author ssadedin
 */
public class NetworkProber {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(NetworkProber.class);
    
    /**
     * The URL to test
     */
    private String testUrl;

    private String proxyUserName;
    
    private String proxyPassword;
    
    public boolean builtinJavaWorks = false;
    
    public boolean apacheClientWorks = false;

    /**
     * Whether it is only possible to communicate with raw JDK functions rather than
     * Apache HttpClient.
     */
    public boolean jdkOnly;
    
    public NetworkProber() {
        ContextState ctx = ContextManager.get().getCurrentContextState();
        if(ctx == null || blank(ctx.getCxpHost())) {
            log.info("No server context:  skipping network access check");
            return;
        }
        Configurations cfg = ContextManager.get().getConfigurations();
        testUrl = ctx.getGatewayRoot() + "/router/ddl/test";
    }

    /**
     * Probe via several methods to try and find a network configuration that lets
     * us get out to the open internet
     */
    public void probe() {
        try {
            tryProbe();
        }
        catch(Throwable t) {
            log.warn("Probe attempt failed", t);
        }
        
        if(builtinJavaWorks && !apacheClientWorks) {
            log.info("Found that JDK network libraries work but Apache doesn't");
            jdkOnly = true;
        }
    }

    protected void tryProbe() {
        
        // First, check if everything "just works"
        log.info("Checking basic network access");
        if(checkApacheHttpClient()) {
            log.info("Apache client network access test successful");
            apacheClientWorks = true;
        }
            
        // Does default java access work (non-Apache Http Client)
        if(!checkBuiltInJavaAccess()) {
            log.info("Built in Java network access test failed");
            
            // We don't diagnose any further - assume nothing will work
            builtinJavaWorks = false;
        }
        else
            builtinJavaWorks = true;
        
        
        if(apacheClientWorks) {
            log.info("Network access using default configuration successful: ending probe");
            return;
        }
        
        // Apache client not working, there is one remaining hope:
        // perhaps it can be made to work by setting up proxy 
        // Note: raw JDK may be working but we try not to use it for
        // various reasons (buggy, StartSSL certs don't work, etc).
        
        // Is there a proxy configured? If so, we could use it
        InetSocketAddress proxyAddr = getProxyAddress();
        if(proxyAddr == null) {
            log.info("No proxy set.   Nothing left to try");
            return;
        }
                        
        log.info("Found proxy address - attempting to use");
        Configurations cfg = ContextManager.get().getConfigurations();
        if(checkWithBasicProxyNoAuth(proxyAddr)) {
            log.info("Proxy worked - setting as default");
            cfg.setProxyAddress(proxyAddr.getHostName() + ":" + proxyAddr.getPort());
            apacheClientWorks = true;
            try {
                cfg.save(cfg.getConfigurationFile());
                log.info("Saved configuration with new proxy settings");
            }
            catch (IOException e) {
                log.warn("Unable to save configuration file with new proxy settings",e);
            }
            // return;
        }
        
        // Still no luck - maybe we need authentication details?
        proxyUserName = cfg.getProxyAuthUserName();
        proxyPassword = cfg.getProxyAuthPassword();
        if(blank(proxyUserName) || blank(proxyPassword)) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        requestProxyPassword();
                    }
                });
            }
            catch (Exception e1) {
                log.warn("Error while waiting for password response", e1);
                return;
            }
            
            while(proxyUserName == null || proxyPassword == null) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { }
            }
            log.info("Retrieved username " + proxyUserName);
        }
            
        if(!blank(proxyUserName) && !blank(proxyPassword)) {
            if(checkProxyWithAuth(proxyAddr)) {
                apacheClientWorks = true;
                log.info("Saving proxy auth credentials");
                cfg.setProxyAddress(proxyAddr.getHostName() + ":" + proxyAddr.getPort());
                cfg.setProxyAuthUserName(proxyUserName);
                cfg.setProxyAuthPassword(proxyPassword);
                try {
                    cfg.save(cfg.getConfigurationFile());
                    log.info("Saved configuration with new proxy auth settings");
                }
                catch (IOException e) {
                    log.warn("Unable to save configuration file with new proxy auth settings",e);
                }
            }
            else
                log.info("Proxy auth test failed - ignoring user provided credentials");
        }
        else 
            log.info("Proxy credentials not provided");
    }
    
    public void requestProxyPassword() {
        Frame window = new Frame();
        window.setIconImage(StatusDisplayManager.activeImage);

        // Create a modal dialog
        final Dialog d = new Dialog(window, "MedCommons DDL - Please Enter Your Proxy Username and Password", true);
        d.setMaximumSize(new Dimension(200, 300));

        // Use a flow layout
        d.setLayout( new FlowLayout() );
        
        d.add(new JLabel("User Name: "));
        final JTextField username = new JTextField(20);
        d.add(username);
        
        d.add(new JLabel("Password: "));
        final JPasswordField password = new JPasswordField(20);
        d.add(password);
        
        // Create an OK button
        Button ok = new Button ("OK");
        ok.addActionListener (new ActionListener() {
            public void actionPerformed( ActionEvent e )
            {
                proxyUserName = username.getText();
                proxyPassword = new String(password.getPassword());
                d.setVisible(false);
            }
        });

        d.add( ok );

        // Show dialog
        d.pack();
        d.setVisible(true);        
    }

    private boolean checkWithBasicProxyNoAuth(InetSocketAddress proxyAddr) {
        HttpClient client = JSONSimpleGET.createHttpClient(proxyAddr.getHostName()+":"+proxyAddr.getPort(), null, null, 30000);
        GetMethod get = new GetMethod(testUrl);
        try {
            client.executeMethod(get);
            String result = get.getResponseBodyAsString();
            log.info("Successfully retrieved response body : " + result + " using proxy without authentication");
            return true;
        }
        catch(Exception e) {
            log.error("Failed to query using unauthenticated proxy " + proxyAddr, e);
            get.releaseConnection();
            return false;
        }
    }
    
    private boolean checkProxyWithAuth(InetSocketAddress proxyAddr) {
        HttpClient client = JSONSimpleGET.createHttpClient(proxyAddr.getHostName()+":"+proxyAddr.getPort(),
                                                            this.proxyUserName, this.proxyPassword, 30000);
        GetMethod get = new GetMethod(testUrl);
        try {
            client.executeMethod(get);
            String result = get.getResponseBodyAsString();
            log.info("Successfully retrieved response body : " + result + " using proxy authentication");
            return true;
        }
        catch(Exception e) {
            log.error("Failed to query using unauthenticated proxy " + proxyAddr, e);
            get.releaseConnection();
            return false;
        }
    }

    private InetSocketAddress getProxyAddress() {
        
        try {
            System.setProperty("java.net.useSystemProxies", "true");
            List l = ProxySelector.getDefault().select(new URI(testUrl));
            for(Iterator iter = l.iterator(); iter.hasNext();) {
                Proxy proxy1 = (Proxy) iter.next();
                log.info("proxy type : " + proxy1.type());
                InetSocketAddress addr = (InetSocketAddress) proxy1.address();
                if(addr == null) {
                    log.info("No Proxy found in network settings");
                }
                else {
                    log.info("proxy hostname : " + addr.getHostName());
                    log.info("proxy port : " + addr.getPort());
                    return addr;
                }
            }
            log.info("No usable proxy found");
        }
        catch (Exception e) {
            log.error("Failed to probe for proxy",e);
        }
        return null;
    }

    private boolean checkBuiltInJavaAccess() {
        
        try {
            URLConnection conn = new URL(testUrl).openConnection();
            InputStream is = conn.getInputStream();
            byte [] buffer = new byte[1024];
            int n =  is.read(buffer);
            log.info("Successfully read " + n + " bytes from " + testUrl + " as network check");
            return true;
        }
        catch (Throwable t) {
            log.warn("failed to access " + testUrl, t);
            return false;
        }
    }

    /**
     * Look to see if we can get to the server with the current default 
     * settings.
     */
    private boolean checkApacheHttpClient() {
        JSONSimpleGET get = new JSONSimpleGET();
        try {
            
            String url = testUrl;
    	    String https = "https://";
            if(url.startsWith(https))
                url = JSONSimpleGET.HTTPS + "://" + url.substring(https.length());
            JSONObject obj = get.executeMethod(new GetMethod(url));
            return true;
        }
        catch(Throwable t) {
            log.warn("failed to access " + testUrl, t);
            return false;
        }
    }

}
