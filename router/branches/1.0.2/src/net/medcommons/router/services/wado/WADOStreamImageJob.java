package net.medcommons.router.services.wado;


/*
 * $Id: $
 * Created on Dec 22, 2004
 */

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.RepositoryConstants;
import net.medcommons.modules.itk.ITKCacheManager;
import net.medcommons.modules.itk.ImageTransformDimensions;
import net.medcommons.modules.itk.RescaleWindowCenter;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;

import org.apache.log4j.Logger;


/**
 * WADOImageJob is a kind of EncodeJob that encodes a WADO image to an output stream.
 * 
 * @author ssadedin
 */
public class WADOStreamImageJob extends EncodeJob implements RepositoryConstants{
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(WADOStreamImageJob.class);  

  private static Object synchObject = new Object();
  /**
   * Font to be used - created and cached for life of server
   */
  private static Font font = new Font(WADOConstants.DEFAULT_FONT, Font.BOLD, 24);

  final static DecimalFormat format = new DecimalFormat();
  
  static {
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);
    Metric.register("imageBytesPerSecond", new TimeSampledMetric(Metric.getInstance("bytesEncoded"), 500, 10));
  }

    /**
     * The parameters for the encoding operation
     * 
     * @uml.property name="params"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private WADOParameterForm params = null;

  
  /**
   * The input stream containing the DICOM image.
   */
  private File inputFile = null;
  
  /**
   * The response to which the encoded image should be sent (if any)
   * Note:  this is provided separately to the output stream so that
   * unit tests and performance tests can pass a non-http output stream
   */
  private HttpServletResponse response = null;  

  /**
   * The output stream to which the images should be encoded. 
   */
  private OutputStream out;

    /**
     * Timer used to time operations
     * 
     * @uml.property name="timer"
     * @uml.associationEnd multiplicity="(0 1)"
     */
   // private ITimer timer = null;

  
  /**
   * Set to true if this Job will count the number of encoded bytes
   */
  private static Boolean countBytes = null;
    
  /**
   * Used for measuring performance TODO: move to more general performance
   * framework
   */
  double time[] = new double[6];
  String timeStr[] = new String[6];
  boolean timeActive = false;
  
  // Make all the TIMESTR_* strings the same length so they 
  // are formatted out appropriately.
  private final static String TIMESTR_INIT   = "Initialized :";
  private final static String TIMESTR_FILE   = "File Found  :";
  private final static String TIMESTR_WL     = "Window/Level:";
  private final static String TIMESTR_SCALE  = "Scaling     :";
  private final static String TIMESTR_TEXT   = "Text Render :";
  private final static String TIMESTR_ENCODE = "Image Encode:";
  
	/**
	 * Utility class that wraps an OutputStream and counts the bytes being written
   * through it.
	 */
  private static  class ByteCounterOutputStream extends OutputStream {
    
    /**
     * Number of bytes written by this stream
     */
    public long count = 0;
    
    /**
     * The stream to which bytes will be passed through
     */
    private OutputStream wrappedStream;
    
    /**
     * Creates a ByteCounterOutputStream to count bytes for the given wrappedStream
     */
    public ByteCounterOutputStream(OutputStream targetStream) {
      super();
      this.wrappedStream = targetStream;
    }
    
    /**
     * Write and count the characters
     */
    public void write(int b) throws IOException {
      ++count;
      this.wrappedStream.write(b);
    }    
    
    /**
     * Write and count the characters
     */
    public void write(byte[] b, int off, int len) throws IOException {
      count += len;
      this.wrappedStream.write(b, off, len);
    }
    
    /**
     * Write and count the characters
     */
    public void write(byte[] b) throws IOException {
      count += b.length;
      this.wrappedStream.write(b);
    }
  }
    
  /**
   * Creates a WADOImageJob for use by the EncoderManager
   * 
   * @param clientId
   * @param priority
   * @param wadoImage
   */
  public WADOStreamImageJob(
      String clientId, 
      int priority,
      WADOParameterForm params,
      HttpServletResponse response,
      OutputStream out,
      File inputFile
      ) 
  {
    super(clientId, priority);
    
    if(WADOStreamImageJob.countBytes == null) {
    	WADOStreamImageJob.countBytes = 
          Boolean.valueOf(Configuration.getProperty("CountEncodedImageBytes", false));
          log.info("CountEncodedImageBytes: "  + countBytes);
    }
    
    this.inputFile = inputFile;
    this.response = response;
    this.out = 
      countBytes.booleanValue() ? new ByteCounterOutputStream(out) : out;
    this.params = params;
    
    for (int i = 0; i < time.length; i++) {
      time[i] = 0;
      timeStr[i] = "Uninitialized";
    }
    /*
    if (timeActive) {
      timer = TimerFactory.newTimer();
      timer.reset();
      timer.start();
      calculateTime(0, TIMESTR_INIT);
    }     
    */  
  }

  /**
   * Encodes the DICOM image as a JPEG stream.
   * 
   * Several transforms are performed during this process:
   * <ul>
   * <li>Images are mapped to 8 bit values using LUT.
   * <li>Images are rescaled
   * <li><i>Future </i> Image subregions
   * </ul>
   * 
   * @param uParams
   * @param out
   * @param lut
   * @throws IOException
   */
  public void encode() throws EncodeException {
      String path=null;
    try{
        long imageTime = System.currentTimeMillis();
        long imageWriteComplete=imageTime;
        byte buffer[] = new byte[32*1024];
        boolean thumbnail = false;
        
        
        File scratchDir = ITKCacheManager.getCacheDirectory();
        if (!scratchDir.exists()){
            throw new FileNotFoundException(scratchDir.getAbsolutePath());
        
        }
        
        RescaleWindowCenter rescale = new RescaleWindowCenter(scratchDir);
		
		
        // What is going on here with the two sets of parameters?
        // appears one is being used for thumbnails, the other big images.
       int rows = params.getMaxRows();
       int cols = params.getMaxColumns();
       if (rows < 0){
               rows = params.getRows();
               cols = params.getColumns();
       }
       int maxHeight = rows;
		int maxWidth = cols;
		int window = params.getWindowWidth();
		int level = params.getWindowCenter();
		log.info("window width = " + window);
		log.info("window level = " + level);
		ImageTransformDimensions imageParameters = new ImageTransformDimensions();
		fillInImageParameters(params, imageParameters);
		
		File f = null;
		if ( (maxHeight == 140) && (maxWidth == 140)){
			String thumbnailName = inputFile.getAbsolutePath() + THUMBNAIL_SUFFIX;
			File thumbnailFile = new File (thumbnailName);
			if (thumbnailFile.exists()){
				thumbnail = true;
				f = thumbnailFile;
				log.info("Using thumbnail:" + thumbnailFile.getAbsolutePath());
			}
			else{
				log.info("thumbnail does not exist:" + thumbnailFile.getAbsolutePath());
			}
		}
		if (!thumbnail){
		//synchronized (synchObject){
			f = rescale.generateJPEG(inputFile, imageParameters);
		//}
		}
		//transformDicomImage.resizeImage(scratchImage.getAbsolutePath(), scratchJPEG.getAbsolutePath(), min, max,rows, cols );
		if (f == null){
			throw new IOException("Failed to generate JPEG file from ITK with source DICOM " + inputFile.getAbsolutePath());
		}
        response.setStatus(HttpServletResponse.SC_OK);
        FileInputStream jpegIn = new FileInputStream(f);
        BufferedInputStream buffIn = new BufferedInputStream(jpegIn);
        response.setContentType("image/jpeg");
        int offset = 0;
        int n;
        while ((n = buffIn.read(buffer)) != -1){
            out.write(buffer,0, n);
            offset+=n;
        }
        long imageResizeComplete = System.currentTimeMillis();
        
        log.info("Read file " + f.getAbsolutePath());
        log.info("Image generation " + (imageResizeComplete - imageWriteComplete) + "msec");
        if (!thumbnail){
        boolean isDeleted = f.delete();
	        if (!isDeleted){
	        	log.error("JPEG cache file not deleted: " + f.getAbsolutePath());
	        	
	        }
        }
        
        inputFile.setLastModified(System.currentTimeMillis());
       // }
}
catch (IOException e) {
 
  log.error("Encoding error", e);
 // try { path = imageFile.getCanonicalPath(); } catch (IOException
            // e1) { }
  throw new EncodeException(
      "Unable to encode image " + path + " with params " + params.toString(), e);
}

catch(Exception e){
    log.error("Encoding error", e);

          throw new EncodeException(
              "Unable to encode image " + path + " with params " + params.toString(), e);
        
}
finally {
  
  if (timeActive) {

    calculateTime(5, TIMESTR_ENCODE);

    double totalTime = 0.0;
    StringBuffer buff = new StringBuffer();
    for (int i = 0; i < time.length; i++) {
      long delta = 0;
      totalTime += time[i];
      buff.append("\n");
      buff.append(timeStr[i]);
      buff.append(":");
      buff.append(format.format(time[i]));
      buff.append("msec");
      if (i!= 0){
        buff.append("\t\t cummulative:");
        buff.append(format.format(totalTime));
      }
    }
    log.info(buff.toString());
  }      
  Metric.addSample("imagesEncoded");
  if(countBytes.booleanValue()) {
    Metric.addSample("bytesEncoded", new Long(((ByteCounterOutputStream)this.out).count));
  }
}
}

private void fillInImageParameters(WADOParameterForm params, ImageTransformDimensions imageParams){
	imageParams.setWindow(params.getWindow());
	imageParams.setLevel(params.getLevel());
	// What is going on here with the two sets of parameters?
    // appears one is being used for thumbnails, the other big images.
	   int rows = params.getMaxRows();
	   int cols = params.getMaxColumns();
	   if (rows < 0){
       rows = params.getRows();
       cols = params.getColumns();
	}
	int maxHeight = rows;
	int maxWidth = cols;
	imageParams.setOutputMaxHeight(maxHeight);
	imageParams.setOutputMaxWidth(maxWidth);

   if (params.isRegionSpecified()){
	   log.info("Region specified");
	   log.info("Top Left X:" + params.getTopLeftX());
	   log.info("Top Left Y:" + params.getTopLeftY());
	   log.info("Bottom right X:" + params.getBottomRightX());
	   log.info("Bottom right Y:" + params.getBottomRightY());
	  
			  
	   imageParams.setInputSubregionSpecified(true);
	   imageParams.setInputRegionBottomRightX(params.getBottomRightX());
	   imageParams.setInputRegionBottomRightY(params.getBottomRightY());
	   imageParams.setInputRegionTopLeftX(params.getTopLeftX());
	   imageParams.setInputRegionTopLeftY(params.getTopLeftY());
	   
	   
   }
   else{
	   log.info("No region specified");
	   imageParams.setInputSubregionSpecified(false);
   }
	   
   
	
	
}
/*
  public void encodex() throws EncodeException {
    
    WADOImage wadoImage = null;

    try {
      if (params.getWindowLevelGrid() == null) {
        wadoImage = new WADOImage(
            inputStream, 
            params.getFrameNumber(), 
            params.getColumns(), 
            params.getRows());
      } 
      else {
        wadoImage = new WADOWindowLevelGridImage(
            inputStream, 
            params.getFrameNumber(), 
            params.getColumns(), 
            params.getRows(), 
            params.getWindowGridArray(), 
            params.getLevelGridArray());
      }

      wadoImage.createImage(inputStream, params);
      inputStream.close();
      if (timeActive) {
        calculateTime(2, TIMESTR_WL);
      }
      if (wadoImage.getImage() == null) {
        log.info("Image could not be read from inputstream ");
        return;
      }
      response.setStatus(HttpServletResponse.SC_OK);

      // If a subregion is identified *and* it is not the windowLevelGrid case
      // (which extracts a set of subregions using different logic) then
      // extract an image subrectangle before other transforms are performed.
      if (params.isRegionSpecified() && (params.getWindowLevelGrid() == null)) {
        wadoImage.zoomToRegion(params.getMaxColumns(), params.getMaxRows(),
            params.getTopLeftX(), params.getTopLeftY(), params
                .getBottomRightX(), params.getBottomRightY());
      }

      if (params.isScalingRequired()) {
        wadoImage.scaleImage(params.getInterpolationType(), params
            .getMaxColumns(), params.getMaxRows(), params
            .getPhotometricInterpretation());
      }
      if (timeActive) {
        calculateTime(3, TIMESTR_SCALE);
      }

      wadoImage.renderTextOverlays(font, params.isPatientAnnotation(), params
          .isTechniqueAnnotation());
      if (timeActive) {
        calculateTime(4, TIMESTR_TEXT);
      }

      try {
        if (WADOConstants.CONTENT_TYPE_JPEG.equals(params.getContentType()))
          jpegEncode(wadoImage.getImage(), out, response, params.getImageQuality());
        else if (params.getContentType().equals(WADOConstants.CONTENT_TYPE_PNG))
          pngEncode(wadoImage.getImage(), out, response);
        else
          throw new IllegalArgumentException("Unknown content type:"
              + params.getContentType());
      }
      catch(Throwable t) {
        log.info("Exception type " + t.getClass().getName() + " with cause " + t.getCause() + " occurred");
        if(t instanceof IOException) {
          throw (IOException)t; // throw to outer try/catch
        }
      }
    }
    catch (IOException e) {
      String path = null;
     // try { path = imageFile.getCanonicalPath(); } catch (IOException e1) { }
      throw new EncodeException(
          "Unable to encode image " + path + " with params " + params.toString(), e);
    }
    finally {
      if(wadoImage != null) { // If we arrived via exception then these could be null
        try { wadoImage.close(); } catch (IOException e1) { log.warn("Error closing image", e1); }
        wadoImage.clear();
      }
      if (timeActive) {

        calculateTime(5, TIMESTR_ENCODE);

        double totalTime = 0.0;
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < time.length; i++) {
          long delta = 0;
          totalTime += time[i];
          buff.append("\n");
          buff.append(timeStr[i]);
          buff.append(":");
          buff.append(format.format(time[i]));
          buff.append("msec");
          if (i!= 0){
            buff.append("\t\t cummulative:");
            buff.append(format.format(totalTime));
          }
        }
        log.info(buff.toString());
      }      
      Metric.addSample("imagesEncoded");
      if(countBytes.booleanValue()) {
        Metric.addSample("bytesEncoded", new Long(((ByteCounterOutputStream)this.out).count));
      }
    }
  }
*/
 
  /**
   * Captures the time at the specified index level and places
   * the description in the timeStr array.
   * 
   * Resets the timer to start() on exit.
   * Catches all errors due to illegal state problems.
   * @param index
   * @param tStr
   */
  private void calculateTime(int index, String tStr) {
	  /*
    try {
      timer.stop();
      time[index] = timer.getDuration();
      timeStr[index] = tStr;
    } 
    catch (IllegalStateException e) {
      log.error("Illegal State Exception for " + timeStr + e.toString());
      time[index] = -999.99;
      timeStr[index] = timeStr[index] + " " + e.toString();
    } 
    finally {
      timer.reset();
      timer.start();
    }
    */
  }

  /**
   * Attempts to terminate the job by closing the connection
   */
  public void terminate() {
    try {
      log.info("Terminating job " + this.toString());
      this.response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);  // What to send?
      this.response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, "Request Terminated");
      this.out.close();
    } 
    catch (IOException e) {
      log.warn("Exception while terminating job " + this.toString(), e);
    }
  }
}
