/*
 * $Id: $
 * Created on Jan 12, 2005
 */
package net.medcommons.router.util.metrics;

import junit.framework.TestCase;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.SimpleCounter;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;

/**
 * @author ssadedin
 */
public class TimeSampledMetricTest extends TestCase {

  public void testGradient() throws Exception {
    Metric metric = new SimpleCounter();
    metric.sample(new Long(5));
    TimeSampledMetric tsm = new TimeSampledMetric(metric, 1000, 5);
    
    System.out.println("Waiting for buffer to fill ...");
    
    // Wait 6 seconds
    Thread.sleep(6000);    
    
    // After 5 seconds the window should be full of samples, all 5
    assertEquals(5.0, tsm.getValue().doubleValue(), 0.1);
    assertEquals(0.0, tsm.getGradient().doubleValue(),0.01);
    
    // Add a new value
    metric.sample(new Long(10));    
    
    // Wait for 1 sample to occur
    Thread.sleep(1005);
    
    // Now we should have 5 samples in the window, last = 15, first = 5
    // so rate of change is 10 / 5 seconds = 2/second
    assertEquals(2.0, tsm.getGradient().doubleValue(), 0.5);
  }

}
