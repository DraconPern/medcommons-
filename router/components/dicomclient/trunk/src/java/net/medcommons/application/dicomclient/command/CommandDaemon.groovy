package net.medcommons.application.dicomclient.command

import java.util.concurrent.Future;

import javax.net.ssl.SSLHandshakeException;

import net.medcommons.application.dicomclient.http.CommandServlet;
import org.json.JSONObject;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.CommandBlock;
import net.medcommons.application.dicomclient.utils.CommandInterpreter;
import net.medcommons.application.utils.JSONSimpleGET;

/**
 * Background thread that polls for commands from the server for this DDL
 * 
 * @author ssadedin
 */
class CommandDaemon implements Runnable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CommandDaemon.class);

    /**
     * Interval between polls
     */
    public final static long POLL_INTERVAL = 500l
    
    /**
     * Interval when there is an error in a poll
     */
    public final static long POLL_ERROR_INTERVAL = 5000l
    
    /**
     * Timeout for socket connections
     * <p>
     * This prevents a "hang" on the connection or server from permanently
     * disabling polling (a problem observed in practice).  The value is chosen
     * to be more than the long polling timeout on the server side
     * (60 seconds, currently).
     */
    public final static int POLL_TIMOUT_MS = 65000
    
    
    /**
     * Whether polling should be active or not
     */
    private static boolean active = true
    
    /**
     * Client for making HTTP calls
     */
    JSONSimpleGET json = new JSONSimpleGET(POLL_TIMOUT_MS)
    
    /**
     * Main loop - runs forever
     */
    @Override
    public void run() {
        while(true) {
            
            try {
                poll()
            }
            catch(Throwable t) {
                try {
                    log.error "Failure in command poller", t
                    Thread.sleep(POLL_ERROR_INTERVAL)
                }
                catch(Throwable t2) {
                    log.warn("Failed to send error log: " + t2.toString())
                }
            }
            Thread.sleep(POLL_INTERVAL)
        }
        
    }
    
    public static void activate() {
        log.info "Activating command poller"
        active = true
    }
    
    public static void deactivate() {
        log.info "Deactivating command poller"
        active = false
    }
    
    /**
     * Poll the host indicated by the current context state
     */
    void poll() {
        
        if(!active) 
            return 
        
        ContextState ctx = ContextManager.get().currentContextState
        if(!ctx || !ctx.cxpHost) {
            log.info "No server context:  skipping command poll"
            return
        }
        
        Configurations cfg = ContextManager.get().getConfigurations()
        
        String url = "$ctx.gatewayRoot/router/ddl?poll=true&ddlid=${cfg.DDLIdentity}"
            
        JSONObject result = fetchUrl(url)
        if(result.status != "ok") {
            log.error "Command poll failed: " + result.error
            return
        }
        
        final JSONArray commands = result.commands
        log.info "Got ${commands.length()} commands from server"
        
        for(int i=0; i<commands.length(); ++i) {
            executeCommand(commands.get(i))
        }
    }
    
    /**
     * Fetch first with apache http-client, but if that fails, try
     * just using plain Java - this has proven to work around some proxy 
     * issues.
     */
    JSONObject fetchUrl(String url) {
        try {
            return fetchUrlByHttpClient(url)
        }
        catch(Throwable t) {
            log.info("Failed to fetch using http client: " + t.toString())
            try {
                return fetchUrlByJDK(url)
            }
            catch(SSLHandshakeException sslShakeEx) {
                log.info("Native JDK call failed with SSL Handshake error - YOU ARE PROBABLY USING A STARTCOM CERT"+ sslShakeEx.toString())
                throw t;
            }
            catch(Throwable t2) {
                log.info("Failed to fetch using native JDK: " + t2.toString())
                throw t;
            }        }
    }
    
    /**
     * Fetch using Apache Http Client - better but in some environments
     * may not work eg: environments with restrictive proxy settings.
     * 
     * @param url
     * @return
     */
    JSONObject fetchUrlByHttpClient(String url) {
       url = url.replaceAll("^https://", "insecure-https://")
       json.get(url) 
    }
    
    /**
     * Fetch using default JDK apis.   These won't work with StartCom
     * certs and are worse in other ways, however in some environments
     * they may work where Apache HttpClient doesn't (eg: where
     * a restrictive proxy is configured).
     * 
     * @param url
     * @return
     */
    JSONObject fetchUrlByJDK(String url) {
        log.info "Fetching $url using JDK apis"
        
        new JSONObject(new URL(url).text)
    }
    
    /**
     * Execute the given command
     */
    void executeCommand(JSONObject obj) {
        CommandBlock command = extractCommand(obj)
        if(!command)
            throw new IllegalArgumentException("Empty command received: " + obj)
        
		CommandInterpreter interpreter = CommandInterpreter.getCommandInterpreter();
        Future<JSONObject> result = interpreter.invokeCommand(command)
        if(result) 
            CommandServlet.registerCommandResult(obj.windowId, obj.command, result)
    }
    
    /**
     * Convert the given JSON object to a CommandBlock
     */
    CommandBlock extractCommand(JSONObject obj) {
        obj.hashMap.inject(new CommandBlock(obj.command)) { cmd, entry -> 
            cmd.addProperty(entry.key,entry.value)
        }        
    }
}
