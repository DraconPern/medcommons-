/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.impl;

import java.sql.Timestamp;

public class Rights {

    private Long id;
    private Long documentId;
    private Long groupNumber;
    private String medcommonsId;
    private Timestamp creationTime;
    private Timestamp expirationTime;
    private Timestamp rightsTime;
    private Timestamp acceptedTime;
    
    public Rights() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    public Timestamp getAcceptedTime() {
        return acceptedTime;
    }

    public void setAcceptedTime(Timestamp acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Timestamp getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Timestamp expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Long getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(Long groupNumber) {
        this.groupNumber = groupNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedcommonsId() {
        return medcommonsId;
    }

    public void setMedcommonsId(String medcommonsId) {
        this.medcommonsId = medcommonsId;
    }

    public Timestamp getRightsTime() {
        return rightsTime;
    }

    public void setRightsTime(Timestamp rightsTime) {
        this.rightsTime = rightsTime;
    }

}
