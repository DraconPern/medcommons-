/*
 * $Id$
 * Created on Jan 12, 2005
 */
package net.medcommons.modules.utils.metrics;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * TimeSampledMetric augments a MovingWindow by adapting it to a TimerTask interface
 * and scheduling samples periodically at a user configurable time period.
 * 
 * @author ssadedin
 */
public class TimeSampledMetric extends Metric {
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(TimeSampledMetric.class);

  /**
   * The metric that is sampled periodically
   */
  private Metric metric;
  
  /**
   * The internal buffer of samples
   */
  private MovingWindow window;
  
  /**
   * The TimerTask used to perform periodic sampling
   */
  private TimerTask timerTask;
  
  /**
   * The timer used to run the sample TimerTask
   */
  private Timer timer;
  
  /**
   * The period at which the metric will be sampled in milliseconds.
   */
  private long samplePeriodMs;

  /**
   * Creates a TimeSampledMetric that samples the given metric periodically every
   * samplePeriodMs milliseconds into a buffer with windowSize elements.
   * 
   * @param metric
   * @param samplePeriodMs
   * @param windowSize
   */
  public TimeSampledMetric(final Metric metric, long samplePeriodMs, int windowSize) {
    this.metric = metric;
    this.window = new MovingWindow(windowSize);
    this.timer = new Timer(true);   
    this.samplePeriodMs = samplePeriodMs;
    this.timerTask = new TimerTask() {
      public void run() {
        try {
          sample();
        }
        catch(Exception e) {
          log.warn("Exception while sampling metric", e);
        }
      }      
    };    
    this.timer.scheduleAtFixedRate(this.timerTask, samplePeriodMs, samplePeriodMs);
  }
  
  /**
   * Returns the average of the values in the sample window.
   * 
   * @see net.medcommons.modules.utils.metrics.Metric#getValue()
   */
  public Number getValue() {    
    return new Double(window.getValue().doubleValue() / (double)window.getSize());
  }
  
  /**
   * Returns the rate of change of the values in the sample window
   * in units/second.
   */
  public Number getGradient() {
    return new Double(window.getGradient().doubleValue() * (this.samplePeriodMs/1000.0));    
  }

  /**
   * @see net.medcommons.modules.utils.metrics.Metric#sample(java.lang.Number)
   */
  public void sample(Number obj) {
    Number value = metric.getValue();
    //log.debug("Sampled value " + value + " buffer="+ this.window.toString() + " gradient=" + this.getGradient());
    //System.out.println("Sampled value " + value);
    window.sample(value);
  }
  
  public String toString() {
    return super.toString() + "(samplePeriod="+samplePeriodMs+",window="+window.toString() + ")";
  }
}
