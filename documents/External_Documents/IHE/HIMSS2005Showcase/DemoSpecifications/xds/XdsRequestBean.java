/*
 * xdsRequestBean.java
 *
 * Created on October 4, 2004, 3:12 PM
 */

package gov.nist.registry.xds;

import java.util.Collection;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;

import java.io.PrintStream;

import java.util.Iterator;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

/**
 *
 * @author  andrew
 */
public class XdsRequestBean {
    
    private String ipAddress = null;
    private Node metadata = null;
    private Collection documents = null;
    private String uri = null;
    private String[] contentIds = null;
    private String[] mimeTypes = null;
    private Object[] contents = null;
    
    
    /** Creates a new instance of xdsRequestBean */
    public XdsRequestBean() {
    }
    
    /**
     * Getter for property ipAddress.
     * @return Value of property ipAddress.
     */
    public java.lang.String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * Setter for property ipAddress.
     * @param ipAddress New value of property ipAddress.
     */
    public void setIpAddress(java.lang.String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    
    /**
     * Getter for property documents.
     * @return Value of property documents.
     */
    public java.util.Collection getDocuments() {
        return documents;
    }
    
    /**
     * Setter for property documents.
     * @param documents New value of property documents.
     */
    public void setDocuments(java.util.Collection documents) {
        this.documents = documents;
    }
    
    /**
     * Getter for property uri.
     * @return Value of property uri.
     */
    public java.lang.String getUri() {
        return uri;
    }
    
    /**
     * Setter for property uri.
     * @param uri New value of property uri.
     */
    public void setUri(java.lang.String uri) {
        this.uri = uri;
    }
    
    /**
     * Setter for property metadata.
     * @param metadata New value of property metadata.
     */
    public void setMetadata(org.w3c.dom.Node metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Getter for property metadata.
     * @return Value of property metadata.
     */
    public org.w3c.dom.Node getMetadata() {
        return metadata;
    }
    
    public int getNumberAttachments() {
        if (this.getDocuments() == null)
            return 0;
        return this.getDocuments().size();
    }
    
    public void populateContentIds(){
        contentIds = new String[this.getNumberAttachments()];
        
        Iterator it = documents.iterator();
        int count = 0;
        while(it.hasNext()) {
            AttachmentPart ap = (AttachmentPart) it.next();
            contentIds[count] = ap.getContentId();
            count++;
        }
        
    }
    
    /**
     * Getter for property contentIds.
     * @return Value of property contentIds.
     */
    public java.lang.String[] getContentIds() {
        if(contentIds == null)
            this.populateContentIds();
        return this.contentIds;
    }
    
    /**
     * Setter for property contentIds.
     * @param contentIds New value of property contentIds.
     */
    public void setContentIds(java.lang.String[] contentIds) {
        this.contentIds = contentIds;
    }
    
    public void populateMimeTypes(){
        mimeTypes = new String[this.getNumberAttachments()];
        
        Iterator it = documents.iterator();
        int count = 0;
        while(it.hasNext()) {
            AttachmentPart ap = (AttachmentPart) it.next();
            mimeTypes[count] = ap.getContentType();
            count++;
        }
        
    }
    
    
    /**
     * Getter for property mimeTypes.
     * @return Value of property mimeTypes.
     */
    public java.lang.String[] getMimeTypes() {
        if(mimeTypes == null)
            this.populateMimeTypes();
        return this.mimeTypes;
    }
    
    /**
     * Setter for property mimeTypes.
     * @param mimeTypes New value of property mimeTypes.
     */
    public void setMimeTypes(java.lang.String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }
    
    public void populateContents() throws SOAPException {
        contents = new Object[this.getNumberAttachments()];
        
        Iterator it = documents.iterator();
        int count = 0;
        while(it.hasNext()) {
            AttachmentPart ap = (AttachmentPart) it.next();
            contents[count] = ap.getContent();
            count++;
        }
        
    }
    
    /**
     * Getter for property contents.
     * @return Value of property contents.
     */
    public java.lang.Object[] getContents() throws SOAPException {
        if(contents == null)
            this.populateContents();
        return this.contents;
    }
    
    /**
     * Setter for property contents.
     * @param contents New value of property contents.
     */
    public void setContents(java.lang.Object[] contents) {
        this.contents = contents;
    }
    
    /**
     * Getter for property metadataAsString.
     * @return Value of property metadataAsString.
     */
    public String getMetadataAsString() {
        String out = metadataToString(metadata);
        return out;
    }
 String metadataToString(Node node) {
     StringBuffer b = new StringBuffer();
    int type = node.getNodeType();
    switch (type) {
      case Node.ELEMENT_NODE:
        b.append("<" + node.getNodeName());
        NamedNodeMap attrs = node.getAttributes();
        int len = attrs.getLength();
        for (int i=0; i<len; i++) {
            Attr attr = (Attr)attrs.item(i);
            b.append(" " + attr.getNodeName() + "=\"" +
                      escapeXML(attr.getNodeValue()) + "\"");
        }
        b.append('>');
        NodeList children = node.getChildNodes();
        len = children.getLength();
        for (int i=0; i<len; i++)
          b.append(metadataToString(children.item(i)));
        b.append("</" + node.getNodeName() + ">");
        break;
      case Node.ENTITY_REFERENCE_NODE:
        b.append("&" + node.getNodeName() + ";");
        break;
      case Node.CDATA_SECTION_NODE:
        b.append("<![CDATA[" + node.getNodeValue() + "]]>");
        break;
      case Node.TEXT_NODE:
        b.append(escapeXML(node.getNodeValue()));
        break;
      case Node.PROCESSING_INSTRUCTION_NODE:
        b.append("<?" + node.getNodeName());
        String data = node.getNodeValue();
        if (data!=null && data.length()>0)
           b.append(" " + data);
        b.append("?>");
        break;
    }
    return new String(b);
  }

  static String escapeXML(String s) {
      return s;
      /*
    StringBuffer str = new StringBuffer();
    int len = (s != null) ? s.length() : 0;
    for (int i=0; i<len; i++) {
       char ch = s.charAt(i);
       switch (ch) {
       case '<': str.append("&lt;"); break;
       case '>': str.append("&gt;"); break;
       case '&': str.append("&amp;"); break;
       case '"': str.append("&quot;"); break;
       case '\'': str.append("&apos;"); break;
       default: str.append(ch);
     }
    }
    return str.toString();*/
  }
}
    