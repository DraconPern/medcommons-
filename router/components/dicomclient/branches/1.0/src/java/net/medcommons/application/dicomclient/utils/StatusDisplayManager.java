package net.medcommons.application.dicomclient.utils;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.trunc;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jnlp.*;
import javax.swing.*;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.http.utils.Voucher;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.utils.DashboardMessageGenerator;
import net.medcommons.application.utils.JSONSimpleGET;
import net.medcommons.application.utils.DashboardMessageGenerator.MessageType;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;

/**
 * Displays status in the system tray if possible; otherwise
 * writes messages to log.
 * @author mesozoic
 *
 */
public class StatusDisplayManager {
    
	private static Logger log = Logger.getLogger(StatusDisplayManager.class);
	
	private static String startupTooltip = "DICOM Data Liberator";
	
	/**
	 * Some static (compile time) options that enable certain features.
	 * For now they are turned off but we might want them back one day.
	 */
	private static Map<String,Boolean> OPTIONS = new HashMap<String, Boolean>() {{
	    put("SHOW_UPLOAD_DICOM_FOLDER", false);
	    put("SHOW_UPLOAD_DICOM_CD", false);
	}};

	private String errorImageFile  = "toolbar/Error.png";
	private String idleImageFile   = "toolbar/Inactive.png";
	public String activeImageFile = "toolbar/Active.png";

	private static Image idleImage = null;
	public static Image activeImage = null;
	private static Image errorImage = null;

	static TrayIcon trayIcon = null;

	SystemTray tray = null;

	private  String currentWorklist = null;
	private  String currentServer = null;
	private  String currentToolTip = null;
	private static boolean traySupported = false;
	private static boolean useSytemTrayPopups = true; // Even if the system supports the system tray we may want to ignore it.
	
	private static String operatingSystem =  System.getProperty("os.name");

	private static StatusDisplayManager sdm = null;
	
	private long popupDialogDisplayMsec = 8*1000;
	
	
	public static boolean testMode = false;

    private PopupMenu popup;
    
    /**
     * A queue used to implement the system tray notification replacements
     * in an orderly manner.
     */
    private NotificationWindowQueue notificationQueue = new NotificationWindowQueue();

	public StatusDisplayManager() {
		sdm = this;
		
		if(testMode)
		    return;
		

		final String configureDDL = "Configure DDL";
		final String showStatus = "Show Status";
		final String clearStatus = "Clear Status";
		final String shutdown = "Shutdown";
		final String uploadFolder = "Upload DICOM Folder";
		final String uploadCD = "Upload CD";
		final String poller = "Poller";
		final String stopPoller = "Stop";
		
		
		//		 get the StatusDisplayManager instance

        idleImage = ImageLoader.loadImage(idleImageFile);
        activeImage = ImageLoader.loadImage(activeImageFile);
        errorImage = ImageLoader.loadImage(errorImageFile);

		if(ContextUtils.isJDK6Orlater() && SystemTray.isSupported()) {
			try {
				String os = operatingSystem.toUpperCase();
				log.info("==Operating system is " + os);
				if ((os.indexOf("OSX")!= -1) || (os.indexOf("DARWIN") != -1) || (os.indexOf("OS X") != -1)){
					useSytemTrayPopups = false;
				}
				tray = SystemTray.getSystemTray();
				traySupported = true;
				log.info("System tray is supported");
			}
			catch(UnsupportedOperationException e) {
				log.error("System tray not available", e);
			}
		}
		else {
		    useSytemTrayPopups = false;
			log.error("System tray not supported on this machine");
		}
			/*
			 * Create a action listener to listen for default action executed on
			 * the tray icon.
			 *
			 */

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try{
					
					if (e== null){
						log.error("Action event is null - why?. Ignoring event");
						return;
					}
					else if (e.getActionCommand() == null){
						log.error("Action event " + e + " has null action command - why?. Ignoring event");
						return;
					}
					log.info("actionEvent:" + e.getActionCommand() + " :" + e);
					if (e.getActionCommand().equals(showStatus)){

						URL url = new URL("http://localhost:16092/localDDL/status.html");
						StatusDisplayManager.showDocument(url);

					}
					else if (e.getActionCommand().equals(configureDDL)){
						StatusDisplayManager.showConfigure();

					}
					else if (e.getActionCommand().equals(clearStatus)){
						log.info("Clearing status");
						resetToIdle();
					}
					else if (e.getActionCommand().equals(uploadFolder)){
						SwingUtilities.invokeLater(new Runnable() {
							 public void run() {
								 //Turn off metal's use of bold fonts
								 UIManager.put("swing.boldMetal", Boolean.FALSE);
							 	createAndShowFileChooser();
							 }
						});


					}
					else if (e.getActionCommand().equals(uploadCD)){
						SwingUtilities.invokeLater(new Runnable() {
							 public void run() {
								 //Turn off metal's use of bold fonts
								 UIManager.put("swing.boldMetal", Boolean.FALSE);
								 createAndShowCDChooser();
							 }
							 });


					}


					else if (e.getActionCommand().equals(shutdown)){
						try{
							Shutdown.cleanup();
						}
						finally{
							System.exit(0);
						}

					}
					}
					catch(Exception err){
						log.error("Error handling menu event", err);
					}
				}
			};
			  MouseListener ml = new MouseListener ()
                   {
                       public void mouseClicked (MouseEvent e)
                       {
                          System.out.println ("Tray icon: Mouse clicked");
                       }

                       public void mouseEntered (MouseEvent e)
                       {
                          System.out.println ("Tray icon: Mouse entered");
                       }

                       public void mouseExited (MouseEvent e)
                       {
                          System.out.println ("Tray icon: Mouse exited");
                       }

                       public void mousePressed (MouseEvent e)
                       {
                          System.out.println ("Tray icon: Mouse pressed");
                       }

                       public void mouseReleased (MouseEvent e)
                       {
                          System.out.println ("Tray icon: Mouse released");
                       }
                   };
             
			popup = new PopupMenu("DDL");
			
			// create menu item for the default action
			MenuItem defaultItem = new MenuItem(configureDDL);
			MenuItem showStatusItem = new MenuItem(showStatus);


			MenuItem clearStatusItem = new MenuItem(clearStatus);
			MenuItem tools = new MenuItem("Tools");
			MenuItem shutDown = new MenuItem(shutdown);

			MenuItem uploadFolderItem = new MenuItem(uploadFolder);
			MenuItem uploadCDItem = new MenuItem(uploadCD);


			defaultItem.addActionListener(listener);
			popup.add(defaultItem);

			popup.addSeparator();
			showStatusItem.addActionListener(listener);
			popup.add(showStatusItem);

			if(OPTIONS.get("SHOW_UPLOAD_DICOM_FOLDER"))
    			popup.add(uploadFolderItem);
			
			uploadFolderItem.addActionListener(listener);
			
			if(OPTIONS.get("SHOW_UPLOAD_DICOM_CD"))
    			popup.add(uploadCDItem);
			
			uploadCDItem.addActionListener(listener);
			
			clearStatusItem.addActionListener(listener);
			//clearStatusItem.setEnabled(false);
			popup.addSeparator();
			popup.add(clearStatusItem);
			
			popup.addSeparator();
			shutDown.addActionListener(listener);
			shutDown.setEnabled(true);
			popup.add(shutDown);
			tools.addActionListener(listener);

			
			if (traySupported){
				// / ... add other items
				// construct a TrayIcon
				trayIcon = new TrayIcon(idleImage, startupTooltip,
						popup);
	
				trayIcon.setImageAutoSize(true);
	
				// set the TrayIcon properties
				trayIcon.addActionListener(listener);
				trayIcon.addMouseListener(ml);
	
				// ...
				// add the tray image
				try {
					tray.add(trayIcon);
					setIdleIcon();
				} catch (AWTException e) {
					log.error("Error adding system tray icon", e);
	
					return;
				}
				catch(IllegalArgumentException e){
					log.error("Error adding system tray icon", e);
					return;
				}
				setToolTip(startupTooltip);
				PropertyChangeListener [] listeners = tray.getPropertyChangeListeners("DDL");
				if (listeners != null){
					log.info("Property change listenrers for DDL:" + listeners.length);
					for (int i=0;i<listeners.length;i++){
						log.info("listener + " + i + " is " + listeners[i]);
					}
				}
				else{
					log.info("Null listeners");
				}
				
			}
		
			ContextManager.get().addListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					log.info("Change notification for " + evt.getPropertyName());
					if("currentContextState".equals(evt.getPropertyName())) {
						ContextState newCtx = (ContextState)evt.getNewValue();
                        sendPrompt((ContextState)evt.getOldValue(), newCtx);
						setCurrentWorklist(newCtx.getCxpHost(), newCtx.getGroupName());
					}
				}
			});
	}
	
	/**
	 * The most recent patient uploaded is displayed in a menu
	 */
	ArrayList<VoucherMenu> patientMenus = new ArrayList<VoucherMenu>();
    public VoucherMenu addPatientMenu(ContextState state, Voucher voucher) {
        VoucherMenu patientMenu = new VoucherMenu(state, voucher);
        popup.insert(patientMenu, 0);
        patientMenus.add(patientMenu);
        return patientMenu;
 	}

    Menu pollerMenu = null;
    public void addPollerMenu(ContextState ctx) {
        
        if(pollerMenu != null) {
            popup.remove(pollerMenu);
            pollerMenu = null;
        }
        
        pollerMenu = new Menu("Poller - " + ctx.getGroupName());
        
        popup.insert(pollerMenu, popup.getItemCount()-1);
        
        
        final CheckboxMenuItem pauseMenu = new CheckboxMenuItem("Pause");
        pollerMenu.add(pauseMenu);
        pauseMenu.setState(PollGroupCommand.getRunning().getPause());  
        pauseMenu.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                PollGroupCommand poller = PollGroupCommand.getRunning();
                if(poller == null)
                    return;
                
                poller.setPause(!poller.getPause());
                pauseMenu.setState(poller.getPause());
                poller.save();
                setToolTip(null);
            }
        });
        
        MenuItem stopMenu = new MenuItem("Stop");
        pollerMenu.add(stopMenu);
        stopMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equals("Stop")) {
                    
                    final PollGroupCommand running = PollGroupCommand.getRunning();
                    if(running != null)  {
                        
                        int result = JOptionPane.showConfirmDialog(null,
                                "You have chosen to stop your poller for the " + running.getCtx().getGroupName() + " Group.\n\n" +
                                "If you wish to restart it you will need to login to your group and use the link on the settings page.\n\n" +
                                "Choose 'Yes' to stop your poller or 'No' to cancel and leave it running.",
                                "Stopping Group Poller", JOptionPane.YES_NO_OPTION); 
                        if(result == JOptionPane.NO_OPTION)
                            return;
                    }
                    
                    popup.remove(pollerMenu);
                    pollerMenu = null;
                    
                    
                    PollGroupCommand.getConfigFile().delete();
                    if(running != null) {
                        setMessage("Stopping Group Poller", "Sending stop request to poller. Shutdown may take a moment. Your DDL will keep running.");
                        new Thread() {
                            public void run() {
                                running.setStop(true);
                                int count = 0;
                                while(PollGroupCommand.getRunning() != null) {
                                    try { Thread.sleep(2000); } catch (InterruptedException e1) {  }
                                    if(count++>100) {
                                        log.info("Failed to shut down group poller.");
                                        get().setMessage("Unable to Stop Group Poller", "The Poller did not response to the stop request.  Please restart your DDL.");
                                        break;
                                    }
                                }
                                
                            }
                        }.start();
                    }
                    
                    
                    setToolTip(null);
                }
            }
        });
        
        final StartOnLoginHelper sol = new StartOnLoginHelper();
        if(sol.isStartupSupported()) {
            final CheckboxMenuItem runOnLogin = new CheckboxMenuItem("Run at Startup");
            pollerMenu.add(runOnLogin);
            runOnLogin.setState(sol.isStartupEnabled());
            runOnLogin.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean isEnabled = sol.isStartupEnabled();
                    if(isEnabled) {
                        sol.disableStartOnLogin();
                        runOnLogin.setState(false);
                    }
                    else {
                        sol.enableStartOnLogin();
                        runOnLogin.setState(true);
                    }
                }
            });
        }
            
        this.setToolTip(this.currentToolTip);
    }
	
	public void sendPrompt(ContextState oldCtx, ContextState newCtx) {
		if(!Str.eq(oldCtx.getCxpHost(), newCtx.getCxpHost()) && !blank(newCtx.getCxpHost())) {
			log.info("New upload host set - signalling dashboard");
			sdm.sendDashboardMessage(DashboardMessageGenerator.MessageType.PROMPT, null, 
					"DDL " + ContextManager.get().getConfigurations().getDicomLocalAeTitle() + 
					"  Ready");
		}
	}

	public static void showConfigure(){
		if (sdm == null)
			return;
		String configureURL = "http://localhost:16092/localDDL/configure.html";
		try{
			URL url = new URL(configureURL);
			StatusDisplayManager.showDocument(url);
		}
		catch(MalformedURLException e){
			StatusDisplayManager.DisplayModalError("Error showing configuration box", e.getLocalizedMessage(), true);

		}
	}
	
	/**
	 * Set the current tooltip displayed, including default contextual information.
	 * 
	 * @param toolTip
	 */
	public void setToolTip(String toolTip) {
		if (!traySupported) return;
		
		// Allow null to be passed in which case we do not change the current Tooltip,
		// we just update other info.
		if(toolTip != null) 
    		this.currentToolTip = toolTip;
		
		if (SystemTray.isSupported()) {
		    
		    // Note we have to be economical with the size of tooltip text here 
		    // because there seems to be limit after which windows just truncates it
		    java.util.List<String> lines  = new ArrayList<String>();
			if(currentServer != null) 
				lines.add("Server: " + currentServer);
			
			if(currentWorklist != null)
				lines.add("Worklist: " + Str.trunc(currentWorklist,20));
			else
				lines.add("No worklist set"); 
			
			PollGroupCommand pgc = PollGroupCommand.getRunning(); 
			
            if(pgc != null && pgc.getCtx() != null) { 
                if(pgc.isPause()) 
    			    lines.add(trunc("Polling Paused - " + pgc.getCtx().getGroupName() + " on " + pgc.getCtx().getCxpHost(), 45));
                else 
    			    lines.add(trunc("Polling " + pgc.getCtx().getGroupName() + " on " + pgc.getCtx().getCxpHost(),45));
            }
			
			lines.add(this.currentToolTip);
			trayIcon.setToolTip(Str.join(lines, "\n"));
		}
	}

	
	public static void setImage(Image image) {
		if (!traySupported) return;
		if (trayIcon != null)
			trayIcon.setImage(image);

	}

	public static void setActiveIcon() {
		if (!traySupported) return;
		setImage(activeImage);
	}

	public static void setIdleIcon() {
		if (!traySupported) return;
		setImage(idleImage);
	}

	public static void setErrorIcon() {
		if (!traySupported) return;
		setImage(errorImage);
	}
	public static Image getCurrentImage(){
		if (!traySupported) return (null);
		if (trayIcon != null)
			return(trayIcon.getImage());
		else
			return(null);
	}

	public void resetToIdle(){
		if (!traySupported) return;
		setMessage(startupTooltip, "Waiting for next event");
		setToolTip(startupTooltip);
		setIdleIcon();
	}

	public void sendDashboardMessage(DashboardMessageGenerator.MessageType messageType, String key, String message){
		try{
			ContextState contextState = ContextManager.get().getCurrentContextState();
			if (DashboardMessageGenerator.validStateForMessageGeneration(contextState)){
				String baseURL = DashboardMessageGenerator.createBaseMessageURL(contextState);
				String url = DashboardMessageGenerator.makeMessageURL(baseURL, messageType, key,  message);
				JSONSimpleGET.runLater(url);
			}
			else{
				log.info("Message '" + message +"' not sent to server; contextState not initialized");
			}
		}
		catch(Exception e){
			log.error("Error generating error message to dashboard",e);
		}
	}
	
	public void setErrorMessage(String caption, String message){
		setErrorMessage(caption, message, null);
	}
	
	public void setErrorMessage(String caption, String message, String transactionKey) {
		log.info("Error message displayed:" + caption + ", message =" + message);
		try{
			if(!useSytemTrayPopups){
			    notificationQueue.getQueue().offer(new ToolTipReplacementFrame(popupDialogDisplayMsec, caption, message, errorImage));
			}
			else if (traySupported & SystemTray.isSupported()) {
				trayIcon.displayMessage(caption, message, TrayIcon.MessageType.ERROR);
			}
			else{
				log.error(caption + ": " + message);
			}
		}
		catch(Exception e){
			log.error("Error setting error message: + " + message, e);
			DisplayModalError("Internal error", "Can not display error message\n" + message + "\n in system tray popup.", false);
		}
		
		// Note: transactionKey might be null, but that's ok
		sendDashboardMessage(DashboardMessageGenerator.MessageType.ERROR, transactionKey, caption + ": " +  message);
		
	}
	public void setMessage(String caption, String message){
		setMessage(caption, message, null);
	}
	public void setMessage(String caption, String message,String transactionkey) {
		log.info("Message displayed:" + caption + ", message =" + message);
		
		try{
			if(!useSytemTrayPopups) {
			    notificationQueue.getQueue().offer(new ToolTipReplacementFrame(popupDialogDisplayMsec, caption, message, activeImage));
			}
			else if (traySupported && SystemTray.isSupported()) {
				trayIcon.displayMessage(caption, message, TrayIcon.MessageType.INFO);
			}
			else{
				log.info(caption + ":" + message);
			}
		}
		catch(Exception e){
			log.error("Error setting error message: + " + message, e);
			DisplayModalError("Internal error", "Can not display error message\n" + message + "\n in system tray popup.", false);
		}
		if (transactionkey != null)
			sendDashboardMessage(DashboardMessageGenerator.MessageType.INFO, transactionkey, caption + ":" +message);
	}


	public void setCurrentWorklist(String currentServer, String currentWorklist){
		boolean wasChanged = false; 
		if(Str.eq(this.currentServer,currentServer)) {
			wasChanged = false;
		}
		else 
		if(blank(currentServer)) {
			this.currentServer = null;
			wasChanged = true;
		}
		else {
			this.currentServer = currentServer;
			wasChanged = true;
		}
		
		if(Str.eq(this.currentWorklist,currentWorklist)) {
			// Don't change state - if wasChange is true leave
			// as true; if false (the default) leave as false.
		}
		else 
		if(blank(currentWorklist)) {
			this.currentWorklist = null;
			wasChanged = true;
		}
		else {
			this.currentWorklist = currentWorklist;
			wasChanged = true;
		}
		
		if(wasChanged) {
			setToolTip(this.currentToolTip);
		}
	}

	static public StatusDisplayManager getStatusDisplayManager() {
	    return get();
	}

	static public StatusDisplayManager get() {
	    if(testMode) {
	        return new StatusDisplayManager() {

                @Override
                public void resetToIdle() {
                }

                @Override
                public void sendDashboardMessage(MessageType messageType, String key, String message) {
                }

                @Override
                public void sendPrompt(ContextState oldCtx, ContextState newCtx) {
                }

                @Override
                public void setCurrentWorklist(String currentServer, String currentWorklist) {
                }

                @Override
                public void setErrorMessage(String caption, String message, String transactionKey) {
                }

                @Override
                public void setErrorMessage(String caption, String message) {
                }

                @Override
                public void setMessage(String caption, String message, String transactionkey) {
                }

                @Override
                public void setMessage(String caption, String message) {
                }

                @Override
                public void setToolTip(String toolTip) {
                }
	        };
	    }
	    else
			return sdm;
	}

	static public void DisplayModalError(String title, String message, boolean exit){
	    DisplayModalError(title, message, exit, false);
	}
	
	static public void DisplayModalError(String title, String message, boolean exit, final boolean sendLogs){
		final JFrame frame = new JFrame("DialogDemo");
		final boolean exitDDL = exit;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        if(sendLogs) {
            title += " - Log File Upload";
            message += "\r\n\r\n" +
                    "If you would like to send a log file with details of this error to MedCommons for analysis, please click Yes below.\r\n\r\n If you are not happy to send your log file, please select 'No'";
        }
        
        
		final String titleContents = title;
		final String messageContents = message;

        // Create and set up the content pane.
        JPanel newContentPane = new JPanel(new BorderLayout());
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(sendLogs) {
	            	int result = JOptionPane.showConfirmDialog(frame,
	        			    messageContents,
	        			    titleContents,
	        			    JOptionPane.YES_NO_OPTION,
	        			    JOptionPane.ERROR_MESSAGE);
	            	
	            	if(result == JOptionPane.YES_OPTION) {
	            	    try {
		            	    UploadLogFileCommand.doStandaloneUpload();
		            	    JOptionPane.showMessageDialog(frame, "Log file uploaded - Thank you!");
	            	    }
	            	    catch(Exception e) {
	            	        System.out.println("Failed to upload log");
	            	        e.printStackTrace();
	            	    }
	            	}
                }
                else {
	            	JOptionPane.showMessageDialog(frame,
	        			    messageContents,
	        			    titleContents,
	        			    JOptionPane.ERROR_MESSAGE);
                }
            	
            	if(exitDDL) {
            		System.exit(1);
            	}
            }
        });
        
        //Display the window.
        //frame.pack();
       // frame.setVisible(true);

	}
	public static boolean showDocument(URL url)

	{
		boolean success = false;
		PersistenceService ps = null;
		BasicService bs = null;
		try {
			ps = (PersistenceService) ServiceManager
					.lookup("javax.jnlp.PersistenceService");
			bs = (BasicService) ServiceManager
					.lookup("javax.jnlp.BasicService");
			System.err.println("Persistence Service is " + ps);
			System.err.println("BasicServcies is " + bs);
		}
		catch(UnavailableServiceException e) {
		    try {
                java.awt.Desktop.getDesktop().browse(url.toURI());
            }
            catch (Exception e1) {
                log.error("Unable to launch browser for URL" + url, e1);
                return false;
            }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (bs == null)
			return (false);
		if (url == null){
			log.error("showDocument() failed - url is null");
		}
		try{
			success = bs.showDocument(url);
		}
		catch(Exception e){
			log.error("Exception displaying URL " + url, e);
		}
		return (success);

	}

	/**
     * Create the upload DICOM window and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void createAndShowFileChooser() {
    	log.info("Creating DicomFileChooser");
    	
        //Create and set up the window.
        JFrame frame = new JFrame("Upload DICOM files");

       frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Add content to the window.
        // frame.add(new DicomFileChooser(null,false));
        
        // DicomFileChooser fc = new DicomFileChooser(null, false);


        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        
        DicomFileChooser fc = new DicomFileChooser(null, false);
        
        frame.setAlwaysOnTop(false);
        frame.setVisible(false);
        frame.dispose();
    }
    public static void createAndShowCDChooser() {
    	log.info("Creating DicomFileChooser for CD");
        //Create and set up the window.
        //JFrame frame = new JFrame("Upload DICOM files");

       // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Add content to the window.
       // frame.add(new DicomFileChooser(frame));
        DicomFileChooser fc = new DicomFileChooser(null,true);


        //Display the window.
        //frame.pack();
        //frame.setVisible(true);

    }
    
    public static class ImageLoader {

	   public static java.awt.Image loadImage(final String imageName) {
	      final java.lang.ClassLoader loader = ImageLoader.class.getClassLoader();
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
	               final int BlockLen = 256;
	               int offset = 0;
	               int len;
	               byte imageData[] = new byte[BlockLen];
	               while ((len = is.read(imageData, offset, imageData.length - offset)) > 0) {
	                  if (len == (imageData.length - offset)) {
	                     byte newData[] = new byte[imageData.length * 2];
	                     System.arraycopy(imageData, 0, newData, 0, imageData.length);
	                     imageData = newData;
	                     newData   = null;
	                  }
	                  offset += len;
	               }
	               image = java.awt.Toolkit.getDefaultToolkit()
	                          .createImage(imageData);
	            } catch (java.io.IOException ex) {}
	         }
	      return image;
	   }
	   public static ImageIcon createImageIcon(final String path) {
		 //  final java.lang.ClassLoader loader = ImageLoader.class.getClassLoader();
		   ImageIcon icon = null;
		   try{
		   icon = (ImageIcon)
	         java.security.AccessController.doPrivileged(
	            new java.security.PrivilegedAction() {
	               public Object run() {

	                	  java.net.URL imgURL = ImageLoader.class.getResource(path);
	                	  log.info("imgURL is " + imgURL);
	                	  log.info("imgURL:" + imgURL.toExternalForm());
	                	  return new ImageIcon(imgURL);

	               }
	            });
		   }
		   catch(NullPointerException e){
			   log.info("Icon " + path +" could not be loaded");
		   }
			return(icon);
		}
	}



}
