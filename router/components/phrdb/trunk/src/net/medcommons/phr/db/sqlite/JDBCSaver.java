/*
 * $Id$
 * Created on 03/04/2007
 */
package net.medcommons.phr.db.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.medcommons.phr.PHRElement;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.sqlite.JDBCPHRElement.Disposition;
import net.medcommons.phr.db.xml.XMLPHRDocument;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

public class JDBCSaver {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(JDBCSaver.class);

    private Connection c;

    private String name;
    
    protected Stack<Integer> sequence = new Stack<Integer>();
    
    private int count = 0;
    
    private int depth = 0;
    
    private XMLPHRDocument document = null;

    protected PreparedStatement insert = null;
    protected PreparedStatement update = null;
    protected PreparedStatement select = null;
    protected PreparedStatement delete = null;

    /**
     * @param c - connection to use
     * @param name - name of document to add in meta data
     * @throws SQLException 
     */
    public JDBCSaver(Connection c, XMLPHRDocument doc, String name) throws SQLException {
        super();
        this.c = c;
        this.name = name;
        this.document = doc;
    }

    public CCRElement save(String prefix,String seq, CCRElement e) throws SQLException, PHRException {
        
        ++count;
        
        boolean isRoot = false;
        try {
            // Remember root node so we know when we are finished
            if(count == 1) {
                isRoot = true;
                this.prepare();
            } 
            ++depth;
            
            // log.debug("Storing element " + prefix + " with seq " + seq);

   
            // Is it a text node?
            // If from is a simple text node, then set it's value in to and return
            List<Content> contents = e.getContent();
            boolean allText = false;
            for (Content content : contents) {
                if(content instanceof Text)
                    allText = true;
    
                if(content instanceof Element) {
                    allText = false;
                    break;
                }
            }
      
            // Save meta data
            saveMetaData(prefix, seq, e);
    
            if(!doUpdate(e, seq, allText)) { // Is the child already attached to the database?
                e = doInsert(prefix, seq, e, allText);
            }
      
            if(!allText) {
                List<Content> children = new ArrayList<Content>(e.getChildren()); 
                int i=0;
                for(Content c : children) {
                    if(c instanceof CCRElement) {
                        CCRElement ccrEl = (CCRElement) c;
                        String childSeq = seq+"."+i;
                        //log.info("Saving element " + ccrEl+ " at sequence " + childSeq);
                        save(prefix + "." + ccrEl.getName(),childSeq,ccrEl);
                        ++i;
                    }
                }
            }
            return e;
        }
        finally {
            if(isRoot) { // root node finished, we are done, so close statement
                try { this.insert.close(); } catch (Exception e1) {  }
                try { this.update.close(); } catch (Exception e1) {  }
                try { this.select.close(); } catch (Exception e1) {  }
                try { this.delete.close(); } catch (Exception e1) {  }
            }
        }
    }

    /**
     * Saves all the meta data for the given element 
     * @throws PHRException 
     */
    private void saveMetaData(String prefix, String seq, CCRElement e) throws SQLException, PHRException {
        Map<String, PHRElement> meta = e.getDocument().getMetaData(e);
        if(meta != null) {
            for (String key : meta.keySet()) {
                PHRElement me = meta.get(key);
                if(me == null) {
                    // TODO: should actually delete it in the database
                    continue; 
                }
                
                boolean updated = false;
                if(me instanceof JDBCPHRElement) {
                    JDBCPHRElement jme = (JDBCPHRElement) me;
                    if(jme.getId()!=null) {
                        this.doUpdate(jme, seq, true);
                        updated = true;
                    }
                }
                if(!updated)
                    doInsert(prefix+"."+key,seq,me,true);
            }
        }
    }

    /**
     * Performs a physical update of the given element, if it exists
     * in the database. Returns true iff an update occurs.
     * @throws PHRException 
     */
    private boolean doUpdate(CCRElement e, String seq, boolean allText) throws SQLException, PHRException {
        JDBCPHRElement je = null;
        if (e instanceof JDBCPHRElement) {
            je = (JDBCPHRElement) e;
        }
        else {
            // Check database for this seq
            /*
            this.select.clearParameters();
            this.select.setString(1,seq);
            ResultSet r = this.select.executeQuery();
            if(r.next()) { // Found the row
                je = new JDBCPHRElement(r);
            }
            */
            
        }
        
        if(je != null) {
            if(je.getId()!=null) {
                // Update the node
                // name=?,seq=?,text_value=?,numeric_value=?,date_value=?,status,last_modified_time=? where id = ?
                int col = 0;
                this.update.clearParameters();
                this.update.setString(++col, je.getFullName());
                this.update.setString(++col, seq);
                if(allText) {
                    String value = je.getTextTrim();
                    this.update.setString(++col, value);
                    setDateValue(this.update, ++col, je);
                    setNumericValue(this.update, ++col, value);
                }                
                else {
                    insert.setNull(++col,Types.VARCHAR);        
                    insert.setNull(++col,Types.DOUBLE); // Numeric value
                    insert.setNull(++col, Types.TIMESTAMP); // Timestamp value
                }
                this.update.setLong(++col, new Long(System.currentTimeMillis()));
                this.update.setLong(++col, je.getId());
                this.update.execute();
                if(this.update.getUpdateCount()>0) {
                    // Make sure it ends up with same sequence as stored in database
                    je.setSeq(seq);
                    return true;
                }
                else {
                    log.debug("Update of element " + je + " failed: element deleted?");
                    throw new PHRException("Update of existing element failed.  Concurrent database edits?");
                }
            }
        }
        return false;
    }

    /**
     * Performs a physical insert into the database
     * @throws PHRException 
     */
    private JDBCPHRElement doInsert(String prefix, String seq, PHRElement e, boolean allText) throws SQLException, PHRException {
        
        log.debug("Inserting element " + e + " at seq " + seq);
        
        int col = 1;
        insert.clearParameters();
        insert.setString(col++,prefix);
        insert.setString(col++,seq);    
        if(allText) { // Save text, no children
            String value = e.getElementValue();
            insert.setString(col++,value);
            setNumericValue(this.insert,col++, value);
   
            // Is it an ExactDateTime? 
            setDateValue(insert, col++, e);
        }
        else {
            insert.setNull(col++,Types.VARCHAR);        
            insert.setNull(col++,Types.DOUBLE); // Numeric value
            insert.setNull(col++, Types.TIMESTAMP); // Timestamp value
        }
        insert.setString(col++,"Active");
        long currentTimeMillis = System.currentTimeMillis();
        insert.setLong(col++, currentTimeMillis);
        if(e instanceof JDBCPHRElement) {
            JDBCPHRElement je = ((JDBCPHRElement)e);
            insert.setString(col++, je.getDisposition().name());
            je.setSeq(seq);
        }
        else
            insert.setString(col++, JDBCPHRElement.Disposition.CHILD.name());
        
        insert.execute();
        
        if(! (e instanceof JDBCPHRElement)) {
            ResultSet keys = null;
            try {
                keys = insert.getGeneratedKeys();
                if(keys.next()) {
                    JDBCPHRElement je = new JDBCPHRElement(keys.getLong(1), prefix, seq, "Active", Disposition.CHILD, currentTimeMillis);
                    je.setContent(e.removeContent());
                    if(e.getParentElement()==null) {
                        this.document.setRootElement(je);
                    }
                    else {
                        e.getParentElement().replaceChild(e,je);
                    }
                    return je;
                }
                else
                    throw new PHRException("No key generated from newly inserted phr element");
            }
            finally {
                if(keys != null)
                    keys.close();
            }
        }
        else
            return (JDBCPHRElement)e;
    }

    /**
     * Sets appropriate value for numeric field on given prepared statement
     */
    private void setNumericValue(PreparedStatement p, int col, String value) throws SQLException {
        try {
            double d = Double.parseDouble(value);
            p.setDouble(col,d); // Numeric value
        }
        catch (NumberFormatException e2) {
            p.setNull(col,Types.DOUBLE); // Numeric value
        }
    }

    /**
     * Sets appropriate value for date on given prepared statement
     */
    private void setDateValue(PreparedStatement p,int col, PHRElement e) throws SQLException {
        boolean dateWasSet = false;
        String value = e.getElementValue();
        if("ExactDateTime".equals(e.getName())) {
            // Parse and store time
            try {
                Date d = CCRElement.parseDate(value);
                p.setLong(col,d.getTime()); // Timestamp value
                dateWasSet = true;
            }
            catch (ParseException e1) {
                log.warn("Unable to parse date " + value);
            }
        }
        if(!dateWasSet)
            p.setNull(col, Types.TIMESTAMP); // Timestamp value            
    }

    private void prepare() throws SQLException {
        this.insert =  c.prepareStatement("insert into document ("+
                        "id,name,seq,text_value,numeric_value,date_value,status,last_modified_time,disposition)" +
                        "values (NULL,?,?,?,?,?,?,?,?)");
        
        this.update = c.prepareStatement(
            "update document set " +
            "name=?,seq=?,text_value=?,numeric_value=?,date_value=?,last_modified_time=? where id = ?");
        
        this.select = 
            c.prepareStatement("select id,name,seq,status,text_value,last_modified_time,disposition from document where seq = ?");
        
        this.delete = 
            c.prepareStatement("delete from document where seq = ? and status = 'Active' and disposition = '"+Disposition.CHILD.name()+"'");
     }
}
