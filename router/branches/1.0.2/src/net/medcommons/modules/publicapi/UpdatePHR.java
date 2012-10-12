package net.medcommons.modules.publicapi;

import java.io.IOException;
import java.io.InputStream;

import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.cxp.server.RLSHandler;
import net.medcommons.modules.cxp.server.TransactionUtils;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.router.services.ccr.RLSCXPHandler;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.jdom.Document;

@UrlBinding("/updatePHR")
public class UpdatePHR extends BasePublicAPIAction {
	private static Logger log = Logger.getLogger("UpdatePHR");

	private FileBean document;

	public FileBean getDocument() {
		return document;
	}

	public void setDocument(FileBean document) {
		this.document = document;
	}

	@DefaultHandler
	public Resolution updatePHR() {
		log.info("updatePHR");
		/*
		RLSHandler rlsHandler = new RLSCXPHandler(); // Breaks module
														// framework
		TransactionUtils transactionUtils = new TransactionUtils();
		ServicesFactory serviceFactory = new RESTProxyServicesFactory(getAuth());
		DocumentDescriptor docDescriptor = null;
		Document responseMessage = null;
		String redirectURL = null;
		String newAuth = null;
		try {
			if (this.document != null) {
				if (!transactionExists){
					throw new TransactionNotFoundException(getToken());
				}

				docDescriptor = createDocumentDescriptor(getStorageId(),
						getReference(), getContentType());
				log.info("content type is " + getContentType());
				docDescriptor.setDocumentName(this.document.getFileName());
				docDescriptor.setLength(this.document.getSize());
				InputStream is = this.getDocument().getInputStream();
				String registrySecret = null;
				boolean mergeIncomingCCR = true;

				
				repository.putInputStream(docDescriptor, is);
				if (DocumentTypes.CCR_MIME_TYPE.equals(getContentType())) {

					String trackingNumber = transactionUtils
							.generateConfirmationCode(serviceFactory,
									docDescriptor.getContentType(),
									docDescriptor.getStorageId(), docDescriptor
											.getGuid(), registrySecret,
									PIN.SHA1Hasher);

					rlsHandler.newDocumentEvent(docDescriptor.getStorageId(),
							docDescriptor.getGuid(), trackingNumber,
							mergeIncomingCCR);
					
					responseMessage = generateResponse(200, "OK", redirectURL, newAuth);
				}
				else{
					responseMessage = generateResponse(415, "Unsupported content type " + getContentType(), redirectURL, newAuth);
					
				}
				
			} else {
				responseMessage  = generateResponse(400, "No attachment found", redirectURL, newAuth);

			}
		}

		catch (IOException e) {
			String message = "Error uploading attachment";
			if (docDescriptor != null) {
				message += docDescriptor.toString();
			}
			log.error(message, e);
			responseMessage = generateResponse(500, message, redirectURL, newAuth);
			
			
		}catch (TransactionException e) {
			String message = "Error uploading attachment:";
			if (getToken() != null){
				message += "token = " + getToken() + ";";
			}
			if (docDescriptor != null) {
				message += docDescriptor.toString();
			}
			log.error(message, e);
			responseMessage = generateResponse(500, message, redirectURL, newAuth);
		} 
		catch (TransactionNotFoundException e) {
			String message = "Transaction not found:";
			if (getToken() != null){
				message += "token = " + getToken();
			}
			if (docDescriptor != null) {
				message += docDescriptor.toString();
			}
			log.error(message, e);
			responseMessage = generateResponse(500, message, redirectURL, newAuth);
		}
		catch (Exception e){
			String message = "Error uploading attachment";
			if (docDescriptor != null) {
				message += docDescriptor.toString();
			}
			log.error(message, e);
			responseMessage = generateResponse(500, message, redirectURL, newAuth);
		}
		*/
		return new StreamingResolution("text/txt", "Obsolete?");
	}
	
	
}
