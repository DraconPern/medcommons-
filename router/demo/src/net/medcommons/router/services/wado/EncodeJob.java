/*
 * $Id: $
 * Created on Dec 22, 2004
 */
package net.medcommons.router.services.wado;

import org.apache.log4j.Logger;

/**
 * EncodeJob represents a job to be undertaken by the EncoderManager.
 * 
 * @author ssadedin
 */
public abstract class EncodeJob implements Comparable {
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(EncodeJob.class);

  /**
   * Priority value indicating a high priority low
   */
  public static final int PRIORITY_HIGH = 10;

  /**
   * Priority value indicating a medium priority 
   */
  public static final int PRIORITY_MED = 5;
  
  /**
   * Priority value indicating a low priority 
   */
  public static final int PRIORITY_LOW = 0;
  
  /**
   * The next Job Id to be assigned.
   */
  public static int nextId = 0;
  
  /**
   * The priority of this EncodeJob
   */
  private int priority = PRIORITY_MED;
  
  /**
   * The time at which this job was queued - or 0 if it has not been enqueued.
   */
  private long enqueueTimeMs = 0;
  
  /**
   * Set to true if this job is terminated.
   */
  private boolean terminated = false;
  
  /**
   * An identifier for the client that asked for the image to be encoded.
   * Allows for operations to be performed based on the client identity
   * (such as cancelling all operations for a particular client).
   * <i>Note: the web interface will place the session id here</i>
   */
  private String clientId = null;
  
  /**
   * The id of this job
   */
  private int id;

  /**
   * Creates an encoding job for use by the EncoderManager
   * 
   * @param clientId
   * @param priority
   * @param wadoImage
   */
  public EncodeJob(String clientId, int priority) {
    super();
    this.clientId = clientId;
    this.priority = priority; 
    synchronized(EncodeJob.class) {
      this.id = nextId++;
    }
  }

  /**
   * Called by the EncoderEngine to perform the encoding operation
   */
  public abstract void encode() throws EncodeException;
  
  /**
   * Called by the EncoderEngine when it wants to terminate a job.
   */
  public abstract void terminate();
  
  /**
   * Responsible for ordering encoding jobs according to their priority
   */
  public int compareTo(Object obj) {
    EncodeJob otherJob = (EncodeJob)obj;    
    if(otherJob.priority != this.priority) 
        return otherJob.priority - this.priority;
     else { // priorities are the same - process in order of age
       return (int)(this.enqueueTimeMs - otherJob.enqueueTimeMs);    
     }
  }
  
  public String toString() {
    return "EncodeJob:" + Integer.toHexString(hashCode()) + "(Id=" + id + ",priority=" + this.priority + ")";
  }

    /**
     * @return Returns the enqueueTimeMs.
     * 
     * @uml.property name="enqueueTimeMs"
     */
    public long getEnqueueTimeMs() {
        return enqueueTimeMs;
    }

    /**
     * @param enqueueTimeMs The enqueueTimeMs to set.
     * 
     * @uml.property name="enqueueTimeMs"
     */
    public void setEnqueueTimeMs(long enqueueTimeMs) {
        this.enqueueTimeMs = enqueueTimeMs;
    }

    /**
     * @return Returns the clientId.
     * 
     * @uml.property name="clientId"
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return Returns the priority.
     * 
     * @uml.property name="priority"
     */
    public int getPriority() {
        return priority;
    }

  
  public long getAge() {
    return System.currentTimeMillis() - this.enqueueTimeMs;
  }

    /**
     * @return Returns the terminated.
     * 
     * @uml.property name="terminated"
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @param terminated The terminated to set.
     * 
     * @uml.property name="terminated"
     */
    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    /**
     * @return Returns the id.
     * 
     * @uml.property name="id"
     */
    public int getId() {
        return id;
    }

}
