/*
 * $Id: RecipeServiceException.java 60 2004-05-06 18:28:34Z mquigley $
 */

package net.medcommons.central.ws.recipe;

public class RecipeServiceException extends Exception {

  public RecipeServiceException() {
    super();
  }
  
  public RecipeServiceException(String msg) {
    super(msg);
  }
  
  public RecipeServiceException(String msg, Throwable t) {
    super(msg, t); 
  }

}
