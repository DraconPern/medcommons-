package net.medcommons.application.dicomclient.utils;

import static net.medcommons.modules.utils.Str.blank;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;

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
	private static Logger log = Logger.getLogger(CommandInterpreter.class.getName());
	private static CommandInterpreter commandInterpreter= null;
	
	private CommandInterpreter(){
		
	}
	
	public static CommandInterpreter getCommandInterpreter(){
		if (commandInterpreter == null){
			commandInterpreter = new CommandInterpreter();
		}
		return(commandInterpreter);
	}
	
	public void invokeCommand(CommandBlock commandBlock){
		String commandName = commandBlock.getCommand();
		if ("download".equals(commandName)) {
			download(commandBlock);
		}
		else
		if ("start".equals(commandName)) {
		    start(commandBlock);
		}
		else
		if ("upload".equals(commandName)) {
		    upload(commandBlock);
		}
		else
		if ("ping".equals(commandName)) {
		   log.info("Received ping from dashboard"); 
		}
		else {
			log.error("Unknown command:" + commandBlock);
		}
	}
	
	private void start(CommandBlock cmd) {
		if(!blank(cmd.getProperty("cxphost"))) {
		    ContextManager contextManager = ContextManager.getContextManager();
		    ContextState contextState = cmd.toContextState();
		    contextManager.setCurrentContextState(contextState);
		}
	}
	
	private void upload(CommandBlock cmd) {
	    
	    // The command could be simply triggering the upload or it could
	    // be setting the whole context for the upload at the same time
		if(!blank(cmd.getProperty("cxphost"))) {
		    ContextManager contextManager = ContextManager.getContextManager();
		    ContextState contextState = cmd.toContextState();
		    contextManager.setCurrentContextState(contextState);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            //Turn off metal's use of bold fonts
	            UIManager.put("swing.boldMetal", Boolean.FALSE);
	            StatusDisplayManager.createAndShowCDChooser();
	        }
	    });
	}
	
	private void download(CommandBlock commandBlock){
		ContextManager contextManager = ContextManager.getContextManager();
		ContextState contextState = commandBlock.toContextState();
		
		contextState = TransactionUtils.saveTransaction(contextState);
		contextManager.setCurrentContextState(contextState);
		contextManager.downloadGuid(true, contextState);
		if (Str.blank(contextState.getStorageId())){
			throw new IllegalArgumentException("storageid must not be blank or null");
		}
		if (Str.blank(contextState.getAuth())){
			throw new IllegalArgumentException("auth must not be blank or null");
		}
		if (Str.blank(contextState.getGuid())){
			throw new IllegalArgumentException("guid must not be blank or null");
		}
		if (Str.blank(contextState.getCxpHost())){
			throw new IllegalArgumentException("cxphost must not be blank or null");
		}
		if (Str.blank(contextState.getAccountId())){
			throw new IllegalArgumentException("accountid must not be blank or null");
		}
	}
}
