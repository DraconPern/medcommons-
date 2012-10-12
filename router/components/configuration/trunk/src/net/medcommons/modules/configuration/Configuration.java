/*
 * $Id: Configuration.java 1170 2006-05-05 13:56:24Z ssadedin $
 */

package net.medcommons.modules.configuration;

import static net.medcommons.modules.utils.Str.nvl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.medcommons.modules.utils.Str;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * A base utility class that brings together all the various sources of configuration
 * for the MedCommons Gateway.
 * <p/>
 * Properties are loaded from MedCommonsBootParameters.properties which may be overridden by
 * a local file LocalBootParameters.properties if it exists.
 * <p/>
 * Spring configuration is loaded from conf/medcommons-config.xml, but that location may be
 * overriden firstly by the System property "medcommons.spring.config.path" and secondly
 * by the setting inside either of the BootParameters properties files "spring.config.path".
 * <i>Note: the system property overrides all other settings.</p>
 *
 * @author ssadedin
 */
public class Configuration {

    private HashMap configMap;

    private static Logger log = Logger.getLogger(Configuration.class);

    public static String jndiName = "medcommonsConfiguration";

    private static Properties configProperties;

    private static Configuration staticConfig = null;

    /**
     * Spring bean factory used to provide general configuration services
     */
    private static XmlBeanFactory beanFactory = null;

    private static String springConfigurationPath = "conf/medcommons-config.xml";
    
    /**
     * The path from which local boot parameters was loaded, if any
     */
    public static String loadedBootParametersPath = null;

    public Configuration() {
        configMap = new HashMap();
    }

    public Configuration(boolean standalone){
        this();
        if (standalone)
            staticConfig = this;
    }
    
    /**
     * This static config is used by unit tests which don't have access to JNDI.
     */
    private static Configuration testConfig;

    /**
     * Regex pattern used to parse variables from property files
     */
    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([\\w\\.]*)\\}");

    public static void load(String path, String propertiesPath, String springConfigPath) throws Exception {
        testConfig = new Configuration();
        
        if(path != null) {
            File configFile = new File(path);
            if (!configFile.exists()){
                throw new FileNotFoundException("Missing config file:" + configFile.getAbsolutePath());
            }
            if (!configFile.canRead()){
            	throw new RuntimeException("Config file can not be read (permissions?)" + configFile.getAbsolutePath());
            }
            FileInputStream inputStream = new FileInputStream(path);
            if (inputStream == null){
            	throw new NullPointerException("Null input stream attempting to read configuration file " + configFile.getAbsolutePath());
            }
            testConfig.loadConfig(new FileInputStream(path));
            loadProperties("", propertiesPath, false);
        }

        // Look for a LocalBootParameters.properties file
        File bp = new File(propertiesPath);
        if(bp.exists()) {
            File lbp = new File(bp.getParentFile(), "LocalBootParameters.properties");
            if(lbp.exists()) {
                loadProperties("", lbp.getAbsolutePath(), false);
                loadedBootParametersPath = lbp.getAbsolutePath();
            }
        }

        if(springConfigPath != null) {
            springConfigurationPath = springConfigPath;
        }
        else {
            // Look at system property first
            springConfigurationPath = System.getProperty("medcommons.spring.config.path", getProperty("spring.config.path",  springConfigurationPath));
        }
    }

    /**
     * Loads configuration properties from the given paths.  Spring configuration is
     * is loaded from property inside the propertiesPath (MedCommonsBootParameters).
     *
     * @param path
     * @param propertiesPath
     */
    public static void load(String path, String propertiesPath) throws Exception {
        load(path,propertiesPath, null);
    }
    
    public static void reload() throws ConfigurationException {
        log.info("Reloading properties from " + loadedBootParametersPath);
        loadProperties("", loadedBootParametersPath, false);
    }

    public void loadConfig(InputStream is) throws Exception {
        log.debug("Configuring.");

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(is);

        Element root = document.getRootElement();
        List properties = root.getChildren("property");

        Iterator i = properties.iterator();
        while (i.hasNext()) {
            Element attribute = (Element) i.next();

            String name = attribute.getAttribute("name").getValue();
            String value = attribute.getTextTrim();

            configMap.put(name, value);

            //ssadedin: ridiculously verbose - very annoying when running tests
            //log.debug("Stored configured value: (name: " + name + ")(value: " + value + ")");
        }

        log.debug("Configuration complete.");
    }

    public Set keys() {
        return configMap.keySet();
    }

    public Object getConfiguredValue(String key) throws ConfigurationException {
        if (!configMap.containsKey(key)) {
            return null;
        }
        return configMap.get(key);
    }

    public String getConfiguredValue(String key, String defaultValue) {
        if (!configMap.containsKey(key)) {
            return defaultValue;
        }
        return (String) configMap.get(key);
    }

    public void setConfiguredValue(String key, Object value) {
        configMap.put(key, value);
    }

    public static Configuration getInstance() throws ConfigurationException {
        if (staticConfig != null)
            return(staticConfig);
        try {
            InitialContext initCtx = new InitialContext();
            Configuration config = (Configuration) initCtx.lookup(jndiName);
            return config;
        }
        catch (NamingException e) {
            if (testConfig != null) { // Only not null if load() called, eg. by
                                      // unit test.
                return testConfig;
            }
            // throw new ConfigurationException("Unable to locate configuration in JNDI under name " + jndiName, e);
            return new Configuration();
        }
    }

    /**
     * Loads a configuration parameter from the configuration properties file.
     * The values are cached and never reloaded. Note that this method is
     * different to the other methods in this class which load from the
     * config.xml.
     *
     * @param name
     * @return
     * @throws ConfigurationException
     */
    public static String getProperty(String name) throws ConfigurationException {
        if (configProperties == null) {
            log.info("Initializing config properties");
            String configRoot = nvl((String) Configuration.getInstance().getConfiguredValue("ConfigRoot"),"conf/");
            String bootConfiguration = 
                nvl((String) Configuration.getInstance().getConfiguredValue("MedCommonsBootParameters"),"MedCommonsBootParameters.properties");
            String configFilePath = configRoot + bootConfiguration;
            loadProperties(name, configFilePath, true);
            String localConfigPath = configRoot  + "LocalBootParameters.properties";
            loadProperties(name, localConfigPath, false); // false = no error when not found
        }
        String value = configProperties.getProperty(name);
       // log.info("value of " + name + " is " + value);
        if(value == null)
            return null;

        value = substitute(value);
        return value;
    }

    /**
     * Substitute values in a configuration string
     *
     * @param value
     * @return
     * @throws ConfigurationException
     */
    public static String substitute(String value) throws ConfigurationException {
        Matcher m = SUBSTITUTION_PATTERN.matcher(value);
        while(m.find()) {
            String sub = m.group(1);
            value = value.substring(0,m.start()) + getProperty(sub) + value.substring(m.end());
            // TODO: if reading the config file is ever a performance issue, we can avoid
            // re-matching - but we have to adjust the offsets to account for the fact that
            // 'value' has changed since the original matching was done
            m = SUBSTITUTION_PATTERN.matcher(value);
        }
        return value;
    }

    public static Properties getAllProperties() throws ConfigurationException {
        // Ensure that the properties are actually loaded
        getProperty("dummy");
        return configProperties;
    }

    /**
     * Loads a configuration parameter from the configuration properties file.
     * The values are cached and never reloaded. Note that this method is
     * different to the other methods in this class which load from the
     * config.xml.
     *
     * @param name
     * @return
     * @throws ConfigurationException
     */
    public static String getProperty(String name, String defaultValue) {
        try {
            String value = getProperty(name);
            if (value != null) {
                return value;
            }
        }
        catch (ConfigurationException e) {
            log.warn("Error while trying to read config value " + name + ".  Using default value (" + defaultValue + ")", e);
        }
        return defaultValue;
    }

    /**
     * @param name
     * @param configFilePath
     * @throws ConfigurationException
     */
    private static void loadProperties(String name, String configFilePath, boolean errorNotFound) throws ConfigurationException {
        log.info("Loading config properties from path " + configFilePath);
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            if(configProperties == null) {
                configProperties = new Properties();
            }
            FileInputStream in;
            try {
                in = new FileInputStream(configFile);
                configProperties.load(in);
            }
            catch (FileNotFoundException e) {
                if(errorNotFound) {
                    throw new ConfigurationException(
                        "Unable to read configuration value " + name + " from file "
                        + configFilePath);
                }
            }
            catch (IOException e) {
                throw new ConfigurationException("Unable to read configuration value " + name + " from file "
                                + configFilePath);
            }
        }
        else {
            if(errorNotFound) {
                throw new ConfigurationException("Unable to find configuration file " + configFilePath);
            }
            else {
                log.info("Config file " + configFilePath + " not found.");
            }
        }
    }

    /**
     * Reads an integer property from the configuration file
     *
     * @param name -
     *            name of property to read
     * @param defaultValue -
     *            the default value, returned if the property is not defined.
     * @throws ConfigurationException
     */
    public static int getProperty(String name, int defaultValue) {
        try {
            String stringValue = getProperty(name);
            if (stringValue != null) {
                return Integer.parseInt(stringValue.trim());
            }
            else {
                return defaultValue;
            }
        }
        catch (ConfigurationException e) {
            log.warn("Unable to read configured value '" + name + "':" + e.toString());
            return defaultValue;
        }
    }

    /**
     * Reads an integer property from the configuration file
     *
     * @param name -
     *            name of property to read
     * @param defaultValue -
     *            the default value, returned if the property is not defined.
     * @throws ConfigurationException
     */
    public static boolean getProperty(String name, boolean defaultValue) {
        try {
            String stringValue = getProperty(name);
            if (stringValue != null) {
                return "true".equals(stringValue.trim());
            }
            else {
                return defaultValue;
            }
        }
        catch (ConfigurationException e) {
            log.warn("Unable to read configured value '" + name + "':" + e.toString());
            return defaultValue;
        }
    }

    /**
     * Retrieve object configured by Spring
     *
     * @param id
     * @return
     */
    public static <T> T getBean(String id) {
        if(beanFactory == null) {
            log.info("Spring configuration path is " + springConfigurationPath);
            System.setProperty("medcommons.spring.config.path", springConfigurationPath);
            beanFactory = new XmlBeanFactory(new FileSystemResource(new File(springConfigurationPath)));
        }
        return (T)beanFactory.getBean(id);
    }
    
    public static void resetBeans() {
        beanFactory = null;
    }

}