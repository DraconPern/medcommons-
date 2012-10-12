package net.medcommons.modules.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.test.ResourceNames;

public class ConfigurationUtils implements ResourceNames {
	/**
	 * Logger to use with this class
	 */

	private static Logger log = Logger.getLogger("ConfigurationUtils");

	public static void initConfiguration() throws Exception{

			log.info("Configuration path is " + CONFIGURATION_CONFIGPATH);

			/*
			 * Initialize the configuration class in standlone (e.g., non-JNDI)
			 * mode.
			 */
			boolean standalone = true;
			Configuration config = new Configuration(standalone);

			File configFile = new File(CONFIGURATION_CONFIGPATH);
			if (!configFile.exists()){
				throw new FileNotFoundException(configFile.getAbsolutePath());
			}

			Configuration.load(CONFIGURATION_CONFIGPATH,
					MEDCOMMONS_BOOT_PARAMETERS_PATH, DEFAULT_SPRING_CONFIG_PATH);



	}
}
