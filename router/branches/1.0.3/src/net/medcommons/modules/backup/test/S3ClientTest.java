package net.medcommons.modules.backup.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.s3.S3Client;
import net.medcommons.test.interfaces.ResourceNames;

public class S3ClientTest extends TestCase {

	String contentType = "application/octet-stream";
	
	protected String publicKeyId = "17877TPCB8NNDD5W7YG2";

	
	protected String privateKey = "NONE";// Need to store this somewhere..; 
	
	protected String bucket = "mesozoic.medcommons.net";
	
	protected File resources;
	
	protected File scratch;
	
	protected Properties properties = null;
	
	private void init() throws FileNotFoundException, IOException{
		if (properties == null){
			resources = FileUtils.resourceDirectory();
			if (!resources.exists())
				throw new FileNotFoundException(resources.getAbsolutePath());
			File propFile = new File(resources, "Junit_test.properties");
			if (!propFile.exists())
				throw new FileNotFoundException(propFile.getAbsolutePath());
			FileInputStream in = new FileInputStream(propFile);
			properties = new Properties();
			properties.load(in);
			
			String scratchDir = properties.getProperty(ResourceNames.ScratchDirectory);
		    scratch = new File(scratchDir);
			boolean isDeleted = scratch.delete();
			
			boolean isCreated = scratch.mkdirs();
		}
	}
	
	public void testPut() throws Exception{
		init();
		S3Client s3client = new S3Client(publicKeyId, privateKey);
		String fileKey = "Document.ccr";
		String fileToUpload = properties.getProperty(fileKey);
		String guid = properties.getProperty(fileKey + ".GUID");
		
		File f = new File(resources, fileToUpload);
		if (!f.exists()){
			throw new FileNotFoundException("File " + f.getAbsolutePath() + " not found");
		}
		byte[] hash = S3Client.md5(new java.io.FileInputStream(f));
		long contentLength = f.length();
		FileInputStream in = new FileInputStream(f);
		s3client.put(bucket, guid, hash, contentType, contentLength, in);
		
		/*
		try {
		    if (args.length == 0)
			usage();
		    else if (args.length == 3 && (args[0].equals("get"))) {
			S3Client s3client = new S3Client();

			s3client.get(args[1], args[2], System.out);
		    }
		    else if (args.length == 4 && (args[0].equals("put"))) {
			S3Client s3client = new S3Client(publicKeyId, privateKey);

			File f = new File(args[3]);

			byte[] hash = S3Client.md5(new java.io.FileInputStream(f));

			s3client.put(args[1], args[2], hash,
				     "application/octet-stream", f.length(),
				     new java.io.FileInputStream(f));
		    }

		    else
			usage();
		} catch (Exception ex) {
		    ex.printStackTrace(System.err);
		}
		*/
	}
	
	
	public void testGet() throws Exception{
		init();
		S3Client s3client = new S3Client(publicKeyId, privateKey);
		String fileKey = "Document.ccr";
		String fileToUpload = properties.getProperty(fileKey);
		String guid = properties.getProperty(fileKey + ".GUID");
		
		File f = new File(scratch, guid);
		
		
		FileOutputStream out = new FileOutputStream(f);
		
		s3client.get(bucket, guid, out);
		SHA1 sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();
		String incomingGuid = sha1.calculateFileHash(f);
		assertEquals(incomingGuid, guid);
		
		
	}
}
