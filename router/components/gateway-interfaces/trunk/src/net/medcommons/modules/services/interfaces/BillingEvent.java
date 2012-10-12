/*
 * $Id: BillingEvent.java 2885 2008-09-03 08:34:18Z ssadedin $
 * Created on 15/05/2008
 */
package net.medcommons.modules.services.interfaces;

import org.json.JSONObject;


/**
 * An instance of an event that incurs a charge to a MedCommons user.
 * 
 * @author ssadedin
 */
public class BillingEvent {
    
    /**
     * Type of billing event
     */
    BillingEventType type;
    
    /**
     * Quantity associated with billing event.  For example, for a fax,
     * since charge is levied by the page, the number of pages in the 
     * fax is the quantity.
     */
    int quantity = 1;
    
    @Override
    public String toString() {
        return "BillingEvent{type: "+type+", quantity:"+quantity+"}";
    }
    
    public JSONObject toJSON() {
        return new JSONObject().put("type", type.name()).put("quantity", new Integer(quantity));
    }

    public BillingEvent(BillingEventType type) {
        this.type = type;
    }

    public BillingEventType getType() {
        return type;
    }

    public void setType(BillingEventType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
