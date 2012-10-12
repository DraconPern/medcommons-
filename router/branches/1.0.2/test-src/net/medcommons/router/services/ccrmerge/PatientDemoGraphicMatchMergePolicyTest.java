/*
 * $Id: PatientDemoGraphicMatchMergePolicyTest.java 2758 2008-07-18 10:52:22Z ssadedin $
 * Created on 17/07/2008
 */
package net.medcommons.router.services.ccrmerge;

import net.medcommons.router.util.BaseTestCase;

import org.junit.Before;
import org.junit.Test;

public class PatientDemoGraphicMatchMergePolicyTest extends BaseTestCase {

    public PatientDemoGraphicMatchMergePolicyTest() throws Exception {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCheckDateMatch() {
        PatientDemoGraphicMatchMergePolicy p = new PatientDemoGraphicMatchMergePolicy();
        
        /*
        assertTrue(p.checkDateMatch("2000", "2000"));
        assertTrue(p.checkDateMatch("2000", "3/3/2000"));
        assertTrue(p.checkDateMatch("3/3/2000", "2000"));
        assertTrue(p.checkDateMatch("3/2000", "2000"));
        assertTrue(p.checkDateMatch("3/2000", "3/3/2000"));
        
        assertTrue(!p.checkDateMatch("1/2000", "2/1/2000"));
        assertTrue(p.checkDateMatch("2/2000", "2/1/2000"));
        
        assertTrue(!p.checkDateMatch("1/2000", "2001"));
        assertTrue(!p.checkDateMatch("2000", "2001"));
        
        assertTrue(p.checkDateMatch("2000-03-03", "2000"));
        assertTrue(p.checkDateMatch("2000-03", "2000-03"));
        assertTrue(p.checkDateMatch("2000-03-03", "2000-03"));
        assertTrue(p.checkDateMatch("2000-03-13T04:31:20.0Z", "2000-03"));
        
        assertTrue(!p.checkDateMatch("2001-01-01", "2000"));
        assertTrue(!p.checkDateMatch("2001-03", "2000-03"));
        assertTrue(!p.checkDateMatch("2001-03-01", "2000-03"));
        assertTrue(!p.checkDateMatch("2000-02-03", "2000-03"));
        assertTrue(!p.checkDateMatch("2000-01-01", "2000-02-01"));
        assertTrue(!p.checkDateMatch("2000-03-01", "2000-03-02"));
        assertTrue(!p.checkDateMatch("2001-01-13T04:31:20.0Z", "2000-01"));
        assertTrue(!p.checkDateMatch("2000-02-13T04:31:20.0Z", "2000-01"));
        assertTrue(!p.checkDateMatch("2000-01-02T04:31:20.0Z", "2000-01-01"));
        */
    }

}
