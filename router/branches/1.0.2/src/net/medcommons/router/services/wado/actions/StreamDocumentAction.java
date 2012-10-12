/*
 * $Id: StreamDocumentAction.java 3085 2008-11-18 20:25:29Z sdoyle $
 */
package net.medcommons.router.services.wado.actions;

import static net.medcommons.modules.utils.Str.blank;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.DicomMetadataKeys;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.personalbackup.BackupGenerator;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.services.interfaces.NodeKeyProvider;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.HibernateUtil;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.DocumentNotFoundException;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryFactory;

import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Session;

/**
 * Streams a document directly as a response, based on guid and account id.
 */
public class StreamDocumentAction extends Action {
    
    private static HashMap<String, String> CONTENT_TYPE_EXTENSIONS = new HashMap<String, String>();
    
    static {
        CONTENT_TYPE_EXTENSIONS.put(CCRConstants.CCR_MIME_TYPE, ".xml");
        CONTENT_TYPE_EXTENSIONS.put(CCRConstants.DICOM_MIME_TYPE, ".dcm");
        CONTENT_TYPE_EXTENSIONS.put(CCRConstants.HTML_MIME_TYPE, ".html");
        CONTENT_TYPE_EXTENSIONS.put(CCRConstants.JPG_MIME_TYPE, ".jpg");
        CONTENT_TYPE_EXTENSIONS.put(CCRConstants.PDF_MIME_TYPE, ".pdf");
    }
    
    NodeKeyProvider nodeKeyProvider = Configuration.getBean("nodeKeyProvider");
    
    DocumentIndexService indexService = Configuration.getBean("documentIndexService");

    /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(StreamDocumentAction.class);

	/**
	 * Method execute
	 */
	@SuppressWarnings("unchecked")
    public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {      
        
        final String guid = request.getParameter("guid");
        
        
        if(guid == null)
            throw new IllegalArgumentException("Parameter guid is required");
        
        String accountId = request.getParameter("accid");
        if(accountId== null)
            throw new IllegalArgumentException("Parameter accid is required");

        UserSession desktop = UserSession.get(request);
        
        final String auth = desktop.getAuthenticationToken();
        
        // Find the document details
        DocumentDescriptor desc = indexService.getDocument(accountId, guid);
        
        // If the document type is a DICOM study, send them a zip file of the series 
        if(desc != null && DocumentTypes.DICOM_STUDY_MIME_TYPE.equals(desc.getContentType())) {
            
            final String studyInstanceUID = desc.getMetadataValue(DicomMetadataKeys.STUDY_INSTANCE_UID);
            
            Session s = HibernateUtil.currentSession();
            try {
                // Get all the series documents from the repository
                // TODO: make this use the indexer interface!
                List<DocumentDescriptor> seriesDocs = 
                    s.createQuery("from DocumentDescriptor d where d.guid = ?").setParameter(0, desc.getSha1()).list();
                
                final Set<String> seriesGuids = new HashSet<String>();
                for(DocumentDescriptor seriesDoc : seriesDocs) {
                    if(studyInstanceUID.equals(seriesDoc.getMetadataValue(DicomMetadataKeys.STUDY_INSTANCE_UID))) {
                        seriesGuids.add(seriesDoc.getSha1());
                    }
                }
                    
                BackupGenerator generator = new BackupGenerator(accountId, auth) {
                    @Override
                    protected boolean includeDocument(DocumentDescriptor descriptor) {
                        return seriesGuids.contains(descriptor.getGuid());
                    }
                };
                
                OutputStream out = response.getOutputStream();
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition","inline; filename=" + "MedCommons-Dicom-" + accountId + ".zip");
                generator.generateBackup(out);
                out.close();
            }
            finally {
                HibernateUtil.closeSession();
            }
            return null;
        }
        
        // Validate whether the given account has access to the requested document
        DocumentReference [] docRef = desktop.getServicesFactory().getDocumentService().resolve(accountId,guid); 
        
        if(docRef == null || docRef.length == 0) 
            throw new IllegalArgumentException("Unable to resolve document " + guid + " with account " + accountId);

        DocumentReference ref = null;
        String nodeKey = nodeKeyProvider.getNodeKey();
        String nodeId = Configuration.getProperty("NodeID");
        for (DocumentReference r : docRef) {
            if(r.getLocation().equals(nodeId) || nodeKey.equals(r.getLocationKey())) {
                ref = r;
                break;
            }
        }
        
        if(ref == null) {
            throw new DocumentNotFoundException("The document you requested is not available on this instance.");
        }
        
        String dl = request.getParameter("dl");
        
        LocalFileRepository repo = (LocalFileRepository) RepositoryFactory.getLocalRepository();
        Properties props = repo.getProperties(ref.getStorageAccountId(), guid);
        String contentType = props.getProperty(RepositoryFileProperties.CONTENT_TYPE);
        log.info("Streaming back document with content type " + contentType);
        if(!blank(dl)) {
            String fileName = props.getProperty(RepositoryFileProperties.NAME);
            if(blank(fileName)) {
                fileName = guid+CONTENT_TYPE_EXTENSIONS.get(contentType);
            }
            response.setHeader("Content-Disposition", "attachment; filename="+fileName);
        }
        response.setContentType(contentType);
        response.setContentLength(Integer.parseInt(props.getProperty(RepositoryFileProperties.LENGTH)));
        FileHelper.copy(repo.getDocument(ref.getStorageAccountId(), guid), response.getOutputStream());
	    return null;
   }
      
}
    

