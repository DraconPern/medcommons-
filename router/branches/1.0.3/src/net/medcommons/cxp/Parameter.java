package net.medcommons.cxp;
/**
 * Used to represent name/value pairs in both input and 
 * output messages.
 * @author sean
 *
 */
public class Parameter {
	
	private String name = null;
	private String value  = null;

	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return(this.name);
	}
	public void setValue(String value){
		this.value = value;
	}
	
	public String getValue(){
		return(this.value);
	}
}
