package net.medcommons.application.dicomclient.test;

public class DicomClientParameters {

	String ExportMethod="CSTORE";
	String DICOMRemoteHost="127.0.0.1";
	int DICOMLocalDicomPort=3002;
	String DICOMRemoteAETitle="CAUDIPTERYX";


	String ExportDirectory="/Users/mesozoic/DDL";
	String DICOMLocalAETitle="MCDICOM";
	int DICOMRemotePort=4096;

	/*
	 dicomRemoteHost
	 dicomRemoteAeTitle
	 dicomRemotePort
	 dicomLocalAeTitle
	 dicomLocalPort
	 exportDirectory
	 exportMethod
	 */
	public String toURLArguments(){
		StringBuffer buff = new StringBuffer();
		buff.append("&dicomRemoteHost=");
		buff.append(DICOMRemoteHost);
		buff.append("&dicomRemoteAeTitle=");
		buff.append(DICOMRemoteAETitle);
		buff.append("&dicomRemotePort=");
		buff.append(DICOMRemotePort);
		buff.append("&dicomLocalAeTitle=");
		buff.append(DICOMLocalAETitle);
		buff.append("&dicomLocalPort=");
		buff.append(DICOMLocalDicomPort);
		buff.append("&exportDirectory=");
		buff.append(ExportDirectory);
		buff.append("&exportMethod=");
		buff.append(ExportMethod);


		return(buff.toString());
	}
}
