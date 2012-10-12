package net.medcommons.application.dicomclient;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.upload.State;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.application.upload.UploadContext;
import net.medcommons.application.utils.UploadFileChooser;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.utils.FormatUtils;

import org.apache.log4j.Logger;

/**
 * Top level DicomOnDemand dialog box.
 * 
 * @author mesozoic
 *
 */
public class UploadPopupWindow extends JFrame implements ActionListener,StatusUpdate{
	
	private static UploadPopupWindow singletonWindow= null;
	ContextState uploadContext = null;
	String patientFamilyName = null;
	String patientGivenName = null;
	String patientGender = null;
	String storageId = null;
	
	private String dicomOnDemandFile  = "toolbar/DicomOnDemand.png";
	private String medcommonsLogoFile   = "toolbar/mc_logo.gif";
	
	
	private final static String LOADCD  = "Load DICOM CD";
	private final static String LOADFOLDER = "Load DICOM Folder";
	private final static String UPDATECONFIG = "Update DICOM Configuration";
	
	private static Logger log = Logger.getLogger(UploadPopupWindow.class);
	
	JLabel medCommons;
	JButton loadDICOMCD;
	JButton loadDICOMFolder;
	JButton updateConfig;
	JLabel ipAddress; 
	JProgressBar progressBar; 
	JLabel rateText; 
	JLabel messageText;
	JCheckBox publicGroup;
	JLabel groupName;
	JLabel version;
	
	boolean initialized = false;
	boolean uploadError = false;
	ImageIcon dicomOnDemandIcon;
	ImageIcon medcommonsIcon;
	
	public UploadPopupWindow(){
		super("DICOM On Demand");
		init();
	}
	
	
		public void init() {
			ContextManager contextManager = ContextManager.get();
			Configurations configurations = contextManager.getConfigurations();
			contextManager.setStatusUpdate(this);
			Image dodImage = StatusDisplayManager.ImageLoader.loadImage(dicomOnDemandFile);
			Image mcImage = StatusDisplayManager.ImageLoader.loadImage(medcommonsLogoFile);
			dicomOnDemandIcon = new ImageIcon(dodImage);
			medcommonsIcon = new ImageIcon(mcImage);
			// Only one DicomOnDemand dialog visible at a time.
			try{
				try{
					if (singletonWindow != null){
						singletonWindow.setVisible(false);
						singletonWindow.dispose();
						singletonWindow = null;
					}
				}
				catch(Exception e){
					log.error("Error closing existing DoD window", e);
				}
				singletonWindow = this;
				
			}
			catch(Exception e){
				log.error("Exception closing existing window", e);
			}
			String hostAddress = "UNKNOWN";
			try{
				InetAddress addr = InetAddress.getLocalHost();
				hostAddress = addr.getHostAddress();
			}
			catch(Exception e){
				log.error("Error retrieving localhost address", e);
			}
			
			JLabel dodTitle = new JLabel(dicomOnDemandIcon, SwingConstants.RIGHT);
			//dodTitle.setFont(font);
			
			ipAddress = new JLabel(hostAddress);
			
			loadDICOMCD = new JButton(LOADCD);
			loadDICOMCD.setToolTipText("Click to upload a DICOM CD to the DICOM on Demand Server");
			loadDICOMFolder = new JButton(LOADFOLDER);
			loadDICOMFolder.setToolTipText("Click to select and upload a folder of DICOM images \n" +
					" to the DICOM on Demand Server");
		
			updateConfig = new JButton(UPDATECONFIG);
			updateConfig.setToolTipText("Update DICOM configurations for receiving DICOM associations");
			
			loadDICOMCD.addActionListener(this);
			loadDICOMFolder.addActionListener(this);
			updateConfig.addActionListener(this);
			
			progressBar = new JProgressBar(0, 100);
			progressBar.setStringPainted(true);
			rateText = new JLabel("Transfer Rate   ");
			messageText = new JLabel("            ");
			publicGroup = new JCheckBox("Public Access", true);
			groupName = new JLabel("Group:");
			
			version = new JLabel("Rev: " + configurations.getVersion());
			
			this.setBackground(Color.white);
			Container content = this.getContentPane();
		    content.setBackground(Color.white);

		        
		    
		    Object localData[][] = { 
		    		{ "Local DICOM", ""},
		    		{ "IP Address", hostAddress},
                    { "AE Title", configurations.getDicomLocalAeTitle()},
                    { "Port", configurations.getDicomLocalPort()} 
		    		};
		    Object remoteData[][] = { 
		    		{ "Remote DICOM", ""},
		    		{ "Hostname", configurations.getDicomRemoteHost()},
                    { "AE Title", configurations.getDicomRemoteAeTitle()},
                    { "Port", configurations.getDicomRemotePort()} 
		    		};
		    Object columnNames []= {"Name", "Value"};
		    JTable localDicomtable = new JTable(localData,columnNames);
		    JTable remoteDicomtable = new JTable(remoteData,columnNames);
		    localDicomtable.validate();
		    remoteDicomtable.validate();
		    
		   
		    
			try{
	    		uploadContext = contextManager.getCurrentContextState();
	    		
	    		groupName.setText("Group: " + uploadContext.getGroupName());
	    		medCommons = new JLabel(medcommonsIcon, SwingConstants.RIGHT);
	    		//JLabel title = new JLabel(caption, icon, SwingConstants.RIGHT);
	    	   
	    	    setLayout(new GridBagLayout()); 
	    	    GridBagConstraints c = new GridBagConstraints();
	    	    c.fill = GridBagConstraints.HORIZONTAL;
	    	    int row = 0;
	    	    
	    	    c.gridx = 0; c.gridy = row; c.gridwidth = 1;
	    	    add(dodTitle, c);
	    	    c.gridx = 3; c.gridy = row; c.gridwidth = 1;
	    	    add(medCommons, c);
	    	    row++;
	    	   
	    	    c.gridx = 0; c.gridy = row; c.gridwidth = 1;
	    	    add(loadDICOMCD, c);
	    	  
	    	    c.gridx = 3; c.gridy = row; c.gridwidth = 1;
	    	    add(loadDICOMFolder, c);
	    	    row++;
	    	    row++;
	    	    
	    	    c.gridx = 0; c.gridy = row; c.gridwidth = 1;
	    	    add(localDicomtable,c);
 	    
	    	    c.gridx = 2; c.gridy = row; c.gridwidth = 1;
	    	    add(remoteDicomtable,c);
  
	    	    c.gridx = 3; c.gridy = row; c.gridwidth = 1;
	    	    add(updateConfig, c);
	    	    row++;
	    	    
	    	   
	    	    c.gridx = 0; c.gridy = row; c.gridwidth = 3;
	    	    add(progressBar, c);
	    	    
	    	    row++;
	    	    c.gridx = 0; c.gridy = row; c.gridwidth = 3;
	    	    add(rateText, c);
	    	    
	    	    row++;
	    	    c.gridx = 0; c.gridy = row; c.gridwidth = 3;
	    	    add(messageText, c);
	    	    
	    	    
	    	    row++;
	    	    c.gridx = 0; c.gridy = row; c.gridwidth =1;
	    	    add(groupName,c);
	    	    c.gridx = 3; c.gridy = row; c.gridwidth =1;
	    	    
	    	    Panel p = new Panel();
	    	    p.setLayout(new FlowLayout());
	    	    p.add(publicGroup);
	    	    publicGroup.setBackground(Color.WHITE);
	    	    p.add(version);
	    	    // add(publicGroup,c);
	    	    add(p,c);
	    	    validate();
	    	    
	    	    
	    	    validate();
	    	    pack();
	    	    initialized = true;
			}
			catch(Exception e){
			   
			    log.error("Initialization failure ", e);
			}
		    
		    
		  }
		
		public void actionPerformed(ActionEvent e) {
			log.info("event " + e.getActionCommand() + ":" + e.getSource());
			String command = e.getActionCommand();
			if (LOADFOLDER.equals(command)){
				StatusDisplayManager.createAndShowFileChooser();
				//FolderUploadThread uploadThread = new FolderUploadThread(this);
				//new Thread(uploadThread).start();
				updateMessage("Selecting DICOM Folder");
			}
			else if (LOADCD.equals(command)){
				//CDUploadThread uploadThread = new CDUploadThread(this);
				//new Thread(uploadThread).start();
				StatusDisplayManager.createAndShowCDChooser();
				updateMessage("Selecting DICOM CD");
			}
			else if (UPDATECONFIG.equals(command)){
				
				StatusDisplayManager.showConfigure();
			}
		}
		
	    public void updateProgress(long byteCount, long totalBytes){
	    	
	        int progress = 0;
	        if (totalBytes > 0){
	            double percent = (byteCount * 100.0)/(totalBytes * 1.0);
	            if (log.isDebugEnabled())
	            	log.debug("updateProgress: " + byteCount + ", total=" + totalBytes + ", " + percent + "%");
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
	    	if (log.isDebugEnabled())
	    		log.debug("Message:" + message);
	        messageText.setText(message);
	    }
	    public void  updateState(State state){
	        if (log.isDebugEnabled())
	        	log.debug("updateAppletState:" + state.getDisplayName());
	        
	    	
	    	validate();
	    }
	    
	    private class FolderUploadThread implements Runnable{
	    	UploadPopupWindow parent = null;
	        private FolderUploadThread(UploadPopupWindow parent){
	            this.parent = parent;
	        }
	        public void run(){
	        	UploadContext context = new UploadContext(uploadContext.getCXPEndpoint(), 
	        			null, uploadContext.getStorageId(), uploadContext.getAuth(), 
	        			uploadContext.getAccountId(),null, CXPConstants.MergeCCRValues.ALL);
	            UploadFileChooser fileChooser = new UploadFileChooser(context, false, this.parent);
	            
	            add(fileChooser);
	            
	        }
	    }
	    private class CDUploadThread implements Runnable{
	    	UploadPopupWindow parent = null;
	        private CDUploadThread(UploadPopupWindow parent){
	            this.parent = parent;
	        }
	        public void run(){
	        	UploadContext context = new UploadContext(uploadContext.getCXPEndpoint(), 
	        			null, uploadContext.getStorageId(), uploadContext.getAuth(), 
	        			uploadContext.getAccountId(),null, CXPConstants.MergeCCRValues.ALL);
	            UploadFileChooser fileChooser = new UploadFileChooser(context, true, this.parent);
	            
	            add(fileChooser);
	           
	        }
	    }
	}


