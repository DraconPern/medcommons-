/*
 * $Id: BillingCounters.java 2946 2008-10-02 22:30:15Z ssadedin $
 * Created on 15/05/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.HashMap;

import org.json.JSONObject;

/**
 * A wrapper / extension of HashMap to represent a set of MedCommons counter
 * values  indexed on the counter type ({@link BillingEventType}).
 * 
 * @author ssadedin
 */
public class BillingCounters extends HashMap<BillingEventType, Integer> {    
    
    public BillingCounters() {
        for(BillingEventType t : BillingEventType.values()) {
            this.put(t, 0);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{ ");
        for(BillingEventType type : this.keySet()) {
            b.append(type.name() + " => " + get(type) + " ");
        }
        b.append("}");
        return b.toString();
    }
    
    public JSONObject toJSON() {
        JSONObject e = new JSONObject();
        for(BillingEventType type : this.keySet()) {
            e.put(type.toString(), get(type));
        }
        return e;
    }
}
