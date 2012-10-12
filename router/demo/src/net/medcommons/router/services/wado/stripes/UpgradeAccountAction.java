/*
 * $Id: UpgradeAccountAction.java 3487 2009-09-11 07:20:28Z ssadedin $
 * Created on 09/07/2008
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.urlencode;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * Calculates a redirect URL that embarks the user on the process of
 * upgrading their account.
 * 
 * @author ssadedin
 */
public class UpgradeAccountAction extends CCRActionBean {
    
    @DefaultHandler
    public Resolution upgrade() throws Exception {
        String accountsBaseUrl = Configuration.getProperty("AccountsBaseUrl");
        // AccountSettings settings = session.getAccountSettings(ccr.getPatientMedCommonsId());
        // String purchaseUrl = accountsBaseUrl + "mod/voucherhome.php?vcopy&c="+settings.getVoucherCouponNum() + "&o=" + settings.getVoucherOtpHash() ;
        
        String src =  accountsBaseUrl + ccr.getPatientMedCommonsId();
        
        String importUrl = "AccountImport.action?sourceUrl=" +
                           urlencode(src)+"&sourceAuth=" +
                           urlencode(session.getAuthenticationToken()) +
                           "&auto=true";
        
        String nextUrl = "gwredir.php?dest="+urlencode(importUrl);
        
        return new RedirectResolution(accountsBaseUrl + "acct/login.php?next=" + urlencode(nextUrl), false);
    }

}
