package net.medcommons.application.dicomclient;

import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.eq;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.medcommons.application.dicomclient.dicom.CstoreScp;
import net.medcommons.application.dicomclient.http.HttpServer;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.application.dicomclient.utils.StatusMessage;
import net.medcommons.application.upload.StatusUpdate;
import net.medcommons.modules.services.interfaces.GroupPatientQueryService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.TransferStatusService;

import org.apache.log4j.Logger;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;


/**
 * The ContextManager is the root node of the graph for
 * dynamic context information as well as configuration
 * values. This object is initialized once on startup;
 * other classes can access via getContextManager().
 *
 * Note race conditions if this object is used inappropriately
 * to manage state. For example - if the user switches worklists
 * or CXP servers the values are changed here; other classes should
 * capture data from this class when (say) a request is made so
 * that queued transfers to one worklist are not sent to another
 * because this object was updated.
 *
 * TODO: We might put in some synchronize mechanism so that
 * the context is updated in a transaction. We might also persist this
 * object into a database table.
 *
 * TODO: Need to clean up some things here - this object is evolving into
 * a 'manager' which keeps track of server threads so they can be easily
 * started/stopped/replaced. A good thing but we may need to rename or
 * refactor.
 *
 * @author mesozoic
 *
 */
public class ContextManager {
    
	private PropertyChangeSupport  pcs = new PropertyChangeSupport(this);
	
	private List<StatusMessage> messages;
	private CstoreScp currentDcmServer = null;
	private HttpServer httpServer = null;
	private ContextState currentContextState = new ContextState();
	private StatusUpdate statusUpdate = null;
	private boolean useREST = false;
	
	private PromptState promptState = PromptState.NEW;

	private boolean displayUploadedCCR = true;

	private  File downloadCache;

    private  File uploadCache;



	private Configurations configurations;

	private static ContextManager contextManager = null;

	private static Logger log = Logger.getLogger(ContextManager.class.getName());

	public ContextManager() {
	}


	public ContextManager(Configurations configurations){
		if(configurations == null){
			throw new NullPointerException("Context manager initialization requires non-null configuration object");
		}
		if (contextManager != null){
			log.error("@@@@@@@@@ContextManager already initialized " + contextManager);
			this.configurations = configurations;
			return; // Don't set static variable - it's immutable.
			//throw new IllegalStateException("ContextManager already initialized:" + contextManager);
		}
		
		this.configurations = configurations;
		contextManager = this;
	}



	public static ContextManager get(){
		return contextManager;
	}
	public Configurations getConfigurations(){
		return(this.configurations);
	}
	//public void setConfigurations(Configurations configurations){
	//	this.configurations = configurations;
	//}
	
	public String getLocalHostAddress(){
		String hostAddress;
		try{
		InetAddress addr = InetAddress.getLocalHost();
        hostAddress = addr.getHostAddress();
		}
		catch(UnknownHostException e){
			hostAddress = e.toString();
		    log.error("Unknown host", e);
		}
        return(hostAddress);
	}
	public String getLocalHostName(){
		String hostName;
		try{
		InetAddress addr = InetAddress.getLocalHost();
		hostName = addr.getHostName();
		}
		catch(UnknownHostException e){
			hostName = e.toString();
		    log.error("Unknown host", e);
		}
        return(hostName);
	}

	public String toString(){
		StringBuffer buff = new StringBuffer("ContextManager[");
		
		return(buff.toString());

	}
	public void downloadGuid(boolean downloadReferences, ContextState contextState){
		try{
			DownloadHandler downloadHandler = DownloadHandler.Factory();
			downloadHandler.queueDownload(downloadReferences,  contextState);
		}
		catch(Exception e){
			log.error("Error downloading " + this, e);
		}
	}

	public void setMessages(List<StatusMessage> messages){
		this.messages = messages;
	}
	public List<StatusMessage> getMessages(){
		return(this.messages);
	}
	public void clearMessages(){
		if (this.messages != null)
			this.messages.clear();
	}
	public void addMessage(StatusMessage message){
		if (this.messages == null){
			this.messages = new ArrayList<StatusMessage>();
		}
		this.messages.add(message);
	}

	public void setDownloadCache(File downloadCache){
		this.downloadCache = downloadCache;
	}
	public File getDownloadCache(){
		return(this.downloadCache);
	}
	public void setUploadCache(File uploadCache){
		this.uploadCache = uploadCache;
	}
	public File getUploadCache(){
		return(this.uploadCache);
	}
	public void setStatusUpdate(StatusUpdate statusUpdate){
		this.statusUpdate = statusUpdate;
	}
	public StatusUpdate getStatusUpdate(){
		return(this.statusUpdate);
	}
	public CstoreScp getDcmServer(){
		return(this.currentDcmServer);
	}
	public void setDisplayUploadedCCR(boolean displayUploadedCCR){
		this.displayUploadedCCR = displayUploadedCCR;
	}
	public boolean getDisplayUploadedCCR(){
		return(this.displayUploadedCCR);
	}
	
	public void startDcmServer() {

        try {
            Configurations configurations = contextManager.getConfigurations();
            if (configurations == null){
                throw new NullPointerException("ContextManager configuration is null");
            }
            ImportHandler importHandler = new ImportHandler();
            UploadHandler uploadHandler = new UploadHandler();


            currentDcmServer = new CstoreScp(importHandler);
            currentDcmServer.setDimseRspDelay(0);
            currentDcmServer.setCacheDirectory(contextManager.getUploadCache().getAbsolutePath());

            String sListeningPort = configurations.getDicomLocalPort() + "";
            log.info("sListeningPort = '" + sListeningPort + "'");
            int currentPort = CstoreScp.parseInt(sListeningPort,"illegal port number", 1, 0xffff);
            currentDcmServer.setPort(configurations.getDicomLocalPort());
            currentDcmServer.setAEtitle(configurations.getDicomLocalAeTitle());

            log.info("Starting DICOM CSTORE SCP on port " + currentPort
                    + " with AETitle " + configurations.getDicomLocalAeTitle());
            log.info(currentDcmServer.getVersion());

            Thread t = new Thread(currentDcmServer);
            t.start();

        }
        catch (Exception e) {
            log.error("Exception starting DDL:", e);
            StatusDisplayManager.get().setErrorMessage("Error starting DDL", e.getLocalizedMessage());
        }

    }

    public void stopDcmServer() {

        if (currentDcmServer != null) {
            log.info("Stopping CSTORE server");
            currentDcmServer.stop();
            currentDcmServer = null;
        }
    }

    public void startHttpServer() throws Exception{
    	 httpServer = new HttpServer();
         httpServer.start();
    }
    public void stopHttpServer() throws Exception{
    	httpServer.stop();
    }

    public boolean getUseREST(){
    	return(this.useREST);
    }
    
    public void setUseREST(boolean useREST){
    	this.useREST = useREST;
    }
    
    public void setCurrentContextState(ContextState currentContextState){
        
        // Check if the context state is a known one
        currentContextState.checkAllowed();
        
    	ContextState old = this.currentContextState;
    	this.currentContextState = currentContextState;
    	
        if(blank((currentContextState.getApplianceRoot()))) {
            currentContextState.setApplianceRoot(currentContextState.getGatewayRoot());
        }
        
        if(!eq(this.currentContextState.getApplianceRoot(), old.getApplianceRoot()))
            this.queryService = null;
    	
    	this.pcs.firePropertyChange("currentContextState", old, currentContextState);
    	log.info("Current context state set to " + currentContextState);
    }
    
    
    /**
     * Cached patient query service
     */
    GroupPatientQueryService queryService = null;

    /**
     * Returns an instance of the patient query service which communicates directly with the appliance
     * to query patients from the group patient list.
     */
    public GroupPatientQueryService getQueryService() throws ServiceException {
        
        if(this.currentContextState == null || blank(this.currentContextState.getApplianceRoot()))
            throw new IllegalStateException("No context state set:  cannot query patient service");
        
        try {
            Service serviceModel = new ObjectServiceFactory().create(GroupPatientQueryService.class,
                            "GroupPatientQueryServiceImpl", "http://ws.identity.medcommons.net", null);

            String url = getCurrentContextState().getApplianceRoot() 
                           + "/identity/ws/GroupPatientQueryServiceImpl";
                         
            log.info("Connecting to query service at " + url);
            queryService = (GroupPatientQueryService)new XFireProxyFactory().create(serviceModel, url);
            
        }
        catch (Exception e) {
            throw new ServiceException("Unable to create GroupPatientQueryService", e);
        }
        return queryService;
    }
    
    public ContextState getCurrentContextState(){
    	return(this.currentContextState);
    }

    public void addListener(PropertyChangeListener listener) {
    	this.pcs.addPropertyChangeListener(listener);
    }
    
    public void removeListener(PropertyChangeListener listener) {
    	this.pcs.removePropertyChangeListener(listener);
    }
}
