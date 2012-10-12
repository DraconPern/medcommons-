package net.medcommons.document;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.phr.ccr.CCRBuilder;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.jdom.input.SAXBuilder;

/**
 * Defines a factory for creating an object pool of validating SAX Parsers using
 * the CCR XDS file. 
 * 
 * @author sean
 *
 */
public class ValidatingParserFactory extends BasePoolableObjectFactory{

	//private static String XSD_LOCATION = "conf/CCR_20051109.xsd";
	//private static String NAMESPACE = "urn:astm-org:CCR";

	private String xsdLocation = null;
	private String namespace = null;
	public ValidatingParserFactory(String xsdLocation, String namespace){
		this.xsdLocation = xsdLocation;
		this.namespace = namespace;
	}
	
	public Object makeObject(){
		CCRParseErrorHandler errorHandler = new CCRParseErrorHandler();
		SAXBuilder builder = null;
        
        if(this.namespace.equals(CCRConstants.CCR_NAMESPACE_URN)) {
          builder = new CCRBuilder("org.apache.xerces.parsers.SAXParser", true);
        }
        else
          builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
            
		builder.setReuseParser(true); // Want to reuse this parser.
		String xsd = xsdLocation;
		String ns = namespace;
		builder.setFeature(
				"http://apache.org/xml/features/validation/schema",
				true);

		builder
				.setProperty(
						"http://apache.org/xml/properties/schema/external-schemaLocation",
						ns + " " + xsd);

		builder.setValidation(true);
		builder.setErrorHandler(errorHandler);
		return(builder);
	}
	/**
	 * Passivate immediately assigns a new error handler object (the only 
	 * object that contains state).
	 * @param obj
	 */
	public void passivateObject(Object obj){
		CCRParseErrorHandler errorHandler = new CCRParseErrorHandler();
		SAXBuilder builder = (SAXBuilder) obj;
		builder.setErrorHandler(errorHandler);
		
		
	}
}
/*

public class StringBufferFactory extends BasePoolableObjectFactory { 
    // for makeObject we'll simply return a new buffer 
    public Object makeObject() { 
        return new StringBuffer(); 
    } 
     
    // when an object is returned to the pool,  
    // we'll clear it out 
    public void passivateObject(Object obj) { 
        StringBuffer buf = (StringBuffer)obj; 
        buf.setLength(0); 
    } 
     
    // for all other methods, the no-op  
    // implementation in BasePoolableObjectFactory 
    // will suffice 
}
*/