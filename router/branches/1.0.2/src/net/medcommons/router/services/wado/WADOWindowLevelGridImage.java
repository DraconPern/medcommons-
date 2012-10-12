/*
 * $Id: $
 * Created on Aug 29, 2004
 */
package net.medcommons.router.services.wado;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;


/**
 * The WindowLevelGridImage class creates an image containing a grid
 * of sub images, each sub-image representing values for windowing and
 * leveling as specified by the window and level grid arrays passed
 * in the constructor.
 * 
 * @author ssadedin
 */
public class WADOWindowLevelGridImage extends WADOImage2 {

  
	/**
	 * Logger to use with this class
	 */
  private static Logger log = Logger.getLogger(WADOWindowLevelGridImage.class);  
  private int windowGridArray[] = null;
  
  private int levelGridArray[] = null;  

 
  
  private DicomObject dicomObject = null;
  /**
   * @param originalWidth
   * @param originalHeight
   * @param windowLevelGridArray
   * @throws IOException
   */
  public WADOWindowLevelGridImage(InputStream inputStream, int frameNumber, int columns, int rows, int[] windowGridArray, int[] levelGridArray ) throws IOException {
    super(inputStream, frameNumber, columns, rows);
    this.windowGridArray = windowGridArray;
    this.levelGridArray = levelGridArray;
    log.info("Constructor");
  }  

  public void createImage(File imageFile, WADOParameterForm params)
      throws IOException {
      Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
      
      
     
      while (iter.hasNext()){
          this.reader =  iter.next();
          // image reader from dcm4che 2.x
          if (this.reader.getClass().getCanonicalName().indexOf("dcm4che2") > 0)
              break;
      }
     
      BufferedImage originalImage = null;
      
      
      reader.setInput(imageInputStream, false);

     
   
      
      dicomObject = ((DicomStreamMetaData) reader.getStreamMetadata()).getDicomObject();
      DicomImageReadParam imageParam =
          (DicomImageReadParam) reader.getDefaultReadParam();
      /*ColorModelParam cmParam = null;
      cmParam = cmFactory.makeParam(dataset);
      DcmImageReadParam imageParam =
        (DcmImageReadParam) reader.getDefaultReadParam();
      params.setPhotometricInterpretation(dataset.getString(Tag.PhotometricInterpretation));
      */
      BufferedImage grid[] = new BufferedImage[9];

      for (int i = 0; i < grid.length; i++) {
          imageParam.setWindowCenter((float) params.getLevelGridArray()[i]);
          imageParam.setWindowWidth((float) params.getWindowGridArray()[i]);
          grid[i] = reader.read(params.getFrameNumber(), imageParam);
      }

      BufferedImage gridImage =
        new BufferedImage(
          this.getOriginalWidth(),
          this.getOriginalHeight(),
          BufferedImage.TYPE_INT_RGB);
      //BufferedImage.TYPE_BYTE_GRAY (faster, but generates slightly bad results:Java bug?)
      Graphics2D g2d = gridImage.createGraphics();

      int nRows = 3;
      int nCols = 3;

      //  Output size of tiles
      int tileWidth = this.getOriginalWidth() / nCols;
      int tileHeight = this.getOriginalHeight() / nRows;

      // If there is no region specified - then simply
      // use the center 1/3 of the image as the region
      // to be displayed.
      int newTopLeftX = tileWidth;
      int newTopLeftY = tileHeight;
      int newWidth = tileWidth;
      int newHeight = tileHeight;

      if (params.isRegionSpecified()) {
        newTopLeftX = (int) (params.getTopLeftX() * this.getOriginalWidth());
        newTopLeftY = (int) (params.getTopLeftY() * this.getOriginalHeight());
        newWidth =
          (int) (params.getBottomRightX() * this.getOriginalWidth()) - newTopLeftX;
        newHeight =
          (int) (params.getBottomRightY() * this.getOriginalHeight())
            - newTopLeftY;
        float wRatio = this.getOriginalWidth() / newWidth;
        float hRatio = this.getOriginalHeight() / newHeight;
        if (wRatio < hRatio)
          newHeight = (int) (this.getOriginalHeight() / wRatio);
        else
          newWidth = (int) (this.getOriginalWidth() / hRatio);
        // Adjust starting coordinates to remain inside of image.
        if ((newTopLeftX + newWidth) > this.getOriginalWidth())
          newTopLeftX = this.getOriginalWidth() - newWidth;
        if ((newTopLeftY + newHeight) > this.getOriginalHeight())
          newTopLeftY = this.getOriginalHeight() - newHeight;
        
        log.info("newWidth="  + newWidth + ", newHeight=" + newHeight); 
        log.info("newTopLeftX = " + newTopLeftX + ", newTopLeftY= " +
          newTopLeftY);
       

      }
      else{
    	  log.info("No size parameters specified");
      }

      for (int i = 0; i < nRows; i++) {
        for (int j = 0; j < nCols; j++) {
          int index = (i * nCols) + j;
          log.info("Drawing image - row = " + i + ", col=" + j);
          g2d.drawImage(
            grid[index].getSubimage(
              newTopLeftX,
              newTopLeftY,
              newWidth,
              newHeight),
            tileWidth * j,
            tileHeight * i,
            tileWidth,
            tileHeight,
            null);
        }

      }

      g2d.dispose();
      this.setImage(gridImage);
    }    
  
  /**
   * Overrides the display of text to show labels for each item in the grid
   */
  protected void renderWindowLevelText(Font font, float x, float y,
      int imageH, int imageW, Graphics2D graphics, FontRenderContext frc) {

    TextLayout tl = new TextLayout("Img:"
        + dicomObject.getString(Tag.InstanceNumber), font, frc);

    int nRows = 3;
    int nCols = 3;
    for (int i = 0; i < nRows; i++) {
      for (int j = 0; j < nCols; j++) {
        int subimageWidth = imageW / nCols;
        int subimageHeight = imageH / nRows;
        int index = (i * nCols) + j;

        int window = windowGridArray[index];
        int level = levelGridArray[index];
        tl = new TextLayout("W:" + window + " L:" + level, font, frc);
        x = subimageWidth * j;
        y = (float) (subimageHeight * (i + 1) - tl.getBounds().getHeight());
        tl.draw(graphics, x, y);
        log.info("Rendered text for row " + i + ", column" + j);
      }
    }
  }
}
