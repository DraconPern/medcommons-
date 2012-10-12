/*
 * Created on Jun 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.selection.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import sun.util.calendar.CalendarDate;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.configuration.ConfigurationException;
import net.medcommons.router.services.dicom.util.DICOMUtils;
import net.medcommons.router.services.dicom.util.MCStudy;

/**
 * Wraps a study for the purposes of user interface display in the selection screen
 * 
 * @author ssadedin
 */
public class SelectionStudy implements Serializable {
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(SelectionStudy.class);


  /**
   * The internal medcommons study being displayed
   */
  private MCStudy study;
  
  /**
   * Set to true if the user selected this study in the user interface
   */
  private boolean selected;

  /**
   * Creates a SelectionStudy
   * @param study
   */
  public SelectionStudy(MCStudy study) {
    this.study = study;
    this.selected = false;    
  }
  
  /**
   * @return Returns the selected.
   */
  public boolean isSelected() {
    return selected;
  }
  /**
   * @param selected The selected to set.
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }
  /**
   * @return Returns the study.
   */
  public MCStudy getStudy() {
    return study;
  }
  
  /**
   * @param study The study to set.
   */
  public void setStudy(MCStudy study) {
    this.study = study;
  }
  
  public Date getDate() {
    return DICOMUtils.parseDate(study.getStudyDate());
  }
  
  /**
   * Returns a URL for the study, as would be valid on the MedCommons site or a router.
   * 
   * @return
   * @throws ConfigurationException
   */
  public String getWadoUrl() /* throws ConfigurationException */ {
         
    String orderFormName;
    try {
      orderFormName = Configuration.getInstance().getConfiguredValue("net.medcommons.selection.web.order-form-name", "WADOViewer.jsp");
    } catch (ConfigurationException e) {
      log.warn("Unable to get configured value for order form.  Using default.");
      orderFormName = "WADOViewer.jsp";
    }
    
    // Hack city
    char [] shortGuid = study.mcGUID.substring(0,12).toUpperCase().toCharArray();
    
    String track = "";
    for (int i = 0; i < shortGuid.length; i++) {
      char c = shortGuid[i];
      if(c < 65) {
        track += c;
      }
      else {
        track += (c - 65);
      }      
    }
    
    String url;
    url = "/router/" + orderFormName + "?guid=" 
      + study.mcGUID
      + "&name=John+Smith" 
      + "&tracking="+ track + "&address=123%20Lucky%20St&state=MT&city=Butte&zip=83132&cardnumber=7817574478133225&amount=150.00&tax=12.00&charge=162.00&expiration=12/09&copyto=agropper@medcommons.org&comments=%20CERVICAL%20SPINE%20&history=%3cunknown%3e";
    return url;
  }  
  
  public void setWadoUrl(String url) {
    // do nothing - here only becuase struts insists on it. argh.
  }
  
}
