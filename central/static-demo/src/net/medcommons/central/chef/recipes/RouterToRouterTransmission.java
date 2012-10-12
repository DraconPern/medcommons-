/*
 * $Id: RouterToRouterTransmission.java 70 2004-05-12 03:20:31Z mquigley $
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
    String sourceRouterGuid = (String) parameters.get("sourceRouterGuid");
    String targetRouterGuid = (String) parameters.get("targetRouterGuid");
    String packageGuid = (String) parameters.get("packageGuid");    
    if(sourceRouterGuid == null || targetRouterGuid == null || packageGuid == null) {
      String msg = "Missing required parameters: ";
      msg += "(sourceRouterGuid: " + sourceRouterGuid + ")";
      msg += "(targetRouterGuid: " + targetRouterGuid + ")";
      msg += "(packageGuid: " + packageGuid + ")";
      
      throw new RecipeException(msg); 
    }
    
    try {
      log.info("Recipe beginning.");
      
      String fetchCmdGuid = fetchPackageFromSource(sourceRouterGuid, packageGuid);
      log.info("Sent request to transmit package from router to central.");
  
      log.info("Waiting for transmission to successfully complete.");    
      waitForCommandComplete(sourceRouterGuid, fetchCmdGuid);
      log.info("Transmission completed successfully.");
  
      String storeCmdGuid = storePackageOnTarget(targetRouterGuid, packageGuid);
      log.info("Sent request to transmit package from central to router.");
      
      log.info("Waiting for transmission to successfully complete.");
      waitForCommandComplete(targetRouterGuid, storeCmdGuid);
      log.info("Tranmission completed successfully.");
      
      log.info("Recipe completed successfully.");
      
    } catch(Exception e) {
      throw new RecipeException("Recipe invocation failed: " + e.toString(), e); 
    }
    
  }
  
  private String fetchPackageFromSource(String sourceRouterGuid, String packageGuid) throws Exception {
    RemoteCommand cmd = new RemoteCommand();
    cmd.setOperation(RemoteCommand.TRANSMIT_PACKAGE_TO_CENTRAL_OPERATION);
    cmd.addParameter("packageGuid", packageGuid);
    
    Controller ctrl = Controller.getInstance();
    String cmdGuid = ctrl.pushRemoteCommand(sourceRouterGuid, cmd);
    
    log.info("Pushed command to router (sourceRouterGuid: " + sourceRouterGuid + "): " + cmd.toString());
    
    return cmdGuid;
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
  
  private String storePackageOnTarget(String targetRouterGuid, String packageGuid) throws Exception {
    RemoteCommand cmd = new RemoteCommand();
    cmd.setOperation(RemoteCommand.TRANSMIT_PACKAGE_TO_ROUTER_OPERATION);
    cmd.addParameter("packageGuid", packageGuid);
        
    Controller ctrl = Controller.getInstance();
    String cmdGuid = ctrl.pushRemoteCommand(targetRouterGuid, cmd);
    
    log.info("Pushed command to router (targetRouterGuid: " + targetRouterGuid + "): " + cmd.toString());
    
    return cmdGuid;  
  }

}
