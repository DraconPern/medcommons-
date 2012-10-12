/*
 * Created on Aug 30, 2004
 *
 * Simple, generic file upload program.
 * 
 * Hard-coded credentials (username "purple01", password "medcommons"). On the
 * good side - the purple box is now somewhat locked down (in a toy way).
 * 
 */
package net.medcommons.router.services.transfer.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.medcommons.router.services.transfer.TransferStatus;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.log4j.Logger;

/**
 * Implements HTTP peer-to-peer transactions between purple boxes.
 * 
 * <ol>
 * <li>Need to put in a better security system. Current hardcoded
 * username/password combinations will not scale.
 * </ol>
 * 
 * The current system has been tested only on static data sets 
 * (series are finished before they are passed to this class) but
 * the basic model may support data sets that are being added to as
 * it is being transmitted.
 * 
 * Duplicate detection not extensively tested.
 * Need to update routing queue entry for bytes transmitted, elapsed
 * time.
 * 
 * @author sean
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments To do:
 * 
 * Make this Runnable. Now remove the runnable part. The threading happens on a
 * higher level. Add new items to list. Update on # bytes sent; #bytes total.
 * used by both image and metadata upload.
 *  
 */
public class UploadFileList {

  private String targetURL = null;

  ArrayList fileList = new ArrayList();

  ArrayList parameterList = new ArrayList();

  private Logger log = Logger.getLogger(UploadFileList.class);

  private String mcGuid = null;

  private boolean done = false;

  private long nBytesSent = 0;

  private int nFilesSent = 0;

  private long nBytesTotal = 0;

  private int nFilesTotal = 0;
  
  private long timeStarted = 0;
  
  private long timeCompleted = 0;

  /**
   *  The target URL defines the servlet that the file upload
   * is sent to.
   */
  public UploadFileList(String mcGuid) {
    this.mcGuid = mcGuid;
  }

  public void setTargetURL(String targetURL){
    this.targetURL = targetURL;
    //log.info("targetURL is " + targetURL);
  }
   /**
    * Returns a time counter in milliseconds of when the transfer started.
    * @return
    */
   public long getTimeStarted(){
     return(timeStarted);
   }
   
   /**
    * Returns a time counter in milliseconds of when the transfer completed.
    * @return
    */
   public long getTimeCompleted(){
     return(timeCompleted);
   }
   public long getNBytesSent(){
     return(nBytesSent);
   }
   public long getNBytesTotal(){
     return(nBytesTotal);
   }
  /**
   * Returns the user name for HTTP credentials on remote purple box.
   * 
   * @return
   */
  private String getUserForTransaction() {
    return ("purple01");
  }

  /**
   * Returns the password for HTTP credentials on remote purple box.
   * 
   * @return
   */
  private String getPasswordForTransaction() {
    return ("medcommons");
  }

  /**
   * In the future this will permit files to be added to the upload as earlier
   * files are in the process of being uploaded; this reduces latency.
   *  
   */
  public int transferFiles() {
    //while(!done){
    int status = -1;
    timeStarted = System.currentTimeMillis();
    try {
      //wait();
      if (fileAvailableForUpload()) {
        // Transfer file
        // update file counter
        List nextImages = getFiles();
        status = uploadFiles(nextImages);

      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    /*
     * Note: the *Total variables are currently used for the calculation of
     * rates; in the future the *Sent variables should be used instead.
     */
    timeCompleted = System.currentTimeMillis();
    long totalTime = timeCompleted - timeStarted;
    float time = (totalTime) / 1000.0f;
    float mbPerSecond = (nBytesTotal / (1024.0f * 1024.0f)) / time;

    log.info("Time = " + time + " seconds, transfer = " + mbPerSecond
        + " MB/second, " + nFilesTotal + " files ");
    return (status);

  }

  public long getTotalBytes() {
    return (nBytesTotal);
  }

  public int getNFiles() {
    return (nFilesTotal);
  }

  /**
   * Returns true if there are more files to send.
   * 
   * @return
   */
  public boolean fileAvailableForUpload() {
    return (nFilesTotal > nFilesSent);
  }

  private class parameter {
    String name = null;

    String value = null;

    parameter(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  /**
   * Adds a parameter to the file upload. These scope of these parameters should
   * make sense on the entire upload (e.g., these include items like the order
   * guid but not parameters on individual files);
   * 
   * @param name
   * @param value
   */
  public void addParameter(String name, String value) {
    parameterList.add(new parameter(name, value));
  }

  public/* synchronized */
  boolean addFileToList(File f) {
    boolean added = false;
    if (!done) {
      fileList.add(f);
      nFilesTotal++;
      nBytesTotal += f.length();
      added = true;
    }
    //notify();
    return (added);
  }

  public void stop() {
    done = true;
    //notify();
  }

  private/* synchronized */
  List getFiles() {
    List subList = fileList.subList(nFilesSent, fileList.size());
    return (subList);

  }

  /**
   * Uploads a list of files in single HTTP connection.
   * 
   * Each file has a counter (useful for debugging) and the guid of the series
   * object passed in. In the future we may pass the hash code too.
   * 
   * @param l
   */
  private int uploadFiles(List l) {

    MultipartPostMethod filePost = new MultipartPostMethod(targetURL);
    
    int status = -1;
    long byteCounter = 0;

    // Put the parameters on the list
    for (int i = 0; i < parameterList.size(); i++) {
      parameter p = (parameter) parameterList.get(i);
      filePost.addParameter(p.name, p.value);
    }
    try {
      filePost.addParameter("mcGuid", mcGuid);
      for (int i = 0; i < l.size(); i++) {
        File f = (File) l.get(i);
        filePost.addParameter("mcGuid", mcGuid);
        filePost.addParameter(f.getName(), f);
        filePost.addParameter("counter", Integer.toString(i));
        //.info("Added file " + f.getName());
        byteCounter += f.length();

      }

      HttpClient client = new HttpClient();
      client.setConnectionTimeout(5000);
      client.getState().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(
          getUserForTransaction(), getPasswordForTransaction());
      client.getState().setCredentials(null, null, defaultcreds);
      filePost.setDoAuthentication(true);
      //log.info("Started execute method");
      status = client.executeMethod(filePost);
      //log.info("Ended execute method");
      if (status == TransferStatus.OK) {
        nFilesSent += l.size();
        nBytesSent += byteCounter;
      } else if (status == TransferStatus.DUPLICATE) {
        // Not an error; data already exists on the other site.
        log.info("Duplicate data was not re-transmitted for guid: " + mcGuid);
      } else {
        log
            .error("Upload failed, response="
                + HttpStatus.getStatusText(status));
        
      }

    } catch (Exception ex) {
      log.error("Error: " + ex.getMessage());
      ex.printStackTrace();
    } finally {
      filePost.releaseConnection();
    }
    return (status);
  }

}