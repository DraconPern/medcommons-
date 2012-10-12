/*
 * $Id$
 * Created on 30/01/2007
 */
package net.medcommons.rest;

public interface RESTConfiguration {
    /**
     * Return the given property, if it exists, or null otherwise
     * 
     * @throws RESTConfigurationException
     */
    String getProperty(String name) throws RESTConfigurationException;
    
    /**
     * Return the given property, if it exists, or the given default otherwise
     * 
     * @throws RESTConfigurationException
     */
     String getProperty(String name, String defaultValue);
    
    /**
     * Return the given property, if it exists, or the given default otherwise
     * 
     * @throws RESTConfigurationException
     */
     int getProperty(String name, int defaultValue);
    
    /**
     * Return the given property, if it exists, or the given default otherwise
     * 
     * @throws RESTConfigurationException
     */
     boolean getProperty(String name, boolean defaultValue);
}
