package net.medcommons.application.upload;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.utils.FormatUtils;
import net.medcommons.application.utils.AppletParameters;
import net.medcommons.application.utils.AppletWindowUtilities;
import net.medcommons.application.utils.UploadFileChooser;
import net.medcommons.modules.cxp.CXPConstants;

import org.apache.log4j.Logger;


public class UploadAccountFilesApplet extends JApplet implements AppletParameters,StatusUpdate{
	UploadContext uploadContext = null;
	String patientFamilyName = null;
	String patientGivenName = null;
	String patientGender = null;
	String storageId = null;
	
	private static Logger log = Logger.getLogger(UploadAccountFilesApplet.class);
	JTextArea stateText; 
	JTextArea messageText; 
	JLabel patientDemographics;
	JProgressBar progressBar; 
	JTextArea rateText; 
	boolean initialized = false;
	boolean uploadError = false;
	public void init() {
		//JTextField titleText = new JTextField("Upload File Status:");
		stateText = new JTextArea(State.INITIALIZING.displayName);
		messageText = new JTextArea("Initializing", 4, 500);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		rateText = new JTextArea(" Rate   ");
		Configurations configurations = new Configurations();
		ContextManager contextManager = new ContextManager(configurations);
	        
		try{
    		uploadContext = parseParameters();
    		
    		patientDemographics = new JLabel(getDemographics() );
    		
    	    AppletWindowUtilities.setNativeLookAndFeel();
    	    //Container content = getContentPane();
    	    //content.setBackground(Color.white);
    	    setLayout(new GridBagLayout()); 
    	    GridBagConstraints c = new GridBagConstraints();
    	    c.fill = GridBagConstraints.HORIZONTAL;
    	    int row = 0;
    	    c.gridx = 0; c.gridy = row; c.gridwidth = 3;
    	    add(patientDemographics, c);
    	    
    	    row++;
    	    c.gridx = 1; c.gridy = row; c.gridwidth = 1;
    	    add(stateText, c);
    	    row++;
    	    c.gridx = 1; c.gridy = row; c.gridwidth = 2;
    	    add(messageText, c);
    	    row++;
    	    c.gridx = 0; c.gridy = row; c.gridwidth = 3;
    	    add(progressBar, c);
    	    row++;
    	    c.gridx = 0; c.gridy = row; c.gridwidth = 3;
    	    add(rateText, c);
    	    validate();
    	    
    	    
    	    validate();
    	    initialized = true;
		}
		catch(Exception e){
		    stateText.setText(e.getLocalizedMessage());
		    log.error("Initialization failure ", e);
		}
	    
	    
	  }
	public void start() {
	    super.start();
	    if (initialized){
    		showStatus("Starting..");
    		
    		FileUploadThread fileUploadThread = new FileUploadThread(this);
    		new Thread(fileUploadThread).start();
    		Container content = getContentPane();
    		Component[] components = content.getComponents();
    		log.info("There are " + components.length + " components");
    		stateText.setText(State.UPLOADING.getDisplayName());
	    }
	    else{
	        log.info("Applet not started; failed initialization");
	    }
    }

    public void stop() {
        super.stop();
    	log.info("stopping... ");
    }

    public void destroy() {
    	log.info("preparing for unloading...");
    }
    
    private String getDemographics(){
        String demographics = patientFamilyName + "," + patientGivenName;
        if (patientGender != null)
            demographics += " Gender:" + patientGender;
        demographics += " MedCommons Account " + storageId;
        return(demographics);
    }

    protected UploadContext parseParameters(){
    	storageId = getParameter(StorageId);
    	String authToken = getParameter(AuthToken);
    	String senderId = getParameter(SenderId);
    	String cxpEndpoint = getParameter(CXPEndpoint);
    	String paymentBypassToken = getParameter(PaymentBypassToken);
    	patientFamilyName = getParameter(PatientFamilyName);
    	patientGivenName = getParameter(PatientGivenName);
    	patientGender = getParameter(PatientGender);
    	CXPConstants.MergeCCRValues mergeCCR =  CXPConstants.MergeCCRValues.valueOf(getParameter(MergeCCR));
    	if (storageId == null){
    	    throw new IllegalArgumentException("Null storageId");
    	}
    	if (authToken == null){
    	    throw new IllegalArgumentException("Null auth token");
    	}
    	if (cxpEndpoint == null){
    	    throw new IllegalArgumentException("Null cxpEndpoint");
    	}
    	
    	    
    	UploadContext uploadContext = new UploadContext(cxpEndpoint, null, storageId, authToken, senderId,paymentBypassToken, mergeCCR);
    	return(uploadContext);
    }
    public void  updateState(State state){
        log.info("updateAppletState:" + state.displayName);
        
    	stateText.setText(state.getDisplayName());
    	if (state.equals(State.FAILED)){
    	    messageText.setForeground(Color.RED);
    	    messageText.setBackground(Color.WHITE);
    	    uploadError = true;
    	}
    	else if (state.equals(State.FINISHED)){
    		messageText.setText("Upload complete: please choose one CCR as your new Current CCR");
    	}
    	validate();
    }
    public void updateProgress(long byteCount, long totalBytes){
    	
        int progress = 0;
        if (totalBytes > 0){
            double percent = (byteCount * 100.0)/(totalBytes * 1.0);
            log.info("updateProgress: " + byteCount + ", total=" + totalBytes + ", " + percent + "%");
            if (percent > 100.0){
                percent = 100.0;
            }
            progress = (int) Math.round(percent);
            //progress = FormatUtils.formatNumberTenths(percent);
        }
        
        if (!uploadError)
            progressBar.setValue(progress);
        else
            progressBar.setValue(0);
       
    }
    public void updateRate(long byteCount, long elapsedTime){
        String rate = FormatUtils.formatKbPerSecond(byteCount, elapsedTime);
        rateText.setText(rate + " KB/second");
    }
    public void updateMessage(String message){
        messageText.setText(message);
    }
    
    private class FileUploadThread implements Runnable{
        UploadAccountFilesApplet parent = null;
        private FileUploadThread(UploadAccountFilesApplet parent){
            this.parent = parent;
        }
        public void run(){
            UploadFileChooser fileChooser = new UploadFileChooser(uploadContext, false, this.parent);
            
            add(fileChooser);
            log.info("Added file chooser");
        }
    }
  
}
