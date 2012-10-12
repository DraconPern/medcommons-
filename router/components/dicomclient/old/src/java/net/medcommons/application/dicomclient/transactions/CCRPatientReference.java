package net.medcommons.application.dicomclient.transactions;

import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;

/**
 * Sort of a hack class - a union of the CCRReference and PixDemographicData class.
 * This permits the 'join' of data on the server so that it can be sent back via JSON.
 * Otherwise -JavaScript would have to get the references and then the demographic data seperately,
 * or perhaps both types of objects could be returned to be assembled in JavaScript.
 *
 * @author mesozoic
 *
 */
public class CCRPatientReference extends CCRReference{
	String givenName = "Unknown";
	String familyName= "Unknown";
	String groupName= "Unknown";

	public CCRPatientReference(CCRReference ccrReference, PixDemographicData pixDemographicData, PixIdentifierData pixIdentifierData){
		this.setFileLocation(ccrReference.getFileLocation());
		this.setGuid(ccrReference.getGuid());
		this.setId(ccrReference.getId());
		this.setStorageId(ccrReference.getStorageId());
		this.setTimeEntered(ccrReference.getTimeEntered());
		if (pixDemographicData != null){
			Long contextStateId = pixIdentifierData.getContextStateId();
			ContextState contextState = TransactionUtils.getContextState(contextStateId);
    		this.familyName = pixDemographicData.getFamilyName();
    		this.givenName = pixDemographicData.getGivenName();
    		if (contextState != null){
    			this.groupName = contextState.getGroupName();
    		}
    		else{
    			this.groupName = "No group yet defined for upload";
    		}
		}

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
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}
	public String getGroupName(){
		return(this.groupName);
	}
}