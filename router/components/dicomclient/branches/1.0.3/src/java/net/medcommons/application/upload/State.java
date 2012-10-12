package net.medcommons.application.upload;

/**
 * Defines the state of the upload transfer.
 * @author sean
 *
 */
public enum State {
    
    INITIALIZING("Initializing"), 
    UPLOADING ("Uploading"), 
    FINISHED ("Finished"), 
    CANCELLED("Cancelled"), 
    FAILED("Failed");
   
    
    
    String displayName = null;
    public String getDisplayName(){
        return(this.displayName);
    }
    State(String displayName){
        this.displayName = displayName;
    }
}
