/**
 * 
 */
package net.medcommons.application.dicomclient.utils;

import static java.lang.Thread.sleep;

import java.awt.*;
import java.lang.reflect.Method;

import javax.swing.*;

import org.apache.log4j.Logger;

public class ToolTipReplacementFrame implements Runnable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ToolTipReplacementFrame.class);

    long closeTime;
	JFrame frame = null;
	long nMsec;
	long startTime;

	static boolean tryTransparency = true;
	    
	JLabel title = null;
	JLabel label =  null;
	
	public ToolTipReplacementFrame(long nMsec, String caption, String message, Image image){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.nMsec = nMsec;
		frame = new JFrame("Frame With No Title Bar");
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setSize(400,400);
		ImageIcon icon = new ImageIcon(image);
		label = new JLabel(message);
		label.setSize(label.getPreferredSize());
		
		title = new JLabel(caption, icon, SwingConstants.RIGHT);
		title.setSize(title.getPreferredSize());
		Container container =frame.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		container.add(title);
		container.add(label);
		frame.pack();
		frame.setVisible(false);
	}
	
	public void run() {
	    show();
	}

    public void show() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int frameWidth;
            int frameHeight = 40;
            int maxWidth = Math.max(title.getWidth(), label.getWidth());
            frameWidth = maxWidth + 20;
            frame.setBounds(screenSize.width - frameWidth - 40, frameHeight, frameWidth, frameHeight);
            frame.setVisible(false);
            frame.toFront();
            frame.requestFocus();
    		startTime = System.currentTimeMillis();
    		closeTime = startTime + nMsec;
    		
    		long fadeInTimeMs = 3000;
	        while(System.currentTimeMillis() < startTime + fadeInTimeMs){
	            try{Thread.sleep(100);}
	            catch(InterruptedException e){;}
	            if (tryTransparency) {
	                try {
	                    long currentTime = System.currentTimeMillis();
	                    long diffTime = currentTime - startTime;
	                    if (diffTime < 0) diffTime = 0;
	                    float opacity = Math.min((diffTime * 1.0f)/(fadeInTimeMs),1.0f);
	                    Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
	                    Method mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
	                    mSetWindowOpacity.invoke(null, frame, Float.valueOf(opacity));
	                    if(!frame.isVisible())
	                        frame.setVisible(true);
	                }
	                catch(ClassNotFoundException e){
	                    log.info(e.getMessage() + ", This JVM apparently can't set window opacity."
	                            + "\n will use standard JFrames instead."
	                    );
	                    tryTransparency = false;
	                }
	                catch(Exception e){
	                    log.error("Error setting window opacity",e);
	                    tryTransparency = false;
	                }
	            }
	        }
	        
	        if(!frame.isVisible())
	            frame.setVisible(true);
	        
	        try { sleep(closeTime - System.currentTimeMillis()); } catch (InterruptedException e) { }
	        
	    }
	    finally{
	        frame.setVisible(false);
	        frame.dispose();
	        frame = null;
	    }
    }
	
}