/*
 * $Id: ImportAction.java 3652 2010-04-02 17:45:49Z ssadedin $
 * Created on 17/10/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRReferenceElement;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

import org.jdom.filter.ElementFilter;

public class ImportAction extends CCRActionBean {
    
    private String idaction = "replace";
    
    @Validate(required=true,  mask=MCID_PATTERN)
    private String patientId;
    
    /**
     * If set to true, the 'clean patient' flag will be set which will enable
     * the prompt to 'save as new patient' when the patient is saved
     */
    private boolean clean = false;
    
    public Resolution checkId() {
        return new ForwardResolution("/checkImportId.ftl"); 
    }
    
    @DefaultHandler
    public Resolution updateId() throws Exception {
        
        if("cancelImport".equals(idaction))
            return cancelImport();
        
        CCRActorElement patientActor = this.ccr.getPatientActor();
        patientActor.removeAccountId();
        
        if(clean) { 
            ctx.getRequest().getSession().setAttribute("cleanPatient", "true");
            ccr.setNewCcr(true);
            ccr.setGuid(null);
            ccr.setStorageId(ServiceConstants.PUBLIC_MEDCOMMONS_ID);
        }
        
        if("replace".equals(idaction)) {
            ccr.setStorageId(patientId);
            ccr.addPatientId(patientId, CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE);
        }
           
        return checkReferences();
    }
    
    DocumentIndexService index = Configuration.getBean("documentIndexService");
    
    List<CCRReferenceElement> missingSeries = new ArrayList<CCRReferenceElement>();
        
    public Resolution checkReferences() throws Exception {
        
        // Check each reference to see if it can be resolved in the context
        // of this user's account
        findMissingReferences();
        
        // Hack: needed be FreeMarker templates - TODO: remove
        ctx.getRequest().setAttribute("ccrIndex", String.valueOf(this.getCcrIndex()));
        
        if(missingSeries.isEmpty())
            return showCCRResolution();
        else
            return new ForwardResolution("/missingReferences.ftl");
    }

    /**
     * Find all references in the active CCR that the patient
     * does not have have access to.
     * 
     * @throws RepositoryException
     * @throws PHRException 
     */
    private void findMissingReferences() throws RepositoryException, PHRException {
        
        CCRElement refs = ccr.getReferences();
        if(refs == null)
            return;
        
        for(CCRElement e : refs.getChildren("Reference")) {
            CCRReferenceElement ref = (CCRReferenceElement) e;
            if(ref.getGuid() != null && index.getDocument(ccr.getStorageId(),ref.getGuid()) == null) {
                missingSeries.add(ref);
            }
        }
    }
    
    /**
     * Remove all references from the active CCR that the patient 
     * does not have access to.
     * 
     * @return
     * @throws Exception
     */
    public Resolution removeReferences() throws Exception {
        
        findMissingReferences();
        
        List<CCRElement> refs = ccr.getReferences().getChildren("Reference");
        for(CCRReferenceElement ref : missingSeries) {
            
            if(blank(ref.getGuid()))
                continue;
            
            for(ListIterator<MCSeries> i = ccr.getSeriesList().listIterator(); i.hasNext();) {
                MCSeries s = i.next();
                if(s.getMcGUID() != null && s.getMcGUID().equals(ref.getGuid()))
                    i.remove();
            }
            
            refs.remove(ref); 
        }
        
        if(refs.isEmpty())
            ccr.getRoot().removeChild("References");
        
        return showCCRResolution();
    }
    
    /**
     * Output flag - set to true if display should remove the current tab
     * displayed in the UI.
     */
    private boolean removeTab = false;
    
    /**
     * Cancel the current import operation and fall back to show
     * one of the user's other open tab, or create a new CCR 
     * @return
     */
    public Resolution cancelImport() {
        
        session.getCcrs().remove(this.ccr); 
        
        // Is there another CCR to revert back to?
        if(session.getCcrs().isEmpty()) {
            return new ForwardResolution("/NewCCR.action");
        }
        
        this.session.setActiveCCR(ctx.getRequest(), session.getCcrs().get(0));
        this.removeTab = true;
        
        return new ForwardResolution("/viewEditCCR.do");
    }

    private Resolution showCCRResolution() {
        ctx.getRequest().setAttribute("ccrIndex", session.getActiveCCRIndex(ctx.getRequest()));
        ctx.getRequest().setAttribute("view", "0e");
        return new ForwardResolution("/resetView.ftl"); 
        
        // return new ForwardResolution("/viewEditCCR.do");
    }
    
    @Override
    public void setContext(ActionBeanContext ctx) {
        super.setContext(ctx);
        if(ctx.getRequest().getAttribute("patientIdContext") != null)
            this.patientId = (String) ctx.getRequest().getAttribute("patientIdContext");
    }

    public String getIdaction() {
        return idaction;
    }

    public void setIdaction(String idaction) {
        this.idaction = idaction;
    }


    public boolean getRemoveTab() {
        return removeTab;
    }

    public void setRemoveTab(boolean removeTab) {
        this.removeTab = removeTab;
    }

    public List<CCRReferenceElement> getMissingSeries() {
        return missingSeries;
    }

    public void setMissingSeries(List<CCRReferenceElement> missingSeries) {
        this.missingSeries = missingSeries;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

}
