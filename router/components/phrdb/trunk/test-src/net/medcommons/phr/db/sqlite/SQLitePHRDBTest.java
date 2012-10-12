/*
 * $Id$
 * Created on 03/04/2007
 */
package net.medcommons.phr.db.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import net.medcommons.phr.BaseTest;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.db.xml.XMLPHRDB;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.phr.resource.Spring;

public class SQLitePHRDBTest extends BaseTest {

    public SQLitePHRDBTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        File d = new File("test-data/sqldb");
        if(!d.exists()) {
            d.mkdirs();
        }
    }

    public void testOpen() throws Exception {
        SQLitePHRDB db = new SQLitePHRDB();
        db.connect("sample_ccr");
        db.open("sample_ccr");
        db.close();
    }

    public void testSave() throws Exception {
        // Delete the database, if it exists
        File f = new File("test-data/sqldb/ccr_lots_of_data.db");
        f.delete();
        
        // Open CCR from XML
        XMLPHRDB xmlDb = new XMLPHRDB();
        PHRDocument d = xmlDb.open("ccr_minimal");
        
        SQLitePHRDB db = new SQLitePHRDB();
        db.connect("testdb");
        db.save("ccr_lots_of_data", d);
        db.close();
     }

    public void testUpdate() throws Exception {
        // Delete the database, if it exists
        File f = new File("test-data/sqldb/update.db");
        f.delete();
        
        // Open CCR from XML
        XMLPHRDB xmlDb = new XMLPHRDB();
        PHRDocument d = xmlDb.open("ccr_minimal");
        
        // Save in sqldb
        SQLitePHRDB db = new SQLitePHRDB();
        db.connect("update");
        d.setValue("patientGivenName", "save1");
        db.save("minimal1", d);
        db.close();
        
        db.connect("update");
        PHRDocument d1 = db.open("minimal1");
        d1.setValue("patientGivenName", "save2");  // Note: exists in original
        d1.setValue("patientMiddleName", "save2Middle"); // Note: does not exist in original
        db.save("minimal1",d1);
        db.close();
        
        db.connect("update");
        PHRDocument d2 = db.open("minimal1");
        XMLPHRDocument xd = (XMLPHRDocument) d2;
        assertEquals("save2Middle",d2.getValue("patientMiddleName"));
        
        // Since the middle name was added, check that it got the right sequence - not the same as one of the siblings
        JDBCPHRElement pgn = (JDBCPHRElement) xd.queryProperty("patientGivenName");
        JDBCPHRElement pmn = (JDBCPHRElement) xd.queryProperty("patientMiddleName");
        JDBCPHRElement pfn = (JDBCPHRElement) xd.queryProperty("patientFamilyName");
        assertTrue(JDBCElementSorter.compareSequences(pfn.getSeq(),pmn.getSeq())>0);
        assertTrue(JDBCElementSorter.compareSequences(pmn.getSeq(),pgn.getSeq())>0);
        db.close();
    }
    
    public void testSave2Documents() throws Exception {
        // Delete the database, if it exists
        File f = new File("test-data/sqldb/save2.db");
        f.delete();
        // Open CCR from XML
        XMLPHRDB xmlDb = new XMLPHRDB();
        PHRDocument d = xmlDb.open("ccr_minimal");
        
        // Save in sqldb
        SQLitePHRDB db = new SQLitePHRDB();
        db.connect("save2");
        d.setValue("patientGivenName", "save1");
        db.save("minimal1", d);
        d.setValue("patientGivenName", "save2");
        db.save("minimal2", d);
        db.close();
        
        // Open them and check!
        db.connect("save2");
        PHRDocument d1 = db.open("minimal1");
        PHRDocument d2 = db.open("minimal2");
        db.close();
        
        // Even though we saved as different name, should still be same document
        //assertEquals("save2",d1.getValue("patientGivenName"));
        assertEquals("save2",d2.getValue("patientGivenName"));
    }
    
    public void testAlloc() throws Exception {
        SQLitePHRDB db = new SQLitePHRDB();
        db.connect("sample_ccr");
        db.open("sample_ccr");
        
        assertEquals("only one ccr stored, next seq should be 1","1",db.allocateSequence(""));
        assertEquals("0.5.0.2",db.allocateSequence("0.5.0"));
        assertEquals("0.8.0.0.7",db.allocateSequence("0.8.0.0"));
        // Non-existent
        assertEquals("0.8.0.164.0",db.allocateSequence("0.8.0.164"));
        
        db.close();
    }

    public void testDelete() throws Exception {
        // Delete the database, if it exists
        File f = new File("test-data/sqldb/delete.db");
        f.delete();
        
        // Open CCR from XML
        XMLPHRDB xmlDb = new XMLPHRDB();
        PHRDocument d = xmlDb.open("ccr_minimal");
        xmlDb.close();
        
        // Save in sqldb
        SQLitePHRDB db = new SQLitePHRDB();
        db.connect("delete");
        db.save("testdelete", d);
        db.close();
        
        db.connect("delete");
        d = db.open("testdelete");
        assertNotNull(d.queryProperty("patientFamilyName"));
        d.remove("patientFamilyName");
        assertNull(d.queryProperty("patientFamilyName"));
        db.save("testdelete", d);
        db.close();
        
        db.connect("delete");
        d = db.open("testdelete");
        db.close();
        assertNull(d.queryProperty("patientFamilyName"));
    }
    
    public void TestSQLiteAutoCommit() throws Exception {
        JDBCPHRDBConfig cfg = Spring.getBean("JdbcPhrDbConfig");
        
        File testdb = new File("test.db");
        if(testdb.exists() && !testdb.delete())
            throw new RuntimeException("Failed to delete test database");
        
        // Driver to Use
        Class.forName(cfg.getDriverClass());
        
        // Create Connection Object to SQLite Database
        Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");        
        c.setAutoCommit(false);
        
        Statement s = c.createStatement();
        s.execute("create table fubar ( foo integer,  bar varchar(60) )");
        c.commit();
        
        s.execute("insert into fubar values (1,'foo')");
        c.commit();
        
        ResultSet r = s.executeQuery("select * from fubar");
        if(!r.next()) throw new RuntimeException("Couldn't select");
        r.close();
        s.close();
        c.close();
        
        c = DriverManager.getConnection("jdbc:sqlite:test.db");
        c.setAutoCommit(false);
        s = c.createStatement();
        r = s.executeQuery("select * from fubar");
        if(!r.next()) throw new RuntimeException("Couldn't select");
        r.close();
        
        s.execute("insert into fubar values (1,'foo')");
        c.commit();
 
        s.close();
        c.close();
    }

}
