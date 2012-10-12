package net.medcommons.modules.filestore.test;

import java.io.*;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.modules.filestore.*;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.test.interfaces.ResourceNames;


import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Used by unit tests that exercise the repository
 * 
 * @author ssadedin
 */
public class RepositoryBase  extends TestCase implements ResourceNames {
    
    final static String DEFAULT_SIMPLE_PROPERTIES = "SimpleRepositoryConfiguration.properties";
    final static String BUILD_DIRECTORY = "etc/configurations";
    
	
	Repository repository = null;
	
	protected File resources;
	protected File scratch;
	
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("RepositoryBase");
	
	
	private static String CONFIG_FILE = "conf/SimpleRepositoryConfiguration.properties";
	
	Properties properties = new Properties();
	public RepositoryBase(String s){
		super(s);
	}
	
	public void setUp(){
		try {
		
			log.info("user dir" + System.getProperty("user.dir"));
			log.info("user.home" + System.getProperty("user.home"));
			
			// Create simple respository
			repository = createRepository(null, CONFIG_FILE);
			resources = FileUtils.resourceDirectory();
			log.info("Resource dir:" + resources.getAbsolutePath());
			System.out.println("Resource dir:" + resources.getAbsolutePath());
			
			assertTrue("Missing resource directory " + resources.getAbsolutePath(), resources.exists());
			
			File propFile = new File(resources, "Junit_test.properties");
			
			log.info("Property files:" + propFile.getAbsolutePath());
			System.out.println("Property files:" + propFile.getAbsolutePath());
			assertTrue("Missing property file " + propFile.getAbsolutePath(), propFile.exists());
			
			
			FileInputStream in = new FileInputStream(propFile);
			assertNotNull("InputStream for properties is null", in);
			properties.load(in);
		} catch (Exception e) {
			System.out.println("Exception initializing repository");
			e.printStackTrace(System.out);
			throw new RuntimeException("Exception initializing repository", e);
		}
	}
	
	public static Repository createRepository(String repositoryClass, String propertyFileLocation) throws IOException,
	IllegalAccessException, InstantiationException, ClassNotFoundException{
	    
	    Repository repository = null;
	    if (repositoryClass == null){
	        repository =new SimpleRepository();
	        String propLocation = propertyFileLocation;
	        if(propLocation == null) {
	            // This is for testing within Tomcat of the basic repository API.
	            propLocation = "conf/"+DEFAULT_SIMPLE_PROPERTIES;
	        }
	        
	        Properties props = loadProperties(propLocation);
	        repository.init(props);
	    }
	    else{
	        repository = (Repository) (Class.forName(repositoryClass)).newInstance();
	        Properties props = loadProperties(propertyFileLocation);
	        repository.init(props);
	    }
	    return(repository);
	}
	
	private static Properties loadProperties(String filename) throws IOException{
	    System.out.println("Loading properties for " + filename);
	    File f = new File(filename);
	    if (!f.exists()){
	        File buildDir = new File(BUILD_DIRECTORY);
	        System.out.println("Going to try build directory " + buildDir.getAbsolutePath());
	        if(!buildDir.exists())
	            throw new FileNotFoundException("Directory for configuration file is missing:\n" +
	                    f.getAbsolutePath());
	        
	        File f2 = new File(buildDir, filename);
	        if(!f2.exists())
	            throw new FileNotFoundException("Configuration file for repository is missing:\n" +
	                    f.getAbsolutePath() + ",\n" +
	                    f2.getAbsolutePath());
	        f = f2;
	    }
	    
	    FileInputStream in = new FileInputStream(f);
	    try {
	        Properties props = new Properties();
	        props.load(in);
	        return props;
	    }
	    finally {
	        IOUtils.closeQuietly(in);
	    }
	}
}
