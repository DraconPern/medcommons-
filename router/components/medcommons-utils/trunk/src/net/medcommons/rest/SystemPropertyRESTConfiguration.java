package net.medcommons.rest;

import static net.medcommons.modules.utils.Str.blank;

/**
 * A simple config class that reads froms ystem properties.
 * 
 * @author ssadedin
 */
public class SystemPropertyRESTConfiguration implements RESTConfiguration {

    @Override
    public String getProperty(String name) throws RESTConfigurationException {
        return System.getProperty(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        try {
            return blank(getProperty(name)) ? defaultValue : getProperty(name);
        }
        catch (RESTConfigurationException e) {
            return defaultValue;
        } 
    }

    @Override
    public int getProperty(String name, int defaultValue) {
        try {
            return getProperty(name)==null ? defaultValue : Integer.parseInt(getProperty(name));
        }
        catch (RESTConfigurationException e) {
            return defaultValue;
        } 
    }

    @Override
    public boolean getProperty(String name, boolean defaultValue) {
        try {
            return getProperty(name)==null ? defaultValue : Boolean.parseBoolean(getProperty(name));
        }
        catch (RESTConfigurationException e) {
            return defaultValue;
        } 
    }

}
