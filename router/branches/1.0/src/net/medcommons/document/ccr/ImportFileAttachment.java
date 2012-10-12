package net.medcommons.document.ccr;

import static net.medcommons.modules.utils.Str.blank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.modules.services.interfaces.DocumentService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.services.dicom.util.MCInstance;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;

/**
 * Utility class for importing a single attached file to a CCR. 
 * <P>
 * TODO: Remove the dependency on MCSeries as the abstraction here.
 * TODO: Create a streaming version of this method as well. 
 * 
 * @author sean
 *
 */
public class ImportFileAttachment {

	DocumentService documentService = null; 
	
	public ImportFileAttachment(ServicesFactory factory) throws ServiceException {
		super();
		documentService = factory.getDocumentService();
	}
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(ImportFileAttachment.class);
	
	/**
	 * Takes a the file attachment as a string together with the content type of the attachment and
	 * places this file in the repository. It returns a MCSeries object which can be used to create 
	 * &gt;References&lt; in CCRDocument.
	 * 
	 * @param contents - byte contents of document
	 * @param contentType - content type of document being imported
	 * @param event - charge, if any, to be recorded as pending for this document (may be null)
	 * @return
	 * @throws ImportAttachmentException 
	 */
	public MCSeries exec(String storageId, byte[] contents, String title, String contentType, BillingEvent event) throws ImportAttachmentException {
	    try {
	        SHA1 sha1 = new SHA1();
	        sha1.initializeHashStreamCalculation();
	        
	        String documentGuid = sha1.calculateByteHash(contents);
	        
	        // Create a new series
    	    MCSeries series = new MCSeries(documentGuid, storageId, "", null);
	        
	        LocalFileRepository localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
	        
	        documentService.addDocument(storageId, documentGuid, event);
	         
	        localFileRepository.putDocument(storageId, contents, title, contentType, event != null);
	        
	        // Put the document type in repository
	        log.info("Created repository document with name " + documentGuid);    
	        
	        // Create a new instance
	        MCInstance instance = 
	            new MCInstance(documentGuid, contentType, storageId, documentGuid);
	        
	        // Add the instance to the series
	        series.addInstance(instance);
	        series.setMcGUID(documentGuid);
	        series.setSeriesStatus(MCSeries.STATUS_CLOSED);
	        if(blank(title))
	            title = "FAX";
	        series.setSeriesDescription(title);
	        instance.setMimeType(contentType);
	        
	        log.info("generated series:" + series);
	        return series;
	    }
	    catch(TransactionException e){
	        throw new ImportAttachmentException("Unable to import attachment title="+title+" contentType="+contentType+" to storage id " + storageId, e);
	    }
	    catch (FileNotFoundException e) {
	        throw new ImportAttachmentException("Unable to import attachment title="+title+" contentType="+contentType+" to storage id " + storageId, e);
	    }
	    catch (IOException e) {
	        throw new ImportAttachmentException("Unable to import attachment title="+title+" contentType="+contentType+" to storage id " + storageId, e);
	    }      
	    catch(NoSuchAlgorithmException e){
	        throw new ImportAttachmentException("Unable to import attachment title="+title+" contentType="+contentType+" to storage id " + storageId, e);
	    }
	    catch(ServiceException e){
	        throw new ImportAttachmentException("Unable to import attachment title="+title+" contentType="+contentType+" to storage id " + storageId, e);
	    }
	}
}
