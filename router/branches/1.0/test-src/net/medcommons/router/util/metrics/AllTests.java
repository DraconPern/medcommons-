/*
 * $Id: $
 * Created on Jan 12, 2005
 */
package net.medcommons.router.util.metrics;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ssadedin
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for net.medcommons.router.util.metrics");
    //$JUnit-BEGIN$
    suite.addTestSuite(MovingWindowTest.class);
    suite.addTestSuite(TimeSampledMetricTest.class);
    //$JUnit-END$
    return suite;
  }
}
