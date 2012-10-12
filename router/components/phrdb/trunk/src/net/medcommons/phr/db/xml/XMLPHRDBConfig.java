/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.db.xml;

import org.jdom.input.SAXBuilder;

public class XMLPHRDBConfig {
    
    private String fileStoreRoot;
    
    private String schemaPath;
    
    private SAXBuilder builder;
    
    public XMLPHRDBConfig() {
    }

    public String getFileStoreRoot() {
        return fileStoreRoot;
    }

    public void setFileStoreRoot(String fileStoreRoot) {
        this.fileStoreRoot = fileStoreRoot.replaceAll("/$", "");
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public SAXBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(SAXBuilder builder) {
        this.builder = builder;
    }
}
