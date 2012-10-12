/*
 * $Id: SetEmergencyCCRAction.java 2971 2008-10-21 06:47:21Z ssadedin $
 * 
 * Copyright MedCommons Inc. 2005
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Sets the specified CCR to be the current emergency CCR for the
 * current account.
 * 
 * Requires a current account id in the session.
 * 
 * @author ssadedin
 */
public class SetEmergencyCCRAction extends CCRJSONActionBean {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(SetEmergencyCCRAction.class);

  
  @DefaultHandler
  public Resolution set() throws Exception {
      String accId = ccr.getPatientMedCommonsId();
      if(accId == null || ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(accId)) {
          throw new CCROperationException("Invalid Patient Account.  A CCR set as Emergency CCR must have a valid MedCommons Account Id for Patient Account");
      } 
      
      // Copy the CCR
      CCRDocument eccr = ccr.copy();
      session.getCcrs().add(eccr);
      
      eccr.setLogicalType(AccountDocumentType.EMERGENCYCCR);
      eccr.setStorageMode(StorageMode.LOGICAL);
      
      // Store it
      StoreTransaction tx = session.tx(eccr);
      tx.registerDocument(null);
      tx.storeDocument();
      
      log.info("Setting emergency CCR for account " + accId + " to " + eccr.getGuid());
      
      JSONObject einfo = eccr.getEmergencyInfo();
      
      session.getServicesFactory().getAccountService().setEmergencyCCR(accId, eccr.getGuid(), einfo.toString());
      
      ActivityEvent evt = new ActivityEvent(ActivityEventType.PHR_UPDATE, "Updated Emergency CCR", session.getOwnerPrincipal(), accId);
      session.getServicesFactory().getActivityLogService().log(evt);
      
      // Update in the session so that any displays relying on account settings get updated 
      session.getAccountSettings(accId).setEmergencyCcrGuid(eccr.getGuid());
      
      result.put("ccrIndex", session.getCcrs().indexOf(eccr));
      return new StreamingResolution("text/plain", result.toString());      
  }  
}
