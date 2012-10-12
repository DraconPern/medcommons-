package net.medcommons.application.dicomclient;

import static net.medcommons.modules.utils.Str.blank;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.xml.parsers.FactoryConfigurationError;

import net.medcommons.application.dicomclient.http.HttpServer;
import net.medcommons.application.dicomclient.http.utils.Voucher;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.CommandBlock;
import net.medcommons.application.dicomclient.utils.DirectoryUtils;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.Shutdown;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.utils.HttpLoggerUtils;
import net.medcommons.application.utils.JSONSimpleGET;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.utils.Str;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Main entry point of DICOM Client.
 * This is currently called the DDL.
 *
 * Contains a DICOM SCP, a DICOM SCU, an HTTP server, and a CXP Client.
 *
 * There are no command side options. All preferences are set via configurations.
 *
 * @author mesozoic
 *
 * TODO
 * Put Use Spring for building components
 * Look at Spring for handling errors.
 *
 */


public class DICOMClient {

    private static Logger log  = null;

    private static String uploadCacheFolder = "UploadCache";

    private static String downloadCacheFolder = "DownloadCache";




    private StatusDisplayManager statusDisplayManager = null;



    private static File localConfigFile;

    private ContextManager contextManager;

    private static boolean localConfigPresent = false;
    
    private static CommandBlock initialCommand = null;
   
    

    
    /**
     * DICOMClient is the main class for the DDL.
     * The context manager is used throughout the application. There is only one instance of the
     * context manager. The configProperties are ephemeral - these get copied into a Configuration
     * object which is available to the rest of the system via the context manager.
     * @param contextManager
     * @param configProperties
     */

    public DICOMClient(ContextManager contextManager, Properties configProperties) {
        this.contextManager = contextManager;
      
        
        
        try{
        initializeStatusDisplayManager();
        
        // Need to get all the properties.
        // Have function - pass down the enum and have it set the values.
        initConfiguration(configProperties);

        // Turn off most SSL safeguards.
        JSONSimpleGET.initInsecureSocketLayer(443);

        // Install a shutdown hook to clean things up
        // on application shutdown.
        Shutdown shutdown = new Shutdown();
        Runtime rt = Runtime.getRuntime();
        rt.addShutdownHook(shutdown);
        }
        catch(NoClassDefFoundError e){
        	System.out.println("Not able to load classes; probably wrong JVM version");
        }

        catch(Exception e){
            e.printStackTrace();
            StatusDisplayManager.DisplayModalError("Initialization error", e.getClass().getCanonicalName() + "\n" + e.getLocalizedMessage(), true);
        }
    }


    private void initConfiguration(Properties configProperties) throws IOException ,ParseException{
        Configurations configurations = contextManager.getConfigurations();
        //
        Enumeration<Object> configs = configProperties.keys();
        while (configs.hasMoreElements()) {
            String key = (String) configs.nextElement();
            String value = configProperties.getProperty(key);

            System.out.println("key:" + key + ", value:" + value);
            ResourceName resource = ResourceName.getResourceName(key);
            if (resource == null){
                throw new NullPointerException("Unknown resource in configurations:" + key);
            }
            if ((value == null) || (value.equals(""))) {
                value = resource.getDefault();

            }
            setConfiguration(resource, value);
        }

        configurations.setGatewayRoot(System.getProperty("gatewayRoot"));

        configurations.setConfigurationFile(localConfigFile);
        System.out.println("Gateway root set to " + configurations.getGatewayRoot());
        InputStream in = loadResource("timestamp.txt");

        if (in != null){
            InputStreamReader is = new InputStreamReader(in);
            char[] buffer = new char[256];
            int i = is.read(buffer);
            String dateString = new String(buffer, 0, i).trim();


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = dateFormat.parse(dateString);
            System.out.println("Date string " + dateString + " resolved to " + d.toLocaleString());
            configurations.setBuildTime(d);
        }
        else{
            System.out.println("No timestamp in jar file");
        }
        in = loadResource("ddlversion.txt");

        if (in != null){
            InputStreamReader is = new InputStreamReader(in);
            char[] buffer = new char[256];
            int i = is.read(buffer);
            String vString = new String(buffer, 0, i).trim();


            String versionString = getVersionString(vString);
            configurations.setVersion(versionString);
            System.out.println("DDL Version:" + configurations.getVersion());
        }
        else{
            System.out.println("No ddlverson.txt in jar file");
            configurations.setVersion("UNKNOWN");
        }
        String ddlIdentity = configurations.getDDLIdentity();
        if (ddlIdentity == null){
        	ddlIdentity = createDDLIdentity();
        	configurations.setDDLIdentity(ddlIdentity);
        }
        configurations.save(configurations.getConfigurationFile());

        configureLogging(configurations);
        
        configurations.save(configurations.getConfigurationFile());

    }


    private void configureLogging(Configurations configurations) throws IOException, FileNotFoundException,
            FactoryConfigurationError {
        InputStream in = loadResource("log4j.xml");
        if (in != null) {

            byte[] buffer = new byte[256];
            File baseDir = configurations.getBaseDirectory();
            File logDir = new File(baseDir, "logs");
            
            DirectoryUtils.makeDirectory(logDir);

            File log4jFile = new File(logDir, "log4j.xml");
            if (log4jFile.exists()){
            	boolean deleted = log4jFile.delete();
            	System.out.println("Deleted existing log file contents:" + log4jFile.getAbsolutePath() + " " + deleted);
            }
            
            try {
                System.out.println("Creating new log4j.xml file");
                FileOutputStream out = new FileOutputStream(log4jFile);
                int i;
                while ((i = in.read(buffer)) != -1){
                    out.write(buffer, 0, i);
                }
                out.close();
                System.out.println("Finished writing file " + log4jFile.getAbsolutePath());
                
                DOMConfigurator.configure(log4jFile.getAbsolutePath());
                log = Logger.getLogger(DICOMClient.class.getName());
                
            }
            catch(Exception e) {
                System.err.println("Error loading newly-created log4j.xml file:" + log4jFile.getAbsolutePath());
                e.printStackTrace(System.err);
            }
            
            
        }
        else{
            System.err.println("No log4j.xml in jar file");
            throw new FileNotFoundException("log4j.xml not in jar file; can not configure log");
        }
        
        // Set to null by default so that no logging is performed
        HttpLoggerUtils.setURL(null);
        
        // Set the http appender to always send to the appliance targeted by the current context state
        ContextManager.getContextManager().addListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("currentContextState".equals(evt.getPropertyName()))  {
                    ContextState state = (ContextState) evt.getNewValue();
                    HttpLoggerUtils.setURL(state.getGatewayRoot() + "/router/Logging");
                }
            } 
        });
        
    }

    /*
     * Sets
     */
    public void setConfiguration(ResourceName resource, String value) {


        Configurations configurations = contextManager.getConfigurations();
        if (log != null)
            log.info("Configuration:" + resource + ", value=" + value);
        else
            System.out.println("Configuration:" + resource + ", value=" + value);
        switch (resource) {
        case CLEAR_CACHE:
            if ("true".equalsIgnoreCase(value))
                configurations.setClearCache(true);
            else
                configurations.setClearCache(false);
            break;
        case DICOM_REMOTE_AE_TITLE:
            configurations.setDicomRemoteAeTitle(value);
            break;

        case DICOM_REMOTE_HOST:
            configurations.setDicomRemoteHost(value);
            break;

        case DICOM_REMOTE_PORT:
            if ((value.equalsIgnoreCase("UNKNOWN")) || ("".equals(value)) || (value == null))
                configurations.setDicomRemotePort(Integer.MIN_VALUE);
            else
                configurations.setDicomRemotePort(Integer.parseInt(value));
            break;
        case DICOM_LOCAL_AE_TITLE:
            configurations.setDicomLocalAeTitle(value);
            break;

        case DICOM_LOCAL_PORT:

            if ((value.equalsIgnoreCase("UNKNOWN")) || ("".equals(value)) || (value == null))
                configurations.setDicomLocalPort(Integer.MIN_VALUE);
            else
                configurations.setDicomLocalPort(Integer.parseInt(value));

            break;
        case LOCAL_HTTP_PORT:
            configurations.setLocalHttpPort(Integer.parseInt(value));
            break;
        case PROXY_PORT:
            // This sets the port for the ProxyServer
            configurations.setProxyPort(Integer.parseInt(value));
            // This configures the CXP client to send to the ProxyServer
            //CXPClient.setHttpProxy("caudipteryx", value);
            // CXPClient.setHttpProxy(null, null);
            break;

        case SENDER_ACCOUNTID:
            configurations.setSenderAccountId(value);
            break;
        case CONFIGVERSION:
            configurations.setConfigVersion(value);
            break;


        case EXPORT_METHOD:
            configurations.setExportMethod(value);
            break;
        case DICOM_TIMEOUT:
            configurations.setDicomTimeout(Integer.parseInt(value));
            break;
        case DDL_IDENTITY:
        	if ("UNKNOWN".equals(value)){
        		throw new IllegalArgumentException("DDL Identity can not be set to '" + value + "'");
        	}
            configurations.setDDLIdentity(value);
            break;    
        case AUTOMATIC_UPLOAD_TO_Voucher:
            configurations.setAutomaticUploadToVoucher(Boolean.parseBoolean(value));
            break;    
        case EXPORT_DIRECTORY:
            File f = new File(value);
            if (f.exists())
                configurations.setExportDirectory(f);
            
            else{
            	f.mkdirs();
            	if (!f.exists()){
            		configurations.setExportDirectory(f);
            	}
            	else{
	                String message ="ExportDirectory can not be set to " + f.getAbsolutePath() + "\n File does not exist";
	                if (log!= null){
	                    log.error(message);
	                }
	                else{
	                    System.err.println(message);
	                }
            	}
            }
            break;
        default:
            throw new RuntimeException("Unknown case:" + resource);
        }
    }


    private static File baseDirectory() throws IOException{
        String userHome = System.getProperty("user.home");
        File userHomeDir = new File(userHome);
        File base = new File(userHomeDir, "DDL");
        DirectoryUtils.makeDirectory(base);
        return(base);
    }
    public static void initializeDatabase() throws IOException{

    	File base = ContextManager.getContextManager().getConfigurations().getBaseDirectory();



        File databaseDir = new File(base, "DB");

        DirectoryUtils.makeDirectory(databaseDir);

        LocalHibernateUtil.initializeSessionFactory(databaseDir);
    }
    public static void initializeDownloadHandler(){
    	DownloadHandler.InitFactory();
    }
    public static ContextManager initializeFiles() throws IOException{
        Configurations configurations = new Configurations();

        File base = baseDirectory();
        configurations.setBaseDirectory(base);
        File baseCache = new File(base, "Cache");
        DirectoryUtils.makeDirectory(baseCache);


        localConfigFile = new File(base, "DDL.properties");
        localConfigPresent = localConfigFile.exists();
        File uploadCache = new File(baseCache, uploadCacheFolder);
        DirectoryUtils.makeDirectory(uploadCache);
        File downloadCache = new File(baseCache, downloadCacheFolder);
        DirectoryUtils.makeDirectory(downloadCache);


        ContextManager contextManager = new ContextManager(configurations);
        contextManager.setUploadCache(uploadCache);
        contextManager.setDownloadCache(downloadCache);
        return(contextManager);
    }

    private void initializeStatusDisplayManager() {
        statusDisplayManager = new StatusDisplayManager();
    }
    /**
     * Returns the revision of this version of the DDL.
     * @return
     */
    private static String getVersionString(String revisionString){
        /*
         * Note that if you are on a developer's machine that the version might
         * be a string like "1645:1652M". This is because it's between svn
         * versions.
         */

        int semiLoc = revisionString.indexOf(":");
        if (semiLoc != -1){
            revisionString = revisionString.substring(semiLoc + 1);
        }
        return(revisionString);
    }

    private  void validDicomConfig(){
        Configurations configurations = contextManager.getConfigurations();
        boolean badConfig = false;
        StringBuffer buff = new StringBuffer();

        String remoteAETitle = configurations.getDicomRemoteAeTitle();
        if ("UNKNOWN".equalsIgnoreCase(remoteAETitle)){
            badConfig = true;
            buff.append("\nUninitialized value for DICOMRemoteAETitle:" + remoteAETitle);

        }
        String remoteHost = configurations.getDicomRemoteHost();
        if ("UNKNOWN".equalsIgnoreCase(remoteHost)){
            badConfig = true;
            buff.append("\nUninitialized value for DICOMRemoteHost:" + remoteHost);

        }
        int remotePort = configurations.getDicomRemotePort();
        if (remotePort == Integer.MIN_VALUE){
            badConfig = true;
            buff.append("\nUninitialized value for DICOMRemotePort");
        }

        int localPort = configurations.getDicomLocalPort();
        if (localPort == Integer.MIN_VALUE){
            badConfig = true;
            buff.append("\nUninitialized value for DICOMLocalPort");
        }
        if (badConfig){
            log.info("invalid local config - some DICOM configuraions not set");
            try{
                Thread.sleep(1000*10);
            }
            catch(Exception e){;}
            if (false){
	            statusDisplayManager.DisplayModalError("Some configurations need to be set by user",
	                    "\nPlease edit items on about page" +
	                    "\n" + buff.toString() +
	                    "\n All UNKNOWN values must be replaced with real values", false);
	            // Now launch config dialog after a small delay so that the error dialog appears first.
	            try{Thread.sleep(1000);} catch(Exception e){;}
            }
           
           // StatusDisplayManager.showAbout(); TODO: This may be halting processing in soylatte.

        }


    }
    
    /**
     * Main loop for running DDL
     */
    private void runDDL() {
    	ContextManager contextManager = ContextManager.getContextManager();
    	Configurations configurations = contextManager.getConfigurations();
         try {
             
        	 statusDisplayManager.setMessage("Dicom Data Liberator", "Initializing DDL...");
        	 contextManager.startHttpServer();
        	 
             // Set context state as soon as possible so that messages
             // can get to the dashboard to indicate progress immediately
             ContextState contextState = getDefaultContextState();
             if(contextState != null) {
             	contextManager.setCurrentContextState(contextState);
             	log.info("Set default context state " + contextState);
             	log.info("Can support voucher creation:" + Voucher.contextComplete(contextState));
             	StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
             	sdm.setCurrentWorklist(contextState.getCxpHost(), contextState.getGroupName());
             	configurations.setAutomaticUploadToVoucher(true); // DICOM On Demand case
             }
             else {
            	 log.info("Initial context state is null");
             }
             
        	 contextManager.startDcmServer();
        	
             statusDisplayManager.setMessage("Dicom Data Liberator", "The DDL has been successfully started");

             validDicomConfig();
             
             log.info("======== DDL Successfully started =====");
             log.info("Build time:" + configurations.getBuildTime());
             log.info("Build version" + configurations.getVersion());
             
             // String classPath = System.getProperty("java.class.path",".");
             //  log.info("classpath is " + classPath);
             
             
             boolean isDicomOnDemand = isDoDApplication();
             if(isDicomOnDemand) {
            	 UploadPopupWindow popupWindow = new UploadPopupWindow();
                 popupWindow.setVisible(false);
                 // popupWindow.toFront();
                 // popupWindow.requestFocus();
            	 log.info("DICOM On Demand Dialog displayed");
             }
             
             // The DDL is now running - so if there was a command
             // specified in the jnlp then run it.
             if (initialCommand != null){
     			runCommandOnExistingDDL(initialCommand);
     			
     		 }
             
             synchronized (this) {
            	wait();
             }

         }
         catch (BindException e){
        	 log.error("Multiple DDLs running", e);
        	 StatusDisplayManager.DisplayModalError("Multiple DDLs running", "This copy of DDL will exit", true);
         }
         catch(Exception e){
        	 e.printStackTrace(System.err);
         }

    }
    /**
     *  <property name="defaultAccountID" value="1117658438174637"/>
    <property name="defaultAuth" value="29e669d17b056fe5a0a8c28f12a35ed9aef3992c"/>	
    <property name="defaultGroupAccountID" value="1172619833385984"/>	
    <property name="defaultCXPHost" value="ci.myhealthespace.com"/>
    <property name="defaultCXPPort" value="443"/>
    <property name="defaultCXPPath" value="/gateway/services/CXP2"/>
    <property name="defaultCXPProtocol" value="https"/>
     * @return
     */
    private static ContextState getDefaultContextState(){
        
        // A context set for the command to be launched should over ride the default
        // context.  
    	ContextState contextState = null;
        if(initialCommand != null && !Str.blank(initialCommand.getProperty("cxphost"))) {
            contextState = initialCommand.toContextState();
            return contextState;
        }
        
    	String defaultAccountID = System.getProperty("defaultAccountID");
    	String defaultAuth = System.getProperty("defaultAuth");
    	String defaultGroupAccountID = System.getProperty("defaultGroupAccountID");
    	String defaultGroupName= System.getProperty("defaultGroupName");
    	String defaultCXPHost= System.getProperty("defaultCXPHost");
    	String defaultCXPPort= System.getProperty("defaultCXPPort");
    	String defaultCXPPath= System.getProperty("defaultCXPPath");
    	String defaultCXPProtocol= System.getProperty("defaultCXPProtocol");
    	
    	if (!blank(defaultAccountID) && (!blank(defaultAuth))){
    		String defaultGatewayRoot = defaultCXPProtocol + "://" + defaultCXPHost;
    		contextState = new ContextState();
    		contextState.setAccountId(defaultAccountID);
    		contextState.setAuth(defaultAuth);
    		contextState.setGroupAccountId(defaultGroupAccountID);
    		contextState.setCxpHost(defaultCXPHost);
    		contextState.setCxpPort(defaultCXPPort);
    		contextState.setCxpPath(defaultCXPPath);
    		contextState.setCxpProtocol(defaultCXPProtocol);
    		contextState.setGroupName(defaultGroupName);
    		contextState.setGatewayRoot(defaultGatewayRoot);
    	}
    	
    	log.info("defaultAccountID = " + defaultAccountID);
    	log.info("defaultAuth=" + defaultAuth);
    	log.info("contextState:" + contextState);
    	return(contextState);
    }
    
    private static String userhome = "@user.home";

    private static String getSystemProperty(String name, String defaultValue){
        String property = System.getProperty(name);
      
        if (
        		(property == null) || 
        		("".equals(property)) || 
        		("UNKNOWN".equalsIgnoreCase(property)) ||
        		("null".equalsIgnoreCase(property))
        		){
            property = defaultValue;
        	System.out.println("No value found for property " + name + ", using default " + defaultValue);
        }
        else{
             System.out.println("found value " + property + " for " + name);
        }
        return(property);

    }
    private static boolean isSystemPropertyTrue(String propertyName){
    	boolean test = false;
    	String systemValue = System.getProperty(propertyName);
		if (!Str.blank(systemValue)){
			if ("true".equalsIgnoreCase(systemValue))
				test = true;
		}
		return(test);
    }
    private static boolean isDoDApplication(){
    	return(isSystemPropertyTrue("popupDoDDialog"));
    }
    private static boolean isPublicDemoGroup(){
    	return(isSystemPropertyTrue("publicDemoGroup"));
    	
    }
    private static boolean displayUploadedCCR(){
    	return(isSystemPropertyTrue("displayUploadedCCRPopup"));
    }
    
    private static boolean useREST(){
    	return(isSystemPropertyTrue("useREST"));
    	
    }
    
    private static void showDoDExitScreen(){
    	final JFrame frame = new JFrame("DICOM On Demand");
	
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	JOptionPane.showMessageDialog(frame,
            			"DICOM On Demand's DDL application is already running.\n" +
            			"Please quit running DDL by selecting 'Shutdown' from the DDL menu.",
        			    "DDL Already running",
        			    JOptionPane.ERROR_MESSAGE);
            	System.exit(1);
            }
    	});
    	
    	
    	

    }
    /**
     * Launch DDL
     * @param commands
     */
    public static void main(String[] commands) {
    	
    	initialCommand = getCommandBlock();
    	
    	if(isDDLAlreadyRunning()) {
    		System.out.println("Exiting - DDL already running");
    		System.out.println("In the future - the following arguments will be forwarded to the running DDL instance");
    		for(int i=0;i<commands.length;i++) {
    			System.out.println("main arg[" + i + "] = " + commands[i]);
    		}
    		System.out.println("Command block is " + initialCommand);
    		if(initialCommand != null) {
    			runCommandOnExistingDDL(initialCommand);
    			System.exit(0);
    		}
    		if(isDoDApplication()) {
    			// Will exit when dialog is closed.
    			showDoDExitScreen();
    		}
    		else {
    			System.exit(0);
    		}
    	}
    	else {
    		System.out.println("DDL is not running");
    	}
    	
    	setNativeLookAndFeel();
   	
        ContextManager contextManager = null;
        try{
	        contextManager = initializeFiles();
	        initializeDatabase();
	        initializeDownloadHandler();
	        contextManager.setDisplayUploadedCCR(displayUploadedCCR());
	        contextManager.setUseREST(useREST());
	        
        }
        catch(IOException e){
        	System.err.println("IO error (probably file permissions");
        	e.printStackTrace(System.err);
        }

        String userHome = System.getProperty("user.home");
        System.err.println("User home is " + userHome);
        File userHomeDir = new File(userHome);
        System.err.println("User home directory is " + userHomeDir.getAbsolutePath());
        System.err.println("SecurityManager = " + System.getSecurityManager());
        
        Properties props = System.getProperties();
        try {
            props.store(System.err, "System properties");
        }
        catch(IOException e) {
         ; // Ignore
        }
        
        System.setSecurityManager(null);
        String jnlp = System.getProperty("jnlpx.remove");
        System.err.println("jnlp is " + jnlp);
        
        for (int i=0;i<commands.length;i++){
			System.err.println("main arg[" + i + "] = " + commands[i]);
		}

        try {
            Properties configProperties = loadConfiguration();
            
            configureLogging();

            DICOMClient client = new DICOMClient(contextManager, configProperties);
           
            client.runDDL();
        }
        catch (Throwable e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace(System.err);
            StatusDisplayManager.DisplayModalError("Startup error", e.getLocalizedMessage(), true);
        }

    }


    private static void configureLogging() throws IOException, FileNotFoundException,
            FactoryConfigurationError {
        File logDir = new File(baseDirectory(), "logs");
        DirectoryUtils.makeDirectory(logDir);

        File log4jFile = new File(logDir, "log4j.xml");

        File logOutputFile = new File("DDL.log");
        System.out.println("Log file:" + logOutputFile.getAbsolutePath());
        HttpServer.setLogFile(logOutputFile);
        if (logOutputFile.exists()){
           logOutputFile.delete();
           FileOutputStream logOut = new FileOutputStream(logOutputFile);
           PrintStream printStream = new PrintStream(logOut);
           printStream.println("New log created at DDL startup :" + new Date().toString());
           printStream.close();
           logOut.close();
           
        }

        System.out.println("About to configure log4J from file:" + log4jFile.getAbsolutePath() + ": exists?: " + log4jFile.exists());


        if (log4jFile.exists()){
            try{
                DOMConfigurator.configure(log4jFile.getAbsolutePath());
                log = Logger.getLogger(DICOMClient.class.getName());
                log.info("DDL Started");
            }
            catch(Exception e){
                System.err.println("Error loading log4j.xml file");
                e.printStackTrace(System.err);
                throw new RuntimeException("Error loading log4j.xml file ", e);
            }
        }
    }
    private static Properties loadConfiguration() throws IOException {
        
        File ddlPropertiesFile = null;
        String ddlPropertiesURL = System.getProperty("ddl.configuration");
        System.err.println("ddlPropertiesUrl is " + ddlPropertiesURL);

        Properties configProperties = new Properties();
        InputStream configIn = null;
        if (ddlPropertiesURL != null){
            URL ddlURL = new URL(ddlPropertiesURL);
            // Load the server's config file.
            configIn = ddlURL.openStream();
        }
        else 
        if (ddlPropertiesFile != null){
            if (ddlPropertiesFile.exists()){
                configIn = new FileInputStream(ddlPropertiesFile);
            }
            else{
                System.err.println("Missing input ddlPropertiesFile - no file at " + ddlPropertiesFile.getAbsolutePath());
            }
        }
        
        if (configIn != null){
            configProperties.load(configIn);
            configIn.close();
        }
        
        String defaultDICOMRemoteAETitle= getSystemProperty("defaultDICOMRemoteAETitle","UNKNOWN");
        String defaultDICOMRemoteDicomPort=getSystemProperty("defaultDICOMRemoteDicomPort","UNKNOWN");
        String defaultDICOMRemoteHost=getSystemProperty("defaultDICOMRemoteHost","UNKNOWN");
        String defaultLocalHttpPort=getSystemProperty("LocalHttpPort", "16092");

        //String defaultDICOMLocalAETitle=getSystemProperty("defaultDICOMLocalAETitle","MCDICOM");
        //String defaultDICOMLocalDicomPort=getSystemProperty("defaultDICOMLocalDicomPort","3002");
        String defaultDICOMLocalAETitle=getSystemProperty("defaultDICOMLocalAETitle","UNKNOWN");
        String defaultDICOMLocalDicomPort=getSystemProperty("defaultDICOMLocalDicomPort","UNKNOWN");
        String defaultExportMethod = getSystemProperty("defaultExportMethod","FILE");
        String defaultDicomTimeout = getSystemProperty("defaultDicomTimeout", "15");
        File   candidateExportDirectory = new File(baseDirectory(), "DICOMExport");
        String ddlIdentity = null;
        
        String defaultExportDirectory = getSystemProperty("defaultExportDirectory",candidateExportDirectory.getAbsolutePath());
        candidateExportDirectory = new File(defaultExportDirectory);
        if (!candidateExportDirectory.exists()){
            candidateExportDirectory.mkdir();
        }
        System.out.println("defaultExportDirectory is " + candidateExportDirectory.getAbsolutePath());
        
        
        if (!localConfigPresent){
            
            System.out.println("Creating new local configuration file");
            ddlIdentity = createDDLIdentity();
            FileOutputStream fileOut = new FileOutputStream(localConfigFile);
            PrintWriter out = new PrintWriter(fileOut);
            StringBuffer buff = new StringBuffer();
            buff.append("# DDL Configuration file");

            buff.append("\n");
            buff.append("\n## Local device. This is the DICOM information for the DDL");
            buff.append("\n## itself. Third party devices use this information to send");
            buff.append("\n## data to the DDL. These need not be changed unless your site");
            buff.append("\n## requires different different values. ");
            buff.append("\nDICOMLocalAETitle=" + defaultDICOMLocalAETitle);
            buff.append("\nDICOMLocalDicomPort=" + defaultDICOMLocalDicomPort);
            buff.append("\n");
            buff.append("\n# Edit all lines below which contain the string UNKNOWN to contain valid values for your DICOM configuration.");
            buff.append("\n");
            buff.append("\n");

            buff.append("\nDICOMRemoteAETitle="+defaultDICOMRemoteAETitle);
            buff.append("\nDICOMRemoteHost=" + defaultDICOMRemoteHost);
            buff.append("\nDICOMRemotePort=" + defaultDICOMRemoteDicomPort);
            buff.append("\nLocalHttpPort=" + defaultLocalHttpPort);
            buff.append("\n");
            buff.append("\n### DO NOT EDIT THE FOLLOWING LINES ###");
            buff.append("\nConfigVersion=UNKNOWN");
            buff.append("\nSenderAccountId=UNKNOWN");
            buff.append("\nExportMethod=" + defaultExportMethod);
            buff.append("\nExportDirectory=" + defaultExportDirectory );
            buff.append("\nDICOMTimeout=" + defaultDicomTimeout);
            buff.append("\nDDLIdentity=" + ddlIdentity);
            //if (isDoDApplication()){
            buff.append("\nAutomaticUploadToVoucher=true");
            //}
            buff.append("\n");


            out.print(buff.toString());
            out.close();




            System.err.println("No local config file - created default local copy: " + localConfigFile.getAbsolutePath());

        }
        

        // Read in the local config file. The values might or might not be valid (e.g., they
        // might include "UNKNOWN" values.
        FileInputStream f = new FileInputStream(localConfigFile);
        configProperties.load(f);
        f.close();
        System.out.println("Loaded configurations from local file " + localConfigFile.getAbsolutePath());
        

        if (!"UNKNOWN".equalsIgnoreCase(defaultDICOMRemoteAETitle)){
            configProperties.setProperty("DICOMRemoteAETitle", defaultDICOMRemoteAETitle);
        }
        if (!"UNKNOWN".equalsIgnoreCase(defaultDICOMRemoteDicomPort)){
            configProperties.setProperty("DICOMRemotePort", defaultDICOMRemoteDicomPort);
        }
        if (!"UNKNOWN".equalsIgnoreCase(defaultDICOMRemoteHost)){
            configProperties.setProperty("DICOMRemoteHost", defaultDICOMRemoteHost);
        }
        if (!"UNKNOWN".equalsIgnoreCase(defaultDICOMLocalAETitle)){
            configProperties.setProperty("DICOMLocalAETitle", defaultDICOMLocalAETitle);
        }
        if (!"UNKNOWN".equalsIgnoreCase(defaultDICOMLocalDicomPort)){
            configProperties.setProperty("DICOMLocalDicomPort", defaultDICOMLocalDicomPort);
        }
        try{
            String portValue = configProperties.getProperty("DICOMLocalDicomPort");
            if (!"UNKNOWN".equalsIgnoreCase(portValue)){
                int port = Integer.parseInt(portValue);
            }
            else{
                configProperties.setProperty("DICOMLocalDicomPort", "3002");
            }

        }
        catch(Exception e){
            System.err.println("Invalid port number:" + configProperties.getProperty("DICOMLocalDicomPort"));
            e.printStackTrace(System.err);
            configProperties.setProperty("DICOMLocalDicomPort", "3002");
        }
        return configProperties;
    }


    /**
     * Calculates a globally unique id for this instance of the DDL.
     * Concatenates the current time, a random number, and the names and MAC addresses of
     * the local network interfaces and then calculates the SHA1 hash of these values.
     * 
     * On some machines the network interfaces may not be available; in this case the 
     * concatenated string is only the current time in msec and a random number.
     * @return
     */
    private static String createDDLIdentity(){
    	long time = System.currentTimeMillis();
    	StringBuffer buff = new StringBuffer(Long.toString(time));
    	double rand = Math.random();
		buff.append("\n").append(rand);
    	try{
    		Enumeration <NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    		while (interfaces.hasMoreElements()){
    			NetworkInterface anInterface = interfaces.nextElement();
    			String name = anInterface.getName();
    			buff.append("\nName: ").append(name).append(" HardwareAddress: ");
    			byte[] hardwareAddress = anInterface.getHardwareAddress();
    			if (hardwareAddress != null){
	    			for (int i = 0; i < hardwareAddress.length; i++) {
	    	            buff.append(hardwareAddress[i]);
	    	        }
    			}
    			else{
    				System.err.println("Null hardware address for network interface " + name);
    				buff.append("UNKNOWN");
    			}
    			
    		}
    	}
    	catch(SocketException e){
    		System.err.println("Error retrieving socket information");
    		e.printStackTrace(System.err);
    		
    	}
    	String hopefullyUniqueString = buff.toString();
    	System.out.println(hopefullyUniqueString);
    	
    	SHA1 sha1 = new SHA1();
    	sha1.initializeHashStreamCalculation();
    	String ddlIdentity = sha1.calculateStringHash(hopefullyUniqueString);
    	return(ddlIdentity);
    }
    
    private InputStream loadResource(String name) throws IOException{
        ClassLoader loader = getClass().getClassLoader();
         InputStream is = loader.getResourceAsStream( name);

         if (is == null){
             throw new IOException("Resource file not found:" + name);
         }
         return(is);
    }
    
    /**
     * Returns a CommandBlock from the input data arguments if the arguments contain a 'command'.
     * 
     * @return
     */
    private static CommandBlock getCommandBlock(){
    	CommandBlock commandBlock = null;
    	String command = System.getProperty("command");
        System.out.println("Got command " + command);
    	if (command != null){
    		commandBlock = new CommandBlock(command);
    		Properties props = System.getProperties();
    		Enumeration propertyNames = props.propertyNames();
    		while (propertyNames.hasMoreElements()){
    			String name = (String) propertyNames.nextElement();
    			if (name.indexOf("command_") != -1){
    				String parameterName = name.substring(8);
    				System.out.println("Parsed command string " + name  + " as " + parameterName);
    				commandBlock.addProperty(parameterName, props.getProperty(name));
    			}
    			
    		}
    	}
    	return(commandBlock);
    }
    
    private static boolean isDDLAlreadyRunning(){
    	boolean running = false;
    	
    	String configureURL = "http://localhost:16092/localDDL/configure.html";
    	 //create a singular HttpClient object
        HttpClient client = new HttpClient();

        //establish a connection within 5 seconds
        client.getHttpConnectionManager().
            getParams().setConnectionTimeout(5000);

     
        HttpMethod method = new GetMethod(configureURL);
        String responseBody = null;
        try{
            client.executeMethod(method);
            responseBody = method.getResponseBodyAsString();
            if (responseBody != null){
            	running = true;
            	System.out.println("There is an instance of DDL already running");
            }
            else{
            	System.out.println("There is no other DDL currently running");
            }
        } catch (HttpException he) {
            System.err.println("Http error connecting to '" + configureURL + "'");
            System.err.println(he.getMessage());
           
        } catch (IOException ioe){
            System.err.println("Unable to connect to '" + configureURL + "'");
            
        }
        return(running);
    }

    private static void runCommandOnExistingDDL(CommandBlock commandBlock){
	
	
	
    HttpClient client = new HttpClient();

    //establish a connection within 5 seconds
    client.getHttpConnectionManager().
        getParams().setConnectionTimeout(5000);

    

 
    HttpMethod method = null;

    String url = commandBlock.urlRequest();
    System.out.println("About to invoke url:" + url);
    method = new GetMethod(url);
    
    String responseBody = null;
    try{
        client.executeMethod(method);
        responseBody = method.getResponseBodyAsString();
        if (responseBody != null){
        
        	System.out.println(responseBody);
        }
        else{
        	System.err.println("Error:There is no other DDL currently running to run comand on:" + url);
        }
    } catch (HttpException he) {
        System.err.println("Http error connecting to '" + url + "'");
        System.err.println(he.getMessage());
       
    } catch (IOException ioe){
        System.err.println("Unable to connect to '" + url + "'");
        
    }
   
    }
    
    public static void setNativeLookAndFeel() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
          System.out.println("Error setting native LAF: " + e);
        }
      }    
}
