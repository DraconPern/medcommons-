package net.medcommons.modules.storagehandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import net.medcommons.emcbridge.data.DicomWrapper;
import net.medcommons.emcbridge.data.SeriesObject;
import net.medcommons.modules.cxp.server.RepositoryMetadataHandler;
import net.medcommons.modules.filestore.SimpleRepository;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DicomMetadata; 
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.DocumentTypes;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import com.documentum.fc.client.IDfSysObject;
/**
 * Generates gateway series metadata from Documentum data.
 * Generates the following items:
 * Series folder
 * Series property file
 * Series metadata.xml
 * All image properties files.
 * 
 * Assumes that the images will be named with the documentum document id.
 * @author sean
 */
public class SeriesMetadata {
    final static String repositoryRoot = "/opt/gateway/data/Repository/";
    final static File repositoryDir = new File(repositoryRoot);
    
    private static Logger log = Logger.getLogger("SeriesMetadata");
    
    public static void CreateSeriesMetadata(String storageId, String guid, String contentType,SeriesObject seriesObject) throws IOException,JDOMException, ServiceException{
        File storageDir = new File(repositoryDir,storageId);
        if (!storageDir.exists()){
            throw new FileNotFoundException("Storage directory does not exist:" + storageDir.getAbsolutePath());
        }
        File seriesDir = new File(storageDir, guid);
        if (!seriesDir.exists()){
            boolean success = seriesDir.mkdir();
            if (!success){
                throw new IOException("Can not create series directory " + seriesDir.getAbsolutePath());
            }
        }
        RepositoryMetadataHandler repositoryMetadata = new RepositoryMetadataHandler();
        ArrayList<DicomWrapper> images = seriesObject.getImages();
        String seriesInstanceUid = seriesObject.getIdentifier();
        CompoundDocumentDescriptor seriesDescriptor = new CompoundDocumentDescriptor();
        seriesDescriptor.setContentType(DocumentTypes.DICOM_MIME_TYPE);
        seriesDescriptor.setCreationDate(new Date());
        seriesDescriptor.setDocumentName(guid);
        seriesDescriptor.setGuid(guid);
        seriesDescriptor.setNDocuments(images.size());
        seriesDescriptor.setSha1(guid);
        seriesDescriptor.setStorageId(storageId);
       
        int imageCount = 0;
        int imagesInWrongSeries = 0;
        for (int i=0;i<images.size();i++){
            DicomWrapper image = images.get(i);
            DicomMetadata metadata = image.getDicomMetadata();
            if (metadata.getSeriesInstanceUid().equals(seriesInstanceUid)){
                String imageId = image.getExternalDocumentId();
                metadata.setDocumentName(metadata.getSopInstanceUid());
                seriesDescriptor.setDocumentName(imageId);
                seriesDescriptor.setContentType(DocumentTypes.DICOM_MIME_TYPE);
                repositoryMetadata.addDocument(seriesDescriptor, metadata);
                seriesDescriptor.setParentName(metadata.getSeriesInstanceUid());
                imageCount++;
            }
            else{
                imagesInWrongSeries++;
            }
        }
        File metadataFile = SimpleRepository.metadataFile(seriesDescriptor);
        repositoryMetadata.generateMetadataFile(metadataFile, false);
        log.info("Creating metadata for series " + seriesInstanceUid + " " + guid + " with " + imageCount + " images, array size " + images.size());
        if (imagesInWrongSeries != 0){
            log.info("There were " + imagesInWrongSeries + " images in the incorrect series");
        }
        log.info("Wrote series metadata to " + metadataFile.getAbsolutePath());
        Properties seriesProperties = SimpleRepository.createCompoundDocumentProperties(seriesDescriptor);
        File seriesPropFile = SimpleRepository.propertyFile(storageId, seriesDescriptor.getGuid());
        FileOutputStream outProperties = new FileOutputStream(seriesPropFile);
        seriesProperties.store(outProperties, "Compound document");
        log.info("wrote series property file to " + seriesPropFile.getAbsolutePath());
        outProperties.close();
       
        for (int i=0;i<images.size();i++){
            DicomWrapper image = images.get(i);
            IDfSysObject objc = image.getDocumentumMetadata();
            DicomMetadata dicomMetadata = image.getDicomMetadata();
            if (dicomMetadata.getSeriesInstanceUid().equals(seriesInstanceUid)){
                String imageId = image.getExternalDocumentId();
                CompoundDocumentDescriptor imageDoc = new CompoundDocumentDescriptor();
                imageDoc.setGuid(seriesDescriptor.getSha1());
                imageDoc.setSha1(dicomMetadata.getSha1());
                imageDoc.setContentType(DocumentTypes.DICOM_MIME_TYPE);
                imageDoc.setDocumentName(imageId);
                imageDoc.setStorageId(storageId);
                imageDoc.setCreationDate(new Date());
                imageDoc.setLength(dicomMetadata.getLength());
                imageDoc.setParentName(dicomMetadata.getSeriesInstanceUid());
                imageDoc.setNDocuments(1);
                
                File imagePropertyFile = SimpleRepository.CompoundFileMemberPropertyFile(imageDoc);
                log.info("image property file is " + imagePropertyFile.getAbsolutePath());
                outProperties = new FileOutputStream(imagePropertyFile);
                Properties imageProperties = SimpleRepository.createProperties(imageDoc);
                imageProperties.store(outProperties, "Compound document");
                outProperties.close();
                log.info("Just wrote image properties file " + imagePropertyFile.getAbsolutePath());
            }
            repositoryMetadata.clear();
            //log.info(image);
            //InputStream in = service.retrieveDocument(parameters, imageId);
          
            /*
            
            File imageFile = new File(seriesDir, image.getDicomMetadata().getSopInstanceUid());
            FileOutputStream fout = new FileOutputStream(imageFile);
            log.info("About to write image to " + imageFile.getAbsolutePath());
            byte b[] = new byte[4096];
            int j;
            while ((j = in.read(b)) != -1) {
                fout.write(b,0, j);
            }
            fout.close();
            in.close();
            */
            
        }
    }
    
}
