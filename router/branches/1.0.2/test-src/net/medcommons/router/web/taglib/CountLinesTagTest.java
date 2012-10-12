/*
 * $Id$
 * Created on 08/02/2007
 */
package net.medcommons.router.web.taglib;

import junit.framework.TestCase;

public class CountLinesTagTest extends TestCase {

    CountLinesTag t = new CountLinesTag();
    
    public CountLinesTagTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testNoLines() {
        t.setValue("");
        t.setCols(80);
        assertEquals(t.count(), 0);
    }

    public void testOneLine() {
        t.setValue("The quick lazy fox");
        t.setCols(80);
        assertEquals(t.count(), 1);
    }
    
    public void testWrappedLine() {
        t.setValue("The quick lazy fox jumped over the lazy dog");
        t.setCols(40);
        assertEquals(2, t.count());
    }
    public void testBlankLines() {
        t.setValue("\n\n\n\n");
        t.setCols(40);
        assertEquals(4, t.count());
    }

    public void testBlankLineSeparator() {
        t.setValue("\n\nBig Dog\n\nLonely Chicken.");
        t.setCols(80);
        assertEquals(5,t.count());
    }    
        public void testCR() {
        t.setValue("\r\n\nBig Dog\n\nLonely Chicken.");
        t.setCols(80);
        assertEquals(5,t.count());
    }
}
