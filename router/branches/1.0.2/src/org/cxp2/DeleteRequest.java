
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
 *         &lt;element name="storageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "storageId",
    "docinfo",
    "registryParameters"
})
@XmlRootElement(name = "DeleteRequest")
public class DeleteRequest {

    @XmlElement(required = true)
    protected String storageId;
    
    @XmlElement(required = true)
    protected List<Document> docinfo;
    
    @XmlElement(required = true)
    protected List<RegistryParameters> registryParameters;

    /**
     * Gets the value of the storageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStorageId() {
        return storageId;
    }

    /**
     * Sets the value of the storageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStorageId(String value) {
        this.storageId = value;
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

}
