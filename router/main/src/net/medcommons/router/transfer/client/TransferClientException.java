/*
 * $Id: TransferClientException.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.transfer.client;

public class TransferClientException extends Exception {
  
  public TransferClientException() {
    super();
  }
  
  public TransferClientException(String msg) {
    super(msg);
  }
  
  public TransferClientException(String msg, Throwable t) {
    super(msg, t);
  }

}
