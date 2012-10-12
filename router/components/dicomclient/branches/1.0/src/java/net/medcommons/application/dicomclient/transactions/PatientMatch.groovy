package net.medcommons.application.dicomclient.transactions;

import java.util.List;
import java.util.WeakHashMapimport net.medcommons.modules.services.interfaces.PatientDemographicsimport net.medcommons.application.dicomclient.ContextManagerimport groovy.util.Expandoimport net.medcommons.application.dicomclient.utils.StatusDisplayManager
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import static net.medcommons.application.utils.Str.*
import net.sourceforge.pbeans.Storeimport net.medcommons.application.dicomclient.utils.DB
import org.apache.log4j.Logger;

public class PatientMatch {
    
	private static Logger log = Logger.getLogger(PatientMatch.class);
	
	public static ContextManager contextManager = ContextManager.get()
	 
	static WeakHashMap<String, PatientDemographics> knownPatients = new  WeakHashMap<String, PatientDemographics>();

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
    public static List<PixDemographicData> getPatient(String givenName, String middleName, String familyName) {
	    
        if(!familyName)
        	return null
	    
        Store db= DB.get();
        
        def params =['familyName':familyName.toUpperCase()]
        
        if(givenName)
        	  params.givenName = givenName.toUpperCase()
        
        if(middleName)
        	  params.middleName = middleName.toUpperCase()
        	  
        // Should sort by time? Priority?
        List<PixDemographicData> matchingPatients = db.all(PixDemographicData.class, params)
        if(!matchingPatients) 
            return null; 
	        
        return matchingPatients;
	}
	
	/**
	 * Cache the given patient for later reference.  This
	 * is used only to speed up later lookups.
	 */
	public static void cache(PatientDemographics patient) {
        synchronized(knownPatients) {
            log.info "Caching patient (given: $patient.givenName family: $patient.familyName sex: $patient.sex)"
            knownPatients[patient.accountId] = 
                new PatientDemographics(accountId:patient.accountId, givenName: patient.givenName, familyName:patient.familyName, sex: patient.sex)
        }
	    
	}
	
	public static void flushCache() {
        synchronized(knownPatients) {
            knownPatients.clear()
        }
	}
	
	/**
	 * Search the patient cache for patients matching requested attributes.
	 * <p>
	 * Family name *must* be provided and must match to return a patient.
	 * <p>
	 * Other Attributes that are provided and are non-null are 
	 * required as exact non-case-sensitive matches in order 
	 * to return a match.
	 */
	static PatientDemographics searchCache(def search) {
        synchronized(knownPatients) {
            
            def results = []
            knownPatients.each { accountId, p ->
                
                // Absolutely require match in family name
                // note this still matches if both null
                if(!eqi(p.familyName,search.familyName))
                    return
                
                // if both specify sex then it must match
                if(p.sex && search.sex && p.sex != search.sex)
                    return
                
                // if both specify given name then it must match
                if(p.givenName && search.givenName && !eqi(search.givenName,p.givenName))
                    return
                
                // If nothing stopped us getting to here then we found a match
                results << p
            }
            
            log.info "Found " + results?results[0]:null
            
            if(!results)
                return null
                
            if(results.size() > 1) {
                log.warn "Found ${results.size()} patients matching ${search} - returning no match due to ambiguity"
                return null
            }
            
            return results[0]
        }
	}
	
	/**
	 * Queries the current remote group for patients matching the given attributes
	 */
	static PatientDemographics queryGroup(String givenName, String middleName, String familyName, String sex) {
	    
        def patients = contextManager.getQueryService().query(givenName, middleName, familyName, sex)
        if(!patients)
            return null
        
        if(patients.size() > 1) {
            log.warn "Patient ${givenName?:''} ${middleName?:''} ${familyName?:''} matched multiple existing patients. Please resolve manually."
            StatusDisplayManager.get().setErrorMessage("Duplicate Patient", 
                    "Patient ${givenName?:''} ${middleName?:''} ${familyName?:''} matched multiple existing patients. Please resolve manually.")
            return null;        
        }
        
        def p = patients[0]
        
        log.info "Matched patient account ${p.accountId} to ${givenName} ${middleName} ${familyName} ${sex}"
        cache(p)
        return p
	}
	
	/**
	 * Attempts to create a new context state for the requested patient
	 * by resolving an existing storage id for the patient if possible.
	 */
	static ContextState resolveContextState(String givenName, String middleName, String familyName, String sex, ContextState baseCtx) {
	     
	    // If a storage id has already been allocated to current context state then
	    // simply return a clone of this state so that the patient
	    // data gets assigned to the specified patient.
	    if(baseCtx.storageId)
	        return baseCtx.clone()
	    
	    // Automatic conversion from DICOM form to CCR / registry form
        switch(sex) {
	        case 'M': sex = 'Male'; break;
	        case 'F': sex = 'Female'; break;
        }
        
	    def search = [ givenName: givenName, middleName: middleName, familyName: familyName, sex: sex ]
	    
	    // First query the cached patients
	    def patient = searchCache(search)
	    
	    // If not found in cache, ask at the group level
	    if(!patient) 
	        patient = queryGroup(search.giveName, search.familyName, search.sex, baseCtx.auth)
	        
	    log.info "resolveContextState found patient " + patient?.accountId + " for (given: $givenName, family: $familyName, sex: $sex)"
	    
	    if(!patient)
	        return null
	        
	    ContextState result = baseCtx.clone()
	    result.storageId = patient.accountId
	    return result
	}
    
    /**
     * Returns a patient given the identifier. Two use cases:
     * <ul>
     *   <li>Query for patient with an identifier from outside MedCommons (say - an identifier
     *   from the DICOM header </li>
     *   <li>Query for patient with a given MedCommons ID. </li>
     * </ul>
     */
    public static PixDemographicData getPatient(String affinityDomain, String affinityIdentifier) {
         
    	PixIdentifierData identifier = PatientMatch.getIdentifier(affinityDomain, affinityIdentifier);
    	
    	if(identifier == null) {
    		log.info("No matching identifier for " + affinityDomain + "," + affinityIdentifier);
    		return null;
    	}
    	
    	Store db = DB.get();
		Long demographicsId = identifier.getPixDemographicDataId();
		               
		def matchingPatients = db.all(PixDemographicData.class, [id:demographicsId])
		if(!matchingPatients)
		   throw new IllegalStateException("Matching identifier in database " + affinityDomain + ", " + affinityIdentifier +
				   " has no matching PixDemographicData with id " + demographicsId);
		
		if(matchingPatients.size() == 1)
		   return matchingPatients.get(0);
		   
		// Should not find more than one patient
		throw new IllegalStateException("Matching identifier in database " + affinityDomain + ", " + affinityIdentifier +
				   " has multiple matching PixDemographicData with id " + demographicsId);
	}
			
    /**
     * Returns the PixIdentifierData object for a given patient in a specified affinityDomain
     *
     * @param pixDemographicDataId
     * @param affinityDomain
     * @return
     */
    public static PixIdentifierData getIdentifier(Long pixDemographicDataId, String affinityDomain){
         
        Store db = DB.get();

        List<PixIdentifierData> matchingIdentifiers = 
            db.all(PixIdentifierData.class, [affinityDomain:affinityDomain.toUpperCase(), pixDemographicDataId:pixDemographicDataId])

        if(!matchingIdentifiers)
            return null;
        
        if(matchingIdentifiers.size()==1)
               return matchingIdentifiers.get(0);
            
        log.warn("Matching identifier in database " + affinityDomain + ", with pixDemographicDataId " + pixDemographicDataId +
 		 	   " should have a single matching entry, not " + matchingIdentifiers.size());
            
        return matchingIdentifiers.get(0);
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
       Store db = DB.get();
       PixIdentifierData matchingPixData = null;
       DicomMetadata dicomM = null;
	   List<DicomMetadata> matchingDicom = db.all(DicomMetadata.class, [studyInstanceUid:dicomTransaction.studyInstanceUid])
	   if(matchingDicom.size() > 0) 
		  dicomM = matchingDicom.get(0); // All DICOM objects with this StudyInstanceUID will have same PatientID; just grab the first.
     
       if(dicomM !=null) 
    	   matchingPixData = getIdentifier(dicomM.getInstitutionName(), dicomM.getPatientId());
       
       return matchingPixData;
   }
     
   /* Returns the PixIdentifierData object for a given patient in a specified affinityDomain
    *
    * @param pixDemographicDataId
    * @param affinityDomain
    */
    public static PixIdentifierData getIdentifier(String affinityDomain, String affinityIdentifier) {
       
    	if((affinityDomain == null) || (affinityIdentifier == null))
    		return null;
    	
        Store db = DB.get();
        log.debug("Searching for identifier " + affinityDomain + "," +affinityIdentifier );
        
        List<PixIdentifierData> matchingIdentifiers = 
            db.all(PixIdentifierData.class, [affinityDomain: affinityDomain.toUpperCase(), affinityIdentifier: affinityIdentifier.toUpperCase()])
            
        if(matchingIdentifiers.size() == 0) {
        	log.debug("No matches for " + affinityDomain + "," +affinityIdentifier );
            return null;
        }
         
        if(matchingIdentifiers.size() ==1) {
        	log.debug("Exact match for " + affinityDomain + "," +affinityIdentifier );
            return matchingIdentifiers.get(0);
        }
        
            
        log.warn("Matching identifier in database " + affinityDomain + ", " + affinityIdentifier +
     			   " should have a single matching entry, not " + matchingIdentifiers.size());
            
        return matchingIdentifiers.get(0);
    }
}
