package net.medcommons.client.utils.dicom;

import java.util.HashMap;

import net.medcommons.modules.services.interfaces.DicomMetadata;

import org.apache.log4j.Logger;


public class DICOMSeries {
	String seriesInstanceUID = null;
	String seriesDescription = null;
	int seriesNumber;
	HashMap<String, DicomMetadata> instances = new HashMap<String, DicomMetadata>();
	
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DICOMSeries.class.getName());

	
	public DICOMSeries(DicomMetadata dicomMetadata){
		seriesInstanceUID = dicomMetadata.getSeriesInstanceUid();
		seriesDescription = dicomMetadata.getSeriesDescription();
		seriesNumber = dicomMetadata.getSeriesNumber();
		instances.put(dicomMetadata.getSopInstanceUid(), dicomMetadata);
	}
	
	public void addInstance(DicomMetadata dicomMetadata){
		String instanceSeriesUID = dicomMetadata.getSeriesInstanceUid();
		if (seriesInstanceUID.equals(instanceSeriesUID)){
			String sopInstanceUID = dicomMetadata.getSopInstanceUid();
			DicomMetadata d = instances.get(sopInstanceUID);
			if (d== null){
				instances.put(sopInstanceUID, dicomMetadata);
			}
			else{
				log.info("Attempt to add duplicate SOPInstanceUID " +
						sopInstanceUID);
			}
		}
		else{
			throw new RuntimeException("Attempt to add DICOM object with mismatched SeriesInstanceUID. Expecting\n"
					+ seriesInstanceUID +" instead of \n" + instanceSeriesUID);
		}
	}

}
