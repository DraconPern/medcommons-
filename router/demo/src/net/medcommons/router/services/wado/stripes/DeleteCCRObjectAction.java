/*
 * $Id: DeleteCCRObjectAction.java 2468 2008-03-13 06:27:56Z ssadedin $
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.util.Iterator;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ValidationResult;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * Deletes an object from the CCR specified by it's CCRDataObjectID or it's ActorID
 * 
 * @author ssadedin
 */
public class DeleteCCRObjectAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DeleteCCRObjectAction.class);
    
    
    //////////////////////
    //
    //  INPUTS
    
    /**
     * CCRDataObjectID or ActorID of object to be deleted
     */
    private String ccrDataObjectID;
    
    /**
     * Name of section from which to delete the object
     */
    private String section;
    
    /**
     * Comma separated list of objects to delete
     */
    private String ccrDataObjectIDs;
    
    /**
     * Deletes a specified CCRDataObject from the CCR
     */
    @DefaultHandler
    public Resolution deleteObject() {
        try {
            
            if(Str.blank(ccrDataObjectID)) 
                throw new IllegalArgumentException("Require valid CCRDataObjectID to delete");
            
            CCRElement sectionElement = getSectionElement();
            
            CCRElement e = findById(sectionElement,ccrDataObjectID);
            
            // Check that there are no references
            validateReferences(e);
            
            removeAndValidate(e);
        
            return new JavaScriptResolution("ok");            
        }
        catch(Exception e) {
            log.error("Unable to delete Order Request",e);
            return new JavaScriptResolution(e.toString());            
        }
        
    }


    /**
     * Search for references to the given element in the CCR.  If any are
     * found, throw exception.
     * @throws CCROperationException 
     */
    private void validateReferences(CCRElement e) throws PHRException, CCROperationException {
        if(e.getName().equals("Actor")) {
            final String actorObjectID = e.getChildText("ActorObjectID");
            Iterator i = ccr.getRoot().getDescendants( new ElementFilter("ActorID") {
                @Override
                public boolean matches(Object obj) {
                    return super.matches(obj) ? ((Element)obj).getText().equals(actorObjectID) : false;
                    
                }
            });
            
            if(i.hasNext()) {
                throw new CCROperationException("Element has references within the CCR");
            }
        }
    }


    private CCRElement findById(CCRElement sectionElement, String objId) throws CCROperationException {
        CCRElement e = sectionElement.getChildByObjectID(objId);
        
        // If null as ccrDataObjectID, try interpreting as actorID
        if(e == null) {
             for (Iterator i = sectionElement.getChildren().iterator(); i.hasNext();) {
                CCRElement c = (CCRElement) i.next();
                if(objId.equals(c.getChildText("ActorID")) || objId.equals(c.getChildText("ActorObjectID"))) {
                    log.info("Found object " + objId + " as ActorID in section " + section);
                    e = c;
                    break;
                }
            }
        }
        
        if(e == null) 
            throw new CCROperationException("CCRDataObject with ID " + objId + " not found");
        return e;
    }


    /**
     * Delete a specified section from the CCR
     */
    public Resolution deleteSection() {
        try {
            
            ValidationResult preValidation = this.ccr.getJDOMDocument().validate();
            
            CCRElement sectionElement = this.getSectionElement();
            if(ccrDataObjectIDs != null) {
                String[] ids = ccrDataObjectIDs.split(",");
                for(String id: ids) {
                    CCRElement toRemove = findById(sectionElement, id);
                    validateReferences(toRemove);
                    toRemove.getParent().removeContent(toRemove);
                }
            }
            
            if(sectionElement.getChildren().isEmpty()) {
                sectionElement.getParentElement().removeContent(sectionElement);
            }
            
            ValidationResult postValidation = this.ccr.getJDOMDocument().validate();
            if(!postValidation.isPassed() && preValidation.isClear()) 
                throw new CCROperationException("Deleting objects " + ccrDataObjectIDs + " from section " + section + " causes validation failures");
            
            return new JavaScriptResolution("ok");
        }
        catch(Exception e) {
            log.error("Unable to delete section " + section,e);
            return new JavaScriptResolution(e.toString());            
        }
    }
    private void removeAndValidate(CCRElement e) throws PHRException, CCROperationException {
        ValidationResult preValidation = this.ccr.getJDOMDocument().validate();
        
        Element parent = e.getParentElement();
        int index = parent.indexOf(e);
        
        parent.removeContent(e);
        
        postValidate(e, preValidation, parent, index);
    }


    private void postValidate(CCRElement e, ValidationResult preValidation, Element parent, int index)
                    throws PHRException, CCROperationException {
        ValidationResult postValidation = this.ccr.getJDOMDocument().validate();
        if(!postValidation.isPassed() && preValidation.isClear()) {
            parent.addContent(index,e);
            throw new CCROperationException("Deleting object " + ccrDataObjectID + " from section " + section + " causes validation failures");
        }
    }

    private CCRElement getSectionElement() throws PHRException {
        CCRElement sectionElement = this.ccr.getRoot().getChild("Body").getChild(section);
        if(sectionElement == null) {
            // Is it a direct child of the body?
            sectionElement = this.ccr.getRoot().getChild(section);
        }
        
        if(blank(section) || sectionElement == null) 
            throw new IllegalArgumentException("Section " + section + " is invalid or not found in the CCR");
        return sectionElement;
    }
    

    public String getCcrDataObjectID() {
        return ccrDataObjectID;
    }

    public void setCcrDataObjectID(String ccrDataObjectID) {
        this.ccrDataObjectID = ccrDataObjectID;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }


    public String getCcrDataObjectIDs() {
        return ccrDataObjectIDs;
    }


    public void setCcrDataObjectIDs(String ccrDataObjectIDs) {
        this.ccrDataObjectIDs = ccrDataObjectIDs;
    }
}
