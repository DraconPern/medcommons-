/*
 * $Id: AccountDocumentAction.java 2664 2008-06-23 04:34:12Z ssadedin $
 * Created on 16/11/2007
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.BufferedInputStream;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;

/**
 * Supports functions to select / upload and then add a given document to a user's
 * account as one of the predefined document types.
 * 
 * @author ssadedin
 */
public class AccountDocumentAction extends BaseActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AccountDocumentAction.class);
    
    /// INPUTS
    private String storageId;
    
    private String comment;
    
    private String returnUrl;
    
    @Validate(on={"add"}, required=true)
    private FileBean uploadedFile;
    
    private String documentType;
    
    /// OUTPUTS
    private String message;
    
    @DefaultHandler
    public Resolution select() throws NotLoggedInException {
        UserSession.required(ctx.getRequest());
        if(!this.session.hasAccount()) {
            throw new NotLoggedInException();
        }
        
        if(blank(storageId)) {
            storageId = session.getOwnerMedCommonsId();
        }
        
        return new ForwardResolution("/accountDocument.jsp");
    }
   
   /**
    * Add specified document to repository
    */
   public Resolution add() throws Exception {
       
        if(blank(storageId)) {
            storageId = session.getOwnerMedCommonsId();
        }
        
       log.info("User " + session.getOwnerMedCommonsId()+ " Adding document to account " + storageId);
       
       LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();
       
       ServicesFactory svc = session.getServicesFactory();
       
       SHA1 sha1 = new SHA1();
       sha1.initializeHashStreamCalculation();
       
       BufferedInputStream bstream = new BufferedInputStream(uploadedFile.getInputStream());
       
       bstream.mark((int)uploadedFile.getSize()+1);
       
       String guid = sha1.calculateHashString(bstream);
       
       bstream.reset();
       
       svc.getDocumentService().registerDocument(guid, storageId);
       
       guid = repo.putDocument(storageId, bstream, uploadedFile.getContentType());
       
       bstream.close();
       
       uploadedFile.delete();

       log.info("Created repository document with name " + guid);    
       
       svc.getAccountService().addAccountDocument(storageId, guid, AccountDocumentType.valueOf(documentType), comment, true, null);
       
       svc.getActivityLogService().log(new ActivityEvent(ActivityEventType.ACOUNT_DOCUMENT_ADDED, "Document added to your Account",session.getOwnerPrincipal(), storageId, null, null));
       
       this.message = "Your document was successfully added.";
       
       if(blank(returnUrl)) 
           return new ForwardResolution("/accountDocument.jsp");
       else
           return new RedirectResolution(returnUrl);
           
   }
   
   public String getStorageId() {
       return storageId;
   }
   
   public void setStorageId(String storageId) {
       this.storageId = storageId;
   }
   
   public String getComment() {
       return comment;
   }
   
   public void setComment(String comment) {
       this.comment = comment;
   }
   
   public String getReturnUrl() {
       return returnUrl;
   }
   
   public void setReturnUrl(String returnUrl) {
       this.returnUrl = returnUrl;
   }
   
   public FileBean getUploadedFile() {
       return uploadedFile;
   }
   
   public void setUploadedFile(FileBean uploadedFile) {
       this.uploadedFile = uploadedFile;
   } 
   
   public String getDocumentType() {
       return documentType;
   }
    
   public void setDocumentType(String documentType) {
       this.documentType = documentType;
   }

   public String getMessage() {
       return message;
   }
   
   public void setMessage(String message) {
       this.message = message;
   }
}
