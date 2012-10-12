/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.selftest;

/**
 * Represents the result of a Self Test
 * @author ssadedin
 */
public class SelfTestResult {
    
    private String name;
    private String message;
    private Throwable exception;
    private String status;
    private String tips;
    private long timeMs;
    
    @Override
    public String toString() {
       return String.format("Name: %s status=%s, message=%s, ex=", name,status,message,exception);
    }
    
    public Throwable getException() {
        return exception;
    }
    public void setException(Throwable exception) {
        this.exception = exception;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }
}
