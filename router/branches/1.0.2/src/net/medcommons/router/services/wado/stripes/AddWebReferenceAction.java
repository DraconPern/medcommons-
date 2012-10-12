package net.medcommons.router.services.wado.stripes;

import net.medcommons.router.web.stripes.CCRJSONActionBean;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Adds a Web Reference to the active CCR
 * 
 * @author ssadedin
 */
public class AddWebReferenceAction extends CCRJSONActionBean {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(AddWebReferenceAction.class);

  /**
   * The URL of the web reference to be added
   */
  @Validate(required=true, mask="^https{0,1}://.*")
  private String webRefUrl;

  @DefaultHandler
  public Resolution add() throws Exception {
      log.info("Adding web reference " + webRefUrl + " to CCR " + this.ccr.getGuid());
      ccr.addURLReference(webRefUrl);
      ccr.setGuid(null);
      return new JSONResolution();
  }
  
  public String getWebRefUrl() {
      return webRefUrl;
  }
  
  public void setWebRefUrl(String webRefUrl) {
      this.webRefUrl = webRefUrl;
  }
}
