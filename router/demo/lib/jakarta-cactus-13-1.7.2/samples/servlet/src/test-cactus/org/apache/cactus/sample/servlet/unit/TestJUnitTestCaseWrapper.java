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

import org.apache.cactus.ServletTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Pure JUnit Test Case that we run on the server side using Cactus, by
 * using a {@link ServletTestSuite}.
 *
 * @version $Id: TestJUnitTestCaseWrapper.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestJUnitTestCaseWrapper extends TestCase
{
    /**
     * Used to verify that the setUp method does get called.
     */
    private boolean isSetUpCalled;

    /**
     * Used to verify that the testXXX method does get called.
     */
    private boolean isTestXXXCalled;
    
    /**
     * Runs this pure JUnit Test Case with Cactus, wrapping it in
     * a Servlet Test Case.
     * 
     * @return the test suite containing all tests to run
     */
    public static Test suite()
    {
        ServletTestSuite suite = new ServletTestSuite();
        suite.addTestSuite(TestJUnitTestCaseWrapper.class);
        return suite;
    }

    //-------------------------------------------------------------------------

    /**
     * No-op test just to verify that pure JUnit tests can be executed on the 
     * server side using Cactus. 
     */
    public void setUp()
    {
        this.isSetUpCalled = true;
    }
    
    /**
     * No-op test just to verify that pure JUnit tests can be executed on the 
     * server side using Cactus. 
     */
    public void testXXX()
    {
        assertTrue("setUp() should have been called", this.isSetUpCalled);
        this.isTestXXXCalled = true;
    }

    /**
     * No-op test just to verify that pure JUnit tests can be executed on the 
     * server side using Cactus. 
     */
    public void tearDown()
    {
        assertTrue("testXXX() should have been called", this.isTestXXXCalled);
    }

}
