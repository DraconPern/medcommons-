/*
 * $Id$
 * Created on 03/04/2007
 */
package net.medcommons.phr.db.sqlite;

public class JDBCPHRDBConfig {
    
    private String fileStoreRoot;
    private String driverClass;
    private String connectUrl;

    public String getFileStoreRoot() {
        return fileStoreRoot;
    }

    public void setFileStoreRoot(String fileStoreRoot) {
        this.fileStoreRoot = fileStoreRoot;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

}
