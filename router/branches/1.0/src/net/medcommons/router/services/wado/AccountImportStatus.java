/*
 * $Id: AccountImportStatus.java 2832 2008-08-15 19:26:36Z sdoyle $
 * Created on 17/06/2008
 */
package net.medcommons.router.services.wado;


import net.medcommons.modules.transfer.DownloadFileAgent;
import net.medcommons.modules.transfer.UploadFileAgent;

import org.apache.log4j.Logger;

/**
 * Provides intelligent progress tracking of an account import operation.
 * <p>
 * Tracks the state of the import through 5 stages and provides an overall
 * progress indicator {@link #progress} which relates the fraction of the import
 * that has been completed.  This is only an estimate and in some cases
 * may be a crude estimate, eg. when the reference size information is
 * missing from the CCR.
 * <p>
 * For accurate progress information this class relies on it's creator
 * setting the {@link #uploader} and {@link #downloader} properties
 * when they are available at the relevant stages of upload / download.
 * 
 * @author ssadedin
 */
public class AccountImportStatus {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AccountImportStatus.class);
    
    /**
     * Valid states through which an account import transitions
     */
    public static enum State {
        INITIALIZING, DOWNLOADING, UPLOADING, MERGING, FINISHED, CANCELLATION_REQUESTED, CANCELLED, FAILED_MERGE
    }
    
    /**
     * The value of {@link #progress} that is sent back to the client when 
     * we begin uploading (importing) the downloaded content into
     * the local repository
     */
    public static final float PROGRESS_BEGIN_UPLOAD = 0.55f;

    /**
     * The value of {@link #progress} that is sent back to the client when 
     * we are beginning download of references
     */
    public static final float PROGRESS_BEGIN_DOWNLOAD = 0.15f;

    /**
     * The value of {@link #progress} that is sent back to the client when 
     * we are beginning merge of CCRs
     */
    public static final float PROGRESS_BEGIN_MERGE = 0.85f;
    
    
    private float progress = 0.0f;
    
    private String status = "Initializing";
    
    private boolean errorFlag = false;
    
    /**
     * Estimate of number of bytes that will be transferred in the import
     */
    private long expectedByteCount = 0;
    
    private State state = State.INITIALIZING;
    
    /**
     * CXP Client used for download
     */
    private DownloadFileAgent downloader = null;
    
    
    public synchronized DownloadFileAgent getDownloader() {
        return downloader;
    }

    private UploadFileAgent uploader = null;


    public synchronized float getProgress() {
        switch(state) {
            case DOWNLOADING:
                if(this.downloader != null && this.expectedByteCount > 0) {
                    float pct = (float)this.downloader.getBytesTransferred() / (float)this.expectedByteCount;
                    if(pct > 1.0f) 
                        pct = 1.0f; 
                    progress = PROGRESS_BEGIN_DOWNLOAD + pct * (PROGRESS_BEGIN_UPLOAD - PROGRESS_BEGIN_DOWNLOAD);
                    log.info("bytes = " + this.downloader.getBytesTransferred() + " / " + this.expectedByteCount + " (" + (100*pct) + "%)");
                }
                break;
            case UPLOADING:
                if(this.uploader != null) {
                    float pct = (float)this.uploader.getBytesTransferred() / (float)this.downloader.getBytesTransferred();
                    if(pct > 1.0f) 
                        pct = 1.0f; 
                    progress = PROGRESS_BEGIN_UPLOAD + pct * (PROGRESS_BEGIN_MERGE - PROGRESS_BEGIN_UPLOAD);
                    log.info("bytes = " + this.uploader.getBytesTransferred() + " / " + this.downloader.getBytesTransferred() + " (" + (100*pct) + "%)");
                }
                break;
                
            default:
                // do nothing
        }
        
        return progress;
    }

    public synchronized void setProgress(float progress) {
        this.progress = progress;
    }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        this.status = status;
    }

    public synchronized boolean getErrorFlag() {
        return errorFlag;
    }

    public synchronized void setErrorFlag(boolean errorFlag) {
        this.errorFlag = errorFlag;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void setState(State state) {
        this.state = state;
    }

    public synchronized void setDownloader(DownloadFileAgent downloader) {
        this.downloader = downloader;
    }

    public synchronized long getExpectedByteCount() {
        return expectedByteCount;
    }

    public synchronized void setExpectedByteCount(long expectedByteCount) {
        this.expectedByteCount = expectedByteCount;
    }

    public synchronized void setUploader(UploadFileAgent uploader) {
        this.uploader = uploader;
    }

}
