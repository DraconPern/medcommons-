/*
 * $Id: DocumentMetadata.java 3054 2008-11-07 13:33:49Z ssadedin $
 * Created on 07/11/2008
 */
package net.medcommons.modules.services.interfaces;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DocumentMetadata {
    
    String key;
    
    String value;
    
    public DocumentMetadata() {
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DocumentMetadata == false) {
            return false;
          }
          if (this == obj) {
            return true;
          }
          DocumentMetadata rhs = (DocumentMetadata) obj;
          return new EqualsBuilder().append(getKey(), rhs.getKey()).append(getValue(), rhs.getValue()).isEquals();        
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 67).append(getKey()).append(getValue()).toHashCode();        
    }
    
    /**
     * @param key
     * @param value
     */
    public DocumentMetadata(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
