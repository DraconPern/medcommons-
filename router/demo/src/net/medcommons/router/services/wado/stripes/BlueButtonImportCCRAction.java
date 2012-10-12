/*
 * $Id: ImportCCRAction.java 3655 2010-04-05 23:01:06Z ssadedin $
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.services.interfaces.ServiceConstants.PUBLIC_MEDCOMMONS_ID;
import static net.medcommons.modules.utils.Str.blank;

import java.io.*;
import java.security.NoSuchAlgorithmException;

import net.medcommons.document.ccr.BlueButtonParser;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.sourceforge.stripes.action.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Accepts a file upload of a CCR and imports it into MedCommons.  The
 * file is not saved, but the user is shown a view where they
 * can choose to do that.
 * 
 * @param uploadedFile - a multipart form parameter containing file 
 *                       data to import
 * @author ssadedin */
@UrlBinding("/blue")
public class BlueButtonImportCCRAction extends ImportCCRAction {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BlueButtonImportCCRAction.class);
    
    private LocalFileRepository repo =  
        (LocalFileRepository)RepositoryFactory.getLocalRepository();
    
    private BlueButtonParser converter = new BlueButtonParser();
    
    private File tempFile ;
    
    private CCRDocument resultCCR;
    
    @DefaultHandler 
    @DontValidate
    public Resolution init() throws Exception {
        ctx.setAttribute("initialContentsUrl", "importblue.jsp");
        return new ForwardResolution("/platform.jsp");
    }
    
    public Resolution uploadBlue() throws Exception {
        
        Resolution result = super.upload();
        
        attachBlueButtonFile(PUBLIC_MEDCOMMONS_ID);
        
        return result;
    }

    public void attachBlueButtonFile(String storageId) throws NoSuchAlgorithmException, IOException,
                    ConfigurationException, ServiceException, TransactionException, PHRException {
        // Now attach the file
        if(tempFile == null)
            throw new IllegalStateException("Internal error - required temporary file not set");
        
        if(!tempFile.exists()) 
            throw new IllegalStateException("Internal error - required temporary file not found");

        try {
            String guid = repo.putDocument(storageId, tempFile, CCRConstants.BLUE_BUTTON_MIME_TYPE);
            
            session.getServicesFactory().getDocumentService().addDocument(storageId,guid);
            
            // Create the series
            MCSeries series = resultCCR.createReferenceSeries(
                                uploadedFile.getFileName(),
                                resultCCR.getStorageId(), 
                                guid, 
                                CCRConstants.BLUE_BUTTON_MIME_TYPE); 
            series.setInSession(true);
            resultCCR.setGuid(null);
            // Update the XML with the new document reference
            log.info("Adding reference to guid " + guid + " to CCR");
            resultCCR.addReference(series);
            resultCCR.addConfirmationRequiredFlag(series);
        }
        finally {
            if(!tempFile.delete()) {
                log.warn("Unable to delete temporary file: " +tempFile +" scheduling for delete on exit");
                tempFile.deleteOnExit();
            }
        }
    }

    @Override
    protected CCRDocument extractCCR() throws CCRImportException {
        return extractCCR(ServiceConstants.PUBLIC_MEDCOMMONS_ID);
    }
    
    public CCRDocument extractCCR(String patientId) throws CCRImportException {
        
        try {
            resultCCR = CCRDocument.createFromTemplate(patientId);
            resultCCR.setCreateTimeMs(System.currentTimeMillis());
            
            File dir = repo.getScratchDirectory();
            tempFile = new File(dir, SHA1.sha1(String.valueOf(System.currentTimeMillis()+Math.random())));
            if(tempFile.exists())
                throw new RuntimeException("Internal file name collision: please retry your operation");
                
            InputStream is = null;
            try {
                uploadedFile.save(tempFile);
                converter.extractFromStream(new FileInputStream(tempFile));
            }
            finally {
                IOUtils.closeQuietly(is);
            }
            
            resultCCR.getJDOMDocument().setValue("patientGivenName", converter.getFirstName());
            resultCCR.getJDOMDocument().setValue("patientFamilyName", converter.getLastName());
            resultCCR.setPatientDateOfBirth(converter.getDateOfBirthValue());
            
            if(!blank(converter.getSex())) 
                resultCCR.getJDOMDocument().setValue("patientGender", converter.getSex());
            
            return resultCCR;
        }
        catch(Exception e) {
            log.error("Unable to import blue button file", e);
            throw new CCRImportException("medcommons.invalidBlueButtonFile", e);
        }
    }

    public CCRDocument getResultCCR() {
        return this.resultCCR;
    }
}