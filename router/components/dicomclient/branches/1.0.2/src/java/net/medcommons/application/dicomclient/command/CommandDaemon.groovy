package net.medcommons.application.dicomclient.command

import java.util.concurrent.Future;

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
    public final static long POLL_INTERVAL = 1000l
    
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
                log.error "Failure in command poller", t
                Thread.sleep(POLL_ERROR_INTERVAL)
            }
            Thread.sleep(POLL_INTERVAL)
        }
        
    }
    
    /**
     * Poll the host indicated by the current context state
     */
    void poll() {
        ContextState ctx = ContextManager.get().currentContextState
        if(!ctx || !ctx.cxpHost) {
            log.info "No server context:  skipping command poll"
            return
        }
        
        Configurations cfg = ContextManager.get().getConfigurations()
        
        String url = "$ctx.gatewayRoot/router/ddl?poll=true&ddlid=${cfg.DDLIdentity}"
            
        url = url.replaceAll("^https://", "insecure-https://")
            
        JSONObject result = json.get(url)
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
