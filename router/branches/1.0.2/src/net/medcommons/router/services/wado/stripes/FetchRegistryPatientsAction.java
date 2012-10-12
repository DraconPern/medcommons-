//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.stripes;

import java.util.Iterator;

import net.medcommons.router.web.stripes.JSONActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Queries the currently configured registry for patients belonging to the
 * current provider and returns them in JSON form.
 * 
 * @author ssadedin
 */
public class FetchRegistryPatientsAction extends JSONActionBean {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(FetchRegistryPatientsAction.class);


  @DefaultHandler
  public Resolution fetch() throws Exception {
      
      String registry = session.getAccountSettings().getRegistry();
      log.info("Fetching patients from registry " + registry); 
      
      Document results = session.getServicesFactory().getSecondaryRegistryService().queryRLS(null,null,null,null,null,null,null,null,"0", registry);
      
      JSONArray patients = new JSONArray();
      Iterator entries = results.getDescendants(new ElementFilter("RLSentry"));
      for (Iterator iter = entries; iter.hasNext();) {
          Element entry = (Element)iter.next();
          JSONObject patientObj = new JSONObject();
          for (Iterator entryChildren = entry.getChildren().iterator(); entryChildren.hasNext();) {
              Element prop = (Element) entryChildren.next();
              patientObj.put(prop.getName(), prop.getTextTrim());
          }
          patients.put(patientObj);
      }
      
      result.put("patients",patients);
      result.put("status","ok"); 
          
      return new StreamingResolution("text/javascript",result.toString());
  }
}
