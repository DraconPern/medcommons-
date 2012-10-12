package net.medcommons.modules.cxp.protocol;

/**
 * Interface for measuring I/O in socket transfers.
 * Necessary because XFire doesn't report true transfer I/O - it reports progress on 
 * reading cached files on disk and not on the network.
 * 
 * @author mesozoic
 *
 */
public interface MeteredIO {

	public long getInputBytes();
	public long getOutputBytes();
	public void cancelStream();
	
}
