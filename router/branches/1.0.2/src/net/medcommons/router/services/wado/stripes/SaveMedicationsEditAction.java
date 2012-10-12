/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.util.ArrayList;
import java.util.List;

import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Saves edits of a Medications section
 * 
 * @author ssadedin
 */
public class SaveMedicationsEditAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SaveMedicationsEditAction.class);
    
    
    private List<CCRElement> medications = new ArrayList<CCRElement>();
    
    public SaveMedicationsEditAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution save() throws Exception {
        try { 
            CCRElement medicationsSection = ccr.getRoot().getOrCreate("Body").getOrCreate("Medications");
            medicationsSection.removeContent();
            
            List<CCRElement> medications = this.ccr.create("medications", "Medication", this.ctx.getRequest().getParameterMap());
            for(CCRElement med : medications) {
                if(med != null) {
                    String objectId = ccr.generateObjectID();
                    log.info("Created medications with object id " + objectId);
                    med.createPath("CCRDataObjectID",objectId);
                    // TODO:  ID of patient is probably NOT the right thing here.  We should be using the
                    // MedCommons ID of the current user
                    String sourceActorID = XPathUtils.getValue(ccr.getJDOMDocument(),"patientActorID");
                    med.createPath("Source/Actor/ActorID",sourceActorID);
                    medicationsSection.addContent(med);
                }
            }
            return new JavaScriptResolution("ok");            
        }
        catch(Exception e) {
            log.error("Unable to save Medications",e);
            return new JavaScriptResolution(e.toString());            
        }
    }
}
