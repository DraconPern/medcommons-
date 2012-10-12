/*
 * $Id$
 * Created on 17/01/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import net.medcommons.modules.services.interfaces.AccountHolderRight;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.AccountShare;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.Application;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.ActionBeanContext;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Provides functions to manage sharing of accounts and assiciated permissions.
 * 
 * @author ssadedin
 */
public class AccountSharingAction implements ActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AccountSharingAction.class);
    
    /**
     * Stripes context
     */
    private ActionBeanContext ctx;
    
    /**
     * The desktop owner's shares with other accounts
     */
    private ArrayList<AccountShare> shares = new ArrayList<AccountShare>();
    
    /**
     * Rights shared by the current ccr's patient with the owner of the current desktop
     */
    private String sharedRights;
    
    /**
     * The Current CCR that is shared when desktop owner is different to current ccr owner  
     */
    private CCRDocument currentCcr;
    
    /**
     * The index of the CCR displayed
     */
    private int ccrIndex = -1;
    
    /**
     * A shareId which may be 
     * 
     *  <li>an account id
     *  <li>an external share id prefixed with 'es_'
     */
    private String shareId;
    
    
    /**
     * Output - group information queried by info()
     */
    private AccountSettings groupInfo = null;

    /**
     * Output - share information queryied by info()
     */
    private Application application;
    
    
    private static Map<String, String> rightsValues = new HashMap<String, String>();
    static {
        rightsValues.put("RW", "RW");
        rightsValues.put("R", "R");
        rightsValues.put("W", "W");
        rightsValues.put("", "None");
    }
     
    /**
     * Queries the current sharing information and forwards to appropriate display 
     * 
     * @throws ServiceException 
     * @throws IOException 
     * @throws JDOMException 
     */
    @DefaultHandler
    public Resolution display() throws ServiceException, PHRException{        
       
       UserSession d = UserSession.get(this.ctx.getRequest());
       
       String displayAccountId = queryShares(); 
       
       // If user is displaying their own account information, show them what they are sharing to other people
       if(true /*d.getOwnerMedCommonsId().equals(displayAccountId)*/)  {  // Everyone now gets to manage full permissions!
           return new ForwardResolution("/managePermissions.jsp");
       }
       else { // displaying someone elses account info.  Show them what they are allowed to see by that person
           
           // Find the actual rights the desktop owner has wrt to the displayed account 
           for (AccountShare share : this.shares) {
               for (AccountHolderRight right : share.getAccounts()) {
                if(right.getAccountId().equals(d.getOwnerMedCommonsId())) {
                    this.sharedRights = right.rights;
                }
            }            
           }
           return new ForwardResolution("/displayPermissions.jsp");
       }
    }
    
    public Resolution table() throws Exception {
        this.queryShares();
        return new ForwardResolution("/permissionsTable.jsp");
    }

    /**
     * Determines the user to display shares for and then queries shares
     * from the acct server and stores them in instance variables. 
     * 
     * @return - the account id that was queried.
     */
    private String queryShares() throws PHRException, ServiceException {
        UserSession d = UserSession.get(this.ctx.getRequest());
        // Find which account we will display for
        // By default we display the sharing info for the owner of the desktop
        String displayAccountId = d.getOwnerMedCommonsId();

        // But if a CCR is open with a different patient id, then we show 
        // the sharing information between that patient and the desktop holder
        if(!d.getCcrs().isEmpty()) { 
            // Find the account id
            int lastAccessed = d.getLastAccessedCcr();
            if(lastAccessed >= 0 && lastAccessed < d.getCcrs().size()) {
                this.currentCcr = d.getCcrs().get(lastAccessed);
            }
            else 
                this.currentCcr = d.getCcrs().get(0);
            
            d.setActiveCCR(this.ctx.getRequest(), this.currentCcr);

            displayAccountId = this.currentCcr.getPatientMedCommonsId();
        }

        if(!blank(displayAccountId)) 
            this.shares = d.getServicesFactory().getAccountService().querySharingRights(displayAccountId);
        
        
        // Cosmetically it looks better if the individuals go at the end
        AccountShare individuals = null;
        for (AccountShare s : this.shares) {
            if("Individuals".equals(s.getPracticeName())) {
                individuals = s;
                this.shares.remove(s);
                break;
            }
        }
        
        if(individuals != null) {
            this.shares.add(individuals);
        }
        
        
        return displayAccountId;
    }
    
    /**
     * Updates the current user's account with new consents / account sharing priviliges.
     * A note is added to the activity log.
     */
    public Resolution update() throws ServiceException, IOException {
        
        HashMap<String, Object>  result = new HashMap<String, Object>();
        result.put("status", "ok");
       try {
           Enumeration<String> e = this.ctx.getRequest().getParameterNames();
           ArrayList<AccountHolderRight> update = new ArrayList<AccountHolderRight>();
           while(e.hasMoreElements()) {
               String account = e.nextElement();
               if(account.matches("^[0-9]{16}$") || account.matches("^es_.*$") || account.matches("^at_.*$")) {
                   update.add(new AccountHolderRight(account, ctx.getRequest().getParameter(account)));
               }
           } 

           UserSession d = UserSession.get(this.ctx.getRequest());
           
           String affectedAccountId = d.getOwnerMedCommonsId();
           
           if(ccrIndex >= 0) {
               CCRDocument ccr = d.getCcrs().get(ccrIndex);
               affectedAccountId = ccr.getPatientMedCommonsId();
               if(Str.blank(affectedAccountId))
                   throw new IllegalArgumentException("No patient account id - cannot update sharing rights without patient medcommons id");
           }
           
           d.getServicesFactory().getAccountService().updateSharingRights(affectedAccountId, update);

           // Update activity log
           ActivityEvent evt = new ActivityEvent(ActivityEventType.CONSENT_UPDATE, "Updated consents for " + update.size() + " accounts", d.getOwnerPrincipal(), affectedAccountId);
           d.getServicesFactory().getActivityLogService().log(evt);
       }
       catch(Exception e) {
           log.error("Failed to update sharing rights",e);
           result.put("status", "failed");
           result.put("error", e.getMessage());
       }
       
       return new JavaScriptResolution(result);
    }
    
    /**
     * Query for information about the requested account and return it
     * as a small snippet of HTML for display.
     */
    public Resolution info() {
        
        try {
            UserSession d = UserSession.required(this.ctx.getRequest());
            if(this.shareId.startsWith("at_")) {
                String applicationToken = this.shareId.substring(3);
                this.application = d.getServicesFactory().getAccountService().queryApplicationInfo(applicationToken);
                return new ForwardResolution("/shareInfo.ftl");
            }
            else {
                this.groupInfo = d.getAccountSettings(this.shareId);
                return new ForwardResolution("/groupInfo.ftl");
            }
        }
        catch(Exception ex) {
            log.error("Failed to retrieve information for share " + this.shareId,ex);
            return new ForwardResolution("/groupInfoError.ftl"); 
        }
    }
    
    public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
        this.ctx = (ActionBeanContext)ctx;
        ctx.getResponse().setHeader("Cache-Control","no-cache"); // HTTP 1.1
        ctx.getResponse().setHeader("Pragma","no-cache"); // HTTP 1.0
    }    
    
    public ActionBeanContext getContext() {
        return ctx;
    }

    public ArrayList<AccountShare> getShares() {
        return shares;
    }

    public void setShares(ArrayList<AccountShare> shares) {
        this.shares = shares;
    }

    public Map<String, String> getRightsValues() {
        return rightsValues;
    }

    public String getSharedRights() {
        return sharedRights;
    }

    public void setSharedRights(String sharedRights) {
        this.sharedRights = sharedRights;
    }

    public CCRDocument getCurrentCcr() {
        return currentCcr;
    }

    public int getCcrIndex() {
        return ccrIndex;
    }

    public void setCcrIndex(int ccrIndex) {
        this.ccrIndex = ccrIndex;
    }

    public AccountSettings getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(AccountSettings groupInfo) {
        this.groupInfo = groupInfo;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

}
