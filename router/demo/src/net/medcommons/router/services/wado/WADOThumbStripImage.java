package net.medcommons.router.services.wado;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.router.services.dicom.util.MCInstance;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;

/**
 * An image that renders a strip of small thumbnails that span
 * all the instances of a series. 
 * 
 * @author ssadedin
 */
public class WADOThumbStripImage extends WADOImage2 {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(WADOThumbStripImage.class);
    
    private MCSeries series;
    

    LocalFileRepository  localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();

    public WADOThumbStripImage(InputStream inputStream, int frameNumber, int columns, int rows, MCSeries series) throws IOException {
        super(inputStream, frameNumber, columns, rows);
        this.series = series;
    }

    @Override
    public void createImage(WADOParameterForm params) {
        try {            
            BufferedImage originalImage = extractImage(params);        
                
            long startTimeMs = System.currentTimeMillis();
            int totalFrames = reader.getNumImages(false);
            int height = reader.getHeight(0); 
            
            int numFrames = series.getInstances().size();
            this.setNumberOfFrames(numFrames);
            if(log.isInfoEnabled())
                log.info("Creating thumbstrip image (" + numFrames + " frames)");
            
            int imageType = originalImage.getType();
            if(imageType == BufferedImage.TYPE_CUSTOM) 
                imageType = BufferedImage.TYPE_INT_RGB; 
            
            BufferedImage sprite = 
                new BufferedImage(params.getMaxColumns(), (int) (numFrames*params.getMaxRows()), imageType);
            
            Graphics2D gfx = sprite.createGraphics();
            ImageObserver observer = new ImageObserver() {
              public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                  log.info("Notified of image change ?!");
                  return false;
              }
            };               
            
            int i=0;
            int numRenderedImages = 10; 
            int spacing = numFrames / numRenderedImages;
            BufferedImage img = null;
            for(MCInstance instance : series.getInstances()) {
                
                if(spacing == 0 || i % spacing == 0) {
                    InputStream inputStream = null;
                    try {
                        inputStream = localFileRepository.getDocument(params.getStorageId(), params.getMcGUID(),instance.getReferencedFileID());
                        this.imageInputStream =   ImageIO.createImageInputStream(inputStream);
                        reader.setInput(imageInputStream, false);
                        originalImage = extractImage(params);        
                    }
                    finally {
                        if(inputStream != null) {
                            closeQuietly(inputStream);
                            inputStream = null;
                        }
                    }
                    img = reader.read(params.getFrameNumber(),dicomParam);
                }
                gfx.drawImage(img, 0,i*params.getMaxRows(), params.getMaxColumns(), params.getMaxRows(), observer);
                ++i;
            }
            log.info("Created thumbstrip (" + numFrames + " frames) in " + (System.currentTimeMillis() - startTimeMs));
            image = sprite;
        }
        catch(IOException e){
            log.error("Error reading image ", e);
        }
        catch(RuntimeException e){
            log.error("Error reading image ", e); // TODO: Possible race condition if data is being retrieved from offline storage handler
        }
        catch (ServiceException e) {
            log.error("Error reading image ", e); // TODO: Possible race condition if data is being retrieved from offline storage handler
        }
    }
    
    
    
    public MCSeries getSeries() {
        return series;
    }

    public void setSeries(MCSeries series) {
        this.series = series;
    }

    @Override
    public void scaleImage(int interpolationType, int maxColumns, int maxRows,
                    String photometricInterpretation, boolean isFrameSprite) {
    }

    @Override
    public void renderTextOverlays(Font font, boolean patient, boolean localizers) {
    }
}
