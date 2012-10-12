package net.medcommons.application.dicomclient.transactions;

import java.util.Date;

/**
 * Instance of download queue object. This queue object is a command that then generates other transfers
 * which may be themselves queued. The purpose of this class of object is to capture the user events and
 * process them in an orderly way.
 * <P>
 * The information stored here is sufficient for performing a download from an appliance. All
 * contextual information should be present - the user may be queuing downloads from multiple
 * appliances.
 * <P>
 * The attachments field can have three values:
 * <ol>
 * <li>NONE - No attachments are downloaded.</li>
 * <li>ALL - All attachments are potentially downloaded. Note that if a CCR contains a PDF document it might not
 * be downloaded if the application is seeking only DICOM </li>
 * <li>A comma-separated list of GUIDs that are a subset of the GUIDs referenced in the CCR. This is used for the case
 * where only some of the referenced documents can be downloaded. The GUIDs <i>must</i> exist within the CCR for
 * security reasons (the application must check). </li>
 * </ol>
 * @author mesozoic
 *
 */
public class DownloadQueue  {
	private Long id;

	public final static String ATTACHMENTS_ALL = "ALL";
	public final static String ATTACHMENTS_NONE = "NONE";
	private String attachments = null;
	private Date creationTime;
	private String state;
	private Long contextStateId;




	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return (this.id);
	}

	public void setAttachments(String attachments){
		this.attachments = attachments;
	}

	public String getAttachments(){
		return(this.attachments);
	}

	public void setCreationTime(Date creationTime){
		this.creationTime = creationTime;
	}
	public Date getCreationTime(){
		return(this.creationTime);
	}

	public void setState(String state){
		this.state = state;
	}
	public String getState(){
		return(this.state);
	}

    
    public void setContextStateId(Long contextStateId){
        this.contextStateId = contextStateId;
    }
    public Long getContextStateId(){
        return(this.contextStateId);
    }

}
