/*
 * $Id: ActivitySession.java 3418 2009-07-08 05:51:58Z ssadedin $
 * Created on 02/11/2007
 */
package net.medcommons.modules.services.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.MultiHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class ActivitySession {
    
    private ArrayList<ActivityEvent> events = new ArrayList<ActivityEvent>();
    
    private static HashMap<ActivityEventType, Integer> EVENT_PRIORITIES = new HashMap<ActivityEventType, Integer>();
    
    {
        EVENT_PRIORITIES.put(ActivityEventType.PHR_ACCESS, 0);
        EVENT_PRIORITIES.put(ActivityEventType.CXP_IMPORT, 1);
        EVENT_PRIORITIES.put(ActivityEventType.ACOUNT_DOCUMENT_ADDED, 3);
        EVENT_PRIORITIES.put(ActivityEventType.PHR_SEND, 2);
        EVENT_PRIORITIES.put(ActivityEventType.PHR_UPDATE, 2);
        EVENT_PRIORITIES.put(ActivityEventType.CXP_EXPORT, 2);
        EVENT_PRIORITIES.put(ActivityEventType.CONSENT_UPDATE, 2);
        EVENT_PRIORITIES.put(ActivityEventType.SUMMARY, 3);
        
        // Make unknown entries highest priority so that we get legacy behavior preserved
        EVENT_PRIORITIES.put(ActivityEventType.UNKNOWN, 8);
    }
    
    /**
     * Time at which activity began
     */
    private long beginTimeMs;
    
    /**
     * Time of last activity event
     */
    private long endTimeMs;
    
    /**
     * Id for this session
     */
    private String sessionId;
    

    /**
     * @param sessionId
     */
    public ActivitySession(String sessionId) {
        super();
        this.sessionId = sessionId;
    }
    /**
     * The most "important" event in the session.  Modifications are
     * more important than viewing, exposure of data (Sending) is more important 
     * than Modifications.
     */
    private ArrayList<ActivityEvent> primaryEvents = new ArrayList<ActivityEvent>();
    
    
    /**
     * Collects the given events into a set of ActivitySessions such that
     * all events with the same session id are organized into the same
     * session object.
     * 
     * @param events
     * @return a list of ActivitySessions 
     */
    public static List<ActivitySession> fromEvents(Collection<ActivityEvent> events, long timeoutMs) {
        
        List<ActivitySession>  activitySessions = new ArrayList<ActivitySession>();
        
        // Resolve the email addresses
        HashMap<String, ActivitySession> sessions = new HashMap<String, ActivitySession>();
        ActivityEvent lastEvent = null;
        ActivitySession lastSession = null;
        for(ActivityEvent e : events) {
            if(isSharedSession(e, lastEvent, timeoutMs)) {
                lastSession.addEvent(e);
            }
            else
            if(e.getSessionId()==null || e.getSessionId().isEmpty()) {
                ActivitySession s = new ActivitySession(e.getSessionId());
                s.addEvent(e);
                lastSession = s;
                activitySessions.add(s);
            }
            else {
                ActivitySession s = sessions.get(e.getSessionId());
                if(s == null || e.getSessionId() == null || e.getType() == ActivityEventType.UNKNOWN) {
                    sessions.put(e.getSessionId(), s = new ActivitySession(e.getSessionId()));
                    activitySessions.add(s);
                }
                s.addEvent(e);
                lastSession = s;
            }
            lastEvent = e;
        }
        
        return activitySessions;
    }
    
    /**
     * Returns true if the given two sessions can be considered
     * the same session for display purposes.  At the moment this means:
     * 
     *  - they have the same session id
     *  or
     *  - they have the same actor as the source of the activity AND they
     *    occur within 1 hour of each other.
     * 
     * @param timeoutMs - maximum time to consider two sessions with different ids
     *                     from same actor as identical
     * 
     * @return
     */
    public static boolean isSharedSession(ActivityEvent e1, ActivityEvent e2, long timeoutMs) {
        if(e1 == null || e2 == null)
            return false;
        
        String sessionId1 = e1.getSessionId();
        if(sessionId1 == e2.getSessionId())
            return true;
        
        if(sessionId1 != null && sessionId1.equals(e2.getSessionId()))
            return true;
        
        // Different session ids
        // Can only combine if the source account is the same
        if(e1.getSourceAccount() == null || !e1.getSourceAccount().equals(e2.getSourceAccount())) 
            return false;
        
        // Don't share POPS sessions together
        if(ServiceConstants.PUBLIC_MEDCOMMONS_ID.equals(e1.getSourceAccount().getMcId()))
            return false;
        
        // Check timestamps
        return ( Math.abs(e1.getTimeStampMs() - e2.getTimeStampMs()) < timeoutMs);
    }    
    public void addEvent(ActivityEvent e) {
        
        if(events.isEmpty() || beginTimeMs >= e.getTimeStampMs())
            beginTimeMs = e.getTimeStampMs();
        
        events.add(e);
        
        // If there are already primary events, only add this as primary if it 
        // supercedes existing primary events
        if(!primaryEvents.isEmpty()) {
            ActivityEvent p = primaryEvents.get(0);
            if(EVENT_PRIORITIES.get(e.getType()) > EVENT_PRIORITIES.get(p.getType())) {
                // Remove the old events and replace with this event
                primaryEvents.clear();
                primaryEvents.add(e);
            }
            else
            if(EVENT_PRIORITIES.get(e.getType()).equals(EVENT_PRIORITIES.get(p.getType()))) {
                primaryEvents.add(e);
            }
        }
        else {
            primaryEvents.add(e);
        }
    }
    
    public ActivityEvent getSummary() {
        
        if(primaryEvents.isEmpty()) {
            return null;
        }
        
        // If only 1 event then return that one
        ActivityEvent primary = primaryEvents.get(0);
        if(primaryEvents.size() == 1) {
            return primary;
        }
        
        // If more than 1 event, make an artificial summary event
        
        // Sort into types
        MultiHashMap types = new MultiHashMap();
        for(ActivityEvent e : primaryEvents) {
            types.put(e.getType(), e);
        }
        
        StringBuilder desc = new StringBuilder();
        for(ActivityEventType t : (Set<ActivityEventType>)types.keySet()) {
            if(desc.length()!=0) 
                desc.append(", ");
            desc.append(t.desc(types.getCollection(t).size()));
        }
         
        ActivityEvent e = 
            new ActivityEvent(ActivityEventType.SUMMARY, desc.toString(), primary.getSourceAccount(), primary.getAffectedAccountId(), primary.getTrackingNumber(), primary.getPin());
        
        e.setTimeStampMs(this.beginTimeMs);
        return e;
    }
    
    public JSONObject toJSON() {
        JSONObject s = new JSONObject();
        ActivityEvent summary = getSummary();
        s.put("beginTime", beginTimeMs/1000)
         .put("endTime", endTimeMs/1000)
         .put("sessionId", sessionId)
         .put("summary", summary==null?null : summary.toJSON());
        
        JSONArray evts = new JSONArray();
        for(ActivityEvent e : this.events) {
            evts.put(e.toJSON());
        }
        s.put("events", evts);
        return s;
    }

    public long getBeginTimeMs() {
        return beginTimeMs;
    }

    public void setBeginTimeMs(long beginTimeMs) {
        this.beginTimeMs = beginTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public ArrayList<ActivityEvent> getPrimaryEvents() {
        return primaryEvents;
    }

    public void setPrimaryEvents(ArrayList<ActivityEvent> primaryEvents) {
        this.primaryEvents = primaryEvents;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ArrayList<ActivityEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ActivityEvent> events) {
        this.events = events;
    }


}
