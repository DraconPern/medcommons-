/*
 * $Id$
 * Created on 4/07/2006
 */
package net.medcommons.phr.ccr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import net.medcommons.phr.db.xml.XMLPHRDocument;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class CCRBuilder extends SAXBuilder {

    public CCRBuilder() {
        super();
        this.setFactory(new CCRElementFactory());
    }

    public CCRBuilder(boolean arg0) {
        super(arg0);
        this.setFactory(new CCRElementFactory());
    }

    public CCRBuilder(String arg0) {
        super(arg0);
        this.setFactory(new CCRElementFactory());
    }

    public CCRBuilder(String arg0, boolean arg1) {
        super(arg0, arg1);
        this.setFactory(new CCRElementFactory()); 
    }

    @Override
    public Document build(File file) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(file);
        return d;
    }

    @Override
    public Document build(InputSource in) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(in);
        d.setModified(false);
        return d;
    }

    @Override
    public Document build(InputStream in, String systemId) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(in, systemId);
        d.setModified(false);
        return d;
    }

    @Override
    public Document build(InputStream in) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(in);
        d.setModified(false);
        return d;
    }

    @Override
    public Document build(Reader characterStream, String systemId) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(characterStream, systemId);
        d.setModified(false);
        return d;
    }

    @Override
    public Document build(Reader characterStream) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(characterStream);
        d.setModified(false);
        return d;
    }

    @Override
    public Document build(String systemId) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(systemId);
        d.setModified(false);
        return d;
    }

    @Override
    public Document build(URL url) throws JDOMException, IOException {
        XMLPHRDocument d  = (XMLPHRDocument) super.build(url);
        d.setModified(false);
        return d;
    }
}
