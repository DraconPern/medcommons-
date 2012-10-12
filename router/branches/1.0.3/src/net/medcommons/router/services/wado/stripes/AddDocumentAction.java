//Created by MyEclipse Struts
//XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.7.101/xslt/JavaClass.xsl

package net.medcommons.router.services.wado.stripes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * Adds document uploaded as file to repository
 * 
 * @author ssadedin
 */
public class AddDocumentAction extends CCRActionBean {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AddDocumentAction.class);

    private LocalFileRepository localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
    
    private FileBean uploadedFile;

    /**
     * Method execute
     * 
     * @return ActionForward
     * @throws
     * @throws ConfigurationException -
     *           if configuration cannot be accessed
     * @throws SelectionException -
     *           if a problem scanning the selections occurs
     */
    public Resolution upload() throws Exception {

        String fileName = uploadedFile.getFileName();
        log.info("Got file data: " + fileName + " of size " + uploadedFile.getSize());

        File scratchFile = new File(localFileRepository.getScratchDirectory(), (SHA1.sha1(System.currentTimeMillis() + "_"+ Math.random())));
        log.info("Storing uploaded file in scratch file " + scratchFile);
        uploadedFile.save(scratchFile);
        
        SHA1 sha1 = new SHA1();
        sha1.initializeHashStreamCalculation();
        String documentGuid = sha1.calculateByteHash(FileUtils.readFileToByteArray(scratchFile));

        session.updateStorageId(ccr);
        session.getServicesFactory().getDocumentService().addDocument(ccr.getStorageId(),documentGuid);
        
        // Put the document into the repository
        String contentType = resolveContentType(scratchFile);
        documentGuid = localFileRepository.putDocument(ccr.getStorageId(), scratchFile, uploadedFile.getFileName(), contentType);

        log.info("Created repository document with name " + documentGuid);    

        // Create the series
        MCSeries series = ccr.createReferenceSeries(fileName, ccr.getStorageId(), documentGuid, contentType);
        
        series.setInSession(true);

        ccr.setGuid(null);

        // Update the XML with the new document reference
        log.info("Adding reference to guid " + documentGuid + " to CCR");                    
        ccr.addReference(series);          
        ccr.addConfirmationRequiredFlag(series);

        log.info("Series count is now " + series.size());
        
        return new ForwardResolution("/addDocument.jsp?close=true");
    }

    /**
     * Attempts to resolve a content type for the given file by 
     * using the content type header in combination with various
     * heuristics.
     * 
     * @param uploadedFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String resolveContentType(File scratchFile) throws FileNotFoundException, IOException {
        String contentType = uploadedFile.getContentType();
        try {
            
            if(DocumentTypes.XML_MIME_TYPE.equals(contentType)) {
                // Note we have to test explicitly for CCR because
                // it comes as text/xml mimetype
                byte[] bytes = FileUtils.readFileToByteArray(scratchFile); // Inefficient!
                CCRDocument.isCcr(bytes);
                contentType = CCRDocument.CCR_MIME_TYPE;
                return contentType;
            }
        }
        catch(InvalidCCRException e) {
            log.debug("Failed to interpret XML as CCR");
        }
        
        // Exception may not be a problem if the content
        // can be accurately determined.
        String uFilename = uploadedFile.getFileName().toUpperCase();
        
        if (uFilename.endsWith(".JPG"))
            contentType = CCRDocument.JPG_MIME_TYPE;
        else if (uFilename.endsWith(".PNG"))
            contentType = CCRDocument.PNG_MIME_TYPE;
        else if (uFilename.endsWith(".PDF"))
            contentType = CCRDocument.PDF_MIME_TYPE;
        else if (uFilename.endsWith(".MOV"))
            contentType = CCRDocument.QUICKTIME_MIME_TYPE;
        else {
            log.error("Unknown content type for uploaded file:" + uploadedFile.getFileName());
            contentType = "application/octet-stream"; // Not sure if this makes sense.
        }
        
        return contentType;
    }

    public FileBean getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

}
