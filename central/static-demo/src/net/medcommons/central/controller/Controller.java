/*
 * $Id: Controller.java 68 2004-05-11 17:42:15Z mquigley $
 */

package net.medcommons.central.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.medcommons.central.guid.GuidFactory;
import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;

import org.apache.log4j.Logger;

public class Controller {
  
  private HashMap pendingMap;
  private HashMap completeMap;

  private Logger log = Logger.getLogger(Controller.class);

  public static String JNDI_NAME = "medcommons/Controller";

  public Controller() {
    pendingMap = new HashMap();
    completeMap = new HashMap();
    
    log.info("Created new Controller instance.");
  }  
  
  public synchronized String pushRemoteCommand(String routerGuid, RemoteCommand cmd) throws Exception {
    String rcGuid = GuidFactory.getInstance().allocateGuid();
    cmd.setGuid(rcGuid);
    
    if(!pendingMap.containsKey(routerGuid)) {
      pendingMap.put(routerGuid, new ArrayList()); 
    }
    
    ArrayList commandList = (ArrayList) pendingMap.get(routerGuid);
    commandList.add(cmd);
    pendingMap.put(routerGuid, commandList);
    
    return rcGuid;
  }
  
  public synchronized RemoteCommand[] popRemoteCommandsForExecution(String routerGuid) {
    if(!pendingMap.containsKey(routerGuid)) {
      pendingMap.put(routerGuid, new ArrayList()); 
    }
    
    ArrayList commandList = (ArrayList) pendingMap.get(routerGuid);
    
    RemoteCommand[] cmdArr = new RemoteCommand[commandList.size()];
    for(int i = 0; i < commandList.size(); i++) {
      cmdArr[i] = (RemoteCommand) commandList.get(i);
    }

    commandList.clear();
    pendingMap.put(routerGuid, commandList);
        
    return cmdArr;
  }
  
  public synchronized void pushRemoteCommandResult(String deviceGuid, RemoteCommandResult result) {
    if(!completeMap.containsKey(deviceGuid)) {
      completeMap.put(deviceGuid, new HashMap()); 
    }
    
    HashMap deviceCompletedMap = (HashMap) completeMap.get(deviceGuid);
    deviceCompletedMap.put(result.getGuid(), result);
    completeMap.put(deviceGuid, deviceCompletedMap);
  }
  
  public synchronized boolean checkRemoteCommandResult(String deviceGuid, String resultGuid) {
    if(!completeMap.containsKey(deviceGuid)) {
      completeMap.put(deviceGuid, new HashMap()); 
    } 
    
    HashMap deviceCompletedMap = (HashMap) completeMap.get(deviceGuid);
    return deviceCompletedMap.containsKey(resultGuid);
  }
  
  public synchronized RemoteCommandResult popRemoteCommandResult(String deviceGuid, String resultGuid) throws ControllerException {
    if(!checkRemoteCommandResult(deviceGuid, resultGuid)) {
      throw new ControllerException("No such result: (deviceGuid: " + deviceGuid + ")(resultGuid: " + resultGuid + ")"); 
    } 
    
    HashMap deviceCompletedMap = (HashMap) completeMap.get(deviceGuid);
    RemoteCommandResult result = (RemoteCommandResult) deviceCompletedMap.get(resultGuid);
    deviceCompletedMap.remove(resultGuid);
    completeMap.put(deviceGuid, deviceCompletedMap);
    
    return result;
  } 
  
  public static Controller getInstance() throws NamingException {
    InitialContext ctx = new InitialContext();
    return (Controller) ctx.lookup(JNDI_NAME);
  }

}
