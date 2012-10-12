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

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Test that it is possible to override a servlet redirector as defined in
 * <code>cactus.properties</code> on a per test case basis.
 *
 * @version $Id: TestServletRedirectorOverride.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestServletRedirectorOverride extends ServletTestCase
{
    /**
     * Verify that it is possible to override the default redirector.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginRedirectorOverride1(WebRequest theRequest)
    {
        theRequest.setRedirectorName("ServletRedirectorOverride");
    }

    /**
     * Verify that it is possible to override the default redirector.
     */
    public void testRedirectorOverride1()
    {
        assertEquals("value2 used for testing", 
            config.getInitParameter("param2"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to set back the original redirector
     * again.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginRedirectorOverride2(WebRequest theRequest)
    {
        theRequest.setRedirectorName("ServletRedirector");
    }

    /**
     * Verify that it is possible to set back the original redirector
     * again.
     */
    public void testRedirectorOverride2()
    {
        assertEquals("value1 used for testing", 
            config.getInitParameter("param1"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that when no redirector is overriden the default redirector
     * is the expected one.
     */
    public void testRedirectorOverride3()
    {
        assertEquals("value1 used for testing", 
            config.getInitParameter("param1"));
    }
}
