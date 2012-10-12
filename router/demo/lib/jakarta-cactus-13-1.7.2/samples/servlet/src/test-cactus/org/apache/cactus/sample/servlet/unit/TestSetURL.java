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
 * Test the {@link WebRequest#setURL} method.
 *
 * @version $Id: TestSetURL.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSetURL extends ServletTestCase
{
    /**
     * Verify that we can simulate the basic parts of the URL : server name,
     * default server port of 80, root servlet context, URI.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURLBasics(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "", "/test/test.jsp", null, 
            null);
    }

    /**
     * Verify that we can simulate the basic parts of the URL : server name,
     * default server port of 80, no servlet context, servlet path.
     */
    public void testSimulatedURLBasics()
    {
        assertEquals("/test/test.jsp", request.getRequestURI());
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals(80, request.getServerPort());
        assertEquals("", request.getContextPath());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL1(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/mywebapp", "/test/test.jsp", 
            null, null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL1()
    {
        assertEquals("/mywebapp/test/test.jsp", request.getRequestURI());
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals(80, request.getServerPort());
        assertEquals("/mywebapp", request.getContextPath());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL2(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", "/lawn", 
            "/index.html", null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL2()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/lawn/index.html", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/lawn", request.getServletPath());
        assertEquals("/index.html", request.getPathInfo());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL3(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", "/garden", 
            "/implements/", null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL3()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/garden/implements/", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/garden", request.getServletPath());
        assertEquals("/implements/", request.getPathInfo());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL4(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", 
            "/help/feedback.jsp", null, null);
    }

    /**
     * Verify that we can simulate different parts of the URL.
     */
    public void testSimulatedURL4()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/help/feedback.jsp", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/help/feedback.jsp", request.getServletPath());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate different parts of the URL. Also verify
     * that HTTP parameters put in the simulation URL will be
     * available on the server side as real HTTP parameters.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURL5(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/catalog", 
            "/help/feedback.jsp", null, "PARAM1=param1&PARAM2=&PARAM3=param3");
    }

    /**
     * Verify that we can simulate different parts of the URL. Also verify
     * that HTTP parameters put in the simulation URL will be
     * available on the server side as real HTTP parameters.
     */
    public void testSimulatedURL5()
    {
        assertEquals("jakarta.apache.org", request.getServerName());
        assertEquals("/catalog/help/feedback.jsp", request.getRequestURI());
        assertEquals(80, request.getServerPort());
        assertEquals("/catalog", request.getContextPath());
        assertEquals("/help/feedback.jsp", request.getServletPath());
        assertEquals("PARAM1=param1&PARAM2=&PARAM3=param3", 
            request.getQueryString());
        assertEquals(request.getParameter("PARAM1"), "param1");
        assertEquals(request.getParameter("PARAM2"), "");
        assertEquals(request.getParameter("PARAM3"), "param3");
    }

    //-------------------------------------------------------------------------

    /**
     * Verify values used by the framework when all values defined in 
     * <code>setURL()</code> are null.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSimulatedURLNullValues(WebRequest theRequest)
    {
        theRequest.setURL(null, null, null, null, null);
    }

    /**
     * Verify values used by the framework when all values defined in 
     * <code>setURL()</code> are null.
     */
    public void testSimulatedURLNullValues()
    {
        assertNotNull(request.getServerName());
        assertTrue(request.getServerPort() > 0);
        assertNotNull(request.getContextPath());       
        assertNotNull(request.getServletPath());       
        assertNull(request.getPathInfo());       
        assertNull(request.getQueryString());       
    }
}
