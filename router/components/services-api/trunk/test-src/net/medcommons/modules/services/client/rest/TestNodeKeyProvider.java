package net.medcommons.modules.services.client.rest;

import java.io.FileInputStream;
import java.util.Properties;

import net.medcommons.modules.services.interfaces.NodeKeyProvider;
import net.medcommons.modules.services.interfaces.ServiceException;

public class TestNodeKeyProvider implements NodeKeyProvider {
    
    static Properties nodeKey = new Properties();
    
    static {
        try {
            nodeKey.load(new FileInputStream("data/node_key.properties"));
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to load node key");
        }
    }

    @Override
    public String getNodeKey() throws ServiceException {
        return nodeKey.getProperty("key");
    }

}
