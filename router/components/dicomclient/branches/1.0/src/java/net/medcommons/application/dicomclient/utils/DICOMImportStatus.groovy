package net.medcommons.application.dicomclient.utils

import java.lang.Mathimport org.apache.commons.lang.builder.ToStringBuilder
import java.lang.Cloneableimport org.json.JSONObject/**
 * @author ssadedin
 */
public class DICOMImportStatus implements Cloneable {
    
    String id = String.valueOf(System.currentTimeMillis() + Math.random()*10000)
    
    String status = "New"
    
    String path
    
    String transferKey
    
    Date createDateTime
    
    /**
     * Description of error, if any
     */
    String message

    /**
     * Type (or name) of document that images are being added to.
     * Default (if null / blank) is CURRENTCCR
     */
    String documentType
    
    DICOMImportStatus clone() {
        return super.clone();
    }
    
    String toString() {
        return ToStringBuilder.reflectionToString(this)
    }
    
    JSONObject toJSON() {
        new JSONObject().put("id",id)
                        .put("status", status)
                        .put("path", path)
                        .put("createDateTime", createDateTime)
                        .put("transferKey",transferKey)
                        .put("message", message);
    }
}
