package net.medcommons.client.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.namespace.QName;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.DicomMetadata.DicomPreset;
import net.medcommons.modules.utils.dicom.DicomNameParser;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import astmOrgCCR.*;
import astmOrgCCR.ActorType.Address;
import astmOrgCCR.ActorType.InformationSystem;
import astmOrgCCR.ActorType.Person;
import astmOrgCCR.ActorType.Person.Name;
import astmOrgCCR.CodedDescriptionType.ObjectAttribute;
import astmOrgCCR.CodedDescriptionType.ObjectAttribute.AttributeValue;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.*;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Body.Procedures;
import astmOrgCCR.LocationDocument.Location;
import astmOrgCCR.LocationsDocument.Locations;

/**
 * Simple DICOM to CCR generator.
 * Parts of this should be factored out to another module so that it can be
 * use in non-DICOM projects.
 *
 * @author mesozoic
 *
 */
public class CCRGenerator {
      public static final String EXACT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
      private final static String CCR_NAMESPACE = "urn:astm-org:CCR";
      private final static String UNKNOWN = "UNKNOWN";
    private static Logger log = Logger.getLogger(CCRGenerator.class.getName());
    private CCRCrossReferences crossReferences;
    private ContinuityOfCareRecordDocument newContinuityOfCareDocument;
    private ProcedureType dicomProcedure = null;
    private String versionNumber = null;
   
    
    public CCRGenerator(){
    	newContinuityOfCareDocument = ContinuityOfCareRecordDocument.Factory.newInstance();
    	newContinuityOfCareDocument.addNewContinuityOfCareRecord();
    	 long time=System.currentTimeMillis();
    	 ContinuityOfCareRecord newCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    	 newCCR.setCCRDocumentObjectID("MC" + time);
         newCCR.addNewBody();
       
 	        CodedDescriptionType language = newCCR.addNewLanguage();
 	        language.setText("English");
        
 	       newCCR.setVersion("V1.0");
         

         DateTimeType ccrCreationTime = newCCR.addNewDateTime();
         
         DateFormat exactDateFormat = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
         exactDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
         exactDateFormat.setLenient(true);
         String exactDateTime = exactDateFormat.format(new Date());
         ccrCreationTime.setExactDateTime(exactDateTime);
         ContextManager contextManager = ContextManager.get();

         Configurations configurations = contextManager.getConfigurations();
         versionNumber = configurations.getVersion();
    }
    /**
     * Sets the patient object in the new CCR to be the one from the old CCR
     * 
     * @param originalCCRDocument
     */
    public CCRGenerator(ContinuityOfCareRecordDocument originalCCRDocument){
    	this();
    	ContinuityOfCareRecord originalCCR = originalCCRDocument.getContinuityOfCareRecord();
    	List<Patient> patientList = originalCCR.getPatientList();
    	if (patientList.size()>1){
    		// If the patient is a siamese twin - there may be parts of the code
    		// which fail below (and in MedCommons in general). Throw 
    		// an exception now.
    		throw new IllegalArgumentException("CCR contains multiple(" + patientList.size() + 
    				") patients");
    	}
    	Patient oldPatient = patientList.get(0); // Assume not Siamese twins
    	String patientActorId = oldPatient.getActorID();
    	
    	ContinuityOfCareRecord newCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    	Patient patient = newCCR.addNewPatient();
    	patient.setActorID(patientActorId);
    	
    	Actors actors = originalCCR.getActors();
    	
    	List<ActorType> actorList = actors.getActorList();
    	for (ActorType a: actorList){
    		String actorObjectId = a.getActorObjectID();
    		if (patientActorId.equals(actorObjectId)){
    			Actors newActors = newCCR.addNewActors();
    			newActors.addNewActor();// Add new dummy actor
    			newActors.getActorList().set(0, a); // Replace with old actor.
    			break;
    		}
    	}
    	// Need to see if the patient source references things that should be brought over.
    	
    }



    private ActorType addPersonActor(Actors actors, String actorId, String personName, String personAddress, String personTelephoneNumber, 
    		String personEmail){
        ActorType actor = actors.addNewActor();
        actor.setActorObjectID(actorId);
        Person person = actor.addNewPerson();
        Name name = person.addNewName();
        PersonNameType currentName = name.addNewCurrentName();
        DicomNameParser dicomNameParser = new DicomNameParser();
        String givenName = dicomNameParser.givenName(personName);
        String middleName = dicomNameParser.middleName(personName);
        String familyName = dicomNameParser.familyName(personName);
        if (givenName != null)
            currentName.addGiven(givenName);
        else
        	currentName.addGiven("UNKNOWN");
        if (middleName != null)
            currentName.addMiddle(middleName);
        if (familyName != null)
            currentName.addFamily(familyName);
        else
        	currentName.addFamily("UNKNOWN");


        if (personAddress != null){
            Address address = actor.addNewAddress();
            address.setLine1(personAddress);
        }
        if (personTelephoneNumber != null){
            CommunicationType telephone = actor.addNewTelephone();
            telephone.setValue(personTelephoneNumber);
        }
        if (personEmail != null){
        	CommunicationType email = actor.addNewEMail();
        	email.setValue(personEmail);
        }
        SourceType actorSource = actor.addNewSource();
        ActorReferenceType sourceActor = actorSource.addNewActor();
        sourceActor.setActorID(crossReferences.referenceSourceActorId);
        return(actor);


    }
    /**
     * Creates a MedCommons ID for a particular actor.
     * Note that the source of this is unknown.
     * <Source>
            <Description>
              <Text>Unknown</Text>
            </Description>
          </Source>
     * @param actor
     * @param medcommonsId
     * @param sourceDescription
     */
    public static void addMedcommonsID(ActorType actor, String medcommonsId, String sourceDescription){
    	IDType idtype = actor.addNewIDs();
        idtype.setID(medcommonsId);
        CodedDescriptionType cdt = idtype.addNewType();
        cdt.setText(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);
        SourceType  source = idtype.addNewSource();
        CodedDescriptionType description = source.addNewDescription();
        description.setText(sourceDescription);
        
    }
    private void addPatientActor(Actors actors, CCRCrossReferences ccrCrossReferences, String storageId, DicomMetadata dicomMetadata){
        ActorType actor = actors.addNewActor();
        actor.setActorObjectID(ccrCrossReferences.patientActorId);
        Person person = actor.addNewPerson();
        Name name = person.addNewName();
        PersonNameType currentName = name.addNewCurrentName();
        DicomNameParser dicomNameParser = new DicomNameParser();
        String givenName = dicomNameParser.givenName(dicomMetadata.getPatientName());
        String middleName = dicomNameParser.middleName(dicomMetadata.getPatientName());
        String familyName = dicomNameParser.familyName(dicomMetadata.getPatientName());
        
        if (givenName != null)
            currentName.addGiven(givenName);
        else
        	currentName.addGiven("UNKNOWN");
        
        if (middleName != null)
            currentName.addMiddle(middleName);
        if (familyName != null)
            currentName.addFamily(familyName);
        else
        	currentName.addFamily("UNKNOWN");

       log.info("DICOM: given: '" + givenName + "', family:'" + familyName + "'");
       
     
       log.info(currentName.toString());
       List<String> f = currentName.getGivenList();
       for (int i=0;i<f.size(); i++)
       {
    	   log.info("Given entry : " + i + " = '" + f.get(i) + "'");
       }
       
        boolean hasDob = false;
        if (dicomMetadata.getPatientDateOfBirth() != null){
        	DateTimeType dob = person.addNewDateOfBirth();
            CodedDescriptionType dobValue = dob.addNewApproximateDateTime();

            dobValue.setText(dicomMetadata.getPatientDateOfBirth());
            log.info("Added patient date of birth " + dobValue.getText());
            hasDob = true;

        }
        else{
        	log.info("No patient dob");
        }
        if ((!hasDob) && (dicomMetadata.getPatientAge() != null)){ // Only add age if there is no DOB.
            MeasureType age = MeasureType.Factory.newInstance();
            age.setValue(dicomMetadata.getPatientAge());
            log.info("Added patient age " + age.getValue());
           
        }
        else{
        	log.info("age not added: hasDob=" + hasDob + ", aget =" +dicomMetadata.getPatientAge());
        }
        if (dicomMetadata.getPatientSex() != null){
            CodedDescriptionType sex = person.addNewGender();
            sex.setText(DicomNameParser.parseSex(dicomMetadata.getPatientSex()));
        }
        if (dicomMetadata.getPatientAddress() != null){
            Address address = actor.addNewAddress();
            address.setLine1(dicomMetadata.getPatientAddress());
        }
        if (dicomMetadata.getPatientTelephoneNumber()!= null){
            CommunicationType telephone = actor.addNewTelephone();
            telephone.setValue(dicomMetadata.getPatientTelephoneNumber());
        }
        if (dicomMetadata.getPatientEmail()!= null){
            CommunicationType email = actor.addNewEMail();
            email.setValue(dicomMetadata.getPatientEmail());
        }

        IDType idtype = actor.addNewIDs();
        idtype.setID(storageId);
        SourceType mcidSource = idtype.addNewSource();
        ActorReferenceType mcidRef = mcidSource.addNewActor();
        mcidRef.setActorID(ccrCrossReferences.medCommonsInformationActorId);
        ActorReferenceType medCommonsActorRef = idtype.addNewIssuedBy();
        medCommonsActorRef.setActorID(ccrCrossReferences.medCommonsInformationActorId);
        CodedDescriptionType cdt = idtype.addNewType();
        cdt.setText(DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);

        if (dicomMetadata.getPatientId() != null){
            idtype = actor.addNewIDs();
            ActorReferenceType issuedby = idtype.addNewIssuedBy();
            issuedby.setActorID(ccrCrossReferences.referenceSourceActorId);
            idtype.setID(dicomMetadata.getPatientId());
            cdt = idtype.addNewType();
            String institution = dicomMetadata.getInstitutionName();
            if (institution == null){
            	institution = UNKNOWN;
            }
            cdt.setText(institution);
            SourceType dicomIdSource = idtype.addNewSource();
            ActorReferenceType dicomIdRef = dicomIdSource.addNewActor();
            dicomIdRef.setActorID(ccrCrossReferences.referenceSourceActorId);
        }
        SourceType actorSource = actor.addNewSource();
        ActorReferenceType sourceActor = actorSource.addNewActor();
        sourceActor.setActorID(ccrCrossReferences.referenceSourceActorId);

    }

    
    /**
     * Initializes the cross reference structure so that Reference objects
     * can be created.
     * 
     * @param storageId
     * @param dicomMetadata
     * @param transactions
     * @param referenceProper
     * @return
     * @throws IOException
     */
    public ContinuityOfCareRecordDocument initializeCrossReferences(String storageId, DicomMetadata dicomMetadata) throws IOException{

    	ContinuityOfCareRecord aCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    

        crossReferences = new CCRCrossReferences();
        Actors actors = aCCR.getActors();
        if (actors == null)
            actors = aCCR.addNewActors();
        DicomMetadata firstMetadata = dicomMetadata;

        ActorType dicomActor = getInformationSystemActor(actors, firstMetadata.getInstitutionName(), "1.0");
        ActorType medCommonsActor = getInformationSystemActor(actors, "MedCommons", versionNumber);
        if (dicomActor != null){
            crossReferences.referenceSourceActorId = dicomActor.getActorObjectID();
        }
        else{
            crossReferences.referenceSourceActorId = addInformationSystemActor(actors,  firstMetadata, "1.0",null);
        }
        if (medCommonsActor != null){
        	 crossReferences.medCommonsInformationActorId = medCommonsActor.getActorObjectID();
        }
        else{
        	crossReferences.medCommonsInformationActorId =
                addInformationSystemActor(actors, "MedCommons", versionNumber, null);
        }

        Patient[] patientArray  = aCCR.getPatientArray();
        Patient patient = null;
        if ((patientArray != null) &&(patientArray.length >0)){
        	patient = patientArray[0];
        	
        	
        }
        if (patient==null){
        	throw new NullPointerException("No Patient defined in CCR");
        }
        crossReferences.patientActorId = patient.getActorID();

        

       

        Body body = aCCR.getBody();
        if (body == null)
        	body = aCCR.addNewBody();

        Procedures procedures = body.getProcedures();
        if (procedures == null)
        	procedures = body.addNewProcedures();

        dicomProcedure = procedures.addNewProcedure();

        // In the future - maybe test for duplicates.
        dicomProcedure.setCCRDataObjectID(makeId("PROCEDURE"));
        CodedDescriptionType procedureType = dicomProcedure.addNewType();
        procedureType.setText("Radiology");
        CodedDescriptionType procedureDescription = dicomProcedure.addNewDescription();
        String studyDescription = firstMetadata.getStudyDescription();
        if (studyDescription == null){
        	studyDescription = "Study";
        }
        
        // Add StudyInstanceUID
        procedureDescription.setText(studyDescription);
        ObjectAttribute studyUIDAttribute = procedureDescription.addNewObjectAttribute();
        studyUIDAttribute.setAttribute("StudyInstanceUID");
        
    	addObValue(studyUIDAttribute.addNewAttributeValue(), firstMetadata.getStudyInstanceUid());
       
        if (firstMetadata.getReasonForStudy() != null){
        	CodedDescriptionType reason = dicomProcedure.addNewDescription();
        	reason.setText(firstMetadata.getReasonForStudy());
        }
        Date studyDate = firstMetadata.getStudyDate();
        String studyDateTime = null;
        if (studyDate != null){
        	DateFormat exactDateFormat = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
            exactDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            exactDateFormat.setLenient(true);
            studyDateTime =exactDateFormat.format(studyDate);
        	 List<DateTimeType>  procedureDateTime = dicomProcedure.getDateTimeList();
             if ( (procedureDateTime == null) || (procedureDateTime.size() == 0)){
                 DateTimeType d = dicomProcedure.addNewDateTime();
                 d.setExactDateTime(studyDateTime);
                 log.info("set procedure date to be " +  studyDateTime);

             }
             else{
            	 log.info("Did not set study date " + studyDateTime + ", procedureDateTimeSize is " + 
            			 procedureDateTime.size());
             }
        }
        else{
        	log.error("DICOM study has no date" );
        	}
        if (firstMetadata.getBodyPart() != null){
            SiteType site = dicomProcedure.addNewSite();
            //CodeType siteCode =  site.addNewCode();
            site.setText(firstMetadata.getBodyPart());
        }
        



        Locations locations = dicomProcedure.addNewLocations();
        Location location = locations.addNewLocation();
        ActorReferenceType locationActor = location.addNewActor();
        locationActor.setActorID(crossReferences.referenceSourceActorId);

        if (firstMetadata.getAccessionNumber() != null){
            IDType accession = dicomProcedure.addNewIDs();
            ActorReferenceType issuedby = accession.addNewIssuedBy();
            issuedby.setActorID(crossReferences.referenceSourceActorId);
            accession.setID(firstMetadata.getAccessionNumber());
            SourceType accessionSource = accession.addNewSource();
            ActorReferenceType accessionActorSource = accessionSource.addNewActor();
            accessionActorSource.setActorID(crossReferences.referenceSourceActorId);
        }

        CodedDescriptionType procedureStatus = dicomProcedure.addNewStatus();
        procedureStatus.setText("In Progress");

        MethodType method = dicomProcedure.addNewMethod();
        method.setText(firstMetadata.getModality());
        SourceType dicomSource = dicomProcedure.addNewSource();
        ActorReferenceType dicomRef = dicomSource.addNewActor();
        dicomRef.setActorID(crossReferences.referenceSourceActorId);

        // Editing needed below. Need to detect if the physician is there first before adding new actor.
        if (firstMetadata.getReferringPhysicianName() != null){
            crossReferences.toActorId = makeId("AA");
            To to = aCCR.addNewTo();
            ActorReferenceType[] actorRefs = new ActorReferenceType[1];

            actorRefs[0] = ActorReferenceType.Factory.newInstance();

            actorRefs[0].setActorID(crossReferences.toActorId);
            CodedDescriptionType actorRole = CodedDescriptionType.Factory.newInstance();
            actorRole.setText("Referring Physician");
            CodedDescriptionType actorRoles[] = new CodedDescriptionType[1];
            actorRoles[0] = actorRole;
            actorRefs[0].setActorRoleArray(actorRoles);
            to.setActorLinkArray(actorRefs);
            String personName = firstMetadata.getReferringPhysicianName();
            String personAddress = firstMetadata.getReferringPhysicianAddress();
            String personTelephoneNumber = firstMetadata.getReferringPhysicianTelephoneNumber();
            String personEmail = firstMetadata.getReferringPhysicianEmail();
            addPersonActor(actors,  crossReferences.toActorId, personName, personAddress, personTelephoneNumber, personEmail);
        }
        else{
            ; // No To actor generated.
        }
        /**
         * The From element is derived from the contents of the
         * DICOM Performing Physician Name.
         */
        if (firstMetadata.getPerformingPhysicianName() != null){
            crossReferences.fromActorId = makeId("AA");
            
            From from = aCCR.addNewFrom();
            
            ActorReferenceType[] actorRefs = new ActorReferenceType[1];

            actorRefs[0] = ActorReferenceType.Factory.newInstance();

            actorRefs[0].setActorID(crossReferences.fromActorId);
            CodedDescriptionType actorRole = CodedDescriptionType.Factory.newInstance();
            actorRole.setText("Performing Physician");
            CodedDescriptionType actorRoles[] = new CodedDescriptionType[1];
            actorRoles[0] = actorRole;
            actorRefs[0].setActorRoleArray(actorRoles);
            from.setActorLinkArray(actorRefs);
            String personName = firstMetadata.getPerformingPhysicianName();
            addPersonActor(actors, crossReferences.fromActorId, personName, null, null, null);
        }
        else{
            // Make it be the MedCommons Information System at the site.
            crossReferences.fromActorId = crossReferences.referenceSourceActorId;
            From from = aCCR.addNewFrom();
            
            ActorReferenceType[] actorRefs = new ActorReferenceType[1];

            actorRefs[0] = ActorReferenceType.Factory.newInstance();

            actorRefs[0].setActorID(crossReferences.fromActorId);
            CodedDescriptionType actorRole = CodedDescriptionType.Factory.newInstance();
            actorRole.setText("DICOM Source");
            CodedDescriptionType actorRoles[] = new CodedDescriptionType[1];
            actorRoles[0] = actorRole;
            actorRefs[0].setActorRoleArray(actorRoles);
            
            from.setActorLinkArray(actorRefs);
            
            

        }

        ActorType patientActor = getActorWithID(actors, crossReferences.patientActorId);
        if (patientActor == null){
        	throw new NullPointerException("Patient actor (with id " +  crossReferences.patientActorId + " does not exist in this CCR");
        	 	
        	
        }
        
        PurposeType purposeType = aCCR.addNewPurpose(); // Is this right - just add the purpose on?
        CodedDescriptionType purposeDescription = purposeType.addNewDescription();
        if (firstMetadata.getReasonForStudy() != null)
            purposeDescription.setText(firstMetadata.getReasonForStudy());
        else if (firstMetadata.getStudyDescription() != null)
            purposeDescription.setText(firstMetadata.getStudyDescription());
        else{
        	purposeDescription.setText("DICOM Imaging Study");
        }


       

        boolean valid = aCCR.validate();

        //log.info("Created Ccr\n" + out.toString());
        log.info("Valid CCR:" + valid);
        return(newContinuityOfCareDocument);

    }
    /**
     * Derives all patient demographics from the DICOM headers.
     * 
     * TODO: Note we assume that the DicomMetadata is used here to generate elements in the procedure.
     * It assumed that the first element (e.g., first image) has consistent data with all of the DICOM
     * images. If this is a single study being uploaded this is a very good assumption.
     * 
     * @param StorageId
     * @param dicomMetadata
     * @param transactions
     * @throws IOException
     */
    public ContinuityOfCareRecordDocument createDemographicsFromDICOM(String storageId, List<DicomMetadata> dicomMetadata, List<DicomTransaction> transactions, Properties referenceProper) throws IOException{

    	ContinuityOfCareRecord aCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    

        crossReferences = new CCRCrossReferences();
        Actors actors = aCCR.getActors();
        if (actors == null)
            actors = aCCR.addNewActors();
        DicomMetadata firstMetadata = dicomMetadata.get(0);

        ActorType dicomActor = getInformationSystemActor(actors, firstMetadata.getInstitutionName(), "1.0");
        ActorType medCommonsActor = getInformationSystemActor(actors, "MedCommons", versionNumber);
        if (dicomActor != null){
            crossReferences.referenceSourceActorId = dicomActor.getActorObjectID();
        }
        else{
            crossReferences.referenceSourceActorId = addInformationSystemActor(actors,  firstMetadata, "1.0",null);
        }
        if (medCommonsActor != null){
        	 crossReferences.medCommonsInformationActorId = medCommonsActor.getActorObjectID();
        }
        else{
        	crossReferences.medCommonsInformationActorId =
                addInformationSystemActor(actors, "MedCommons", versionNumber, null);
        }

        Patient[] patientArray  = aCCR.getPatientArray();
        Patient patient = null;
        if ((patientArray != null) &&(patientArray.length >0)){
        	patient = patientArray[0];
        	crossReferences.patientActorId = patient.getActorID();
        }
        else{
        	crossReferences.patientActorId = makeId("PATIENT");
        }

        

        if (patient == null){
        	patient = aCCR.addNewPatient();
        	patient.setActorID(crossReferences.patientActorId);
        }

        Body body = aCCR.getBody();
        if (body == null)
        	body = aCCR.addNewBody();

        Procedures procedures = body.getProcedures();
        if (procedures == null)
        	procedures = body.addNewProcedures();

        dicomProcedure = procedures.addNewProcedure();

        // In the future - maybe test for duplicates.
        dicomProcedure.setCCRDataObjectID(makeId("PROCEDURE"));
        CodedDescriptionType procedureType = dicomProcedure.addNewType();
        procedureType.setText("Radiology");
        CodedDescriptionType procedureDescription = dicomProcedure.addNewDescription();
        String studyDescription = firstMetadata.getStudyDescription();
        if (studyDescription == null){
        	studyDescription = "Study";
        }
        procedureDescription.setText(studyDescription);

        if (firstMetadata.getBodyPart() != null){
            SiteType site = dicomProcedure.addNewSite();
            //CodeType siteCode =  site.addNewCode();
            site.setText(firstMetadata.getBodyPart());
        }



        Locations locations = dicomProcedure.addNewLocations();
        Location location = locations.addNewLocation();
        ActorReferenceType locationActor = location.addNewActor();
        locationActor.setActorID(crossReferences.referenceSourceActorId);

        if (firstMetadata.getAccessionNumber() != null){
            IDType accession = dicomProcedure.addNewIDs();
            ActorReferenceType issuedby = accession.addNewIssuedBy();
            issuedby.setActorID(crossReferences.referenceSourceActorId);
            accession.setID(firstMetadata.getAccessionNumber());
            SourceType accessionSource = accession.addNewSource();
            ActorReferenceType accessionActorSource = accessionSource.addNewActor();
            accessionActorSource.setActorID(crossReferences.referenceSourceActorId);
        }

        CodedDescriptionType procedureStatus = dicomProcedure.addNewStatus();
        procedureStatus.setText("In Progress");

        MethodType method = dicomProcedure.addNewMethod();
        method.setText(firstMetadata.getModality());
        SourceType dicomSource = dicomProcedure.addNewSource();
        ActorReferenceType dicomRef = dicomSource.addNewActor();
        dicomRef.setActorID(crossReferences.referenceSourceActorId);

        // Editing needed below. Need to detect if the physician is there first before adding new actor.
        if (firstMetadata.getReferringPhysicianName() != null){
            crossReferences.toActorId = makeId("AA");
            To to = aCCR.addNewTo();
            ActorReferenceType[] actorRefs = new ActorReferenceType[1];

            actorRefs[0] = ActorReferenceType.Factory.newInstance();

            actorRefs[0].setActorID(crossReferences.toActorId);
            CodedDescriptionType actorRole = CodedDescriptionType.Factory.newInstance();
            actorRole.setText("Referring Physician");
            CodedDescriptionType actorRoles[] = new CodedDescriptionType[1];
            actorRoles[0] = actorRole;
            actorRefs[0].setActorRoleArray(actorRoles);
            to.setActorLinkArray(actorRefs);
            String personName = firstMetadata.getReferringPhysicianName();
            String personAddress = firstMetadata.getReferringPhysicianAddress();
            String personTelephoneNumber = firstMetadata.getReferringPhysicianTelephoneNumber();
            String personEmail = firstMetadata.getReferringPhysicianEmail();
            addPersonActor(actors,  crossReferences.toActorId, personName, personAddress, personTelephoneNumber, personEmail);
        }
        else{
            ; // No To actor generated.
        }
        /**
         * The From element is derived from the contents of the
         * DICOM Performing Physician Name.
         */
        if (firstMetadata.getPerformingPhysicianName() != null){
            crossReferences.fromActorId = makeId("AA");
            
            From from = aCCR.addNewFrom();
            
            ActorReferenceType[] actorRefs = new ActorReferenceType[1];

            actorRefs[0] = ActorReferenceType.Factory.newInstance();

            actorRefs[0].setActorID(crossReferences.fromActorId);
            CodedDescriptionType actorRole = CodedDescriptionType.Factory.newInstance();
            actorRole.setText("Performing Physician");
            CodedDescriptionType actorRoles[] = new CodedDescriptionType[1];
            actorRoles[0] = actorRole;
            actorRefs[0].setActorRoleArray(actorRoles);
            from.setActorLinkArray(actorRefs);
            String personName = firstMetadata.getPerformingPhysicianName();
            addPersonActor(actors, crossReferences.fromActorId, personName, null, null, null);
        }
        else{
            // Make it be the MedCommons Information System at the site.
            crossReferences.fromActorId = crossReferences.referenceSourceActorId;
            From from = aCCR.addNewFrom();
            
            ActorReferenceType[] actorRefs = new ActorReferenceType[1];

            actorRefs[0] = ActorReferenceType.Factory.newInstance();

            actorRefs[0].setActorID(crossReferences.fromActorId);
            CodedDescriptionType actorRole = CodedDescriptionType.Factory.newInstance();
            actorRole.setText("DICOM Source");
            CodedDescriptionType actorRoles[] = new CodedDescriptionType[1];
            actorRoles[0] = actorRole;
            actorRefs[0].setActorRoleArray(actorRoles);
            
            from.setActorLinkArray(actorRefs);
            
            

        }

        filterPatientActor(actors,storageId, firstMetadata);

        PurposeType purposeType = aCCR.addNewPurpose(); // Is this right - just add the purpose on?
        CodedDescriptionType purposeDescription = purposeType.addNewDescription();
        if (firstMetadata.getReasonForStudy() != null)
            purposeDescription.setText(firstMetadata.getReasonForStudy());
        else if (firstMetadata.getStudyDescription() != null)
            purposeDescription.setText(firstMetadata.getStudyDescription());
        else{
        	purposeDescription.setText("DICOM Imaging Study");
        }


       

        boolean valid = aCCR.validate();

        //log.info("Created Ccr\n" + out.toString());
        log.info("Valid CCR:" + valid);
        return(newContinuityOfCareDocument);

    }
    /**
     * If the patient actor is 'blank' - replace it.
     * 
     * @param actors
     * @param storageId
     * @param firstMetadata
     */
    public void filterPatientActor(Actors actors, String storageId, DicomMetadata firstMetadata){
    	ActorType patientActor = getActorWithID(actors, crossReferences.patientActorId);
        if (patientActor == null){
        	log.info("Patient actor is null; adding new one");
        	addPatientActor(actors,crossReferences, storageId, firstMetadata);
        }
        else if (isPatientActorBlank(patientActor)){
        	boolean actorReplaced = false;
        	log.info("Patient actor is blank (has no patient name). Drop and insert new demographics");
        	List<ActorType> actorList = actors.getActorList();
        	for (int i=0;i<actorList.size();i++){
        		ActorType candidateActor = actorList.get(i);
        		if (candidateActor == patientActor){
        			actors.removeActor(i);
        			addPatientActor(actors, crossReferences, storageId, firstMetadata);
        			actorReplaced = true;
        			break;
        		}
        	}
        	if (!actorReplaced){
        		log.error("Didn't replace actor \n" + 
        				patientActor.toString() + "\nin\n" + actors.toString());
        		throw new RuntimeException("Failed to replace blank patient actor");
        		
        	}
        }
        else{
        	log.info("Patient actor already exists:" + patientActor.toString());
        	
        	
        	
        	
        }
    }
    /*
    public void addDICOMReference(DicomTransaction trans, DicomMetadata metadata, ProcedureType procedure, Map<String, String> referenceProperties){
    	ContinuityOfCareRecord aCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
         
    	References references = aCCR.getReferences();
    	addReference(references, crossReferences, trans, metadata, procedure);
      
    }
    */
    public boolean isCCRValid(){
    	ContinuityOfCareRecord aCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    	return(aCCR.validate());
    }
    
    /**
     * Returns true if the actor is 'blank' - which currently means that the patient names
     * are missing. False otherwise.
     * @param patientActor
     * @return
     */
    public boolean isPatientActorBlank(ActorType patientActor){
    	boolean isBlank = false;
    	ActorType.Person person = patientActor.getPerson();
    	Name name = person.getName();
    	if (name==null){
    		isBlank=true;
    	}
    	else{
	    	PersonNameType currentName = name.getCurrentName();
	    	if (currentName == null){
	    		isBlank = true;
	    	}
	    	else{
	    		boolean blankFamily = false;
	    		boolean blankGiven = false;
	    		List <String> familyNames = currentName.getFamilyList();
	    		List <String> givenNames = currentName.getGivenList();
	    		if ((familyNames == null) || (familyNames.size() == 0)){
	    			blankFamily = true;
	    		}
	    		if ((givenNames == null) || (givenNames.size() == 0)){
	    			blankGiven = true;
	    		}
	    		if (blankGiven && blankFamily){
	    			isBlank = true;
	    		}
	    	}
    	}
    	return(isBlank);
    }
    /**
     * Public interface to add a simple document (PDF, JPG, &amp;etc)
     * Reference to a CCR. 
     * 
     * @param storageId
     * @param documentName
     * @param documentSha1
     * @param documentMimeType
     * @param documentBytes
     * @param referenceProperties
     * @return
     * @throws IOException
     */
    public ContinuityOfCareRecordDocument addSimpleDocumentReference(
    		String documentName, 
    		String documentSha1, 
    		String documentMimeType,
    		long documentBytes,
    		 Map<String, String> referenceProperties) throws IOException{
    	ContinuityOfCareRecord aCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    	References references = aCCR.getReferences();
    	if (references == null)
        	references = aCCR.addNewReferences();
    	
    	internalAddSimpleDocumentReference(references, crossReferences,documentName, documentSha1, documentMimeType, documentBytes, referenceProperties);
    	return(newContinuityOfCareDocument);
    }
    
    /**
     * Public interface to adding a DICOM reference to the CCR.
     * 
     * @param dicomTransaction
     * @param dicomMetadata
     * @param referenceProperties
     * @return
     * @throws IOException
     */
    public ContinuityOfCareRecordDocument addDicomReference(
    		DicomTransaction dicomTransaction,
            DicomMetadata dicomMetadata,
    		 Map<String, String> referenceProperties) throws IOException{
    	ContinuityOfCareRecord aCCR = newContinuityOfCareDocument.getContinuityOfCareRecord();
    	References references = aCCR.getReferences();
    	if (references == null)
        	references = aCCR.addNewReferences();
    	
    	internalAddDicomReference(references, crossReferences, dicomTransaction, dicomMetadata, dicomProcedure, referenceProperties);
	
    	return(newContinuityOfCareDocument);
    }
   
    
    /**
     * If the sourceActorId is null then we assume that the
     * information system is the source of itself.

     */
    private String addInformationSystemActor(Actors actors, String name, String version, String sourceActorId){
    	if (name == null){
    		name = UNKNOWN;
    	}
    	if (version == null){
    		version = UNKNOWN;
    	}
        String actorId = makeId("AA");
        
        ActorType actor = actors.addNewActor();
        actor.setActorObjectID(actorId);
        InformationSystem infoSystem = actor.addNewInformationSystem();
        infoSystem.setName(name);
        infoSystem.setVersion(version);
        SourceType source = actor.addNewSource();
        ActorReferenceType actorRef = source.addNewActor();

        if (sourceActorId == null)
            sourceActorId = actorId;
        actorRef.setActorID(sourceActorId);
        return(actorId);
    }
    private ActorType getInformationSystemActor(Actors actors, String name, String version){
        ActorType foundActor = null;
        if (name == null){
        	name = UNKNOWN;
        }
        if (actors != null){
            ActorType[] allActors = actors.getActorArray();
            for (int i=0;i<allActors.length; i++){
                ActorType anActor = allActors[i];
                InformationSystem infoSystem = anActor.getInformationSystem();
                if (infoSystem != null){
                    String iName = infoSystem.getName();
                    String iVersion = infoSystem.getVersion();
                    if ((iName != null) &&(iVersion != null)){
	                    if (iName.equalsIgnoreCase(name) && (iVersion.equalsIgnoreCase(version))){
	                        foundActor = anActor;
	                        break;
	                    }
                    }
                }
            }
        }
        return(foundActor);
    }
    private ActorType getActorWithID(Actors actors, String actorID){
        ActorType foundActor = null;
        if (actors != null){
            ActorType[] allActors = actors.getActorArray();
            for (int i=0;i<allActors.length; i++){
                ActorType anActor = allActors[i];
                String actorObjectID = anActor.getActorObjectID();
                if (actorObjectID.equals(actorID)){
                	foundActor = anActor;
                	break;
                }
            }
        }
        return(foundActor);
    }
    private String addInformationSystemActor(Actors actors, DicomMetadata dicomMetadata, String version, String sourceActorId){
        String actorId = makeId("AA");
        if (sourceActorId == null)
            sourceActorId = actorId;
        ActorType actor = actors.addNewActor();
        actor.setActorObjectID(actorId);
        InformationSystem infoSystem = actor.addNewInformationSystem();
        String informationSystem = dicomMetadata.getInstitutionName();
        if (informationSystem == null){
        	informationSystem = UNKNOWN;
        }
        infoSystem.setName(informationSystem);
        infoSystem.setVersion(version);
        if (dicomMetadata.getInstitutionAddress() != null){
            Address address = actor.addNewAddress();
            address.setLine1(dicomMetadata.getInstitutionAddress());
        }
        IDType calledAE = actor.addNewIDs();
        calledAE.setID(dicomMetadata.getCalledAeTitle());
        CodedDescriptionType calledAEType = calledAE.addNewType();
        calledAEType.setText("CalledAETitle");
        SourceType calledSource = calledAE.addNewSource();
        ActorReferenceType calledActor = calledSource.addNewActor();
        calledActor.setActorID(sourceActorId);

        calledActor = calledAE.addNewIssuedBy();
        calledActor.setActorID(sourceActorId);

        IDType callingAE = actor.addNewIDs();
        callingAE.setID(dicomMetadata.getCallingAeTitle());
        CodedDescriptionType callingAEType = callingAE.addNewType();
        callingAEType.setText("CallingAETitle");

        SourceType callingSource = callingAE.addNewSource();
        ActorReferenceType callingActor = callingSource.addNewActor();
        callingActor.setActorID(sourceActorId);
        callingActor = callingAE.addNewIssuedBy();

        callingActor.setActorID(sourceActorId);

        SourceType source = actor.addNewSource();
        ActorReferenceType actorRef = source.addNewActor();

        actorRef.setActorID(sourceActorId);


        return(actorId);
    }
    private String createReferenceUrl(String guid){
        return("mcid://" + guid);
    }
    long idCounter = 1;
    /**
     * IDs need only be unique within a CCR - but
     * the merge in the appliance sometimes doesn't merge
     * references if the IDs are the same (it assumes that they
     * are identical if the ids are identical). Therefore - we
     * make this a bit more complex.
     * @param prefix
     * @return
     */
    private String makeId(String prefix){
    	long time = System.currentTimeMillis();
    	String n = idCounter++ + "N" + time;
        return(prefix + n);
    }

    private String internalAddDicomReference(References references, CCRCrossReferences crossReferences, DicomTransaction dicomTransaction,
            DicomMetadata dicomMetadata, ProcedureType procedure,
            Map<String, String> referenceProperties){
        String seriesDisplayName = dicomTransaction.getSeriesDescription();

        if ((seriesDisplayName == null) || (seriesDisplayName.equals(""))){
            seriesDisplayName = dicomMetadata.getModality() + " Series " + dicomTransaction.getObjectCount() + " Images";
        }
        log.info("Series name is '" + seriesDisplayName + "'");
        String referenceObjectId = makeId("REF");
        ReferenceType ref = references.addNewReference();
        ActorReferenceType actorSource = ref.addNewSource();
        DateFormat exactDateFormat = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
        exactDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        exactDateFormat.setLenient(true);

        Date seriesDate = dicomMetadata.getSeriesDate();
        if (seriesDate != null){ // If it is null - should we get another date(DateOfSecondaryCapture)?
	        String seriesDateTime = exactDateFormat.format(seriesDate);
	        DateTimeType referenceDateTime = ref.addNewDateTime();
	        referenceDateTime.setExactDateTime(seriesDateTime);
        }
        CodedDescriptionType refDescription = ref.addNewDescription();
        refDescription.setText(seriesDisplayName);
        if (procedure != null){
            procedure.addReferenceID(referenceObjectId);
            //InternalCCRLink internalCCRLink = procedure.addNewInternalCCRLink();
            //internalCCRLink.addLinkRelationship("DICOM Series");
            //internalCCRLink.setLinkID(referenceObjectId);
            Date studyDate = dicomMetadata.getStudyDate();
            String studyDateTime = null;
            if (studyDate != null){
            	studyDateTime = exactDateFormat.format(studyDate);
            	 List<DateTimeType>  procedureDateTime = procedure.getDateTimeList();
                 if ( (procedureDateTime == null) || (procedureDateTime.size() == 0)){
                     DateTimeType d = procedure.addNewDateTime();
                     d.setExactDateTime(studyDateTime);
                 }
            }
        }

        actorSource.setActorID(crossReferences.referenceSourceActorId);
        addNewType(ref, "application/dicom");


        ref.setReferenceObjectID(referenceObjectId);
        Locations locs =  ref.addNewLocations();
        Location loc = locs.addNewLocation();
        CodedDescriptionType locdesc = loc.addNewDescription();
        if (dicomTransaction.getSeriesSha1() == null){
            throw new NullPointerException("Null series sha1 for series " + dicomTransaction.getSeriesDescription()
                    + ", " + dicomTransaction.getSeriesInstanceUid());
        }
        addObjectAttribute(locdesc, "URL", createReferenceUrl(dicomTransaction.getSeriesSha1()));

        addObjectAttribute(locdesc, "DisplayName", seriesDisplayName);
        addObjectAttribute(locdesc, "Size", Long.toString(dicomTransaction.getTotalBytes()));
        addObjectAttribute(locdesc, "SeriesInstanceUID", dicomTransaction.getSeriesInstanceUid());
        addObjectAttribute(locdesc, "StudyInstanceUID", dicomTransaction.getStudyInstanceUid());
        



        addCodedObjectAttribute(locdesc, "Confirmed", "True", "Confirmed", "MedCommons", versionNumber);

        List<DicomPreset> presets = dicomMetadata.getPresetArray();
        if (presets != null){
        	for (int i=0;i<presets.size();i++){
        		DicomPreset preset = presets.get(i);
        		log.info("Preset["+i+"] " + preset.windowCenterWidthExplanation + "," + preset.windowWidth + ", " + preset.windowCenter);
        	}
        }
        addObjectAttribute(locdesc, "PRESETS", presets);
        log.info("About to add referenceProperties = " + referenceProperties);
        if (referenceProperties != null){
        	addObjectAttribute(locdesc, "StorageHandler", referenceProperties);
        	
        	
        }
        return(referenceObjectId);
    }
    
   
    
    private String internalAddSimpleDocumentReference(References references, CCRCrossReferences crossReferences, 
    		//DicomTransaction dicomTransaction, 
    		String documentName,
    		String documentSha1,
    		String documentMimeType,
    		long documentBytes,
    		 Map<String, String> referenceProperties
            ){
        
        log.info("Document name is '" + documentName + "'");
        String referenceObjectId = makeId("REF");
        ReferenceType ref = references.addNewReference();
        ActorReferenceType actorSource = ref.addNewSource();
        DateFormat exactDateFormat = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
        exactDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        exactDateFormat.setLenient(true);

       
        CodedDescriptionType refDescription = ref.addNewDescription();
        refDescription.setText(documentName);
    

        actorSource.setActorID(crossReferences.referenceSourceActorId);
        addNewType(ref, documentMimeType);


        ref.setReferenceObjectID(referenceObjectId);
        Locations locs =  ref.addNewLocations();
        Location loc = locs.addNewLocation();
        CodedDescriptionType locdesc = loc.addNewDescription();
       
        addObjectAttribute(locdesc, "URL", createReferenceUrl(documentSha1));

        addObjectAttribute(locdesc, "DisplayName", documentName);
        addObjectAttribute(locdesc, "Size", Long.toString(documentBytes));
        
        
        


        addCodedObjectAttribute(locdesc, "Confirmed", "True", "Confirmed", "MedCommons",  versionNumber);
        if (referenceProperties != null){
        	addObjectAttribute(locdesc, "StorageHandler", referenceProperties);
        	
        	
        }
        

       
        return(referenceObjectId);
    }
    /**
     *  <ObjectAttribute>
              <Attribute>PRESETS</Attribute>
              <AttributeValue>
                <Value>
                  <ObjectAttribute>
                    <Attribute>BONE</Attribute>
                    <AttributeValue>
                      <Value>1500,300</Value>
                    </AttributeValue>
                  </ObjectAttribute>
                  <ObjectAttribute>
                    <Attribute>ABDOMEN</Attribute>
                    <AttributeValue>
                      <Value>40,350</Value>
                    </AttributeValue>
                  </ObjectAttribute>
                </Value>
              </AttributeValue>
            </ObjectAttribute>
     * @param locdesc
     * @param presets
     */


    private CodedDescriptionType createPresetAttribute(DicomPreset preset){
    	CodedDescriptionType cdt = CodedDescriptionType.Factory.newInstance();
    	ObjectAttribute attr = cdt.addNewObjectAttribute();// ObjectAttribute.Factory.newInstance();

    	attr.setAttribute(preset.windowCenterWidthExplanation);
    	AttributeValue attrValue = attr.addNewAttributeValue();
    	addObValue(attrValue, preset.windowWidth + ","+ preset.windowCenter);

log.info("preset ObjectAttribute:" + cdt.toString());
    	return(cdt);
    }
    private CodedDescriptionType createAttributes(Map<String, String> referenceProperties){
    	CodedDescriptionType cdt = CodedDescriptionType.Factory.newInstance();
    	
    	Set<String> keys =  referenceProperties.keySet();
    	Iterator<String> iter = keys.iterator();
    	while (iter.hasNext()){
    		
    		String key =  iter.next();
    		String value = referenceProperties.get(key);
    		log.info("createAttributes key " + key + " => " + value);
    		ObjectAttribute attr = cdt.addNewObjectAttribute();
    		attr.setAttribute(key);
    		AttributeValue attrValue = attr.addNewAttributeValue();
        	addObValue(attrValue, value);
	
    	}
    	
    	

    	return(cdt);
    }
private void addObValue(AttributeValue attrValue, String value){
	 XmlObject valueObject = attrValue.addNewValue();
     XmlCursor editCursor = valueObject.newCursor();
     editCursor.removeXml();
     editCursor.beginElement(new QName(CCR_NAMESPACE, "Value"));
     editCursor.insertChars(value);
     editCursor.dispose();
}

private ObjectAttribute addObjectAttribute(CodedDescriptionType cdt, String attribute, Map<String, String> referenceProperties){
	log.info("addObjectAttribute:" + referenceProperties);
	if (referenceProperties == null) return null;
    ObjectAttribute objAttribute = cdt.addNewObjectAttribute();
    objAttribute.setAttribute(attribute);
    AttributeValue attrValue = objAttribute.addNewAttributeValue();

    CodedDescriptionType properties = createAttributes(referenceProperties);
    log.info("Properties = " + properties.toString());
    attrValue.setValue(properties);

    log.info("**** Object attributes: " + objAttribute.toString());
    return(objAttribute);


}
    private ObjectAttribute addObjectAttribute(CodedDescriptionType cdt, String attribute, List<DicomPreset> presets){
    	if (presets == null) return null;
        ObjectAttribute objAttribute = cdt.addNewObjectAttribute();
        objAttribute.setAttribute(attribute); // Presets
        AttributeValue attrValue = objAttribute.addNewAttributeValue();


        XmlObject valueObject = attrValue.addNewValue();
        XmlCursor editCursor = valueObject.newCursor();
        editCursor.removeXml();
        editCursor.beginElement(new QName(CCR_NAMESPACE, "Value"));

        
        for (int i=0;i<presets.size();i++){
        	DicomPreset preset = presets.get(i);
        	CodedDescriptionType presetAttribute = createPresetAttribute(preset);
        	XmlCursor presetCursor = presetAttribute.newCursor();
        	presetCursor.toFirstContentToken();
        	

        	presetCursor.copyXml(editCursor);
        	presetCursor.dispose();
        	//objAttributecursor.dispose();

        }


        editCursor.dispose();
        log.info(" Object attributes: " + objAttribute.toString());
        return(objAttribute);


    }
    
    private void addNewType(ReferenceType ref, String text){
        CodedDescriptionType codedType = ref.addNewType();
        codedType.setText(text);
    }

    /**
     * <ObjectAttribute>
     *    <Attribute>DisplayName</Attribute>
     *    <AttributeValue>
     *           <Value>EVT + 2.0 B30f</Value>
     *     </AttributeValue>
     *</ObjectAttribute>
     *
     * @param cdt
     * @param attribute
     * @param value
     * @return
     */
    private ObjectAttribute addObjectAttribute(CodedDescriptionType cdt, String attribute, String value){
        ObjectAttribute objAttribute = cdt.addNewObjectAttribute();
        objAttribute.setAttribute(attribute);
        AttributeValue attrValue = objAttribute.addNewAttributeValue();

        XmlObject valueObject = attrValue.addNewValue();
        XmlCursor editCursor = valueObject.newCursor();
        editCursor.removeXml();
        editCursor.beginElement(new QName(CCR_NAMESPACE, "Value"));
        editCursor.insertChars(value);
        editCursor.dispose();
        return(objAttribute);


    }
    private void addCodedObjectAttribute(CodedDescriptionType cdt, String attribute, String value, String codeValue, String codingSystem, String version){
        ObjectAttribute objAttribute = addObjectAttribute(cdt, attribute, value);


        CodeType codeType= objAttribute.addNewCode();
        codeType.setValue(codeValue);
        codeType.setVersion(version);
        codeType.setCodingSystem(codingSystem);
    }

    class CCRCrossReferences{
        String patientActorId;
        String referenceSourceActorId;
        String medCommonsInformationActorId;
        String toActorId;
        String fromActorId;
        String procedureId;
    }
}
