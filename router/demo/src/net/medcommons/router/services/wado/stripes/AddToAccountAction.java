/*
 * $Id: AddToAccountAction.java 3304 2009-03-30 09:30:33Z ssadedin $
 * 
 * Copyright MedCommons Inc. 2005
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.repository.DocumentResolver;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Adds the specified current CCR to the user's account in one of two 
 * ways: 
 * 
 *   <li>If the user has a group worklist then it is added to their worklist
 *   <li>If the user has a voucher patient list then it is added to that
 *   <li>If the user does not have any kind of worklist then it is added to their 'account'
 *       which means it is written to their CCR Log (appears as a tab).
 *       
 *       
 * <p><i>Note:</i> As this services is a JSON service, the <i>sid</i> 
 * parameter is required in general.  However an exception is made when
 * the {@link #storageId} and {@link #accessAuth} parameters are provided
 * so that this service can be called directly from outside without any
 * session context.
 * 
 * @param ccrPurpose - subject text to display in worklist when added
 * @param storageId  - storageId of user to add to patient list (optional)
 * @param accessAuth - auth token for accessing user in patient list
 * 
 * @author ssadedin
 */
public class AddToAccountAction extends CCRJSONActionBean {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(AddToAccountAction.class);
  
  /**
   * The purpose of the CCR
   */
  String ccrPurpose = null;
  
  /**
   * If no CCR index is provided (see {@link CCRActionBean#getCcrIndex()}) then 
   * a Current CCR will be loaded from this field if it is specified.
   */
  @Validate(required=false, mask=MCID_PATTERN)
  String storageId;
  
  
  /**
   * The auth token that will be used to access the Current CCR for 
   * {@link #storageId} if it is used.
   */
  @Validate(required=false, mask=GUID_PATTERN)
  String accessAuth;
  
  @DefaultHandler
  public Resolution add() throws Exception {
      
      initializeCCR();
      
      if(ccr == null)
          throw new CCROperationException("No CCR specified.  ccrIndex or storageId and accessAuth parameters must be supplied and valid.");
      
      if(ccr.getGuid()==null)  
          throw new CCROperationException("Specified CCR is unsaved. Cannot add an unsaved CCR to an account");
      
      if(blank(ccrPurpose))
          ccrPurpose = ccr.getDocumentPurpose();
      
      String accId = session.getOwnerMedCommonsId();
      
      ServicesFactory factory = session.getServicesFactory();
      AccountSettings settings = session.getAccountSettings();
      if(!blank(settings.getRegistry())) {
          addToGroupWorklist(factory);
      }
      else
      if(settings.getVouchersEnabled()) {
          addToVoucherPatientList();
      }
      else {
          addToIndividualAccount(accId, factory);
      }
      
      session.getAccountTrackingNumbers().add(ccr.getTrackingNumber());
      result.put("success", true);          
      
      return new StreamingResolution("text/javascript",this.result.toString());
  }

  /**
   * Resolves a CCR to add to the patient list.  If a ccrIndex was
   * specified then this will just be that CCR in the user's session.
   * <p>
   * If no CCR was specified then an attempt to load the CCR based
   * on supplied {@link #storageId} and {@link #accessAuth} will be 
   * made.
   */
  private void initializeCCR() throws ConfigurationException, RepositoryException, ServiceException, PHRException {
      if(this.ccr != null)
          return;
      
      if(blank(storageId) || blank(accessAuth))
          throw new IllegalArgumentException("If ccrIndex is not supplied then storageId and accessAuth must be supplied");
      
      AccountSettings patientSettings = session.getAccountSettings(storageId);
      if(patientSettings == null)
          throw new IllegalArgumentException("Invalid patient specified (no records found for this patient).");
      
      if(blank(patientSettings.getCurrentCcrGuid())) 
          throw new IllegalArgumentException("Invalid patient specified:  Patient has no Current CCR.");
          
      DocumentResolver resolver = new DocumentResolver(new RESTProxyServicesFactory(accessAuth));
      this.ccr = resolver.resolveCCR(storageId, patientSettings.getCurrentCcrGuid());
  }

  private void addToVoucherPatientList() throws Exception {
      
      
      String patientId = ccr.getPatientMedCommonsId();
      if(blank(patientId))
          throw new CCROperationException("Cannot add a CCR without a patient account id to a patient list");
      
      log.info("Adding voucher for patient " + patientId + " to shared vouchers for account " + session.getOwnerMedCommonsId());
      
      AccountSettings patientSettings = session.getAccountSettings(patientId);
      if(patientSettings.getVoucher() == null)
          throw new CCROperationException("The specified CCR was not created from a voucher.");
          
      ServicesFactory svc = session.getServicesFactory(); 
      svc.getAccountService().shareVoucher(session.getOwnerMedCommonsId(), patientSettings.getVoucher());
      svc.getDocumentService().grantAccountAccess(patientId, session.getOwnerMedCommonsId(), Rights.ALL);
      
      writeActivity();
  }

  private void writeActivity() throws IOException, ServiceException, PHRException {
      session.getServicesFactory().getActivityLogService().log(
                      new ActivityEvent(ActivityEventType.CONSENT_UPDATE, 
                                      session.getMessage("addToAccount.activitylog.addedToPatientList"), 
                                      session.getOwnerPrincipal(), 
                                      ccr.getPatientMedCommonsId(), 
                                      ccr.getTrackingNumber(), null));
  }

  private void addToGroupWorklist(ServicesFactory factory) throws PHRException, ServiceException, ConfigurationException, IOException {
      Date dob = ccr.getPatientDateOfBirth();
      String patientId = ccr.getPatientMedCommonsId();
      if(!Str.blank(patientId)) {
          factory.getDocumentService().grantAccountAccess(patientId, session.getAccountSettings().getGroupId(), Rights.ALL);
      }
      
      AccountSettings settings = session.getAccountSettings(); 
      log.info("Adding patient " + patientId + " to patient list " + settings.getRegistry() + "( group = " + settings.getGroupName()+")");
      
      // Write to worklist
      factory.getSecondaryRegistryService().addCCREvent(
                      ccr.getPatientGivenName(), 
                      ccr.getPatientFamilyName(), 
                      ccr.getPatientGender(), 
                      patientId, 
                      CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE,
                      "pops",
                      "pops", 
                      dob != null ? dob.toGMTString() : null, 
                      ccr.getPatientAge(), 
                      ccr.getTrackingNumber(), 
                      null, 
                      ccr.getGuid(), 
                      ccr.getDocumentPurpose(), 
                      "", 
                      "MedCommons", 
                      Configuration.getProperty("RemoteAccessAddress")+"/access?g="+ccr.getGuid(), // viewer url
                      ccr.getPurposeText(), 
                      settings.getRegistry());
      
      this.writeActivity();
      
  }
  
  private void addToIndividualAccount(String accId, ServicesFactory factory) throws ServiceException, PHRException {
      factory.getAccountService().addCCRLogEntry(
                      ccr.getGuid(),
                      "pops", 
                      XPathUtils.getValue(ccr.getJDOMDocument(),"sourceEmail"), 
                      XPathUtils.getValue(ccr.getJDOMDocument(),"toEmail"), 
                      accId, new Date(),
                      ccrPurpose,
                      "Complete",
                      ccr.getTrackingNumber());
      
      log.info("Adding ccr " + ccr.getGuid() + " to account "  + accId);
  }
  
  @Override
  protected void checkSID() {
      HttpServletRequest r = ctx.getRequest();
      if(blank(r.getParameter("storageId")) || blank(r.getParameter("accessAuth")))
	      super.checkSID();
  }  
  
  
  public String getCcrPurpose() {
      return ccrPurpose;
  }
  
  public void setCcrPurpose(String ccrPurpose) {
      this.ccrPurpose = ccrPurpose;
  }

public String getStorageId() {
    return storageId;
}

public void setStorageId(String storageId) {
    this.storageId = storageId;
}

public String getAccessAuth() {
    return accessAuth;
}

public void setAccessAuth(String accesssAuth) {
    this.accessAuth = accesssAuth;
}

}
