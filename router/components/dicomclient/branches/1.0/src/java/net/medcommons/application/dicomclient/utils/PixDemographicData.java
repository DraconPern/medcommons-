package net.medcommons.application.dicomclient.utils;

import net.sourceforge.pbeans.annotations.PersistentClass;

@PersistentClass(table="pix_demographic_data", idField="id", autoIncrement=true)
public class PixDemographicData {
	private Long id; // Used by hibernate
	private String givenName;
	private String familyName;
	private String middleName;
	private String dob;
	private String gender;


	public void setId(Long id){
		this.id = id;
	}
	public Long getId(){
		return(this.id);
	}
	public void setGivenName(String givenName){
		this.givenName = givenName;
	}
	public String getGivenName(){
		return(this.givenName);
	}
	public void setFamilyName(String familyName){
		this.familyName = familyName;
	}
	public String getFamilyName(){
		return(this.familyName);
	}
	public void setMiddleName(String middleName){
		this.middleName = middleName;
	}
	public String getMiddleName(){
		return(this.middleName);
	}
	public void setDob(String dob){
		this.dob = dob;
	}
	public String getDob(){
		return(this.dob);
	}

	public void setGender(String gender){
		this.gender = gender;
	}
	public String getGender(){
		return(this.gender);
	}
	
}
