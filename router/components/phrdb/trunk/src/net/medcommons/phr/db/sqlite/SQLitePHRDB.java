/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.db.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.PriorityQueue;

import net.medcommons.phr.DocumentNotFoundException;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRElement;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.PHRDB;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.phr.resource.Spring;

import org.apache.log4j.Logger;
import org.jdom.Document;

public class SQLitePHRDB implements PHRDB {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SQLitePHRDB.class);
    
    /**
     * Internal JDBC connection
     */
    private Connection conn = null;
    
    public void delete(PHRDocument phr, String path) {
        // TODO Auto-generated method stub
    }

    public PHRDocument open(String id) throws PHRException {
        
        if(this.conn == null)
            throw new PHRException("Not connected!");
        
        try {
            // Find the requested document
            String seq = queryDocumentSeqByName(id);
            if(seq == null)
                throw new DocumentNotFoundException("Unable to locate document " + id);
            
            log.debug("Found document " + seq + " for CCR name " + id);
            
            // Load all the rows
            Statement s = conn.createStatement();
            PriorityQueue<JDBCPHRElement> elements = new PriorityQueue<JDBCPHRElement>(50,new JDBCElementSorter());
            String sql = "select id,name,seq,status,text_value,last_modified_time,disposition from document where seq = '"
                + seq + "' or seq like '" + seq + ".%' and status = 'Active'";
            log.info("Loading rows:  " + sql );
            ResultSet results = s.executeQuery(sql);
            while(results.next()) {
                JDBCPHRElement e = new JDBCPHRElement(results);
                elements.add(e);
            }
            
            long startTimeMs = System.currentTimeMillis();
            
            // Pop off the root node
            JDBCPHRElement root = elements.remove();
            
            // Note: important that element is set on document prior to adding children
            // as they may reference it.
            XMLPHRDocument d = new XMLPHRDocument();
            d.setRootElement(root);
            
            // TODO: support for generic document types - somehow map to short name 
            root.setName("ContinuityOfCareRecord");
            while(!elements.isEmpty()) {
                JDBCPHRElement e = elements.remove();
                //log.info("Inserting PHR element " + e);
                root.insertChild(e);
            }
            long endTimeMs = System.currentTimeMillis();
            
            log.info("Loaded CCR " + id + " in " + (endTimeMs - startTimeMs) + " ms");
            results.close();
            s.close();
            return d;
        }
        catch (SQLException e) {
            throw new PHRException("Unable to open PHR document " + id,e);
        }
    }

    /**
     * Search for a document with meta data documentName equal to given name 
     * 
     * @param documentName
     * @return
     * @throws SQLException
     * @throws PHRException
     */
    private String queryDocumentSeqByName(String documentName) throws SQLException, PHRException {
        PreparedStatement p = 
            this.conn.prepareStatement("select seq from document where disposition='"+JDBCPHRElement.Disposition.META+"' and name = 'ccr.documentName' and text_value = ? and status = 'Active'");
        ResultSet resultSet = null;
        try {
            p.setString(1, documentName);
            p.execute();
            resultSet = p.getResultSet();
            if(!resultSet.next())
                return null;

            return resultSet.getString(1);
        }
        finally {
            if(p!=null) {
                p.close();
            }
            if(resultSet!=null) {
                resultSet.close();
            }
        }
    }
    

    /**
     * Saves the given document in this database under the given name.
     * <p/>
     * If a document with the same name already exists, it will be synchronized
     * with the one provided.  If it does not exist, a new document will
     * be created with the given id.
     */
    public void save(String id, PHRDocument phr) throws PHRException {
        
        if(this.conn == null)
            throw new PHRException("Not connected!");
        
        Document d = phr.getDocument();
        
        if(! (d instanceof XMLPHRDocument)) {
            throw new PHRException("Unable to save phr: PHR type " + d.getClass().getName() + " not supported");
        }
        
        PreparedStatement delete = null;
        try {
            long timeMs = System.currentTimeMillis();
            
            CCRElement ccrRoot = (CCRElement)d.getRootElement();
            
            // Try and find existing document with this id
            String seq = queryDocumentSeqByName(id);
            if(seq == null) {
                seq = this.allocateSequence("");
            }
            else {
                // If the document already exists, and the user is not saving a live instnace
                // then delete the stored one completely
                if (ccrRoot instanceof JDBCPHRElement) {
                    JDBCPHRElement jeRoot = (JDBCPHRElement) ccrRoot;
                    if(jeRoot.getId() == null) {
                        log.info("Deleting document " + id + " in order to save because document " + id + " exists with different root node");
                        this.delete(id);
                    }
                }
                else {
                    log.info("Deleting document " + id + " in order to save because document " + id + " exists but saved verions is not persistent");
                    this.delete(id);
                }
            }
            
            
            // First remove any deleted elements
            for (PHRElement e : ccrRoot.getDocument().getDeletedElements()) {
                if(e instanceof JDBCPHRElement) {
                    JDBCPHRElement je = (JDBCPHRElement) e;
                    delete = deleteElement(delete, je);
                }
            }
            
            JDBCSaver s = new JDBCSaver(conn, ccrRoot.getDocument(), id);
            ccrRoot.getDocument().setMetaData(ccrRoot, "documentName", id);
            ccrRoot = s.save("ccr",seq,ccrRoot);
            ccrRoot.setName("ContinuityOfCareRecord");
            
            conn.commit(); 
            
            long endTimeMs = System.currentTimeMillis();
            
            log.info("Document saved in " + (endTimeMs - timeMs) + " ms");
        }
        catch (SQLException e) {
            try {
               if(conn!=null) conn.rollback();
            }
            catch (SQLException e1) {
                throw new PHRException("Unable to rollback transaction after previous failure",e);
            }
        }
        finally {
            if(delete != null) {
                try { delete.close(); } catch (SQLException e) { }
            }
        }
    }

    /**
     * Deletes the given element, and any children, from the database.
     */
    @SuppressWarnings("unchecked")
    private PreparedStatement deleteElement(PreparedStatement delete, JDBCPHRElement je) throws SQLException {
        if(je.getId() != null) {
            log.info("Removing deleted element " + je);

            if(delete == null) {
                delete = this.conn.prepareStatement("update document set status = 'Deleted' where id = ?");
            }
            delete.clearParameters();
            delete.setLong(1, je.getId());
            delete.execute();
            
            List<CCRElement> children = je.getChildren();
            for (CCRElement c : children) {
                if (c instanceof JDBCPHRElement) {
                    JDBCPHRElement cje = (JDBCPHRElement) c;
                    this.deleteElement(delete, cje);
                }
            }
        }
        return delete;
    }

    protected String allocateSequence(String prefix) throws SQLException {
        String dot = prefix.length()==0? "" : ".";
        Statement s =  this.conn.createStatement();
        ResultSet resultSet = null;
        try {
            resultSet = s.executeQuery("select seq from document where seq glob '"
                                         +prefix+dot+"*' and seq not glob '"+prefix+dot+"*.*' and disposition = '"+JDBCPHRElement.Disposition.CHILD+"'"
                                         +" and status = 'Active'");
            String maxSeq = null;
            while(resultSet.next()) {
                if(maxSeq == null)
                    maxSeq = resultSet.getString(1);
                else {
                    String seq = resultSet.getString(1);
                    if(JDBCElementSorter.compareSequences(seq,maxSeq)>0) {
                        maxSeq = seq;
                    }
                }
            }
            
            // Increment last seq and return it
            if(maxSeq == null) {
                return prefix + dot + "0";
            }
            int maxSeqInt = Integer.parseInt(maxSeq.substring(prefix.length()+dot.length()));
            return prefix + dot +  (maxSeqInt+1);
        }
        finally {
            if(resultSet!=null) {
                resultSet.close();
            }
            if(s!=null) {
                s.close();
            }
        }
    }

    /**
     * @param id
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void connect(String id) throws PHRException {
        try {
            JDBCPHRDBConfig cfg = Spring.getBean("JdbcPhrDbConfig");
            
            File rootDir = new File(cfg.getFileStoreRoot());
            if(!rootDir.exists()) {
                rootDir.mkdirs();
            }
            
            // Driver to Use
            Class.forName(cfg.getDriverClass());
            
            // Open the connection
            String url = cfg.getConnectUrl().replace("{dbname}", id);
            
            // Create Connection Object to SQLite Database
            this.conn = DriverManager.getConnection(url);
            
            conn.setAutoCommit(false);

            checkDocumentTable(conn);
        }
        catch (ClassNotFoundException e) {
            throw new PHRException("Unable to connect to database " + id, e);
        }
        catch (SQLException e) {
            throw new PHRException("Unable to connect to database " + id, e);
        }
    }

    /**
     * @throws SQLException
     */
    private void checkDocumentTable(Connection conn) throws SQLException {
        // Create a Statement object for the database connection, dunno what this stuff does though.
        Statement stmt = conn.createStatement();

        // Test for the table's existence
        try {
            stmt.execute("select 1 from document");
        }
        catch(SQLException e) {
            // Does not exist?  Try and create it
            log.info("document table not found: creating ...");
            
            stmt.execute("create table document (" + 
                            "id INTEGER PRIMARY KEY," +
                            "name varchar(512)," + 
                            "seq varchar(512)," + 
                            "text_value text," + 
                            "numeric_value number(12,6)," +
                            "date_value timestamp," +
                            "status varchar(12)," +
                            "last_modified_time timestamp," +
                            "disposition varchar(12)" +
                            " )");
            log.info("Created document table successfully.");
            conn.commit();
        }
        finally {
            if(stmt != null)
                stmt.close();
        }
    }

    public void delete(String id) throws PHRException {
        if(this.conn == null)
            throw new PHRException("Not connected");
        
        try {
            this.doDelete(id);
            this.conn.commit();
        }
        catch(Throwable ex) {
            try { this.conn.rollback(); } catch (SQLException e) {  }
            if (ex instanceof PHRException)
                throw (PHRException) ex;
            else
                throw new PHRException(ex);
        }
    }
    
    public void doDelete(String id) throws PHRException {
        
        if(this.conn == null)
            throw new PHRException("Not connected");

        Statement delete = null;
        try {
            String seq = queryDocumentSeqByName(id);
            if(seq == null)
                    throw new DocumentNotFoundException("Unable to locate document " + id);
                
            delete = this.conn.createStatement();
            int rowsAffected = delete.executeUpdate("delete from document where seq like '" + seq + ".%'");
            if(rowsAffected <= 0) {
                throw new PHRException("Unable to delete existing document " + id + ": attempt failed with no rows updated");
            }
        }
        catch(SQLException ex) {
            throw new PHRException("Unable to delete document " + id, ex);
        }
        finally {
            if(delete != null)
                try { delete.close(); } catch (SQLException e) {  }
        }

    }

    public void close() throws PHRException {
        if(this.conn != null) {
            try {
                this.conn.close();
                this.conn = null;
            }
            catch (SQLException e) {
                throw new PHRException(e);
            }
        }
        else
            throw new PHRException("Connection not open");
    }
}
