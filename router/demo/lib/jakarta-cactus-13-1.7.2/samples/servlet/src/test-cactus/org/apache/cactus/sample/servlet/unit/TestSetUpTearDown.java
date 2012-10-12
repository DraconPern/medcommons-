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
import org.apache.cactus.WebResponse;

/**
 * Test that <code>setUp()</code> and <code>tearDown()</code> methods are 
 * called and can access implicit objects in <code>ServletTestCase</code>.
 *
 * @version $Id: TestSetUpTearDown.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSetUpTearDown extends ServletTestCase
{
    /**
     * Put a value in the session to verify that this method is called prior
     * to the test, and that it can access servlet implicit objects.
     */
    protected void setUp()
    {
        session.setAttribute("setUpFlag", "a setUp test flag");
    }

    /**
     * Verify that <code>setUp()</code> has been called and that it put a
     * value in the session object.
     */
    public void testSetUp()
    {
        assertEquals("a setUp test flag", session.getAttribute("setUpFlag"));
    }

    //-------------------------------------------------------------------------

    /**
     * Set an HTTP response header to verify that this method is called after
     * the test, and that it can access servlet implicit objects.
     */
    protected void tearDown()
    {
        response.setHeader("Teardownheader", "tear down header");
    }

    /**
     * Verify that <code>tearDown()</code> has been called and that it created
     * an HTTP reponse header.
     */
    public void testTearDown()
    {
    }

    /**
     * Verify that <code>tearDown()</code> has been called and that it created
     * an HTTP reponse header.
     *
     * @param theResponse the HTTP connection that was used to call the
     *                    server redirector. It contains the returned HTTP
     *                    response.
     */
    public void endTearDown(WebResponse theResponse)
    {
        assertEquals("tear down header", 
            theResponse.getConnection().getHeaderField("Teardownheader"));
    }
}
