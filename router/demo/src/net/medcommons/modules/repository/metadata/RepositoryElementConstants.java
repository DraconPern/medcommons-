package net.medcommons.modules.repository.metadata;

/**
 * This interface defines the elements that are defined in the 
 * XML templates for the Repository. 
 * 
 * @author mesozoic
 *
 */
public interface RepositoryElementConstants {
	
	
	
	public final static String DocumentName ="DocumentName";
	public final static String WindowWidth = "WindowWidth";
	public final static String WindowCenter = "WindowCenter";
	public final static String ContentType = "ContentType";
	public final static String SopInstanceUID = "SopInstanceUID";
	public final static String Frames = "Frames";
	public final static String DisplayOrder = "DisplayOrder";
	public final static String InstanceNumber = "InstanceNumber";
	public final static String[] DICOMFileElements = {
		DocumentName,
		WindowWidth,
		WindowCenter,
		ContentType,
		SopInstanceUID,
		Frames,
		DisplayOrder,
		InstanceNumber
	};
	public final static String StudyInstanceUID = "StudyInstanceUID";
	public final static String StudyDate = "StudyDate";
	public final static String StudyDescription = "StudyDescription";
	public final static String SeriesDate = "SeriesDate";
	public final static String SeriesDescription = "SeriesDescription";
	public final static String SeriesInstanceUID = "SeriesInstanceUID";
	public final static String SeriesNumber = "SeriesNumber";
	public final static String Modality = "Modality";
	
	public final static String[] DICOMMetadataElements = {
		StudyInstanceUID,
		StudyDate,
		StudyDescription,
		SeriesDate,
		SeriesDescription,
		SeriesInstanceUID,
		SeriesNumber,
		Modality
	};
	
	public final static String Source = "Source";
	public final static String Revision = "Revision";
	public final static String METADATA = "Metadata";
	public final static String FILES = "Files";
	public final static String CreationDate = "CreationDate";
	
	public final static String[] CompoundDocumentElements = {
		Source,
		CreationDate,
		Revision,
		METADATA,
		FILES
	};
	
}
