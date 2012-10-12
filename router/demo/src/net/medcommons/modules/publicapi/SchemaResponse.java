package net.medcommons.modules.publicapi;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import net.medcommons.modules.services.interfaces.PHRProfile;
import net.medcommons.modules.utils.Str;

import org.codehaus.xfire.service.invoker.RequestScopePolicy;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Manages generating of response consistent with a XSD.
 * 
 * @author sdoyle
 *
 */
public abstract class SchemaResponse {
    private final static String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss_z" ;
	
	protected abstract Document generateResponse(String remoteAccessAddress , PHRTransaction transaction, int status, String reason,
			String redirectURL, String newAuth);
	
	/**
	 * Returns the value used in "&useSchema=value".
	 * @return
	 */
	protected abstract String getSchemaURLToken();
	protected abstract Document createSchemaReference(String remoteAccessAddress, PHRTransaction transaction);
		
	protected String generateLocation(String remoteAccessAddress, String xsdURI){
		return(remoteAccessAddress + xsdURI);
	}
	protected  String createRESTURI(String remoteAccessAddress, PHRTransaction transaction){
		String token = "Unknown";
		String authToken = null;
		if(transaction != null) {
			token = transaction.getToken();
			authToken = transaction.getOriginalAuthToken();
		}
		StringBuilder buff = new StringBuilder(remoteAccessAddress);
		buff.append("/DocumentEditSession");
		buff.append("?token=");
		buff.append(token);
		if (authToken != null){
		    buff.append("&authorizationToken=");
		    buff.append(authToken);
		}
		buff.append("&useSchema=");
		buff.append(getSchemaURLToken());
		
		return(buff.toString());
	    }
	
	protected  Document generateSchemaDoc( String namespace, String schemaLocation){
		Namespace xsiNS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema-instance");
		
		Namespace defaultNamespace = Namespace.getNamespace(namespace);
		
		Element root = new Element("EditSession",defaultNamespace);
	
		root.setAttribute("schemaLocation",
				defaultNamespace.getURI() + " " + 
				schemaLocation,
				xsiNS);
		Document doc = new Document(root);
		return(doc);
	}
	
	protected String createEditSessionURI(String remoteAccessAddress, PHRTransaction transaction, String newReference){
		
		StringBuffer buff = new StringBuffer(remoteAccessAddress);
		buff.append("/getPHREditSession");
		buff.append("?storageId=");
		buff.append(transaction.getStorageId());
		buff.append("&useSchema=").append(getSchemaURLToken());
		buff.append("&auth=").append(transaction.getOriginalAuthToken());
		buff.append("&reference=").append(newReference);
		
		
		
		return(buff.toString());
	}
	
	/**
	 * Returns a JDOM object containing a reference to another 'Profile' for editing.
	 * @param profileObj
	 * @param ns
	 * @param editSessionURI
	 * @return
	 */
	protected Element createProfileElement(PHRProfile profileObj, Namespace ns, String editSessionURI){
	    Element profile = new Element("Profile", ns);
	    Element profileAlias = new Element("Alias", ns);
	    profile.addContent(profileAlias);
	    String name = profileObj.getName();
	    if ((Str.blank(name) || "null".equals(name))){
	        if (profileObj.getDate() != null){
    	        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    	        name=dateFormat.format(profileObj.getDate());
	        }
	        else{
	            name = "Unknown:" + System.currentTimeMillis();
	        }
	        
	    }
	    profileAlias.setText(name);
	    Element requestSessionURI = new Element("RequestSessionURI", ns);
	    profile.addContent(requestSessionURI);
	    requestSessionURI.setText(editSessionURI);
	    return(profile);
	    
	}
	
	protected abstract SAXBuilder borrowObject() throws Exception;
	protected abstract void returnObject(SAXBuilder builder) throws Exception;
	
	protected boolean isValidMessage(Document response) throws IOException, JDOMException{
		boolean isValid = false;
		XMLOutputter serializer = new XMLOutputter();
		StringWriter sOut = new StringWriter();
		String sDoc;
		SAXBuilder builder = null;
	
			
		try{
			try {
				builder = borrowObject();
				serializer.output(response, sOut);
				sDoc = sOut.getBuffer().toString();
				StringReader reader = new StringReader(sDoc);
				builder.build(reader);
				isValid = true;
			}
			
			finally {
				returnObject(builder);
			}
		}
		catch(Exception e){
			throw new IOException("Error validating message", e);
		}
		return(isValid);
		}

	}

