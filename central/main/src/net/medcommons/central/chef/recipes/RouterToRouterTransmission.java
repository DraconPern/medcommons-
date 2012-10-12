/*
 * $Id: RouterToRouterTransmission.java 86 2004-07-13 03:38:51Z mquigley $
 */

package net.medcommons.central.chef.recipes;

import java.util.HashMap;

import javax.naming.NamingException;

import net.medcommons.central.chef.Recipe;
import net.medcommons.central.chef.RecipeException;
import net.medcommons.central.controller.Controller;
import net.medcommons.central.controller.ControllerException;
import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;

import org.apache.log4j.Logger;

public class RouterToRouterTransmission implements Recipe {

  private static Logger log = Logger.getLogger(RouterToRouterTransmission.class);

  public void cook(HashMap parameters) throws RecipeException {
    log.info("Starting.");
    
    // Get required parameters.
    String sourceDeviceGuid = (String) parameters.get("sourceDeviceGuid");
    String destinationDeviceGuid = (String) parameters.get("destinationDeviceGuid");
    String folderGuid = (String) parameters.get("folderGuid");    
    if(sourceDeviceGuid == null || destinationDeviceGuid == null || folderGuid == null) {
      String msg = "Missing required parameters: ";
      msg += "(sourceDeviceGuid: " + sourceDeviceGuid + ")";
      msg += "(destinationDeviceGuid: " + destinationDeviceGuid + ")";
      msg += "(folderGuid: " + folderGuid + ")";
      
      throw new RecipeException(msg); 
    }
    
    try {
      log.info("Recipe beginning.");
      
      String sendCmd = sendFolder(sourceDeviceGuid, destinationDeviceGuid, folderGuid);
      
      log.info("Recipe completed successfully.");
      
    } catch(Exception e) {
      throw new RecipeException("Recipe invocation failed: " + e.toString(), e); 
    }
    
  }
    
  private String sendFolder(String sourceDeviceGuid, String destinationDeviceGuid, String folderGuid) throws Exception {
    RemoteCommand cmd = new RemoteCommand();
    cmd.setOperation(RemoteCommand.SEND_FOLDER_OPERATION);
    cmd.addParameter("folderGuid", folderGuid);
    cmd.addParameter("destinationDeviceGuid", destinationDeviceGuid);
    
    return Controller.getInstance().pushRemoteCommand(sourceDeviceGuid, cmd);
  }
  
  private void waitForCommandComplete(String deviceGuid, String cmdGuid) throws RecipeException {
    try {
      Controller ctrl = Controller.getInstance();
      
      for(int i = 0; i < 100; i++) {
        Thread.sleep(1000);
        if(ctrl.checkRemoteCommandResult(deviceGuid, cmdGuid)) {
          RemoteCommandResult result = ctrl.popRemoteCommandResult(deviceGuid, cmdGuid);
          if(!result.isSuccess()) {
            throw new RecipeException("Command failed: " + result.toString()); 
          } else {
            return;
          }
        }
      }
    } catch(NamingException ne) {
      throw new RecipeException("Error fetching the Controller: " + ne.toString(), ne);
    } catch(ControllerException ce) {
      throw new RecipeException("Error accessing the Controller: " + ce.toString(), ce);
    } catch(InterruptedException ie) {
      throw new RecipeException("Interrupted."); 
    }
  }
  
}
