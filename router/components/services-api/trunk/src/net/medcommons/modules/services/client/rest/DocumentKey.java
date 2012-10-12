package net.medcommons.modules.services.client.rest;
/**
 * Holds guid/key pairs; returned by web service.
 * @author sean
 *
 */
public class DocumentKey implements net.medcommons.modules.services.interfaces.DocumentKey{
	private String guid = null;
	private String key = null;

	public DocumentKey(){
		super();
	}
	public DocumentKey(String guid, String key){
		this.guid = guid;
		this.key =key;
	}

	public void setGuid(String guid){
		this.guid = guid;
	}
	public String getGuid(){
		return(this.guid);
	}
	public void setKey(String key){
		this.key = key;
	}
	public String getKey(){
		return(this.key);
	}
}
