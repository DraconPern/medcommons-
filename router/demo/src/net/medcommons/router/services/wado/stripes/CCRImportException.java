package net.medcommons.router.services.wado.stripes;

public class CCRImportException extends Exception {
    
    String messageKey;
    
    public CCRImportException(String messageKey, Throwable t) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
