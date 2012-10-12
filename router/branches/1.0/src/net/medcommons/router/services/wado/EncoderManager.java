/*
 * $Id: $
 * Created on Dec 22, 2004
 */
package net.medcommons.router.services.wado;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;

import org.apache.commons.collections.BinaryHeap;
import org.apache.commons.collections.Buffer;
import org.apache.log4j.Logger;

/** * EncoderManager manages requests to encode images.   The underlying problem * is that encoding images requires significant resources - both CPU and memory.  * Therefore allowing clients to perform unlimited numbers of these operations * can cause problems for the server.   Additional to this, there are different * reasons for encoding which may have different priorities.   We would like to * therefore ensure that the high priority encodings are performed as quickly * as possible while lower priority ones (eg. ones that a user is not waiting for) * get done after the high priority ones.   Finally, there are also times when * encodings might be cancelled - for example, the user switches to a whole * different set of images - encodings waiting in the queue can then be cancelled. *  * @author ssadedin */
public class EncoderManager {
  
  private static final int MAX_ACTIVE_JOBS_DEFAULT = 4;
    
  /**
   * Default Maximum amount of time any job is allowed to stay queued - after this time
   * the job will be forcibly terminated.
   */
  private static final long MAX_JOBS_AGE_MS = 60000;
  
  /**
   * flag to indicate whether initialized
   */
  private static boolean initialized = false;
  
  /**
   * The number of allowed active jobs
   * Note:  this is read from the maxActiveEncodingJobs configuration 
   * property at runtime.
   */
  private static int maxActiveJobs = MAX_ACTIVE_JOBS_DEFAULT;
  
  
  
  /**
   * Maximum amount of time any job is allowed to stay queued - after this time
   * the job will be forcibly terminated.
   */
  private static long maxJobAge = MAX_JOBS_AGE_MS;
  
  /**
   * Whether or not job queueing will be enabled.  If this is set to
   * false, maxActiveJobs is ignored.
   */
  private static boolean enableJobThrottling = true;
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(EncoderManager.class);
  
  /**
   * The queue of EncodingJob objects waiting to be run
   */
  private Buffer waitingJobs = new BinaryHeap();
  
  /**
   * Jobs that are running
   */
  private Set activeJobs = new HashSet();

    /**
     * The one and only EncoderManager
     * 
     * @uml.property name="instance"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private static EncoderManager instance = new EncoderManager();

  
  /**
   * Enqueues this job to run.  The job will wait in the queue until
   * its turn to run according to its parameters (priority etc.).
   *  
   * @param job
   * @throws EncodeException
   */
  public void encode(EncodeJob job) throws EncodeException {

    job.setEnqueueTimeMs(System.currentTimeMillis());
    
    // A slightly inefficient but simple method of 
    // limiting the number of allowed jobs and when the maximum is taken,
    // executing them in order
    synchronized(waitingJobs) {
      // Check to see if this job can run ...
      if(activeJobs.size() >= maxActiveJobs) {  // Too many active jobs
        
        // Enqueue the job
        waitingJobs.add(job);        
        
        if(log.isDebugEnabled())
          log.info("Encoding job limit " 
              + maxActiveJobs + " exceeded: " + activeJobs.size() + " active, " + waitingJobs.size() + " waiting");
          
        // Wait in loop until we are the next job scheduled to run
        while(true) {
          try {
            // Wait for someone to exit the queue.  When a job exits, waitingJobs.notify() 
            // will be called to break us out from the wait.
            waitingJobs.wait();            
          } 
          catch (InterruptedException e) {
            log.warn("Interrupted while waiting for encoding job " + job + " to execute.  Job will run immediately");
          }
          
          if(job.isTerminated()) {
            log.info("Job " + job + " returning without executing (terminated).");
            this.waitingJobs.remove(job);
            return;
          }
          
          // Check if we are next in line to run
          if((activeJobs.size() < maxActiveJobs) && (this.waitingJobs.get() == job)) {
            log.info("Waiting job " + job + " allowed to run");
            this.waitingJobs.remove();
            break;
          }
          else {
            if(job.getAge() > this.maxJobAge) {
              terminate(job);
            }
          }
        }
      }
      else {
        log.info("Job " + job + " scheduled immediately");
      }
      
      // Our job has been allowed to run!
      activeJobs.add(job);
    }

    try {
      job.encode();
    }
    finally { // Make sure that no matter what, we notify the pool we are done and clear the active job   
      synchronized(waitingJobs) {
        int oldSize = activeJobs.size();
        activeJobs.remove(job);
        log.info("Removed job " + job + " from active pool, active reduced from " + oldSize + " to " + activeJobs.size());
        waitingJobs.notifyAll();
      }    
    }
  }
  
  /**
   * @param job
   */
  public void terminate(EncodeJob job) { 
    try {
      job.terminate();
    }
    catch(Throwable t) {
      log.warn("Error occurred while terminating job " + job.toString(), t);
    }
    job.setTerminated(true);
  }

    /**
     * Returns a list containing the current active jobs
     * @return
     * 
     * @uml.property name="activeJobs"
     */
    public List getActiveJobs() {
        synchronized (this.waitingJobs) {
            return new ArrayList(this.activeJobs);
        }
    }

    /**
     * Returns a list containing the current active jobs
     * @return
     * 
     * @uml.property name="waitingJobs"
     */
    public Buffer getWaitingJobs() {
        Buffer result = new BinaryHeap();
        synchronized (this.waitingJobs) {
            result.addAll(this.waitingJobs);
        }
        return result;
    }

    /**
     * Singleton accessor method.
     * 
     * @return Returns the instance.
     * 
     * @uml.property name="instance"
     */
    public static EncoderManager getInstance() {
        if (!EncoderManager.initialized) {
            maxActiveJobs = Configuration.getProperty("maxActiveEncodingJobs", MAX_ACTIVE_JOBS_DEFAULT);

            enableJobThrottling = Configuration.getProperty("enableEncodingJobThrottling", true);

            maxJobAge = Configuration.getProperty("maxEncodingJobAge", (int) MAX_JOBS_AGE_MS);

            initialized = true;
        }
        return instance;
    }

  
}
