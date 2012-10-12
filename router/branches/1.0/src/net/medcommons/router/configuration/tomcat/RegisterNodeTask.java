/*
 * $Id$
 * Created on 21/10/2005
 */
package net.medcommons.router.configuration.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimerTask;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.services.client.rest.NodeServiceProxy;
import net.medcommons.modules.services.interfaces.NodeKeyProvider;
import net.medcommons.modules.services.interfaces.NodeService;
import net.medcommons.modules.services.interfaces.ServiceException;

import org.apache.log4j.Logger;

public class RegisterNodeTask extends TimerTask implements NodeKeyProvider {
    
    private static final String DATA_NODE_KEY_PROPERTIES = "data/node_key.properties";
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RegisterNodeTask.class);
    
    SHA1 sha1 = new SHA1().initializeHashStreamCalculation();
    
    /**
     * Cached version of node key - stored after first loaded on startup
     */
    private static String cachedNodeKey;

    @Override
    public void run() {
        NodeService nodeService = new NodeServiceProxy(null);
        try {
	        String host = Configuration.getProperty("RemoteAccessAddress");        
	        String nodeId = Configuration.getProperty("NodeID");        
            log.info("Registering node as type gw ip-address=" + host);
            
            // Try to read the key
            String key = getNodeKey();
            
            nodeService.registerNode("gw",nodeId, host, key);
        }
        catch(ServiceException e) {
            log.warn("Unable to register node.  Services may be refused or operate incorrectly",e);
        }
        catch (ConfigurationException e) {
            log.warn("Unable to register node.  Services may be refused or operate incorrectly",e);
        }
    }

    public String getNodeKey() throws ServiceException {
        
        if(cachedNodeKey != null)
            return cachedNodeKey;
        
        try {
            String key = null;
            Properties keyProperties = new Properties();
            File f = new File(DATA_NODE_KEY_PROPERTIES);
            if(!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            
            if(f.exists()) {
                keyProperties.load(new FileInputStream(DATA_NODE_KEY_PROPERTIES));
                key = keyProperties.getProperty("key");
                log.info("Loaded existing node key " + key + " from " + DATA_NODE_KEY_PROPERTIES);
            }
            else {
                String host = Configuration.getProperty("RemoteAccessAddress");        
                String keyBase = System.currentTimeMillis() + "-gateway-node-key-" + Math.random() + "-" + host;
                key = sha1.calculateByteHash(keyBase.getBytes());
                log.info("Generated new node key " + key);
                keyProperties.setProperty("key", key);
                keyProperties.store(new FileOutputStream(DATA_NODE_KEY_PROPERTIES), "MedCommons Gateway Node Registration Key");
                log.info("Saved new node key to file " + DATA_NODE_KEY_PROPERTIES);
            }
            cachedNodeKey = key;
            return key;
        }
        catch (FileNotFoundException e) {
            throw new ServiceException("Failed to load / create node key",e);
        }
        catch (IOException e) {
            throw new ServiceException("Failed to load / create node key",e);
        }
        catch (ConfigurationException e) {
            throw new ServiceException("Failed to load / create node key",e);
        }
    }    
}
