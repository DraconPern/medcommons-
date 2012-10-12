package net.medcommons.modules.crypto.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.crypto.io.SHA1InputStream;
import net.medcommons.modules.crypto.io.SHA1OutputStream;
import net.medcommons.modules.utils.FileUtils;

/**
 * Some tests of SHA1 I/O.
 * 
 * Note that the SHA1OutputStream fails and has been removed from the test. See class for details.
 * @author mesozoic
 */
public class IOTest extends CryptoBase {
	private static Logger log = Logger.getLogger("IOTest");

	public void testIOFilters() throws Exception {
		String testFilename = "Document.SingleDICOMFile";
		String scratchFilename = "ScratchFileJUNIT";
		String filename = properties.getProperty(testFilename);
		String guid = properties.getProperty(testFilename + ".GUID");
		if (filename == null)
			throw new NullPointerException("Missing property for "
					+ testFilename);

		File f = FileUtils.getTestResourceFile(filename);
		if (!f.exists())
			throw new FileNotFoundException(
					"Logical file Document.SingleDICOMFile, filename = "
							+ f.getAbsolutePath());
		SHA1 sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();
		String inputGuid = sha1.calculateFileHash(f);
		FileInputStream in = new FileInputStream(f);
		SHA1InputStream sha1InputStream = new SHA1InputStream(in);

		File outputFile = new File(scratchFilename);
		FileOutputStream out = new FileOutputStream(outputFile);
		SHA1OutputStream sha1OutputStream = new SHA1OutputStream(out);
		log.info("Calculated hash from file= " + inputGuid);
		log.info("Stored hash value        = " + guid);
		String outputGuid = null;
		long outputLength = 0;
		try {
			byte[] buffer = new byte[64 * 1024];
			int bytesRead;

			while ((bytesRead = sha1InputStream.read(buffer)) != -1) {
				
				sha1OutputStream.write(buffer, 0, bytesRead); // write
			}
			
		} 
		catch(Throwable e){
			log.error("Error writing file " + outputFile.getAbsolutePath(), e);
			throw new Exception(e);
		}
		finally {
			if (sha1InputStream != null)
				try {
					sha1InputStream.close();
				} catch (IOException e) {
					;
				}
			if (sha1OutputStream != null)
				try {
					sha1OutputStream.close();
				} catch (IOException e) {
					;
				}
			SHA1 sha1Out = new SHA1();
			sha1Out.initializeHashStreamCalculation();
			outputGuid = sha1Out.calculateFileHash(outputFile);
			outputLength = outputFile.length();
			outputFile.delete();
		}
		String sha1InputGuid =  sha1InputStream.getHash();
		String sha1OutputGuid = sha1OutputStream.getHash();
		log.info("sha1InputStream hash     = " +sha1InputGuid);
		log.info("sha1OutputStream hash    = " + sha1OutputGuid);
		log.info("file hash of output file = " + outputGuid);
		log.info("sha1input  length = " + sha1InputStream.getLength());
		log.info("sha1output length = " + sha1OutputStream.getLength());
		log.info("File length       = " + f.length());
		assertEquals("SHA1InputStream output does not match known value",
				guid, sha1InputGuid
				);
		assertEquals("SHA1OutputStream output does not match known value", 
				guid, sha1OutputGuid
				);
		assertEquals("SHA1.calculateFileHash(output) does not match known value",
				guid, outputGuid
				);
		assertEquals("SHA1.calculateFileHash(input) does not match known value",
				guid, inputGuid
				);
		assertEquals("File lenght mismatch", f.length(), outputLength);

	}

}
