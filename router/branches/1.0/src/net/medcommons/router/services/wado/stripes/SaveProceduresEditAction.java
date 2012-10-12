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
 * Saves edits of a Procedures section
 * 
 * @author ssadedin
 */
public class SaveProceduresEditAction extends CCRActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SaveProceduresEditAction.class);
    
    
    private List<CCRElement> procedures = new ArrayList<CCRElement>();
    
    public SaveProceduresEditAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution save() throws Exception {
        try { 
            CCRElement proceduresSection =   
                ccr.getOrCreate((CCRElement)ccr.getJDOMDocument().getRootElement(),"Body").getOrCreate("Procedures");
            proceduresSection.removeContent();
            
            List<CCRElement> procedures = this.ccr.create("procedures", "Procedure", this.ctx.getRequest().getParameterMap());
            for(CCRElement proc : procedures) {
                if(proc != null) {
                    proc.createPath("CCRDataObjectID",ccr.generateObjectID());
                    // TODO:  ID of patient is probably NOT the right thing here.  We should be using the
                    // MedCommons ID of the current user
                    String sourceActorID = XPathUtils.getValue(ccr.getJDOMDocument(),"patientActorID");
                    proc.createPath("Source/Actor/ActorID",sourceActorID);
                    proceduresSection.addContent(proc);
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
