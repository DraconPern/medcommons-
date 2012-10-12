package net.medcommons.application.dicomclient.utils;

import static net.medcommons.modules.utils.Str.blank;

import java.util.*;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.command.AdminCommand;
import net.medcommons.application.dicomclient.http.action.StatusUpdateActionBean;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.utils.MonitorTransfer;
import net.medcommons.modules.utils.Str;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * CommandInterpreter
 * 
 * Used to execute commands from the dashboard or from another DDL 
 * via the CommandServlet.
 * 
 * @author mesozoic
 *
 */
public class CommandInterpreter {
    
	private static Logger log = Logger.getLogger(CommandInterpreter.class);
	
	private static CommandInterpreter commandInterpreter= null;
	
	private static Map<String,Class<? extends Command>> commands = new HashMap<String, Class<? extends Command>>();
	
	static {
	    register("uploadlog", UploadLogFileCommand.class);
	    register("quickupload", QuickStartUpload.class);
	    register("selectfolder", SelectFolderCommand.class);
	    register("scanfolder", ScanFolderCommand.class);
	    register("uploaddicom", UploadDICOMCommand.class);
	    register("pollgroup", PollGroupCommand.class);
	    register("shutdown", Shutdown.class);
	    register("stoppoller", StopPollerCommand.class);
	    register("echo", EchoCommand.class);
	    register("queryid", QueryIDCommand.class);
	    register("admin", AdminCommand.class);
	}
	
	/**
	 * List of safe commands that anybody can execute.  
	 * <p>
	 * "queryid" is included here even though it is not technically safe because it
	 * enforces safety itself by prompting the user under the right circumstances.
	 */
	public static final Set<String> SAFE_COMMANDS = new HashSet<String>() {{
	    add("ping");
	    add("uploadlog");
	    add("echo");
	    add("queryid");
	    add("start");
	}};
	
	public static void register(String commandName, Class command) {
	    commands.put(commandName, command);
	}
	
	private CommandInterpreter(){
		
	}
	
	public static CommandInterpreter getCommandInterpreter(){
		if (commandInterpreter == null){
			commandInterpreter = new CommandInterpreter();
		}
		return(commandInterpreter);
	}
	
	public Future<JSONObject> invokeCommand(CommandBlock commandBlock){
		String commandName = commandBlock.getCommand();
		if("download".equals(commandName)) {
			download(commandBlock);
		}
		else
		if("start".equals(commandName)) {
		    start(commandBlock);
		}
		else
		if("cleanupload".equals(commandName)) {
		    ClearUtils.clearAll();
		    upload(commandBlock);
		}
		else
		if ("upload".equals(commandName)) {
		    upload(commandBlock);
		}
		else
		if ("cancelUpload".equals(commandName)) {
		    cancelUpload(commandBlock);
		}
		else
		if(commands.containsKey(commandName)) {
		    try {
                return ((Command)commands.get(commandName).newInstance()).execute(commandBlock);
            }
		    catch (InstantiationException e) {
		        throw new RuntimeException("Failed to create command " + commandName,e);
            }
		    catch (IllegalAccessException e) {
		        throw new RuntimeException("Failed to create command " + commandName,e);
            }
		}
		else
		if("ping".equals(commandName)) {
		   // log.info("Received ping from dashboard"); 
		}
		else {
			throw new IllegalArgumentException("Unknown command: " + commandName);
		}
		return null;
	}
	
	private void cancelUpload(CommandBlock commandBlock) {
	    String transferKey = commandBlock.getProperty("transferKey");
	    if(blank(transferKey))
	        throw new IllegalArgumentException("Missing required parameter " + transferKey);
	    
	    // Do the in-memory cancels first
	    MonitorTransfer.cancel(transferKey);
	    DicomFileChooser.cancel(transferKey);
	    
	    // Hack - to avoid race conditions where the import thread may still be creating new db entries 
	    // even after we can cancel above, we just sleep for a short time.  This is really a complete
	    // hack but the race conditions are actually quite hard to deal with so we will
	    // see if this is effective or not.
	    try { Thread.sleep(200); } catch (InterruptedException e) {  }
	    
	    TransactionUtils.cancelTransaction(transferKey);
    }

    private void start(CommandBlock cmd) {
		if(!blank(cmd.getProperty("cxphost"))) {
		    ContextManager contextManager = ContextManager.get();
		    ContextState contextState = cmd.toContextState();
		    contextManager.setCurrentContextState(contextState);
		}
	}
	
	private void upload(CommandBlock cmd) {
	    
	    // The command could be simply triggering the upload or it could
	    // be setting the whole context for the upload at the same time
		if(!blank(cmd.getProperty("cxphost"))) {
		    ContextManager contextManager = ContextManager.get();
		    ContextState contextState = cmd.toContextState();
		    contextManager.setCurrentContextState(contextState);
		}
		
		MonitorTransfer.clear();
		
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            //Turn off metal's use of bold fonts
	            UIManager.put("swing.boldMetal", Boolean.FALSE);
	            StatusDisplayManager.createAndShowCDChooser();
	        }
	    });
	}
	
	private void download(CommandBlock commandBlock){
		ContextManager contextManager = ContextManager.get();
		ContextState contextState = commandBlock.toContextState();
		
		Store db = DB.get();
		
		db.save(contextState);
		
		contextManager.setCurrentContextState(contextState);
		contextManager.downloadGuid(true, contextState);
	}
}
