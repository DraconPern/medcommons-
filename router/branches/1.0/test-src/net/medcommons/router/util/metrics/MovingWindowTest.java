/*
 * $Id: $
 * Created on Jan 12, 2005
 */
package net.medcommons.router.util.metrics;

import junit.framework.TestCase;
import net.medcommons.modules.utils.metrics.MovingWindow;

/**
 * @author ssadedin
 */
public class MovingWindowTest extends TestCase {

  public void testEmptyWindow() {
    System.out.println("Empty Window Test");
    MovingWindow window = new MovingWindow(5);
    assertEquals(window.getValue(), new Double(0.0));
  }

  public void testZeroSizeWindow() {
    System.out.println("Zero Size Window Test");
    MovingWindow window = new MovingWindow(0);
    assertEquals(window.getValue(), new Double(0.0));
    
    // Add a sample to the zero size window
    window.sample(new Long(0));
    assertEquals(window.getValue(), new Double(0.0));    
  }
  
  public void testHalfFilledWindow() {
    System.out.println("testHalfFilledWindow");
    MovingWindow window = new MovingWindow(5);
    window.sample(new Long(0));
    assertEquals( new Double(0.0),window.getValue());    
    window.sample(new Long(1));
    assertEquals(1.0, window.getValue().doubleValue(),0.01);    
    window.sample(new Long(1));
    assertEquals(2.0, window.getValue().doubleValue(), 0.01);    
    window.sample(new Long(1));
    assertEquals(3.0, window.getValue().doubleValue(), 0.01);    
    window.sample(new Long(1));
    assertEquals(4.0, window.getValue().doubleValue(), 0.01);    
    window.sample(new Long(1));
    assertEquals(5.0, window.getValue().doubleValue(), 0.01);    
    window.sample(new Long(1));
    assertEquals(5.0, window.getValue().doubleValue(), 0.01);    
  }
}
