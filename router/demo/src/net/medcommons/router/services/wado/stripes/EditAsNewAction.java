/*
 * $Id$
 * Created on 05/07/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.medcommons.router.web.stripes.RequiresLogin;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * Copies the existing CCR and opens it in Edit mode as a new
 * CCR that will not get merged back to the Current CCR. 
 * 
 * @author ssadedin
 */
@RequiresLogin
public class EditAsNewAction extends CCRActionBean {
    
    private boolean restore = false;
    
    @DefaultHandler
    public Resolution editNew() throws Exception {
        UserSession.required(ctx.getRequest());
       
       this.ccr.syncFromJDom();
       
       // Copy the existing CCR
       CCRDocument newCCR = this.ccr.copy();
       session.getCcrs().add(newCCR);
       
       // Set to NEWCCR logical type
       if(!blank(newCCR.getPatientMedCommonsId())) {
           newCCR.setStorageMode(StorageMode.LOGICAL);
           newCCR.setLogicalType(AccountDocumentType.NEWCCR);
       }
       else {
           newCCR.setStorageMode(StorageMode.SCRATCH);
           newCCR.setLogicalType(null);
       }
       newCCR.setGuid(null);
       
       // Ensure it has up to date timestamp
       newCCR.setCreateTimeMs(System.currentTimeMillis());
        
       // Set it as the active ccr
       session.setActiveCCR(ctx.getRequest(), newCCR);
       
       // Hack: we want to restore the original source CCR.  To do this,
       // we need to purge it and reload from repository
       String guid = this.ccr.getLoadedFromGuid();
       if(restore && !blank(guid)) {  
           int index = session.getCcrs().indexOf(this.ccr);
           session.getCcrs().set(index,null);
           this.ccr = session.resolve(guid);
           session.getCcrs().set(index, this.ccr);
       }
       
       return new ForwardResolution("/viewEditCCR.do"); 
    }

    public boolean getRestore() {
        return restore;
    }

    public void setRestore(boolean reload) {
        this.restore = reload;
    }

}
