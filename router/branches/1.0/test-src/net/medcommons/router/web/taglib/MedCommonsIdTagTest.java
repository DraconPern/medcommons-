/*
 * $Id: MedCommonsIdTagTest.java 2402 2008-02-14 06:51:52Z ssadedin $
 * Created on 14/02/2008
 */
package net.medcommons.router.web.taglib;

import static net.medcommons.router.web.taglib.MedCommonsIdTag.writeMcId;
import static org.junit.Assert.*;

import java.io.StringWriter;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;


public class MedCommonsIdTagTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testWriteMcId() throws Exception {
        
        // Write a 16 digit id
        StringWriter w = new StringWriter();
        writeMcId("0123456789012345", w);
        assertEquals("0123 4567 8901 2345", w.toString());
        
        w = new StringWriter();
        writeMcId("012345678901234", w);
        assertEquals("0123 4567 8901 234", w.toString());
        
        w = new StringWriter();
        writeMcId("01234567890", w);
        assertEquals("0123 4567 890", w.toString());
        
        w = new StringWriter();
        writeMcId("01234567", w);
        assertEquals("0123 4567", w.toString());
        
        w = new StringWriter();
        writeMcId("012", w);
        assertEquals("012", w.toString());
    }
    
    public static junit.framework.Test suite() { 
        return new JUnit4TestAdapter(MedCommonsIdTagTest.class); 
    }
}
