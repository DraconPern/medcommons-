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
package org.apache.cactus.sample.jetty;

import org.apache.cactus.extension.jetty.JettyTestSetup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Run all tests inside the Jetty container.
 *
 * @version $Id: TestJettyAll.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestJettyAll extends TestCase
{
    /**
     * @return a <code>JettyTestSetup</code> test suite that wraps all our
     *         tests so that Jetty will be started before the tests execute
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(
            "Cactus unit tests executing in Jetty");

        // Functional tests
         suite.addTestSuite(
             org.apache.cactus.sample.servlet.TestSampleServlet.class);
         suite.addTestSuite(
             org.apache.cactus.sample.servlet.TestSampleServletConfig.class);
         suite.addTestSuite(
             org.apache.cactus.sample.servlet.TestSampleTag.class);
         suite.addTestSuite(
             org.apache.cactus.sample.servlet.TestSampleBodyTag.class);
         suite.addTestSuite(
             org.apache.cactus.sample.servlet.TestSampleFilter.class);

        return new JettyTestSetup(suite);
    }
}
