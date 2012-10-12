package net.medcommons.application.dicomclient;

import static net.medcommons.application.dicomclient.utils.Params.where;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.application.dicomclient.transactions.PatientMatch;
import net.medcommons.application.dicomclient.utils.CxpTransaction;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DDLTypes;
import net.medcommons.application.dicomclient.utils.DicomTransaction;
import net.medcommons.application.dicomclient.utils.PixDemographicData;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.utils.dicom.DicomNameParser;
import net.sourceforge.pbeans.Store;

public class PixDataExtractor {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(PixDataExtractor.class);

    /**
     * Try and correlate the patient to existing known data.
     */
    public PixDemographicData extractPixData(DicomMetadata dicomMetadata,  ContextState contextState) {
        return this.extractPixData(null, dicomMetadata, contextState);
    }
    
    /**
     * Try and correlate the patient to existing known data.
     */
    public PixDemographicData extractPixData(CxpTransaction cxpTransaction, DicomMetadata dicomMetadata,  ContextState contextState) {
        PixDemographicData pixDemographicData = null;
        String storageId;
        String institution = dicomMetadata.getInstitutionName();
        String patientId = dicomMetadata.getPatientId();
        PixIdentifierData pixIdentifier = null;
        Store db = DB.get();
        if ((institution != null) && (patientId != null)) {
            pixIdentifier = PatientMatch.getIdentifier(dicomMetadata
                    .getPatientId(), dicomMetadata.getInstitutionName());

            if (pixIdentifier == null) {
                log.info("Creating new PixIdentifierData for "
                        + dicomMetadata.getInstitutionName() + "," + dicomMetadata.getPatientId());
                pixIdentifier = new PixIdentifierData();
                pixIdentifier.setAffinityDomain(dicomMetadata
                        .getInstitutionName().toUpperCase());

                pixIdentifier.setAffinityIdentifier(dicomMetadata
                        .getPatientId().toUpperCase());
                pixIdentifier.setCreationDate(new Date());
                DicomNameParser dicomNameParser = new DicomNameParser();
                String givenName = dicomNameParser.givenName(dicomMetadata
                        .getPatientName());
                String familyName = dicomNameParser
                        .familyName(dicomMetadata.getPatientName());
                String middleName = null;
                List<PixDemographicData> matchingPatients = PatientMatch
                        .getPatient(givenName, middleName, familyName);
                if (matchingPatients != null) {
                    if (matchingPatients.size() == 1) {
                        // Add this DICOM identifier for future matches
                        pixIdentifier
                                .setPixDemographicDataId(matchingPatients
                                        .get(0).getId());
                        //pixIdentifier = TransactionUtils
                        //      .saveTransaction(pixIdentifier);
                    } else {
                        String message = "Ambiguity - there are multiple ("
                                + matchingPatients.size()
                                + ") with name "
                                + familyName
                                + ","
                                + givenName
                                + " and DICOM identifier "
                                + dicomMetadata.getPatientId()
                                + " does not exist in table"
                                + "\n Will continue and make new MedCommons account";
                        log.error(message);
                        StatusDisplayManager.get()
                                .setErrorMessage("Matching ambiguity",
                                        message,
                                        cxpTransaction.getDashboardStatusId());
                    }
                } else {
                    pixDemographicData = new PixDemographicData();
                    pixDemographicData.setFamilyName(familyName);
                    pixDemographicData.setGivenName(givenName);
                    String sex = dicomMetadata.getPatientSex();
                    if ((sex != null) && (!sex.equals(""))) {
                        String gender = null;
                        if (sex.equalsIgnoreCase("F")) {
                            gender = "Female";
                        } else if (sex.equalsIgnoreCase("M")) {
                            gender = "Male";
                        }
                        if (gender != null) {
                            pixDemographicData.setGender(gender);
                        }
                    }
                    pixDemographicData.setDob(dicomMetadata
                            .getPatientDateOfBirth()); 
                    
                    db.save(pixDemographicData);
                    pixIdentifier
                            .setPixDemographicDataId(pixDemographicData
                                    .getId());
                    pixIdentifier.setContextStateId(contextState.getId());
                    db.save(pixIdentifier);
                    log.info("Saved new PixIdentifier" + pixIdentifier.getAffinityDomain() +"," + pixIdentifier.getAffinityIdentifier());
                }

            } else {
                pixDemographicData = PatientMatch.getPatient(pixIdentifier
                        .getAffinityDomain(), pixIdentifier
                        .getAffinityIdentifier());

                if (pixDemographicData == null) {
                    String message = "Internal inconsistency: database has reference to id with affinityDomain="
                            + pixIdentifier.getAffinityDomain()
                            + ", affinityIdentifier="
                            + pixIdentifier.getAffinityIdentifier()
                            + "\nWill upload into new account";
                    log.error(message);
                    StatusDisplayManager.get()
                            .setErrorMessage("Internal inconsistency",
                                    message,
                                    cxpTransaction.getDashboardStatusId());
                } else {
                    PixIdentifierData medcommonsIdentifierData = 
                        PatientMatch.getIdentifier(pixDemographicData.getId(), DDLTypes.MEDCOMMONS_AFFINITY_DOMAIN);
                    if (medcommonsIdentifierData != null) {
                        storageId = medcommonsIdentifierData .getAffinityIdentifier();
                        log.info("Obtained storageId from PixIdentiferData:" + storageId);
                    } else {
                        String message = "Internal inconsistency: patient has no medcommons id="
                                + pixDemographicData.getFamilyName()
                                + ", "
                                + pixDemographicData.getGender()
                                + ", internal database id ="
                                + pixDemographicData.getId()
                                + "\nWill upload into new account";
                        log.error(message);
                        StatusDisplayManager.get()
                                .setErrorMessage("Internal inconsistency",
                                        message,
                                        cxpTransaction.getDashboardStatusId());
                    }
                }
            }
        }
        return pixDemographicData;
    }
    

    
    /**
     * Index the patient data from the given {@link CxpTransaction} and {@link ContextState},
     * assuming that the storage id of the context state is the MedCommons ID of the patient
     * to be associated with the data.
     * 
     * @param cxpTransaction
     * @param uploadContextState
     */
    public void createPixData(CxpTransaction cxpTransaction, ContextState uploadContextState) {
        
        String patientMcId = uploadContextState.getStorageId();
        
        Store db = DB.get();
        
        // Get the metadata
        DicomMetadata dicomMetadata =
            db.selectSingle(DicomMetadata.class, where("studyInstanceUid", cxpTransaction.getStudyInstanceUid()));
        
        PixDemographicData pixDemoData = this.extractPixData(cxpTransaction, dicomMetadata, uploadContextState);
        
        if(pixDemoData != null) {
            db.save(pixDemoData);
        
            PixIdentifierData mcId = new PixIdentifierData();
            mcId.setAffinityDomain(UploadHandler.MEDCOMMONS_AFFINITY_DOMAIN);
            mcId.setAffinityIdentifier(patientMcId);
            mcId.setContextStateId(uploadContextState.getId());
            mcId.setPixDemographicDataId(pixDemoData.getId());
            db.save(mcId);
        }
    }

    public void createPixData(DicomTransaction tx, ContextState uploadCtx) {
        
        String patientMcId = uploadCtx.getStorageId();
        
        Store db = DB.get();

        DicomMetadata dicomMetadata =
            db.selectSingle(DicomMetadata.class, where("studyInstanceUid", tx.getStudyInstanceUid()));
        
        PixDemographicData pixDemoData = this.extractPixData(dicomMetadata, uploadCtx);
        if(pixDemoData != null) {
            db.save(pixDemoData);
        
            PixIdentifierData mcId = new PixIdentifierData();
            mcId.setAffinityDomain(UploadHandler.MEDCOMMONS_AFFINITY_DOMAIN);
            mcId.setAffinityIdentifier(patientMcId);
            mcId.setContextStateId(uploadCtx.getId());
            mcId.setPixDemographicDataId(pixDemoData.getId());
            db.save(mcId);
        }
    }
}
