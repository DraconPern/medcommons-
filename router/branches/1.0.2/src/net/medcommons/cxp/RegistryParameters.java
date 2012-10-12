package net.medcommons.cxp;

import java.io.Serializable;
/**
 * Used as a registry parameter block used by CXP.
 * @author sean
 *
 */
public class RegistryParameters implements Serializable{

	/**
	 * The name of the registry. This needs to be defined - is 
	 * this only a display name? Or the name of the entity
	 * that is going 'to get sued'?
	 */
	private String registryName = null;
	
	/**
	 * The id of the registry. No scheme has yet been invented
	 * for looking up registry by id - but since it may be the
	 * case that the registryName would be "partners.org" but 
	 * that there are many different registries that they have 
	 * for clinical trials/telemedicine/&amp;etc - an id field
	 * will discriminate between the different registries.
	 */
	private String registryId = null;
	
	/**
	 * Parameters (name/value pairs) for this message.
	 */
	private Parameter[] parameters = null;
	
	public void setRegistryName(String registryName){
		this.registryName = registryName;
	}
	public String getRegistryName(){
		return(this.registryName);
	}
	public void setRegistryId(String registryId){
		this.registryId = registryId;
	}
	public String getRegistryId(){
		return(this.registryId);
	}
	public void setParameters(Parameter[] parameters){
		this.parameters = parameters;
	}
	public Parameter[] getParameters(){
		return(this.parameters);
	}
	
}
