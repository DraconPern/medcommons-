/*
 * $Id: $
 * Created on 1/02/2005
 */
package net.medcommons.router.services.xds.consumer.web.action;

import org.apache.struts.action.ActionForm;

/**
 * @author ssadedin
 */
public class PatientQueryForm extends ActionForm {

  /**
   * Id of the patient
   */
  private String patientId;

  /**
   * 
   */
  public PatientQueryForm() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String getPatientId() {
    return patientId;
  }
  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }
}
