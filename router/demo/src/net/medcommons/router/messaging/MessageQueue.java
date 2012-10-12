package net.medcommons.router.messaging;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class MessageQueue {
    
    Long id;
    
    Date queuetime;
    
    String message;
    
    String connectionId;
    
    String source;
    
    public MessageQueue() {
    }
    
    public MessageQueue(String connectionId, String message) {
        super();
        this.connectionId = connectionId;
        this.message = message;
        this.queuetime = new Date();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getQueuetime() {
        return queuetime;
    }

    public void setQueuetime(Date queuetime) {
        this.queuetime = queuetime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
}
