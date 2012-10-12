/*
 * Created on Nov 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.transfer.web;

/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

class DataAlreadyExistsException extends Exception{
  String description = null;
  public DataAlreadyExistsException(String description){
    this.description = description;
  }
  public String toString(){
    return("DataAlreadyExistsException:" + description);
  }
}