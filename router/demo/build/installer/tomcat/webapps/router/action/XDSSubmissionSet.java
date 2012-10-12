/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Feb 11, 2005
 *
 * 
 */
package net.medcommons.router.services.xds.consumer.web.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XDSSubmissionSet {
	String id = null;
	XDSDocument ccr = null; 
	Date date = null;
	ArrayList documents = new ArrayList();
	public void setId(String id){
		this.id =id;
	}
	public String getId(){
		return(this.id);
	}
	public void setCcr(XDSDocument ccr){
		this.ccr = ccr;
	}
	public XDSDocument getCcr(){
		return(this.ccr);
	}
	public void addDocument(XDSDocument doc){
		documents.add(doc);
	}
	public ArrayList getDocuments(){
		return(documents);
	}
	public void setDate(Date date){
		this.date = date;
	}
	public Date getDate(){
		return(this.date);
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer("XDSSubmissionSet: ");
		buff.append(id);
		buff.append("\n CCR in submission set is " + ccr);
		Iterator iter = documents.iterator();
		
		while (iter.hasNext()){
			buff.append("\n Contains: " + iter.next());
			
		}
		return(buff.toString());
	}
}
