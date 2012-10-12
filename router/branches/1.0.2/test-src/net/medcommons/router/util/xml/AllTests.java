/*
 * $Id$
 * Created on 8/04/2005
 */
package net.medcommons.router.util.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ssadedin
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for net.medcommons.router.util.xml");
        //$JUnit-BEGIN$
        suite.addTestSuite(XPathMappingsTest.class);
        //$JUnit-END$
        return suite;
    }
}
