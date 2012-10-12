/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Feb 11, 2005
 *
 * 
 */
package net.medcommons.router.services.xds.consumer.web.action;

import java.io.StringWriter;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.output.XMLOutputter;

/**
 * Container for XDS document metadata. 
 * 
 * @author sean
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XDSDocument {
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger
			.getLogger(XDSDocument.class);

	String sourcePatientInfo = null;
	String sourcePatientId = null;
	String mimeType = null;
	String id = null;
	String url = null;
	
	String name = null;
	String serviceStartTime = null;
	String serviceStopTime = null;
	String creationTime = null;
	String authorDepartment = null;
	String authorInstitution = null;
	String hash = null;
	String size = null;
	String languageCode= null;
	
	public  XDSDocument(){
		;
	}
	public XDSDocument(Element e){
		/*try{
			StringWriter sw = new StringWriter();
			new XMLOutputter().output(e, sw);
			log.info("XDSDocument is " + sw.toString());
		}
		catch(Exception e1){;}
		*/
		id = e.getAttributeValue("id");
		mimeType =e.getAttributeValue("mimeType");
		// Very klunky here. Should use XPATH for neater code.
		Iterator names = e.getDescendants(new ElementFilter("Name"));
		
		Element nameNode = (Element) names.next();
		Iterator lString = nameNode.getDescendants(new ElementFilter("LocalizedString"));
		Element lValue = (Element) lString.next();
		name = lValue.getAttributeValue("value");
		//log.info("document name is " + name);
		
		Iterator slots = e.getDescendants(new ElementFilter("Slot"));
		while(slots.hasNext()){
			Element slot = (Element) slots.next();
			String name = slot.getAttributeValue("name");
			//log.info("slot name = " + name);
			if (name.equals("authorDepartment"))
				authorDepartment = getFirstSlotValue(slot);
			else if (name.equals("authorInstitution"))
				authorInstitution = getFirstSlotValue(slot);
			else if (name.equals("sourcePatientId"))
				sourcePatientId = getFirstSlotValue(slot);
			else if (name.equals("sourcePatientInfo"))		
				sourcePatientInfo = getFirstSlotValue(slot);
			else if (name.equals("serviceStartTime"))
				serviceStartTime = getFirstSlotValue(slot);
			else if (name.equals("serviceStopTime"))
				serviceStopTime = getFirstSlotValue(slot);
			else if (name.equals("hash"))
				hash = getFirstSlotValue(slot);
			else if (name.equals("size"))
				size = getFirstSlotValue(slot);
			else if (name.equals("languageCode"))
				languageCode = getFirstSlotValue(slot);
			else
				; // ignore for now.
			
			
			
		}
		//log.info("authorDepartment:"+authorDepartment);
		
	}
	private String getFirstSlotValue(Element e){
		Iterator iter = e.getDescendants(new ElementFilter("Value"));
		Element value = (Element) iter.next();
		return(value.getText());
	}
	public void setSourcePatientInfo(String sourcePatientInfo){
		this.sourcePatientInfo = sourcePatientInfo;
	}
	public String getSourcePatientInfo(){
		return(this.sourcePatientInfo);
	}
	public void setSourcePatientId(String sourcePatientId){
		this.sourcePatientId = sourcePatientId;
	}
	public String getSourcePatientId(){
		return(this.sourcePatientId);
	}
	public void setMimeType(String mimeType){
		this.mimeType = mimeType;
	}
	public String getMimeType(){
		return(this.mimeType);
	}
	public void setId(String id){
		this.id = id;
	}
	public String getId(){
		return(this.id);
	}
	public void setUrl(String url){
		this.url = url;
	}
	public String getUrl(){
		return(this.url);
	}
	public String toString(){
		return("XDSDocument: " + id + ", mimeType=" + mimeType + ", url=" + url);
	}
	public void setCreationTime(String creationTime){
		this.creationTime = creationTime;
	}
	public String getCreationTime(){
		return(this.creationTime);
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return(this.name);
	}
}
