/*
 * $Id$
 * Created on Jan 11, 2005
 */
package net.medcommons.modules.utils.metrics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * MovingWindow is a kind of Metric that keeps a buffer of a constant number
 * of samples.  Once the window is full, adding a new sample causes the 
 * oldest sample to drop out of the window and be forgotten.
 * 
 * The default value of the window is the total of the samples in the
 * buffer, but it can also calculate other statistics on the contained
 * samples.  
 * 
 * @author ssadedin
 */
public class MovingWindow extends Metric {
  
  /**
   * The internal buffer of samples.
   */
  private List samples = new LinkedList();
  
  /**
   * The maximum allowed size of the buffer.
   */
  private int size;
  
  /**
   * Creates a MovingWindow with the given size buffer
   */
  public MovingWindow(int size) {
    super();
    this.size = size;
  }
  
  /**
   * Returns the average of the values in this MovingWindow
   */
  public Number getValue() {
    synchronized(this.samples) {
      // Calculate the total
      double total = 0.0;
      for (Iterator iter = this.samples.iterator(); iter.hasNext();) {
        Sample sample = (Sample) iter.next();
        total += sample.getValue().doubleValue();
      }
      return new Double(total);
    }
  }

  /**
   * @see net.medcommons.modules.utils.metrics.Metric#sample(java.lang.Object)
   */
  public void sample(Number obj) {
    synchronized(this.samples) {
	    this.samples.add(new Sample(obj));
	    while(this.samples.size()>size) {
	      // Remove oldest (1st) sample
	      this.samples.remove(0);
	    }
	    
	  }
  }
  
  
  /**
   * Returns the rate of change of the values in the sample window
   * 
   * @return
   */
  public Number getGradient() {
    synchronized(this.samples) {
      if(samples.isEmpty())
        return new Double(0.0);
      double mostRecentSample = ((Sample)samples.get(samples.size()-1)).getValue().doubleValue(); 
      double oldestSample = ((Sample)samples.get(0)).getValue().doubleValue();
      double gradient = 
        ((mostRecentSample - oldestSample)/samples.size());
  
      return new Double(gradient);    
    }
  }
  
  public int getSize() {
    return this.samples.size();
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer(20);
    for (ListIterator iter = this.samples.listIterator(); iter.hasNext();) {
      if(iter.hasPrevious())
        buffer.append(",");
      Sample sample = (Sample) iter.next();
      buffer.append(sample.getValue());
    }
    return buffer.toString();
  }  
}
