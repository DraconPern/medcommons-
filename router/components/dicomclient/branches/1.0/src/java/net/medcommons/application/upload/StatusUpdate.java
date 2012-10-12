package net.medcommons.application.upload;

public interface StatusUpdate {
    
    
    /**
     * Callback for display of status in the upload applet.
     * 
     * @param status
     */
    
	public void updateState(State status);
    public void updateProgress(long byteCount, long totalBytes);
	public void updateRate(long byteCount, long elapsedTime);
	public void updateMessage(String message);
}
