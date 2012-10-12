/*
 * $Id: $
 * Created on Jan 3, 2005
 */
package net.medcommons.modules.utils.metrics;

import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * Metric is an abstract class that represents any kind of measurement that
 * might be applied to the system.  It also provides convenience and factory
 * methods for obtaining metrics by name.
 * 
 * @author ssadedin
 */
public abstract class Metric {
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(Metric.class);
  
  /**
   * Global registry of Metrics
   */
  private static Hashtable metrics = new Hashtable(20);

  /**
   * Returns the current value of this metric.
   */
  public abstract Number getValue();
  
  /**
   * Add a sample to this Metric.  The Number obj represents
   * the sample point.
   */
  public abstract void sample(Number obj);
  
  /**
   * Convenience method to create a sample with a null value
   */
  public void sample() {
    this.sample(null);
  }

  /**
   * Looks up the metric with the given name and adds a sample with null
   * value to it.
   */
  public static void addSample(String name) {
    getInstance(name).sample();
  }
  
  /**
   * Looks up the metric with the given name and adds a sample with null
   * value to it.
   */
  public static void addSample(String name, Number value) {
    getInstance(name).sample(value);
  }
  
  /**
   * Registers the given metric with the given name.
   * @param name
   * @param metric
   */
  public static void register(String name, Metric metric) {
    if(metrics.contains(name)) {
      log.warn("Adding duplicate metric with name " + name);
    }    
    metrics.put(name,metric);
    log.info("Registered Metric " + name + " of type " + metric.getClass().getName());
  }
  
  /**
   * Looks up and returns the requested Metric in the global
   * Metric registry.  If the Metric is not present then it is created
   * as a default SimpleCounter.
   * 
   * @param name - name of Metric to look up.
   * @return
   */
  public static Metric getInstance(String name) {
    Metric metric = (Metric) metrics.get(name);    
    if(metric == null) {
      metric = new SimpleCounter();
      register(name,metric);
    }    
    return metric;
  }
}
