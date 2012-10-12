/*
 * $Id: NodeKeyProvider.java 2556 2008-04-25 04:48:56Z ssadedin $
 * Created on 25/04/2008
 */
package net.medcommons.modules.services.interfaces;


/**
 * Interface for providing node key to gateway
 * 
 * @author ssadedin
 */
public interface NodeKeyProvider {
    String getNodeKey() throws ServiceException;
}
