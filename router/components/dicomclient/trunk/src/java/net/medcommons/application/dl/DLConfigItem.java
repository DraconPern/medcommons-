package net.medcommons.application.dl;

import java.io.File;
import java.util.Date;

/**
 * Holder class for different types of configuration values shared with
 * web client via interfaces such as JSON.
 *
 * @author mesozoic
 *
 */
public class DLConfigItem { 
	String name;
	Object value;

	public DLConfigItem(String name, String value){
		this.name = name;
		this.value = value;
	}
	public DLConfigItem(String name, boolean value){
		this.name = name;
		this.value = Boolean.toString(value);
	}
	public DLConfigItem(String name, int value){
		this.name = name;
		this.value = Integer.toString(value);
	}
	public DLConfigItem(String name, File value){
		this.name = name;
		this.value = value.getAbsolutePath();
	}
	public DLConfigItem(String name, Date value){
		this.name = name;
		this.value = Long.toString(value.getTime());
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return(this.name);
	}

	public void setValue(String value){
		this.value = value;
	}
	// Default. Maybe remove others?
	public DLConfigItem(String name, Object value){
		this.name = name;
		this.value = value;
	}

	public Object getValue(){
		if (value == null)
			return("");
		else
			return(this.value);
	}

}
