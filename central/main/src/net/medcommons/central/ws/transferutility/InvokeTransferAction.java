/*
 * $Id: InvokeTransferAction.java 86 2004-07-13 03:38:51Z mquigley $
 */
package net.medcommons.central.ws.transferutility;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.central.chef.Chef;
import net.medcommons.recipe.RecipeInvocation;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class InvokeTransferAction extends Action {
  
  private static Logger log = Logger.getLogger(InvokeTransferAction.class);
  
  public ActionForward execute(ActionMapping aMapping, ActionForm aForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
    log.info("Invoked.");

    ArbitraryTransferForm atf = (ArbitraryTransferForm) aForm;   
    
    HashMap recipeParams = new HashMap();
    recipeParams.put("sourceDeviceGuid", atf.getSourceDeviceGuid());
    recipeParams.put("destinationDeviceGuid", atf.getDestinationDeviceGuid());
    recipeParams.put("folderGuid", atf.getFolderGuid());
    
    RecipeInvocation ri = new RecipeInvocation();
    ri.setRecipeName("RouterToRouterTransmisssion");
    ri.setParameters(recipeParams);
    
    Chef chef = Chef.getInstance();
    chef.invoke(ri);
    
    return aMapping.findForward("success");
  }
}
