
package org.cxp2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegistryParameters complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegistryParameters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameters" type="{http://cxp2.org}Parameter" maxOccurs="unbounded"/>
 *         &lt;element name="registryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="registryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryParameters", propOrder = {
    "parameters",
    "registryId",
    "registryName"
})
public class RegistryParameters {

    @XmlElement(required = true)
    protected List<Parameter> parameters;
    @XmlElement(required = true)
    protected String registryId;
    @XmlElement(required = true)
    protected String registryName;

    /**
     * Gets the value of the parameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Parameter }
     * 
     * 
     */
    public List<Parameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }
        return this.parameters;
    }

    public void setParameters(List<Parameter> parameters){
    	this.parameters = parameters;
    }
    /**
     * Gets the value of the registryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistryId() {
        return registryId;
    }

    /**
     * Sets the value of the registryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistryId(String value) {
        this.registryId = value;
    }

    /**
     * Gets the value of the registryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistryName() {
        return registryName;
    }

    /**
     * Sets the value of the registryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistryName(String value) {
        this.registryName = value;
    }

}
