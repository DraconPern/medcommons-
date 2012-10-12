/*
 * $Id$
 * Created on Jan 11, 2005
 */
package net.medcommons.modules.utils.metrics;

/**
 * Sample is a value of a Metric at a particular time.  It attaches a time
 * to a Metric value. 
 * 
 * @author ssadedin
 */
public class Sample {
  
  
  /**
   * The value of the metric
   */
  private Number value;
  
  /**
   * The time at which the Sample was created.
   */
  private long timeStamp;
  
  
  /**
   * Creates a sample with the given value.
   * 
   * @param value
   */
  public Sample(Number value) {
    super();
    this.value = value;
    this.timeStamp = System.currentTimeMillis();
  }
  
  
  public long getTimeStamp() {
    return timeStamp;
  }
  
  public Number getValue() {
    return value;
  }
}
