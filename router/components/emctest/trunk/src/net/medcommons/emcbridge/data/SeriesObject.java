package net.medcommons.emcbridge.data;

import java.util.ArrayList;



public class SeriesObject implements ReferenceObject{
	ArrayList<DicomWrapper> images = new ArrayList<DicomWrapper>();
	String seriesInstanceUID;
	String studyInstanceUID;
	String seriesFolderId;
	
	public SeriesObject(String studyInstanceUID, String seriesInstanceUID, String seriesFolderId ){
		this.studyInstanceUID = studyInstanceUID;
		this.seriesInstanceUID = seriesInstanceUID;
		this.seriesFolderId = seriesFolderId;
	}
	public String getIdentifier(){
		return(this.seriesInstanceUID);
	}
	public String getSeriesFolderId(){
		return(this.seriesFolderId);
	}
	public String getTitle(){
		if (images.size()==0) return null;
		DicomWrapper d = images.get(0);
		return(d.getDicomMetadata().getSeriesDescription());
	}
	public ArrayList<DicomWrapper> getImages(){
		return(this.images);
	}
	public void addImage(DicomWrapper dicomWrapper){
		String seriesUID = dicomWrapper.getDicomMetadata().getSeriesInstanceUid();
		if (seriesUID.equals(seriesInstanceUID)){
			images.add(dicomWrapper); // Need to make this sortable.
		}
		else{
			throw new IllegalArgumentException("Attempt to add image with series instance UID " + seriesUID + " to a series object with UID " +
					seriesInstanceUID);
		}
	}
}