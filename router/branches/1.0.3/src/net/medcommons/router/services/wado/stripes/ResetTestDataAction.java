/*
 * $Id: ResetTestDataAction.java 3048 2008-11-04 23:17:03Z ssadedin $
 * Created on 17/01/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.TestDataConstants.DOCTOR_ID;
import static net.medcommons.modules.utils.TestDataConstants.USER1_ID;
import static net.medcommons.modules.utils.TestDataConstants.USER2_ID;

import java.io.File;
import java.io.IOException;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.web.stripes.ActionBeanContext;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;

/**
 * Resets the Test Data on this Gateway
 * 
 * @author ssadedin
 */
public class ResetTestDataAction implements ActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ResetTestDataAction.class);
    
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
        factory.getActivityLogService().clear(USER1_ID);
        ActivityEvent evt = new ActivityEvent(ActivityEventType.PHR_UPDATE, "PHR Created",new AccountSpec(USER1_ID), USER1_ID);
        factory.getActivityLogService().log(evt);
        
        // Remove profiles for test users
        for(String userId : new String[] {USER1_ID, USER2_ID, DOCTOR_ID}) {
            File f = new File("data/Repository/"+userId+"/profiles.xml");
            if(f.exists()) {
                log.info("Deleting profiles for test user " + userId);
                f.delete();
            }
        }
        
        indexService.clear(USER1_ID);
        indexService.clear(USER2_ID);
        indexService.clear(DOCTOR_ID);
        
        return new ForwardResolution("/demoDataReset.jsp");
    }
    
    /**
     * Make the test users completely flat broke.  Facilitates testing of 
     * scenarios where credit is needed to pay for an operation but
     * is not available.
     * 
     * @return
     */
    public Resolution broke() {
        return new StreamingResolution("text/plain","ok");
    }
    
    /**
     * Add large amounts of credit to test users, to allow testing of operations
     * that require payment.
     * @return
     */
    public Resolution loaded() {
        return new StreamingResolution("text/plain","ok");
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
