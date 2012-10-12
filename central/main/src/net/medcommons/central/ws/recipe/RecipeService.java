/*
 * $Id: RecipeService.java 61 2004-05-06 19:00:59Z mquigley $
 */

package net.medcommons.central.ws.recipe;

import net.medcommons.recipe.RecipeInvocation;
import net.medcommons.recipe.RecipeInvocationResult;

import org.apache.log4j.Logger;

public class RecipeService {

  private static Logger log = Logger.getLogger(RecipeService.class);

  public RecipeService() {
    log.info("Created new RecipeService instance."); 
  }
  
  public RecipeInvocationResult submitRecipeInvocation(RecipeInvocation ri) throws RecipeServiceException {
    log.info(ri.toString());
    
    RecipeInvocationResult ris = new RecipeInvocationResult();
    ris.setSuccess(false);
    ris.setMessage("This service is not yet functional.");
    
    return ris;
  }

}
