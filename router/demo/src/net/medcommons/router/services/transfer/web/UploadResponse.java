/*
 * Created on Nov 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.transfer.web;

/**
 * Response status. The current status messages are 
 * too loosely defined. Pass status and error messages back
 * up the chain so they can be handled more centrally.
 * 
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UploadResponse {
  public boolean success = false;
  public int httpStatus = -1;
  public String statusMessage = null;
  public Exception exception = null;
  

}
