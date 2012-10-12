/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;
import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import net.medcommons.router.services.repository.DocumentNotFoundException;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * Represents an individual, descrete document.  This translates to the 
 * image level for DICOM or to a document (such as PDF) etc.
 * 
 * @author sean
 *
 * Need to add min/max pixel value.
 */
public class MCInstance implements Comparator,Serializable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(MCInstance.class);
    
	public int InstanceNumber;
	public String SOPInstanceUID;
	public String ReferencedFileID;
	public String SeriesInstanceUID;
	public String StudyInstanceUID;
	public String level = ""; // Note: this might be a multivalued field.
	public String window = "";
	public String mimeType = "";
	public int numFrames = Integer.MIN_VALUE;
      
  /**
   * The primary key of this object, if it exists in the database
   */
  private Long id;

  /**
   * The id of the series to which this instance is attached in the database, if it
   * exists in the database.
   */
  private Long seriesId;
  
  /**
   * A chached object representing the document where this instance is a reference to a 
   * document object.
   */
  private Object documentObj = null;
  
  /**
   * The size of the original (unencrypted) content in bytes.
   */
  private long contentSize = -1;
  
  /**
	 * Storage id that this CCR is to be stored under
	 */
	private String storageId = null;
  
  public MCInstance() {
  }
	
  public MCInstance(String referenceFileId, String mimeType, String storageId, String guid) {
    this.ReferencedFileID = referenceFileId;
    this.mimeType = mimeType;
    this.SOPInstanceUID = guid;
    this.storageId = storageId;
  }
  
	/**
	 * Creates an MCInstance representing a DICOM image corresponding
     * to the meta information in the given Dataset.
     * 
     * @param fsu
	 * @param series
     * @param ds
     */
  public MCInstance(File rootDir, MCSeries series, DicomObject ds) {
    String sopInstanceUID = ds.getString(Tag.SOPInstanceUID);
    this.SOPInstanceUID = sopInstanceUID;
    
    this.setSeries(series);

    // Replace \ characters with /. This is
    // a no-op in Unix; in Windows the \ characters
    // cause havoc down the HTML/JavaScript path. Placing
    // these filenames in Unix conventions removes all 
    // need for escape sequences.
    String instanceFilename =
		DICOMUtils.toFile(rootDir, series.getMcGUID(), ds).toString().replace('\\', '/');
    int pos = instanceFilename.length();
    for (int i = 0; i < 3; i++) {
      pos = instanceFilename.lastIndexOf("/", pos - 1);
    }

    this.ReferencedFileID = DICOMUtils.imageFileSegment(ds);
    
    this.window = ds.getString(Tag.WindowWidth);
    this.level = ds.getString(Tag.WindowCenter);
    String sFrame = ds.getString(Tag.NumberOfFrames);
    
    this.mimeType = "application/dicom";

    try {
      this.InstanceNumber =
        Integer.parseInt(ds.getString(Tag.InstanceNumber));
      if ((sFrame != null) && (!"".equals(sFrame)))
        this.numFrames = Integer.parseInt(sFrame);
    } catch (Exception e) {
      this.InstanceNumber = -1;
      e.printStackTrace();
    }    
  }
  
  /**
   * Returns an object representing this instance resolved via
   * the repository.  This only works for instances that are  documents
   * and only currently for CCRs.
   * <p> - ?? Is this obsolete? Querying documents by SOPInstanceUID doesn't make sense.
   * <p> |= SOPInstanceUID is where the guid for CCR documents is stored when a CCR is modeled as a series. Its a hack.
   * 
   * @throws RepositoryException 
   */
  
  public Object getDocument() throws RepositoryException {
	
      if(this.documentObj != null) 
          return this.documentObj;
      
      if(CCRDocument.CCR_MIME_TYPE.equals(this.mimeType)) {
          try {
              return (this.documentObj = RepositoryFactory.getLocalRepository().queryDocument(storageId,this.SOPInstanceUID));
          }
          catch(DocumentNotFoundException exNotFound) {
              log.warn("Unable to resolve reference to missing document " + this.ReferencedFileID);
          }
      }
      return null;
  }
  

  public long getContentSize() throws RepositoryException {
      if(contentSize == -1) {
          if(!"URL".equals(this.mimeType))
              contentSize = RepositoryFactory.getLocalRepository().getContentLength(storageId, this.SOPInstanceUID);
      }
      return contentSize;
  }

  public void setSeries(MCSeries series) {
    this.SeriesInstanceUID = series.getSeriesInstanceUID();
    this.StudyInstanceUID = series.getStudyInstanceUID();
    this.seriesId = series.getId();
  }

	public int compare(Object obj1, Object obj2) {
		MCInstance instance1 = (MCInstance) obj1;
		MCInstance instance2 = (MCInstance) obj2;
		if (instance1.InstanceNumber == instance2.InstanceNumber)
			return (0);
		else if (instance1.InstanceNumber > instance2.InstanceNumber)
			return (1);
		else
			return (-1);
	}
	public boolean equals(Object obj) {
		if (obj.getClass() == MCInstance.class)
			return (((MCInstance) obj).SOPInstanceUID.equals(SOPInstanceUID));
		else
			return(false);
	}
	public String toString() {
		StringBuffer buff = new StringBuffer("Instance[");
		buff.append("\n\t InstanceNumber: ");
		buff.append(InstanceNumber);
		buff.append(",\n\t SOPInstanceUID: ");
		buff.append(SOPInstanceUID);
		buff.append(",\n\t ReferencedFileID: ");
		buff.append(ReferencedFileID);
		buff.append(",\n\t SeriesInstanceUID: ");
		buff.append(SeriesInstanceUID);
		buff.append(",\n\t StudyInstanceUID: ");
		buff.append(StudyInstanceUID);
		buff.append(",\n\t window: ");
		buff.append(window);
		buff.append(",\n\t level: ");
		buff.append(level);
		buff.append("  ]");

		return (buff.toString());
	}

  /**
   * @return Returns the id.
   */
  public Long getId() {
    return id;
  }
  /**
   * @param id The id to set.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return Returns the instanceNumber.
   */
  public int getInstanceNumber() {
    return InstanceNumber;
  }
  /**
   * @param instanceNumber The instanceNumber to set.
   */
  public void setInstanceNumber(int instanceNumber) {
    InstanceNumber = instanceNumber;
  }
  /**
   * @return Returns the level.
   */
  public String getLevel() {
    return level;
  }
  /**
   * @param level The level to set.
   */
  public void setLevel(String level) {
    this.level = level;
  }
  /**
   * @return Returns the nFrames.
   */
  public int getNumFrames() {
    return numFrames;
  }
  /**
   * @param frames The nFrames to set.
   */
  public void setNumFrames(int frames) {
    numFrames = frames;
  }
  /**
   * @return Returns the referencedFileID.
   */
  public String getReferencedFileID() {
    return ReferencedFileID;
  }
  /**
   * @param referencedFileID The referencedFileID to set.
   */
  public void setReferencedFileID(String referencedFileID) {
    ReferencedFileID = referencedFileID;
  }
  /**
   * @return Returns the seriesInstanceUID.
   */
  public String getSeriesInstanceUID() {
    return SeriesInstanceUID;
  }
  /**
   * @param seriesInstanceUID The seriesInstanceUID to set.
   */
  public void setSeriesInstanceUID(String seriesInstanceUID) {
    SeriesInstanceUID = seriesInstanceUID;
  }
  /**
   * @return Returns the sOPInstanceUID.
   */
  public String getSOPInstanceUID() {
    return SOPInstanceUID;
  }
  /**
   * @param instanceUID The sOPInstanceUID to set.
   */
  public void setSOPInstanceUID(String instanceUID) {
    SOPInstanceUID = instanceUID;
  }
  /**
   * @return Returns the studyInstanceUID.
   */
  public String getStudyInstanceUID() {
    return StudyInstanceUID;
  }
  /**
   * @param studyInstanceUID The studyInstanceUID to set.
   */
  public void setStudyInstanceUID(String studyInstanceUID) {
    StudyInstanceUID = studyInstanceUID;
  }
  /**
   * @return Returns the window.
   */
  public String getWindow() {
    return window;
  }
  /**
   * @param window The window to set.
   */
  public void setWindow(String window) {
    this.window = window;
  }
  /**
   * @return Returns the seriesId.
   */
  public Long getSeriesId() {
    return seriesId;
  }
  /**
   * @param seriesId The seriesId to set.
   */
  public void setSeriesId(Long seriesId) {
    this.seriesId = seriesId;
  }
  
  public Object clone(){
		  MCInstance cloneObj = new MCInstance();
		  cloneObj.setSeriesId(getSeriesId());
		  cloneObj.setLevel(getLevel());
		  cloneObj.setWindow(getWindow());
		  cloneObj.setSeriesInstanceUID(getSeriesInstanceUID());
		  cloneObj.setSOPInstanceUID(getSOPInstanceUID());
		  cloneObj.setNumFrames(getNumFrames());
		  cloneObj.setInstanceNumber(getInstanceNumber());
		  cloneObj.setStudyInstanceUID(getStudyInstanceUID());
		  cloneObj.setReferencedFileID(getReferencedFileID());
		  return(cloneObj);
	}
	  
  public String getMimeType() {
    return mimeType;
  }
  
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setDocumentObj(Object documentObj) {
      this.documentObj = documentObj;
  }
  
  public void setStorageId(String storageId){
	  this.storageId = storageId;
  }
  public String getStorageId(){
	  return(this.storageId);
  }
}
