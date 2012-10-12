package net.medcommons.application.dicomclient.utils;

import org.cxp2.Document;

/**
 * Document extended to contain size information.
 * @author mesozoic
 *
 */
public class CxpDocument extends Document{
    long size;

    public long getSize(){
    	return(this.size);
    }
    public void setSize(long size){
    	this.size = size;
    }
}
