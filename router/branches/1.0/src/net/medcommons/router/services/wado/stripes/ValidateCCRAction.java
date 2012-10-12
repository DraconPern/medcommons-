/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * Validates the current CCR and displays a screen indicating if it is ok or not 
 * @author ssadedin
 */
public class ValidateCCRAction extends CCRActionBean {
	
    public ValidateCCRAction() {
        super();
    } 
     
    @DefaultHandler
    public Resolution validate() throws Exception {
        if (this.ccr == null){
        	throw new NullPointerException("Null CCR");
        }
        else if (this.ccr.getStorageId() == null){
        	throw new NullPointerException("Null storageId for CCR");
        }
       
        CCRDocument validCcr = new CCRDocument(this.ccr.getStorageId(), "","",this.ccr.getXml(),CCRDocument.SCHEMA_VALIDATION_LENIENT);
        this.ctx.getRequest().setAttribute("validationErrors", validCcr.getSchemaValidationMessages());
        this.ctx.getRequest().setAttribute("ccrIndex",session.getCcrs().indexOf(this.ccr));
        return new ForwardResolution("validationResults.jsp");
    }    
}
