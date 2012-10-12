
package org.cxp2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cxpVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="registryParameters" type="{http://cxp2.org}RegistryParameters" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cxpVersion",
    "reason",
    "status",
    "registryParameters"
})
@XmlRootElement(name = "PutResponse")
public class PutResponse {

    @XmlElement(required = true)
    protected String cxpVersion;
    @XmlElement(required = true)
    protected String reason;
    protected int status;
    @XmlElement(required = true)
    protected List<RegistryParameters> registryParameters;
    @XmlElement(required = true)
    protected List<Document> docinfo;

    /**
     * Gets the value of the cxpVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCxpVersion() {
        return cxpVersion;
    }

    /**
     * Sets the value of the cxpVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCxpVersion(String value) {
        this.cxpVersion = value;
    }

    /**
     * Gets the value of the reason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
    }

    /**
     * Gets the value of the status property.
     * 
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     */
    public void setStatus(int value) {
        this.status = value;
    }

    /**
     * Gets the value of the registryParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the registryParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRegistryParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RegistryParameters }
     * 
     * 
     */
    public List<RegistryParameters> getRegistryParameters() {
        if (registryParameters == null) {
            registryParameters = new ArrayList<RegistryParameters>();
        }
        return this.registryParameters;
    }
    
  
    /**
    * Sets the value of the  property.
    * 
    * @param value
    *     allowed object is
    *     {@link RegistryParameters }
    *     
    */
   public void setRegistryParameters(List<RegistryParameters> registryParameters){
   	this.registryParameters = registryParameters;
   }
   
   /**
    * Gets the value of the docinfo property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the docinfo property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getDocinfo().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Document }
    * 
    * 
    */
   public List<Document> getDocinfo() {
       if (docinfo == null) {
           docinfo = new ArrayList<Document>();
       }
       return this.docinfo;
   }
   /**
    * Sets the value of the  property.
    * 
    * @param value
    *     allowed object is
    *     {@link Document }
    *     
    */
   public void setDocinfo(List<Document> docinfo){
   	this.docinfo = docinfo;
   }

}
