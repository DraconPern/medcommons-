/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.db.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRElement;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ValidationResult;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.sqlite.JDBCPHRElement;
import net.medcommons.phr.resource.Spring;
import net.medcommons.phr.util.StringUtil;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.SAXOutputter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML Implementation of PHRDocument interface
 * 
 * @author ssadedin
 */
public class XMLPHRDocument extends Document implements PHRDocument {
    
    /**
     * Schema for validation
     */
    private static Schema xmlSchema = null; 
    
    /**
     * xpath mapping and cache instance
     */
    private XPathCache xpath;
    
    /**
     * Meta data about elements in the document
     */
    private Map<PHRElement,  Map<String, PHRElement> > metadata = new HashMap<PHRElement, Map<String,PHRElement>>(); 
    
    /**
     * Keeps track of elements that have been deleted so that they can be removed in database 
     * if necessary.
     */
    private Set<PHRElement> deletedElements = new HashSet<PHRElement>();
    
    /**
     * Notifications when elements are modified
     */
    private boolean modified = false;
    
    /**
     * 
     */
    public XMLPHRDocument() {
        super();
        this.xpath = Spring.getBean("ccrXPathCache");
    }

    public XMLPHRDocument(Element arg0, DocType arg1, String arg2) {
        super(arg0, arg1, arg2);
        this.xpath = Spring.getBean("ccrXPathCache");
    }

    public XMLPHRDocument(Element arg0, DocType arg1) {
        super(arg0, arg1);
        this.xpath = Spring.getBean("ccrXPathCache");
    }

    public XMLPHRDocument(Element arg0) {
        super(arg0);
        this.xpath = Spring.getBean("ccrXPathCache");
    }

    public XMLPHRDocument(List arg0) {
        super(arg0);
        this.xpath = Spring.getBean("ccrXPathCache");
    }

    public void remove(String path) throws PHRException {
        try {
            // Try and resolve the property
            PHRElement e = (PHRElement) this.xpath.getElement(this, path);
            if(e != null) {
                e.getParentElement().removeChild(e);
                this.deletedElements.add(e);
            }
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to remove child at path " + path, e);
        }
    }

    public String getValue(String attribute) throws PHRException {
        try {
            return xpath.getValue(this,attribute);
        }
        catch (JDOMException e) {
            throw new PHRException(e);
        }
        catch (IOException e) {
            throw new PHRException(e);
        }
    } 

    public XMLPHRDocument setValue(String attribute, String value) throws PHRException {
        try {
            PHRElement e = (PHRElement)xpath.getElement(this, attribute);
            if(e == null) {
                e = ((CCRElement)this.getRootElement()).createProperty(attribute);
            }        
            e.setElementValue(value);
            return this;
        }
        catch (JDOMException ex) {
            throw new PHRException("Unable to retrieve element for path " + attribute, ex);
        }
    }
    
    public ValidationResult validate() throws PHRException {

        try {
            Schema schema = getSchema();
            ValidatorHandler vh =  schema.newValidatorHandler();
            final ValidationResult v = new ValidationResult();
           
            vh.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException e) {
                    v.errors.add(e);
                }

                public void fatalError(SAXParseException e) {
                    v.errors.add(e);
                }

                public void warning(SAXParseException e) {
                    v.errors.add(e);
                }
            });
            SAXOutputter so = new SAXOutputter(vh);
            so.output(this);
            return v;
        }
        catch (SAXException e) {
            throw new PHRException("Unable to validate document",e);
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to validate document",e);
        }
    }

    /**
     * @return
     * @throws SAXException
     * @throws PHRException 
     */
    private Schema getSchema() throws SAXException, PHRException {
        if(xmlSchema == null) {
            XMLPHRDBConfig cfg = (XMLPHRDBConfig) Spring.getBean("xmlPhrDbConfig");
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            if(StringUtil.blank(cfg.getSchemaPath())) {
                throw new PHRException("No schemaPath is configured in XMLPHRDBConfig.  Please configure this value before attempting validation");
            }
            StreamSource ss = new StreamSource(cfg.getSchemaPath());
            xmlSchema = factory.newSchema(ss);
        }
        return xmlSchema;
    }
    
    public PHRElement getRoot() {
        return (PHRElement) this.getRootElement();
    }

    public Document getDocument() {
        return this;
    }

    public CCRElement queryProperty(String path, String[]... params) throws PHRException {
        try {
            HashMap<String,String> queryParams = null;
            if(params.length>0) {
                queryParams = new HashMap<String, String>();
                for (String[] param : params) {
                    queryParams.put(param[0],param[1]);
                }
            }
            return (CCRElement)xpath.getElement(this, path, queryParams);
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to query for path:  " + path,e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> queryTextValues(String path, String[]... params) throws PHRException {
        try {
            HashMap<String,String> queryParams = null;
            if(params.length>0) {
                queryParams = new HashMap<String, String>();
                for (String[] param : params) {
                    queryParams.put(param[0],param[1]);
                }
            } 
            
            List results = (List) xpath.getXPathResult(this, path, queryParams, true);
            if(results.isEmpty() || results.get(0) instanceof String)
                return (List<String>)results;
            else {
                List<String> textResults = new ArrayList<String>(results.size());
                for(Object result : results) {
                    CCRElement e = (CCRElement)result;
                    textResults.add(e.getText());
                }
                return textResults;
            }
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to query for path:  " + path,e);
        }
    }
    
    public XPathCache getXPathCache() {
        return this.xpath;
    }
    
    public void setMetaData(PHRElement e, PHRElement me) {
        Map<String, PHRElement> m = this.metadata.get(e);
        if(m == null) {
            m = new HashMap<String, PHRElement>();
            this.metadata.put(e,m);
        }
        m.put(me.getName(), me);
    }
    
    public void setMetaData(PHRElement e, String name, String value) {
        Map<String, PHRElement> m = this.metadata.get(e);
        if(m != null) {
            PHRElement me =  m.get(name);
            if(me != null) {
                me.setElementValue(value);
            }
            else {
                JDBCPHRElement jdbce = new JDBCPHRElement(name, "");
                jdbce.setText(value);
                jdbce.setDisposition(JDBCPHRElement.Disposition.META);
                m.put(name, jdbce);
            }
        }
        else { // no meta data at all - create it
            m = new HashMap<String, PHRElement>();
            this.metadata.put(e,m);
            JDBCPHRElement jdbce = new JDBCPHRElement(name, "");
            jdbce.setText(value);
            jdbce.setDisposition(JDBCPHRElement.Disposition.META);
            m.put(name,jdbce);
        }
     }
   
    public Map<String, PHRElement> getMetaData(PHRElement e) {
        return this.metadata.get(e);
    }
    
    public String getMetaData(PHRElement e, String name) {
        Map<String, PHRElement> m = this.metadata.get(e);
        if(m != null) {
            return m.get(name).getElementValue();
        }
        else {
           return null; 
        }
    }

    public Set<PHRElement> getDeletedElements() {
        return deletedElements;
    }

    public boolean getModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

}
