package net.medcommons.application.dicomclient.utils;

import static net.medcommons.modules.utils.Str.blank;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.UploadPopupWindow;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.utils.DashboardMessageGenerator;
import net.medcommons.application.utils.JSONSimpleGET;
import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;

/**
 * Displays status in the system tray if possible; otherwise
 * writes messages to log.
 * @author mesozoic
 *
 */
public class StatusDisplayManager {
	private static String startupTooltip = "DICOM Data Liberator";
	private static Logger log = Logger.getLogger(StatusDisplayManager.class
			.getName());




	private String errorImageFile  = "toolbar/Error.png";
	private String idleImageFile   = "toolbar/Inactive.png";
	private String activeImageFile = "toolbar/Active.png";

	



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
	
	private long popupDialogDisplayMsec = 10*1000;
	
	private static boolean tryTransparency = true;

	public StatusDisplayManager() {
		sdm = this;
		

		final String configureDDL = "Configure DDL";
		final String showStatus = "Show Status";
		final String clearStatus = "Clear Status";
		final String shutdown = "Shutdown";
		final String uploadFolder = "Upload DICOM Folder";
		final String uploadCD = "Upload CD";
		final String dicomOnDemandDialog = "DICOM On Demand";
		
		
		//		 get the StatusDisplayManager instance

		//if (ContextUtils.isJDK6Orlater() && SystemTray.isSupported()) {
			
			
			idleImage = ImageLoader.loadImage(idleImageFile);
			activeImage = ImageLoader.loadImage(activeImageFile);
			errorImage = ImageLoader.loadImage(errorImageFile);
			try{
				String os = operatingSystem.toUpperCase();
				log.info("==Operating system is " + os);
				if ((os.indexOf("OSX")!= -1) || (os.indexOf("DARWIN") != -1) || (os.indexOf("OS X") != -1)){
					useSytemTrayPopups = false;
				}
				tray = SystemTray.getSystemTray();
				traySupported = true;
				log.info("System tray is supported");
				
			}
			catch(UnsupportedOperationException e){
				log.error("System tray not available", e);
			}
		//}
		//else{
		//	log.error("System tray not supported on this machine");
		//}
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
					else if (e.getActionCommand().equals(dicomOnDemandDialog)){
						
						UploadPopupWindow popupWindow = new UploadPopupWindow();
			             popupWindow.setVisible(true);
			             popupWindow.toFront();
			             popupWindow.requestFocus();
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
             
			// create a popup menu
			PopupMenu popup = new PopupMenu("DDL");
			 String popupUploadDialog = "true";// = System.getProperty("popupUploadDialog");
			 if (!Str.blank(popupUploadDialog)){
				 if ("true".equalsIgnoreCase(popupUploadDialog)){
					 MenuItem dodPopup = new MenuItem(dicomOnDemandDialog);
					 popup.add(dodPopup);
					 dodPopup.addActionListener(listener);
				 }
			 }
             
			
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

			popup.add(uploadFolderItem);
			uploadFolderItem.addActionListener(listener);
			
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
		
			ContextManager.getContextManager().addListener(new PropertyChangeListener() {
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
	
	public void sendPrompt(ContextState oldCtx, ContextState newCtx) {
		if(!Str.eq(oldCtx.getCxpHost(), newCtx.getCxpHost()) && !blank(newCtx.getCxpHost())) {
			log.info("New upload host set - signalling dashboard");
			sdm.sendDashboardMessage(DashboardMessageGenerator.MessageType.PROMPT, null, 
					"DDL " + ContextManager.getContextManager().getConfigurations().getDicomLocalAeTitle() + 
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
	public void setToolTip(String toolTip) {
		if (!traySupported) return;
		this.currentToolTip = toolTip;
		if (SystemTray.isSupported()) {
			StringBuilder buff = new StringBuilder();
			boolean firstEntry = true;
			if (currentServer != null){
				buff.append("Current server: " + currentServer);
				firstEntry = false;
			}
			if (currentWorklist != null){
				if (!firstEntry) buff.append("\n");
				buff.append("Current worklist: " + currentWorklist);
				firstEntry = false;
			}
			else{
				buff.append("No worklist has been set");
				firstEntry = false;
			}
			if (!firstEntry){
				buff.append("\n");
			}
			buff.append(this.currentToolTip);
			trayIcon.setToolTip(buff.toString());
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
			ContextState contextState = ContextManager.getContextManager().getCurrentContextState();
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
			if (!useSytemTrayPopups){
				
				ToolTipReplacementFrame newFrame = new ToolTipReplacementFrame(popupDialogDisplayMsec, caption, message, errorImage);
				new Thread(newFrame).start();
			}
			else if (traySupported & SystemTray.isSupported()) {
				trayIcon.displayMessage(caption, message, TrayIcon.MessageType.ERROR);
			}
			else{
				log.error(caption + ":" + message);
			}
		}
		catch(Exception e){
			log.error("Error setting error message: + " + message, e);
			DisplayModalError("Internal error", "Can not display error message\n" + message + "\n in system tray popup.", false);
		}
		
		// Note: transactionKey might be null, but that's ok
		sendDashboardMessage(DashboardMessageGenerator.MessageType.ERROR, transactionKey, caption + ":" +  message);
		
	}
	public void setMessage(String caption, String message){
		setMessage(caption, message, null);
	}
	public void setMessage(String caption, String message,String transactionkey) {
		log.info("Message displayed:" + caption + ", message =" + message);
		
		try{
			if (!useSytemTrayPopups){
				
				ToolTipReplacementFrame newFrame = new ToolTipReplacementFrame(popupDialogDisplayMsec,caption, message, activeImage);
				new Thread(newFrame).start();
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
		return (sdm);
	}

	static public void DisplayModalError(String title, String message, boolean exit){
		final JFrame frame = new JFrame("DialogDemo");
		final String titleContents = title;
		final String messageContents = message;
		final boolean exitDDL = exit;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JPanel newContentPane = new JPanel(new BorderLayout());
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	JOptionPane.showMessageDialog(frame,
        			    messageContents,
        			    titleContents,
        			    JOptionPane.ERROR_MESSAGE);
            	if (exitDDL){
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
		} catch (Exception e) {
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
	public class ToolTipReplacementFrame implements Runnable{
		long closeTime;
		JFrame frame = null;
		long nMsec;
		long startTime;
		
		public ToolTipReplacementFrame(long nMsec, String caption, String message, Image image){
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			this.nMsec = nMsec;
			frame = new JFrame("Frame With No Title Bar");
			frame.setUndecorated(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//frame.setSize(400,400);
			int frameWidth;
			int frameHeight = 40;
			ImageIcon icon = new ImageIcon(image);
			JLabel label = new JLabel(message);
			label.setSize(label.getPreferredSize());
			
			
			JLabel title = new JLabel(caption, icon, SwingConstants.RIGHT);
			title.setSize(title.getPreferredSize());
			int maxWidth = Math.max(title.getWidth(), label.getWidth());
			frameWidth = maxWidth + 20;
			Container container =frame.getContentPane();
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			container.add(title);
			container.add(label);
			frame.pack();
			
			
			frame.setBounds(screenSize.width - frameWidth - 40, frameHeight, frameWidth, frameHeight);
			frame.setVisible(true);
			frame.toFront();
			frame.requestFocus();
			
			
			startTime = System.currentTimeMillis();
			closeTime = startTime + nMsec;
		}
		public void run(){
			try{
				
				while (System.currentTimeMillis() < closeTime){
					try{Thread.sleep(500);}
					catch(InterruptedException e){;}
					if (tryTransparency){
						try{
							long currentTime = System.currentTimeMillis();
							long diffTime = currentTime - startTime;
							if (diffTime < 0) diffTime = 0;
							float opacity = (diffTime * 1.0f)/(nMsec);
						 Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
						 Method mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
						   mSetWindowOpacity.invoke(null, frame, Float.valueOf(opacity));
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
				
			}
			finally{
				frame.setVisible(false);
				frame.dispose();
				frame = null;
			}
		}
		
	}
}
