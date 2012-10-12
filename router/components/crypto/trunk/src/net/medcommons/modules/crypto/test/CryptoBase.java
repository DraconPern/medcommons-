package net.medcommons.modules.crypto.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.test.interfaces.ResourceNames;

import org.apache.log4j.Logger;


/**
 * Base test class for JUnit tests in the crypto module.
 * Note that while the test program has a dependency on
 * the repository module that the net.medcommons.modules.crypto.* classes
 * do not; the repository is simply a way to get input/output streams.
 *
 * @author mesozoic
 *
 */
public class CryptoBase  extends TestCase implements ResourceNames {

	//Repository repository = null;


	public File scratchDir;


	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("CryptoBase");

	public Properties properties = new Properties();

	public void setUp(){
		try {
			// Create simple respository

			//repository = RepositoryFactory.create(null, "etc/jboss/router/conf/SimpleRepositoryConfiguration.properties");



			File propFile = FileUtils.getTestResourceFile("Junit_test.properties");

			assertTrue("Missing property file " + propFile.getAbsolutePath(), propFile.exists());


			FileInputStream in = new FileInputStream(propFile);
			assertNotNull("InputStream for properties is null", in);
			properties.load(in);

			String scratchDirectoryName = properties.getProperty(ScratchDirectory);
			scratchDir = new File(scratchDirectoryName);
			if (!scratchDir.exists()){
				boolean success = scratchDir.mkdirs();
				if (!success)
					throw new IOException("Unable to make scratch directory " + scratchDir.getAbsolutePath());
			}
			scratchDir = new File(scratchDirectoryName);

		} catch (Exception e) {
			System.out.println("Exception initializing repository");
			e.printStackTrace(System.out);
			throw new RuntimeException("Exception initializing repository", e);
		}


	}

}
