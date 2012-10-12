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

public class SchemaResponse12 extends SchemaResponse {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("SchemaResponse12");
	String schemaURLToken = null;
	String xsdURI = "/EditSession12.xsd";
	String namespace = "http://www.medcommons.net/editsession12";
	
	 /**
     * Pool of parsers for validating documents
    */
	private ObjectPool validatingParserPool =
        new GenericObjectPool(
                        new ValidatingParserFactory("conf/EditSession12.xsd",namespace)); 
	
	public SchemaResponse12(String schemaURLToken){
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

		String originalURI = transaction.getOriginalURI();
		if (originalURI != null) {
			Element originalURIElement = new Element("OriginalURI",
					defaultNamespace);
			originalURIElement.setText(originalURI);
			root.addContent(originalURIElement);
		}
		return (doc);
	}
	
	protected String getDownloadRootURI(String remoteAccessAddress, PHRTransaction transaction){
		if (transaction == null){
			return null;
		}
		else{
			String uri = remoteAccessAddress + "/getDocument?token=";
			uri+=transaction.getToken();
			uri+="&useSchema=";
			uri+=getSchemaURLToken();
			uri+="&reference=";
			return(uri);
			
		}
			
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
		if (transaction.getHealthURL() != null){
			Element summaryText1 = new Element("SummaryText1", defaultNamespace);
			summaryText1.setText(transaction.getHealthURL());
			root.addContent(summaryText1);
			
			Element summaryText2 = new Element("SummaryText2", defaultNamespace);
			summaryText2.setText("MedCommons HealthURL");
			root.addContent(summaryText2);
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
		
		// RedirectURI
		
		// OriginalURI
		String originalURI = transaction.getOriginalURI();
		if (originalURI != null) {
			Element originalURIElement = new Element("OriginalURI",
					defaultNamespace);
			originalURIElement.setText(originalURI);
			root.addContent(originalURIElement);
		}
		// DownloadDocumentRootURI
		String downloadDocumentURI = getDownloadRootURI(remoteAccessAddress, transaction);
		if (downloadDocumentURI != null){
			Element downloadDocument = new Element("DocumentDownloadRootURI", defaultNamespace);
			downloadDocument.setText(downloadDocumentURI);
			root.addContent(downloadDocument);
		}
		
		// UploadReferenceCopy
		Element uploadReference= new Element("UploadReferenceCopy",defaultNamespace);
		uploadReference.setText("true");
		root.addContent(uploadReference);
		
		if((transaction.getProfiles() != null) && (transaction.getProfiles().size() >1)){
            Element profiles = new Element("Profiles", defaultNamespace);
            root.addContent(profiles);
            List<PHRProfile> profileNames= transaction.getProfiles();
           // log.info("PHR transaction = " + transaction.toString() + ", nProfiles = " + transaction.getProfiles().size());
            for(PHRProfile p : profileNames) {
               // log.info("profile name=" + p.getName() + ", guid=" + p.getGuid() + ", date=" + p.getDate());
                if ((p.getGuid() != null) && (!transaction.getCalculatedGuid().equals(p.getGuid()))){
                    // Only generate profiles for CCRs other than the one in this transaction
                    String editSessionURI = createEditSessionURI(remoteAccessAddress,transaction, p.getGuid());
                    profiles.addContent(createProfileElement(p,defaultNamespace, editSessionURI));
                }
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
	
	
	/*
	private static Schema xmlSchema;
    
    private Schema getSchema() throws SAXException {
        if(xmlSchema == null) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource ss = new StreamSource("conf/EditSession12.xsd");
            xmlSchema = factory.newSchema(ss);
        }
        return xmlSchema;
    }
    */

}