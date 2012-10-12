/*
 * $Id$
 * Created on 28/03/2007
 */
package net.medcommons.phr;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

public class BaseTest extends TestCase {
    
    public BaseTest() {
        BasicConfigurator.configure();
        System.setProperty("medcommons.spring.config.path", "test-data/config.xml");
    }
    
    public BaseTest(String name) {
        super(name);

        BasicConfigurator.configure();
        System.setProperty("medcommons.spring.config.path", "test-data/config.xml");
    }

}
