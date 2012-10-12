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
import org.apache.cactus.WebResponse;

/**
 * Test global client side <code>begin()</code> and <code>end()</code> 
 * methods.
 *
 * @version $Id: TestGlobalBeginEnd.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestGlobalBeginEnd extends ServletTestCase
{
    /**
     * true if <code>end()</code> has been called.
     */
    private boolean isClientGlobalEndCalled;

    /**
     * Verifies that <code>end()</code> has been called correctly.
     * 
     * @exception Throwable on test failure
     */
    protected void runTest() throws Throwable
    {
        super.runTest();

        // Make sure we verify if end() has been called only on
        // the client side. Reason is that the runTest() method is
        // called both on the client side and on the server side.
        if (this.request == null)
        {
            if (!this.isClientGlobalEndCalled)
            {
                fail("end() has not been called");
            }
        }
    }

    /**
     * Verify that it is possible to modify the <code>WebRequest</code> in
     * the common <code>begin()</code> method. It also verifies that
     * <code>begin()</code> is called at all.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void begin(WebRequest theRequest)
    {
        theRequest.addParameter("param1", "value1");
    }

    /**
     * Verify that it is possible to read the connection object once in
     * endXXX() and then again in <code>end()</code>. It also
     * verifies that <code>end()</code> is called at all.
     *
     * @param theResponse the response from the server side.
     */
    public void end(WebResponse theResponse)
    {
        assertEquals("Hello there!", theResponse.getText());
        this.isClientGlobalEndCalled = true;
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to modify the <code>WebRequest</code> in
     * the common <code>begin()()</code> method. It also verifies that
     * <code>begin()</code> is called at all.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGlobalBeginEnd(WebRequest theRequest)
    {
        assertEquals("value1", theRequest.getParameterGet("param1"));
    }

    /**
     * Verify that it is possible to modify the <code>WebRequest</code> in
     * the common <code>begin()()</code> method. It also verifies that
     * <code>begin()()</code> is called at all.
     * 
     * @exception Exception on test failure
     */
    public void testGlobalBeginEnd() throws Exception
    {
        assertEquals("value1", request.getParameter("param1"));
        response.getWriter().print("Hello there!");
    }

    /**
     * Verify that it is possible to read the connection object once in
     * endXXX() and then again in <code>end()</code>. It also
     * verifies that <code>end()</code> is called at all.
     *
     * @param theResponse the response from the server side.
     */
    public void endGlobalBeginEnd(WebResponse theResponse)
    {
        assertEquals("Hello there!", theResponse.getText());
    }

}
