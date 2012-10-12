package net.medcommons.router.services.wado.stripes;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.*;
import net.medcommons.router.services.wado.DocumentNotFoundException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.medcommons.router.web.stripes.ZipResolution;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;

@UrlBinding("/document")
public class DisplayDocumentAction extends CCRActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DisplayDocumentAction.class);
    
    @Validate(required=true, mask=GUID_PATTERN)
    String guid;
    
    DocumentIndexService indexService = Configuration.getBean("documentIndexService");
    
	LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();

    @DefaultHandler
    public Resolution display() throws Exception {
        
        checkAccess();
        
        // Find the content type
        String patientId = Str.bvl(ccr.getPatientMedCommonsId(), ServiceConstants.PUBLIC_MEDCOMMONS_ID);
        DocumentDescriptor reference = indexService.getDocument(patientId, guid);
        String contentType = reference.getContentType();
        if("image/jpg".equals(contentType))
            contentType="image/jpeg"; 
        
        return new ZipResolution(new StreamingResolution(contentType, repo.getDocument(patientId, guid)));
    }

    private void checkAccess() throws Exception {
        for(CCRDocument c : session.getCcrs()) {
            for(MCSeries s : c.getSeriesList()) {
                if(guid.equals(s.getMcGUID()) &&  s.isInSession()) {
                    log.info("Found guid " + guid + " in current session");
                    return;
                }
            }
        }
        
        // Verify that the document being rendered is available to the current user
        DocumentResolver resolver = new DocumentResolver(session.getServicesFactory());
        if(!resolver.canResolve(session.getOwnerMedCommonsId(), guid)) 
            throw new DocumentNotFoundException();
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
    
}
