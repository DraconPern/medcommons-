/*
 * $Id$
 * Created on 17/01/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.DemoDataConstants.JANES_ID;
import static net.medcommons.modules.utils.DemoDataConstants.JBEWELL_ID;
import static net.medcommons.modules.utils.DemoDataConstants.STAYLOR_ID;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.services.interfaces.ProfileService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.DemoDataConstants;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.router.web.stripes.ActionBeanContext;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Resets the Demo Data on this Gateway
 * 
 * @author ssadedin
 */
public class ResetDemoDataAction implements ActionBean {
    
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ResetDemoDataAction.class);
    
    /**
     * Stripes context
     */
    private ActionBeanContext ctx;
    
    /**
     * Index service used to clear indexes for test users
     */
    private DocumentIndexService indexService = Configuration.getBean("documentIndexService");
    
    
    @DefaultHandler
    public Resolution reset() throws ServiceException, IOException {
        log.info("Clearing Demo Data"); 
        ServicesFactory factory = Configuration.getBean("systemServicesFactory");
        factory.getActivityLogService().clear(JANES_ID);
        ActivityEvent evt = new ActivityEvent(ActivityEventType.PHR_UPDATE, "PHR Created", new AccountSpec(JANES_ID), JANES_ID);
        factory.getActivityLogService().log(evt);
        
        
        // Create profiles for Demo patients
        for(String userId : new String[] {JANES_ID, JBEWELL_ID, STAYLOR_ID}) {
            File f = new File("data/Repository/"+userId+"/profiles.xml");
            if(f.exists()) {
                log.info("Deleting profiles for test user " + userId);
                f.delete();
            }
        }
        
        ProfileService profiles = Configuration.getBean("profilesService");
        profiles.createProfile(JANES_ID, new PHRProfile(AccountDocumentType.CURRENTCCR.name(),null));
        profiles.createProfile(JANES_ID, new PHRProfile(DemoDataConstants.DEMO_CCR_GUID2));
        
        profiles.createProfile(JBEWELL_ID, new PHRProfile(AccountDocumentType.CURRENTCCR.name(),null));
        profiles.createProfile(JBEWELL_ID, new PHRProfile(DemoDataConstants.JBEWELL_CCR_GUID2));
        
        profiles.createProfile(STAYLOR_ID, new PHRProfile(AccountDocumentType.CURRENTCCR.name(),null));
        
        
        indexService.clear(JANES_ID);
        indexService.clear(JBEWELL_ID);
        indexService.clear(STAYLOR_ID);
        
        // Should really add some documents back for the test users 
        // painful to do the full job though ...
        DocumentDescriptor desc = new DocumentDescriptor();
        desc.setCreationDate(new Date());
        desc.setStorageId(JANES_ID);
        desc.setSha1(DemoDataConstants.DEMO_CCR_GUID);
        desc.setContentType(CCRConstants.CCR_MIME_TYPE);
        desc.setLength(78000); // fake
        indexService.index(desc);
        
        desc.setSha1(DemoDataConstants.DEMO_PDF);
        desc.setContentType(DocumentTypes.PDF_MIME_TYPE);
        desc.setLength(91250);
        indexService.index(desc);
        
        return new ForwardResolution("/demoDataReset.jsp");
    }
    
    public void setContext(net.sourceforge.stripes.action.ActionBeanContext ctx) {
        this.ctx = (ActionBeanContext)ctx;
        ctx.getResponse().setHeader("Cache-Control","no-cache"); // HTTP 1.1
        ctx.getResponse().setHeader("Pragma","no-cache"); // HTTP 1.0
    }    
    
    public ActionBeanContext getContext() {
        return ctx;
    }
}
