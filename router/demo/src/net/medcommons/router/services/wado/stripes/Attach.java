/*
 * $Id: Attach.java 2971 2008-10-21 06:47:21Z ssadedin $
 * Created on 24/03/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * Attaches an uploaded document to a user's Current CCR
 * 
 * @author ssadedin
 */
public class Attach extends BaseActionBean {
    
    /**
     * URL to which user will be redirected
     */
    String callback;
   
    /**
     * File to be uploaded
     */
    FileBean file;
    
    private LocalFileRepository repository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
    
     @DefaultHandler
    public Resolution attach() throws Exception {
        
        // 1. Get the user's Current CCR
        String currentCCRGuid = this.session.getAccountSettings().getCurrentCcrGuid();
        if(blank(currentCCRGuid))
            throw new IllegalArgumentException("User " + this.session.getOwnerMedCommonsId() + " does not have a Current CCR");
        
        CCRDocument ccr = this.session.resolve(currentCCRGuid);
        
        assert ccr != null : "Expected CCR " + currentCCRGuid + " not found";
        
        // 2. Store the attachment
        String fileGuid = repository.putDocument(session.getOwnerMedCommonsId(), file.getInputStream(), file.getContentType());
        file.delete();
        
        // 3. Add the reference
        MCSeries series = ccr.createReferenceSeries(currentCCRGuid, session.getOwnerMedCommonsId(), fileGuid, file.getContentType());
        ccr.addReference(series);
        
        // 4. Save current CCR
        StoreTransaction tx = session.tx(ccr);
        tx.registerDocument(null);
        tx.storeDocument();
        tx.writeActivity(ActivityEventType.PHR_UPDATE, "Added reference " + file.getFileName() + " to Current CCR");
        
        if(!blank(callback))
            return new RedirectResolution(callback, false);
        else
            return new ForwardResolution("/attach_document_result.ftl");
    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

}
