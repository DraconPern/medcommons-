/*
 * $Id: DBDocumentIndexService.java 3736 2010-06-03 11:21:01Z ssadedin $
 * Created on 03/11/2008
 */
package net.medcommons.router.services.index;

import static net.medcommons.modules.filestore.DicomMetadataKeys.SERIES_INSTANCE_UID;
import static net.medcommons.modules.filestore.DicomMetadataKeys.STUDY_INSTANCE_UID;
import static net.medcommons.modules.utils.DocumentTypes.DICOM_MIME_TYPE;
import static net.medcommons.modules.utils.HibernateUtil.closeSession;
import static net.medcommons.modules.utils.HibernateUtil.currentSession;
import static net.medcommons.modules.utils.Str.blank;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.gt;
import static org.hibernate.criterion.Restrictions.le;
import static org.hibernate.criterion.Restrictions.like;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.filestore.DicomMetadataKeys;
import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.MetadataHandler;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.DocumentTypes;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;

/**
 * An index service that stores document meta data in a database.
 * 
 * @author ssadedin
 */
public class DBDocumentIndexService implements DocumentIndexService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DBDocumentIndexService.class);
    
    
    /**
     * SHA1 object used to hash a study instance UID
     */
    SHA1 sha1 = new SHA1().initializeHashStreamCalculation();
    
    public List<DocumentDescriptor> getDocuments(String storageId, Date from, Date to, DocumentDescriptor matching) throws ServiceException {
        
        Session s = currentSession();
        try {
            Criteria c = s.createCriteria(DocumentDescriptor.class);
            
            c.add(eq("storageId",storageId));
            
            if(to != null)
                c.add(le("creationDate",to));
            
            if(from != null)
                c.add(gt("creationDate", from));
            
            if(matching != null) {
                
                if(!blank(matching.getDocumentName()))
                    c.add(like("name", matching.getDocumentName()));
                
                if(!blank(matching.getSha1()))
                    c.add(eq("guid",matching.getSha1()));
                
                if((!blank(matching.getContentType())))
                    c.add(eq("guid",matching.getContentType()));
            }
        
            return c.list();
        }
        catch(Exception e) {
            throw new ServiceException("Unable to query document index for storage id = " + storageId + " from " + from + " to " + to + " matching " + matching,e);
        }
        finally {
            closeSession();
        }
        
    }
    
    private static boolean initialized = false;
    
    public void init() {
        if(initialized)
            return;
        
        initialized = true;
        
        Session s = currentSession();        
        Statement stmt = null;
        try {
	        stmt = s.connection().createStatement();
            try {
	            stmt.executeUpdate("create index idx_didx_guid on document_index (didx_guid)");
	        }
	        catch(Exception e) {
	            log.info("Failed to initialize document_index table");
	        }
	        try {
	            stmt.executeUpdate("create index idx_didx_creation_date on document_index (didx_creation_date)");
	        }
	        catch(Exception e) {
	            log.info("Failed to initialize document_index table");
	        }
            try {
                stmt.executeUpdate("create index idx_didx_storage_id on document_index (didx_storage_id)");
            }
            catch(Exception e) {
                log.info("Failed to initialize document_index table");
            }
        }
        catch (Exception e) {
            log.error("Unable to create statement for initializing document_index table");
        }
        finally {
            if(stmt != null) try { stmt.close(); } catch (SQLException e) { }
            closeSession();
        }
    }

    public void index(DocumentDescriptor desc) throws ServiceException {
        
        init();
        
        // Do not index individual instances of compound documents
        // Instead, index the finalized version
        if(desc instanceof CompoundDocumentDescriptor && blank(desc.getGuid()))
            return;
        
        log.info("Indexing document " + desc);
        Session s = currentSession();        
        try {
	        s.beginTransaction();
	        
	        // Any existing entry for this guid + storage id?
	        Long count = (Long) s.createQuery("select count(*) from DocumentDescriptor where sha1 = ? and storageId = ?")
	            .setParameter(0, desc.getSha1())
	            .setParameter(1, desc.getStorageId())
	            .uniqueResult();
	        
	        if(count != 0) {
	            log.info("Duplicate descriptor " + desc.toShortString() + " indexed (skipping)");
	            return;
	        }
	        
	        String originalName = desc.getDocumentName();
	        if(desc instanceof CompoundDocumentDescriptor) {
	            // A hack: we want the name of a DICOM series to appear in the index,
	            // not the arbitrary number assigned to it's file.  Therefore we replace
	            // the document name with the parent document name for compound documents
	            CompoundDocumentDescriptor img = (CompoundDocumentDescriptor)desc;
	            
	            // For the case of DICOM, we create a "virtual" indexed document that holds metadata about the study
	            if(DICOM_MIME_TYPE.equals(img.getContentType())) {
	                
	                // First index the study
	                DocumentDescriptor studyDesc = indexStudy(img);
	                
	                // Then index the series, setting the study as its parent
	                indexSeries(img, studyDesc);
	            }
	        }
	        else {
	            s.save(desc);
	        }
	        
	        s.getTransaction().commit();
        }
        catch(Exception e) {
            s.getTransaction().rollback();
            throw new ServiceException("Unable to save document index entry " + desc.toString(), e);
        }
        finally {
            closeSession();
        }
    }

    /**
     * Creates an entry in the index for a DICOM series associated with the given
     * image and study.
     * 
     * @param img           image document providing metadata for the series
     * @param studyDesc     study to which series should be attached
     */
    private void indexSeries(CompoundDocumentDescriptor img, DocumentDescriptor studyDesc) {
        Session s = currentSession();
        DocumentDescriptor seriesDesc = loadDocument(img.getStorageId(), img.getGuid());
        if(seriesDesc == null) {
	        seriesDesc = new CompoundDocumentDescriptor();
	        seriesDesc.setContentType(DocumentTypes.DICOM_SERIES_MIME_TYPE);
	        seriesDesc.setLength(img.getLength());
	        seriesDesc.setStorageId(img.getStorageId());
	        seriesDesc.setDocumentName(img.getParentName());
	        seriesDesc.setSha1(img.getGuid());
	        seriesDesc.setGuid(studyDesc.getSha1());
	        seriesDesc.setCreationDate(img.getCreationDate());
	        seriesDesc.addMetadata(STUDY_INSTANCE_UID, img.getMetadataValue(STUDY_INSTANCE_UID));
	        seriesDesc.addMetadata(SERIES_INSTANCE_UID, img.getMetadataValue(SERIES_INSTANCE_UID));
	        s.save(seriesDesc);
        }
    }

    /**
     * Creates an entry in the index for the study associated with
     * this DICOM document. This is a "virtual" entry in the sense
     * that it has no physical manifistation in the storage system.
     * 
     * @TODO        contemplate moving this to {@link MetadataHandler}
     * @param img   descriptor of study being indexed
     */
    private DocumentDescriptor indexStudy(CompoundDocumentDescriptor img) {
        
        Session s = currentSession();
        
        String studyUID = img.getMetadataValue(DicomMetadataKeys.STUDY_INSTANCE_UID);
        String studyGuid = sha1.calculateStringHash(studyUID);
        
        DocumentDescriptor studyDesc = loadDocument(img.getStorageId(),studyGuid);
        if(studyDesc == null) {
            studyDesc = new SimpleDocumentDescriptor();
            studyDesc.setSha1(studyGuid);
            studyDesc.setDocumentName(img.getMetadataValue(DicomMetadataKeys.STUDY_DESCRIPTION));
            studyDesc.setLength(img.getLength());
            studyDesc.setStorageId(img.getStorageId());
            studyDesc.setContentType(DocumentTypes.DICOM_STUDY_MIME_TYPE); // Is there a better mime type for a study?
            studyDesc.setCreationDate(img.getCreationDate());
            studyDesc.addMetadata(DicomMetadataKeys.STUDY_INSTANCE_UID, studyUID);
        }
        else {
            studyDesc.setLength(img.getLength() + studyDesc.getLength());
        }
        log.info("Indexing study descriptor " + studyDesc);
        s.saveOrUpdate(studyDesc);
        return studyDesc;
    }

    public void clear(String storageId) {
        Session s = currentSession();
        
        log.info("Clearing document index for user " + storageId);
        try {
            s.createQuery("delete from DocumentDescriptor where storageId = ?").setParameter(0, storageId).executeUpdate();
        }
        finally {
            closeSession();
        }
    }

    public DocumentDescriptor getDocument(String storageId, String guid) {
        Session s = currentSession();
        try {
            return loadDocument(storageId, guid);
        }
        finally {
            closeSession();
        }
    }

    private DocumentDescriptor loadDocument(String storageId, String guid) {
        Session s = currentSession();
        DocumentDescriptor d = (DocumentDescriptor) s.createQuery("from DocumentDescriptor where storageId = ? and sha1 = ?")
                    .setParameter(0, storageId)
                    .setParameter(1, guid)
                    .uniqueResult();
        
        if(d != null && d.getMetadata() != null)
            Hibernate.initialize(d.getMetadata());
        
        if(d instanceof SimpleDocumentDescriptor)
            d.setGuid(d.getSha1());
        
        return d;
    }
}
