package net.medcommons.modules.xml;

import java.util.Date;

import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XMLPHRDocument;

import org.jdom.Document;
/**
 * Document metadata needed by the RLS and other central servers.
 * <P>
 * Currently this is really only implemented by the CCR - but the nature
 * of the information is such that really any medical document should
 * be able to provide this same metadata (HL7, DICOM, &amp;etc.).
 * <P>
 * Squirrelly detail:
 * Which is the true document - the JDOM one or the unparsed one?
 * In the case of the viewer - it's the first one. In the case of 
 * CXP -it's the latter. 
 * 
 * @author mesozoic
 *
 */
public interface RegistryDocument extends DocumentTypes {
	
	/**
	 * A simple string - used in error and logging messages.
	 * @return
	 */
	public String getDocumentType();
	public void setDocumentType(String documentType);
	public String getXml();
	public XMLPHRDocument getJDOMDocument() throws PHRException;
	public String getPatientGivenName() throws PHRException;
	public String getPatientFamilyName() throws PHRException;
	public String getPatientMedCommonsId() throws PHRException;
	public String getPatientGender() throws PHRException;
	public String getPatientAge() throws PHRException;
	public String getPatientEmail() throws PHRException;
	public String getToEmail() throws PHRException;
		
    public Date getPatientDateOfBirth() throws PHRException;
    
    public String getDocumentPurpose() throws PHRException;
    public String getDocumentPurposeText() throws PHRException;
    
    public void setStorageId(String storageId);
    public String getStorageId();
    
    public void setGuid(String guid);
    public String getGuid();
    public void setTrackingNumber(String trackingNumber);
    public String getTrackingNumber();
   
    /**
     * In the case of the CCR - this is CCRDocumentObjectID
     * @param documentObjectId
     */
    public void setDocumentObjectId(String documentObjectId) throws PHRException;
    public void setCreateTimeMs(long creationTime) throws PHRException;
    public void addPatientId(String patientId, String idType) throws PHRException;
    
    public void syncFromJDom() throws PHRException;
}

