/*
 * $Id$
 * Created on 03/04/2007
 */
package net.medcommons.phr.db.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Content;
import org.jdom.Namespace;

import net.medcommons.phr.PHRElement;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;

/**
 * Extends CCRElement to add attributes used to persist it independently to
 * a row in a relational database. 
 * 
 * @author ssadedin
 */
public class JDBCPHRElement extends CCRElement {
    
    public static enum Disposition {
        CHILD, ATTRIBUTE, META;
    }
        
    public static final char SEQ_SEPARATOR = '.';

    private Long id;
    
    private String fullName;
    
    private String seq;
    
    private int depth = 0;
    
    private String status;
    
    private Disposition disposition;
    
    private long lastModifiedTimeMs;
    
    /**
     */
    public JDBCPHRElement(Long id, String fullName, String seq, String status, Disposition disposition, long lastModifiedTimeMs) {
        this.id = id;
        this.fullName = fullName;
        this.seq = seq;
        this.status = status;
        this.disposition = disposition;
        this.lastModifiedTimeMs = lastModifiedTimeMs;
        init();
    }


    /**
     * Creates this element from the given result set 
     */
    public JDBCPHRElement(ResultSet rs) throws SQLException, PHRException {
        //  select id,name,seq,status,text_value,last_modified_time,disposition from document
        this.id = rs.getLong(1);
        this.fullName = rs.getString(2);
        this.seq = rs.getString(3);
        this.status = rs.getString(4);
        this.lastModifiedTimeMs = rs.getLong(6);
        init();
        String v = rs.getString(5);
        if(v != null) {
            this.setText(v);
        }
        this.disposition = Disposition.valueOf(rs.getString(7));
    }


    /**
     * 
     */
    private void init() {
        int l = seq.length();
        for(int i=0; i<l; ++i) {
            if(seq.charAt(i)==SEQ_SEPARATOR) {
                ++depth;
            }
        }
        int dotIndex = this.fullName.lastIndexOf(SEQ_SEPARATOR);
        String elementName = dotIndex >=0 ? this.fullName.substring(dotIndex+1) : this.fullName;
        this.setName(elementName);
    }


    public JDBCPHRElement(String name, String seq) {
        this.fullName = name;
        this.seq = seq;
    }
    
    /**
     * If the given element is a child of this one, inserts it.  Otherwise
     * returns false and does not insert it.
     */
    public boolean insertChild(JDBCPHRElement e) {
        
        if(e.seq.regionMatches(0, this.seq, 0, this.seq.length())) {
            
            // They match up to the point that my seq ends, but have to be careful if e is actually a sibling
            // eg.  "0.1" and "0.11.0" both match prefixes
            if(e.seq.length()>this.seq.length()) {
                if(e.seq.charAt(this.seq.length())==SEQ_SEPARATOR) { // Is a child
                    
                    // See if any of my children will accept this child
                    for (Iterator iter = this.getChildren().iterator(); iter.hasNext();) {
                        JDBCPHRElement c = (JDBCPHRElement) iter.next();
                        if(c.insertChild(e))
                            return true;
                    }
                    
                    // None of my children will look after this child, I will have to do it
                    this.addChild(e);
                    
                    return true;
                }
            }
            else 
            if(e.getDisposition()==Disposition.META) { // Has same sequence - might be meta data 
                this.getDocument().setMetaData(this,e);
            }
        }
        return false;
    }


    public String getFullName() {
        return fullName;
    }


    public Long getId() {
        return id;
    }


    public long getLastModifiedTimeMs() {
        return lastModifiedTimeMs;
    }


    public String getSeq() {
        return seq;
    }


    public String getStatus() {
        return status;
    }
    
    public String toString() {
        return new StringBuilder().append("JDBCPHRElement[id=").append(id).append(" ")
                                  .append(fullName).append(".").append(seq).append("]").toString();
    }

    public Disposition getDisposition() {
        return disposition;
    }


    public void setDisposition(Disposition disposition) {
        this.disposition = disposition;
    }


    public void setSeq(String seq) {
        this.seq = seq;
    }
    
        
}
