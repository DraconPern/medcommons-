/*
 * $Id$
 * Created on 22/03/2005
 */
package net.medcommons.router.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.sun.xml.bind.StringInputStream;

/**
 * A utility DataSource for the activation framework
 * which reads its data from a literal string.
 * 
 * @author ssadedin
 */
public class StringDataSource implements DataSource {

    /**
     * The contents of the data source.
     */
    private String contents;
    
    /**
     * The mimeType of the data
     */
    private String mimeType;
    
    /**
     * The name of the data source
     */
    private String name;
    
    /**
     * The input stream that will be returned to callers.
     */
    private InputStream stream;

    /**
     * @param contents
     * @param mimeType
     */
    public StringDataSource(String name, String contents, String mimeType) {
        super();
        this.contents = contents;
        this.mimeType = mimeType;
        this.stream = new StringInputStream(contents);
    }
    
    /**
     * @see javax.activation.DataSource#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    /**
     * @see javax.activation.DataSource#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    /**
     * @see javax.activation.DataSource#getContentType()
     */
    public String getContentType() {
        return mimeType;
    }

    /**
     * @see javax.activation.DataSource#getName()
     */
    public String getName() {
        return name;
    }

    public String getContents() {
        return contents;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
