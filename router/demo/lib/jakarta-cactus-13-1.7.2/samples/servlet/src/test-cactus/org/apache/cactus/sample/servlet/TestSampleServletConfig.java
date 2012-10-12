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
package org.apache.cactus.sample.servlet;

import javax.servlet.ServletException;

import org.apache.cactus.ServletTestCase;

/**
 * Tests of the <code>SampleServletConfig</code> servlet class. This to show
 * how servlet methods that makes calls to <code>getServletConfig()</code>,
 * <code>getServletContext()</code>, <code>log()</code>, ... (i.e. methods that
 * have been inherited from <code>GenericServlet</code>) can be unit-tested
 * with Cactus.
 *
 * @version $Id: TestSampleServletConfig.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSampleServletConfig extends ServletTestCase
{
    /**
     * Verify that we can unit test a servlet that makes calls to
     * <code>getServletConfig()</code>, <code>getServletContext()</code>,
     * <code>log()</code>, ... (i.e. methods inherited from
     * <code>GenericServlet</code>).
     * 
     * @exception ServletException on test failure
     */
    public void testServletConfig() throws ServletException
    {
        SampleServletConfig servlet = new SampleServletConfig();

        // VERY IMPORTANT : Call the init() method in order to initialize the
        //                  Servlet ServletConfig object.
        servlet.init(config);

        assertEquals("value1 used for testing", servlet.getConfigData());
    }
}
