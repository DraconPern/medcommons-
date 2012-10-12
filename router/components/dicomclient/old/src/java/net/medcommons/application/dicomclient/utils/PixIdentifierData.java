package net.medcommons.application.dicomclient.utils;

import java.util.Date;

import net.medcommons.application.dicomclient.transactions.ContextState;

/**
 * Generalized identifer database.
 *
 * Each patient (or user) may have multiple identities. Each has at least one identifier (the MedCommons ID).
 * Additional identifiers can be from information in the DICOM identifiers or from the downlaoded CCR.
 * <P>
 * For example - a CCR might be downloaded from a MedCommons appliance and it contains three &Lt;IDs&gt; in the patient &lt;Actor&gt;
 * object:
 * <ol>
 *   <li> MedCommons Id of 1234567890123456. An instance of PixIdentifierData is created with affinityDomain "MedCommons"
 *   and affinityIdentifier of "1234567890123456". </li>
 *   <li> A SSN of 111-222-3333.  </li>
 *   <li> An ID of ABC123 from hospital XYZ</li>
 * </ol>
 * These three objects are created after the PixDemographicData object is created; the database-generated id from this
 * object is placed into the pixDemographicDataId field of this object.
 * <P>
 * Subsequently - if a DICOM study is pushed to the DDL which is matched to the PixDemographicData object - a new PixIdentiferData
 * object may be created from the DICOM object's PatientID field.
 * 
 * Context data is present for a particular MedCommonsId.
 * Perhaps it would be useful for other third party systems (like Documentum).
 *
 * @author mesozoic
 *
 */
public class PixIdentifierData {

	private Long id; // Used by hibernate
	private String affinityDomain = "";
	private String affinityIdentifier = "";
	private Date creationDate;
	private Long pixDemographicDataId;
	private Long contextStateId;

	public void setId(Long id){
		this.id = id;
	}
	public Long getId(){
		return(this.id);
	}
	public void setAffinityDomain(String affinityDomain){
		this.affinityDomain = affinityDomain.toUpperCase();
	}
	public String getAffinityDomain(){
		return(this.affinityDomain);
	}
	public void setAffinityIdentifier(String affinityIdentifier){
		this.affinityIdentifier = affinityIdentifier.toUpperCase();
	}
	public String getAffinityIdentifier(){
		return(this.affinityIdentifier);
	}
	public void setCreationDate(Date creationDate){
		this.creationDate = creationDate;
	}
	public Date getCreationDate(){
		return(this.creationDate);
	}
	public void setPixDemographicDataId(Long pixDemographicDataId){
		this.pixDemographicDataId = pixDemographicDataId;
	}
	public Long getPixDemographicDataId(){
		return(this.pixDemographicDataId);
	}
	
	public void setContextStateId(Long contextStateId){
		this.contextStateId = contextStateId;
	}
	public Long getContextStateId(){
		return(this.contextStateId);
	}
}
