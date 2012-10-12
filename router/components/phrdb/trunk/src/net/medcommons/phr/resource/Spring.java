/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr.resource;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public class Spring {
    
    private static final String DEFAULT_SPRING_CONFIG_PATH = "conf/medcommons-config.xml";

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(Spring.class);
    
    /**
     * Spring factory used to resolve beans.  We attempt to look this up via JNDI.
     * 
     * If not there, we create one from a FileSystemResource on the file conf/config.xml
     */
    static XmlBeanFactory beanFactory = null;
    
    /**
     * Retrieve the requested bean, requested via Spring
     */
    public static <T> T getBean(String id) {
        if(beanFactory == null) {
            beanFactory = new XmlBeanFactory(new FileSystemResource(new File(System.getProperty("medcommons.spring.config.path", DEFAULT_SPRING_CONFIG_PATH))));
        }
        return (T) beanFactory.getBean(id);
    }
}
