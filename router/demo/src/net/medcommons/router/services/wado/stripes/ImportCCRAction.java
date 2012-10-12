/*
 * $Id: ImportCCRAction.java 3841 2010-09-13 12:34:23Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.empty;
import static net.medcommons.modules.utils.Str.nvl;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.TrackingService;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.event.Listener;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.wado.AccessMode;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Accepts a file upload of a CCR and imports it into MedCommons.  The
 * file is not saved, but the user is shown a view where they
 * can choose to do that.
 * 
 * @param uploadedFile - a multipart form parameter containing file 
 *                       data to import
 * @author ssadedin */
public class ImportCCRAction extends BaseActionBean {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ImportCCRAction.class);
    
    
    @Validate(required=true)
    protected FileBean uploadedFile;
    
    public ImportCCRAction() {
    }

    @DefaultHandler
    public Resolution upload() throws Exception {

        log.info("Importing new CCR");
        
        HttpServletRequest request = this.ctx.getRequest();
        
        log.info("Got file data: " + uploadedFile.getFileName());
        
        boolean newDesktop = !UserSession.has(request);
        final UserSession desktop = UserSession.get(request);
        
        CCRDocument newCCR;
        try {
            newCCR = extractCCR();
        }
        catch(CCRImportException e) {
            request.setAttribute("message", session.getMessage(e.getMessageKey()));
            request.setAttribute("errormessage", e.getMessage());
            return ctx.getSourcePageResolution();
        }
        
        newCCR.setStorageMode(StorageMode.SCRATCH);
        
        if(newDesktop) {            
            // Get a new tracking number
            RESTProxyServicesFactory factory = new RESTProxyServicesFactory(null);            
            TrackingService trackingService = factory.getTrackingService();
            String trackingNumber = trackingService.allocateTrackingNumber();                          
            newCCR.setTrackingNumber(trackingNumber);
        }
       
        if(empty(desktop.getReplyPin()))
            desktop.setReplyPin(PIN.generate());
        
        // HACK: if existing desktop, patient mode is not picked up
        if(Str.equals(request.getParameter("am"), "p")) { 
            desktop.setAccessMode(AccessMode.PATIENT);
            desktop.getCcrs().add(newCCR);
        }
        newCCR.setNewCcr(true);
        
        // If access mode is 'p' for patient, put the account id of the user as the patient id
        if(desktop.isPatientMode() && desktop.hasAccount()) { 
            // If patient does not yet have a current ccr, make this their current ccr automatically
            if(blank(desktop.getAccountSettings().getCurrentCcrGuid())) {
                
                boolean noCurrentCCR = true;
                for(CCRDocument ccr : desktop.getCcrs()) {
                    if(ccr.getLogicalType() == AccountDocumentType.CURRENTCCR) {
                        noCurrentCCR = false;
                    }
                }
                
                if(noCurrentCCR) {
                    newCCR.setStorageMode(StorageMode.LOGICAL);
                    newCCR.setLogicalType(AccountDocumentType.CURRENTCCR);
                }
            }
        }
        
        request.setAttribute("resetTabText", Boolean.TRUE); 
        
        newCCR.getEvents().subscribe("save", newCCR, new Listener<CCRDocument>() {
            public void onEvent(CCRDocument ccr) throws Exception {
                String patientId = ccr.getPatientMedCommonsId();
                if(!blank(patientId)) {
                    ActivityEvent event = new ActivityEvent(ActivityEventType.PHR_UPDATE, "CCR Imported", desktop.getOwnerPrincipal(), patientId, ccr.getTrackingNumber(),desktop.getReplyPin());
                    desktop.getServicesFactory().getActivityLogService().log(event);  
                    ccr.getEvents().unsubscribe("save", ccr, this);
                }
            }
        }); 
        
        String patientId = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
        if(!desktop.isPatientMode()) {
            CCRDocument ccr = session.getCurrentCcr(ctx.getRequest());
            if(ccr != null)
                patientId = ccr.getPatientMedCommonsId();
            else
                patientId = nvl(newCCR.getPatientMedCommonsId(), patientId);
        }
        
       session.setActiveCCR(request,newCCR);
        
       if(!Str.equals(patientId, newCCR.getPatientMedCommonsId()) && !blank(newCCR.getPatientMedCommonsId())) {
           
            if(desktop.isPatientMode() && desktop.hasAccount()  && blank(newCCR.getPatientMedCommonsId())) {
                newCCR.addPatientId(desktop.getOwnerMedCommonsId(), CCRConstants.MEDCOMMONS_PATIENT_ID_TYPE);
            }
            else
                return new ForwardResolution(ImportAction.class,"checkId").addParameter("patientId", patientId);
       } 
       
       if(!desktop.checkPermissions(patientId, "W")) {
           request.setAttribute("patientId", patientId);
           return new ForwardResolution("/inaccessibleImportId.ftl");
       }

       return new ForwardResolution(ImportAction.class, "checkReferences").addParameter("patientId", patientId);
    }

    protected CCRDocument extractCCR() throws CCRImportException {
        // Todo: how can we detect the encoding of the file?
        String ccrXml = null;
        try {
            ccrXml = IOUtils.toString(uploadedFile.getInputStream());
        
            // Check that it seems to be a valid CCR
            CCRDocument.isCcr(ccrXml.getBytes("UTF-8")); // Not a CCR!
            CCRDocument newCCR =
                new CCRDocument(session.getOwnerMedCommonsId(),
                                null, 
                                null, 
                                ccrXml, 
                                CCRDocument.SCHEMA_VALIDATION_STRICT);
            return newCCR;
        }
        catch(InvalidCCRException e) {
            throw new CCRImportException("medcommons.invalidCcr", e);
        }
        catch (PHRException e){
            log.error("Error parsing CCR" , e);
            log.error(ccrXml);
            throw new CCRImportException("medcommons.invalidCcr", e);
        }
        catch (JDOMException e){
            log.error("Error parsing CCR" , e);
            log.error(ccrXml);
            throw new CCRImportException("medcommons.invalidCcr", e);
        }
        catch (RepositoryException e) {
            throw new CCRImportException("medcommons.invalidCcr", e);
        }
        catch (IOException e) {
            throw new CCRImportException("medcommons.invalidCcr", e);
        }
        catch (ParseException e) {
            throw new CCRImportException("medcommons.invalidCcr", e);
        }
    }

    public FileBean getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    
}