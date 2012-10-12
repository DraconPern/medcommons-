/*
 * $Id: $
 * Created on Aug 28, 2004
 */
package net.medcommons.router.services.wado;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;



/**
 * WADOImage is an in-memory instance of a WADO Image.  It exposes a number of
 * operations which can be performed to window, level and scale an image.
 * 
 * This version uses dcm4che2
 * 
 * Changes needed: have it return a stream, not a image to WADOImageJob.
 * 
 * @author mesozoic
 */
public class WADOImage2 {
  
   
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(WADOImage2.class);
  
  /**
   * The largest size (height) image we will render in the viewer
   */
  public final static int MAX_SPRITE_HEIGHT = 32767;

  /**
   * Formatter used to parse magnification formats
   */
  final static DecimalFormat magFormat = new DecimalFormat("#0.00");
  
  
  /**
   * The height of a single frame of the original image 
   * (prior to scaling etc.)
   */
  private int originalHeight = Integer.MIN_VALUE;

  /**
   * The width of a single frame of the original image 
   * (prior to scaling etc.)
   */
  private int originalWidth = Integer.MIN_VALUE;

  /**
   * The actual width of the output image including any
   * spare / blank area required to fit the rendered image
   * due to aspect ratio mismatch.
   */
  private int outputWidth = Integer.MIN_VALUE;  
 
  /**
   * The actual height of the output image including any
   * spare / blank area required to fit the rendered image
   * due to aspect ratio mismatch.
   */
  private int outputHeight = Integer.MIN_VALUE;
  
  /**
   * Desired number of rows in the image
   */
  private int rows = Integer.MIN_VALUE;
    
  /**
   * Desired number of columns in the image
   */
  private int columns = Integer.MIN_VALUE;
  
  private int numberOfFrames = Integer.MIN_VALUE;
  
  private double scale = 1.0;  
  
  private  DicomObject ds = null;
  
  /**
   * The actual image data represented by this WADOImage
   */
  private BufferedImage image = null;
  
  /**
   * DICOM data set used to extract internal data
   */
  protected DicomMetadata dataset;
  
  /**
   * Internal image reader used to read images
   */
  protected ImageReader reader;
  
  /**
   * Internal image input stream used to read images
   */
  protected ImageInputStream imageInputStream;
  
  
  private String []imageOrientation = null;
  private String []imagePosition = null;
  private String []patientOrientation = null;
  
  final static String ROW = "ROW";
  final static String COL = "COL";
  
  /**
   * Creates a WADO image from the given File
   * <p>
   * The rows and columns are the desired values, however the actual rendered 
   * image size may differ if the aspect ratio of the source image does not match the
   * provided values.  The image will be scaled so that the largest dimension fits 
   * inside the corresponding 'max' rows  - see {@link #scaleImage(int, int, int, String, boolean)}.
   * 
   * @param columns     desired number of columns (width in pixels)
   * @param rows        desired number of rows    (height in pixels)
   * @throws IOException
   */
  public WADOImage2(InputStream inputStream, int frameNumber, int columns, int rows) throws IOException {
    super();
    this.rows = rows;
    this.columns = columns;    
    
    Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
   
    int iterCounter = 0;
    while (iter.hasNext()){
        iterCounter++;
        this.reader =  iter.next();
        // image reader from dcm4che 2.x
        if (this.reader.getClass().getCanonicalName().indexOf("dcm4che2") > 0)
            break;
    }
    
    this.imageInputStream =   ImageIO.createImageInputStream(inputStream);
    
    reader.setInput(imageInputStream, false);

   
    this.originalHeight = reader.getHeight(frameNumber);
    this.originalWidth = reader.getWidth(frameNumber);    
    
    ds = ((DicomStreamMetaData) reader.getStreamMetadata()).getDicomObject();
    patientOrientation = ds.getStrings(Tag.PatientOrientation);
    imageOrientation   = ds.getStrings(Tag.ImageOrientationPatient);
    imagePosition      = ds.getStrings(Tag.ImagePositionPatient);
   
    if (patientOrientation!= null){
        for (int i=0;i<patientOrientation.length;i++){
            log.debug("patient orientation " + i + "= '" + patientOrientation[i] + "'");
        }
    }
    if (imageOrientation!= null){
        for (int i=0;i<imageOrientation.length;i++){
            log.debug("image orientation " + i + "= '" + imageOrientation[i] + "'");
        }
    }
   
    if (imagePosition!= null){
        for (int i=0;i<imagePosition.length;i++){
            log.debug("image position " + i + "= '" + imagePosition[i] + "'");
        }
    }
  }
  
  /**
   * Closes the internal streams used to read images.
   * @throws IOException
   */
  public void close() throws IOException {
  
   this.imageInputStream.close();   
   this.imageInputStream = null;
   this.reader.dispose();
   this.reader = null;
  
  }
  
  public void clear(){
    image = null;
    //dataset.clear();
    //dataset = null;
  }
  
  /**
   * Check if resources are released
   */
  protected void finalize() {
    //log.info("Finalizing image " + super.toString());
    if(imageInputStream != null) {
      log.warn("imageInputStream not cleaned up in finalize()");
    }
  }
  
  /**
   * Renders the overlay text on the output image.
   * 
   * Needs rethinking: there will be lists of items to be rendered. How to represent
   * coordinates? Need something like a layout manager.
   * @param outputImage
   * @param dataset
   * @param imageScale
   */
  public void renderTextOverlays(
    Font font,
    boolean patient,
    boolean localizers) {
    
    
      
    float x, y;
    int imageW = this.image.getWidth();
    int imageH = this.image.getHeight();
    log.debug("Image w=" + imageW + ", image h=" + imageH);
    if ((imageW < 150) && (imageH<150)){
        // It's a thumbnail. Just return.
        return;
    }
    Graphics2D graphics = this.image.createGraphics();
    FontRenderContext frc = graphics.getFontRenderContext();
    TextLayout tl = null;

    int verticalSpacing = 10;
    int horizontalEdgeSpacing = 4;

   
    if (patient) {      
      String patientName= ds.getString(Tag.PatientName);
      if ((patientName == null) || (patientName.equals("")))
        patientName = "MISSING PATIENT NAME";
      tl = new TextLayout(patientName, font, frc);

      // gets the width and height of the bounds of the Text
      double sw = tl.getBounds().getWidth();
      double sh = tl.getBounds().getHeight();

      x = (float) (this.image.getWidth() - sw - horizontalEdgeSpacing);
      y = (float) (sh) + verticalSpacing;
      tl.draw(graphics, x, y);

      String patientID = ds.getString(Tag.PatientID);
      if (patientID==null)
          patientID = "MISSING PATIENT ID";
      tl = new TextLayout(patientID, font, frc);
      sw = tl.getBounds().getWidth();
      sh = tl.getBounds().getHeight();
      x = (float) (this.image.getWidth() - sw - horizontalEdgeSpacing);
      y += tl.getBounds().getHeight() + verticalSpacing;
      tl.draw(graphics, x, y);

      String age = ds.getString(Tag.PatientAge);
      if ((age == null) || (age.equals(""))) {
        age = getDateOfBirth();
      }

      String sex = getPatientSex();

      tl = new TextLayout(sex + " " + age, font, frc);
      sw = tl.getBounds().getWidth();
      sh = tl.getBounds().getHeight();
      x = (float) (this.image.getWidth() - sw - horizontalEdgeSpacing);
      y += tl.getBounds().getHeight() + verticalSpacing;
      tl.draw(graphics, x, y);
      
      // Technique overlays
      x = 0.0f;
      y = (float) (imageH * 0.75);
      tl =
        new TextLayout(
          this.getOriginalWidth() + " x " + this.getOriginalHeight(),
          font,
          frc);
      tl.draw(graphics, x, y);
      y += tl.getBounds().getHeight() + verticalSpacing;

      String magnificationFactor = magFormat.format(this.scale);
      tl = new TextLayout("Mag: " + magnificationFactor + "x", font, frc);
      tl.draw(graphics, x, y);

      String sliceThickness = ds.getString(Tag.SliceThickness);
      if ((sliceThickness != null) && (!sliceThickness.equals(""))) {

        tl = new TextLayout("ST: " + sliceThickness, font, frc);
        y += tl.getBounds().getHeight() + verticalSpacing;
        tl.draw(graphics, x, y);
      }

      String kvp = ds.getString(Tag.KVP);
      if ((kvp != null) && (!kvp.equals(""))) {

        tl = new TextLayout("KVP: " + kvp, font, frc);
        y += tl.getBounds().getHeight() + verticalSpacing;
        tl.draw(graphics, x, y);
      }

      y += tl.getBounds().getHeight() + verticalSpacing;
      tl =
        new TextLayout(
          "Img:" + ds.getString(Tag.InstanceNumber),
          font,
          frc);
      tl.draw(graphics, x, y);
      
      if (monochrome){
          renderWindowLevelText(font, x, y, imageH, imageW, graphics, frc);
      }
    }
    
    if (localizers) {
     
      
      try{
          if ((patientOrientation != null) && (patientOrientation.length >1)){
              String rowAxis = patientOrientation[0];
              if (rowAxis == null){
                  log.error("rowAxis for patientOrientation is null");
              }
              else{
                  tl = new TextLayout(rowAxis,font, frc);
                  double sw = tl.getBounds().getWidth();
                  double sh = tl.getBounds().getHeight();
          
                  x = (float) (imageW - sw - horizontalEdgeSpacing);
                  y = (float) ( (imageH/2) - verticalSpacing - sh);
                  tl.draw(graphics, x, y);
                  if (patientOrientation.length==2) {
                      String columnAxis = patientOrientation[1];
                      if (columnAxis == null){
                          log.error("columnAxis for patientOrientation is null");
                      }
                      else{
                          tl = new TextLayout(columnAxis,font, frc);
                          sw = tl.getBounds().getWidth();
                          sh = tl.getBounds().getHeight();
              
                          x = (float) ((imageW/2) - sw - horizontalEdgeSpacing);
                          y = (float) ( (imageH) - verticalSpacing - sh);
                          tl.draw(graphics, x, y);
                      }
                  }
              }
          }
      }
      catch(Exception e){
          log.error("Exception handling patient orientation overlay ", e);
      }
          
      try{
          if ((imageOrientation != null) &&  (imageOrientation.length >1)){
              String columnRendering = getOrientation(imageOrientation, COL);
              String rowRendering = getOrientation(imageOrientation, ROW);
              log.info("rowRendering = " + rowRendering);
              log.info("columnRendering = " + columnRendering);
              tl = new TextLayout(columnRendering,font, frc);
              double sw = tl.getBounds().getWidth();
              double sh = tl.getBounds().getHeight();
      
              x = (float) ((imageW/2) - sw - horizontalEdgeSpacing);
              y = (float) ( (imageH) - verticalSpacing - sh);
              tl.draw(graphics, x, y);
      
             tl = new TextLayout(rowRendering,font, frc);
             sw = tl.getBounds().getWidth();
             sh = tl.getBounds().getHeight();
      
              x = (float) (imageW - sw - horizontalEdgeSpacing);
              y = (float) ( (imageH/2) - verticalSpacing - sh);
              tl.draw(graphics, x, y);
      
              
          }
      }
      catch(Exception e){
          log.error("Exception handling imageOrientation ", e);
      }
     
    
     
      graphics.dispose();
    }
  }
  
  /**
   * Returns the sex of the patient to which this image belongs,
   * or "?" if the sex is not specified.
   */
  public String getPatientSex() {
    String sex = "";// TODO dataset.getString(Tags.PatientSex);
    return sex == null ? "?" : sex;
  }

  /**
   * Returns the DOB of the patient to which this image belongs or 
   * blank if the DOB is not specified.
   * 
   * @return
   */
  public String getDateOfBirth() {
    String dob = "";// TODO dataset.getString(Tags.PatientBirthDate);    
    return dob == null ? "" : dob;
  }

  /**
   * @param font
   * @param dataset
   * @param x
   * @param y
   * @param imageH
   * @param graphics
   * @param frc
   */
  protected void renderWindowLevelText(Font font, float x, float y, int imageH, int imageW, Graphics2D graphics, FontRenderContext frc) {
    TextLayout tl;
    // Only need to draw one window/level value
    String window = Integer.toString(specifiedWindowWidth);
    String level =  Integer.toString(specifiedWindowCenter);
    tl = new TextLayout("W:" + window + " L:" + level, font, frc);
    y = (float) (imageH - tl.getBounds().getHeight());
    tl.draw(graphics, x, y);
  
  }

  /**
   * Calculates the scaling factor for the image and sets the 
   * output size of the image appropriately to contain the 
   * scaled image.
   * <p>
   * If specific columns and rows were set for the image then uses those 
   * to calculate scale and the output image is sized such that it 
   * always exactly matches the specified rows and columns.  This may result in
   * an only partially filled image being returned if the aspect ration of
   * the image is different to that of the specified rows and columns.
   * <p>
   * If specific rows and columns are not set but maximum rows and columns are 
   * passed as parameters then the scale is calculated to fit within the specified
   * maximum size and the output image size is exactly the size of the scaled
   * image, acquiring the same aspect ratio as the original image.
   * <p>
   * If either row,col spec is missing for either max or the requested values
   * then {@link IllegalArgumentException} is thrown.
   * 
   * @param maxColumns      maximum width in pixels for output image.
   *                        IGNORED if rows and columns were set
   *                        in constructor
   *                        
   * @param maxRows         maximum height in pixels for output image
   *                        IGNORED if rows and columns were set 
   *                        in constructor
   *                        
   * @param numFrames       the number of frames that are being rendered
   *                        in the output image.                        
   *                        
   * @return                the scaling factor that will be applied to both
   *                        dimensions of the input image to produce the 
   *                        output image.
   */
  public double calculateScale(int maxColumns, int maxRows, int numFrames) {
    double scale = 1.0;
    double scaleWidth, scaleHeight;
    
    if((this.columns != Integer.MIN_VALUE) && (this.rows != Integer.MIN_VALUE)) {
      scaleWidth =
        ((double) this.columns) / ((double) this.originalWidth);
      scaleHeight =
        ((double) this.rows) / ((double) this.originalHeight);
      scale = Math.min(scaleWidth, scaleHeight);
      this.outputWidth = this.columns;
      this.outputHeight = this.rows;
    } 
    else
    if((maxColumns != Integer.MIN_VALUE) && (maxRows != Integer.MIN_VALUE)) {
      scaleWidth =
        ((double) maxColumns) / ((double) this.originalWidth);
      scaleHeight =
        ((double) (maxRows)) / ((double) this.originalHeight);
      scale = Math.min(scaleWidth, scaleHeight);
      this.outputWidth = (int) (this.originalWidth * scale);
      this.outputHeight = (int) (this.originalHeight * numFrames * scale);
    }
    else
        throw new IllegalArgumentException("Expected either rows,columns to be set or maxRows and maxCols");
    
    //log.info("Image dimensions:" + this.toString());
    return (scale);
  }

  private boolean isMonochrome(String pmi){
    boolean isMonochrome = false;
    if ("MONOCHROME1".equals(pmi) || "MONOCHROME2".equals(pmi))
       isMonochrome = true;
    return(isMonochrome);
  }
  
  private int specifiedWindowCenter = 0;;
  private int specifiedWindowWidth  = 0;
  private boolean monochrome = false;
  
  /**
   * Creates the in-memory image matching the given WADO parameters
   * 
   * @param dataset
   * @param params
   * @param reader
   * @param imageFile
   */  
  public void createImage(InputStream inputStream, WADOParameterForm params){
	  BufferedImage originalImage = null;
	  DicomImageReadParam param =  (DicomImageReadParam) reader.getDefaultReadParam();
      param.setWindowCenter(params.getWindowCenter());
      param.setWindowWidth(params.getWindowWidth());
      specifiedWindowCenter = params.getWindowCenter();
      specifiedWindowWidth = params.getWindowWidth();
      String pmi = params.getPhotometricInterpretation();
      monochrome = isMonochrome(pmi);
      try {
          originalImage = reader.read(params.getFrameNumber(), param);        
          if(params.isFrameSprite()) {
              long startTimeMs = System.currentTimeMillis();
              int totalFrames = reader.getNumImages(false);
              int height = reader.getHeight(0); 
              
              // The frame sprite is rendered in 32,768 pixel strips
              // We cannot send back a sprite higher than 32,768 because many browsers have a problem with that
              // Problem:  zoom and scaling are applied *after* we create the image, so we have
              // to take them into account in advance and anticipate their affects by scaling down 
              // appropriately
              double scale = calculateScale(params.getMaxColumns(), params.getMaxRows(), totalFrames);
              
              // Calculate the region to draw
              Rectangle region = params.isRegionSpecified() ? 
                  calculateZoomRegion(params.getTopLeftX(), params.getTopLeftY(), params.getBottomRightX(), params.getBottomRightY())
              : 
                  new Rectangle(0,0, reader.getWidth(0), reader.getHeight(0));
              
              int regionHeight = (int)region.getHeight();
              setOriginalHeight(regionHeight);
              setOriginalWidth((int)region.getWidth());
              
              int maxFrames = (int)Math.floor((double)MAX_SPRITE_HEIGHT / (scale * height));
              int numFrames = Math.min(totalFrames - params.getFrameNumber(), maxFrames); 
              this.setNumberOfFrames(numFrames);
              if(log.isInfoEnabled())
	              log.info("Creating multiframe sprite image "+region.getWidth()+" x " + regionHeight*numFrames + " (" + numFrames + " frames)");
              
              BufferedImage sprite = new BufferedImage((int)region.getWidth(), regionHeight*numFrames, originalImage.getType());
              Graphics2D gfx = sprite.createGraphics();
			  ImageObserver observer = new ImageObserver() {
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    log.info("Notified of image change ?!");
                    return false;
                }
	          };               
	          
	          // We already have the first image, so do it outside the loop
              gfx.drawImage(originalImage.getSubimage( 
		          (int)region.getMinX(), (int)region.getMinY(), (int)region.getWidth(), regionHeight),0, 0, observer);
              
              for(int i=1; i<numFrames; ++i) {
                  BufferedImage img = reader.read(params.getFrameNumber()+i,param);
		          gfx.drawImage(img.getSubimage(
		              (int)region.getMinX(), (int)region.getMinY(), (int)region.getWidth(), regionHeight),
		              0,i*regionHeight, observer);
              }
              image = sprite;
              log.info("Created sprite "+region.getWidth()+" x " + regionHeight*numFrames + " (" + numFrames + " frames) in "
                      + (System.currentTimeMillis() - startTimeMs));
          }
          else {
	    	  image=originalImage;
          }
      }
      catch(IOException e){
          log.error("Error reading image ", e);
      }
      catch(RuntimeException e){
          log.error("Error reading image ", e); // TODO: Possible race condition if data is being retrieved from offline storage handler
      }
     
    }

  
  /**
   * scaleImage - scales the given image to the given maxColumns and maxRows size
   * based on the given interpolation type.
   * 
   * @param isFrameSprite      true if a sprite containing all frames is being rendered
   */
  public void scaleImage(int interpolationType, int maxColumns, int maxRows,
                         String photometricInterpretation, boolean isFrameSprite) {
      this.setScale(this.calculateScale(maxColumns, maxRows, isFrameSprite?numberOfFrames:1));
      // Dead code?
      if (interpolationType == WADOConstants.INTERPOLATION_FAST) {
        AffineTransform affineTransform =
          AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp affineTransformOp =
          new AffineTransformOp(affineTransform, null);
            this.image =  affineTransformOp.filter(this.image, null);
      } 
      else {
        BufferedImage outputImage = null;
        boolean monochrome = isMonochrome(photometricInterpretation);
        if (monochrome)
          outputImage = new BufferedImage(
            this.getOutputWidth(),
            this.getOutputHeight(),
            BufferedImage.TYPE_BYTE_GRAY);
        else
          outputImage = new BufferedImage(
              this.getOutputWidth(),
              this.getOutputHeight(),
              BufferedImage.TYPE_INT_RGB); // Not sure this is correct.
        Graphics2D g2d = outputImage.createGraphics();
        g2d.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(
          this.image,
          0,
          0,
          this.getOutputWidth(),
          this.getOutputHeight(),
          null);
        g2d.dispose();
        log.info("scaleImage: maxRows = " + maxRows + ", maxCols=" + maxColumns + ", rows="
                + this.image.getHeight() + ", cols=" + this.image.getWidth() +
                ", outputHeight = " + this.getOutputHeight() + ", outputWidth=" +
                this.outputWidth + ", scale = " + this.getScale() );
        this.image = outputImage;
      }    
  }
  
  /**
   * Returns a BufferedImage that is the result of zooming to the rectangle
   * specfied by topLeftX, topLeftY, bottomLeftX, bottomRightY, zooming to 
   * fit size maxColumns x maxRows while maintaining the aspect ratio of the
   * original image (?? - need to check if this is really accurate, ssadedin).
   */
  public void zoomToRegion(int maxColumns, int maxRows, double topLeftX, double topLeftY, double bottomRightX, double bottomRightY) {
      this.image = zoomToRegion(image,maxColumns,maxRows, topLeftX, topLeftY, bottomRightX, bottomRightY);
  }
  
  public BufferedImage zoomToRegion(BufferedImage img, int maxColumns, int maxRows, double topLeftX, double topLeftY, double bottomRightX, double bottomRightY) {
      
    Rectangle region = calculateZoomRegion(topLeftX, topLeftY, bottomRightX, bottomRightY);

    // log.info("x=" + newTopLeftX + ", y=" + newTopLeftY + ", w=" + newWidth + ", h=" + newHeight);
    this.setOriginalWidth((int)region.getWidth());
    this.setOriginalHeight((int)region.getHeight());  
    return img.getSubimage((int)region.getMinX(), (int)region.getMinY(), (int)region.getWidth(), (int)region.getHeight());
  }
  
  /**
   * Calculate actual pixel coordinates of the current image specified by 
   * the given region specified in normalized / fractional coordinates.
   */
  protected Rectangle calculateZoomRegion(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY) {
      
      topLeftX = Math.max(topLeftX,0.0);
      topLeftY = Math.max(topLeftY,0.0);
      bottomRightX = Math.min(bottomRightX,1.0);
      bottomRightY = Math.min(bottomRightY,1.0);
      
      int newTopLeftX =
        (int) (topLeftX * this.getOriginalWidth());
      int newTopLeftY =
        (int) (topLeftY * this.getOriginalHeight());
      int newWidth =
        (int) (bottomRightX * this.getOriginalWidth())
          - newTopLeftX;
      int newHeight =
        (int) (bottomRightY * this.getOriginalHeight())
          - newTopLeftY;

      // Adjust magnified image to fit aspect ratio
      // ssadedin:  note the explicit cast to float to avoid rounding.
      float wRatio = ((float)this.getOriginalWidth()) / ((float)newWidth);
      float hRatio = (float)this.getOriginalHeight() / ((float)newHeight);
      float minRatio = Math.min(wRatio, hRatio);
      float maxRatio = Math.max(wRatio, hRatio);
      
      newHeight = (int) (this.getOriginalHeight() / minRatio);
      newWidth = (int) (this.getOriginalWidth() / minRatio);
      
      // If rescaled image goes out of bounds then move
      // coordinates.
      // A bit of a kludge - should rewrite using rectangles
      // with intersection, clipping wadoImage.
      if ((newHeight + newTopLeftY) > this.getOriginalHeight())
        newTopLeftY = this.getOriginalHeight() - newHeight - 1;
      if ((newWidth + newTopLeftX) > this.getOriginalWidth())
        newTopLeftX = this.getOriginalWidth() - newWidth - 1;
      
      return new Rectangle(newTopLeftX, newTopLeftY, newWidth, newHeight);
  }

public String toString() {
    StringBuilder buff = new StringBuilder("WADOImage[");
    buff.append("\n originalWidth = " + originalWidth);
    buff.append("\n originalHeight = " + originalHeight);
    buff.append("\n outputWidth = " + outputWidth);
    buff.append("\n outputHeight = " + outputHeight);
    buff.append("\n numberOfFrames = " + numberOfFrames);
    buff.append("]");
    return (buff.toString());
  }

    /**
     * @return Returns the originalHeight.
     * 
     * @uml.property name="originalHeight"
     */
    public int getOriginalHeight() {
        return originalHeight;
    }

    /**
     * @param originalHeight The originalHeight to set.
     * 
     * @uml.property name="originalHeight"
     */
    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    /**
     * @return Returns the originalWidth.
     * 
     * @uml.property name="originalWidth"
     */
    public int getOriginalWidth() {
        return originalWidth;
    }

    /**
     * @param originalWidth The originalWidth to set.
     * 
     * @uml.property name="originalWidth"
     */
    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    /**
     * @return Returns the outputHeight.
     * 
     * @uml.property name="outputHeight"
     */
    public int getOutputHeight() {
        return outputHeight;
    }

    /**
     * @param outputHeight The outputHeight to set.
     * 
     * @uml.property name="outputHeight"
     */
    public void setOutputHeight(int outputHeight) {
        this.outputHeight = outputHeight;
    }

    /**
     * @return Returns the outputWidth.
     * 
     * @uml.property name="outputWidth"
     */
    public int getOutputWidth() {
        return outputWidth;
    }

    /**
     * @param outputWidth The outputWidth to set.
     * 
     * @uml.property name="outputWidth"
     */
    public void setOutputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
    }

    /**
     * @return Returns the numberOfFrames.
     * 
     * @uml.property name="numberOfFrames"
     */
    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    /**
     * @param numberOfFrames The numberOfFrames to set.
     * 
     * @uml.property name="numberOfFrames"
     */
    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    /**
     * @return Returns the scale.
     * 
     * @uml.property name="scale"
     */
    public double getScale() {
        return scale;
    }

    /**
     * @param scale The scale to set.
     * 
     * @uml.property name="scale"
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @return Returns the image.
     * 
     * @uml.property name="image"
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * @param image The image to set.
     * 
     * @uml.property name="image"
     */
    protected void setImage(BufferedImage image) {
        this.image = image;
    }

    private int parseInt(String value){
    	int val = Integer.MIN_VALUE;
    	if (value != null){
    		if (value.indexOf(".") != -1){
    			float f = Float.parseFloat(value);
    			val = Float.floatToIntBits(f);
    		}
    		else{
    			val = Integer.parseInt(value);
    		}
    	}
    	return(val);
    }

    /**
     * Returns the orientation marker for the specified row or column based
     * on cosine metadata.
     * 
     * "C.7.6.2.1.1 Image Position And Image Orientation. The Image Position (0020,0032) 
     * specifies the x, y, and z coordinates of the upper left hand corner of the image; 
     * it is the center of the first voxel transmitted. Image Orientation (0020,0037) 
     * specifies the direction cosines of the first row and the first column with 
     * respect to the patient. These Attributes shall be provide as a pair. 
     * Row value for the x, y, and z axes respectively followed by the Column 
     * value for the x, y, and z axes respectively. The direction of the axes 
     * is defined fully by the patient's orientation. The x-axis is increasing 
     * to the left hand side of the patient. The y-axis is increasing to the
     *  posterior side of the patient. The z-axis is increasing toward the head of 
     *  the patient. The patient based coordinate system is a right handed system, 
     *  i.e. the vector cross product of a unit vector along the positive x-axis and a 
     *  unit vector along the positive y-axis is equal to a unit vector along the 
     *  positive z-axis."
     */
    
    String getOrientation(String[] stringVector, String rowOrCol)
    {
            //char *orientation=new char[4];
           // char *optr = orientation;
            //*optr='\0';
            // The spec says that this is a 6 element array; Acuo generates 10 elements.
            // But the first 6 values look OK.
            if ((stringVector == null) || (stringVector.length < 6))
                return(null);
            StringBuffer buff = new StringBuffer();
            
            double doubleVector [] = new double[3];
            if (ROW==rowOrCol){
                for (int i=0;i<3;i++){
                    doubleVector[i] = Double.parseDouble(stringVector[i]);
                }
            }
            else{
                for (int i=0;i<3;i++){
                    doubleVector[i] = Double.parseDouble(stringVector[i+3]);
                }
            }
            log.debug("Vector  X=" + doubleVector[0] + ", Y=" + doubleVector[1] + 
                    ", Z=" + doubleVector[2] + ":" + rowOrCol);
                    
            String orientationX = doubleVector[0] < 0.0 ? "R" : "L"; 
            String orientationY = doubleVector[1] < 0.0 ? "A" : "P";
            String orientationZ = doubleVector[2] < 0.0 ? "F" : "H";
            
            double absX = Math.abs(doubleVector[0]);
            double absY = Math.abs(doubleVector[1]);
            double absZ = Math.abs(doubleVector[2]);
            log.debug("absX = " + absX + ", absY=" + absY + ", absZ=" + absZ);
            int i;
            for (i=0; i<3; ++i) {
                    if (absX>.0001 && absX>absY && absX>absZ) {
                            buff.append(orientationX);
                            absX=0;
                    }
                    else if (absY>.0001 && absY>absX && absY>absZ) {
                            buff.append(orientationY);
                            absY=0;
                    }
                    else if (absZ>.0001 && absZ>absX && absZ>absY) {
                            buff.append(orientationZ);
                            absZ=0;
                    }
                    else break;
                    
            }
           // log.info("orientation is " + buff.toString());
            return buff.toString();
    }
}
