/*
 * Created on Aug 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer.web;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.services.transfer.RoutingQueue;
import net.medcommons.router.services.transfer.TransferStatus;
import net.medcommons.router.services.transfer.client.RemoteController;
import net.medcommons.storage.FileUtils;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.log4j.Logger;

/**
 * Performs file upload per RFC 1867 "Form-based File Upload in HTML".
 * 
 * All images are placed into a scratch folder in the Images/Import directory in
 * a temporary folder named
 * <P>
 * Images/Import/scratch_&lt;timestamp&gt;/
 * </P>
 * where the timestamp is the time that the UploadServlet is inved.
 * 
 * After the upload transaction is complete the contents of the folder are moved
 * to
 * <P>
 * Image/Import/&lt;DataGuid&gt;/
 * </P>
 * 
 * Note that multiple upload commands may put data into the same
 * &lt;DataGuid&gt;/ folder.
 * 
 * Perhaps should move this to a Struts framework. The
 * UploadServlet/UploadComplete could manage session information. Or - maybe
 * it's better to have these be separate since the UploadServlet/UploadComplete
 * might need to be managed over reboots of client and server.
 */
public class UploadServlet extends HttpServlet {

  private Logger log = Logger.getLogger(UploadServlet.class);

  File rootDir = null;
  
  

  public void init() {
    try {
     

      System.setProperty("org.apache.commons.logging.Log",
          "org.apache.commons.logging.impl.SimpleLog");
      System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
          "true");
      System.setProperty(
          "org.apache.commons.logging.simplelog.log.httpclient.wire", "warn");
      System
          .setProperty(
              "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
              "warn");

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response); // Need to put DoS limits.
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    boolean isMultipart = FileUpload.isMultipartContent(request);
    String guid = null;
    File guidDirectory = null;
    File scratchDir = null;
    String requestID = null;
    RoutingQueue rq = null;
    long totalBytesTransferred = 0;
    int totalFilesTransferred = 0;
    UpdateRoutingStatus updateRoutingStatus = null;
    RemoteController remoteController = RemoteController.getRemoteController();
    if (remoteController == null)
      throw new NullPointerException("RemoteController not initialized");
    //			Create a new file upload handler
    DiskFileUpload upload = new DiskFileUpload();
    TrackingFileItemFactory fileItemFactory = new TrackingFileItemFactory();
    upload.setFileItemFactory(fileItemFactory);
    requestID = request.getParameter("requestID");
    long totalBytes = -1;
    try{totalBytes = Long.parseLong(request.getParameter("totalBytes"));}
    catch(Exception e){log.error("Error parsing number of bytes from request header");}
    
    int totalImages = -1;
    try{totalImages = Integer.parseInt(request.getParameter("totalImages"));}
    catch(Exception e){log.error("Error parsing number of images from request header");}
    
    log.error("===Request id from header  is " + requestID);
    log.error("===totalImages from header is " + totalImages);
    log.error("===totalBYtes from header is " + totalBytes);
    updateRoutingStatus = new UpdateRoutingStatus(fileItemFactory);
    updateRoutingStatus.totalImages = totalImages;
    updateRoutingStatus.totalBytes = totalBytes;
    rq = remoteController.getRoutingQueueWithID(requestID);
    rq.setBytesTotal(totalBytes);
    updateRoutingStatus.rq = rq;
    
    try {
      Thread t = new Thread(updateRoutingStatus);
      t.start();
      log.info("got upload request");
      //			Parse the request
      int maxMemorySize = 1024 * 64;
      int maxRequestSize = 1024 * 1024 * 500; // 500MB per series limit.
      log.info("maxRequestSize is " + maxRequestSize);

      File importDir = new File(rootDir, "Import");
      scratchDir = new File(importDir, "scratch_"
          + Long.toString(System.currentTimeMillis()));
      importDir.mkdir();
      scratchDir.mkdir();
      log.info("About to invoke parseRequest");
      List /* FileItem */
      items = upload.parseRequest(request, maxMemorySize, maxRequestSize,
          scratchDir.getAbsolutePath());
      log.info("Past parseRequest");
      //			Process the uploaded items
      Iterator iter = items.iterator();
      // First extract parameters
      int count = 0;
      while (iter.hasNext()) {
        count++;
        // This loop happens very fast - appears that the
        // entire thing has already arrived.
        FileItem item = (FileItem) iter.next();

        if (item.isFormField()) {
          String name = item.getFieldName();
          String value = item.getString();
          if (name.equals("mcGuid")) {
            guid = value;
            guidDirectory = FileUtils.resolveGUIDAddress(guid);
            //if (guidDirectory.exists())
            //  throw new
            // DataAlreadyExistsException(guidDirectory.getAbsoluteFile().toString());
            guidDirectory.mkdirs();
          }
          else if (name.equals("requestID")){
            requestID = value;
            //log.info("requestID=" + requestID);
            
            
          }

          //log.info("name=" + name);
          //log.info("value=" + value);
        }
      }
      log.info("Past initial iteration of " + count + "items");
      // Save the files
      iter = items.iterator();
      while (iter.hasNext()) {
        FileItem item = (FileItem) iter.next();

        if (!item.isFormField()) {

          String fieldName = item.getFieldName();
          String fileName = item.getName();
          String contentType = item.getContentType();
          boolean isInMemory = item.isInMemory();
          long sizeInBytes = item.getSize();
          totalBytesTransferred+=sizeInBytes;
          totalFilesTransferred++;
          //log.info("About to save " + fileName + " byte = " + sizeInBytes);
          
          File uploadedFile = new File(guidDirectory, fileName);
          item.write(uploadedFile);
          //rq.setBytesTransferred(totalBytesTransferred);
          //remoteController.updateRoutingQueue(rq);
          

        }
      }
      log.info("Past write of " + count + "items");
      //log.info("upload completed for DataGuid " + guid);
      
    } catch (DataAlreadyExistsException e) {
      log.error("Ignoring duplicate data:" + e.toString());
      response.setStatus(TransferStatus.DUPLICATE);
    
    } catch (Exception e) {
      e.printStackTrace();
      guidDirectory.delete();
      response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE, e.toString());
      rq.setGlobalStatus("ERROR");
      remoteController.updateRoutingQueue(rq);
     
    } finally {
      updateRoutingStatus.completed = true;
      updateRoutingStatus = null;
      boolean success = scratchDir.delete();
      if (!success)
        log.error("Can't delete directory " + scratchDir.getAbsolutePath());
    }

  }
  
  private class TrackingFileItemFactory extends DefaultFileItemFactory{
    int itemCount = 0;
    public FileItem createItem(
        String fieldName, 
        String contentType, 
        boolean isFormField, 
        String fileName) {
      FileItem item = super.createItem(fieldName, contentType, isFormField, fileName);
      if (fileName!= null){
       //log.info("creating FileItem for " + fileName + " " + itemCount);
       itemCount++;
      }else
        ;//log.info("not a file: " + fieldName + " contentType=" + contentType + " is form field= " + isFormField);
      
      return(item);
    }
  }
  /**
   * Updates routing queue tables with estimate of number of bytes transferred.
   * 
   * The estimate should be reasonable as long as the size of elements in the transfer
   * are similar. The number of images and the total byte size of the transfer
   * are known. The FileUpload API only lets us detect the number of images 
   * transferred. So, the proportion of images that are done are multiplied by
   * the total bytes size to get an estimate of how many images have been tranferred.
   * 
   * This estimate is weak for two reasons:
   * <ol>
   * <li> The size of the images need not be uniform.
   * <li> If the images are large the updates don't occur frequently; this will
   *      appear broken to the user.
   * </ol>
   * However - it is sufficient for the current implementation. The byte counts are 
   * used here only to show transfer progress to a user and are not used to ensure 
   * system or data integrity.
   * @author sean
   *
   */
  private class UpdateRoutingStatus implements Runnable{
    boolean completed = false;
    long timeOut =  3000; // 3 seconds
    TrackingFileItemFactory itemFactory = null;
    int totalImages = -1;
    long totalBytes = -1;
    RoutingQueue rq = null;
    RemoteController remoteController = RemoteController.getRemoteController();
    public UpdateRoutingStatus(TrackingFileItemFactory itemFactory){
      this.itemFactory = itemFactory;
    }
    /**
     * Periodically updates the routing queue entry with an estimate of the
     * number of bytes transferred. Thread exits when any of the following 
     * conditions occurs:
     * <ol>
     * <li> number of items completed equals the total number in the 
     * transfer.
     * <li> an error occurs while transmitting this state to the pink box.
     * <li> The calling doPost() method exits - either from completion or due to 
     * an error.
     * </ol>
     *  //TODO add test for bytes not changing.
     */
    public void run(){
      while(!completed){
        try{
          Thread.sleep(timeOut);
          float done = ((1.0f) * itemFactory.itemCount)/totalImages;
          long approxBytesCompleted = (long)( done * totalBytes);
          rq.setBytesTransferred(approxBytesCompleted);
          remoteController.updateRoutingQueue(rq);
          if (totalImages == itemFactory.itemCount)
            completed=true;
          
          
        }
        catch(Exception e){
          e.printStackTrace();
          completed=true;
        }
      }
      try{
        // Test on exit if the file transfer is complete.
        // If so, mark the images done. In all other cases
        // do not update the state of the RoutingQueue item -
        // it will be sent from elsewhere.
        if (totalImages == itemFactory.itemCount){
          rq.setBytesTransferred(totalBytes);
          rq.setGlobalStatus("DONE");
          remoteController.updateRoutingQueue(rq);
        }
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  

}