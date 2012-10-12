/*
 * $Id$
 * Created on 13/09/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * Clears pending change notifications from the given CCR,
 * causing all current pending change notifications to not 
 * be notified again. 
 * 
 * @author ssadedin
 */
public class ClearChangeNotificationsAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ClearChangeNotificationsAction.class);
    
    @DefaultHandler
    public Resolution clear() {
        
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("status", "failed");
        String patientId = null;
        try {
            if(this.session == null)
                throw new NotLoggedInException();
            
            patientId = XPathUtils.getValue(ccr.getJDOMDocument(),"patientMedCommonsId");
            if(patientId == null) {
                throw new CCRStoreException("CCR does not have a valid MedCommons Patient Id");
            }
             
            log.info("Clearing notifications on ccr " + this.ccr.getGuid() + " for patient " + patientId);
            
            List<Element> pending = new ArrayList<Element>();
            // Update the change history
            for (Iterator iter = ccr.getChangeHistory().getDescendants(new ElementFilter("NotificationStatus")); iter.hasNext();) {
                Element ns = (Element) iter.next();
                if(CCRConstants.CCR_CHANGE_NOTIFICATION_STATUS_PENDING.equals(ns.getTextTrim()))
                    pending.add(ns);
            }
            
            for (Element element : pending) {
                element.setText(CCRConstants.CCR_CHANGE_NOTIFICATION_STATUS_NOTIFIED);
            }
            
            // Save as current CCR
            StoreTransaction tx = new StoreTransaction(this.session.getServicesFactory(), this.session.getAccountSettings(), this.ccr);
            tx.getMergeLogic().updateChangeHistory(tx,ccr);
            tx.registerDocument(null);
            tx.storeDocument();
            
            log.info("Created new Current CCR for " + patientId + " with " + ccr.getGuid());
           
            // Ensure session is appropriately updated too
            session.getAccountSettings(patientId).setCurrentCcrGuid(this.ccr.getGuid());
            
            result.put("status", "ok");
        }
        catch (IOException e) {
            log.error("Unable to update clear notifications for Current CCR of patient " + patientId + " by user " + this.session.getOwnerMedCommonsId(), e);
            result.put("message", e.getMessage());
        }
        catch (CCRStoreException e) {
            log.error("Unable to update clear notifications for Current CCR of patient " + patientId + " by user " + this.session.getOwnerMedCommonsId(), e);
            result.put("message", e.getMessage());
        }
        catch (ServiceException e) {
            log.error("Unable to update clear notifications for Current CCR of patient " + patientId + " by user " + this.session.getOwnerMedCommonsId(), e);
            result.put("message", e.getMessage());
        }
        catch (NoSuchAlgorithmException e) {
            log.error("Unable to update clear notifications for Current CCR of patient " + patientId + " by user " + this.session.getOwnerMedCommonsId(), e);
            result.put("message", e.getMessage());
        }
        catch (NotLoggedInException e) {
            log.error("Unable to update clear notifications for Current CCR.  User is not currently logged on.", e);
            result.put("message", "User is not logged on or session timeout occurred.  Please login again.");
        }
        catch (PHRException e) {
            log.error("Unable to update clear notifications for Current CCR of patient " + patientId + " by user " + this.session.getOwnerMedCommonsId(), e);
            result.put("message", e.getMessage());
        }
         
        return new JavaScriptResolution(result);
    }
}
