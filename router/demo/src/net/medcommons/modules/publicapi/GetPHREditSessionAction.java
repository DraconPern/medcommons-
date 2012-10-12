package net.medcommons.modules.publicapi;

/**
 * Returns a reference to a CCR editing session; typically used for 
 * by third party applications to GET a CCR and then PUT back changes.
 * 
 * Need to work out authentication.
 * TODO: Special case for 'currentCCR'?
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.crypto.Utils;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.ccr.StorageModel;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.jdom.Document;

@UrlBinding("/getPHREditSession")
public class GetPHREditSessionAction extends BasePublicAPIAction {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger("GetPHRReferenceAction");

    /**
     * Parameter used by tests - setting this to true will cause the action to
     * return with content type = 'plain/text' instead of the preconfigured
     * HealthFrame content type. This stops healthframe from opening the
     * returned blob if all you want to do is look at it for test / development
     * purposes.
     */
    private boolean plainContentType = false;

    /**
     * The target document type (if any) to which the merge should be performed
     */
    private AccountDocumentType defaultMergeTarget = AccountDocumentType.CURRENTCCR;
    /**
     * Creates the transaction state.
     * 
     * @param docDescriptor
     * @param auth
     * @return
     */
    private PHRTransaction createTransaction(DocumentDescriptor docDescriptor,
            String auth) {

        PHRTransaction phrTransaction = new PHRTransaction(docDescriptor);
        phrTransaction.setOriginalReference(getReference());
        phrTransaction.setSenderId(getSenderId());
        phrTransaction.setOriginalAuthToken(auth);
        phrTransaction.setOriginalURI(getOriginalURI());
        phrTransaction.setDescription("PHR Edit");
        String healthURL = getServerRoot() + "/" + docDescriptor.getStorageId();
        phrTransaction.setHealthURL(healthURL);
        SHA1 sha1 = new SHA1();
        String token = null;
        sha1.initializeHashStreamCalculation();
        String authInput = auth;
        if (authInput == null)
            authInput = "";
        String hashInput = phrTransaction.getTimeCreated() + authInput
                + getReference();
        String s[] = new String[1];
        s[0] = hashInput;
        byte[] hashvalue = sha1.calculateHash(s);
        token = Utils.encodeBytes(hashvalue, 0, hashvalue.length);
        phrTransaction.setToken(token);

        return phrTransaction;
    }

    /*
     * 
     */
    @DefaultHandler
    public Resolution getPHRReference() {

        String contentType = plainContentType ? "text/plain"
                : healthBookContentType;

        String description = "StorageId= " + getStorageId() + ", reference="
                + getReference() + ", contentType = " + healthBookContentType;
        BufferedWriter logBuffer = null;
        if (isAuthorized()) {
            try {
                String guid = calculateGuid(getStorageId(), getReference());

                description = "StorageId= " + getStorageId() + ", guid= "
                        + guid + ", reference=" + getReference()
                        + ", contentType = " + contentType;

                // Should get content type from repository.
                DocumentDescriptor doc = createDocumentDescriptor(
                        getStorageId(), guid, contentType);
                PHRTransaction phrTransaction = createTransaction(doc,
                        getAuth());
                phrTransaction.setCalculatedGuid(guid);
                getAccountInformation(phrTransaction);
                Properties props = repository.getMetadata(doc);
                doc.setContentType(props
                        .getProperty(RepositoryFileProperties.CONTENT_TYPE));
                phrTransaction.setContentType(doc.getContentType());

                Document xmlDoc = null;
                xmlDoc = getSchemaResponse().createSchemaReference(
                        remoteAccessAddress, phrTransaction);
                phrTransaction.setTransactionState(TransactionState.NEW);

                String sDoc = Str.toString(xmlDoc);
                BasePublicAPIAction.writeTransationObject(phrTransaction);
                logBuffer = BasePublicAPIAction
                        .getTransactionLogBuffer(phrTransaction);
                BasePublicAPIAction.writeLog(logBuffer, "getPHRReference");
                log.info(getHttpRequestInfo());
                BasePublicAPIAction.writeLog(logBuffer, getHttpRequestInfo());
                BasePublicAPIAction.writeLog(logBuffer, sDoc);
              
                //UserSession d = UserSession.get(getHttpRequest()); 
                UserSession d = UserSession.get(getHttpRequest(), phrTransaction.getStorageId(), phrTransaction.getOriginalAuthToken());
                ensureCurrentCCRExists(d, phrTransaction);
                log.info("getPHRReference: \n" + sDoc);
                // addResponseHeader("content-disposition", "filename=" +
                // getGuid() + "");

                // perform schema validation before returning
                boolean isValid = validateResponse(xmlDoc);
                if (isValid) {
                    BasePublicAPIAction
                            .writeLog(logBuffer, "response is valid");
                }

                return new StreamingResolution(contentType, new StringReader(
                        sDoc));
            } catch (IOException e) {
                BasePublicAPIAction.writeLog(logBuffer, e);
                // ssadedin: don't quite understand why IOException means
                // unknown medcommons account
                // sean - typically a 'file not found' is thrown when looking
                // for an account.
                // This should be changed to a typed error.
                return generateErrorResolution(500,
                        "Unknown MedCommons Account", e);
            } catch (ServiceException e) {
                BasePublicAPIAction.writeLog(logBuffer, e);
                return generateErrorResolution(501, description, e);
            } catch (PHRTransactionException e) {
                BasePublicAPIAction.writeLog(logBuffer, e);
                return generateErrorResolution(502, description, e);
            } catch(Exception e){
                
                BasePublicAPIAction.writeLog(logBuffer, e);
                return generateErrorResolution(503, description, e);
            }
            finally {
                if (logBuffer != null) {
                    try {
                        logBuffer.close();
                    } catch (Exception e) {
                        ;
                    }
                }
            }
        } else {

            return (generateAuthenticationFailureResolution(403,
                    "Authorization failed"));
        }

    }
    
    /*
     * If a CurrentCCR does not exist for a patient; make the requested CCR the CurrentCCR.
     */
    private void ensureCurrentCCRExists(UserSession d, PHRTransaction phrTransaction) throws PHRException, ServiceException, ConfigurationException, RepositoryException{
        if (phrTransaction == null) throw new NullPointerException("phrTransaction");
        
        log.info("phrTransaction = " + phrTransaction);
        log.info("phrTransaction.getOriginalReference() = " + phrTransaction.getOriginalReference());
        log.info("phrTransaction.getOriginalGuid() = " + phrTransaction.getOriginalGuid());
        log.info("phrTransaction.getStorageId() = " + phrTransaction.getStorageId());
        //AccountDocumentType type = AccountDocumentType.valueOf(phrTransaction.getOriginalGuid());
        
        log.info("UserSession getOwnerMedCommonsId=" + d.getOwnerMedCommonsId());
        if (d.getOwnerMedCommonsId() == null){
            throw new NullPointerException("user session has null OwnerMedCommonsId");
        }
        CCRDocument ccr = null;
        try{
            
            ccr = d.resolve(d.getAccountSettings(phrTransaction.getStorageId()).getAccountDocuments().get(defaultMergeTarget));
        }
        catch(Exception e){
            log.error("Error attempting to resolve current CCR for account");
        }
        if (ccr == null){
            log.info("No Current CCR for patient; making the CCR with guid " + 
                    phrTransaction.getOriginalGuid() + " the current CCR");
            ccr = d.resolve(phrTransaction.getOriginalGuid());
            ccr.setLogicalType(defaultMergeTarget);
            ccr.setStorageMode(StorageMode.LOGICAL);

            StorageModel storageModel = Configuration.getBean("systemStorageModel");
            storageModel.replaceCurrentCCR(phrTransaction.getStorageId(), ccr);
        }
        
    }

    private boolean validateResponse(Document xmlDoc)
            throws PHRTransactionException {
        try {
            if (!this.getSchemaResponse().isValidMessage(xmlDoc))
                throw new PHRTransactionException(
                        "Output document not valid according to schema");
            return (true);
        } catch (Exception e) {
            throw new PHRTransactionException(
                    "Failed to validate output document", e);
        }
    }

    public boolean getPlainContentType() {
        return plainContentType;
    }

    public void setPlainContentType(boolean plainContentType) {
        this.plainContentType = plainContentType;
    }

    public static Logger getLog() {
        return log;
    }
}
