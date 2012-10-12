/*
 * $Id: QuickReplyAction.java 3568 2009-12-05 20:00:34Z ssadedin $
 * Created on 08/11/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.text.ParseException;
import java.util.HashMap;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Sends a "quick" reply to the sender of a CCR
 * 
 * @author ssadedin
 */
public class QuickReplyAction extends CCRActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(QuickReplyAction.class);
    
    private String comment;
    
    /**
     * Creates a reply CCR and set it as 'ccr' variable in request scope
     */
    public Resolution create() throws Exception { 
        // Create an appropriate reply CCR
        CCRDocument replyCCR = session.getReplyCcr(ccr);
        initSharingCCR(replyCCR);
        return new ForwardResolution("/notificationsTableTable.jsp");
    }
    
    /**
     * Share a copy of this CCR with another user
     */
    public Resolution share() throws Exception {
        CCRDocument copy = ccr.copy();
        session.getCcrs().add(copy);
        initSharingCCR(copy);
        return new ForwardResolution("/notificationsTableTable.jsp");
    }
    
    /**
     * Set the specified CCR as the one to render in the dialog
     * 
     * @param ccrToShare
     */
    private void initSharingCCR(CCRDocument ccrToShare) {
        session.setActiveCCR(ctx.getRequest(), ccrToShare);
        ctx.getRequest().setAttribute("ccr", ccrToShare);
        if(blank(session.getReplyPin())) {
            session.setReplyPin(PIN.generate());
        }
    }
    
    @DefaultHandler
    public Resolution reply() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        
        try {
            log.info("Sending quick reply with comment " + comment);
            
            
            CCRDocument replyCCR = ccr;
            
            String toEmail = replyCCR.getValue("toEmail");
            CCRElement toPerson = replyCCR.getRoot().queryProperty("toPerson");
            String destAcctId = toPerson != null ? toPerson.queryTextProperty("personMedCommonsId") : null;
            
            if(blank(toEmail) && blank(destAcctId))
                throw new CCROperationException("Cannot send without recipient email or Account ID");
            
            replyCCR.getJDOMDocument().queryProperty("purposeText");
            replyCCR.setMedCommonsComment(comment);
            
            replyCCR.setStorageMode(StorageMode.SCRATCH);
            replyCCR.setLogicalType(null); 
            
            // Store the CCR and notify recipient
            StoreTransaction tx = new StoreTransaction(session.getServicesFactory(), session.getAccountSettings(), replyCCR);
            tx.registerDocument(session.getReplyPin(), new String[]{ session.getOwnerMedCommonsId(), Rights.ALL });
            tx.storeDocument();
            tx.notifyRegistry();
            tx.writeActivity(ActivityEventType.PHR_SEND, "Reply to " + Str.bvl(toEmail, "#"+destAcctId));
            tx.sendEmail(destAcctId);
            
            // TODO:  CREATE TAB HERE?
            
            result.put("guid", tx.getDocumentGuid());
            result.put("trackingNumber", replyCCR.getTrackingNumber());
            result.put("ccrIndex", session.getCcrs().indexOf(replyCCR));
            result.put("status", "ok");
        }
        catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        
        return new JavaScriptResolution(result);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
