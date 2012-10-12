/*
 * $Id: VoucherAction.java 3321 2009-04-22 05:42:30Z ssadedin $
 * Created on 14/08/2008
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.rest.RESTUtil.RestCall;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class VoucherAction extends CCRJSONActionBean {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(VoucherAction.class);
    
    @DefaultHandler
    public Resolution complete()  throws Exception {
        String url = Configuration.getProperty("AccountsBaseUrl") + "/mod/vouchercomplete.php";
        RestCall call = new RestCall(url, session.getAuthenticationToken(), "accid", ccr.getPatientMedCommonsId(), "fmt","json");
        JSONObject obj = call.fetchJSONResponse();
        if(!"ok".equals(obj.getString("status")))
            throw new CCROperationException("Unable to complete voucher for account " + ccr.getPatientMedCommonsId());
        return new StreamingResolution("text/json",result.toString());
    }
}
