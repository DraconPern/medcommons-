package net.medcommons.router.services.repository;

import java.io.File;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.crypto.AES;
import net.medcommons.modules.crypto.DummyKey;
import net.medcommons.modules.crypto.DummyKeySpec;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.keymanagement.EncryptedKeyCache;
import net.medcommons.modules.keymanagement.EncryptedKeyCacheFactory;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.ServiceException;

import org.apache.log4j.Logger;

/**
 * Adds encryption to a simple file repository
 * 
 * @author ssadedin
 */
public class EncryptionRepositoryListener implements RepositoryListener {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(EncryptionRepositoryListener.class);

    /**
     * Key cache that will be used to optimize key retrieval
     */
	private EncryptedKeyCache keyCache;
	
    public EncryptionRepositoryListener(String nodeId, EncryptedKeyCache keyCache) {
        this.keyCache = keyCache;
    }
    
    public EncryptionRepositoryListener(String nodeId) {
        keyCache = EncryptedKeyCacheFactory.encryptedKeyCache(nodeId);    
    }

    /**
     * Creates or retrieves the existing key for the document.  These are used
     * in the other lifecycle phases of document storage.
     */
    @Override
    public void onBeginStoreDocument(RepositoryEvent evt) throws RepositoryException {
        log.info("onBeginStoreDocument " + evt.desc.toShortString());
        
        DocumentDescriptor document = evt.desc;
        if(document.getKey() != null) {
            log.debug("Document " + document.toShortString() + " already has a key");
            return;
        }
        
        try {
            resolveExistingKey(document);
            if(document.getKey() != null)
                return;
            
            log.info("Generating new encryption key for document " + document.toShortString());
            AES aes = new AES();
            Key key = aes.generateKey();
            document.setKey(key);
            document.setKeyStored(false);
            SecretKeySpec decryptionKey = aes.getKeyFromKey(key);
            document.setDecryptionKey(decryptionKey);
        }
        catch(Exception e) {
            throw new TransactionException("Unable to generate AES key for " + document , e);
        }
    }

    /**
     * Attempt to resolve an existing key for this document.
     * 
     * @param document
     * @throws ServiceException
     */
    private void resolveExistingKey(DocumentDescriptor document) throws ServiceException {
        
        synchronized(keyCache) {
            
            SecretKeySpec decryptionKey = keyCache.getDecryptionKey(document.getStorageId(), document.getGuid());
            if(decryptionKey == null)
                return;
            
            log.info("Found existing decryption key for document " + document.toShortString());
            
            Key key = keyCache.getEncryptionKey(document.getStorageId(), document.getGuid());
            if(key == null) 
                throw new IllegalStateException("Encryption key found but no decryption key set");
            
            document.setKey(key);
            document.setDecryptionKey(decryptionKey);
            document.setKeyStored(true);
            
            // TODO:  what to do about keyStored?   Leaving it false probably means
            // the key will get overwritten which is poor for performance but 
            // not a problem otherwise
            
        }
    }

    @Override
    public void onEndStoreDocument(RepositoryEvent evt) throws RepositoryException {
        log.info("onEndStoreDocument " + evt.desc.toShortString());
        DocumentDescriptor document = evt.desc;
        try {
            Key key = document.getKey();
            String docInfo = new String(document.getStorageId() + ", " + document.getGuid());
            if(!document.getKeyStored()) {
                log.info("Storing key for " + docInfo);
                synchronized(keyCache) {
	                keyCache.addNewKey(document.getStorageId(),document.getGuid(), key);   
                }
            }
            else 
                log.info("Not storing key (already saved) " + docInfo);
        }
        catch (ServiceException e) {
            throw new RepositoryException("Failed to create or store encryption key for document " + document, e);
        }
    }

    /**
     * Wraps the input stream contained in the event with a decryption stream that
     * will decipher the encrypted bytes.
     */
    @Override
    public void onInput(RepositoryEvent evt) {
        log.debug("onInput: " + evt.desc.toShortString());
        try {
            AES aes = new AES();                
            SecretKeySpec decryptionKey = evt.desc.getDecryptionKey();
            if(decryptionKey == null) {
                synchronized(keyCache) {
	                evt.desc.setKey(keyCache.getEncryptionKey(evt.desc.getStorageId(), evt.desc.getGuid()));
	                evt.desc.setDecryptionKey(keyCache.getDecryptionKey(evt.desc.getStorageId(), evt.desc.getGuid()));
                }
                decryptionKey = evt.desc.getDecryptionKey();
            }
            
            if(decryptionKey == null) {
                
                // If there was no key at all registered with the encryption key service
                // then we assume that this is a patient that was created with 
                // encryption turned off.  Therefore we register a dummy key 
                // for the patient.   
                Key key = new DummyKey();
                log.info("Registering dummy encryption key for storage account " + evt.desc.getStorageId());
                synchronized(keyCache) {
	                keyCache.addNewKey(evt.desc.getStorageId(), evt.desc.getGuid(), key);
                }
                decryptionKey = aes.getKeyFromKey(key);
            }
            
            evt.in = aes.createInputStream(evt.in, decryptionKey);
        }
        catch(Exception e){
            throw new RuntimeException("Decryption failure for "+ evt.desc, e);
        }
    }

    /**
     * Wraps the output stream contained in the event with an encrypted stream
     * using the key that should already have been set on the {@link DocumentDescriptor}
     * contained in the event.
     */
    @Override
    public void onOutput(RepositoryEvent evt) throws RepositoryException {
        log.info("onOutput " + evt.desc.toShortString());
        try {
            AES aes = new AES();
            Key key = evt.desc.getKey();
            if (key == null) 
                throw new NullPointerException("Null key for document: " + evt.desc);
            
            if(key instanceof DummyKeySpec)
                return;
            
            byte [] iv = aes.generateInitializationVector();
            
            Cipher encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            
            // Generating the encrypted file
            evt.out.write(iv, 0, AES.IV_SIZE); // Write out the initialization vector as the first 16 bytes
            evt.out = new CipherOutputStream(evt.out, encrypt);
        }
        catch(Exception e) {
            throw new RepositoryException("Unable to create encrypted stream for document " + evt.desc, e);
        }
    }

    @Override
    public void onFileUnavailable(RepositoryEvent evt, File f) throws RepositoryException {
        
    }

    @Override
    public void onDelete(RepositoryEvent evt) throws RepositoryException {
        // Can't do very much - we should really check 
        // if the user has *any* documents and if not extinguish their
        // encryption key
    }
}
