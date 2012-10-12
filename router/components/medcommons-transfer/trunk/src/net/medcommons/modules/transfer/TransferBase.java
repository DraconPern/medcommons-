package net.medcommons.modules.transfer;

import java.util.List;

import net.medcommons.modules.cxp.client.CXPClient;

import org.apache.log4j.Logger;
import org.cxp2.Document;
import org.cxp2.Parameter;
import org.cxp2.RegistryParameters;

public class TransferBase {
	private static Logger log = Logger.getLogger(TransferBase.class);
	long totalBytes = -1;
	long startTime = -1;
	CXPClient client = null;

	public void setCXPClient(CXPClient client){
		this.client = client;
	}
	
	/**
	 * Causes the CXP call to be terminated on the next read() or write().
	 * A CancelledStreamException is thrown in the thread performing the read.
	 *
	 */
	public void cancelStream(){
		if (this.client != null){
			this.client.cancelStream();
		}
	}
	public void displayRegistryParameters(List<RegistryParameters> registryParameters){
		if (registryParameters == null){
			log.info("Null RegistryParameters");
		}
		for (int i=0;i<registryParameters.size(); i++){
			RegistryParameters r = (RegistryParameters) registryParameters.get(i);
			log.info("Registry Parameters:" + r.getRegistryId() + "," + r.getRegistryName());
			List<Parameter> params = r.getParameters();
			for (int k=0;k<params.size();k++){
				Parameter p = params.get(k);
				log.info("  Parameter name=" + p.getName() + ", value=" + p.getValue());
			}
			
		}
	}
	
	public String documentToString(Document doc) {
		StringBuffer buff = new StringBuffer("Document[");
		String guid = doc.getGuid();
		String name = doc.getDocumentName();
		buff.append(guid);
		if (name != null) {
			buff.append(",");
			buff.append(name);
		}
		buff.append("]");
		return (buff.toString());
	}
	/**
	 * Returns the number of bytes transferred. Default is
	 * number of bytes sent; this method is overridden in 
	 * DownloadFileAgent to be the number of bytes read.
	 * @return
	 */
	public long getBytesTransferred(){
		if (client == null)
			return(0);
		else
			return(client.getOutputBytes());
	}
	
	public long getInputBytes(){
		if (client == null)
			return(0);
		else
			return(client.getInputBytes());
	}
	public long getOutputBytes(){
		if (client == null)
			return(0);
		else
			return(client.getOutputBytes());
	}
	
	public long getEstimatedByteCount() {
		if (client == null)
			return(0);
		else
			return(client.getByteCount());
	}
	

	
	/**
	 * Returns the number of objects transfered. For simple documents - this 
	 * is the number of documents. For complex documents - it's the number of 
	 * documents within the complex document. So - if there was one CCR, one
	 * PDF, and one DICOM series with 100 images in it - the total number of
	 * objects transferred is 102.
	 * @return
	 */
	public int getObjectsTransferred(){
		if (client==null)
			return(0);
		else
			return(client.getObjectCount());
	}
	
	public boolean statusOK(int status) {
		return(CXPClient.statusOK(status));
	}

	public boolean statusMissing(int status) {
		return(CXPClient.statusMissing(status));
	}
	
	public void startTransactionTimer(){
	    startTime = System.currentTimeMillis();
	}
	public long getTotalBytes(){
	    return(this.totalBytes);
	}
	public void setTotalBytes(long totalBytes){
	    this.totalBytes = totalBytes;
	}
	public long getElapsedTime(){
	    return(System.currentTimeMillis() - startTime);
	}
	
	
}
