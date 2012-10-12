package net.medcommons.modules.crypto;

/**
 * Simple structure for holding decryption parameters.
 * @author sean
 *
 */
public class DecryptionParameters {
	private long streamLength = -1;
	private byte[] iv = null;
	
	public void setStreamLength(long streamLength){
		this.streamLength = streamLength;
	}
	public long getStreamLength(){
		return(streamLength);
	}
	
	public void setIV(byte[] iv){
		this.iv = iv;
	}
	public byte[] getIV(){
		return(iv);
	}
}
