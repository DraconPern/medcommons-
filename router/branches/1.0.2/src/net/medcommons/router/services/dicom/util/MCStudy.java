/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;


/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCStudy implements Serializable {

	private String StudyInstanceUID = null;
	private String PatientName = null;
	private String PatientID = null;
	private String StudyDate = null;
	private String StudyTime = null;
	private String StudyDescription = null;
	private String modality = null;
	private Set series = null;
    
  public Long id;
	
	public MCStudy(){
		;
	}

	
	/**
   * Creates an MCStudy from the given DICOM header information.
   * 
   * @param sha - will be used to gneerate a guid for the study
   * @param ds - study meta data will be extracted from the dicom header in this Dataset
   */
  public MCStudy(DicomObject ds) {

    this.setPatientName(ds.getString(Tag.PatientName));
    this.setPatientID(ds.getString(Tag.PatientID));
    this.setStudyInstanceUID(ds.getString(Tag.StudyInstanceUID));
    this.setStudyDate(ds.getString(Tag.StudyDate));
    this.setStudyTime(ds.getString(Tag.StudyTime));
    this.setStudyDescription(ds.getString(Tag.StudyDescription));
    this.setModality(ds.getString(Tag.Modality));
    this.setSeries(new HashSet());    
  }
  

	public String toString(){
		StringBuffer buff = new StringBuffer("Study[");
		buff.append(PatientName);
		buff.append(",");
		buff.append(StudyDate);
		buff.append(",");
		buff.append(StudyDescription);
		buff.append("]");
		return(buff.toString());
	}

  /**
   * @return Returns the patientID.
   */
  public String getPatientID() {
    return PatientID;
  }
  /**
   * @param patientID The patientID to set.
   */
  public void setPatientID(String patientID) {
    PatientID = patientID;
  }
  /**
   * @return Returns the patientName.
   */
  public String getPatientName() {
    return PatientName;
  }
  /**
   * @param patientName The patientName to set.
   */
  public void setPatientName(String patientName) {
    PatientName = patientName;
  }
  
  /**
   * @return - the series at index i
   */
  public MCSeries getSeries(int index) {
    
    // SS: hacky - can't figure out how to make hibernate
    // actually use an arraylist!
    
    Iterator iter = this.series.iterator();
    for(int i=0; i<index && iter.hasNext(); ++i, iter.next()) {
      // This space intentionally left blank
      ;
    }    
    MCSeries series = iter.hasNext() ? (MCSeries)iter.next() : null;
    
    return series;
  }
  
  /**
   * Finds a series within this study by its seriesInstanceUID
   * 
   * @param seriesInstanceUID
   * @return - the series requested or null if not found.
   */
  public MCSeries getSeries(String seriesInstanceUID) {    
    Iterator iter = this.series.iterator();
    for (Iterator iterator = this.series.iterator(); iterator.hasNext();) {
      MCSeries series = (MCSeries) iterator.next();      
      if(series.getSeriesInstanceUID().equals(seriesInstanceUID))
        return series;
    }
    return null;
  }
  
  /**
   * @return Returns the series.
   */
  public Set getSeries() {
    return series;
  }
  /**
   * @param series The series to set.
   */
  public void setSeries(Set series) {
    this.series = series;
  }
  /**
   * @return Returns the studyDate.
   */
  public String getStudyDate() {
    return StudyDate;
  }
  /**
   * @param studyDate The studyDate to set.
   */
  public void setStudyDate(String studyDate) {
    StudyDate = studyDate;
  }
  /**
   * @return Returns the studyDescription.
   */
  public String getStudyDescription() {
    return StudyDescription;
  }
  /**
   * @param studyDescription The studyDescription to set.
   */
  public void setStudyDescription(String studyDescription) {
    StudyDescription = studyDescription;
  }
  /**
   * @return Returns the studyInstanceUID.
   */
  public String getStudyInstanceUID() {
    return StudyInstanceUID;
  }
  /**
   * @param studyInstanceUID The studyInstanceUID to set.
   */
  public void setStudyInstanceUID(String studyInstanceUID) {
    StudyInstanceUID = studyInstanceUID;
  }
  /**
   * @return Returns the studyTime.
   */
  public String getStudyTime() {
    return StudyTime;
  }
  /**
   * @param studyTime The studyTime to set.
   */
  public void setStudyTime(String studyTime) {
    StudyTime = studyTime;
  }
  /**
   * @return Returns the id.
   */
  public Long getId() {
    return id;
  }
  /**
   * @param id The id to set.
   */
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getModality(){
    return(this.modality);
  }
  public void setModality(String modality){
    this.modality= modality;
  }
}
