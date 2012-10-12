/*
 * Created on Jun 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.medcommons.router.services.selection.action;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @author ssadedin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StudyListForm extends ActionForm {

  /**
   * Array of MCStudy objects visible on the selection screen
   */
  private ArrayList studies = new ArrayList(10);
  
  /**
   * Whether or not each study is selected
   */
  private ArrayList selected = new ArrayList(10);
  
  
  
  /**
   * 
   */
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {

    // Set all the selected studies to unselected
    for (Iterator iter = this.studies.iterator(); iter.hasNext();) {
      SelectionStudy study = (SelectionStudy) iter.next();
      study.setSelected(false);
    }
  }
  /**
   * @return Returns the studies.
   */
  public ArrayList getStudies() {
    return studies;
  }
  /**
   * @param studies The studies to set.
   */
  public void setStudies(ArrayList studies) {
    this.studies = studies;
  }
  
  /**
   * @return Returns the selected.
   */
  public ArrayList getSelected() {
    return selected;
  }
  /**
   * @param selected The selected to set.
   */
  public void setSelected(ArrayList selected) {
    this.selected = selected;
  }
}
