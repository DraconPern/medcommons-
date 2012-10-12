package net.medcommons.modules.crypto.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.utils.FilenameFileFilter;

public class FileGuid {


	/**
	 * Calculates the SHA-1 hash of a given file.
	 * <P>
	 * Useful for many routines test routines that want to test the value of the
	 * SHA-1 hash returned by the repository.
	 *
	 * @param f
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateFileGuid(File f)
			throws NoSuchAlgorithmException, IOException {

		SHA1 sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();

		return (sha1.calculateFileHash(f));
	}

	/**
	 * Returns the SHA1 hash of the ordered SHA-1 hashes of the objects in a directory.
	 * The SHA-1 hashes are assumed to be in a properties files.
	 * @param dir
	 * @param fileSuffix
	 * @param propertyFilename
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String calculateDirectorySHA1(File dir, String fileSuffix, String propertyName) throws NoSuchAlgorithmException,IOException{
		String dirSha1 = null;
		FilenameFileFilter filter = new FilenameFileFilter();

		filter.setFilenameFilter(fileSuffix) ;

		File[] files = dir.listFiles(filter);
		if ((files == null)|| (files.length==0))
			throw new IOException("No property files found in directory:" + dir.getAbsolutePath());
		List<File> lFiles = Arrays.asList(files);

		Collections.sort(lFiles);


		String sha1s[] = new String[files.length];
		Properties props = new Properties();
		for (int i=0;i<files.length;i++){
			File f = lFiles.get(i);
			InputStream is = new FileInputStream(f);
			props.clear();
			props.load(is);
			String fileHash = props.getProperty(propertyName);
			if ((fileHash == null) || ("".equals(fileHash)))
					throw new IllegalStateException("Property file " +
							f.getAbsolutePath() + " is missing property '"
							+ propertyName + "'");
			sha1s[i] = fileHash;
			is.close();

		}
		List<String> sha1Values = Arrays.asList(sha1s);
		Collections.sort(sha1Values);
		String sortdSha1s[] = sha1Values.toArray(sha1s);


		SHA1 sha1 = new SHA1();

		sha1.initializeHashStreamCalculation();
		dirSha1 = sha1.calculateStringNameHash(sortdSha1s);
		return(dirSha1);

	}


}
