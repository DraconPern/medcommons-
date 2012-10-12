package net.medcommons.application.dicomclient.http;

import static net.medcommons.application.utils.Str.bvl;
import static net.medcommons.application.utils.Str.nvl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.application.dicomclient.utils.*;
import net.medcommons.application.utils.MonitorTransfer;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Accepts Command
 * @author mesozoic
 *
 */
public class CommandServlet extends HttpServlet {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CommandServlet.class);
    
    /**
     * List of asynchronous events to send back to callers
     */
	private static Vector<String> events = new Vector<String>();

	private static HashMap<String, HashMap<String, Future<JSONObject>>> commands = new HashMap<String, HashMap<String,Future<JSONObject>>>();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	    throws ServletException, IOException {
	    
		// log.info("=====Command====");
        response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
        response.setHeader("Pragma","no-cache"); // HTTP 1.0
		response.setContentType("text/javascript");
		response.setStatus(HttpServletResponse.SC_OK);
		JSONObject result = new JSONObject();
		String windowId = nvl(request.getParameter("windowId"),"legacy");
		try{
			Map<String, String[]> parameters = request.getParameterMap();
			CommandBlock commandBlock = new CommandBlock(parameters);
			
			if(!CommandInterpreter.SAFE_COMMANDS.contains(commandBlock.getCommand())) {
			    UI.get().verifySafeCommand(commandBlock);
			}
			
			// log.info("Accepted command " + commandBlock.getCommand());
			CommandInterpreter interpreter = CommandInterpreter.getCommandInterpreter();
			Future<JSONObject> commandResult = interpreter.invokeCommand(commandBlock);
			JSONObject commandResults = 
			    getCompletedCommands(windowId, commandBlock, commandResult);			
			if(commandResults != null) {
				result.put("result", commandResults);
			}
			
			result.put("transfers", MonitorTransfer.getCurrentTransfersJSON());
			result.put("imports", DicomFileChooser.getImportsJSON());
			if(PollGroupCommand.getRunning() != null) { 
			    result.put("pollers", new JSONArray().put(PollGroupCommand.getRunning().getCtx().toJSON()));
			}
			result.put("version",System.getProperty("ddl.version"));
			synchronized(events) {
				result.put("events", Str.join(events, ","));
				events.clear();
			}
         	result.put("status","ok");
		}
		catch(Exception e){
			log.error("CommandServlet error", e);
			result.put("status","failed");
			result.put("error", e.getMessage());
			StatusDisplayManager.get().setErrorMessage("Command Failed", e.getMessage());
		}
		String jsonp = bvl(request.getParameter("jsonp"),"");
		String json = result.toString();
        log.info(request.getParameter("command") + "=>" + json);
		response.getWriter().println(jsonp + "(" + json + ")");
		response.flushBuffer();
	}

    private JSONObject getCompletedCommands(String windowId, CommandBlock commandBlock, Future<JSONObject> commandResult)
            throws InterruptedException, ExecutionException {
        
        JSONObject commandResults = null;
        synchronized(commands) {
            
            if(!commands.containsKey(windowId)) 
                commands.put(windowId, new HashMap<String, Future<JSONObject>>());
            
            HashMap<String, Future<JSONObject>> winCmds = commands.get(windowId);
            
        	if(commandResult != null && !commandResult.isDone()) {
        	    log.info("Registering command " + commandBlock.getCommand() + " for window " + windowId);
        		winCmds.put(commandBlock.getCommand(), commandResult);
        	}
        	
        	List<String> remove = new ArrayList<String>();
        	for (Map.Entry<String, Future<JSONObject>> entry : winCmds.entrySet()) {
    			if("ping".equals(commandBlock.getCommand())) {
                    if(entry.getValue().isDone()) {
                        if(commandResults == null)
                            commandResults = new JSONObject();
                        
                        commandResults.put(entry.getKey(), entry.getValue().get());
                        remove.add(entry.getKey());
                    }
                    else
                        log.info("Command " + entry.getKey() + " not done yet");
    			}
            }
        	
        	if(!remove.isEmpty()) {
        	    log.info("Found " + remove.size() + " completed commands for window " + windowId);
        	    for (String commandName : remove) {
        	        winCmds.remove(commandName);
        	    }
            }
        	
        	if(commandResult != null && commandResult.isDone()) {
        	    if(commandResults == null)
        	        commandResults = new JSONObject();
        	    
        	    commandResults.put(commandBlock.getCommand(), commandResult.get());
        	}
        }
        return commandResults;
    }
    
    /**
     * Register the given Future as a command result that should be returned to 
     * a polling browser when it completes
     */
    public static void registerCommandResult(String windowId, String commandName, Future<JSONObject> commandResult) {
        synchronized(commands) {
            
            if(!commands.containsKey(windowId)) 
                commands.put(windowId, new HashMap<String, Future<JSONObject>>());
            
            HashMap<String, Future<JSONObject>> winCmds = commands.get(windowId);
            
        	if(commandResult != null) {
        	    log.info("Registering command " + commandName + " for window " + windowId);
        		winCmds.put(commandName, commandResult);
        	}
        }
    }
	
    /**
	 * Add an event to be reported back to the next poller that calls
	 * <p>
	 * Note that if multiple pollers are calling then there is no guarantee
	 * which of them will receive the event.  This may be problematic
	 * in scenarios where multiple windows are open and polling the DDL.
	 * Instead we should really make callers specify a unique 'cookie' 
	 * that we can use to make sure every caller receives the event.
	 */
	public static void signal(String event) {
	    synchronized(events) {
		    events.add(event);
	    }
	}
	
}