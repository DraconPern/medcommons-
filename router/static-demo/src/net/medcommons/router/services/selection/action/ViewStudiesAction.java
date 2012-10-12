//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.selection.action;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.configuration.ConfigurationException;
import net.medcommons.router.services.dicom.util.MCStudy;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/** 
 * XDoclet definition:
 * @struts:action validate="true"
 */
public class ViewStudiesAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(ViewStudiesAction.class);

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
      throws ConfigurationException, SelectionException 
  {

    log.info("Viewing studies");
  
    StudyListForm studyForm = (StudyListForm) form;

       
    String url;
    String guids = "";
    SelectionStudy firstSelectedStudy = null;
    for (Iterator iter = studyForm.getStudies().iterator(); iter.hasNext();) {
      SelectionStudy selStudy = (SelectionStudy)iter.next();
      if(selStudy.isSelected()) {
        log.info("Study " + selStudy.getStudy().getMcGUID() 
            + "[" + selStudy.getStudy().getStudyDescription() + " selected");
        guids += "&guids=" + selStudy.getStudy().getMcGUID();
        if(firstSelectedStudy == null)
          firstSelectedStudy = selStudy;
      }
    }
    
    if(firstSelectedStudy == null) {
      return mapping.findForward("nostudy");
    }
    
    url = firstSelectedStudy.getWadoUrl();    
    
    url += guids;
    
    try {
      log.info("Redirecting to " + url);
      response.sendRedirect(response.encodeUrl(url));
    } 
    catch (IOException e) {
      throw new SelectionException("Unable to redirect to WADO viewer", e);
    }
    
    return null;
  }
}