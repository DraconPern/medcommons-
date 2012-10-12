/*
 * $Id: $
 * Created on Jan 3, 2005
 */
package net.medcommons.modules.utils.metrics;

/**
 * A simple metric that models an integer value 
 * 
 * @author ssadedin
 */
public class SimpleCounter extends Metric {

  /**
   * The value of this metric
   */
  private long value;

  public Number getValue() {
    return new Long(value);
  }

  /**
   * Increments the count
   */
  public void sample(Number obj) {
    if(obj != null)
      value += obj.longValue();
    else
      ++value;
      
    // Don't let it wrap negative!
    if(value<0)
      value = 0;
  }
}
