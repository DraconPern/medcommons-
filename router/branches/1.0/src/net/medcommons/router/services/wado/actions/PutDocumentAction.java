//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.nvl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.crypto.Base64Coder;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * Stores a document to the repository, passed as a base64 encoded string.
 * 
 * @author ssadedin
 */
public class PutDocumentAction extends Action {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(PutDocumentAction.class);

private LocalFileRepository localFileRepository = null;

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

    String fileName = request.getParameter("fileName");
    String fileData =request.getParameter("data");
    String contentType = request.getParameter("contentType");
    String accountId = request.getParameter("accountId");
    
    if(blank(fileName))
        throw new IllegalArgumentException("Missing parameter fileName");
    log.info("Got file name: " + fileName);
    
    if(blank(contentType))
        throw new IllegalArgumentException("Missing parameter contentType");
    log.info("Got content type: " + contentType);
    
    if(blank(fileData))
        throw new IllegalArgumentException("Missing parameter fileData");
    log.info("Got " + fileData.length() + " chars of file data");
     
    
    String decoded = Base64Coder.decode(fileData);    

    SHA1 sha1 = new SHA1();
    sha1.initializeHashStreamCalculation();
    String documentGuid = sha1.calculateStringHash(decoded);
    
    RESTProxyServicesFactory factory = new RESTProxyServicesFactory(null);
    factory.getDocumentService().addDocument(accountId, documentGuid);
    
    if(!blank(accountId)) {
        if(blank(request.getParameter("documentType")))
            throw new IllegalArgumentException("Document Type must be provided if account id is specified.");
            
	    factory.getAccountService().addAccountDocument(
                        accountId, 
                        documentGuid, 
                        AccountDocumentType.valueOf(request.getParameter("documentType")),
                        nvl(request.getParameter("comment"),""), true, null                                        
                                        );
    }
    
    // Put the document into the repository
    if (localFileRepository == null)
		localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
    
    
    documentGuid = localFileRepository.putDocument(accountId, decoded, contentType);
    
    // Put the docment type in repository
    log.info("Created repository document with name " + documentGuid);    
    
    response.getOutputStream().print(documentGuid);
    return null;
  }
}
