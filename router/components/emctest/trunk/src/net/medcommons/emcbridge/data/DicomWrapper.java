package net.medcommons.emcbridge.data;

import java.util.Map;

import net.medcommons.modules.services.interfaces.DicomMetadata;


import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.emc.solution.common.data.ObjectData;
import com.emc.solution.osa.client.dao.MasterClientData;
import com.emc.solution.osa.constants.OsaConstants;

/**
 * Wrapper around DICOM files - contains both DICOM and Documentum metadata.
 * 
 * The Documentum data is retrieved from Documentum; the DICOM data is derived
 * from the DICOM bytestream.
 * 
 * @author mesozoic
 * 
 */
public class DicomWrapper {
	DicomMetadata dicomMetadata;
	IDfSysObject documentumData;
	String externalDocumentId;

	public DicomWrapper(DicomMetadata dicomMetadata, IDfSysObject documentumData, String externalDocumentId) {
		this.dicomMetadata = dicomMetadata;
		this.documentumData = documentumData;
		this.externalDocumentId = externalDocumentId;
	}

	public IDfSysObject getDocumentumMetadata() {
		return (this.documentumData);
	}

	
	public DicomMetadata getDicomMetadata() {
		return (this.dicomMetadata);
	}
	
	public String getExternalDocumentId(){
	    return(this.externalDocumentId);
	}
 
	/**
	 * Paves over DICOM metadata with values from Documentum. The current CCR
	 * generator functions work with the DICOM metadata - this will be made more
	 * general later. We need to extract more data from the order and patient
	 * object.
	 * 
	 * TODO: Are other objectMaps needed here?
	 * @param wrapper
	 * @param patientObjectMap
	 * @return
	 */
	public static DicomWrapper useDocumentumMetadata(DicomWrapper wrapper,
			Map patientObjectMap) {
		DicomMetadata dicom = wrapper.dicomMetadata;

		dicom.setPatientAddress((String) patientObjectMap
				.get(OsaConstants.OSA_PATIENT_ADDRESS));
		dicom.setPatientTelephoneNumber((String) patientObjectMap
				.get(OsaConstants.OSA_PATIENT_PHONE));
		dicom.setPatientId((String) patientObjectMap.get(OsaConstants.OSA_PATIENT_ID));
		dicom.setPatientName((String) patientObjectMap.get(OsaConstants.OSA_PATIENT_NAME));
		
		// Kludge.  Lower level routines should not created
		// actors if the data doesn't exist.
		if (null==wrapper.dicomMetadata.getCalledAeTitle()){
			wrapper.dicomMetadata.setCalledAeTitle("Unknown");
		}
		if (null==wrapper.dicomMetadata.getCallingAeTitle()){
			wrapper.dicomMetadata.setCallingAeTitle("Unknown");
		}

		return (wrapper);
	}
	public static DicomWrapper useDocumentumMetadata(DicomWrapper wrapper, MasterClientData masterClientData, IDfSysObject imageData) throws DfException{
		ObjectData patientData = masterClientData.getPatientData();
		Map<String, String>physicianMap = masterClientData.getPhysicianMap();
		//String faxOrderId = masterClientData.getFaxOrderId();
		
		
		
		DicomMetadata dicom = wrapper.dicomMetadata;
		Map<String, String> patientObjectMap = patientData.getStringAttributeMap();
	
		dicom.setPatientAddress((String) patientObjectMap
				.get(OsaConstants.OSA_PATIENT_ADDRESS));
		dicom.setPatientTelephoneNumber((String) patientObjectMap
				.get(OsaConstants.OSA_PATIENT_PHONE));
		
		//OsaConstants.OSA_PATIENT_SSN
		dicom.setPatientId((String) patientObjectMap.get(OsaConstants.OSA_PATIENT_ID));
		dicom.setPatientName((String) patientObjectMap.get(OsaConstants.OSA_PATIENT_NAME));
		dicom.setPatientDateOfBirth(patientObjectMap.get(OsaConstants.OSA_PATIENT_DOB));
	
		dicom.setReferringPhysicianName(physicianMap.get(OsaConstants.OSA_REF_PHYSICIAN_NAME));
		dicom.setReferringPhysicianAddress(physicianMap.get(OsaConstants.OSA_REF_PHYSICIAN_ADDRS));
		dicom.setReferringPhysicianTelephoneNumber(physicianMap.get(OsaConstants.OSA_REF_PHYSICIAN_PHONE));
		dicom.setReferringPhysicianEmail(physicianMap.get(OsaConstants.OSA_REF_PHYSICIAN_EMAIL));
		dicom.setInstitutionName(imageData.getString(OsaConstants.OSA_INSTITUTION_NAME));
	
		//OsaConstants.OSA_INSTITUTION_NAME
		
		/*
		 * *  OsaConstants.OSA_REF_PHYSICIAN_NAME
	 *	OsaConstants.OSA_REF_PHYSICIAN_ADDRS
	 *	OsaConstants.OSA_REF_PHYSICIAN_FAX
	 *	OsaConstants.OSA_REF_PHYSICIAN_PHONE
	 *	OsaConstants.OSA_REF_PHYSICIAN_EMAIL
		 */
		// Kludge.  Lower level routines should not created
		// actors if the data doesn't exist.
		if (null==wrapper.dicomMetadata.getCalledAeTitle()){
			wrapper.dicomMetadata.setCalledAeTitle("Unknown");
		}
		if (null==wrapper.dicomMetadata.getCallingAeTitle()){
			wrapper.dicomMetadata.setCallingAeTitle("Unknown");
		}

		return (wrapper);
	}

}
