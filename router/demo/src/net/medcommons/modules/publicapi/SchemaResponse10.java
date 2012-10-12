package net.medcommons.modules.publicapi;

import java.util.List;

import net.medcommons.document.ValidatingParserFactory;
import net.medcommons.modules.crypto.Base64Coder;
import net.medcommons.modules.services.interfaces.PHRProfile;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class SchemaResponse10 extends SchemaResponse {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("SchemaResponse10");
	String schemaURLToken = null;
	String xsdURI = "/EditSession10.xsd";
	String namespace = "http://www.medcommons.net/editsession01";
	

	 /**
     * Pool of parsers for validating documents
    */
	private ObjectPool validatingParserPool =
        new GenericObjectPool(
                        new ValidatingParserFactory("conf/EditSession10.xsd",namespace)); 
	 
	public SchemaResponse10(String schemaURLToken) {
		this.schemaURLToken = schemaURLToken;
	}

	protected String getSchemaURLToken() {
		return (schemaURLToken);
	}
	

	public Document generateResponse(String remoteAccessAddress,
			PHRTransaction transaction, int status, String reason,
			String redirectURL, String newAuth) {
		log.info("updatePHR response status=" + status + ", reason = " + reason
				+ ", redirectURL = " + redirectURL);
		
		Document doc = generateSchemaDoc(
				namespace, generateLocation(remoteAccessAddress, xsdURI));
						

		Element root = doc.getRootElement();
		Namespace defaultNamespace = root.getNamespace();

		Element token = new Element("SessionToken", defaultNamespace);
		if (transaction != null) {
			token.setText(transaction.getToken());
		} else {
			token.setText("Unknown");
			log.error("Transaction is null");
		}
		root.addContent(token);

		Element contentType = new Element("ContentType", defaultNamespace);
		contentType.setText("text/xml");
		root.addContent(contentType);

		Element authorization = new Element("AuthorizationToken",
				defaultNamespace);
		if (newAuth != null) {
			authorization.setText(newAuth);
		} else {
			authorization.setText("Unknown");
		}
		root.addContent(authorization);

		Element sessionElement = new Element("SessionURI", defaultNamespace);
		String sessionURI;
		if (transaction != null) {
			sessionURI = createRESTURI(remoteAccessAddress, transaction)
					+ "&useSchema=" + schemaURLToken;
		} else
			sessionURI = "Unknown";

		sessionElement.setText(sessionURI);
		root.addContent(sessionElement);

		Element statusElement = new Element("Status", defaultNamespace);
		statusElement.setText(status + "");
		root.addContent(statusElement);

		Element reasonElement = new Element("Reason", defaultNamespace);
		reasonElement.setText(reason);
		root.addContent(reasonElement);

		if (redirectURL != null) {
			Element redirectElement = new Element("RedirectURI",
					defaultNamespace);
			redirectElement.setText(redirectURL);
			root.addContent(redirectElement);
		}

		return (doc);
	}
	
	protected Document createSchemaReference(String remoteAccessAddress, PHRTransaction transaction){
		
		
		
		Document doc = generateSchemaDoc(
				namespace, generateLocation(remoteAccessAddress, xsdURI));
		Element root = doc.getRootElement();
		Namespace defaultNamespace = root.getNamespace();
		
		Element token = new Element("SessionToken",defaultNamespace);
		token.setText(transaction.getToken());
		root.addContent(token);
		
		
		
		Element contentType = new Element("ContentType",defaultNamespace);
		contentType.setText(transaction.getContentType());
		root.addContent(contentType);
		
		
		
		Element authorization = new Element("AuthorizationToken",defaultNamespace);
		if (transaction.getOriginalAuthToken() != null)
			authorization.setText(transaction.getOriginalAuthToken());
		root.addContent(authorization);
		
		Element downloadURL = new Element("SessionURI",defaultNamespace);
		downloadURL.setText(createRESTURI(remoteAccessAddress, transaction));
		root.addContent(downloadURL);
		
		Element phrAlias = new Element("Alias",defaultNamespace);
		phrAlias.setText(transaction.getPhrAlias());
		root.addContent(phrAlias);
		
		Element personName = new Element("PersonName",defaultNamespace);
		personName.setText(transaction.getPhrPersonName());
		root.addContent(personName);
		byte [] image = transaction.getPhrImage();
		if (image != null){
			Element phrImage = new Element("PersonImage",defaultNamespace);
			
			char[] encodedImage = Base64Coder.encode(image);
			phrImage.setText(new String(encodedImage));
			root.addContent(phrImage);
		}
		Element applicationTitle = new Element("ApplicationTitle",defaultNamespace);
		applicationTitle.setText("MedCommons CCR Editor:" + transaction.getPhrPersonName());
		root.addContent(applicationTitle);
		
		Element status= new Element("Status",defaultNamespace);
		status.setText("200");
		root.addContent(status);

		Element reason= new Element("Reason",defaultNamespace);
		reason.setText("OK");
		root.addContent(reason);
		
		if (transaction.getProfiles() != null){
			Element profiles = new Element("Profiles", defaultNamespace);
			root.addContent(profiles);
			List<PHRProfile> profileNames= transaction.getProfiles();
			for(PHRProfile p : profileNames) {
				String editSessionURI = createEditSessionURI(remoteAccessAddress,transaction, p.getGuid());
				profiles.addContent(createProfileElement(p,defaultNamespace, editSessionURI));
			}
		}
		
		return doc;
	
	}

    protected  SAXBuilder borrowObject() throws Exception{
		return((SAXBuilder) validatingParserPool.borrowObject());
	}
	protected  void returnObject(SAXBuilder builder) throws Exception{
		validatingParserPool.returnObject(builder);
	}

}
