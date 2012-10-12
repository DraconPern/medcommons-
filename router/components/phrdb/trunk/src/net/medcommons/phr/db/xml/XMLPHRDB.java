/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.db.xml;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ValidationResult;
import net.medcommons.phr.db.PHRDB;
import net.medcommons.phr.resource.Spring;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * XMLPHRDB implements an XML PHR database based around a JDOM document
 * 
 * @author ssadedin
 */
public class XMLPHRDB implements PHRDB { 
    
    /**
     * Configuration of this database. 
     */
    private XMLPHRDBConfig cfg;
    
    public XMLPHRDB() {
        this.cfg = (XMLPHRDBConfig) Spring.getBean("xmlPhrDbConfig");
    }

    /**
     * Open the document with given id from this database
     */
    public PHRDocument open(String id) throws PHRException {        
        try {
            PHRDocument d = (PHRDocument) ((SAXBuilder)Spring.getBean("xmlPhrDbBuilder")).build(this.getDataSource(id).getInputStream());
            return d;
        }
        catch (IOException e) {
            throw new PHRException(e);
        }
        catch (JDOMException e) {
            throw new PHRException(e);
        }        
    }

    public void save(String id, PHRDocument phr) throws PHRException {
        // Todo: should we have option to allow save of invalid documents?
        try {
            ValidationResult v = phr.validate();
            if(!v.isPassed()) {
                throw new PHRException("Attempt to save invalid phr with validation failures: " + v.toString());
            }
            
            DataSource src = this.getDataSource(id); 
            OutputStream out = src.getOutputStream();
            new XMLOutputter(Format.getPrettyFormat().setEncoding("UTF-8")).output(phr.getDocument(), out);
            out.flush();
            out.close();
        }
        catch (IOException e) {
            throw new PHRException("Unable to save document " + id, e);
        }
    }
    
    public void delete(String id) throws PHRException {
        DataSource ds = this.getDataSource(id);
        if(ds instanceof FileDataSource) {
            FileDataSource fds = (FileDataSource)ds;
            if(!fds.getFile().delete()) {
                throw new PHRException("Unable to delete reqested PHR " + id);
            }
        }
        else {
            throw new PHRException("Delete is not implemented for data source of type " + ds.getClass().getName());
        }
    }
    
    /**
     * Return a data source that can be used to load / save the document.  Default
     * is to store as a file in the configured root directory, but subclasses can
     * override to provide any source that can work as a stream. 
     */
    private DataSource getDataSource(String id) {
        return new FileDataSource(cfg.getFileStoreRoot() + "/" + id + ".xml");
    }

    public void close() throws PHRException {
        // noop
    }

    public void connect(String connectString) throws PHRException {
        // noop
    }
}
