/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.apache.cactus.sample.servlet.unit;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite containing all test cases that should be run on all J2EE 
 * APIs.
 *
 * @version $Id: TestShareAll.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public abstract class TestShareAll
{
    /**
     * @return a test suite (<code>TestSuite</code>) that includes all shared
     *          tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(
            "Cactus unit tests for all J2EE APIs");

        // Note: This test needs to run first. See the comments in the
        // test class for more information on why
        suite.addTestSuite(TestClientServerSynchronization.class);

        // Lifecycle tests
        suite.addTestSuite(TestGlobalBeginEnd.class);

        // ServletTestCase related tests
        suite.addTestSuite(TestServerSideExceptions.class);
        suite.addTestSuite(TestSetUpTearDown.class);
        suite.addTestSuite(TestSetURL.class);
        suite.addTestSuite(TestTearDownException.class);
        suite.addTestSuite(TestBasicAuthentication.class);
        suite.addTestSuite(TestHttpUnitIntegration.class);
        suite.addTestSuite(TestServletRedirectorOverride.class);
        suite.addTestSuite(TestHttpParameters.class);
        suite.addTestSuite(TestHttpSession.class);
        suite.addTestSuite(TestHttpResponse.class);
        suite.addTestSuite(TestCookie.class);
        suite.addTestSuite(TestRequestDispatcher.class);
        suite.addTestSuite(TestHttpHeaders.class);
        suite.addTestSuite(TestHttpRequest.class);
        suite.addTestSuite(TestServletConfig.class);
        suite.addTestSuite(TestServletContext.class);
        suite.addTest(TestJUnitTestCaseWrapper.suite());
        
        // JspTestCase related tests
        suite.addTestSuite(TestJspOut.class);
        suite.addTestSuite(TestJspPageContext.class);

        return suite;
    }
}
