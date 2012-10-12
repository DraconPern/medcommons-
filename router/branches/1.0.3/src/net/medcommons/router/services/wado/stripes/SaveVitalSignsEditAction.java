/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.util.ArrayList;
import java.util.List;

import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

/**
 * Saves edits of a VitalSigns section
 * 
 * @author ssadedin
 */
public class SaveVitalSignsEditAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SaveVitalSignsEditAction.class);
    
    
    private List<CCRElement> vitalSigns = new ArrayList<CCRElement>();
    
    public SaveVitalSignsEditAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution save() throws Exception {
        try { 
            CCRElement vs =  
                ccr.getOrCreate((CCRElement)ccr.getJDOMDocument().getRootElement(),"Body").getOrCreate("VitalSigns");
            vs.removeContent();
            List<CCRElement> results = this.ccr.create("vitalsigns", "Result", this.ctx.getRequest().getParameterMap());
            for(CCRElement result : results) {
                if(result != null) {
                    result.createPath("CCRDataObjectID",ccr.generateObjectID());
                    result.createPath("Test/CCRDataObjectID",ccr.generateObjectID());
                    // TODO:  ID of patient is probably NOT the right thing here.  We should be using the
                    // MedCommons ID of the current user
                    String sourceActorID = XPathUtils.getValue(ccr.getJDOMDocument(),"patientActorID");
                    result.createPath("Source/Actor/ActorID",sourceActorID);
                    result.createPath("Test/Source/Actor/ActorID",sourceActorID);
                    vs.addContent(result);
                }
            }
            return new JavaScriptResolution("ok");            
        }
        catch(Exception e) {
            log.error("Unable to save VitalSigns",e);
            return new JavaScriptResolution(e.toString());            
        }
    }
}
