// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.stripes;

import java.util.HashMap;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Saves a patient id after it has been edited.
 * 
 * @author ssadedin
 */
public class PatientIdAction extends CCRJSONActionBean {
    
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(PatientIdAction.class);
  
  @Validate(required=true, minvalue=0)
  int patientIdIndex = -1;

  @Validate(required=true, on="!delete")
  String patientIdType;
  
  @Validate(required=true, on="!delete")
  String patientId;

  private String patientMCID;

  private XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");

  @DefaultHandler
  public Resolution deleteId() throws Exception {      
      
      log.info("Deleting all patient ids from CCR " + ccr.getGuid() + " index = " + getCcrIndex());
      
      Document doc = ccr.getJDOMDocument();          
                    
      // Cache the patient medcommons id so that we can tell if it changed
      patientMCID = ccr.getPatientMedCommonsId();
              
      // remove the old IDs
      Element patientActor = xpath.getElement(doc, "patientActor");
      patientActor.removeChildren("IDs");
      
      setOutputs();
     
      return new StreamingResolution("text/plain",this.result.toString());
  }
  
  public Resolution saveId()  throws Exception {
      
      log.info("Updating patient id type = " + patientIdType + " to " + patientId);
      adjustPatientId();
      
      CCRElement existingPatientId = getExistingPatientId();
      if(existingPatientId!=null) {
          xpath.getElement(existingPatientId, "idValue").setText(patientId.trim());
          xpath.getElement(existingPatientId, "idType").setText(patientIdType.trim());
      }
      else { // not an existing patient id
          ccr.addPatientId(patientId, patientIdType);
      }
      
      setOutputs();
      
      return new StreamingResolution("text/plain",this.result.toString());
  }
  
  private CCRElement getExistingPatientId() throws PHRException, JDOMException {
      // If an id already exists with this type, update the existing one
      HashMap vars = new HashMap();
      vars.put("index", String.valueOf(patientIdIndex+1));
      Document doc = ccr.getJDOMDocument();          
      CCRElement existingPatientId = (CCRElement) xpath.getElement(doc, "patientIdByIndex", vars);
      return existingPatientId;
  }
  
  public Resolution replaceId() throws Exception {
      adjustPatientId();
      
      Document doc = ccr.getJDOMDocument();          
      CCRElement patientActor = (CCRElement) xpath.getElement(doc, "patientActor");
      
      CCRElement patientId = getExistingPatientId();
      if(patientId != null)
          patientActor.removeChild(patientId);
      
      setOutputs();
      
      return new StreamingResolution("text/plain",this.result.toString());
  }
  
  private void setOutputs() throws PHRException, ServiceException {
      String newPMCID = ccr.getPatientMedCommonsId();
      if(!Str.equals(patientMCID,newPMCID)) {
          ccr.setStorageId(newPMCID);  
          result.put("newPatientId", newPMCID);
          result.put("writeable", session.checkPermissions(newPMCID, "W"));
      }
  }
  
  private void adjustPatientId() {
      // If the patient id is a MedCommons Account Id, normalize it to our standard form
      if(CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE.equals(patientIdType)) {
          patientId = patientId.replaceAll("\\s", "");
          patientId = patientId.replaceAll("-", "");
      }
  }
  
  public int getPatientIdIndex() {
      return patientIdIndex;
  }
  
  
  public void setPatientIdIndex(int patientIdIndex) {
      this.patientIdIndex = patientIdIndex;
  }
  
  
  public String getPatientIdType() {
      return patientIdType;
  }
  
  
  public void setPatientIdType(String patientIdType) {
      this.patientIdType = patientIdType;
  }
  
  
  public String getPatientId() {
      return patientId;
  }
  
  
  public void setPatientId(String patientId) {
      this.patientId = patientId;
  }
}
