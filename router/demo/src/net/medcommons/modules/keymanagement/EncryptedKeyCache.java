package net.medcommons.modules.keymanagement;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.AES;
import net.medcommons.modules.crypto.DummyKeySpec;
import net.medcommons.modules.services.client.rest.DocumentKey;
import net.medcommons.modules.services.interfaces.DocumentService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

/**
 * Stores keys in memory with the key being storageid
 * <p>
 * TODO: Don't store keys in cache: store byte arrays. XOR them or do something rude so that they
 * are hard to decode by someone poking around in memory.
 * <p><b>This class is not thread safe.  Client code *must* synchronize on the object for all 
 * operations even if accessing in a single threaded manner</b>.  Internally, the cache
 * schedules a timer to maintain keys which will access the cache itself.
 * 
 * @author sean, ssadedin@medcommons.net
 */
public class EncryptedKeyCache {
	
    /**
     * The amount of time between checks for idle cache keys
     */
    private static final int KEY_FLUSH_POLL_PERIOD = 60000;

    /**
     * The time after which keys will be automatically flushed from the cache
     */
    private static final int MAX_KEY_IDLE_CACHE_TIME = 30 * 60 * 1000;

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(EncryptedKeyCache.class);
	 
    /**
     * The actual data structure containing keys
     */
 	private LRUMap keyMap = new LRUMap();
	
	/**
	 * The services for adding/deleting document keys
	 */
	private DocumentService documentService = null;
	
	/**
	 * Node id of this gateway
	 */
	private String nodeId = null;
	
	/**
	 * Crypto library
	 */
	private AES aes = null;
	
	/**
	 * Static reference (useful for JUnit access)
	 */
	private static EncryptedKeyCache encryptedKeyCache = null;
	
	public static EncryptedKeyCache getEncryptedKeyCache(){
		return(encryptedKeyCache);
	}
	
	/**
	 * Used by unit test
	 */
	protected EncryptedKeyCache() {
	}
	
	/**
	 * 
	 * @param clientId
	 * @throws NoSuchAlgorithmException
	 * @throws ServiceException 
	 */
	
	public EncryptedKeyCache(String nodeId, DocumentService documentService) throws NoSuchAlgorithmException, ServiceException{
		this.nodeId = nodeId;
		aes = new AES();
		
		// TODO: one day we will encrypt the keys in memory
		// cacheKey = aes.generateKey();
		this.documentService = documentService;
		encryptedKeyCache = this;
		
		startKeyScavenger();
	}
	
    public void startKeyScavenger() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() { try {
                final long expiryThresholdMs = System.currentTimeMillis() - MAX_KEY_IDLE_CACHE_TIME;
                synchronized(EncryptedKeyCache.this) {
                    List<String> toRemove = new ArrayList<String>();
                    Iterator<String> i = keyMap.orderedMapIterator();
                    while(i.hasNext()) {
                        String entry = i.next();
                        if(((KeyEntry)keyMap.get(entry)).cacheTime > expiryThresholdMs) {
                            // Note: cannot use i.remove() due to 
                            // bug in LRUMap - https://issues.apache.org/jira/browse/COLLECTIONS-330
	                        toRemove.add(entry);
                        }
                        else
                            break;
                    }
                    
                    for(String entry : toRemove) {
                        keyMap.remove(entry);
                    }
                }
            }
            catch(Throwable t) {
              log.warn("Error in key maintenance thread", t);  
            }}
        }, KEY_FLUSH_POLL_PERIOD, KEY_FLUSH_POLL_PERIOD);
    }
	
	/**
	 * Removes everything from the key cache. Shouldn't 
	 * have any effect on anything other than the latency of
	 * retrieving keys that are needed for decryption.
	 *
	 */
	public void clearCache(){
		keyMap.clear();
	}
	
	/**
	 * Adds a new key to the cache.
	 * 
	 * @param documentIdentifier
	 * @param encryptionKey
	 * @param decryptionKey
	 */
	public void addNewKey(String storageId,  String documentIdentifier, Key encryptionKey) throws ServiceException{
	    
        AES aes = new AES();
        SecretKeySpec decryptionKey = aes.getKeyFromKey(encryptionKey);
		KeyEntry keyentry = new KeyEntry(storageId, encryptionKey, decryptionKey);
		log.debug("decryption key is " + decryptionKey);
		String keyIndex = storageId;
		keyMap.put(keyIndex, keyentry);
		String formattedKey = aes.keyToString(decryptionKey);
		
		// Throws error here if it's out of synch (new file on repository, but
		// guid/nodeid pair already exists on central.
		documentService.registerKey(storageId, formattedKey, aes.keyToString(encryptionKey));
		log.debug("Added key for storage id " + storageId + ", document " + documentIdentifier + " key " + formattedKey);
	}
	
	/**
	 * Returns the decryption key for the specified document. It first tries the local
	 * cache, then the server.  If a user has *any* content stored unencrypted then
	 * returns an instance of {@link DummyKeySpec}.
	 * 
	 * @param documentIdentifier
	 * @return
	 */
	public SecretKeySpec getDecryptionKey(String storageId, String documentIdentifier) throws ServiceException{
		SecretKeySpec key = null;
		String keyIndex = storageId;
		KeyEntry found = (KeyEntry) keyMap.get(keyIndex);
		
		if(found != null) {
			key = found.decryptionKey;
			found.tickleTime();
			log.debug("Found key for " + documentIdentifier + " in cache ");
		}
		else {
			
			/**
			 * Not in the cache. Load from server.
			 */
			DocumentKey dKey[] = (DocumentKey[])documentService.getDocumentDecryptionKey(storageId,documentIdentifier, nodeId);
            if(dKey != null) {
            	for(int i=0;i<dKey.length;i++) {
            		DocumentKey k = dKey[i];
            		if (k == null) break;
            		key = aes.getKeyFromString(k.getKey());
            		log.info("Retrieved document key for " + k.getGuid() + " from server:" + k.getKey() + " node=" + nodeId + 
            				" creating key " + key);
            		KeyEntry keyentry = new KeyEntry(storageId, key, key);
            		keyIndex = storageId;
            		keyMap.put(keyIndex, keyentry);		
            	}
            }
            else 
            	log.error("Document key is null for " + documentIdentifier);
		}
		
		return(key);
	}
	
	public Key getEncryptionKey(String storageId,  String documentIdentifier){
		String keyIndex = storageId;
		KeyEntry found = (KeyEntry) keyMap.get(keyIndex);
		Key key = null;
		if (found != null){
			key = found.encryptionKey;
			found.tickleTime();
		}
		log.debug("Returning encryption key " + key + " for document " + documentIdentifier);
		return key;
	}
	
	/**
	 * Removes the encryption and decryption keys from the 
	 * cache.
	 * @param documentIdentifier
	 */
	public void removeKey(String storageId, String documentIdentifier, boolean deleteFromServer) throws ServiceException{
		String keyIndex = storageId;
		keyMap.remove(keyIndex);
		if(deleteFromServer) {
			documentService.deleteDocumentLocation(storageId, documentIdentifier,nodeId);
		}
	}
	/**
	 * TODO: Need to build in access control.
	 * @author sean
	 *
	 */
	private class KeyEntry {
		String storageId = null;
		Key encryptionKey = null;
		SecretKeySpec decryptionKey = null;
		long cacheTime = -1;
		
		/**
		 * @param documentIdentifer
		 * @param key
		 */
		private KeyEntry(String storageId, Key encryptionKey, SecretKeySpec decryptionKey){
			this.storageId = storageId;
			this.decryptionKey = decryptionKey;
			this.encryptionKey = encryptionKey;
			tickleTime();
		}
		/**
		 * Invoked to update the time stamp of the cached key entry.
		 */
		private void tickleTime(){
			cacheTime = System.currentTimeMillis();
		}
	}

}
