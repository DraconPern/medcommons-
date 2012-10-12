/*
 * $Id: ActivityAction.java 2684 2008-06-26 06:37:38Z ssadedin $
 * Created on 07/02/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ActivitySession;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.InsufficientPrivilegeException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import flexjson.JSONSerializer;

/**
 * JSON based web service to allow other system components to 
 * read or write entries to the activity log for a user. 
 * 
 * @author ssadedin
 */
public class ActivityAction implements ActionBean {
   
    /**
     * Default time allowed for amalgamating sessions with the same session id
     * by the same source actor when returning activity events.
     */
    public static final int DEFAULT_ACTIVITY_SESSION_TIMEOUT_MS = 3600 * 1000;

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ActivityAction.class);
    
    ActionBeanContext ctx = null;
    
    String type; 
    String description; 
    String auth; 
    String accid;
    
    /**
     * Maximum number of records to return when retrieving events. 
     * Note that values larger than 50 can cause problems with PHP's 
     * JSON parser.
     */
    int max = 50;
    
    /**
     * Time allowed for amalgamating sessions with the same session id
     * by the same source actor when returning activity events.
     */ 
    int sessionTimeoutMs = DEFAULT_ACTIVITY_SESSION_TIMEOUT_MS;
    
    
    /**
     * Time to return records "since" - only events that occurred after
     * given time will be returned.
     */
    long since = -1;
   
    @DefaultHandler
    public Resolution log() {
        
        JSONObject result = new JSONObject();
        try {
            if(blank(accid)) 
                throw new IllegalArgumentException("account id not provided");
            
            if(blank(auth))
                throw new IllegalArgumentException("auth not provided");
            
            if(blank(type)) 
                throw new IllegalArgumentException("type not provided");
            
            if(blank(description)) 
                throw new IllegalArgumentException("description not provided");
            
            ActivityEventType eventType = ActivityEventType.valueOf(type);
             
            log.info("Adding activity log entry for user " + accid + " by auth " + auth + " of type " + eventType.toString());
            
            UserSession d = new UserSession(ServiceConstants.PUBLIC_MEDCOMMONS_ID, auth, new ArrayList<CCRDocument>());
            
            AccountSpec account = d.getOwnerPrincipal();
            if(eventType == null)
                throw new IllegalArgumentException("Unknown event type " + eventType);
            
            ActivityEvent e = new ActivityEvent(eventType, description, account, accid, null, null);
            d.getServicesFactory().getActivityLogService().log(e);
            result.put("status","ok");
        }
        catch (Exception e) {
            log.error("Unable to log activity event of type " + type + " using auth " + auth + " for account " + accid,e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        return new StreamingResolution("text/plain", result.toString());
    }
    
    /**
     * Return JSON version of the activity log 
     */
    public Resolution json() {
        
        log.info("Retrieving json activity log for " + this.accid + " with session timeout " + this.sessionTimeoutMs + " and filter since " + this.since);
        
        HashMap<String, Object> out = new HashMap<String, Object>();
        JSONSerializer json = new JSONSerializer().include("sessions").include("events").exclude("*.class");
        try { 

            UserSession desktop = new UserSession(null, auth, new ArrayList<CCRDocument>());

            if(!desktop.checkPermissions(this.accid, Rights.READ.toString())) 
                throw new InsufficientPrivilegeException("Authorization token " + this.auth + " not authorized for access to account " + this.accid);

            Collection<ActivityEvent> events = 
                desktop.getServicesFactory().getActivityLogService().load(this.accid, 0, max);
            
            if(since >= 0)
                events = filterEvents(events);
           
            log.info("Found " + events.size() + " activity events");
            List<ActivitySession> sessions = ActivitySession.fromEvents(events,sessionTimeoutMs);
            
            out.put("sessions", sessions);
            out.put("status", "ok"); 
        }
        catch (Exception e) {
            log.error("Unable to read activity events using auth " + auth + " for account " + accid,e);
            out.put("status", "failed");
            out.put("error", e.getMessage());
        }
        return new StreamingResolution("text/plain", json.serialize(out));
    }

    private Collection<ActivityEvent> filterEvents(Collection<ActivityEvent> events) {
        final long sinceMs = since * 1000;
        Collection<ActivityEvent> filteredEvents = new ArrayList<ActivityEvent>();
        for(ActivityEvent e : events) {
            if(e.getTimeStampMs() <= sinceMs)
                break;
            filteredEvents.add(e);
        }
        return filteredEvents;
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    public void setContext(ActionBeanContext ctx) {
        this.ctx = ctx;
        if(ctx.getRequest().getAttribute("oauth_token") != null)
            this.auth = (String) ctx.getRequest().getAttribute("oauth_token");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getAccid() {
        return accid;
    }

    public void setAccid(String affectedAccountId) {
        this.accid = affectedAccountId;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public long getSince() {
        return since;
    }

    public void setSince(long since) {
        this.since = since;
    }

}
