package net.medcommons.modules.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.medcommons.modules.utils.test.ResourceNames;



public class FileUtils implements ResourceNames {


	/**
	 * Returns the total size of the files for a given GUID. This is the sum of all of the data files <i>excluding</i>
	 * the property and metadata files.
	 *
	 * @param dir
	 * @param fileSuffix
	 * @param propertyName
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static long calculateDirectorySize(File dir, String fileSuffix, String propertyName) throws IOException{
		long directorySize = 0;
		FilenameFileFilter filter = new FilenameFileFilter();

		filter.setFilenameFilter(fileSuffix) ;

		File[] files = dir.listFiles(filter);
		if ((files == null)|| (files.length==0))
			throw new IOException("No property files found in directory:" + dir.getAbsolutePath());
		List<File> lFiles = Arrays.asList(files);

		Properties props = new Properties();
		for (int i=0;i<files.length;i++){
			File f = lFiles.get(i);
			InputStream is = new FileInputStream(f);
			props.clear();
			props.load(is);
			String sfileLength = props.getProperty(propertyName);
			if ((sfileLength == null) || ("".equals(sfileLength)))
					throw new IllegalStateException("Property file " +
							f.getAbsolutePath() + " is missing property '"
							+ propertyName + "'");
			long fileLength = Long.parseLong(sfileLength.trim());
			directorySize += fileLength;
			is.close();

		}

		return(directorySize);

	}
	/**
	 * Returns a File reference to a test resource file.
	 * <P>
	 * This provides a standard mechanism for the junit tests to access the files in the
	 * checked-in test set.
	 *
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 */

	public static File getTestResourceFile(String filename) throws FileNotFoundException {



		File dir =  resourceDirectory();



		if (!dir.exists()) {
			throw new FileNotFoundException("Directory not found:"
					+ dir.getAbsolutePath());
		}
		File imageFile = new File(dir, filename);
		if (!imageFile.exists()) {
			throw new FileNotFoundException("Image not found:"
					+ imageFile.getAbsolutePath());
		}
		return (imageFile);
	}
	public static File resourceDirectory() throws FileNotFoundException{

		File deployedParent = new File (System.getProperty("user.dir"));
		File resourceDir = new File(deployedParent, TEST_RESOURCE_DIR);
		if (!resourceDir.exists())
			{
			File parentDir = new File(TEST_RESOURCE_STANDLONE_PARENT);
			if (!parentDir.exists())
				throw new FileNotFoundException("Missing directory for resources:" + parentDir.getAbsolutePath());
			resourceDir = new File(parentDir, TEST_RESOURCE_DIR);
			if (!resourceDir.exists())
				throw new FileNotFoundException("Missing directory for resources:" + resourceDir.getAbsolutePath());

		}
		return(resourceDir);

	}
	public static File[] propertyFiles(File directory){
		FilenameFileFilter filter = new FilenameFileFilter();
		filter.setFilenameFilter(".properties");
		File[] files = directory.listFiles(filter);
		return(files);
	}

	/**
	 * Simple file copy.
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyFile(File in, File out) throws IOException{

		  FileInputStream fis =
              new FileInputStream(in);
			FileOutputStream fos =
			             new FileOutputStream(out);
			FileChannel fcin = fis.getChannel();
			FileChannel fcout = fos.getChannel();

			// do the file copy

			fcin.transferTo(0, fcin.size(), fcout);

			// finish up

			fcin.close();
			fcout.close();
			fis.close();
			fos.close();
	}

	/**
	 * Returns a list of all files in a given directory.
	 * Transverses the directory recursively.
	 * @param directory
	 * @return
	 */
	public static List<File> getFilesInDirectory(File directory){
		List<File> allFiles = new ArrayList<File>();
		if (!directory.isDirectory())
			return(allFiles);
		else{
			File files[] = directory.listFiles();
			for (int i=0;i<files.length;i++){
				if (files[i].isFile()){
					allFiles.add(files[i]);
				}
				else if (files[i].isDirectory()){
					List<File> subdir = getFilesInDirectory(files[i]);
					allFiles.addAll(subdir);

				}
			}

		}
		return(allFiles);
	}
	 /**
	 * Returns a list of all files in a given directory with a given suffix.
	 * Transverses the directory recursively.
	 * @param directory
	 * @return
	 */
	public static List<File> getFilesInDirectory(File directory, String suffix){
		List<File> allFiles = new ArrayList<File>();
		if (!directory.isDirectory())
			return(allFiles);
		else{
			//FilenameFileFilter filter = new FilenameFileFilter();

			//filter.setFilenameFilter(suffix);
			File files[] = directory.listFiles();

			for (int i=0;i<files.length;i++){
				if (files[i].isFile()){
					String filename = files[i].getName();
					if (filename.indexOf(suffix) != -1){
						allFiles.add(files[i]);
					}
				}
				else if (files[i].isDirectory()){
					List<File> subdir = getFilesInDirectory(files[i], suffix);
					allFiles.addAll(subdir);

				}
			}

		}
		return(allFiles);
	}


	 /**
	  * Returns true if the exception was (ultimately) caused by a CancelledStreamException,
	  * false otherwise.
	  * @param thrown
	  * @return
	  */
	public static boolean isStreamCancelled(Throwable thrown){
		 boolean isCancelled = false;
		 if (thrown != null){
			 if (thrown instanceof CancelledStreamException){
				 isCancelled = true;
			 }
			 else{
				 isCancelled = isStreamCancelled(thrown.getCause());
			 }
		 }
		 return(isCancelled);
	 }

	/**
	 * Recursive directory deletion
	 * @param dir
	 * @return
	 */
	  public static boolean deleteDir(File dir) {
	        if (dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i = 0; i < children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
	        }

	        // The directory is now empty so delete it
	        return dir.delete();
	    }
	  
	  public static void writeFile(File f, String contents) throws IOException {
	      if(!f.getParentFile().exists()) 
	          f.getParentFile().mkdirs();
	      
	        FileWriter out = new FileWriter(f);
	        out.write(contents);
	        out.close();
	  } 
	  
	  public static String readFile(File f) throws IOException {
	      FileReader r = new FileReader(f);
	      try {
	          char[] buffer = new char[80];
	          int i = r.read(buffer);
	          return new String(buffer, 0, i);
	      } 
	      finally {
	          r.close();
	      }
	  }
	  
	  public static String readStream(InputStream s) throws IOException {
	      StringBuilder result = new StringBuilder();
	      try {
	          byte[]  buffer = new byte[2048];
	          int read = 0;
	          while((read = s.read(buffer)) > 0) {
	              result.append(new String(buffer, 0, read));
	          }
	          return result.toString();
	      }
	      finally {
		      s.close();
	      }
	  }
	  
      public static byte [] readBytes(InputStream s) throws IOException {
          
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          try {
              byte[]  buffer = new byte[2048];
              int read = 0;
              while((read = s.read(buffer)) > 0) {
                  out.write(buffer,0,read);
              }
              return out.toByteArray();
          }
          finally {
              s.close();
          }
      }
	      
	  /**
	   * Read given file into a byte array
	   */
	  public static byte[] readBytes(File f) throws IOException {
	      byte [] buffer = new byte[(int)f.length()];
	      InputStream i = new FileInputStream(f);
	      try {
	          i.read(buffer);
	          return buffer;
	      } 
	      finally {
	          i.close();
	      }
	  }
		  
}
