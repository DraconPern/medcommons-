//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jdom.Document;
import org.jdom.Element;
import org.json.JSONObject;

/**
 * Initializes the page to allow the user to add a document to a series
 * 
 * @author ssadedin
 */
public class DeletePatientIdAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(DeletePatientIdAction.class);


  /**
   * Method execute
   * 
   * @return ActionForward
   * @throws
   * @throws ConfigurationException -
   *           if configuration cannot be accessed
   * @throws SelectionException -
   *           if a problem scanning the selections occurs
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {      
      JSONObject obj = new JSONObject();
      response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
      response.setHeader("Pragma","no-cache"); // HTTP 1.0
      String patientIdIndex = request.getParameter("patientIdIndex");
      try {
          UserSession desktop = UserSession.required(request);
          XPathCache xpath =(XPathCache) Configuration.getBean("ccrXPathCache");

          log.info("Deleting patient id = " + patientIdIndex);
          CCRDocument ccr = desktop.getCurrentCcr(request);
          Document doc = ccr.getJDOMDocument();
          String patientMCID = ccr.getPatientMedCommonsId();
          
          // If an id already exists with this type, update the existing one
          HashMap vars = new HashMap();
          vars.put("index", String.valueOf(Long.parseLong(patientIdIndex)+1));          
          Element existingPatientId = xpath.getElement(doc, "patientIdByIndex", vars);
          if(existingPatientId!=null) {
              existingPatientId.getParentElement().removeContent(existingPatientId);
          }
          else
              throw new IllegalArgumentException("Invalid Patient Id Index - no ID with index " + patientIdIndex + " found" ); 
          
          obj.put("status","ok"); 
          String newPMCID = ccr.getPatientMedCommonsId();
          if(!Str.equals(patientMCID,newPMCID)) {
              obj.put("newPatientId", newPMCID);
              obj.put("writeable", desktop.checkPermissions(newPMCID, "W"));
          }
      }
      catch(Exception e) {
          log.error("Unable to delete patient id with index " + patientIdIndex, e);
          obj.put("status","failed");
          obj.put("error",e.getMessage());
      }
      response.getOutputStream().print(obj.toString());
    
    return null;
  }
}
