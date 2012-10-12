package net.medcommons.router.selftests;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

/**
 * Simple test to see if the java library is correctly installed.
 * ImageIO can fail if the java.awt.headless mode isn't set to true.
 * @author sean
 *
 */
public class JavaImageIOInstallation implements SelfTest{
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(JavaImageIOInstallation.class);
    
    public SelfTestResult execute(ServicesFactory services) throws Exception {

        File aJPEGFile = new File("webapps/router/SelfTestImage.jpg");
        assert (aJPEGFile.exists()) : "File missing for self-test:" + aJPEGFile.getAbsolutePath();
       
        FileInputStream in = new FileInputStream(aJPEGFile);
        BufferedImage aPhoto = ImageIO.read(in); // Will throw error if ImageIO library not correctly installed.
        Graphics2D g2d = aPhoto.createGraphics();
        
        return null;
    }
}
