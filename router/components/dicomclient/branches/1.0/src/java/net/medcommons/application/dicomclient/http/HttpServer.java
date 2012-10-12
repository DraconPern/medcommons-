package net.medcommons.application.dicomclient.http;

import static net.medcommons.application.dicomclient.utils.Params.where;
import static net.medcommons.application.utils.Str.blank;
import static net.medcommons.application.utils.Str.bvl;

import java.io.*;
import java.net.BindException;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.*;
import net.medcommons.application.dicomclient.utils.Shutdown;
import net.medcommons.modules.cxp.CXPConstants;
import net.sourceforge.pbeans.Store;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.deployer.WebAppDeployer;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;

/**
 * Overview of protocol:
 *
 * The embedded HTTP server is invoked by JavaScript running in a web browser. These commands are used to tell the
 * background application to do things or to set state for future actions.
 * <UL>
 * <LI> setDocumentFocus - sets the information for the CCR of interest. This is interpreted by the
 *      DDL as 'download DDL for demographic info and for adding DICOM but don't download DICOM"</LI
 * <LI> downloadDocument - download the </LI>
 * <LI> setAccountFocus
 * <LI> setAuthorizationContext
 * <LI> tnum - initiates the complete download of a CCR with DICOM references.
 * </UL>
 *
 * <P>
 * The embedded HTTP server also deploys a standard WAR file for the status management.
 * @author mesozoic
 *
 */
public class HttpServer {
    
	private static Logger log = Logger.getLogger(HttpServer.class);
	
	Server server = null;

	private static File logFile = null;

	boolean useCurrentWarFile = false;

	public HttpServer () throws Exception {

		ContextManager contextManager = ContextManager.get();
		Configurations configurations = contextManager.getConfigurations();
		server = new Server(configurations.getLocalHttpPort());

	    deployLocalWarFile("localDDL.war");
	    ContextHandlerCollection contexts = new ContextHandlerCollection();

	    server.addHandler(contexts);
	     
	    Context docFocus = new Context(contexts,"/setDocumentFocus",Context.SESSIONS);
	    docFocus.addServlet("net.medcommons.application.dicomclient.http.HttpServer$SetDocumentFocusServlet", "/*");
	    
	    Context acctFocus = new Context(contexts,"/setAccountFocus",Context.SESSIONS);
	    acctFocus.addServlet("net.medcommons.application.dicomclient.http.HttpServer$SetAccountFocusServlet", "/*");
	    
	    Context authContext = new Context(contexts,"/setAuthorizationContext",Context.SESSIONS);
	    authContext.addServlet("net.medcommons.application.dicomclient.http.HttpServer$SetAuthorizationContextServlet", "/*");
	    
	    Context tnum = new Context(contexts,"/tnum",Context.SESSIONS);
	    tnum.addServlet("net.medcommons.application.dicomclient.http.HttpServer$TnumServlet", "/*");
	    
	    Context log = new Context(contexts,"/log",Context.SESSIONS);
	    log.addServlet("net.medcommons.application.dicomclient.http.HttpServer$logServlet", "/*");
	    
	    Context downloadDocument = new Context(contexts,"/downloadDocument",Context.SESSIONS);
	    downloadDocument.addServlet("net.medcommons.application.dicomclient.http.HttpServer$DownloadDocumentServlet", "/*");
	    
	    
	    Context shutdownServlet = new Context(contexts,"/shutdown",Context.SESSIONS);
	    shutdownServlet.addServlet("net.medcommons.application.dicomclient.http.HttpServer$ShutdownServlet", "/*");
	    
	    Context openFileChooserServlet = new Context(contexts,"/openFileChooser",Context.SESSIONS);
	    openFileChooserServlet.addServlet("net.medcommons.application.dicomclient.http.HttpServer$OpenFileChooser", "/*");
	    
	    Context clearAuthorizationContextServlet = new Context(contexts,"/clearAuthorizationContext",Context.SESSIONS);
	    clearAuthorizationContextServlet.addServlet("net.medcommons.application.dicomclient.http.HttpServer$ClearAuthorizationContextServlet", "/*");
	    
	    
	    Context commandServlet = new Context(contexts,"/CommandServlet",Context.SESSIONS);
	    commandServlet.addServlet("net.medcommons.application.dicomclient.http.CommandServlet", "/*");
	}

	/**
	 * Two step deployment:
	 * <ol>
	 * <li> Copy the war file from the classpath and place it into the local directory.
	 * <li> Load the war file into Jetty.
	 * </ol>
	 * @param warFilename
	 * @throws IOException
	 * @throws Exception
	 */
	private void deployLocalWarFile(String warFilename) throws IOException, Exception{

		long startDeployTime = System.currentTimeMillis();
		Configurations configurations = ContextManager.get().getConfigurations();
		File baseDirectory = configurations.getBaseDirectory();
		File webappDirectory = new File(baseDirectory, "webapps");
		if (!webappDirectory.exists()){
			 DirectoryUtils.makeDirectory(webappDirectory);
		}
		File deployTo = new File(webappDirectory, warFilename);
		if(!useCurrentWarFile) {
	        deployFile(warFilename, deployTo);
		}
		else {
			if(!deployTo.exists()) {
				log.fatal("War file " + deployTo.getAbsolutePath() + " does not exist");
				throw new Exception("Can not start local web deployment, war file does not exist");
			}
			else 
				log.info("Using existing war file:" + deployTo.getAbsolutePath());
		}
		long extractedTime = System.currentTimeMillis();

		// Originally we let Jetty pick a temporary directory, however that seems to cause some problems
		// because the tmp directories on MacOSX contain weird characters that can screw up 
		// deployment (+ signs).  This might sitll be an issue if the user's home directry contains
		// those characters, however short of hacking jetty to handle the folder encoding properly
		// there isn't much we can do.
		File jettyHome = new File(baseDirectory, "jetty");
		File jettyWork = new File(jettyHome,"work");
		jettyWork.mkdirs();
		
		System.setProperty("jetty.home",jettyHome.getCanonicalPath());
		 
		 log.info("Jetty Home = " + System.getProperty("jetty.home"));
		 WebAppDeployer webAppDeployer = new WebAppDeployer();
         webAppDeployer.setContexts( server );
         webAppDeployer.setWebAppDir(webappDirectory.getAbsolutePath() );
         webAppDeployer.setExtract( true );
         webAppDeployer.setParentLoaderPriority( true );
         webAppDeployer.setAllowDuplicates(true);
         webAppDeployer.start();
         
         server.addLifeCycle(webAppDeployer);
         long timeCompleted = System.currentTimeMillis();
         log.info("Extraction time:" + (extractedTime - startDeployTime) + "msec, Deploy time = " + (timeCompleted - extractedTime));

	}

	/**
	 * Copy the specified classloader resource to a physical file at the 
	 * deployTo location.
	 * 
	 * @param resourceName      classloader resource to deploy
	 * @param deployTo         location to deploy to
	 */
    private void deployFile(String resourceName, File deployTo) throws IOException, FileNotFoundException {
        ClassLoader loader = getClass().getClassLoader();
        InputStream is = loader.getResourceAsStream( resourceName);
        if(is == null)
            throw new IOException("Class loader resource not found:" + resourceName);

        FileOutputStream fout = new FileOutputStream(deployTo);
        int total = 0;
        try {
            byte buffer[] = new byte[32*1024];
            int c;
            while((c=is.read(buffer))!=-1) {
                fout.write(buffer,0,c);
                
                // A good way to simulate an obscure startup failure!
                // fout.write(buffer,0,c-5);
                
                total += c;
            }
        }
        finally {
            fout.flush();
            fout.close();
        }
        
        if(deployTo.length() != total) 
            throw new IllegalStateException("Deployed file has incorrect length: " + deployTo.length() + " instead of " + total);
            
        log.info("Deployed classloader resource " + resourceName + " to " + deployTo.getAbsolutePath());
    }
    
	public void start() throws Exception {
		Configurations configurations = ContextManager.get().getConfigurations();
		try{
			log.info("Starting HTTP server on port " + configurations.getLocalHttpPort());
			server.start();
		}
		catch(BindException e){
			
			StatusDisplayManager.DisplayModalError("Resource Conflict",
					"Only one copy of DDL can run at a time. Please quit running version and try again. "+
					"Or, perhaps another application is using port " +
					configurations.getLocalHttpPort(), false

					);
			throw e;
		}
		// server.join();
	}

	public void stop() throws Exception {

		server.stop();

	}

	private static class shutdownThread implements Runnable{
		public void run(){

			try{
				Thread.sleep(1000); // Sleep so that the routine that called this has time to return.
				log.info("About to shut down DDL");
				Shutdown.cleanup();
			}
			catch(InterruptedException e){;}
			finally{
				System.exit(0);
			}
		}
	}
	
	public static class ShutdownServlet extends HttpServlet {
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			Enumeration<String> pnames = request.getParameterNames();
			StringBuffer buf = new StringBuffer("ShutDownServlet (default)");
			buf.append("\n<br>");
			buf.append("url");
			buf.append(request.getRequestURI());
			response.getWriter().println("<h1>ShutdownServlet</h1>");
			while (pnames.hasMoreElements()){
				String pname = pnames.nextElement();
				String value = request.getParameter(pname);
				buf.append("\n<br>");
				buf.append(pname);
				buf.append(":");
				buf.append(value);

			}
			response.getWriter().println(buf.toString());
			response.flushBuffer();
			shutdownThread shutdown = new shutdownThread();
			new Thread(shutdown).start();


			log.info(buf.toString());
		}
	}
	/**
	 * Download GUID for possible future download. Two cases:
	 * <ul>
	 *  <li> Add DICOM - just download the CCR and store it locally </li>
	 *  <li> Download DICOM - download CCR, then download DICOM references </li>
	 * </ul>
	 * @author mesozoic
	 *
	 */
	public static class SetDocumentFocusServlet extends HttpServlet{
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			log.info("=====SetDocumentFocusServlet====");
	        response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
	        response.setHeader("Pragma","no-cache"); // HTTP 1.0
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			try{
				Map<String, String[]> parameters = request.getParameterMap();
				ContextManager contextManager = ContextManager.get();
				ContextState contextState = generateContextState(parameters);
				contextManager.setCurrentContextState(contextState);
				
				boolean downloadReferences = false;
				
				contextManager.downloadGuid(downloadReferences, contextState);
	
				JSONObject result = new JSONObject();
				result.put("status", "ok");
				result.put("url",request.getRequestURI());
				response.getWriter().println(result.toString());
	
				contextState.getGatewayRoot();
				log.info(contextManager.toString());
			}
			catch(Exception e){
				log.error("SetDocumentFocusServlet error", e);
				response.getWriter().println(e.getLocalizedMessage());
				StatusDisplayManager sdm = StatusDisplayManager.get();
				sdm.setErrorMessage("SetDocumentFocusServlet:Communication error", e.getLocalizedMessage());
				
			}
		}
	}
	
	public static class logServlet extends HttpServlet{
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_OK);
			
			long sizeLimit = 1024 * 32;

			//OutputStream out = response.getOutputStream();
			Writer out = response.getWriter();

			if (logFile == null){
			   out.write("Log file not initialized"); 
			}
			if (logFile.exists()){
				long size = logFile.length();


				FileInputStream in = new FileInputStream(logFile);
				if (size > sizeLimit){
					in.skip(size - sizeLimit);
					out.write("\n Log file too large:" + logFile.getAbsolutePath() + ":" + size + " bytes");
					out.write("\n skipping " + (size - sizeLimit) + " bytes...\n\n");
				}
				InputStreamReader readIn = new InputStreamReader(in);
				int i;
				char buffer[] = new char[4096];

				while((i=readIn.read(buffer)) != -1){

					out.write(buffer, 0, i);
				}

			}
			else{
				out.write("Log file " + logFile.getAbsolutePath() + " does not exist");
			}


		}
	}
	/**
	 * Downloads selected guid and any embedded DICOM. Assumes that the
	 * guid refers to a CCR with embedded references.
	 * Now obsolete.
	 * @author mesozoic
	 *
	 */
	public static  class TnumServlet extends HttpServlet{
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			log.error("=====TnumServlet====");
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.info("uri:" + request.getRequestURI());
			log.info("url:" + request.getRequestURL());
			Map<String, String[]> parameters = request.getParameterMap();
			ContextManager contextManager = ContextManager.get();
			ContextState contextState = generateContextState(parameters);
			contextManager.setCurrentContextState(contextState);
			
			StringBuffer buf = new StringBuffer("tmon");
			buf.append("\nObsolete Protocol\n");
			buf.append("\nUse downloadDocument instead\n");
			buf.append("url");
			buf.append(request.getRequestURI());
			log.error(buf.toString());

			response.getWriter().println(buf.toString());


		}
	}
	/**
	 * Downloads selected guid and any embedded DICOM. Assumes that the
	 * guid refers to a CCR with embedded references.
	 * Now obsolete.
	 * @author mesozoic
	 *
	 */
	public static  class DownloadDocumentServlet extends HttpServlet{
	    
		ContextManager contextManager = ContextManager.get();
		
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
		    
			log.info("=====DownloadDocumentServlet====");
	        response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
	        response.setHeader("Pragma","no-cache"); // HTTP 1.0
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			log.info("uri:" + request.getRequestURI());
			JSONObject result = new JSONObject();
			try {
				Map<String, String[]> parameters = request.getParameterMap();
	
				ContextState contextState = generateContextState(parameters);
				contextManager.setCurrentContextState(contextState);
				
				log.info(contextManager.toString());
				boolean downloadReferences = true;
				contextManager.downloadGuid(downloadReferences,contextState);
				
				result.put("status", "ok");
				result.put("url",request.getRequestURI());
			}
			catch(Exception e){
				log.error("SetDocumentFocusServlet error", e);
				result.put("status", "error");
				result.put("error", e.getMessage());
				StatusDisplayManager sdm = StatusDisplayManager.get();
				sdm.setErrorMessage("SetDocumentFocusServlet:Communication error", e.getLocalizedMessage());
			}
			
			String jsonp = bvl(request.getParameter("jsonp"),"");
			response.getWriter().println(jsonp + "(" + result.toString() + ")");
			response.flushBuffer();
		}
	}

	/**
	 * Removes any previous authorization, group or patient account configurations. 
	 * Leaves intact the CXP endpoint and other network configurations.
	 * 
	 * @author sean
	 *
	 */
	public static class ClearAuthorizationContextServlet extends HttpServlet{
        ContextManager contextManager = ContextManager.get();
        protected void doGet(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {

            response.setStatus(HttpServletResponse.SC_OK);
	        response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
	        response.setHeader("Pragma","no-cache"); // HTTP 1.0
            log.info("uri:" + request.getRequestURI());
            log.info("url:" + request.getRequestURL());
			JSONObject result = new JSONObject();
            try {
	
	            clearContextParameters();
	           
				result.put("status", "ok");
				result.put("url",request.getRequestURI());
				
	            log.info(contextManager.toString());
	            StatusDisplayManager.get().setMessage("Clear Authorization Context", 
	                    "Cleared previous authorization context" );
            }
            catch(Exception e){
				result.put("status", "error");
				result.put("error", e.getMessage());
				log.error("ClearAuthorizationContextServlet error", e);
				StatusDisplayManager sdm = StatusDisplayManager.get();
				sdm.setErrorMessage("ClearAuthorizationContextServlet:Communication error", e.getLocalizedMessage());
				
			}
			String jsonp = bvl(request.getParameter("jsonp"),"");
			response.getWriter().println(jsonp + "(" + result.toString() + ")");
			response.flushBuffer();
        }
    }
	
	/**
	 * Sets the context of the local DDL including auth, gateway to send to and other 
	 * information.  Responds with JSON object with attribute 'status' == 'ok' if
	 * successful.  Supports JSONP to allow client callbacks.
	 * 
	 * @author ssadedin
	 */
	public static class SetAccountFocusServlet extends HttpServlet{
		ContextManager contextManager = ContextManager.get();
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		    
            response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
            response.setHeader("Pragma","no-cache"); // HTTP 1.0
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_OK);
			
			log.info("uri:" + request.getRequestURI());
			log.info("url:" + request.getRequestURL());
			JSONObject result = new JSONObject();
			try {
				Map<String, String[]> parameters = request.getParameterMap();
	
				ContextState contextState = generateContextState(parameters);
				contextManager.setCurrentContextState(contextState);
				StatusDisplayManager sdm = StatusDisplayManager.get();
             	sdm.setCurrentWorklist(contextState.getCxpHost(), contextState.getGroupName());
            	result.put("status","ok");
				log.info(contextState.toString());
			}
			catch(Exception e){
				log.error("SetAccountFocusServlet error", e);
				result.put("status","failed");
				result.put("error", e.getMessage());
				StatusDisplayManager sdm = StatusDisplayManager.get();
				sdm.setErrorMessage("SetAccountFocusServlet:Communication error", e.getLocalizedMessage());
			}
			
			String jsonp = bvl(request.getParameter("jsonp"),"");
			response.getWriter().println(jsonp + "(" + result.toString() + ")");
			response.flushBuffer();
		}
	}
	public static class SetAuthorizationContextServlet extends HttpServlet{
		ContextManager contextManager = ContextManager.get();
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			try{
				Map<String, String[]> parameters = request.getParameterMap();
	
				ContextState contextState = generateContextState(parameters);
				contextManager.setCurrentContextState(contextState);
				StatusDisplayManager sdm = StatusDisplayManager.get();
             	sdm.setCurrentWorklist(contextState.getCxpHost(), contextState.getGroupName());
				StringBuffer buf = new StringBuffer("setAuthorizationContext");
				buf.append("\n<br>");
				buf.append("url");
				buf.append(request.getRequestURI());
				response.getWriter().println("AuthorizationContext");
	
				response.getWriter().println(contextManager.toString());
				log.info(buf.toString());
				log.info(contextState.toString());
			}
			catch(Exception e){
				log.error("SetAuthorizationContextServlet error", e);
				response.getWriter().println(e.getLocalizedMessage());
				StatusDisplayManager sdm = StatusDisplayManager.get();
				sdm.setErrorMessage("SetAuthorizationContextServlet:Communication error", e.getLocalizedMessage());
				
			}
			
			/*
			 StatusDisplayManager sdm = StatusDisplayManager.getStatusDisplayManager();
			sdm.setCurrentWorklist(contextManager.getCxpHost(), contextManager.getGroupName());
			*/

		}
	}
	
	private static void clearContextParameters(){
	    
        ContextManager contextManager = ContextManager.get();
        Configurations configurations = contextManager.getConfigurations();
        /*
        contextManager.setAccountId("");
        contextManager.setAccountId("");
        contextManager.setGroupAccountId("");
        contextManager.setGroupName("");
        contextManager.setGuid("");
        contextManager.setStorageId("");
        configurations.setSenderAccountId("");
        
        log.info("Cleared context parameters");
        */
       
    }
	
	private  static ContextState generateContextState(Map<String, String[]> parameters){
		ContextManager contextManager = ContextManager.get();
		ContextState ctx = new ContextState();
		
		Set<String> names = parameters.keySet();
		Iterator<String> iter = names.iterator();
		Configurations configurations = contextManager.getConfigurations();
		log.info("Context parameters");
		while(iter.hasNext()){
			String name= iter.next();
			String values[] = parameters.get(name);
			String value = values[0];
			log.info("name:" + name + ", value:" + value);
			if ("storageId".equalsIgnoreCase(name))
			    ctx.setStorageId(value);
			else if ("cxpProtocol".equalsIgnoreCase(name))
			    ctx.setCxpProtocol(value);
			else if ("cxpHost".equalsIgnoreCase(name))
			    ctx.setCxpHost(value);
			else if ("cxpPort".equalsIgnoreCase(name))
			    ctx.setCxpPort(value);
			else if ("cxpPath".equalsIgnoreCase(name))
			    ctx.setCxpPath(value);
			else if ("guid".equalsIgnoreCase(name))
			    ctx.setGuid(value);
			else if ("applianceRoot".equalsIgnoreCase(name))
			    ctx.setApplianceRoot(value);
			else if ("accountid".equalsIgnoreCase(name)){
				if ((value != null) && (!value.equals("undefined"))){
				    ctx.setAccountId(value);
					configurations.setSenderAccountId(value); // Need to clean
                                                                // up - set in
                                                                // two places?
				}
			}
			else if ("auth".equalsIgnoreCase(name)){
			    ctx.setAuth(value);
			}
			else if ("groupAccountId".equalsIgnoreCase(name)){
				if ((value != null) && (!value.equals("undefined"))){
				    ctx.setGroupAccountId(value);
				}
			}
			else if ("groupName".equalsIgnoreCase(name)){
				if ((value != null) && (!value.equals("undefined"))){
				    ctx.setGroupName(value);
				}
			}
			else if ("flags".equalsIgnoreCase(name)){
				if ((value != null) && (!value.equals("undefined"))){
				    ctx.setFlags(value);
				}
			}
			}
		// configurations.setGatewayRoot(contextState.getGatewayRoot());
        log.info("CXP gateway root URL set to " + configurations.getGatewayRoot());
        log.info("Context state  " + ctx.toString());
        if (CXPConstants.POPS_MEDCOMMONS_ID.equals(ctx.getStorageId())){
        	throw new IllegalArgumentException("DDL does not work with POPS storage accounts for security reasons");
  	
        }
        if (CXPConstants.POPS_MEDCOMMONS_ID.equals(ctx.getAccountId())){
        	throw new IllegalArgumentException("DDL does not work with POPS authentication accounts for security reasons");
  	
        }
        Store db = DB.get();
        ContextState existingState = 
                db.selectSingle(ContextState.class,
			                       where("accountId", ctx.getAccountId())
                                    .and("auth", ctx.getAuth())
                                    .and("groupAccountId", ctx.getGroupAccountId())
                                    .and("storageId", ctx.getStorageId())
                                    .and("guid",ctx.getGuid()));
        
        if (existingState == null) {
        	db.save(ctx);
        }
        else{
        	ctx = existingState;
        }
        return(ctx);
	}
		
	
	/**
	 * Launches file chooser for uploading files.
	 * @author mesozoic
	 *
	 */
	public static  class OpenFileChooser extends HttpServlet{
		ContextManager contextManager = ContextManager.get();
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			log.error("=====OpenFileChooser====");
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			log.info("uri:" + request.getRequestURI());
			log.info("url:" + request.getRequestURL());
			//Map<String, String[]> parameters = request.getParameterMap();

			//generateContextState(parameters);
			StringBuffer buf = new StringBuffer("uploadFileChooser");
			buf.append("url");
			buf.append(request.getRequestURI());


			response.getWriter().println(buf.toString());

			log.info("====" + buf.toString());
			log.info(contextManager.toString());
			new OpenFileThread().start();
			
			
		}
	}
	private static class OpenFileThread extends Thread{
		public void run(){
			log.info("In OpenFileThread");
			DicomFileChooser fc = new DicomFileChooser(null, false);
		}
	}
	public static void setLogFile(File f){
	    logFile = f;
	}
 
}
