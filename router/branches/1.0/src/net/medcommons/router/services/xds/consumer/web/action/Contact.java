/*
 * $Id$
 * Created on 1/03/2005
 */
package net.medcommons.router.services.xds.consumer.web.action;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.dicom.util.DICOMUtils;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.output.XMLOutputter;

/**
 * Contact represents contact information for a person or entity,
 * including address, phone numbers, e-mail etc.
 * 
 * <i>Note: the equals() and hashcode() methods are currently
 * overridden on this class so as to give the behaviour that
 * contacts with the same given and family name are considered
 * equal.  This is probably a very hacky and bad assumption.</i>
 * 
 * @author ssadedin
 */
public class Contact  implements Serializable{
  
  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(Contact.class);

  // Name
  private String givenName = "";
  private String middleName = "";
  private String familyName = "";
  private String title;
  
  // Address
  private String line1;
  private String line2;
  private String city;
  private String state;
  private String country;
  private String postalCode;
  private String organization;
  private Date dateOfBirth = new Date();


  // Other info
  private String gender;  // Male or Female
  
  private String medcommonsId;
  
  // Contact info
  
  /**
   * Map of phone number type (String description as read from CCR) to
   * phone number value (physical number).
   */
  private Map phoneNumbers;  
  
  /**
   * Map of email type (String description as read from CCR) to
   * email address value
   */
  private Map emails;
  
  /**
   * Creates a new Contact from the given JDOM Person element.
   * This is designed to allow creation of contacts directly
   * from CCR XML.
   */
  public Contact(CCRElement personElement) {
    super();
    
    Namespace ccrns = Namespace.getNamespace("urn:astm-org:CCR");

    Iterator iter = personElement.getDescendants(new ElementFilter("CurrentName", ccrns));
    if(iter.hasNext()) {
      Element currentName = (Element)iter.next();
      this.givenName = Str.escapeForJavaScript(currentName.getChildTextTrim("Given",ccrns));
      this.middleName = Str.escapeForJavaScript(currentName.getChildTextTrim("Middle",ccrns));
      this.familyName = Str.escapeForJavaScript(currentName.getChildTextTrim("Family", ccrns));
      this.title =Str.escapeForJavaScript(currentName.getChildTextTrim("Title", ccrns));
    }
    else {
      log.warn("Person element has no CurrentName.  Setting name fields to blank.");
      this.givenName = "";
      this.middleName = "";
      this.familyName = "";
      this.title = "";
    }
    
    phoneNumbers = new HashMap();
    Iterator telephoneIter = personElement.getParentElement().getDescendants(new ElementFilter("Telephone", ccrns));
    while (telephoneIter.hasNext()) {
      Element telephone = (Element) telephoneIter.next();
      this.phoneNumbers.put(telephone.getChildTextTrim("Type",ccrns), telephone.getChildTextTrim("Value",ccrns));      
    }
    
    emails = new HashMap();
    Iterator emailIter = personElement.getParentElement().getDescendants(new ElementFilter("EMail",ccrns));
    while (emailIter.hasNext()) {
      Element email = (Element) emailIter.next();
      // Hack - this is a workaround for faulty XML in the NIST XDS repository
      if(email.getChild("Value",ccrns)==null) {
        this.emails.put("Work", email.getText());      
      }
      else {      
        this.emails.put(email.getChildTextTrim("Type",ccrns), email.getChildTextTrim("Value",ccrns));      
      }
    }
    
    Iterator addressIter = personElement.getParentElement().getDescendants(new ElementFilter("Address",ccrns));    
    if(addressIter.hasNext()) {
	    Element address = (Element) addressIter.next();
	    this.line1 = Str.escapeForJavaScript(address.getChildTextTrim("Line1", ccrns));
	    this.line2 = Str.escapeForJavaScript(address.getChildTextTrim("Line2", ccrns));
	    this.city = Str.escapeForJavaScript(address.getChildTextTrim("City", ccrns));
	    this.state = Str.escapeForJavaScript(address.getChildTextTrim("State", ccrns));
	    this.postalCode = Str.escapeForJavaScript(address.getChildTextTrim("PostalCode", ccrns));
	    this.country = Str.escapeForJavaScript(address.getChildTextTrim("Country", ccrns));
    }
    else {
        log.info("No address found for contact "  + this.givenName + " " + this.familyName);
	    this.line1 = "";
	    this.line2 = "";
	    this.city = "";
	    this.state = "";
	    this.postalCode = "";
	    this.country = "";
    }
    
    // Try to figure out their organization - look for an Organization Actor
    // Slight hack here:  we are making assumptions about the structure of the document.
    // Note that in the CCR the Patient element is not represented as an Actor
    // and thus it is skipped by this "if" block
    if("Actor".equals(personElement.getParentElement().getName())) {
        Iterator organizationIter = 
            personElement.getParentElement().getParentElement().getDescendants(
                            new ElementFilter("Organization", ccrns));   
        if(organizationIter.hasNext()) {
            this.organization = Str.escapeForJavaScript(((Element)organizationIter.next()).getChildTextTrim("Name", ccrns));
        }
    }
    else {
        this.organization = "";
    }
    
    Element dobElement = personElement.getChild("DateOfBirth", ccrns);
    if(dobElement!=null) {
    	String dateTime = null;
        dateTime = dobElement.getChildTextTrim("ApproximateDateTime", ccrns);    
        try{
        if((dateTime != null) && (dateTime.length()!=0)) {
            this.dateOfBirth = DICOMUtils.parseDate(dateTime);
            log.error("DOB: Approximate time is " + dateTime);
        }
        else {
        	dateTime = dobElement.getChildText("ExactDateTime", ccrns);
        	log.error("DOB: Exact Date Time is " + dateTime);
        	this.dateOfBirth = DICOMUtils.parseDateZulu(dateTime);
        }
        }
        catch(Exception e){
        	log.error("Error parsing date:" + dateTime);
        	this.dateOfBirth = null ;
        }
    }
    
    Element genderElement = personElement.getChild("Gender", ccrns);
    if (genderElement != null){
    	//log.info("Gender XML is " + dumpXML(genderElement));
    	this.gender = genderElement.getChild("Text", ccrns).getText();
    	
    }
    else{
    	log.error("Gender is null; setting to UNKNOWN");
    	this.gender = "Unknown";
    }
    
   
    // Try and get the medcommons id - if there is one
    
    try {
        this.medcommonsId = personElement.queryTextProperty("personMedCommonsId");
    }
    catch (PHRException e) {
        this.medcommonsId = null;
    }
  }
  
  public boolean equals(Object otherObj) {
      if(otherObj == this)
          return true;
      Contact otherContact = (Contact)otherObj;
      return this.givenName.equals(otherContact.givenName)
          && this.familyName.equals(otherContact.familyName);
  }

  public int hashCode() {
      return this.givenName.hashCode() + this.familyName.hashCode();
  }
  public Map getEmails() {
    return emails;
  }
  
  public Collection getEmailValues() {
      return emails.values();
  }
  
  public String getGivenName() {
    return givenName;
  }
  public void setGivenName(String firstName) {
    this.givenName = firstName;
  }
  public Map getPhoneNumbers() {
    return phoneNumbers;
  }
  public Collection getPhoneNumberValues() {
      return phoneNumbers.values();
  }
  public String getFamilyName() {
    return familyName;
  }
  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }
  public String getMiddleName() {
    return middleName;
  }
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
public String getCity() {
    return city;
}
public void setCity(String city) {
    this.city = city;
}
public String getCountry() {
    return country;
}
public void setCountry(String country) {
    this.country = country;
}
public String getLine1() {
    return line1;
}
public void setLine1(String line1) {
    this.line1 = line1;
}
public String getLine2() {
    return line2;
}
public void setLine2(String line2) {
    this.line2 = line2;
}
public String getPostalCode() {
    return postalCode;
}
public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
}
public String getState() {
    return state;
}
public void setState(String state) {
    this.state = state;
}
public String getOrganization() {
    return organization;
}
public void setOrganization(String organization) {
    this.organization = organization;
}
public String getGender() {
    return gender;
}
public void setGender(String gender) {
    this.gender = gender;
}
public Date getDateOfBirth() {
    return dateOfBirth;
}
public void setDateOfBirth(Date dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
}

public void setDateOfBirthString(String value){
	; // noop
}
public String getDateOfBirthString(){
	if (dateOfBirth!= null)
		return(DICOMUtils.formatDate(dateOfBirth));
	else
		return(null);
}
public String getMedcommonsId() {
    return medcommonsId;
}
public void setMedcommonsId(String medcommonsId) {
    this.medcommonsId = medcommonsId;
}
private String dumpXML(Element element) {
	 try{
	    	StringWriter sw = new StringWriter();
			new XMLOutputter().output(element, sw);
			return(sw.toString());
	    }
	    catch(Exception e){
	    	log.error("error creating xml output");
	    }
	    return(null);
}
}
