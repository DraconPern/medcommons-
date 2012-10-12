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

import java.io.IOException;

import java.net.URLDecoder;

import java.util.Hashtable;

import org.apache.cactus.Cookie;
import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests of the <code>SampleServlet</code> servlet class.
 *
 * @version $Id: TestSampleServlet.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSampleServlet extends ServletTestCase
{
    /**
     * Verify that we can assert the servlet output stream.
     * 
     * @exception IOException on test failure
     */
    public void testReadServletOutputStream() throws IOException
    {
        SampleServlet servlet = new SampleServlet();

        servlet.doGet(request, response);
    }

    /**
     * Verify that we can assert the servlet output stream.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endReadServletOutputStream(WebResponse theResponse)
        throws IOException
    {
        String expected = "<html><head/><body>A GET request</body></html>";
        String result = theResponse.getText();

        assertEquals(expected, result);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate a POST request to a servlet. Note that
     * we send a parameter to force a POST.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginPostMethod(WebRequest theRequest)
    {
        theRequest.addParameter("param", "value", WebRequest.POST_METHOD);
    }

    /**
     * Verify that we can simulate a POST request to a servlet. Note that
     * we send a parameter to force a POST. Otherwise Cactus will do a GET
     * by default.
     */
    public void testPostMethod()
    {
        SampleServlet servlet = new SampleServlet();

        assertEquals("POST", servlet.checkMethod(request));
        assertEquals("value", request.getParameter("param"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate a GET request to a servlet. Note: Cactus
     * does a GET by default.
     */
    public void testGetMethod()
    {
        SampleServlet servlet = new SampleServlet();

        assertEquals("GET", servlet.checkMethod(request));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that by default the session implicit object is available and can
     * be used.
     */
    public void testSetAttribute()
    {
        SampleServlet servlet = new SampleServlet();

        servlet.setSessionVariable(request);

        assertNotNull(session);
        assertEquals("value_setSessionVariable", 
            session.getAttribute("name_setSessionVariable"));
    }

    /**
     * Verify that we can set an attribute in the request.
     */
    public void testSetRequestAttribute()
    {
        SampleServlet servlet = new SampleServlet();

        servlet.setRequestAttribute(request);

        assertEquals("value_setRequestAttribute", 
            request.getAttribute("name_setRequestAttribute"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate HTTP parameters in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendParams(WebRequest theRequest)
    {
        theRequest.addParameter("param1", "value1");
        theRequest.addParameter("param2", "value2");
    }

    /**
     * Verify that we can send several parameters in the HTTP request.
     */
    public void testSendParams()
    {
        SampleServlet servlet = new SampleServlet();
        Hashtable params = servlet.getRequestParameters(request);

        assertNotNull(params.get("param1"));
        assertNotNull(params.get("param2"));
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate HTTP headers in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendHeader(WebRequest theRequest)
    {
        theRequest.addHeader("testheader", "this is a header test");
    }

    /**
     * Verify that we can simulate HTTP headers in the HTTP request.
     */
    public void testSendHeader()
    {
        SampleServlet servlet = new SampleServlet();
        String headerValue = servlet.getRequestHeader(request);

        assertEquals("this is a header test", headerValue);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate a single cookie sent in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendCookie(WebRequest theRequest)
    {
        // Note: The cookie value that was chosen is a string without spaces
        // because there is a problem with Resin 1.2.1 which does not support
        // quoted cookies. It has been fixed since the 15/12/2000 release of
        // Resin.
        theRequest.addCookie("testcookie", "thisisacookie");
    }

    /**
     * Verify that we can simulate a single cookie sent in the HTTP request.
     */
    public void testSendCookie()
    {
        SampleServlet servlet = new SampleServlet();
        Hashtable cookies = servlet.getRequestCookies(request);

        assertNotNull("Cannot find [testcookie] cookie in request", 
                      cookies.get("testcookie"));
        assertEquals("thisisacookie", cookies.get("testcookie"));
    }

    /**
     * Verify that we can simulate multiple cookies sent in the HTTP request.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendMultipleCookies(WebRequest theRequest)
    {
        theRequest.addCookie("testcookie1", "cookie1");
        theRequest.addCookie("testcookie2", "cookie2");
    }

    /**
     * Verify that we can simulate multiple cookies sent in the HTTP request.
     */
    public void testSendMultipleCookies()
    {
        SampleServlet servlet = new SampleServlet();
        Hashtable cookies = servlet.getRequestCookies(request);

        assertNotNull(cookies.get("testcookie1"));
        assertEquals("cookie1", cookies.get("testcookie1"));

        assertNotNull(cookies.get("testcookie2"));
        assertEquals("cookie2", cookies.get("testcookie2"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to send back a header and verify it on the
     * client side.
     */
    public void testReceiveHeader()
    {
        SampleServlet servlet = new SampleServlet();

        servlet.setResponseHeader(response);
    }

    /**
     * Verify that it is possible to send back a header and verify it on the
     * client side.
     *
     * @param theResponse the response from the server side.
     */
    public void endReceiveHeader(WebResponse theResponse)
    {
        assertEquals("this is a response header", 
            theResponse.getConnection().getHeaderField("responseheader"));
    }

    //-------------------------------------------------------------------------

    /**
     * Test that it is possible to send back a Cookie and verify it on the
     * client side.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginReceiveCookie(WebRequest theRequest)
    {
        // Why do we need to have a begin method here ? Good question !
        // The answer is that in this test, the SampleServlet's
        // setResponseCookie() method sets the domain name of the cookie
        // to return to jakarta.apache.org. It means that for this cookie
        // to be valid the domain of the request (i.e. the host) must be
        // jakarta.apache.org (this is according to the cookies RFCs). Thus
        // we need to simulate the URL and act as if we had sent a request
        // to jakarta.apache.org ! Logical, no ?
        theRequest.setURL("jakarta.apache.org", null, null, null, null);
    }

    /**
     * Test that it is possible to send back a Cookie and verify it on the
     * client side.
     */
    public void testReceiveCookie()
    {
        SampleServlet servlet = new SampleServlet();

        servlet.setResponseCookie(response);
    }

    /**
     * Test that it is possible to send back a Cookie and verify it on the
     * client side.
     *
     * @param theResponse the response from the server side.
     *
     * @exception Exception for backward compatibility with JDK 1.2.2 (not
     *            needed for JDK 1.3+, but needed for URLDecoder.decode())
     */
    public void endReceiveCookie(WebResponse theResponse) throws Exception
    {
        Cookie cookie = theResponse.getCookie("responsecookie");

        assertNotNull("Cannot find [responsecookie]", cookie);
        assertEquals("responsecookie", cookie.getName());


        // Some servers may encode the cookie value (ex: the latest
        // version of Tomcat 4.0). In order for this test to succeed on
        // all servlet engine, we URL decode the cookie value before
        // comparing it.
        assertEquals("this is a response cookie", 
            URLDecoder.decode(cookie.getValue()));

        assertEquals("jakarta.apache.org", cookie.getDomain());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can use a <code>RequestDispatcher</code> in the class to
     * test to forward to another page and compare the result sent to the
     * output stream on the client side.
     * 
     * @exception Exception on test failure
     */
    public void testRequestDispatcherForward() throws Exception
    {
        SampleServlet servlet = new SampleServlet();

        servlet.doForward(request, response, config);
    }

    /**
     * Verify that we can use a <code>RequestDispatcher</code> in the class to
     * test to forward to another page and compare the result sent to the
     * output stream on the client side.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endRequestDispatcherForward(WebResponse theResponse)
        throws IOException
    {
        // We cannot test what is exactly returned by the called JSP between
        // different Servlet engine return different text ! For example some
        // return the JSP comment, other do not, ...
        // Thus, we only test for a match of "Hello !"
        assertTrue("Text missing 'Hello !' : [" + theResponse.getText() + "]", 
            theResponse.getText().indexOf("Hello !") > 0);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can use a <code>RequestDispatcher</code> in the class to
     * test to include another page and compare the result sent to the
     * output stream on the client side.
     * 
     * @exception Exception on test failure
     */
    public void testRequestDispatcherInclude() throws Exception
    {
        SampleServlet servlet = new SampleServlet();

        servlet.doInclude(request, response, config);
    }

    /**
     * Verify that we can use a <code>RequestDispatcher</code> in the class to
     * test to include another page and compare the result sent to the
     * output stream on the client side.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endRequestDispatcherInclude(WebResponse theResponse)
        throws IOException
    {
        // We cannot test what is exactly returned by the included JSP between
        // different Servlet engine return different text ! For example some
        // return the JSP comment, other do not, ...
        // Thus, we only test for a match of "Hello !"
        assertTrue("Text missing 'Hello !' : [" + theResponse.getText() + "]", 
            theResponse.getText().indexOf("Hello !") > 0);
    }
}
