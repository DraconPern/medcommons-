package net.medcommons.application.dicomclient.utils;

import java.awt.*;
import java.io.IOException;

import net.medcommons.modules.utils.FileUtils;

import org.apache.log4j.Logger;

/**
 * Utility to load correct sized images for fitting in the notification area / system tray
 * 
 * @author ssadedin
 */
public class TrayImages {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(TrayImages.class);
    
    public Image idleImage = null;
    public Image activeImage = null;
	public Image errorImage = null;
    
    public Image [] activeImages = null;
    
    public TrayIcon trayIcon;
    
    public TrayImages(String tooltip, PopupMenu popup) {
       this.trayIcon = createIcon(tooltip, popup); 
    }
    
    /**
     * Create a tray icon configured to display nicely on the local system
     * @param tooltip
     * @param popup
     * @return
     */
    public TrayIcon createIcon(String tooltip, PopupMenu popup) {
        idleImage = loadImage("toolbar/Inactive.png");
        trayIcon = new TrayIcon(idleImage, tooltip, popup);
        Dimension size = trayIcon.getSize();
        int width = (int) Math.round(size.getWidth());
        if(width>=20) {
            log.info("Using full size tray icons");
            
            // idle image loaded above
            activeImage = loadImage("toolbar/Active.png");
            errorImage = loadImage("toolbar/Error.png");
            activeImages = new Image[] {
                            loadImage("toolbar/Active_1.png"), 
                            loadImage("toolbar/Active_2.png"),
                            loadImage("toolbar/Active_3.png")
            };
        }
        else {
            log.info("Using 16x16 size tray icons");
            this.idleImage = loadImage("toolbar/16x16/Inactive.png");
            this.activeImage = loadImage("toolbar/16x16/Active.png");
            this.errorImage = loadImage("toolbar/16x16/Error.png");
            this.activeImages = new Image[] {
                            loadImage("toolbar/16x16/Active_1.png"), 
                            loadImage("toolbar/16x16/Active_2.png"),
                            loadImage("toolbar/16x16/Active_3.png")
            }; 
        }
        
        if(width>20 || width < 16) {
            log.info("Using tray icon auto size");
            trayIcon.setImageAutoSize(true);
        }
        
        trayIcon.setImage(idleImage);
        return trayIcon;
    }
    
    public static java.awt.Image loadImage(final String imageName) {
        final java.lang.ClassLoader loader = TrayImages.class.getClassLoader();
        java.awt.Image image  = null;
        java.io.InputStream is = (java.io.InputStream)
        java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction() {
                            public Object run() {
                                if (loader != null) {
                                    return loader.getResourceAsStream(imageName);
                                } else {
                                    return ClassLoader.getSystemResourceAsStream(imageName);
                                }
                            }
                        });
        if (is != null) {
            try {
                byte[] imageData = FileUtils.readBytes(is);
                image = java.awt.Toolkit.getDefaultToolkit().createImage(imageData);
            }
            catch (IOException e) {
                log.warn("Failed to read icon image data from stream: " + e.toString());
            }
        }
        return image;
    }
}
