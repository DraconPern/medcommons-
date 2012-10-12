package net.medcommons.application.dicomclient.transactions;

import java.util.List;

import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

public class PatientMatch {
	 private static Logger log = Logger.getLogger(PatientMatch.class
	            .getName());

	/**
     * This needs a lot more thought.
     * Matches on patient objects. Note that sending a 'null' is the same as a wildcard
     * with the exception of 'familyName' - a null 'familyName' allways returns a null
     * result.
     *
     * @param storageId
     * @param guid
     * @return
     */
    public static List<PixDemographicData> getPatient(String givenName, String middleName, String familyName){
        Session session = LocalHibernateUtil.currentSession();
        try{
        // Select a queued job.
        Criteria qPatient = session
                .createCriteria(PixDemographicData.class);

        if (givenName != null)
        	  qPatient.add(Expression.eq("givenName", givenName.toUpperCase()));
        if (middleName != null)
      	  qPatient.add(Expression.eq("middleName", middleName.toUpperCase()));
        if (familyName == null)
        	return(null);
        else if (familyName != null)
        	qPatient.add(Expression.eq("familyName", familyName.toUpperCase()));

        // Should sort by time? Priority?
        List<PixDemographicData> matchingPatients = qPatient.list();
        if (matchingPatients.size() == 0){
            return(null);
        }
        else{
        	return(matchingPatients);
        }
        }
        finally{
        	LocalHibernateUtil.closeSession();
        }
    }
    
    /**
     * Returns a the PixDemographicData for a given id. The id here is the 
     * internal database id, not any external identifier.
     * 
     * @param pixDemographicId
     * @return
     */
    public static PixDemographicData getPatient(Long pixDemographicId){
        Session session = LocalHibernateUtil.currentSession();
        try{
        // Select a queued job.
        Criteria qPatient = session
                .createCriteria(PixDemographicData.class);

        qPatient.add(Expression.eq("id", pixDemographicId));
        
        List<PixDemographicData> matchingPatients = qPatient.list();
        if (matchingPatients.size() == 0){
            return(null);
        }
        else{
        	return(matchingPatients.get(0));
        }
        }
        finally{
        	LocalHibernateUtil.closeSession();
        }
    }

    /**
     * Returns a patient given the identifier. Two use cases:
     * <ul>
     *   <li>Query for patient with an identifier from outside MedCommons (say - an identifier
     *   from the DICOM header </li>
     *   <li>Query for patient with a given MedCommons ID. </li>
     * </ul>
     * @param affinityDomain
     * @param affinityIdentifier
     * @return
     */
    public static PixDemographicData getPatient(String affinityDomain, String affinityIdentifier){
        	PixIdentifierData identifier = PatientMatch.getIdentifier(affinityDomain, affinityIdentifier);
        	if (identifier == null){
        		log.info("No matching identifier for " + affinityDomain + "," + affinityIdentifier);
        		return null;
        	}
        	Session session = LocalHibernateUtil.currentSession();
        	try{
               Long demographicsId = identifier.getPixDemographicDataId();
               Criteria patientQuery = session.createCriteria(PixDemographicData.class);
               patientQuery.add(Expression.eq("id", demographicsId));
               List<PixDemographicData> matchingPatients =  patientQuery.list();
               if (matchingPatients.size() == 0)
            	   throw new IllegalStateException("Matching identifier in database " + affinityDomain + ", " + affinityIdentifier +
            			   " has no matching PixDemographicData with id " + demographicsId);
               else if (matchingPatients.size() == 1){
            	   return(matchingPatients.get(0));
               }
               else{
            	   throw new IllegalStateException("Matching identifier in database " + affinityDomain + ", " + affinityIdentifier +
            			   " has multiple matching PixDemographicData with id " + demographicsId);
               }
        	}
        	finally{
        		LocalHibernateUtil.closeSession();
        	}


    }
    /**
     * Returns the PixIdentifierData object for a given patient in a specified affinityDomain
     *
     * @param pixDemographicDataId
     * @param affinityDomain
     * @return
     */
    public static PixIdentifierData getIdentifier(Long pixDemographicDataId, String affinityDomain){
        Session session = LocalHibernateUtil.currentSession();

        try {
	        // Select a queued job.
	        Criteria identifierQ = session
	                .createCriteria(PixIdentifierData.class);
	
	        identifierQ.add(Expression.eq("affinityDomain", affinityDomain.toUpperCase()));
	        identifierQ.add(Expression.eq("pixDemographicDataId", pixDemographicDataId));
	
	        List<PixIdentifierData> matchingIdentifiers = identifierQ.list();
	
	        if (matchingIdentifiers.size() == 0) {
	            return(null);
	        }
	        else if (matchingIdentifiers.size() ==1){
	               PixIdentifierData identifier = matchingIdentifiers.get(0);
	               return(identifier);
	        }
	        else {
	            
	            log.warn("Matching identifier in database " + affinityDomain + ", with pixDemographicDataId " + pixDemographicDataId +
	     		 	   " should have a single matching entry, not " + matchingIdentifiers.size());
	            
	            PixIdentifierData identifier = matchingIdentifiers.get(0);
                return(identifier);
               
	        	// throw new IllegalStateException("Matching identifier in database " + affinityDomain + ", with pixDemographicDataId " + pixDemographicDataId +
	     		// 	   " should have a single matching entry, not " + matchingIdentifiers.size());
	        }
        }
        finally{
        	LocalHibernateUtil.closeSession();
        }

    }

    /**
     * Returns the PixIdentifierData for a given DICOM transaction. The DICOM
     * StudyInstanceUID is used to look up the corresponding DicomMetadata object;
     * this is then used to query for a PixIdentifierData object using
     * the DICOM institution name as the affinity domain and the patient id
     * as the affinity identifier.
     * @param dicomTransaction
     * @return
     */
   public static PixIdentifierData getIdentifier(DicomTransaction dicomTransaction){
       Session session = LocalHibernateUtil.currentSession();
       PixIdentifierData matchingPixData = null;
       DicomMetadata dicomM = null;
       try{
    	   Criteria dicomMetadataQ = session.createCriteria(DicomMetadata.class);
    	   dicomMetadataQ.add(Expression.eq("studyInstanceUid", dicomTransaction.getStudyInstanceUid()));
    	   List<DicomMetadata> matchingDicom = dicomMetadataQ.list();
    	   if (matchingDicom.size() > 0){
    		  dicomM = matchingDicom.get(0);// All DICOM objects with this StudyInstanceUID will have same PatientID; just grab the first.
    	   }
       }
       finally{
       	LocalHibernateUtil.closeSession();
       }
       if (dicomM !=null){
    	   matchingPixData = getIdentifier(dicomM.getInstitutionName(), dicomM.getPatientId());
       }
       return(matchingPixData);

   }
   /* Returns the PixIdentifierData object for a given patient in a specified affinityDomain
   *
   * @param pixDemographicDataId
   * @param affinityDomain
   * @return
   */
    public static PixIdentifierData getIdentifier(String affinityDomain, String affinityIdentifier){
    	if ((affinityDomain == null) || (affinityIdentifier == null))
    		return(null);
        Session session = LocalHibernateUtil.currentSession();
        log.debug("Searching for identifier " + affinityDomain + "," +affinityIdentifier );
        try{
	        // Select a queued job.
	        Criteria identifierQ = session
	                .createCriteria(PixIdentifierData.class);

	        identifierQ.add(Expression.eq("affinityDomain", affinityDomain.toUpperCase()));
	        identifierQ.add(Expression.eq("affinityIdentifier", affinityIdentifier.toUpperCase()));

	        List<PixIdentifierData> matchingIdentifiers = identifierQ.list();
	        if(matchingIdentifiers.size() == 0){
	        	log.debug("No matches for " + affinityDomain + "," +affinityIdentifier );
	            return(null);
	        }
	        else 
	        if(matchingIdentifiers.size() ==1) {
	        	log.debug("Exact match for " + affinityDomain + "," +affinityIdentifier );
	               PixIdentifierData identifier = matchingIdentifiers.get(0);
	               return(identifier);
	        }
	        else {
	            
	            log.warn("Matching identifier in database " + affinityDomain + ", " + affinityIdentifier +
		     			   " should have a single matching entry, not " + matchingIdentifiers.size());
	            
	            PixIdentifierData identifier = matchingIdentifiers.get(0);
	            return(identifier);
	        }
        }
        finally{
        	LocalHibernateUtil.closeSession();
        }

    }

    /*
    public static List<PixDemographicData>getPatientFromDICOMName(String dicomName){

    }
	*/
}
