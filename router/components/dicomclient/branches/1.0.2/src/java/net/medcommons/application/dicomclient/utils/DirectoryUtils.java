package net.medcommons.application.dicomclient.utils;

import java.io.File;
import java.io.IOException;

public class DirectoryUtils {

	/**
	 * Makes a read/writable directory at the specified File location.
	 * Note that parent directory must already exist.
	 *
	 * @param dir
	 * @throws IOException
	 */
	private static void makeDir(File dir) throws IOException{
		boolean success = false;
		if (dir.exists()){
			if (!dir.isDirectory()){
				throw new IOException("Can not create existing directory - "
						+ dir.getAbsolutePath() +
						", non-directory file of same name already exists");
			}
		}
		else{
			success = dir.mkdir();
			if (!success){
				throw new IOException("Unable to create directory " +
						dir.getAbsolutePath());
			}
		}
		
		// Set the directory writable by all users.		
		
		success = dir.setWritable(true, false);
		if (!success){
			throw new IOException("Unable to set write permissions on directory:" + dir.getAbsolutePath());
		}
		if (!dir.canWrite()){
			throw new IOException("Setting write permissions failed on directory:"  + dir.getAbsolutePath());
		}

	}
	
	/*
	public static void makeDirectory(File dir) throws IOException{
	   		DirectoryUtils.makeDir(dir);
	         
	}
	*/
		
    public static void makeDirectory(File dir) throws IOException{
   	 if (ContextUtils.isJDK6Orlater())
         	DirectoryUtils.makeDir(dir);
         else if (!dir.exists())
         	dir.mkdir();
   }
}
