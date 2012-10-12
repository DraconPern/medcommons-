/*
 * $Id: Chef.java 70 2004-05-12 03:20:31Z mquigley $
 */

package net.medcommons.central.chef;

import net.medcommons.central.chef.recipes.RouterToRouterTransmission;
import net.medcommons.recipe.RecipeInvocation;
import net.medcommons.recipe.RecipeInvocationResult;

import org.apache.log4j.Logger;

public class Chef {

  public static final String JNDI_NAME = "medcommons/Chef";
  private static Logger log = Logger.getLogger(Chef.class);

  public Chef() {
    log.info("Created new Chef instance."); 
  }
  
  public RecipeInvocationResult invoke(RecipeInvocation invocation) throws ChefException {
    try {
      if("RouterToRouterTransmisssion".equals(invocation.getRecipeName())) {
        Recipe r = new RouterToRouterTransmission();
        r.cook(invocation.getParameters()); 
      }
      
    } catch(Exception e) {
      throw new ChefException("Error preparing your fine cuisine: " + e.toString(), e); 
    }
  
    return null; 
  }

}
