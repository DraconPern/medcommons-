/*
 * $Id: $
 * Created on Dec 22, 2004
 */
package net.medcommons.router.services.wado;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;
import net.medcommons.router.services.dicom.util.MCSeries;

import org.apache.log4j.Logger;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/** * WADOImageJob2 is a kind of EncodeJob that encodes a WADO image to an output stream
 * using dcm4che2 pipeline. *  * @author ssadedin */
public class WADOImageJob2 extends EncodeJob {
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(WADOImageJob2.class);  

  /**
   * Font to be used - created and cached for life of server
   */
  private static Font font = new Font(WADOConstants.DEFAULT_FONT, Font.BOLD, 18);

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
    
    private MCSeries series = null;

  
  /**
   * The input stream containing the DICOM image.
   */
  private InputStream inputStream = null;
  
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
  public WADOImageJob2(
      String clientId, 
      int priority,
      WADOParameterForm params,
      HttpServletResponse response,
      OutputStream out,
      InputStream inputStream,
      MCSeries series
      ) 
  {
    super(clientId, priority);
    
    if(WADOImageJob2.countBytes == null) {
        WADOImageJob2.countBytes = 
          Boolean.valueOf(Configuration.getProperty("CountEncodedImageBytes", false));
          log.info("CountEncodedImageBytes: "  + countBytes);
    }
    
    this.inputStream = inputStream;
    this.response = response;
    this.out = 
      countBytes.booleanValue() ? new ByteCounterOutputStream(out) : out;
    this.params = params;
    
    for (int i = 0; i < time.length; i++) {
      time[i] = 0;
      timeStr[i] = "Uninitialized";
    }
    
    this.series = series;
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
    
    WADOImage2 wadoImage = null;

    try {
       
      
      if (!params.isWindowLevelGrid()) {
          if(params.isThumbStrip()) {
              log.info("Rendering thumbstrip");
              wadoImage = new WADOThumbStripImage(
                      inputStream, 
                      params.getFrameNumber(), 
                      params.getColumns(), 
                      params.getRows(), 
                      series);
          }
          else 
              wadoImage = new WADOImage2(
                      inputStream, 
                      params.getFrameNumber(), 
                      params.getColumns(), 
                      params.getRows());      } 
      else {
          
        wadoImage = new WADOWindowLevelGridImage(
            inputStream, 
            params.getFrameNumber(), 
            params.getColumns(), 
            params.getRows(), 
            params.getWidths(), 
            params.getLevels());
      } 
   
      wadoImage.createImage(params);
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
      // Also ignored for frame sprite case because the zoom is taken care of in the 
      // initial rendering of the multiframe sprite
      if (params.isRegionSpecified() && (!params.isWindowLevelGrid()) && !params.isFrameSprite()) {
        wadoImage.zoomToRegion(params.getMaxColumns(), params.getMaxRows(),
            params.getTopLeftX(), params.getTopLeftY(), params
                .getBottomRightX(), params.getBottomRightY());
      }

      if (params.isScalingRequired()) {
        int maxRows = params.getMaxRows();
        wadoImage.scaleImage(params.getInterpolationType(), params.getMaxColumns(),
                maxRows, params.getPhotometricInterpretation(),
                params.isFrameSprite());
      }
      if (timeActive) {
        calculateTime(3, TIMESTR_SCALE); 
      }

      if(!params.isFrameSprite()) {
	      wadoImage.renderTextOverlays(font, params.isPatientAnnotation(), params
	          .isLocalizerAnnotation());
	      if (timeActive) {
	        calculateTime(4, TIMESTR_TEXT);
	      }
      }
      
      Map<String, String> meta = wadoImage.getOutputMetaData();
      if(meta != null) {
          for(String key : meta.keySet()) {
              Cookie cookie = new Cookie(key, meta.get(key));
              cookie.setPath("/router/");
              cookie.setMaxAge(120);
              response.addCookie(cookie);  
          }
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
        log.info("Exception type " + t.getClass().getName() + " with cause " + t.getCause() + " occurred", t);
        if(t instanceof IOException) {
          throw (IOException)t; // throw to outer try/catch
        }
      }
    }
    
    catch (IOException e) {
      throw new EncodeException(
          "Unable to decode image  with params " + 
          params.toString() + "\n Possible decryption error", e);
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

  /**
   * JPEG image encoding the old fashioned way. Not that speedy - but it beats
   * out the new ImageIO methods.
   */
  private void jpegEncode(BufferedImage outputImage, OutputStream out,
      HttpServletResponse response, int imageQuality) throws IOException {
    try {
      if (response != null) {
        response.setContentType("image/jpeg");
      }
      response.flushBuffer();
      JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(out);
      JPEGEncodeParam param = enc.getDefaultJPEGEncodeParam(outputImage);
      if (imageQuality != 100)
        param.setQuality((float) (imageQuality / 100.0f), true);
      enc.setJPEGEncodeParam(param);

      enc.encode(outputImage);
    } 
    finally {
      out.close();
    }
  }

  /**
   * JPEG encoding the ImageIO way. Slow as a dog.
   * 
   * @author sean
   */
  private void jpegEncodenew(BufferedImage outputImage, OutputStream out,
      HttpServletResponse response, int imageQuality) throws IOException {
    try {
      if (response != null) {
        response.setContentType("image/jpeg");
      }
      Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
      ImageWriter writer = (ImageWriter) iter.next();
      ImageWriteParam writeParam = writer.getDefaultWriteParam();
      writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
      writer.write(outputImage);
    } 
    finally {
      ;
    }
  }

  /**
   * Generates a PNG output image. Slow as a dog.
   * 
   * @param outputImage
   * @param out
   * @throws IOException
   */
  private void pngEncode(BufferedImage outputImage, OutputStream out,
      HttpServletResponse response) throws IOException {
    try {
      if (response != null) {
        response.setContentType("image/png");
      }
      Iterator iter = ImageIO.getImageWritersByFormatName("png");
      ImageWriter writer = (ImageWriter) iter.next();
      ImageWriteParam writeParam = writer.getDefaultWriteParam();
      writer.setOutput(ImageIO.createImageOutputStream(response
          .getOutputStream()));
      writer.write(outputImage);
    } 
    finally {
      ;
    }
  }

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
