package net.medcommons.application.dicomclient.http.action;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.DownloadHandler;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dl.DLConfigItem;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;

public class ConfigureActionBean extends DDLActionBean {
     private static Logger log = Logger.getLogger(ConfigureActionBean.class);


     public ContextManager getContextManager(){
         return(super.getContextManager());
     }
     /*
    public Resolution getApplicationContext() {
        log.info("getApplicationContext.()");
        ResponseWrapper response = new ResponseWrapper();
        try{
            DlActionContext ctx = (DlActionContext) getContext();
            ContextManager contextManager = getContextManager();

            response.setContents(contextManager);
            response.setStatus(ResponseWrapper.Status.OK);
            response.setMessage("");


            return new JavaScriptResolution(contextManager);
        } catch (Exception e) {
            log.error("about", e);
            response.setStatus(ResponseWrapper.Status.ERROR);
            response.setMessage(e.toString());
            response.setContents(ResponseWrapper.throwableToString(e));
            return (new JavaScriptResolution(response));
        }

    }
    */
    public String getDicomEcho(){
        return(DownloadHandler.Factory().echo(false));
    }
    public Resolution getConfigurations(){

    	 ResponseWrapper response = new ResponseWrapper();

    	 try{
	        List<DLConfigItem> items = configurationItems();
	        response.setContents(items);
	        response.setMessage(ResponseWrapper.Status.OK.name());
    	 }
    	 catch(Exception e){
    		 log.error("Error retrieving configuraions", e);
    		 response.setMessage(e.getLocalizedMessage());
    		 response.setContents(e.getLocalizedMessage());
    		 response.setMessage(ResponseWrapper.Status.ERROR.name());
    	 }
        return (new JavaScriptResolution(response));
    }

    private List<DLConfigItem> configurationItems() {
        List<DLConfigItem> configs = new ArrayList<DLConfigItem>();
        try{
        	configs.add(new DLConfigItem("DICOMEcho", DownloadHandler.Factory().echo(true)));
        }
        catch(Exception e){
        	log.error("Error performing DICOM echo" + e.getLocalizedMessage());
        }

        String hostAddress = "";
        try{
            InetAddress addr = InetAddress.getLocalHost();
            hostAddress = addr.getHostAddress();
            configs.add(new DLConfigItem("Hostaddress", hostAddress));
        }
        catch(UnknownHostException e)
        {
            hostAddress = e.toString();
            log.error("Error getting local address", e);
        }
        Configurations configurations = getContextManager().getConfigurations();
        configs.add(new DLConfigItem("clearCache", configurations.getClearCache()));
        ContextState c = getContextManager().getCurrentContextState();
        String gatewayRoot = "";
        String senderAccountId = "";
        if (c != null){
        	gatewayRoot = c.getGatewayRoot();
        }
        configs.add(new DLConfigItem("dicomRemoteAeTitle", configurations.getDicomRemoteAeTitle()));
        configs.add(new DLConfigItem("dicomRemoteHost", configurations.getDicomRemoteHost()));
        configs.add(new DLConfigItem("dicomRemotePort", configurations.getDicomRemotePort()));
        configs.add(new DLConfigItem("dicomLocalAeTitle", configurations.getDicomLocalAeTitle()));
        configs.add(new DLConfigItem("dicomLocalPort", configurations.getDicomLocalPort()));
        configs.add(new DLConfigItem("localHttpPort", configurations.getLocalHttpPort()));
        configs.add(new DLConfigItem("gatewayRoot", gatewayRoot));
        configs.add(new DLConfigItem("senderAccountId", configurations.getSenderAccountId()));
        configs.add(new DLConfigItem("configVersion", configurations.getConfigVersion()));
        configs.add(new DLConfigItem("version", configurations.getVersion()));
        configs.add(new DLConfigItem("configurationFile", configurations.getConfigurationFile()));
        configs.add(new DLConfigItem("baseDirectory", configurations.getBaseDirectory()));
        configs.add(new DLConfigItem("buildTime", configurations.getBuildTime()));
        configs.add(new DLConfigItem("dicomTimeout", configurations.getDicomTimeout()));
        configs.add(new DLConfigItem("AutomaticUploadToVoucher", configurations.getAutomaticUploadToVoucher()));
        configs.add(new DLConfigItem("DDLIdentity", configurations.getDDLIdentity()));
        configs.add(new DLConfigItem("useRawFileUploads", configurations.isUseRawFileUploads()));
        File exportDirectory = configurations.getExportDirectory();
        String exportDir = "";
        if (exportDirectory != null){
        	exportDir = exportDirectory.getAbsolutePath();
        }
        configs.add(new DLConfigItem("exportDirectory", exportDir));
        configs.add(new DLConfigItem("exportMethod", configurations.getExportMethod()));
        log.info("export method is " + configurations.getExportMethod());
        ContextManager contextManager = getContextManager();
        configs.add(new DLConfigItem("accountId", contextManager.getCurrentContextState().getAccountId()));
        configs.add(new DLConfigItem("auth", contextManager.getCurrentContextState().getAuth()));
        configs.add(new DLConfigItem("cxpHost", contextManager.getCurrentContextState().getCxpHost()));
        configs.add(new DLConfigItem("cxpPath", contextManager.getCurrentContextState().getCxpPath()));
        configs.add(new DLConfigItem("cxpPort", contextManager.getCurrentContextState().getCxpPort()));
        configs.add(new DLConfigItem("cxpProtocol", contextManager.getCurrentContextState().getCxpProtocol()));
        configs.add(new DLConfigItem("groupAccountId", contextManager.getCurrentContextState().getGroupAccountId()));
        configs.add(new DLConfigItem("groupName", contextManager.getCurrentContextState().getGroupName()));
        configs.add(new DLConfigItem("messages", contextManager.getMessages()));
        configs.add(new DLConfigItem("displayUploadedCCRPopup", contextManager.getDisplayUploadedCCR()));
        configs.add(new DLConfigItem("proxyAddress", configurations.getProxyAddress()));
        configs.add(new DLConfigItem("proxyAuthUserName", configurations.getProxyAuthUserName()));
        configs.add(new DLConfigItem("proxyAuthPassword", configurations.getProxyAuthPassword()));

        return(configs);
    }
    @DefaultHandler
    public Resolution about() {
        log.info("== configureAction..");
        ContextManager cm = getContextManager();
        log.info("accountid " + cm.getCurrentContextState().getAccountId());
        log.info("cxp host:" + cm.getCurrentContextState().getCxpHost());
        return new ForwardResolution("configure.html");
    }
}
