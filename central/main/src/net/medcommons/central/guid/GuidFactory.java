/*
 * $Id: GuidFactory.java 71 2004-05-12 04:02:33Z mquigley $
 */

package net.medcommons.central.guid;

import java.security.MessageDigest;
import java.util.Date;

import javax.naming.InitialContext;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class GuidFactory {

  public static String JNDI_NAME = "medcommons/GuidFactory";

  private static long seed = new Date().getTime();
  private static Logger log = Logger.getLogger(GuidFactory.class);

  public GuidFactory() {
    log.info("Created new GuidFactory instance.");
  }

  public String allocateGuid() throws GuidFactoryException {
    String seed = "" + getNextSeed() + new Date().toString();
    
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(seed.getBytes());
      byte[] digest = md.digest();
      
      return new String(Hex.encodeHex(digest));
       
    } catch(Exception e) {
      throw new GuidFactoryException("Unable to allocate guid: " + e.toString(), e);
    }
  }
  
  public static GuidFactory getInstance() throws Exception {
    return (GuidFactory) new InitialContext().lookup(JNDI_NAME);
  }
  
  private synchronized long getNextSeed() {
    return seed++;
  }

}
