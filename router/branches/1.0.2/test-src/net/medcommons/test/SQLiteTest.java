/*
 * $Id$
 * Created on 09/03/2007
 */
package net.medcommons.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestCase;

public class SQLiteTest extends TestCase {

    public SQLiteTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testSQLite() throws Exception {
        
        try {
            // The SQLite (3.3.8) Database File
            // This database has one table (pmp_countries) with 3 columns (country_id, country_code, country_name)
            // It has like 237 records of all the countries I could think of.
            String fileName = "pmp.db";
            
            // Driver to Use
            // http://www.zentus.com/sqlitejdbc/index.html
            Class.forName("org.sqlite.JDBC");
            
            // Create Connection Object to SQLite Database
            // If you want to only create a database in memory, exclude the +fileName
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
            
            // Create a Statement object for the database connection, dunno what this stuff does though.
            Statement stmt = conn.createStatement();
            
            // Create a result set object for the statement
            // ResultSet rs = stmt.executeQuery("SELECT * FROM pmp_countries ORDER BY country_name ASC");
            stmt.execute("create table person ( name varchar(20), age number(3,1) )");
            
            // Iterate the result set, printing each column
            // if the column was an int, we could do rs.getInt(column name here) as well, etc.
//            while (rs.next()) {
//                String id   = rs.getString("country_id");   // Column 1
//                String code = rs.getString("country_code"); // Column 2
//                String name = rs.getString("country_name"); // Column 3
//                
//                System.out.println("ID: "+id+" Code: "+code+" Name: "+name);
//            }
            
            // Close the connection
            conn.close();
            
        }
        catch (Exception e) {
            // Print some generic debug info
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        }
        
        
    }

}
