/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.router.selftest;

import net.medcommons.modules.services.interfaces.ServicesFactory;

public interface SelfTest {
    
    /**
     * Executes this test and returns a SelfTestResult describing the outcome
     * or null if the test is successful and no information is necessary.
     * 
     * @param services - a ServicesFactory created for convenience under the
     *                   Test Data Doctor1 authority.
     * @return null, or a SelfTestResult if there is something interesting to report.
     * @throws Exception - for any failure.  Implementations should not catch exceptions.
     */
    public SelfTestResult execute(ServicesFactory services) throws Exception;
    
}
