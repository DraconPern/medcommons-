/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.router.services.transfer.MCOrder;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MCSeries implements Comparator, Serializable {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(MCSeries.class);
	private GuidGenerator guidGenerator = null;

	public final static String STATUS_OPEN = "OPEN";
	public final static String STATUS_CLOSED = "CLOSED";

	public int SeriesNumber;
	public String mcGUID = null;
	public String SeriesInstanceUID;
	public String Modality;
	public String SeriesDescription;
	public String StudyInstanceUID;
	public String SeriesDate;
	public String SeriesTime;
	public String seriesStatus;
	public String storageId = null;
	
	/**
	 * Set to true only when this series has been created by the active session.
	 * This allows us to bypass resolving the document remotely when it has
	 * been saved but no consents are saved because the containing document
	 * has not been saved.
	 */
	private boolean inSession = false;
	
	/**
	 * Tracks the storage id under which this series was loaded.  This is necessary
	 * because if the storage id is changed after loading (eg. by changing the patient
	 * in a CCR) then the attached series need to be restored by reading content
	 * from the old storage id and storing it in the new.   So it is necessary
	 * to remember the original storage id even if a new one is set.
	 */
	private String originalStorageId;

	/**
	 * LastModifiedTime is the time of last DICOM import and saving to disk
	 * in the gateway. It is the time that the series has become fixed content.
	 * It has nothing to do with the DICOM series date/time.
	 */
	public Date lastModifiedTime;

	/**
	 * The id of the study of which this series is a part
	 */
	private Long studyId;

	/**
	 * The instances of images in the series
	 */
	private List<MCInstance> instances;

	/**
	 * Id of this series
	 */
	private Long id;
    
    /**
     * True if this series requires validation by the user.
     */
    private boolean validationRequired = false;
    
    /**
     * Set if payment is required to view the content of 
     * this series.  This is used for quarantining faxes.
     */
    private BillingEvent pendingBillingEvent;
    
    /**
     * Set to true if a payment was outstanding to access
     * this series at the time the document was stored.
     * <p>
     * Note that the payment may no longer be outstanding.
     * It is necessary to query the document service to 
     * get an accurate report of any outstanding charges 
     * on the document(s).
     */
    private boolean paymentRequired = false;
    
    /**
     * Window / Level Presets when this series represents DICOM data
     */
    private List<WindowLevelPreset> presets = new ArrayList<WindowLevelPreset>();
    
	public MCSeries() {
		this.instances = new ArrayList<MCInstance>();
	}
	
  /**
   * Creates a Series that contains a single instance, identified
   * by the given MCInstance.
   */
  public MCSeries(String description, String storageId, String guid, MCInstance instance) {
    this();
    this.SeriesDescription = description;
    this.mcGUID = guid;
    this.originalStorageId = storageId;
    if(instance != null) {
    	this.instances.add(instance);
        //this.instances.put(instance.getSOPInstanceUID(), instance);
    }
  }
	
  /**
	 * Creates a Series not linked to a study
   * 
   * @throws SeriesCreationError
   * @throws NoSuchAlgorithmException
   */
  public MCSeries(MCOrder order, String description) throws SeriesCreationError {
    this();
    this.Modality = "N/A"; 
    
    // Hack, wrong format
    Date date = new Date();
    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
    this.SeriesDate = fmt.format(date);
    this.seriesStatus = STATUS_CLOSED;
    this.studyId = null;
    this.StudyInstanceUID = "";
    this.SeriesDescription = description;
    
		try {
      guidGenerator = new GuidGenerator();
      this.setMcGUID(guidGenerator.generateGuid(String.valueOf(System.currentTimeMillis()).getBytes()));
      
      this.SeriesInstanceUID="";
      for(int i=0; i<12;++i) {
        // HACK HACK!  MUST FIX
        if(i!=0)
          this.SeriesInstanceUID+=".";
        this.SeriesInstanceUID += Math.rint(Math.abs(Math.random()*20));
      }
      
        
    }
    catch (NoSuchAlgorithmException e) {
      throw new SeriesCreationError("Unable to generate guid for new series for order " + order.getId(), e);
    }
    catch (IOException e) {
      throw new SeriesCreationError("Unable to generate guid for new series for order" + order.getId(), e);
    }
    
    this.SeriesNumber = 1;  // always the first after the CCR .... HACK.
    
  }
  

	/**
	* Creates a Series for the given study and Dataset
	* 
	* @param study - the study for which this series is being created.
	 * @param ds - dicom metadata information for the series
	 * @throws NoSuchAlgorithmException
	*/
	public MCSeries(MCStudy study, DicomObject ds)
		throws NoSuchAlgorithmException {
			this();
		guidGenerator = new GuidGenerator();
		String seriesInstanceUID = ds.getString(Tag.SeriesInstanceUID);

		this.Modality = ds.getString(Tag.Modality);
		this.SeriesDescription = ds.getString(Tag.SeriesDescription);
		this.setSeriesInstanceUID(seriesInstanceUID);
		String guid = null;

		try {
			guid = guidGenerator.generateGuid(seriesInstanceUID.getBytes());
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		//guid = DICOMDataManager.encodeBytes(hash, 0, hash.length);
		this.setMcGUID(guid);
		this.StudyInstanceUID = study.getStudyInstanceUID();
		this.SeriesDate = ds.getString(Tag.SeriesDate);
		this.SeriesTime = ds.getString(Tag.SeriesTime);
		this.setStudyId(study.getId());
		
		this.seriesStatus = STATUS_OPEN;

		try {
			this.SeriesNumber =
				Integer.parseInt(ds.getString(Tag.SeriesNumber));
		} catch (NumberFormatException exNum) {
			log.error(
				"Unable to parse series number: "
					+ ds.getString(Tag.SeriesNumber),
				exNum);
			this.SeriesNumber = -1;
		}
	}

	public int compare(Object obj1, Object obj2) {
		MCSeries series1 = (MCSeries) obj1;
		MCSeries series2 = (MCSeries) obj2;
		if (series1.SeriesNumber == series2.SeriesNumber)
			return (0);
		else if (series1.SeriesNumber > series2.SeriesNumber)
			return (1);
		else
			return (-1);
	}

	public int size() {
		return this.instances.size();
	}

	public Date getDate() {
		if ((this.SeriesDate != null) && (this.SeriesDate.trim().length() > 0))
			return DICOMUtils.parseDate(this.SeriesDate);
		else
			return new Date();
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
	 * @return Returns the modality.
	 */
	public String getModality() {
		return Modality;
	}
	/**
	 * @param modality The modality to set.
	 */
	public void setModality(String modality) {
		Modality = modality;
	}
	/**
	 * @return Returns the seriesDate.
	 */
	public String getSeriesDate() {
		return SeriesDate;
	}
	/**
	 * @param seriesDate The seriesDate to set.
	 */
	public void setSeriesDate(String seriesDate) {
		SeriesDate = seriesDate;
	}
	/**
	 * @return Returns the seriesDescription.
	 */
	public String getSeriesDescription() {
		return SeriesDescription;
	}
	/**
	 * @param seriesDescription The seriesDescription to set.
	 */
	public void setSeriesDescription(String seriesDescription) {
		SeriesDescription = seriesDescription;
	}

	/**
	* @return Returns the mcGUID.
	*/
	public String getMcGUID() {
		return mcGUID;
	}
	/**
	 * @param mcGUID The mcGUID to set.
	 */
	public void setMcGUID(String mcGUID) {
		this.mcGUID = mcGUID;
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
	 * @return Returns the seriesNumber.
	 */
	public int getSeriesNumber() {
		return SeriesNumber;
	}
	/**
	 * @param seriesNumber The seriesNumber to set.
	 */
	public void setSeriesNumber(int seriesNumber) {
		SeriesNumber = seriesNumber;
	}
	/**
	 * @return Returns the seriesTime.
	 */
	public String getSeriesTime() {
		return SeriesTime;
	}
	/**
	 * @param seriesTime The seriesTime to set.
	 */
	public void setSeriesTime(String seriesTime) {
		SeriesTime = seriesTime;
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

	public void setInstances(List<MCInstance> instances) {
		this.instances = instances;
	}

	public MCInstance getInstance(int index) {
		return(instances.get(index));
		
	}

	/**
	 * Searchs for the 
	 * 
	 * @param sopInstanceUID
	 * @return
	 */
	public MCInstance getInstance(String sopInstanceUID) {
		MCInstance match = null;
		for (int i=0;i<instances.size();i++){
			MCInstance candidate = instances.get(i);
			if (sopInstanceUID.equals(candidate.getSOPInstanceUID())){
				match = candidate;
				break;
			}
				
		}
		return match;
	}
	
  /**
   * Returns the first instance found in the instance list.
   */
  public MCInstance getFirstInstance() {
    return instances.get(0);
  }

	/**
	 * @return Returns the studyId.
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId The studyId to set.
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * Returns a List the MCInstances belonging to this Series.
	 * 
	 * @return Returns the instances.
	 */
	public List<MCInstance> getInstances() {
		return instances;
	}

	/**
	 * Adds the given instance to this Series
	 * @param instance
	 */
	public void addInstance(MCInstance instance) {
		instance.setStudyInstanceUID(this.getStudyInstanceUID());
		instance.setSeriesId(this.id);
		instance.setSeriesInstanceUID(this.getSeriesInstanceUID());
		this.instances.add(instance);
	}

	public void setSeriesStatus(String seriesStatus) {
		this.seriesStatus = seriesStatus;
	}
	public String getSeriesStatus() {
		return (this.seriesStatus);
	}

	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
    
	public Date getLastModifiedTime() {
		return (this.lastModifiedTime);
	}
	
    /**
     * Returns the mimetype for this series.  The mime type is determined from
     * the first instance in the series.
     */
    public String getMimeType() {
        // Get the first instance
        if(instances.isEmpty()) {
           return "unknown"; 
        }
        MCInstance instance = getFirstInstance();
        return instance.getMimeType();        
    }
    
	public Object clone(){
		MCSeries cloneObj = new MCSeries();
		cloneObj.setStudyId(getStudyId());
		cloneObj.setStudyInstanceUID(getStudyInstanceUID());
		cloneObj.setSeriesTime(getSeriesTime());
		cloneObj.setSeriesDate(getSeriesDate());
		cloneObj.setLastModifiedTime(getLastModifiedTime());
		cloneObj.setSeriesNumber(getSeriesNumber());
		cloneObj.setMcGUID(getMcGUID());
		cloneObj.setModality(getModality());
		cloneObj.setSeriesDescription(getSeriesDescription());
		cloneObj.setSeriesInstanceUID(getSeriesInstanceUID());
		cloneObj.setSeriesStatus(getSeriesStatus());
		return(cloneObj);
		
	}
	/**
	 * Returns a sorted list of instances. Useful for calculating
	 * the SHA-1 hash of the set of instances.
	 * @return
	 */
	public MCInstance[] sortInstances(){
		Collections.sort(instances, new Comparator<MCInstance>() {
		    public int compare(MCInstance o1, MCInstance o2) {
		        int order1 = o1.getInstanceNumber();
		        int order2 = o2.getInstanceNumber();
		        if (order1==order2) return(0);
		        else if (order1 > order2) return(1);
		        else return(-1);
		    }});
		MCInstance sorted[] = instances.toArray(new MCInstance[instances.size()]);
		
		return(sorted);
		 	
		
	}

    public boolean isValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {
        this.validationRequired = validationRequired;
    }
    
    public void setStorageId(String storageId){
        
        if(this.originalStorageId == null)
            this.originalStorageId = storageId;
        
    	this.storageId = storageId;
        for (MCInstance instance : this.instances) {
            instance.setStorageId(storageId);
        }
    }
    
    public String getStorageId(){
    	if (this.storageId != null)
    		return(this.storageId);
    	else 
    		return(this.originalStorageId);
    	
    }

    public String getOriginalStorageId() {
        return originalStorageId;
    }

    public List<WindowLevelPreset> getPresets() {
        return presets;
    }

    public void resetOriginalStorageId() {
        this.originalStorageId = storageId;
    }

    public BillingEvent getPendingBillingEvent() {
        return pendingBillingEvent;
    }

    public void setPendingBillingEvent(BillingEvent pendingBillingEvent) {
        this.pendingBillingEvent = pendingBillingEvent;
    }

    /**
     * Returns true if a payment was outstanding on this series
     * when it was stored.
     * 
     * @see #paymentRequired
     */
    public boolean getPaymentRequired() {
        return paymentRequired;
    }

    public void setPaymentRequired(boolean paymentRequired) {
        this.paymentRequired = paymentRequired;
    }

    public static Logger getLog() {
        return log;
    }

    public GuidGenerator getGuidGenerator() {
        return guidGenerator;
    }

    public static String getSTATUS_OPEN() {
        return STATUS_OPEN;
    }

    public static String getSTATUS_CLOSED() {
        return STATUS_CLOSED;
    }

    public boolean isInSession() {
        return inSession;
    }

    public void setInSession(boolean inSession) {
        this.inSession = inSession;
    }
}
