/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.DocumentNotFoundException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.services.interfaces.TrackingReference;
import net.medcommons.modules.services.interfaces.TrackingService;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * Opens a given CCR in a new "Tab".  This really means
 * loading it into the Session, assigning it an index and 
 * setting a flag so that the new tab knows it is a "new" tab and therefore
 * causing it to modify its tab URL to reflect that.
 * 
 * @author ssadedin
 */
public class OpenCCRAction extends CCRActionBean {
    
    private String trackingNumber = null;
    
    private String pin = null;
    
    public OpenCCRAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution open() throws Exception {                
        
        if((trackingNumber == null) || (pin == null)) {
            throw new IllegalArgumentException("trackingNumber and pin are required");
        }
        
        TrackingService trackingService = this.session.getServicesFactory().getTrackingService();        
        TrackingReference ref = trackingService.validate(trackingNumber, PIN.hash(pin));
        if(ref == null) {
            throw new DocumentNotFoundException("No valid document found for tracking number " + trackingNumber + " with provided pin");
        }
        
        // TODO:  should check that the reference is for this gateway - it could be for another
        
        String guid = ref.getDocument().getGuid();
        CCRDocument ccr =(CCRDocument) RepositoryFactory.getLocalRepository().queryDocument(ref.getDocument().getStorageAccountId(), guid);
        ccr.setTrackingNumber(trackingNumber);
        
        session.getCcrs().add(ccr);
        ctx.getRequest().setAttribute("ccrIndex", String.valueOf(session.getCcrs().indexOf(ccr)));
        ctx.getRequest().setAttribute("ccrOpened",Boolean.TRUE);
        
        return new ForwardResolution("/viewEditCCR.do");
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

}
