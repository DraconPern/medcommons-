/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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
 * Test the J2EE 1.3 specifics of the {@link WebRequest#setURL} method
 * (specifically verify calls to <code>getRequestURL</code>).
 *
 * @version $Id: TestSetURLSpecific.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSetURLSpecific extends ServletTestCase
{
    /**
     * Verify that when <code>setURL()</code> is called with a null
     * pathinfo parameter, the call to <code>getRequestURL</code> works
     * properly.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURLGetRequestURLWhenNull(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "", "/test/test.jsp", null, 
            null);
    }

    /**
     * Verify that when <code>setURL()</code> is called with a null
     * pathinfo parameter, the call to <code>getRequestURL</code> works
     * properly.
     */
    public void testSimulatedURLGetRequestURLWhenNull()
    {
        assertEquals("http://jakarta.apache.org:80/test/test.jsp", 
            request.getRequestURL().toString());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that when <code>setURL()</code> is called with a not null
     * pathinfo parameter, the call to <code>getRequestURL</code> works
     * properly.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURLGetRequestURLWhenNotNull(
        WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", "/lawn", 
                "/index.html", null);
    }

    /**
     * Verify that when <code>setURL()</code> is called with a not null
     * pathinfo parameter, the call to <code>getRequestURL</code> works
     * properly.
     */
    public void testSimulatedURLGetRequestURLWhenNotNull()
    {
        assertEquals("http://jakarta.apache.org:80/catalog/lawn/index.html", 
            request.getRequestURL().toString());
    }
}
