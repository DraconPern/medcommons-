/*
 * $Id: RecipeException.java 68 2004-05-11 17:42:15Z mquigley $
 */

package net.medcommons.central.chef;

public class RecipeException extends Exception {

  public RecipeException() {
    super();
  }
  
  public RecipeException(String msg) {
    super(msg);
  }
  
  public RecipeException(String msg, Throwable t) {
    super(msg, t);
  }

}
