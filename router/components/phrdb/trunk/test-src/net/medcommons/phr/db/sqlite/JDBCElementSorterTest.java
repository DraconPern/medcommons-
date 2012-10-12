/*
 * $Id$
 * Created on 04/04/2007
 */
package net.medcommons.phr.db.sqlite;

import junit.framework.TestCase;

public class JDBCElementSorterTest extends TestCase {

    public JDBCElementSorterTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCompare() {
        assertEquals(0, compare("0","0"));
        assertTrue(compare("0","1")<0);
        assertTrue(compare("1","0")>0);
        assertEquals(0, compare("0.1.3.456.2","0.1.3.456.2"));
        assertTrue(compare("0.3.2","0.1.56.4")>0);
        assertTrue(compare("0.2.75.1","0.2.73.1")>0);
        assertTrue(compare("0.2.25.1","0.2.5.1")>0);
        assertTrue(compare("1","0.1.3.456.2")>0);
        assertTrue(compare("0","0.1")<0);
        assertTrue(compare("0.1","0")>0);
    }
    
    private int compare(String seq1, String seq2) {
        JDBCPHRElement e = new JDBCPHRElement("test",seq1);
        JDBCPHRElement e2 = new JDBCPHRElement("test",seq2);
        JDBCElementSorter s = new JDBCElementSorter();
        return s.compare(e, e2);
    }

}
