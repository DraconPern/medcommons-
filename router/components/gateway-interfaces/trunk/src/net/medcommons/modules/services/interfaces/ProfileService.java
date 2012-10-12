/*
 * $Id: ProfileService.java 3528 2009-10-30 07:45:05Z ssadedin $
 * Created on 15/10/2008
 */
package net.medcommons.modules.services.interfaces;

import java.util.List;

/**
 * Profile Service provides storage and retrieval of PHR profiles - named
 * views of a user's health record.
 */
public interface ProfileService {
    
    /**
     * Create a new profile matching the supplied data.  If a profile of the same
     * name already exists then it will be replaced.
     * 
     * @param profile
     */
    void createProfile(String storageId, PHRProfile profile) throws ServiceException;
   
    /**
     * Hide the specified profile.
     * 
     * @param profileId
     * @throws ServiceException
     */
    void hideProfile(String storageId, String profileId) throws ServiceException;
    
    /**
     * Retrieve all profiles
     */
    List<PHRProfile> getProfiles(String storageId) throws ServiceException;

    /**
     * Delete all profiles for the specified account.
     * 
     * @param storageId
     * @throws ServiceException
     */
    public void deleteProfile(String storageId) throws ServiceException;
    
}
