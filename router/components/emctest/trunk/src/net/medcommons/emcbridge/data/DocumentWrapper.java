package net.medcommons.emcbridge.data;

import java.io.File;

import com.documentum.fc.client.IDfSysObject;

public class DocumentWrapper implements ReferenceObject{
	IDfSysObject documentumData;
	String title;
	File documentFile;
	String identifier;
	
	public DocumentWrapper(IDfSysObject documentumData, String title, File documentFile, String identifier){
		this.documentumData = documentumData;
		this.title = title;
		this.documentFile = documentFile;
		this.identifier = identifier;
	}
	public IDfSysObject getDocumentumData(){
		return(this.documentumData);
	}
	public String getTitle(){
		return(this.title);
	}
	public File getDocumentFile(){
		return(this.documentFile);
	}
	public String getIdentifier(){
		return(this.identifier);
	}
	
}
