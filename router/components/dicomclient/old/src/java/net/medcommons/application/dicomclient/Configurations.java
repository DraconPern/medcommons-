package net.medcommons.application.dicomclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import net.medcommons.application.dicomclient.utils.Commands;

public class Configurations {

	private boolean clearCache;
	private String dicomRemoteAeTitle;
	private String dicomRemoteHost;
	private int dicomRemotePort;
	private String dicomLocalAeTitle;
	private int dicomLocalPort;
	private int localHttpPort;
	private String gatewayRoot;
	private String senderAccountId;
	private int proxyPort;
	private String configVersion;
	private String version;
	private File configurationFile;
	private File baseDirectory;
	private Date buildTime;
	private File exportDirectory;
	private String exportMethod = Commands.EXPORT_METHOD_CSTORE;
	private int dicomTimeout;
	private String ddlIdentity;
	private boolean automaticUploadToVoucher;

	public void setClearCache(boolean clearCache){
		this.clearCache = clearCache;
	}
	public boolean getClearCache(){
		return(this.clearCache);
	}
	public void setDicomRemoteAeTitle(String dicomRemoteAeTitle){
		this.dicomRemoteAeTitle = dicomRemoteAeTitle;
	}
	public String getDicomRemoteAeTitle(){
		return(this.dicomRemoteAeTitle);
	}

	public void setDicomRemoteHost(String dicomRemoteHost){
		this.dicomRemoteHost = dicomRemoteHost;
	}
	public String getDicomRemoteHost(){
		return(this.dicomRemoteHost);
	}
	public void setDicomRemotePort(int dicomRemotePort){
		this.dicomRemotePort = dicomRemotePort;
	}
	public int getDicomRemotePort(){
		return(this.dicomRemotePort);
	}
	public void setDicomLocalAeTitle(String dicomLocalAeTitle){
		this.dicomLocalAeTitle = dicomLocalAeTitle;
	}
	public String getDicomLocalAeTitle(){
		return(this.dicomLocalAeTitle);
	}
	public void setDicomLocalPort(int dicomLocalPort){
		this.dicomLocalPort = dicomLocalPort;
	}
	public int getDicomLocalPort(){
		return(this.dicomLocalPort);
	}
	public void setLocalHttpPort(int localHttpPort){
		this.localHttpPort = localHttpPort;
	}
	public int getLocalHttpPort(){
		return(this.localHttpPort);
	}
	public void setGatewayRoot(String gatewayRoot){
		this.gatewayRoot = gatewayRoot;
	}
	public String getGatewayRoot(){
		return(this.gatewayRoot);
	}
	public void setSenderAccountId(String senderAccountId){
		this.senderAccountId = senderAccountId;
	}
	public String getSenderAccountId(){
		return(this.senderAccountId);
	}
	public void setProxyPort(int proxyPort){
		this.proxyPort = proxyPort;
	}
	public int getProxyPort(){
		return(this.proxyPort);
	}
	public void setConfigVersion(String configVersion){
		this.configVersion = configVersion;
	}
	public String getConfigVersion(){
		return(this.configVersion);
	}
	public void setVersion(String version){
		this.version = version;
	}
	public String getVersion(){
		return(this.version);
	}

	public void setConfigurationFile(File configurationFile){
		this.configurationFile = configurationFile;
	}
	public File getConfigurationFile(){
		return(this.configurationFile);
	}
	public void setBaseDirectory(File baseDirectory){
		this.baseDirectory = baseDirectory;
	}
	public File getBaseDirectory(){
		return(this.baseDirectory);
	}
	public void setBuildTime(Date buildTime){
		this.buildTime = buildTime;
	}
	public Date getBuildTime(){
		return(this.buildTime);
	}
	public void setExportDirectory(File exportDirectory){
		this.exportDirectory = exportDirectory;
	}
	public File getExportDirectory(){
		return(this.exportDirectory);
	}
	public void setExportMethod(String exportMethod){
		this.exportMethod = exportMethod;
	}
	public String getExportMethod(){
		return(this.exportMethod);
	}
	public void setDicomTimeout(int dicomTimeout){
		this.dicomTimeout = dicomTimeout;
	}
	public int getDicomTimeout(){
		return(this.dicomTimeout);
	}
	public void setAutomaticUploadToVoucher(boolean automaticUploadToVoucher){
		this.automaticUploadToVoucher = automaticUploadToVoucher;
	}
	public boolean getAutomaticUploadToVoucher(){
		return(this.automaticUploadToVoucher);
	}

	public void setDDLIdentity(String ddlIdentity){
		this.ddlIdentity = ddlIdentity;
	}
	public String getDDLIdentity(){
		return(this.ddlIdentity);
	}
	public void save(File f) throws IOException{
		FileWriter out = new FileWriter(f);
		Properties p = new Properties();
		p.setProperty("DICOMRemoteAETitle", this.dicomRemoteAeTitle);
		p.setProperty("DICOMRemoteHost", this.dicomRemoteHost);
		p.setProperty("DICOMRemotePort", Integer.toString(this.dicomRemotePort));
		p.setProperty("DICOMLocalAETitle", this.dicomLocalAeTitle);
		p.setProperty("DICOMLocalDicomPort", Integer.toString(this.dicomLocalPort));
		p.setProperty("DICOMTimeout", Integer.toString(this.dicomTimeout));
		p.setProperty("LocalHttpPort", Integer.toString(this.localHttpPort));
		p.setProperty("AutomaticUploadToVoucher", Boolean.toString(this.automaticUploadToVoucher));
		p.setProperty("DDLIdentity", this.ddlIdentity);
		if (this.exportMethod != null)
			p.setProperty("ExportMethod", this.exportMethod);
		if (this.exportDirectory != null)
			p.setProperty("ExportDirectory", this.exportDirectory.getAbsolutePath());
		p.store(out, "Saved from DDL");
	}

}
