package net.medcommons.modules.publicapi.utils;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.cxp.server.RepositoryMetadataHandler;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.repository.GatewayRepository;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.log4j.Logger;

public class UploadContentHandler {
    private static Logger log = Logger.getLogger(UploadContentHandler.class);
    private static String nodeId = null;
    private static boolean encryptionEnabled = false;
    private static boolean backupEnabled = false;
    
    private GatewayRepository repository;
   
    
    public UploadContentHandler(String auth) throws ServiceException{
        super();
        if(nodeId == null) {
            init();
        }
        repository = new GatewayRepository(auth, nodeId, encryptionEnabled, backupEnabled);
    }
    
    private static void init(){
        String path = "conf/config.xml";
        String propertiesPath = "conf/MedCommonsBootParameters.properties";
        try{
            Configuration.load(path, propertiesPath);
            
            nodeId = Configuration.getProperty("NodeID");
            
            String encryptionConfig = Configuration.getProperty("EncryptionEnabled");
            String backupConfig = Configuration.getProperty("Backup_Documents");
            
            if ((encryptionConfig != null) && (!"".equals(encryptionConfig))) {
                encryptionEnabled = Boolean.parseBoolean(encryptionConfig);

            }
            if ((backupConfig != null) && (!"".equals(backupConfig))) {
                backupEnabled = Boolean.parseBoolean(backupConfig);

            }
            
        }
        catch(Exception e){
            log.error("Unable to load config ", e);
        }
    }
   
    
    public DocumentDescriptor importFile(AccountDocumentParameters params, SupportedDocuments supportedDoc, File f) 
    throws IOException, ServiceException 
    {
        RepositoryMetadataHandler metadataHandler  = new RepositoryMetadataHandler();
        DocumentDescriptor d = null;
        switch(supportedDoc){
        case CCR:
            d = importCCRFile(params, f);
            break;
        case DICOM:
            d = importDicomFile(metadataHandler, params, supportedDoc, f);
            break;
        case PDF:
            d = importPdfFile(params, f);
            break;
        case JPG:
            d = importJpegFile(params, f);
            break;
        case PNG:
            d = importPngFile(params, f);
            break;
        }
       
        return(d);
    }
    
    private CompoundDocumentDescriptor  importDicomFile( 
            RepositoryMetadataHandler metadataHandler, 
            AccountDocumentParameters params, 
            SupportedDocuments supportedDoc,  
            File f) 
    throws IOException, ServiceException {
         
        try {
            DicomFileMetadataParser parser = new DicomFileMetadataParser(f);
            DicomMetadata  dicomMetadata = parser.parse();
            SHA1 sha1 = new SHA1();
            sha1.initializeHashStreamCalculation();
            
            String seriesGuid = sha1.calculateStringHash(dicomMetadata.getSeriesInstanceUid());
            log.info("series guid is " + seriesGuid + ", seriesinstanceuid is " + dicomMetadata.getSeriesInstanceUid());
            sha1.reset();
            String imageHash = sha1.calculateStringHash(dicomMetadata.getSopInstanceUid());
            sha1.reset();
            String fileHash = null;
            fileHash = sha1.calculateFileHash(f);
            
            FileInputStream inputStream = new FileInputStream(f);
            CompoundDocumentDescriptor docDescriptor = new CompoundDocumentDescriptor();
            docDescriptor.setParentName(dicomMetadata.getSeriesInstanceUid());
            docDescriptor.setContentType(supportedDoc.getContentType());
            docDescriptor.setDocumentName(imageHash);
            docDescriptor.setStorageId(params.storageid);
            docDescriptor.setGuid(seriesGuid);
            docDescriptor.setSha1(fileHash);
            docDescriptor.setCreationDate(new Date());
            docDescriptor.setMetadataHandler(metadataHandler);
            
            repository.addCompoundDocument(docDescriptor);
            repository.putInputStream(docDescriptor, inputStream);
            return docDescriptor;
        }
        catch(TransactionException e){
            throw new ServiceException(e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new ServiceException(e);
        }    
        
    }
    
    private DocumentDescriptor importPdfFile(AccountDocumentParameters params, File f){
        throw new RuntimeException("Not yet implemented");
    }
    private DocumentDescriptor importJpegFile(AccountDocumentParameters params, File f){
        throw new RuntimeException("Not yet implemented");
    }
    private DocumentDescriptor importPngFile(AccountDocumentParameters params, File f) throws ServiceException {
        try {
            InputStream is = new FileInputStream(f);
            SimpleDocumentDescriptor descriptor = new SimpleDocumentDescriptor();
            descriptor.setStorageId(params.storageid);
            descriptor.setContentType(DocumentTypes.PNG_MIME_TYPE);
            descriptor.setDocumentName(f.getName()); 
            descriptor.setCreationDate(new Date());
            repository.putInputStream(descriptor, is);
            return descriptor;
        } 
        catch (Exception e) {
            throw new ServiceException(e);
        }
    }
    private DocumentDescriptor importCCRFile(AccountDocumentParameters params, File f){
        throw new RuntimeException("Not yet implemented");
    }
   
    public void closeTransaction(AccountDocumentParameters params,String transactionHandle) throws ServiceException, IOException{
        repository.closeCompoundDocument(transactionHandle);
        log.info("Closing transaction handle " + transactionHandle);
    }
}
