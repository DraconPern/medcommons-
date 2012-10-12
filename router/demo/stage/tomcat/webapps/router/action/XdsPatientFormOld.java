/*
 * Created on Dec 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.xds.consumer.web.action;


import org.apache.struts.action.ActionForm;
/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
*/


public class XdsPatientFormOld extends ActionForm {
  
  private XdsPatient patient = null;
  
  public XdsPatientFormOld() {
    
    // By default, create an empty patient
    this.setPatient(new XdsPatient());
  }
  
  /**
   * @return Returns the patient.
   */
  public XdsPatient getPatient() {
    return patient;
  }
  /**
   * @param patient The patient to set.
   */
  public void setPatient(XdsPatient patient) {
    this.patient = patient;
  }
}
