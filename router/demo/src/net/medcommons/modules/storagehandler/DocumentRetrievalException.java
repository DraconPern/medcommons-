package net.medcommons.modules.storagehandler;

import net.medcommons.phr.ccr.CCRElement;

public class DocumentRetrievalException extends RuntimeException{
    
    public DocumentRetrievalException(String service, String identifier){
        super(service + ", identifier=" + identifier);
        
    }
    public DocumentRetrievalException(CCRElement element){
        super(element.toString());
        
    }
}
