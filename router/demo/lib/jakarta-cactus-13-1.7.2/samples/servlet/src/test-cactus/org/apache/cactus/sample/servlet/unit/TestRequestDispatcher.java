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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests manipulating The Request Dispatcher.
 *
 * @version $Id: TestRequestDispatcher.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestRequestDispatcher extends ServletTestCase
{
    /**
     * Verify that getNamedDispatcher() can be used to get a dispatcher.
     * 
     * @exception IOException on test failure
     * @exception ServletException on test failure
     */
    public void testGetRequestDispatcherFromNamedDispatcherOK()
        throws ServletException, IOException
    {
        RequestDispatcher rd = config.getServletContext().getNamedDispatcher(
            "TestJsp");

        assertNotNull("Missing configuration for \"TestJsp\" in web.xml", rd);
        rd.forward(request, response);
    }

    /**
     * Verify that getNamedDispatcher() can be used to get a dispatcher.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endGetRequestDispatcherFromNamedDispatcherOK(
        WebResponse theResponse) throws IOException
    {
        String result = theResponse.getText();

        assertTrue("Page not found, got [" + result + "]", 
            result.indexOf("Hello !") > 0);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that getNamedDispatcher() returns null when passed an invalid
     * name.
     * 
     * @exception IOException on test failure
     * @exception ServletException on test failure
     */
    public void testGetRequestDispatcherFromNamedDispatcherInvalid()
        throws ServletException, IOException
    {
        RequestDispatcher rd = config.getServletContext().getNamedDispatcher(
            "invalid name");

        assertNull(rd);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that request.getRequestDispatcher() works properly with an
     * absolute path
     * 
     * @exception IOException on test failure
     * @exception ServletException on test failure
     */
    public void testGetRequestDispatcherFromRequest1() 
        throws ServletException, IOException
    {
        RequestDispatcher rd = request.getRequestDispatcher("/test/test.jsp");

        rd.include(request, response);
    }

    /**
     * Verify that request.getRequestDispatcher() works properly with an
     * absolute path
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endGetRequestDispatcherFromRequest1(WebResponse theResponse)
        throws IOException
    {
        String result = theResponse.getText();

        assertTrue("Page not found, got [" + result + "]", 
            result.indexOf("Hello !") > 0);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that request.getRequestDispatcher() works properly with a
     * relative path.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGetRequestDispatcherFromRequest2(WebRequest theRequest)
    {
        theRequest.setURL(null, "/test", "/anything.jsp", null, null);
    }

    /**
     * Verify that request.getRequestDispatcher() works properly with a
     * relative path.
     * 
     * @exception IOException on test failure
     * @exception ServletException on test failure
     */
    public void testGetRequestDispatcherFromRequest2() 
        throws ServletException, IOException
    {
        RequestDispatcher rd = request.getRequestDispatcher("test/test.jsp");

        rd.include(request, response);
    }

    /**
     * Verify that request.getRequestDispatcher() works properly with a
     * relative path.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endGetRequestDispatcherFromRequest2(WebResponse theResponse)
        throws IOException
    {
        String result = theResponse.getText();

        assertTrue("Page not found, got [" + result + "]", 
            result.indexOf("Hello !") > 0);
    }

}
