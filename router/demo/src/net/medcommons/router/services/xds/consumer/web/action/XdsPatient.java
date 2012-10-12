/*
 * Created on Dec 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.xds.consumer.web.action;

import java.io.Serializable;


/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XdsPatient implements Serializable{
  String affinityDomain = "^^^&amp;1.3.6.1.4.1.21367.2005.1.1&amp;ISO^PI";
  String patientId = null;
  String documentTypes = null;
  String usage = null;
  
  public void setAffinityDomain(String affinityDomain){
    this.affinityDomain = affinityDomain;
  }
  public String getAffinityDomain(){
    return(this.affinityDomain);
  }
  public void setPatientID(String patientId){
    this.patientId = patientId;
  }
  public String getPatientId(){
    return(this.patientId);
  }
  public void setDocumentTypes(String documentTypes){
    this.documentTypes = documentTypes;
  }
  public String getDocumentTypes(){
    return(this.documentTypes);
  }
  public void setUsage(String usage){
    this.usage = usage;
  }
  public String getUsage(){
    return(this.usage);
  }

}
