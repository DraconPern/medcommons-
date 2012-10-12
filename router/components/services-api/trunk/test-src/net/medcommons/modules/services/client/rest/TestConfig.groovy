package net.medcommons.modules.services.client.rest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.rest.RESTConfiguration;
import net.medcommons.rest.RESTConfigurationException;

public class TestConfig implements RESTConfiguration {
    
    public String getProperty(String name) throws RESTConfigurationException {
        try {
            return Configuration.getProperty(name);
        }
        catch (ConfigurationException e) {
            throw new RESTConfigurationException("Failed retrieving configuration value " + name, e);
        }
    }
    public String getProperty(String name, String defaultValue) {        
        return Configuration.getProperty(name,defaultValue);
    }
    
    public int getProperty(String name, int defaultValue) {
        return Configuration.getProperty(name, defaultValue);
    }
    
    public boolean getProperty(String name, boolean defaultValue) {
        return Configuration.getProperty(name,defaultValue);
    }            
}
