/*
 * $Id$
 * Created on 29/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import net.medcommons.router.services.dicom.util.MCInstance;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.NotLoggedInException;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;

/**
 * Returns a scaled image thumbnail of a series at the requested size.
 * 
 * @author ssadedin
 */
public class ImageThumbAction extends CCRActionBean {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ImageThumbAction.class);
    
    /**
     * Width of image to be rendered
     */
    @Validate(required=true,minvalue=1, maxvalue=1600)
    private int width = 0;
    
    /**
     * Height of image to be rendered
     */
    @Validate(required=true,minvalue=1, maxvalue=1600)
    private int height = 0;
    
    /**
     * The series to create the thumbnail for 
     */
    @Validate(required=true,minvalue=0)
    private int seriesIndex = 0; 
    
    public ImageThumbAction() {
    }
    
    @DefaultHandler
    public Resolution render() throws IOException, RepositoryException, ServiceException, NotLoggedInException {
    	InputStream in = null;
        if(session == null)
            throw new NotLoggedInException();
       
        if(seriesIndex >= ccr.getSeriesList().size())
            throw new IllegalArgumentException("Series index " + seriesIndex + " is out of range");
        
        // Try and find the series
        MCSeries series = ccr.getSeriesList().get(seriesIndex);
         
        // Thumbnail instance is always the first one for the content types we are dealing with
        MCInstance instance = series.getInstance(0);
        String guid = instance.getSOPInstanceUID();

        String localStorageId  = null;
        
        LocalFileRepository  localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();       
        try{
        	localStorageId = series.getStorageId();
        	
        	if (localStorageId == null){
        		throw new NullPointerException("Null storage id with guid " +guid);
        	}
        	in = localFileRepository.getDocument(localStorageId,guid);
        	
        }
        catch(TransactionException e){
        	log.error("Unable to read file for series", e);
        	throw new ServiceException("Unable to read file for " + localStorageId + ", " + guid, e);
        }
        BufferedImage img = ImageIO.read(in);
        
        // We want to maintain the aspect ratio, so choose the smallest of the two axes
        double xScale = (double)width/img.getWidth();
        double yScale = (double)height/img.getHeight();
        double scale = Math.min(xScale,yScale);        
        
        log.info("Scaling thumb image for series " + seriesIndex + " to " + scale + " original");
        
        BufferedImage scaled  =
            new BufferedImage((int)(img.getWidth()*scale), (int)(img.getHeight()*scale), BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = scaled.createGraphics();
        //AffineTransform at =
        //   AffineTransform.getScaleInstance(scale,scale);
        g.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g.drawRenderedImage(img,at);
                        g.drawImage(img, 0, 0, scaled.getWidth(), scaled.getHeight(), null );
        
        this.ctx.getResponse().setContentType("image/jpeg");
        ImageIO.write(scaled, "jpg", this.ctx.getResponse().getOutputStream());        
        return new Resolution() {
            public void execute(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
            } 
        };
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(int seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
