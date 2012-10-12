/*
 * $Id: $
 * Created on Aug 27, 2004
 */
package net.medcommons.router.services.wado;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

/**
 * WADOParameterForm represents the parameters conatined in a WADO HTTP request 
 * as defined by DICOM Supplement 85 (ISO/TC215)  
 * 
 * @author ssadedin
 */
public class WADOParameterForm extends ActionForm {
 
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(WADOParameterForm.class);

  
  /**
   * TODO: doco for all these
   */
  private int frameNumber = 0;
  private int rows = Integer.MIN_VALUE;
  private int columns = Integer.MIN_VALUE;
  private int maxRows = Integer.MIN_VALUE;
  private int maxColumns = Integer.MIN_VALUE;
  
  /**
   * Windowing value - note this is aliased with getters and setters to 'windowWidth' property
   */
  // private int window = Integer.MIN_VALUE;
  private int window = 250;
  
  /**
   * Leveling value - note this is aliased with getters and setters to 'windowCenter' property
   */
  // private int level = Integer.MIN_VALUE;
  private int level = 50;
  
  private String studyUID = null;
  private String seriesUID = null;
  private String objectUID = null;

  private String fname = null;
  
  /**
   * Whether the patient annotation should be shown
   */
  private boolean patientAnnotation = false;
  
  /**
   * Whether the technique annotation should be shown
   */
  private boolean localizerAnnotation = false;
  
  /**
   * Priority of this image request - one of "low", "med", "high"
   */
  private String priority = "high";
  
  private String contentType = WADOConstants.CONTENT_TYPE_JPEG;
  private boolean regionSpecified = false;
  private double topLeftX = 0.0;
  private double topLeftY = 0.0;
  private double bottomRightX = 0.0;
  private double bottomRightY = 0.0;
  private int imageQuality = 100; // Highest quality
  private String patientName = "";
  private String mcGUID = null;

  private String photometricInterpretation = null;
  private int interpolationType = WADOConstants.INTERPOLATION_SMOOTH;

  /**
   * If set to true then instead of a plain image, a 
   * grid of different renderings of the same image is returned
   * where each point in the grid is set to a combination of window / level
   * values specified in the {@link #levels} and {@link #widths} 
   * fields.
   */
  private boolean windowLevelGrid = false;
  
  /**
   * List of window width values to render when {@link #windowLevelGrid} 
   * is true.
   */
  private List<Integer> widths = null;
  
  /**
   * List of window level values to render when {@link #windowLevelGrid} 
   * is true.
   */
  private List<Integer> levels = null;
  
  private String storageId;
  
  /**
   * If set to true, requests that the returned image contain 
   * ALL frames embedded in the DICOM tiled vertically.
   */
  private boolean frameSprite;
  
  /**
   * If set to true, requests that the returned image contain
   * ALL images in the series tiled vertically
   */
  private boolean thumbStrip;
  
  
  /**
   * How many instances to render in a thumbnail strip
   */
  private int thumbStripInstances;
  
  /**
   * Maximum number of pixels to use in a multiframe sprite.
   * This defaults to 32,768 but it can be overridden because
   * some clients just can't handle images that are so high.
   */
  private int maxSpriteHeight = WADOImage2.MAX_SPRITE_HEIGHT;
  
  public String toString() {
    StringBuffer buff = new StringBuffer("parameterBlock[");
    buff.append("\n mcGUID = " + mcGUID);
    buff.append("\n frameNumber = " + frameNumber);
    buff.append("\n rows = " + rows);
    buff.append("\n columns = " + columns);
    buff.append("\n maxRows = " + maxRows);
    buff.append("\n maxColumns = " + maxColumns);
    buff.append("\n window = " + window);
    buff.append("\n level = " + level);
    buff.append("\n studyUID = " + studyUID);
    buff.append("\n seriesUID = " + seriesUID);
    buff.append("\n objectUID = " + objectUID);
    buff.append("\n patientAnnotation = " + patientAnnotation);
    buff.append("\n localizerAnnotation = " + localizerAnnotation);
    buff.append("\n contentType = " + contentType);
    buff.append("\n regionSpecified = " + regionSpecified);
    buff.append("\n topLeftX = " + topLeftX);
    buff.append("\n topLeftY = " + topLeftY);
    buff.append("\n bottomRightX = " + bottomRightX);
    buff.append("\n bottomRightY = " + bottomRightY);
    buff.append("\n imageQuality = " + imageQuality);
    buff.append(
      "\n photometricInterpretation = " + photometricInterpretation);
    buff.append("\n windowLevelGrid = " + windowLevelGrid);
    if (interpolationType == WADOConstants.INTERPOLATION_FAST)
      buff.append("\n interpolationType = FAST");
    else
      buff.append("\n interpolationType = SMOOTH");
    buff.append("  ]");

    return (buff.toString());
  }

    /**
     * @return Returns the bottomRightX.
     * 
     * @uml.property name="bottomRightX"
     */
    public double getBottomRightX() {
        return bottomRightX;
    }

    /**
     * @param bottomRightX The bottomRightX to set.
     * 
     * @uml.property name="bottomRightX"
     */
    public void setBottomRightX(double bottomRightX) {
        this.bottomRightX = bottomRightX;
    }

    /**
     * @return Returns the bottomRightY.
     * 
     * @uml.property name="bottomRightY"
     */
    public double getBottomRightY() {
        return bottomRightY;
    }

    /**
     * @param bottomRightY The bottomRightY to set.
     * 
     * @uml.property name="bottomRightY"
     */
    public void setBottomRightY(double bottomRightY) {
        this.bottomRightY = bottomRightY;
    }

    /**
     * @return Returns the columns.
     * 
     * @uml.property name="columns"
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @param columns The columns to set.
     * 
     * @uml.property name="columns"
     */
    public void setColumns(int columns) {
        this.columns = columns;
    }

    /**
     * @return Returns the contentType.
     * 
     * @uml.property name="contentType"
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType The contentType to set.
     * 
     * @uml.property name="contentType"
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return Returns the frameNumber.
     * 
     * @uml.property name="frameNumber"
     */
    public int getFrameNumber() {
        return frameNumber;
    }

    /**
     * @param frameNumber The frameNumber to set.
     * 
     * @uml.property name="frameNumber"
     */
    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    /**
     * @return Returns the imageQuality.
     * 
     * @uml.property name="imageQuality"
     */
    public int getImageQuality() {
        return imageQuality;
    }

    /**
     * @param imageQuality The imageQuality to set.
     * 
     * @uml.property name="imageQuality"
     */
    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    /**
     * @return Returns the interpolationType.
     * 
     * @uml.property name="interpolationType"
     */
    public int getInterpolationType() {
        return interpolationType;
    }

    /**
     * @param interpolationType The interpolationType to set.
     * 
     * @uml.property name="interpolationType"
     */
    public void setInterpolationType(int interpolationType) {
        this.interpolationType = interpolationType;
    }

    /**
     * @return Returns the level.
     * 
     * @uml.property name="level"
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level The level to set.
     * 
     * @uml.property name="level"
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return Returns the maxColumns.
     * 
     * @uml.property name="maxColumns"
     */
    public int getMaxColumns() {
        return maxColumns;
    }

    /**
     * @param maxColumns The maxColumns to set.
     * 
     * @uml.property name="maxColumns"
     */
    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    /**
     * @return Returns the maxRows.
     * 
     * @uml.property name="maxRows"
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * @param maxRows The maxRows to set.
     * 
     * @uml.property name="maxRows"
     */
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * @return Returns the mcGUID.
     * 
     * @uml.property name="mcGUID"
     */
    public String getMcGUID() {
        return mcGUID;
    }

    /**
     * @param mcGUID The mcGUID to set.
     * 
     * @uml.property name="mcGUID"
     */
    public void setMcGUID(String mcGUID) {
        this.mcGUID = mcGUID;
    }

    /**
     * @return Returns the objectUID.
     * 
     * @uml.property name="objectUID"
     */
    public String getObjectUID() {
        return objectUID;
    }

    /**
     * @param objectUID The objectUID to set.
     * 
     * @uml.property name="objectUID"
     */
    public void setObjectUID(String objectUID) {
        this.objectUID = objectUID;
    }

    /**
     * @return Returns the patientAnnotation.
     * 
     * @uml.property name="patientAnnotation"
     */
    public boolean isPatientAnnotation() {
        return patientAnnotation;
    }

    /**
     * @param patientAnnotation The patientAnnotation to set.
     * 
     * @uml.property name="patientAnnotation"
     */
    public void setPatientAnnotation(boolean patientAnnotation) {
        this.patientAnnotation = patientAnnotation;
    }

    /**
     * @return Returns the patientName.
     * 
     * @uml.property name="patientName"
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * @param patientName The patientName to set.
     * 
     * @uml.property name="patientName"
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * @return Returns the photometricInterpretation.
     * 
     * @uml.property name="photometricInterpretation"
     */
    public String getPhotometricInterpretation() {
        return photometricInterpretation;
    }

    /**
     * @param photometricInterpretation The photometricInterpretation to set.
     * 
     * @uml.property name="photometricInterpretation"
     */
    public void setPhotometricInterpretation(String photometricInterpretation) {
        this.photometricInterpretation = photometricInterpretation;
    }


	/**
	 * Performs some sanity checks for input parameters. Makes minor adjustments if 
	 * necessary.
	 * @param uParams
	 */
	public void adjust() {
		double width = this.bottomRightX - this.topLeftX;
		double height = this.bottomRightY - this.topLeftY;

		if (this.regionSpecified) {

			if (width < 0.1) {
				this.bottomRightX = this.topLeftX + 0.1f;
			}
			if (height < 0.1) {
				this.bottomRightY = this.topLeftY + 0.1f;
			}
			if (this.bottomRightX > 1.0f) {
				double diff = 1.0f - this.bottomRightX;
				this.bottomRightX = 1.0;
				this.topLeftX -= diff;
			}
			if (this.bottomRightY > 1.0f) {
				double diff = 1.0f - this.bottomRightY;
				this.bottomRightY = 1.0;
				this.topLeftY -= diff;
			}

			if (this.topLeftX < 0.0f) {
				this.bottomRightX -= this.topLeftX;
				this.topLeftX = 0.0f;
			}
			if (this.topLeftY < 0.0f) {
				this.bottomRightY -= this.topLeftY;
				this.topLeftY = 0.0f;
			}
		}
	}
	
  /**
   * Sets the region to the given value string.  The value string is expected
   * to be composed of four pieces separated by commas.
   * @param value
   */
  public void setRegion(String value) {
    int counter = 0;
    String [] tokens = value.split(",");
    if (tokens.length == 4) {
      try {
        double topLeftX = Double.parseDouble(tokens[0]);
        double topLeftY = Double.parseDouble(tokens[1]);
        double bottomRightX = Double.parseDouble(tokens[2]);
        double bottomRightY = Double.parseDouble(tokens[3]);

        // Note: the purpose of doing thi separately to above is to ensure that
        // either all
        // the region points get set or none of them get set.
        this.setTopLeftX(topLeftX);
        this.setTopLeftY(topLeftY);
        this.setBottomRightX(bottomRightX);
        this.setBottomRightY(bottomRightY);
        regionSpecified = true;
      } 
      catch (NumberFormatException e) {
        log.warn("Invalid format in region string: " + value);
      }
    }
    else {
      log.warn("Invalid format in region string: " + value);
    }
  }
  
  
  /**
   * Sets the annotation flags based on the given WADO annotation string.
   * @param annotation
   */
  public void setAnnotation(String annotation) {
    
    // Turn off all annotation and then turn back on only what is specified.
    this.setPatientAnnotation(false);
    this.setLocalizerAnnotation(false);
    
    StringTokenizer strtok = new StringTokenizer(annotation, ",");    
    while (strtok.hasMoreTokens()) {
      String token = strtok.nextToken();
      if (token.equals("patient")) {
        this.setPatientAnnotation(true);
      }
      else if (token.equals("localizers")) {
        this.setLocalizerAnnotation(true); 
      }
    }
  }
  
  /**
   * Returns the annotation as a string.  The annotation is a list of
   * annotation types separated by commas.  The only valid types are "patient" 
   * and "technique" (See ISO/WD1.14, 8.2.1).
   * 
   * [Now ignoring WADO spec]
   */
  public String getAnnotation() {
    if(this.patientAnnotation && this.localizerAnnotation) {
      return "patient,localizers";
    }
    else if(this.patientAnnotation) {
      return "patient";
    }
    else if(this.localizerAnnotation) {
      return "localizers";
    }
    else return "";
  }
  
  
  /**
   * Returns true if the image requires scaling, false otherwise.
   * @param uParams
   * @return
   */
  public boolean isScalingRequired() {
    boolean returnVal = false;
    if ((this.columns != Integer.MIN_VALUE)
      && (this.rows != Integer.MIN_VALUE))
      returnVal = true;
    else if (
      (this.maxColumns != Integer.MIN_VALUE)
        && (this.maxRows != Integer.MIN_VALUE))
      returnVal = true;
    return (returnVal);
  }
  
  
  /**
   * Returns the region specified as four comma separated doubles
   * 
   * @return
   */
  public String getRegion() {
    return this.topLeftX + "," + this.topLeftY + "," + this.bottomRightX + "," + this.bottomRightY;
  }

    /**
     * @return Returns the regionSpecified.
     * 
     * @uml.property name="regionSpecified"
     */
    public boolean isRegionSpecified() {
        return regionSpecified;
    }

    /**
     * @param regionSpecified
     *          The regionSpecified to set.
     * 
     * @uml.property name="regionSpecified"
     */
    public void setRegionSpecified(boolean regionSpecified) {
        this.regionSpecified = regionSpecified;
    }

    /**
     * @return Returns the rows.
     * 
     * @uml.property name="rows"
     */
    public int getRows() {
        return rows;
    }

    /**
     * @param rows
     *          The rows to set.
     * 
     * @uml.property name="rows"
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * @return Returns the seriesUID.
     * 
     * @uml.property name="seriesUID"
     */
    public String getSeriesUID() {
        return seriesUID;
    }

    /**
     * @param seriesUID
     *          The seriesUID to set.
     * 
     * @uml.property name="seriesUID"
     */
    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }

    /**
     * @return Returns the studyUID.
     * 
     * @uml.property name="studyUID"
     */
    public String getStudyUID() {
        return studyUID;
    }

    /**
     * @param studyUID
     *          The studyUID to set.
     * 
     * @uml.property name="studyUID"
     */
    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    /**
     * @return Returns the localizerAnnotation.
     * 
     * @uml.property name="localizerAnnotation"
     */
    public boolean isLocalizerAnnotation() {
        return localizerAnnotation;
    }

    /**
     * @param localizerAnnotation
     *          The localizerAnnotation status. Localizers displayed if true.
     * 
     * @uml.property name="localizerAnnotation"
     */
    public void setLocalizerAnnotation(boolean localizerAnnotation) {
        this.localizerAnnotation = localizerAnnotation;
    }

    /**
     * @return Returns the topLeftX.
     * 
     * @uml.property name="topLeftX"
     */
    public double getTopLeftX() {
        return topLeftX;
    }

    /**
     * @param topLeftX
     *          The topLeftX to set.
     * 
     * @uml.property name="topLeftX"
     */
    public void setTopLeftX(double topLeftX) {
        this.topLeftX = topLeftX;
    }

    /**
     * @return Returns the topLeftY.
     * 
     * @uml.property name="topLeftY"
     */
    public double getTopLeftY() {
        return topLeftY;
    }

    /**
     * @param topLeftY
     *          The topLeftY to set.
     * 
     * @uml.property name="topLeftY"
     */
    public void setTopLeftY(double topLeftY) {
        this.topLeftY = topLeftY;
    }

    /**
     * @return Returns the window.
     * 
     * @uml.property name="window"
     */
    public int getWindow() {
        return window;
    }

    /**
     * @param window
     *          The window to set.
     * 
     * @uml.property name="window"
     */
    public void setWindow(int window) {
        this.window = window;
    }
    
    /**
     * @return Returns the fname.
     * 
     * @uml.property name="fname"
     */
    public String getFname() {
        return fname;
    }

    /**
     * @param fname
     *          The fname to set.
     * 
     * @uml.property name="fname"
     */
    public void setFname(String filename) {
        this.fname = filename;
    }
    
    
    public int getWindowCenter() {
        return this.level;
    }
    
    public void setWindowCenter(int windowCenter) {
        this.level = windowCenter;
    }
    
    public int getWindowWidth() {
        return this.window;
    }
    
    public void setWindowWidth(int windowWidth) {
        this.window = windowWidth;
    }

    /**
     * @return Returns the priority.
     * 
     * @uml.property name="priority"
     */
    public String getPriority() {
        return priority;
    }

    /**
     * @param priority The priority to set.
     * 
     * @uml.property name="priority"
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageAccountId) {
        this.storageId = storageAccountId;
    }

    public boolean isFrameSprite() {
        return frameSprite;
    }

    public void setFrameSprite(boolean frameSprite) {
        this.frameSprite = frameSprite;
    }

    public boolean isWindowLevelGrid() {
        return windowLevelGrid;
    }

    public void setWindowLevelGrid(boolean windowLevelGrid) {
        this.windowLevelGrid = windowLevelGrid;
    }

    public List<Integer> getWidths() {
        return widths;
    }

    public void setWidths(List<Integer> widths) {
        this.widths = widths;
    }

    public List<Integer> getLevels() {
        return levels;
    }

    public void setLevels(List<Integer> levels) {
        this.levels = levels;
    }

    public int getMaxSpriteHeight() {
        return maxSpriteHeight;
    }

    public void setMaxSpriteHeight(int maxSpriteHeight) {
        this.maxSpriteHeight = maxSpriteHeight;
    }

    public boolean isThumbStrip() {
        return thumbStrip;
    }

    public void setThumbStrip(boolean thumbStrip) {
        this.thumbStrip = thumbStrip;
    }

    public int getThumbStripInstances() {
        return thumbStripInstances;
    }

    public void setThumbStripInstances(int thumbStripInstances) {
        this.thumbStripInstances = thumbStripInstances;
    }

}
