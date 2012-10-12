/*
 * $Id: $
 * Created on Aug 29, 2004
 */
package net.medcommons.router.services.wado;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.medcommons.modules.utils.Str;

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
  
  private Integer widths[] = null;
  
  private Integer levels[] = null;  

  long [] invalidMask = null;
  
  /**
   * Create a window level grid as a 2d matrix 
   * of renderings of the image from the given image input stream.
   */
  public WADOWindowLevelGridImage(InputStream inputStream, int frameNumber, int columns, int rows, List<Integer> windowGridArray, List<Integer> levelGridArray ) throws IOException {
    super(inputStream, frameNumber, columns, rows);
    this.widths = windowGridArray.toArray(new Integer[windowGridArray.size()]);
    this.levels = levelGridArray.toArray(new Integer[levelGridArray.size()]);;
    this.invalidMask = new long[this.widths.length];
    
    if(columns<0)
        this.columns = getOriginalWidth();
    if(rows<0)
        this.rows = getOriginalWidth();
    
    log.info("Constructor");
  }  

  public void createImage(WADOParameterForm params) {
      
      log.info("Encoding " + this.widths.length + " * " + this.levels.length + " W/L grid");
      try {
          DicomImageReadParam imageParam =
              (DicomImageReadParam) reader.getDefaultReadParam();
          
          /*ColorModelParam cmParam = null;
          cmParam = cmFactory.makeParam(dataset);
          DcmImageReadParam imageParam =
            (DcmImageReadParam) reader.getDefaultReadParam();
          params.setPhotometricInterpretation(dataset.getString(Tag.PhotometricInterpretation));
           */
          
          
          
          BufferedImage grid[] = new BufferedImage[this.levels.length*this.widths.length];
          int k = 0;
          for(int i = 0; i < this.widths.length; i++) {
              for(int j = 0; j < this.levels.length; j++) {
                  imageParam.setWindowCenter((float) this.levels[j]);
                  imageParam.setWindowWidth((float) this.widths[i]);
                  log.info("Reading: " + this.widths[i] + "," + this.levels[j] );
                  try {
                      grid[k] = reader.read(params.getFrameNumber(), imageParam);
                  }
                  catch(ArrayIndexOutOfBoundsException e) {
                      log.warn("Failed to read image from frame " + params.getFrameNumber() + " window width =" + this.widths[i] + " + level = " + this.levels[j] + ": " + e.toString());
                      invalidMask[i] |= (1<<j);
                  }
                  catch(NegativeArraySizeException e) {
                      log.warn("Failed to read image from frame " + params.getFrameNumber() + " window width =" + this.widths[i] + " + level = " + this.levels[j] + ": " + e.toString());
                      invalidMask[i] |= (1<<j);
                  }
                  k++;
              }
          }
          
          BufferedImage gridImage =
              new BufferedImage(
                              this.columns,
                              this.rows,
                              BufferedImage.TYPE_INT_RGB);
          
          // BufferedImage.TYPE_BYTE_GRAY (faster, but generates slightly bad results:Java bug?)
          
          Graphics2D g2d = gridImage.createGraphics();
          
          int nRows = this.widths.length;
          int nCols = this.levels.length;
          
          //  Output size of tiles
          int tileWidth = this.columns / nCols;
          int tileHeight = this.rows / nRows;
          
          int newTopLeftX = 0;
          int newTopLeftY = 0;
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
          else {
              log.info("No size parameters specified");
          }
          
          FontRenderContext ctx = new FontRenderContext(null, true, false);
          Font font = Font.decode(Font.SANS_SERIF).deriveFont(19.0f);
          
          for (int i = 0; i < nRows; i++) {
              for (int j = 0; j < nCols; j++) {
                  int index = (i * nCols) + j;
                  log.info("Drawing image - row = " + i + ", col=" + j);
                  // Images that fail to encode end up null here
                  if(grid[index]==null)  {
                      g2d.setColor(Color.BLACK);
                      g2d.fillRect(tileWidth*j, tileHeight*i, tileWidth, tileHeight);
                      g2d.setColor(Color.RED);
                      new TextLayout("ERROR", font, ctx).draw(g2d, tileWidth*j+20, tileHeight*i+60);
                  }
                  else
                      g2d.drawImage(grid[index].getSubimage( 0, 0, getOriginalWidth(), getOriginalHeight()),
                                    tileWidth * j,
                                    tileHeight * i,
                                    tileWidth,
                                    tileHeight,
                                    null);
                  
                g2d.setColor(Color.WHITE);
                new TextLayout("W:" + widths[i] + " L:" + levels[j], font, ctx).draw(g2d, tileWidth*j+20, tileHeight*i+20);
              }
          }
          
          g2d.dispose();
          this.setImage(gridImage);
      }
      catch (IOException e) {
          throw new RuntimeException("Failed to encode " + this.widths.length + " * " + this.levels.length + " window level grid", e);
      }
    }    
  
    @Override
    public Map<String, String> getOutputMetaData() {
        StringBuilder mask = new StringBuilder();
        for(long m : invalidMask) {
            if(mask.length()>0)
                mask.append("^");
            mask.append(m);
        }
        return Collections.singletonMap("invalidMask", mask.toString());
    }
  
  /**
   * Overrides the display of text to show labels for each item in the grid
   */
  protected void renderWindowLevelText(Font font, float x, float y,
      int imageH, int imageW, Graphics2D graphics, FontRenderContext frc) {

    TextLayout tl = new TextLayout("Img:" + ds.getString(Tag.InstanceNumber), font, frc);

    int nRows = 3;
    int nCols = 3;
    for (int i = 0; i < nRows; i++) {
      for (int j = 0; j < nCols; j++) {
        int subimageWidth = imageW / nCols;
        int subimageHeight = imageH / nRows;
        int index = (i * nCols) + j;

        int window = widths[index];
        int level = levels[index];
        tl = new TextLayout("W:" + window + " L:" + level, font, frc);
        x = subimageWidth * j;
        y = (float) (subimageHeight * (i + 1) - tl.getBounds().getHeight());
        tl.draw(graphics, x, y);
        log.info("Rendered text for row " + i + ", column" + j);
      }
    }
  }

}
