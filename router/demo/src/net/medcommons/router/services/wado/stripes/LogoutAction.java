/*
 * $Id: LogoutAction.java 3886 2010-10-21 07:30:05Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.*;

import org.apache.log4j.Logger;

/**
 * Updates the current CCR in the session and 
 */
public class LogoutAction extends BaseActionBean {

  /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(LogoutAction.class);

	@DefaultHandler
	public Resolution execute() throws Exception {      
        
        if(session != null)
          log.info("Logging out user " + session.getOwnerMedCommonsId());
        else 
          log.info("Logging out user with no desktop");
        
        ctx.getRequest().setAttribute("message", "You have been logged out.");
        
        ctx.getRequest().getSession().invalidate();
        
        return new RedirectResolution(Configuration.getProperty("AccountsBaseUrl")+"acct/logout.php", false);
	}
}
    

