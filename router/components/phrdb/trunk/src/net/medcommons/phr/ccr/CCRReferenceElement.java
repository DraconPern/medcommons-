/*
 * $Id: CCRReferenceElement.java 3553 2009-11-12 05:55:35Z ssadedin $
 * Created on 21/11/2008
 */
package net.medcommons.phr.ccr;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;

import org.jdom.Namespace;

public class CCRReferenceElement extends CCRElement {
    public CCRReferenceElement() {
        super("Reference");
    }
    public CCRReferenceElement(String arg0, Namespace arg1) {
        super(arg0, arg1);
    }
    public CCRReferenceElement(String arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2);
    }
    public CCRReferenceElement(String arg0, String arg1) {
        super(arg0, arg1);
    }
    public CCRReferenceElement(String arg0) {
        super(arg0);
    }
   
    /**
     * Attempts to find the MedCommons GUID from the given reference. If
     * the reference is not in the correct format or does not contain a
     * MedCommons GUID then null is returned.
     * 
     * @param ref
     * @return - the guid from the reference or null
     * @throws PHRException 
     */
    public String getGuid() throws PHRException {
        String url = this.queryTextProperty("referenceURL");
        if (Str.blank(url)) {// No URL - not valid, ignore this entry
            return null;
        }
        if (!url.startsWith("mcid://")) {// bad format url
            return null;
        }
        return url.substring(7);
    }
    
    /**
     * Add a named attribute to the given reference.
     * <p>
     * This is really a hacky way of cramming our own non standard stuff into the CCR
     * so that we can read them back later.
     * 
     * @param reference -   reference to add to
     * @param name      -   name of attribute
     * @param value     -   value of attribute
     * @param desc      -   description, single word descriptive tag
     */
    public void addAttribute(String name, String value, String desc, String version) {
        CCRElement description = this.getChild("Locations").getChild("Location").getChild("Description");
        CCRElement attribute = el("ObjectAttribute");
        attribute.addContent(el("Attribute").setText(name));
        attribute.addContent(el("AttributeValue").addContent(el("Value").setText(value)));
        CCRElement code = el("Code");        
        attribute.addContent(code);
        code.addContent(el("Value").setText(desc));
        code.addContent(el("CodingSystem").setText("MedCommons"));
        code.addContent(el("Version").setText(version)); 
        description.addContent(attribute);
    }
    
    
    
    public String getDisplayName() throws PHRException {
        return this.queryTextProperty("referenceDisplayName");
    }
    
    public String getType() {
        return this.getChild("Type").getChildTextTrim("Text");
    }
    
    public long getSize() throws PHRException {
        String sizeValue = this.queryTextProperty("referenceSize");
        return sizeValue != null ? Long.parseLong(sizeValue) : -1;
    }
    
    /**
     * Adds an object attribute corresponding to the specified preset.
     * If a preset already exists then the passed one will be added
     * to the existing set.
     * 
     *  <ObjectAttribute>
              <Attribute>PRESETS</Attribute>
              <AttributeValue>
                <Value>
                  <ObjectAttribute>
                    <Attribute>BONE</Attribute>
                    <AttributeValue>
                      <Value>1500,300</Value>
                    </AttributeValue>
                  </ObjectAttribute>
                  <ObjectAttribute>
                    <Attribute>ABDOMEN</Attribute>
                    <AttributeValue>
                      <Value>40,350</Value>
                    </AttributeValue>
                  </ObjectAttribute>
                </Value>
              </AttributeValue>
            </ObjectAttribute>
     * @throws PHRException 
     */
    public void addDICOMPreset(int windowCenter, int windowWidth, String windowCenterWidthExplanation) throws PHRException {
        
        CCRElement objAtt = el("ObjectAttribute");
        objAtt.createPath("Attribute", windowCenterWidthExplanation);
        objAtt.createPath("AttributeValue/Value", windowWidth + "," + windowCenter);
        
        CCRElement presets = this.queryProperty("referencePresets");
        if(presets == null) {
            presets = el("ObjectAttribute");
            presets.createPath("Attribute", "PRESETS");
            getPath("Locations/Location/Description").addContent(presets);
        }
        presets.getPath("AttributeValue/Value").addContent(objAtt);
    }
}
