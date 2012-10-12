/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * Run all the Cactus unit tests related to J2EE API 1.3.
 *
 * @version $Id: TestAll.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestAll
{
    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(
            "Cactus unit tests for J2EE 1.3");

        // Add shared tests
        suite.addTest(TestShareAll.suite());

        // Test cases specific to J2EE 1.3 only
        suite.addTestSuite(TestHttpRequestSpecific.class);
        suite.addTestSuite(TestJspTagLifecycle.class);
        suite.addTestSuite(TestFilterHttpHeaders.class);
        suite.addTestSuite(TestSetURLSpecific.class);

        return suite;
    }
}
