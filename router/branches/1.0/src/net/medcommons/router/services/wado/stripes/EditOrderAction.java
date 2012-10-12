/*
 * $Id: EditOrderAction.java 2914 2008-09-12 08:44:02Z ssadedin $
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Handles editing of OrderRequests 
 * 
 * @author ssadedin
 */
public class EditOrderAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(EditOrderAction.class);
    
    //////////////////////
    //
    //  INPUTS
    
    /**
     * Actor ID to assign as source for this object (if any)
     */
    private String physicianActorId;
    
    /**
     * CCRDataObjectID of Plan of OrderRequest to be deleted
     */
    private String ccrDataObjectID;
    
     
    @DefaultHandler
    public Resolution create() throws Exception {
        try { 
            CCRElement planOfCare = ccr.getRoot().getOrCreate("Body").getOrCreate("PlanOfCare");
            List<CCRElement> plans = this.ccr.create("plan", "Plan", this.ctx.getRequest().getParameterMap());
            for(CCRElement plan : plans) {
                if(plan != null) {   
                    setReferringPhysician(plan);
                    CCRElement dateTime = el("DateTime");
                    dateTime.setDate(CCRElement.getCurrentTime());
                    plan.getPath("OrderRequest/Procedures/Procedure").addChild(dateTime);
                    plan.createPath("Type/Text", "Order");
                    plan.createPath("Status/Text", "Pending");  
                    planOfCare.addContent(plan); 
                }
            }
            /*
            ValidationResult validationResult = ccr.getJDOMDocument().validate();
            if(!validationResult.errors.isEmpty()) {
                throw new CCROperationException("Invalid CCR created by operation: " + validationResult.errors.get(0)+   "\n\n Please check the data you have entered.");
            }
            */
            return new JavaScriptResolution("ok");            
        }
        catch(Exception e) {
            log.error("Unable to save new Order Request",e);
            return new JavaScriptResolution(e.toString());            
        }
    }
    
    /**
     * Create a new CCR as an Order CCR based on the active CCR
     */
    public Resolution newccr() throws Exception {
        
        UserSession.required(ctx.getRequest());
        
        if(ccr == null)
            throw new IllegalArgumentException("No CCR Specified for operation");
        
        CCRDocument orderCcr = ccr.copy();
        
        orderCcr.setStorageMode(StorageMode.SCRATCH);
        orderCcr.setLogicalType(null);
        orderCcr.setGuid(null);
        session.setActiveCCR(ctx.getRequest(), orderCcr);
        
        this.ctx.getRequest().setAttribute("displayOrderForm", "true"); 
        
        return new ForwardResolution("/viewEditCCR.do?mode=edit");
    }

    private void setReferringPhysician(CCRElement plan) {
        plan.createPath("Source/Actor/ActorID",physicianActorId);
        plan.createPath("OrderRequest/Source/Actor/ActorID",physicianActorId);
        plan.createPath("OrderRequest/Procedures/Procedure/Source/Actor/ActorID",physicianActorId);
        plan.createPath("OrderRequest/Procedures/Procedure/IDs/Source/Actor/ActorID",physicianActorId);
    }
    
    
    public Resolution open() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            if(Str.blank(ccrDataObjectID)) 
                throw new IllegalArgumentException("Require valid CCRDataObjectID to delete");

            CCRElement e = this.ccr.getRoot().getChild("Body").getChild("PlanOfCare").getChildByObjectID(ccrDataObjectID);
            if(e == null) 
                throw new CCROperationException("CCRDataObject with ID " + this.ccrDataObjectID + " not found");
            result.put("order",e.getJSON());
            result.put("status","ok");
        }
        catch(Exception e) {
            log.error("Unable to edit Order Request",e);
            result.put("status","failed");
            result.put("error",e.toString());
        }
        return new JavaScriptResolution(result);            
         
    }
    
    public Resolution update() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            if(this.physicianActorId == null)
                throw new IllegalArgumentException("Require valid referring physician id");
                
            if(Str.blank(ccrDataObjectID)) 
                throw new IllegalArgumentException("Require valid CCRDataObjectID");

            CCRElement e = this.ccr.getRoot().getChild("Body").getChild("PlanOfCare").getChildByObjectID(ccrDataObjectID);
            
            if(e == null) 
                throw new CCROperationException("CCRDataObject with ID " + this.ccrDataObjectID + " not found");
            
            this.setReferringPhysician(e); 
            List<CCRElement> elements = Arrays.asList(e); 
            this.ccr.update("plan", "Plan", this.ctx.getRequest().getParameterMap(), elements);
            
            result.put("order",e.getJSON());
            result.put("status","ok");
        }
        catch(Exception e) {
            log.error("Unable to update Order Request " + ccrDataObjectID,e);
            result.put("status","failed");
            result.put("error",e.toString());
        }
        return new JavaScriptResolution(result);                    
    }
    
    public Resolution delete() {
        try {
            
            if(Str.blank(ccrDataObjectID)) 
                throw new IllegalArgumentException("Require valid CCRDataObjectID to delete");
           
            CCRElement e = this.ccr.getRoot().getChild("Body").getChild("PlanOfCare").getChildByObjectID(ccrDataObjectID);
            if(e == null) 
                throw new CCROperationException("CCRDataObject with ID " + this.ccrDataObjectID + " not found");
            
            e.getParent().removeContent(e);
        
            return new JavaScriptResolution("ok");            
        }
        catch(Exception e) {
            log.error("Unable to delete Order Request",e);
            return new JavaScriptResolution(e.toString());            
        }
        
    }

    public String getPhysicianActorId() {
        return physicianActorId;
    }

    public void setPhysicianActorId(String physicianActor) {
        this.physicianActorId = physicianActor;
    }

    public String getCcrDataObjectID() {
        return ccrDataObjectID;
    }

    public void setCcrDataObjectID(String ccrDataObjectID) {
        this.ccrDataObjectID = ccrDataObjectID;
    }
}
