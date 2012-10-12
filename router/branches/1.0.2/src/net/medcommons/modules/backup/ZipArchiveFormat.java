package net.medcommons.modules.backup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.personalbackup.BackupGenerator;
import net.medcommons.modules.personalbackup.BackupGenerator.DocumentRefHolder;
import net.medcommons.modules.publicapi.PHRTransactionException;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.router.services.repository.RepositoryException;

/**
 * An archival format that stores backups in zip files.
 * <p>
 * Simple documents are zipped stand alone while compound documents are
 * collected into single zip files.
 * <p>
 * TODO: This format does not cope well with a series being re-opened.  It will
 * cause the whole DICOM series to get backed up again (very wasteful if just 1 image
 * was added).   It needs to become smart enough to add incremental parts.  The
 * way to do that, I think, is to somehow know what was already backed up, and
 * then save the files with extensions .1, .2, .3 etc. in S3.  Restore then needs to
 * query for all extensions until it reaches the highest.
 * 
 * @author ssadedin
 */
public class ZipArchiveFormat extends BackupGenerator implements RepositoryArchiveFormat {
    
    /**
     * The document to be backed up
     */
    DocumentDescriptor doc;

    public ZipArchiveFormat(String storageId, String auth, DocumentDescriptor doc) throws ServiceException {
        super(storageId, auth);
        this.doc = doc;
        this.includeMetaFiles = true;
    }

    /**
     * The default backup generator decrypts the backups it makes. We don't need that.
     */
    @Override
    protected InputStream getDocumentInputStream(CompoundDocumentDescriptor compoundDoc) throws RepositoryException {
        try {
            return new FileInputStream(repository.getFile(compoundDoc));
        }
        catch (FileNotFoundException e) {
            throw new RepositoryException("Failed to open raw file for document " + compoundDoc.toShortString());
        }
        catch (IOException e) {
            throw new RepositoryException("Failed to open raw file for document " + compoundDoc.toShortString());
        }
    }

    @Override
    protected long getDocumentSize(CompoundDocumentDescriptor compoundDoc) throws RepositoryException {
        try {
            return repository.getFile(compoundDoc).length();
        }
        catch (TransactionException e) {
            throw new RepositoryException("Failed to calculate size for document " + compoundDoc.toShortString());
        }
        catch (IOException e) {
            throw new RepositoryException("Failed to calculate size for document " + compoundDoc.toShortString());
        }
    }

    @Override
    protected void zipUtils(ZipOutputStream zipOut) throws IOException, TransactionException {
        // Do nothing, we don't want the utils
    }

    @Override
    protected List<DocumentRefHolder> getAccountDocuments() throws ServiceException,
            IOException, PHRTransactionException {
        
        DocumentRefHolder docRef = new DocumentRefHolder();
        docRef.contentType = DocumentTypes.DICOM_MIME_TYPE;
        docRef.creationDate = doc.getCreationDate();
        docRef.descriptor = doc;
        docRef.guid = doc.getGuid();
        docRef.documentLength = doc.getLength();
        
        List<DocumentRefHolder> result = new ArrayList<DocumentRefHolder>();
        result.add(docRef);
        return result;
    }
    
    @Override
    protected String getBackupLocation(SupportedDocuments docType) {
        return doc.getStorageId();
    }

    @Override
    protected String getSuffix(SupportedDocuments docType) {
        return "";
    }    
}
