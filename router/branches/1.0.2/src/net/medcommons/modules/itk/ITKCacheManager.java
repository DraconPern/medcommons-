package net.medcommons.modules.itk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.medcommons.modules.configuration.Configuration;

import org.apache.log4j.Logger;
/**
 * Primitive file cache manager for ITK files.
 * 
 * Basically removes files that are older than the specified idleTime.
 * 
 * Routines accessing the cached images (like WADOStreamImageJob) should 
 * set the last modified time of files they wish to keep in the cache.
 * Basic algorithm: 
 * @author sdoyle
 *
 */
public class ITKCacheManager implements Runnable{
	 private static Logger log = Logger.getLogger(ITKCacheManager.class);
	private static File cacheDirectory = null;
	private final static long DELAY_MSEC = 1000 * 10; 
	private boolean running = true;
	private static ITKCacheManager itkCacheManger = null;
	
	private final static int DEFAULT_IDLE_TIME = 10 * 1000 * 30; // 10 minutes
	private static long idleTime = DEFAULT_IDLE_TIME ; 
	
	private static ITKCacheManager factory(){
		if (itkCacheManger == null){
			itkCacheManger = new ITKCacheManager();
			String retentionTime = Configuration.getProperty("ITK_ImageCacheRetentionTimeSeconds", "30");
			try{
				long retentionTimeSeconds = Long.parseLong(retentionTime.trim());
				idleTime = retentionTimeSeconds * 1000; 
			}
			catch(Exception e){
				idleTime = DEFAULT_IDLE_TIME; 
			}
			log.info("Cache retention time set to " + idleTime/1000 + " seconds");
			new Thread(itkCacheManger).start();
		}
		return(itkCacheManger);
	}
	private ITKCacheManager(){
		super();
		// Should get config information from 
		
	}
	public static File getCacheDirectory() throws IOException{
		if (cacheDirectory == null){
			if (itkCacheManger == null){
				itkCacheManger = ITKCacheManager.factory();
			}
			File dataDir = new File("data");
	        if (!dataDir.exists()){
	            throw new FileNotFoundException(dataDir.getAbsolutePath());
	        
	        
	        }
	        File repositoryDir = new File(dataDir, "Repository");
	        if (!repositoryDir.exists()){
	            throw new FileNotFoundException(repositoryDir.getAbsolutePath());
	        
	        }
	        File scratchDirectory = new File(repositoryDir, "Scratch");
	        if (!scratchDirectory.exists()){
	            throw new FileNotFoundException(scratchDirectory.getAbsolutePath());
	        }
	        cacheDirectory = new File(scratchDirectory, "ImageCache");
	        if (!cacheDirectory.exists()){
	            boolean success = cacheDirectory.mkdir();
	            if (!success){
	            	throw new IOException("Unable to make directory " +
	            			cacheDirectory.getAbsolutePath());
	            }
	        }
		}
		else{
			if (!cacheDirectory.exists()){
				throw new FileNotFoundException(cacheDirectory.getAbsolutePath());
			}
		}
	    return(cacheDirectory);
	}
	public void run(){
		while(running){
			try{
				File cache = ITKCacheManager.getCacheDirectory();
				deleteOldFiles(idleTime);
				Thread.sleep(DELAY_MSEC);
			}
			
			catch(IOException e){
				// Log error and continue. Need to make this more granular
				log.error("Error cleaning up cache", e);
			}
			catch(Throwable e){
				log.fatal("Unexpected error in cache manager; files might not be being deleted:", e);
				return;
			}
		}
		
	}
	
	/**
	 * Deletes all files older than idleTime msec.
	 * @param idleTime
	 * @throws IOException
	 */
	private void deleteOldFiles(long idleTimeMsec) throws IOException{
		File [] cacheFiles = cacheDirectory.listFiles();
		long deleteTime = System.currentTimeMillis() - idleTimeMsec;
		int filesDeleted = 0;
		int activeFiles = 0;
		int errorFiles = 0;
		//log.info("Files in cache:" + cacheFiles.length);
		for (int i=0; i<cacheFiles.length; i++){
			
			File f = cacheFiles[i];
			long lastModifiedTime = f.lastModified();
			if (lastModifiedTime < deleteTime){
				boolean success = f.delete();
				if (!success){
					errorFiles++;
					log.error("Failed to delete file " + f.getAbsolutePath() + " from cache.");
				}
				else{
					filesDeleted++;
				}
			}
			else{
				// File is active; leave it alone
				activeFiles++;
			}
		}
		if ((filesDeleted == 0) && (errorFiles == 0)){
			// Nothing to do.
			;
		}
		else{
			String message = "Cache reaper: \nFilesDeleted:" + filesDeleted + 
			"\nErrorFiles:" + errorFiles + "\nActiveFiles:" + activeFiles;
			log.info(message);
		}
			
	}
}
