package net.medcommons.modules.configuration.test;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationUtils;
import net.medcommons.modules.utils.test.ResourceNames;


import org.apache.log4j.Logger;

public class ConfigurationTest extends TestCase implements ResourceNames {

	//Repository repository = null;

	protected File resources;

	protected File scratch;

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("ConfigurationTest");



	public void setUp() throws Exception{
		ConfigurationUtils.initConfiguration();
	}

	/**
	 * Very simple test to see if the NodeID can be retrieved and if it is an integer.
	 * If this fails nothing else will really work.
	 *
	 * The NodeID is defined in MedCommonsBootParameters.properties as 'UNKNOWN';
	 * it is overridden in the LocalBootParameters.properties file with an integer
	 * value.
	 * @throws Exception
	 */
	public void testGetNodename() throws Exception {
		String nodeIdentity = Configuration.getProperty("NodeID");
		assertNotNull("Node identity is null", nodeIdentity);
		log.info("Node identity is " + nodeIdentity);
		assertNotSame("Value of NodeID is 'UNKNOWN', must have integer value",
				"UNKNOWN", nodeIdentity);

		int node = -1;
		try {
			node = Integer.parseInt(nodeIdentity);
		} catch (Exception e) {
			log.error("NodeID is not an integer: '" + nodeIdentity + "'");
		}
		assertNotSame("NodeID is not an integer integer: '" + nodeIdentity
				+ "'", -1, node);

	}

    /**
     * Tests that ${} references within the config properties file are
     * properly substituted.
     */
    public void testSubsitute() throws Exception {
        Configuration.getAllProperties().setProperty("foo", "bar");
        Configuration.getAllProperties().setProperty("fu", "fubar");
        assertEquals("the bar is fubarred", Configuration.substitute("the ${foo} is ${fu}red"));
    }
}