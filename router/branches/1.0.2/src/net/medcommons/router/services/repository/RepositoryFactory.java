/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Jun 27, 2005
 *
 * 
 */
package net.medcommons.router.services.repository;

/**
 * Currently there are two respositories active in the running gateway. Both currently view
 * exactly the same files - but the methods of access are very different.
 * <ol>
 * <li> The NetworkRepository is layered on top of WebDAV. It is most suitable for modules that
 * will be factored out of the main gateway in the near future (such as DICOM and CXP services). 
 * <li> The LocalRepository provides read-only access to the local file system. WADO and various JSPs
 * need local,streaming access to the files.
 * </ol>
 * 
 * In the future these two repository interfaces may grow further apart. 
 * <ul>
 * <li>The network layer
 * may be more suitable for clustering farms of repositories; the local repository
 * will probably still just point to the local file system.
 * <li> The security models for the two are very different. If you're in the gateway - you have access
 * to the file system. Over a network the model might be much more complex.
 * </ul>
 * 
 * 
 * @author sean
 *
 * 
 */
public class RepositoryFactory {
	
	final static int XDS_REPOSITORY = 0; // Remove
	final static int MEDCOMMONS_REPOSITORY = 1;
	final static int REPOSITORY = MEDCOMMONS_REPOSITORY;
	/**
     * The singleton repository instance for the source repo
     */
	private static DocumentRepository localRepository = null;
	//private static DocumentRepository networkRespository = null;
	static{
		
		//networkRespository = new MedCommonsRepository();
		localRepository = new LocalFileRepository();
	}
	
   /*
    public static DocumentRepository getNetworkRepository() {
        return networkRespository;
    }
    */
    public static DocumentRepository getLocalRepository() {
        return localRepository;
    }
}
