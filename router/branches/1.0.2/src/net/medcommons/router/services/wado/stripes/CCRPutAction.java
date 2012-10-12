/*
 * $Id$
 */
 package net.medcommons.router.services.wado.stripes;

import static net.medcommons.document.ccr.CCRConstants.SCHEMA_VALIDATION_STRICT;
import static net.medcommons.modules.utils.DocumentTypes.DICOM_MIME_TYPE;
import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.bvl;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.WordUtils.capitalizeFully;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.medcommons.Version;
import net.medcommons.modules.publicapi.utils.AccountDocumentParameters;
import net.medcommons.modules.publicapi.utils.UploadContentHandler;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.modules.services.interfaces.DicomMetadata.DicomPreset;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.modules.utils.dicom.DicomNameParser;
import net.medcommons.modules.utils.dicom.DicomNameParser.DicomName;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRReferenceElement;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.ccrmerge.MergeException;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.json.JSONObject;

/**
 * Takes a CCR as input and merges it's data with a specified 
 * patient's Current CCR.
 * 
 * @author ssadedin
 */
@UrlBinding("/put/{storageId}")
public class CCRPutAction extends BaseActionBean implements ValidationErrorHandler {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CCRPutAction.class);

    /**
     * XML of CCR to merge into patient's account
     */
    @Validate(required=false)
    String ccr;
    
    /**
     * Type of CCR to add content to (used for image uploads)
     */
    @Validate(required=false, mask="[A-z 0-9]{1,120}")
    String documentType;
    
    /**
     * Base 64 encoded image data - used when image data
     * sent as POST data
     */
    @Validate(required=false)
    String image;
    
    /**
     * Uploaded file - used when image data sent using
     * multipart file upload
     */
    @Validate(required=false)
    FileBean imageFile;
    
    /**
     * Account of patient
     */
    @Validate(required=true)
    String storageId;
    
    /**
     * Authentication token
     */
    @Validate(required=true, mask=GUID_PATTERN)
    String auth;
    
    /**
     * Description of update (Optional)
     */
    String description;
    
    /**
     * If true, and the imageFile input is provided then the file
     * will be unzipped
     */
    boolean gzip = false;
    
    @ValidationMethod
    public void checkContentToBeStored(ValidationErrors e) { 
        if(blank(ccr) && blank(image) && blank(status) && imageFile == null) {
            log.info("Failed validation");
            e.add("ccr", new SimpleError("One of 'ccr' or 'image' or 'status' or 'imageFile' parameters must be provided"));
        }
    }
    
    @After(stages={LifecycleStage.RequestComplete})
    public void terminateSession() {
        ctx.getRequest().getSession().invalidate();
    }
    
    @DefaultHandler
    public Resolution put() {
        
        JSONObject result = new JSONObject();
        try { 
            
            if(!blank(ccr))
                putCCR();
            else
            if(imageFile != null)
                putImageFile();
            else
            if(!blank(image))
                putImage();
            else
                throw new IllegalArgumentException("Must specify one of image or ccr");
            
            result.put("status","ok");
        }
        catch(Exception e) {
            log.error("Failed to put CCR or image data to patient account " + storageId, e);
            result.put("status", "failed");
            String message = e.getMessage();
            if(blank(message))
                message = e.toString();
            result.put("error", message);
        }
        return new StreamingResolution("text/plain", result.toString());
    }
    
    
    /**
     * Put the status of the Patient, updating it in their Current CCR
     */
    String status;
    
    public Resolution status() {
        JSONObject result = new JSONObject();
        try { 
            
            log.info("Setting status to " + status + " for patient " + storageId);
            
            AccountSettings settings = session.getAccountSettings(storageId);
            if(blank(settings.getCurrentCcrGuid()))
                throw new IllegalArgumentException("Storage account " + storageId + " does not have a Current CCR");
            
            CCRDocument imageCCR = resolveImageCCR();
            if(imageCCR == null)
                throw new IllegalArgumentException("Unable to access CCR " + settings.getCurrentCcrGuid() + " for account " + storageId);
            
            CCRActorElement patient = imageCCR.getPatientActor();
            patient.createPath("Status/Text", capitalizeFully(status));
            patient.createPath("Status/Code/Value", status.toUpperCase());
            
            // Finalize the size of all references
            finalizeReferenceSizes(imageCCR);
            
            this.description = session.getMessage("ccr.statusChanged", status);
                
            saveCCR(imageCCR);
            result.put("status", "ok");
        }
        catch(Exception e) {
            log.error("Failed to update status for patient account " + storageId, e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        return new StreamingResolution("text/plain", result.toString());
    }

    @SuppressWarnings("unchecked")
    private void finalizeReferenceSizes(CCRDocument ccr) throws PHRException, RepositoryException {
        CCRElement refs = ccr.getReferences();
        if(refs == null) {
            log.warn("No references found in CCR " + ccr.getGuid() + " :  cannot finalize reference size");
            return;
        }
        
        for (CCRElement ref : (List<CCRElement>)refs.getChildren()) {
             finalizeReferenceSize((CCRReferenceElement) ref);   
        }
    }

    private void finalizeReferenceSize(CCRReferenceElement ref) throws PHRException, RepositoryException {
        if(ref.getSize()>0)
            return; // Already has size set
    
        // No size - query it from the repository and add it as a property
        LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();
        long size = repo.getContentLength(storageId, ref.getGuid());
        ref.addAttribute("Size", String.valueOf(size), "Size", Version.getVersionString());
    }
    
    /**
     * Put the image stored in imageFile (stripes file upload) into repository
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    private void putImageFile() throws Exception {
        
        assert imageFile != null;
        
        File scratchFile  = getScratchImageFile();
        
        if(gzip) {
            log.info("Input file is gzipped");
            InputStream in = new GZIPInputStream(imageFile.getInputStream());
            OutputStream out = null;
            try {
                out = new FileOutputStream(scratchFile);
                IOUtils.copyLarge(in,out);
            }
            finally {
                closeQuietly(in);
                closeQuietly(out);
            }
            log.info("Unzipped image file:  input length = " + imageFile.getSize() + " output length = " + scratchFile.length() 
                     + " ratio = " + ((float)imageFile.getSize()) / ((float)scratchFile.length()+1));
            imageFile.delete();
        }
        else
            imageFile.save(scratchFile); // Note stripes only does a move, not a copy
        
        try {
            importImageFile(scratchFile, imageFile.getContentType());
        }
        finally {
            scratchFile.delete();
        }
    }

    /**
     * Parses the image parameter as base 64 encoded bytes for a DICOM image
     * and attempts to attach to the user's Current CCR as a series, if the
     * image is not already present.
     * @throws IOException 
     * @throws ServiceException 
     */
    private void putImage() throws Exception {
        log.info("Attaching image from " + image.length() + " base 64 encoded chars");
        File imgFile = decodeImage();
        try {
            importImageFile(imgFile, DocumentTypes.DICOM_MIME_TYPE);
        }
        finally {
            imgFile.delete();
        }
    }

    private void importImageFile(File imgFile, String contentType) throws Exception {
        
        if(blank(contentType) || contentType.startsWith("application/octet-stream"))
            contentType = DocumentTypes.DICOM_MIME_TYPE;
        
        if(contentType.indexOf(';')>=0) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        } 
        
        // Parse it and get series information
        UploadContentHandler importer = new UploadContentHandler(session.getAuthenticationToken());
        AccountDocumentParameters params = 
            new AccountDocumentParameters(storageId, session.getOwnerMedCommonsId(), auth);
        DocumentDescriptor descr = 
            importer.importFile(params, SupportedDocuments.getDocumentType(contentType), imgFile);
        
        if(!DocumentDescriptor.UNINITIALIZED.equals(descr.getTransactionHandle()))
	        importer.closeTransaction(params, descr.getTransactionHandle());
        
        log.info("Stored image with descriptor " + descr.toShortString());
        
        CCRDocument ccr = resolveImageCCR();
        
        // We only need to add the reference if it doesn't exist already
        if(ccr.findReference(descr.getGuid()) == null) { 
            
            String displayName = "";
            DicomMetadata metaData = null;
            if(DICOM_MIME_TYPE.equals(contentType)) {
		        // A bit inefficient, but we load the meta data to get the series name
		        LocalFileRepository lfr = (LocalFileRepository) RepositoryFactory.getLocalRepository();
		        metaData = lfr.loadMetadata(storageId, descr.getGuid());
		        
	            // If patient name in CCR is blank then we set it from the DICOM
	            if(blank(ccr.getPatientGivenName()) && blank(ccr.getPatientFamilyName())) {
	                setPatientDemographics(ccr, imgFile);
	            }
	            
	            displayName = metaData.getDisplayName();
            }
            else {
                // TODO: this is actually gettable from the multipart headers
                displayName = "Uploaded File";
            }
            
            MCSeries series = 
                ccr.createReferenceSeries(displayName, storageId, descr.getGuid(), contentType);
            CCRReferenceElement ref = ccr.addReference(series);
            
            if(metaData != null) {
                // Set dicom presets, if we can
                List<DicomPreset> presets = metaData.getPresetArray();
                if(presets != null) {
	                log.info("Found " + presets.size() + " presets for DICOM " + metaData.getGuid());
	                for(DicomPreset preset : presets) {
	                    ref.addDICOMPreset(preset.windowCenter, preset.windowWidth, preset.windowCenterWidthExplanation);
	                }
                }
            }
            
            // Save the Current CCR
            StoreTransaction tx = session.tx(ccr);
            tx.registerDocument(null);
            tx.storeDocument();
            tx.notifyRegistry();
            tx.writeActivity(ActivityEventType.PHR_UPDATE, bvl(description, "Update via API Call"));
            
            log.info("Stored new CCR for image guid: " + tx.getDocumentGuid());
            if(log.isDebugEnabled()) 
                log.debug("New CCR Content: \n" + ccr.toString());
        }
    }

    /**
     * Try and resolve the CCR to add DICOM to from the input parameters.
     * If a specific guid was specified, load that.   Otherwise, load Current CCR
     * of patient.  If patient has no Current CCR, create a blank, default CCR.
     * @throws CCROperationException
     */
    private CCRDocument resolveImageCCR() throws CCROperationException {
        try {
            CCRDocument ccr = null;
            AccountDocumentType type = AccountDocumentType.CURRENTCCR;
            if(!blank(documentType)) 
                type = AccountDocumentType.valueOf(documentType);
                
            log.info("Adding image content to document type " + type);
            
            // By default, import into Current CCR
            AccountSettings settings = session.getAccountSettings(storageId);
            String guid = settings.getAccountDocuments().get(type);
            if(blank(guid)) 
                ccr = CCRDocument.createFromTemplate(storageId);
            else 
                ccr = session.resolve(guid);
            
            assert ccr != null : "CCR should have been resolved for DICOM reference";
            
            return ccr;
       } 
       catch (Exception e) {
           throw new CCROperationException("Unable to resolve CCR for DICOM put operation", e);
       }
    }


    /**
     * Read the patient name and sex from the given DICOM metadata
     * and set them on the CCR.
     * 
     * @param ccr
     * @param metaData
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    private void setPatientDemographics(CCRDocument ccr, File f) throws PHRException, InvalidCCRException, FileNotFoundException, IOException {
        
        log.info("setting patient demographics from metadata of incoming image (name = " + f.getAbsolutePath() + ")");
        
        DicomInputStream in = new DicomInputStream(new FileInputStream(f));
        in.setHandler(new StopTagInputHandler(Tag.PixelData));
        DicomObject dcmobj =  in.readDicomObject();
        
        String patientName = dcmobj.getString(Tag.PatientName);
        // .trim() very important here to remove trailing unicode characters
        patientName = new String(patientName.getBytes("8859_1"), "utf-8").trim();
        
        XMLPHRDocument dom = ccr.getJDOMDocument();
        
        DicomName name = DicomNameParser.parse(patientName);
        dom.setValue("patientGivenName", name.given);
        dom.setValue("patientFamilyName", name.family);
        dom.setValue("patientGender", DicomNameParser.parseSex(dcmobj.getString(Tag.PatientSex)));
        
        String dob = dcmobj.getString(Tag.PatientBirthDate);
        if(!blank(dob)) {
            ccr.setPatientDateOfBirth(dob);
        }
        in.close();
    }

    /**
     * Decode the base64 encoded image data in the 'image' 
     * attribute and write to a temporary file, returning the file
     * handle.
     * 
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private File decodeImage() throws UnsupportedEncodingException, IOException {
        
        log.info("decoding " + image.length() + " base64 chars");
        
        byte[] imgBytes = Base64.decodeBase64(image.getBytes("ASCII"));
        
        // Save the image
        File imgFile = getScratchImageFile();
        log.info("Saving image data to file " + imgFile);
        FileUtils.writeByteArrayToFile(imgFile, imgBytes);
        return imgFile;
    }

    /**
     * Generate a unique file name to hold temporary data for a file to
     * be imported.
     * @throws IOException 
     */
    private File getScratchImageFile() throws IOException {
        while(true) {
            long rand = Math.round(Math.random()*1000000);
            File storageFolder = new File("data/Repository/"+storageId);
            if(!storageFolder.exists())
                if(!storageFolder.mkdirs())
                    throw new IOException("Unable to make folder for scratch file for patient " + storageId);
            
            File f = new File(storageFolder, System.currentTimeMillis() + "_" + rand + ".scratch");
            if(!f.exists())
                return f;
        }
    }

    private void putCCR() throws Exception {
        // Save the CCR
        CCRDocument inboundCCR = new CCRDocument(storageId, ccr, SCHEMA_VALIDATION_STRICT);
        inboundCCR.setLogicalType(AccountDocumentType.CURRENTCCR);
        saveCCR(inboundCCR);
    }

    private void saveCCR(CCRDocument ccrToSave) throws CCRStoreException, ServiceException, MergeException,
            PHRException {
        StoreTransaction tx = session.tx(ccrToSave);
        log.info("Saving CCR " + ccrToSave.toString());
        tx.registerDocument(null);
        tx.storeDocument();
        CCRDocument merged = tx.merge();
        // log.info("Merged CCR = " + merged.toString()); 
        tx.notifyRegistry();
        tx.writeActivity(ActivityEventType.PHR_UPDATE, bvl(description, "Update via API Call"));
    }
    
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        JSONObject result = new JSONObject();
        result.put("status", "failed");
        result.put("error", "invalid input for field " + errors.keySet().iterator().next());
        return new StreamingResolution("text/plain", result.toString());
    }

    public String getCcr() {
        return ccr;
    }

    public void setCcr(String ccr) {
        this.ccr = ccr;
    }


    public String getAuth() {
        return auth;
    }


    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FileBean getImageFile() {
        return imageFile;
    }

    public void setImageFile(FileBean imageFile) {
        this.imageFile = imageFile;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
