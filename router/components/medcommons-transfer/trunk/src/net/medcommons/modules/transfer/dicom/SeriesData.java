package net.medcommons.modules.transfer.dicom;

import java.util.ArrayList;

import net.medcommons.modules.services.interfaces.DicomMetadata;



public class SeriesData {
	private String guid = null;
	private String seriesInstanceUID = null;
	/*
	 * This is the storage id.
	 */
	private String medcommonsId = null;
	
	private ArrayList<DicomMetadata> instances = new ArrayList<DicomMetadata>();
	
	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return(this.guid);
	}
	public void setMedcommonsId(String medcommonsId){
		this.medcommonsId = medcommonsId;
	}
	public String getMedcommonsId(){
		return(this.medcommonsId);
	}
	public String getSeriesInstanceUID(){
		return(this.seriesInstanceUID);
	}
	public void addInstance(DicomMetadata metadata){
		if (seriesInstanceUID == null){
			seriesInstanceUID = metadata.getSeriesInstanceUid();
		}
		else{
			String uid = metadata.getSeriesInstanceUid();
			if (!uid.equals(seriesInstanceUID)){
				throw new IllegalArgumentException("Attempt to add a series instance uid of '" +
						uid + "' to an existing series with uid '" + seriesInstanceUID +"'");
			}
		}
		instances.add(metadata);
	}
	public ArrayList<DicomMetadata> getInstances(){
		return(instances);
	}

}
