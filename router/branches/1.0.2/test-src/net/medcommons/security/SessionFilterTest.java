/*
 * $Id$
 * Created on 01/06/2007
 */
package net.medcommons.security;

import java.util.Hashtable;

import net.medcommons.security.SessionFilter;

import junit.framework.TestCase;

/**
 * Tests Session Filter to ensure it decodes encoded parameters correctly.
 * 
 * @author ssadedin
 */
public class SessionFilterTest extends TestCase {

    public SessionFilterTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testParseParameters() {
        String testString = "";
        Hashtable<String, String[]> p = testParse(testString);
        assertTrue("Empty parameter string should have no parameters", p.isEmpty());
        assertTrue(testParse("foo=1").size()==1);
        assertTrue(testParse("foo=1").get("foo")[0].equals("1"));
        assertTrue(testParse("foo=1&foo=2").get("foo")[1].equals("2"));
        assertTrue(testParse("foo=&foo=2").get("foo")[1].equals("2"));
        assertTrue(testParse("foo&foo=2").get("foo")[1].equals("2"));
        assertTrue(testParse("foo&foo=2").get("foo")[0].equals(""));
        assertTrue(testParse("bar&foo=2").get("bar")[0].equals(""));
        assertTrue(testParse("bar&foo=2").get("foo")[0].equals("2"));
        assertTrue(testParse("foo").size()==1);
        
        p = testParse("combined&g=37e1d54bed6c50934e347d12a1ac30c4384f7da5&t=&a=1013062431111407&m=&c=&auth=68a56a3d43e41a49a42b11474a020281f33d2b6c");
        assertTrue(p.size()==7);
        assertTrue(p.get("combined")!=null);
        assertTrue(p.get("combined").length==1);
        assertTrue(p.get("g").length==1);
        assertTrue(p.get("g")[0].equals("37e1d54bed6c50934e347d12a1ac30c4384f7da5"));
    }

    /**
     * @param testString
     * @return
     */
    private Hashtable<String, String[]> testParse(String testString) {
        Hashtable<String, String[]> p = new Hashtable<String, String[]>();
        SessionFilter.parseParameters(testString, p);
        return p;
    }

}
