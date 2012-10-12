package net.medcommons.modules.publicapi;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.utils.SupportedDocuments;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.jdom.Document;

@UrlBinding("/getDocument")
public class GetDocumentAction extends BasePublicAPIAction{
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("GetDocument");
	
	/*
	 
	*/
	@DefaultHandler
    public Resolution getDocument() {
    	log.info("getDocument");
		 
		
		String contentType = SupportedDocuments.CCR.getContentType();
		
		String authToken = "Gateway";
		if (getAuth() != null){
			authToken = getAuth();
		}
		// Should get content type from repository.
	
		String description = "StorageId= " + getStorageId() + ", guid=" +
			getReference() + ", contentType = " + contentType;
		InputStream in = null;
		BufferedWriter logBuffer = null;
		try{
			if (!transactionExists){
				throw new TransactionNotFoundException(getToken());
			}
			logBuffer = BasePublicAPIAction.getTransactionLogBuffer(transaction);
            BasePublicAPIAction.writeLog(logBuffer,"getDocument " + description);
            BasePublicAPIAction.writeLog(logBuffer, getHttpRequestInfo());
            log.info(getHttpRequestInfo());
			String docGuid = getRequestedGuid();
			if (docGuid == null) 
			    docGuid = getGuid();
			DocumentDescriptor doc = createDocumentDescriptor(getStorageId(), docGuid, contentType);
			Properties props = repository.getMetadata(doc);
			in = repository.get(doc);
			
			contentType = props.getProperty(RepositoryFileProperties.CONTENT_TYPE);
			log.info("Repository content type= " + contentType);
			if (in != null){
		        return new StreamingResolution(contentType, in);
			}
			else{
				String message = "Inputstream null for request for storageId " + description;
				BasePublicAPIAction.writeLog(logBuffer,message);
				log.error(message);
				return new StreamingResolution(SupportedDocuments.TEXT.getContentType(),
						new StringReader(message));
				
			}
		}
		catch (TransactionNotFoundException e){
			String message = "Unknown Transaction token: " + e.getLocalizedMessage();
			BasePublicAPIAction.writeLog(logBuffer,message);
			BasePublicAPIAction.writeLog(logBuffer,e);
			Document responseDoc ;
			responseDoc  = getSchemaResponse().generateResponse(remoteAccessAddress,transaction, 500, e.getLocalizedMessage(), null,
					null);
			 
			return generateErrorResolution(505, message,e); 
		}
		catch(TransactionException e){
			String message = "Transaction exception:" + e.getLocalizedMessage() + description;
			log.error(message, e);
			BasePublicAPIAction.writeLog(logBuffer,message);
			BasePublicAPIAction.writeLog(logBuffer,e);
			Document responseDoc ;
			responseDoc = responseDoc = getSchemaResponse().generateResponse(remoteAccessAddress, null, 500, message, null, null);
			
			return generateErrorResolution(506, message,e); 
			
		}
		catch(IOException e){
			String message = "IOException " + e.getLocalizedMessage() + description;
			log.error(message,e);
			BasePublicAPIAction.writeLog(logBuffer,message);
			BasePublicAPIAction.writeLog(logBuffer,e);
			Document responseDoc ;
			responseDoc = responseDoc = getSchemaResponse().generateResponse(remoteAccessAddress, null, 500, message, null, null);
			
			return generateErrorResolution(507, message,e); 
			
			
		}
		
		
    }

}
