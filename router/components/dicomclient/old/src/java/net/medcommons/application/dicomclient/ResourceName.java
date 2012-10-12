package net.medcommons.application.dicomclient;

/**
 * Defines resources to be read out of resource file and their documentation.
 * @author mesozoic
 *
 */
public enum ResourceName {



	CLEAR_CACHE("clearCaches", "If 'true' then cache cleared after each transaction, 'false' never clears cache.", ""),
	DICOM_REMOTE_AE_TITLE("DICOMRemoteAETitle", "The AE Title of the DICOM device images are sent to from this device", ""),
	DICOM_REMOTE_HOST("DICOMRemoteHost","The hostname or IP address of the DICOM device that images are sent to from this device",""),
	DICOM_REMOTE_PORT("DICOMRemotePort", "The port of the DICOM device that images are sent to from this device", ""),

	DICOM_LOCAL_AE_TITLE("DICOMLocalAETitle","The DICOM AE title of this device","MCDICOM"),
	DICOM_LOCAL_PORT("DICOMLocalDicomPort", "The DICOM port of this device","3002"),
	LOCAL_HTTP_PORT("LocalHttpPort", "Port number of the HTTP server embedded in this device","16092"),
	GATEWAY_ROOT("GatewayRoot","CXP endpoint of the Gateway","http://localhost:9080"),
	SENDER_ACCOUNTID("SenderAccountId", "Account of sender - used for creating group entries", "1175376381039160"),
	CONFIGVERSION("ConfigVersion", "Build identifier used for versioning", "UNKNOWN"),
	PROXY_PORT("ProxyPort", "Proxy for CXP transactions", "16093"),
	EXPORT_METHOD("ExportMethod", "Either 'CSTORE' or 'FILE'", "CSTORE"),
	EXPORT_DIRECTORY("ExportDirectory", "Location of export directory if 'ExportMethod' is 'FILE'", null),
	DDL_IDENTITY("DDLIdentity", "Identity of this local DDL", "UNKNOWN"),
	DICOM_TIMEOUT("DICOMTimeout", "Number of seconds for DICOM SCP series timeout", "15"),
	AUTOMATIC_UPLOAD_TO_Voucher("AutomaticUploadToVoucher", "Automatically upload incoming DICOM to voucher accounts", "false");


	private final String name;
	private final String documentation;
	private final String defaultValue;

	ResourceName(String name, String documentation, String defaultValue){
		this.name = name;
		this.documentation = documentation;
		this.defaultValue = defaultValue;

	}

	public String getName(){return(this.name);}
	public String getDocumentation(){return(this.documentation);}
	public String getDefault(){return(this.defaultValue);}

	public static ResourceName getResourceName(String name){
		ResourceName rType = null;
		for (ResourceName candidate: ResourceName.values()){
			if (candidate.getName().equals(name)){
				rType = candidate;
				break;
			}
		}
		return(rType);
	}

}