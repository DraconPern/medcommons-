/*
 * $Id: UploadFileForm.java 2832 2008-08-15 19:26:36Z sdoyle $
 * Created on Jan 14, 2005
 */
package net.medcommons.router.services.wado.actions;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.utils.Str;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;


/**
 * A simple form that lets a user upload a file
 * @author ssadedin
 */
public class UploadFileForm extends ActionForm {

  /**
   * Allows a comment with the upload.
   */
  private String comment;
    
  private FormFile uploadedFile = null;
  
  private String documentType;
    
  private String returnUrl;
  
  /**
   * 
   */
  public UploadFileForm() {
    super();
  }   

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = null;
        if(Str.blank(this.uploadedFile.getFileName())) {
            errors = new ActionErrors(); 
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("medcommons.fileRequired"));
        }
        return errors;
    }

    /**
     * 
     * @uml.property name="uploadedFile"
     */
    public FormFile getUploadedFile() {
        return uploadedFile;
    }

    /**
     * 
     * @uml.property name="uploadedFile"
     */
    public void setUploadedFile(FormFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    

}
