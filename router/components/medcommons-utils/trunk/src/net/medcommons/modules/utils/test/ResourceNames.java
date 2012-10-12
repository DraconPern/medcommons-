package net.medcommons.modules.utils.test;

/**
 * These are resources used solely for JUnit testing. The names here
 * match entries in property files that point to the real values.
 * This is merely a simple dictionary of the elements in the property files.
 *
 * @author mesozoic
 *
 */
public interface ResourceNames {
	public static String DocumentDirectory = "Document.directory";
	public static String ScratchDirectory = "Document.scratch.directory";
	public static String DocumentJpeg = "Document.jpeg";
	public static String DocumentMissing = "Document.missing";
	public static String DocumentCCR = "Document.ccr";
	public static String DocumentCCRWithPDFAndJPEGAnd2DICOM = "Document.ccr.WithPDFAndJPEGAnd2DICOM";
	public static String DocumentCCRWithDICOM = "Document.ccr.WithDICOM";
	public static String DocumentCCRWithPDFAndJPEG = "Document.ccr.WithPDFAndJPEG";
	public static String DocumentPDF="Document.pdf";
	public static String DocumentSingleDICOMFile = "Document.SingleDICOMFile";
	public static String DocumentSingleDICOMFile_MR = "Document.SingleDICOMFile_MR";
	public static String DocumentSingleDICOMFile_RGB = "Document.SingleDICOMFile_RGB";
	public static String DICOMSeries1 = "DICOM.Series1";
	public static String DICOMSeries2 = "DICOM.Series2";

	public static String AccountID1="AccountID_1";
	public static String AccountID2="AccountID_2";

	public static String JUNIT_CXPEndpoint="JUNIT_CXPEndpoint";
	public static String SenderAccountId="SenderAccountId";

	/**
	 * Useful for testing where the account number is missing.
	 */
	public static String AccountID_Never_Existed = "AccountID_Never_Existed";

	public static String DicomRepositoryMetadata = "DicomRepositoryMetadata";

	public static String suffixGUID = ".GUID";

	public static String TEST_RESOURCE_DIR = "tests/modules/";
	public static String TEST_RESOURCE_STANDLONE_PARENT = "etc";


	public static String TEMPLATE_DIRNAME = "Template.directory";

	/**
	   * The path to the XML configuration file for the Configuration class
	   */
	public static String CONFIGURATION_CONFIGPATH ="etc/configurations/config.xml";

	public static String MEDCOMMONS_BOOT_PARAMETERS_PATH = "etc/configurations/MedCommonsBootParameters.properties";

    public static final String DEFAULT_SPRING_CONFIG_PATH = "etc/configurations/medcommons-test-config.xml";
}
