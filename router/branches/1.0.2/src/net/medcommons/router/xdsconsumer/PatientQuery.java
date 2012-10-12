/*
 * Created on Dec 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.xdsconsumer;

/**
 * Queries for documents for a given patient.
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PatientQuery {
  // Fixed affinityDomain for HIMSS
  String affinityDomain = "^^^&amp;1.3.6.1.4.1.21367.2005.1.1&amp;ISO^PI";
  
  public  PatientQuery(){
    // should load config.
  }
  public void queryForExternalLinks(){
    
  }

}
