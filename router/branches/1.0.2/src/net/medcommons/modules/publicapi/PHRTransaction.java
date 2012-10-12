package net.medcommons.modules.publicapi;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.PHRProfile;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * This class contains all of the information needed to 
 * handle an incoming transaction to a PHR such as an upload of 
 * a new CCR. 
 * @author sdoyle
 *
 */
public class PHRTransaction implements Serializable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(PHRTransaction.class);
    
	/**
	 * Time that the transaction was created.
	 */
	private Timestamp timeCreated = new Timestamp(System.currentTimeMillis());
	
	/**
	 * Time of last activity of last activity.
	 */
	private Timestamp timeLastActivity = null;
	
	
	
	/**
	 * status code
	 */
	private int status;
	
	
	public PHRTransaction() {
	}
	
	/**
	 * Create a new PHRTransaction for the given DocumentDescriptor
	 * @param docDescriptor
	 */
	public PHRTransaction(DocumentDescriptor docDescriptor) {
	    this.setOriginalGuid(docDescriptor.getGuid());
	    this.setStorageId(docDescriptor.getStorageId());
	    this.setContentType(docDescriptor.getContentType());
	    this.setTimeLastActivity(this.getTimeCreated());
	}
	
	/**
	 * Description. If the status is an error - then this might be the error
	 * message. 
	 */
	private String description;
	private String token;
	private String storageId;
	private String originalReference;
	private String originalAuthToken;
	private String originalGuid;
	private String originalURI;
	private String transactionType;
	private String senderId;
	private String contentType;
	private String phrPersonName;
	private String phrAlias;
	private byte[] phrImage;
	private String healthURL;
	private String calculatedGuid;
	private List<PHRProfile> profiles;
	private TransactionState transactionState = TransactionState.UNINITALIZED;
	private String[] groupIds;
	
	public void setTimeCreated(Timestamp timeCreated){
		this.timeCreated = timeCreated;
	}
	public Timestamp getTimeCreated(){
		return(this.timeCreated);
	}
	public void setTimeLastActivity(Timestamp timeLastActivity){
		this.timeLastActivity = timeLastActivity;
	}
	public Timestamp getTimeLastActivity(){
		return(this.timeLastActivity);
	}
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return(this.status);
	}
	public void setDescription(String description){
		this.description = description;
	}
	public String getDescription(){
		return(this.description);
	}
	public void setToken(String token){
		this.token = token;
	}
	public String getToken(){
		return(this.token);
	}
	public void setStorageId(String storageId){
		this.storageId = storageId;
	}
	public String getStorageId(){
		return(this.storageId);
	}
	public void setOriginalReference(String originalReference){
		this.originalReference = originalReference;
	}
	public String getOriginalReference(){
		return(this.originalReference);
	}
	public void setOriginalAuthToken(String originalAuthToken){
		this.originalAuthToken = originalAuthToken;
	}
	public String getOriginalAuthToken(){
		return(this.originalAuthToken);
	}
	public void setOriginalGuid(String originalGuid){
		this.originalGuid = originalGuid;
	}
	public String getOriginalGuid(){
		return(this.originalGuid);
	}
	public void setOriginalURI(String originalURI){
		this.originalURI = originalURI;
	}
	public String getOriginalURI(){
		return(this.originalURI);
	}
	public void setTransactionType(String transactionType){
		this.transactionType = transactionType;
	}
	public String getTransactionType (){
		return(this.transactionType);
	}
	
	public void setSenderId(String senderId){
		this.senderId = senderId;
	}
	
	public String getSenderId(){
		return(this.senderId);
	}
	
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	public String getContentType(){
		return(this.contentType);
	}
	
	/*
	 * public String phrPersonName;
	public String phrAlias;
	public byte[] phrImage;
	 */
	public void setPhrPersonName(String phrPersonName){
		this.phrPersonName = phrPersonName;
	}
	public String getPhrPersonName(){
		return(this.phrPersonName);
	}
	public void setPhrAlias(String phrAlias){
		this.phrAlias = phrAlias;
	}
	public String getPhrAlias(){
		return(this.phrAlias);
	}
	public void setPhrImage(byte[] phrImage){
		this.phrImage = phrImage;
	}
	public byte[] getPhrImage(){
		return(this.phrImage);
	}
	
	public void setProfiles(List<PHRProfile> profileNames){
		this.profiles = profileNames;
	}
	public List<PHRProfile> getProfiles(){
		return(this.profiles);
	}
	public void setTransactionState(TransactionState transactionState){
	    log.debug(this.getTransactionState().name() + " => " + transactionState.name());
		this.transactionState = transactionState;
	}
	public TransactionState getTransactionState(){
		return(this.transactionState);
	}
	
	public void setGroupIds(String[] groupIds){
		this.groupIds = groupIds;
	}
	public String[] getGroupIds(){
		return(this.groupIds);
	}
	
	public void setHealthURL(String healthURL){
		this.healthURL = healthURL;
	}
	public String getHealthURL(){
		return(this.healthURL);
	}
	public void setCalculatedGuid(String calculatedGuid){
	    this.calculatedGuid = calculatedGuid;
	}
	public String getCalculatedGuid(){
	    return(this.calculatedGuid);
	}
	public String toString() {        
        return ToStringBuilder.reflectionToString(this);
    }
	
}
