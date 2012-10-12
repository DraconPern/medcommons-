/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer;

import java.io.Serializable;
import java.util.Date;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;



/**
 * @author sean
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCOrder implements Serializable {

  public Long id;

  private String orderGuid = null;

  private String trackingNumber = null;

  private String originatorGuid = null;

  private String recipientGuid = null;

  private String affiliate = null;

  private Date timeCreated = null;

  private String description = null;

  private String patientName = null;

  private String patientId = null;

  private String modality = null;

  private int nimages = 0;

  private int nseries = 0;

  private String patientDob = null;

  private String patientSex = null;

  private String patientAge = null;
  
  private String trackingId = null;

  /**
   *  
   */
  public MCOrder() {
    super();
  }

  /**
   * Creates an order from metadata extracted from the incoming image object and
   * from the contextual infomation in the order manager.
   * 
   * @return
   */

  public MCOrder(OrderManager orm, DicomObject ds) {

    String studyInstanceUID = ds.getString(Tag.StudyInstanceUID);
    String now = Long.toString(System.currentTimeMillis());
    String seed = studyInstanceUID + now;
    String orderGuid = orm.generateOrderGuid(seed.getBytes());

    setOrderGuid(orderGuid);
    setRecipientGuid(recipientGuid);
    setOriginatorGuid(orm.getNodeIdentity());
    setAffiliate(orm.getCurrentAffiliate());
    setDescription(ds.getString(Tag.StudyDescription));
    setTimeCreated(new Date());
    setModality(ds.getString(Tag.Modality));
    setTrackingNumber(orm.generateTrackingNumber());
    setPatientName(ds.getString(Tag.PatientName));
    setPatientId(ds.getString(Tag.PatientID));
    setPatientDob(ds.getString(Tag.PatientBirthDate));
    setPatientSex(ds.getString(Tag.PatientSex));
    setPatientAge(ds.getString(Tag.PatientAge));
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return (this.id);
  }

  public void setOrderGuid(String orderGuid) {
    this.orderGuid = orderGuid;
  }

  public String getOrderGuid() {
    return (this.orderGuid);
  }

  public void setAffiliate(String affiliate) {
    this.affiliate = affiliate;
  }

  public String getAffiliate() {
    return (this.affiliate);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return (this.description);
  }

  public void setOriginatorGuid(String originatorGuid) {
    this.originatorGuid = originatorGuid;
  }

  public String getOriginatorGuid() {
    return (this.originatorGuid);
  }

  public void setRecipientGuid(String recipientGuid) {
    this.recipientGuid = recipientGuid;
  }

  public String getRecipientGuid() {
    return (this.recipientGuid);
  }

  public void setTimeCreated(Date timeCreated) {
    this.timeCreated = timeCreated;
  }

  public Date getTimeCreated() {
    return (this.timeCreated);
  }

  /**
   * Does it make sense to remove this function and move it into the constructor
   * so that tracking numbers can't be modified? Or is this applying constraints
   * at the wrong level (we really care that the database isn't changed, not
   * in-memory copies of objects).
   * 
   * @param trackingNumber
   */
  public void setTrackingNumber(String trackingNumber) {
    this.trackingNumber = trackingNumber;
  }

  public String getTrackingNumber() {
    return (this.trackingNumber);
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public String getPatientName() {
    return (this.patientName);
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getPatientId() {
    return (this.patientId);
  }

  public void setModality(String modality) {
    this.modality = modality;
  }

  public String getModality() {
    return (this.modality);
  }

  public void setNimages(int nimages) {
    this.nimages = nimages;
  }

  public int getNimages() {
    return (this.nimages);
  }

  public void setNseries(int nseries) {
    this.nseries = nseries;
  }

  public int getNseries() {
    return (this.nseries);
  }

  public void setPatientSex(String patientSex) {
    this.patientSex = patientSex;
  }

  public String getPatientSex() {
    return (patientSex);
  }

  public void setPatientDob(String patientDob) {
    this.patientDob = patientDob;
  }

  public String getPatientDob() {
    return (this.patientDob);
  }

  public String getPatientAge() {
    return (this.patientAge);
  }

  public void setPatientAge(String patientAge) {
    this.patientAge = patientAge;
  }

	public String getTrackingId() {
	    return trackingId;
	}
	public void setTrackingId(String trackingId) {
	    this.trackingId = trackingId;
	}
	
  public Object clone() {
    MCOrder newObj = new MCOrder();
    newObj.setAffiliate(getAffiliate());
    newObj.setDescription(getDescription());
    newObj.setOrderGuid(getOrderGuid());
    newObj.setOriginatorGuid(getOriginatorGuid());
    newObj.setPatientId(getPatientId());
    newObj.setPatientName(getPatientName());
    newObj.setRecipientGuid(getRecipientGuid());
    newObj.setTimeCreated(getTimeCreated());
    newObj.setTrackingNumber(getTrackingNumber());
    newObj.setPatientSex(getPatientSex());
    newObj.setPatientDob(getPatientDob());
    newObj.setPatientAge(getPatientAge());
    newObj.setModality(getModality());
    newObj.setNseries(getNseries());
    newObj.setNimages(getNimages());
    return (newObj);

  }
  public String toString(){
    StringBuffer buff = new StringBuffer("MCOrder[");
    buff.append("id =");buff.append(id);buff.append(",");
    buff.append("orderGuid =");buff.append(orderGuid);buff.append(",");
    buff.append("trackingNumber =");buff.append(trackingNumber);buff.append(",");
    buff.append("originatorGuid =");buff.append(originatorGuid);buff.append(",");
    buff.append("recipientGuid =");buff.append(recipientGuid);buff.append(",");
    buff.append("affiliate =");buff.append(affiliate);buff.append(",");
    buff.append("timeCreated =");buff.append(timeCreated);buff.append(",");
    buff.append("description =");buff.append(description);buff.append(",");
    buff.append("patientName =");buff.append(patientName);buff.append(",");
    buff.append("patientId =");buff.append(patientId);buff.append(",");
    buff.append("modality =");buff.append(modality);buff.append(",");
    buff.append("nimages =");buff.append(nimages);buff.append(",");
    buff.append("nseries =");buff.append(nseries);buff.append(",");
    buff.append("patientDob =");buff.append(patientDob);buff.append(",");
    buff.append("patientSex =");buff.append(patientSex);buff.append(",");
    buff.append("patientAge =");buff.append(patientAge);buff.append("]");
    buff.append("trackingId =");buff.append(trackingId);buff.append("]");
    
    return(buff.toString());
  }

}