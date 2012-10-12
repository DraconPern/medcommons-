/*
 * Created on Dec 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.xds.consumer.web.action;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class PatientListForm extends ActionForm {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger
      .getLogger(PatientListForm.class);
  /**
   * Array of XdsPatients objects visible on the selection screen
   */
  private ArrayList patients = new ArrayList(10);
  
  
  /**
   * 
   */
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {

    log.info("Reset - noop");
  }
  /**
   * @return Returns the studies.
   */
  public ArrayList getPatients() {
    log.info("getPatients()");
    log.info("Number of patients in list is " + patients.size());
    for (int i=0;i<patients.size();i++){
      log.info("object " + i + " is " + patients.get(i));
      log.info(((XdsPatient) patients.get(i)).getPatientId());
    }
    return patients;
  }
  /**
   * @param studies The studies to set.
   */
  public void setPatients(ArrayList patients) {
    this.patients = patients;
    log.info("setPatients()");
    log.info("Number of patients in list is " + patients.size());
    for (int i=0;i<patients.size();i++){
      log.info("object " + i + " is " + patients.get(i));
    }
  }
  
  
}
