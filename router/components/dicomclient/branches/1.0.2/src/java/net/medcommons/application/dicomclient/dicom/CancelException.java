package net.medcommons.application.dicomclient.dicom;

import java.io.IOException;

public class CancelException extends IOException{
	public CancelException(){
		super();
	}
	public CancelException(String reason){
		super(reason);
	}
	public CancelException(String reason, Throwable t){
		super(reason, t);
	}
}
