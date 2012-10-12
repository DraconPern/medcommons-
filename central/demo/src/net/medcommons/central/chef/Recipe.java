/*
 * $Id: Recipe.java 68 2004-05-11 17:42:15Z mquigley $
 */

package net.medcommons.central.chef;

import java.util.HashMap;

public interface Recipe {

  public void cook(HashMap parameters) throws RecipeException;

}
