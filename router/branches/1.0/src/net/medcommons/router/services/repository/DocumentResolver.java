/*
 * $Id$
 * Created on 07/11/2006
 */
package net.medcommons.router.services.repository;

import static net.medcommons.modules.utils.Str.eq;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.DocumentReference;
import net.medcommons.modules.services.interfaces.NodeKeyProvider;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.services.interfaces.TrackingReference;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.ccr.StorageModel;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;

/**
 * Resolves documents by guid, account id and / or name to
 * concrete document instances (CCRDocuments). 
 *
 * @author ssadedin
 */
public class DocumentResolver {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DocumentResolver.class);
    
    /**
     * The factory to use for accessing services
     */
    private ServicesFactory factory; 
    
    /**
     * Used to retrieve node key
     */
    private NodeKeyProvider nodeKeyProvider;
    
    /**
     * Create a DocumentResolver
     * 
     * @param factory factory to use for accessing central services
     */
    public DocumentResolver(ServicesFactory factory) {
        super();
        this.factory = factory;
        this.nodeKeyProvider = Configuration.getBean("nodeKeyProvider");
    }
    
    /**
     * Attempts to resolve the given document reference automatically by first trying to resolve it as a 
     * guid (if it matches the correct name pattern for a guid) and then trying it as a logical document. 
     * 
     * @param accid         the storage id of the document to resolve
     * @param nameOrGuid    may contain either the 40 character guid OR the logical name of the document if
     *                       the system storage mode is logical 
     */
    public CCRDocument resolveCCR(String accid, String nameOrGuid) throws ConfigurationException, RepositoryException, ServiceException, PHRException {
        if(nameOrGuid == null)
            throw new IllegalArgumentException("Parameter nameOrGuid should not be null");
            
        if((nameOrGuid.length()==40) && !nameOrGuid.startsWith("medcommons.")) {
            String guid = nameOrGuid;
            DocumentReference reference =  resolveGuid(accid, guid);
            if(reference == null)
                return null;
            
            CCRDocument ccr = (CCRDocument) RepositoryFactory.getLocalRepository().queryDocument(reference.getStorageAccountId(), guid);
            TrackingReference trackingReference = reference.getTrackingReference();
            if(trackingReference != null) {
                ccr.setGuid(guid);
                ccr.setLoadedFromGuid(guid);
                ccr.setTrackingNumber(trackingReference.getTrackingNumber());
                ccr.setAccessPin(trackingReference.getPin());
            }
            return ccr;
        }
        
        // Try and resolve as named document
        return resolveByName(accid, nameOrGuid);
    }

    /**
     * Attempts to resolve the requested guid as a CCR and return it.  Returns null
     * if the document cannot be found.
     * @param accid - account id of owner of document
     * @param guid - guid of document
     * @param auth - auth token proving access to the document
     * 
     * @return
     * @throws RepositoryException 
     * @throws ConfigurationException 
     * @throws ServiceException 
     */
    public DocumentReference resolveGuid(String accid,String guid) throws ConfigurationException, RepositoryException, ServiceException {
        // Validate whether the given account has access to the requested document
        DocumentReference [] docRef = factory.getDocumentService().resolve(accid,guid);
        
        String nodeKey = nodeKeyProvider.getNodeKey(); 
        String nodeId = Configuration.getProperty("NodeID");
        
        for (DocumentReference reference : docRef) {     
            log.debug("resolveGuid: " + reference.getGuid() + " " + reference.getLocation());
            if(eq(nodeKey, reference.getLocationKey()) || nodeId.equals(reference.getLocation())) {
                return reference;
            }            
        } 
        
        if(docRef.length == 0) {
            log.info("No document locations found for document " + guid + " with storage id " + accid);
        }
        else {
            log.info("Found " + docRef.length + " locations for document " + guid + " / storage id " + accid 
                            + ". None matched this node key or ID. [my key="+nodeKey+" found key = " + docRef[0].getLocationKey()+"]");
            
            // Bill is having a party configuring load balanced gateways that write to shared NAS
            // This means gateways can see each other's contents even if different machines
            // are writing the data.  To let this work we support a config flag
            // to return a location even if it has a different node key.
            if(Configuration.getProperty("GlobalSharedRepository", false))
                return docRef[0];
        }
        return null;
    }
    
    public boolean canResolve(String accid, String guid) throws ConfigurationException, RepositoryException, ServiceException {
        return resolveGuid(accid, guid) != null;
    }
    
    /**
     * Tries to resolve the given named document in the given account.  Returns null if not found.
     * 
     * @param accid
     * @param name
     * @return
     * @throws PHRException
     */
    public CCRDocument resolveByName(String accid,String name) throws PHRException {
        StorageModel storageModel = Configuration.getBean("systemStorageModel");
        
        return storageModel.resolveByName(name,accid);
        
    }   
}
