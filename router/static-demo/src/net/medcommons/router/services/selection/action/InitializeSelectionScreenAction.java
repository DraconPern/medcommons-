//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.selection.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.router.configuration.ConfigurationException;
import net.medcommons.router.services.dicom.util.StudyMetadataManager;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/** 
 * XDoclet definition:
 * @struts:action validate="true"
 */
public class InitializeSelectionScreenAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger
      .getLogger(InitializeSelectionScreenAction.class);

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
      throws ConfigurationException, SelectionException {

    log.info("Initializing Selection Screen");

    String dest = (String) Configuration.getInstance().getConfiguredValue(
        "net.medcommons.dicom.directory");
    File rootDirectory = new File(dest);
    log.debug("Dicom root directory is " + rootDirectory);
    if (!rootDirectory.exists())
      throw new SelectionException("Unable to locate dicom directory: " + dest);

    StudyMetadataManager studyMetadata = new StudyMetadataManager();
    try {
      studyMetadata.scan(rootDirectory);
    } catch (IOException e) {
      throw new SelectionException("Unable to scan dicom directory: " + dest, e);
    }

    String[] guids = studyMetadata.guidQuery();
    if (guids == null) {
      guids = new String[0];
    }
    
    log.info("Found " + guids.length +  " guids");
    
    int numberOfStudies = guids.length;
    StudyListForm studyForm = new StudyListForm();
    ArrayList studies = new ArrayList(guids.length);
    for (int i = 0; i < guids.length; i++) {
      studies.add(new SelectionStudy(studyMetadata.getStudyWithGUID(guids[i])));      
    }    
    studyForm.setStudies(studies);
    request.getSession().setAttribute("studyForm", studyForm);

    return mapping.findForward("success");
  }

}