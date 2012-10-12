package net.medcommons.modules.services.interfaces;

/**
 * Queries for known patients of a group based on basic demographics
 * 
 * @author ssadedin
 */
public interface GroupPatientQueryService {
    
    /**
     * Return all patients known for groups to which the presented auth token is authorized
     * and which match the parameters given.  Where a parameter is null, it is treated as a 
     * wildcard.
     * @throws ServiceException 
     */
    PatientDemographics []  query(String firstName, String lastName, String sex, String auth) throws ServiceException;

}
