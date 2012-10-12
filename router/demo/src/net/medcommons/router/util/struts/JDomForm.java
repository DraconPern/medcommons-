/*
 * $Id$
 * Created on 31/03/2005
 */
package net.medcommons.router.util.struts;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.sourceforge.stripes.action.ActionBean;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

/**
 * JDomForm is an interface that indicates support for a JDOM Document
 * within the form.
 * 
 * @author ssadedin
 */
public interface JDomForm extends ActionBean {   
    /**
     * Returns the JDOM Document object for this form. This Document will be
     * populated and read from in the translation of struts properties.
     * 
     * @return
     * @throws CCROperationException 
     * @throws PHRException 
     */
    public XMLPHRDocument getDocument(HttpServletRequest request) throws CCROperationException, PHRException;
    
    /**
     * Returns the namespace to apply to the XPath statement, if any.
     */
    public Namespace getNamespace();
    
    /**
     * Called to create paths that don't exist in the XML when a "set" operation
     * is attempted on the path.
     * 
     * @param path
     * @return true if the path was created, false otherwise
     * @throws IOException 
     * @throws JDOMException 
     * @throws PHRException 
     * @throws InvalidCCRException 
     */
    public Element createPath(String path) throws PHRException;
    
    /**
     * Called if the form is modified
     * @param request 
     * @throws IOException 
     * @throws JDOMException 
     * @throws CCROperationException 
     */
    public void setModified(HttpServletRequest request) throws JDOMException, IOException;
}
