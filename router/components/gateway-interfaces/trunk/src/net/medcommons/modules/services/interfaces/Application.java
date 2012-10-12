/*
 * $Id: Application.java 3083 2008-11-17 07:03:41Z ssadedin $
 * Created on 20/03/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Application {
    
    String name;
    
    String code;
    
    String key;
    
    String email;
    
    String websiteUrl;
    
    Date createDateTime;
    
    String ipAddress;
    
    /**
     * Create an Application object
     */
    public Application(String name, String key, String code, String email, String websiteUrl, Date createDateTime,
                    String ipAddress) {
        this.name = name;
        this.key = key;
        this.code = code;
        this.email = email;
        this.websiteUrl = websiteUrl;
        this.createDateTime = createDateTime;
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
}
