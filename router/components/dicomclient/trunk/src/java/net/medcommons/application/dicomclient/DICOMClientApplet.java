package net.medcommons.application.dicomclient;




import javax.swing.JApplet;
import java.awt.*;
import javax.swing.*;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.utils.DicomFileChooser;
import net.medcommons.application.utils.AppletWindowUtilities;


public class DICOMClientApplet extends JApplet {
	
	private static Logger log = Logger.getLogger(DICOMClientApplet.class);
	public void init() {
		Configurations configurations = new Configurations();
		ContextManager contextManager = new ContextManager(configurations);
	        
	    AppletWindowUtilities.setNativeLookAndFeel();
	    Container content = getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout()); 
	    DicomFileChooser fileChooser = new DicomFileChooser(null, false);
	    
	    content.add(fileChooser);
	    content.add(new JButton("test button"));
	    log.info("Added file chooser");
	    validate();
	    
	    
	  }
	public void start() {
		log.info("Invoked start method");
		Container content = getContentPane();
		Component[] components = content.getComponents();
		log.info("There are " + components.length + " components");
        
    }

    public void stop() {
    	log.info("stopping... ");
    }

    public void destroy() {
    	log.info("preparing for unloading...");
    }

  
}