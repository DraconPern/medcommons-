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
import java.io.PrintWriter;

import java.util.Hashtable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Sample servlet that implement some very simple business logic. The goal is
 * to provide functional tests for Cactus, so we focus on providing as many
 * different test cases as possible rather than implementing a meaningful
 * business logic.
 *
 * @version $Id: SampleServlet.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class SampleServlet extends HttpServlet
{
    /**
     * Entry point for the servlet when a GET request is received. This will
     * be used to verify that we can test for the servlet output stream in
     * Cactus test cases.
     *
     * @param theRequest the HTTP request
     * @param theResponse the HTTP response
     * 
     * @exception IOException on failure
     */
    public void doGet(HttpServletRequest theRequest, 
        HttpServletResponse theResponse) throws IOException
    {
        PrintWriter pw = theResponse.getWriter();

        theResponse.setContentType("text/html");

        pw.print("<html><head/><body>");
        pw.print("A GET request");
        pw.print("</body></html>");
    }

    /**
     * Return the method used to send data from the client (POST or GET). This
     * will be used to verify that we can simulate POST or GET methods from
     * Cactus. This simulates a method which would test the method to
     * implement it's business logic.
     *
     * @param theRequest the HTTP request
     * @return the method used to post data
     */
    public String checkMethod(HttpServletRequest theRequest)
    {
        return theRequest.getMethod();
    }

    /**
     * Set some variable in the HTTP session. It verifies that a session object
     * has automatically been created by Cactus prior to calling this method.
     *
     * @param theRequest the HTTP request
     */
    public void setSessionVariable(HttpServletRequest theRequest)
    {
        HttpSession session = theRequest.getSession(false);

        session.setAttribute("name_setSessionVariable", 
            "value_setSessionVariable");
    }

    /**
     * Set some attribute in the request.
     *
     * @param theRequest the HTTP request
     */
    public void setRequestAttribute(HttpServletRequest theRequest)
    {
        theRequest.setAttribute("name_setRequestAttribute", 
            "value_setRequestAttribute");
    }

    /**
     * Get some parameters from the HTTP request.
     *
     * @param theRequest the HTTP request
     * @return a hashtable containing some parameters
     */
    public Hashtable getRequestParameters(HttpServletRequest theRequest)
    {
        Hashtable params = new Hashtable();

        params.put("param1", theRequest.getParameter("param1"));
        params.put("param2", theRequest.getParameter("param2"));

        return params;
    }

    /**
     * Get a header from the request.
     *
     * @return a test request header
     * @param theRequest the HTTP request
     */
    public String getRequestHeader(HttpServletRequest theRequest)
    {
        return theRequest.getHeader("testheader");
    }

    /**
     * @return the cookies sent in the HTTP request
     *
     * @param theRequest the HTTP request
     */
    public Hashtable getRequestCookies(HttpServletRequest theRequest)
    {
        Hashtable allCookies = new Hashtable();

        Cookie[] cookies = theRequest.getCookies();

        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = cookies[i];

                allCookies.put(cookie.getName(), cookie.getValue());
            }
        }

        return allCookies;
    }

    /**
     * Set a header in the HTTP response. This is to verify that Cactus tests
     * can assert the returned headers.
     *
     * @param theResponse the HTTP response
     */
    public void setResponseHeader(HttpServletResponse theResponse)
    {
        theResponse.setHeader("responseheader", "this is a response header");
    }

    /**
     * Set a cookie for sending back to the client. This is to verify that
     * it is possible with Cactus to assert the cookies returned to the client
     *
     * @param theResponse the HTTP response
     */
    public void setResponseCookie(HttpServletResponse theResponse)
    {
        Cookie cookie = new Cookie("responsecookie", 
            "this is a response cookie");

        cookie.setDomain("jakarta.apache.org");
        theResponse.addCookie(cookie);
    }

    /**
     * Use a <code>RequestDispatcher</code> to forward to a JSP page. This is
     * to verify that Cactus supports asserting the result, even in the case
     * of forwarding to another page.
     *
     * @param theRequest the HTTP request
     * @param theResponse the HTTP response
     * @param theConfig the servlet config object
     * 
     * @exception IOException on failure
     * @exception ServletException on failure
     */
    public void doForward(HttpServletRequest theRequest, 
        HttpServletResponse theResponse, ServletConfig theConfig) 
        throws IOException, ServletException
    {
        RequestDispatcher rd = 
            theConfig.getServletContext().getRequestDispatcher(
            "/test/test.jsp");

        rd.forward(theRequest, theResponse);
    }

    /**
     * Use a <code>RequestDispatcher</code> to include a JSP page. This is
     * to verify that Cactus supports asserting the result, even in the case
     * of including another page.
     *
     * @param theRequest the HTTP request
     * @param theResponse the HTTP response
     * @param theConfig the servlet config object
     * 
     * @exception IOException on failure
     * @exception ServletException on failure
     */
    public void doInclude(HttpServletRequest theRequest, 
        HttpServletResponse theResponse, ServletConfig theConfig) 
        throws IOException, ServletException
    {
        RequestDispatcher rd = 
            theConfig.getServletContext().getRequestDispatcher(
            "/test/test.jsp");

        rd.include(theRequest, theResponse);
    }
}
