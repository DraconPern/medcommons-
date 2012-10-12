/*
 * $Id: GuidService.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.guid;

import org.apache.log4j.Logger;

import net.medcommons.central.guid.GuidFactory;

public class GuidService {

  private static Logger log = Logger.getLogger(GuidService.class);

  public String allocateGuid() throws GuidServiceException {
    try {
      GuidFactory factory = GuidFactory.getInstance();
      return factory.allocateGuid();
      
    } catch(Exception e) {
      throw new GuidServiceException("Error allocating guid: " + e.toString(), e); 
    }
  }

}
