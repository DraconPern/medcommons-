/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCStudy implements Serializable {
	final static String GUID = "guid";
	final static String STUDYINSTANCEUID = "StudyInstanceUID";
	final static String PATIENTNAME = "PatientName";
	final static String PATIENTID = "PatientID";
	final static String STUDYDATE = "StudyDate";
	final static String STUDYTIME = "StudyTime";
	final static String STUDYDESCRIPTION = "StudyDescription";

	public String mcGUID = null;
	public String StudyInstanceUID = null;
	public String PatientName = null;
	public String PatientID = null;
	public String StudyDate = null;
	public String StudyTime = null;
	public String StudyDescription = null;
	public ArrayList series = null;
	
	public MCStudy(){
		;
	}
	public MCStudy(Properties p){
		this();
		try {

		mcGUID = p.getProperty(GUID, null);
		StudyInstanceUID =  p.getProperty(STUDYINSTANCEUID, null);
		PatientName =  p.getProperty(PATIENTNAME, null);
		PatientID = p.getProperty(PATIENTID, null);
		StudyDate = p.getProperty(STUDYDATE, null);
		StudyTime = p.getProperty(STUDYTIME, null);
		StudyDescription = p.getProperty(STUDYDESCRIPTION, null);
		// Needs to recursively load the series too.
		series = new ArrayList();
		}
		catch(RuntimeException e){
			e.printStackTrace();
			throw e;
		}
		
	}

	
	/*
	 * Saves object to properties file.
	 */
	public void save(Properties p){
	
		p.put(GUID, mcGUID);
		p.put(STUDYINSTANCEUID, StudyInstanceUID);
		p.put(PATIENTNAME, PatientName);
	 	p.put(PATIENTID, PatientID);
		p.put(STUDYDATE, StudyDate);
		p.put(STUDYTIME, StudyTime);
		p.put(STUDYDESCRIPTION, StudyDescription);
		// Needs to recursively save the series too.
		
	}
	public String toString(){
		StringBuffer buff = new StringBuffer("Study[");
		buff.append(mcGUID);
		buff.append(",");
		buff.append(PatientName);
		buff.append(",");
		buff.append(StudyDate);
		buff.append(",");
		buff.append(StudyDescription);
		buff.append("]");
		return(buff.toString());
	}
  /**
   * @return Returns the mcGUID.
   */
  public String getMcGUID() {
    return mcGUID;
  }
  /**
   * @param mcGUID The mcGUID to set.
   */
  public void setMcGUID(String mcGUID) {
    this.mcGUID = mcGUID;
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
  public MCSeries getSeries(int i) {
      return (MCSeries)this.series.get(i);
  }
  
  /**
   * @return Returns the series.
   */
  public ArrayList getSeries() {
    return series;
  }
  /**
   * @param series The series to set.
   */
  public void setSeries(ArrayList series) {
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
}
