/*
 * Created on Nov 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.transfer;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Static codes used to signal HTTP transfer status.
 * @author sean
 *
 */
public class TransferStatus {
  
  /**
   * Transfer successful.
   */
  public final static int OK = HttpStatus.SC_OK;
  /**
   * Duplicate fixed content sent to server.
   */
  public final static int DUPLICATE = 299;
}
