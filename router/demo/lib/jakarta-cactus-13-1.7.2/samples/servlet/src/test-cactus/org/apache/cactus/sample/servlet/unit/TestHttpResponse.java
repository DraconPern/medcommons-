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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests that writes to the HTTP response.
 *
 * @version $Id: TestHttpResponse.java 239010 2004-06-19 15:10:53Z vmassol $
 */
public class TestHttpResponse extends ServletTestCase
{
    /**
     * Verify that it is possible to write to the servlet output stream.
     * 
     * @exception IOException on test failure
     */
    public void testWriteOutputStream() throws IOException
    {
        PrintWriter pw = response.getWriter();

        pw.println("should not result in an error");
    }

    /**
     * Verify that it is possible to write to the servlet output stream.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endWriteOutputStream(WebResponse theResponse) throws IOException
    {
        BufferedReader br = 
            new BufferedReader(new InputStreamReader(new DataInputStream(
            theResponse.getConnection().getInputStream())));

        assertEquals("should not result in an error", br.readLine());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that the <code>WebResponse.getText()</code> method works.
     * 
     * @exception IOException on test failure
     */
    public void testGetResponseAsText() throws IOException
    {
        PrintWriter pw = response.getWriter();

        // Note: Ideally we could also test multi line to verify that end 
        // of lines are correctly handled. However, the different containers
        // handle end of lines differently (some return "\r\n" - Windows
        // style, others return "\n" - Unix style).
        pw.print("<html><head/><body>A GET request</body></html>");
    }

    /**
     * Verify that the <code>WebResponse.getText()</code> method works.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endGetResponseAsText(WebResponse theResponse)
        throws IOException
    {
        String expected = "<html><head/><body>A GET request</body></html>";
        
        String result = theResponse.getText();

        assertEquals(expected, result);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that the <code>getTextAsArray()</code> method
     * works with output text sent on multiple lines. We also verify that
     * we can call it several times with the same result.
     * 
     * @exception IOException on test failure
     */
    public void testGetResponseAsStringArrayMultiLines() throws IOException
    {
        PrintWriter pw = response.getWriter();

        response.setContentType("text/html");
        pw.println("<html><head/>");
        pw.println("<body>A GET request</body>");
        pw.println("</html>");
    }

    /**
     * Verify that the <code>getTextAsArray()</code> method
     * works with output text sent on multiple lines. We also verify that
     * we can call it several times with the same result.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endGetResponseAsStringArrayMultiLines(WebResponse theResponse)
        throws IOException
    {
        String[] results1 = theResponse.getTextAsArray();
        String[] results2 = theResponse.getTextAsArray();

        assertTrue("Should have returned 3 lines of text but returned ["
            + results1.length + "]", results1.length == 3);
        assertEquals("<html><head/>", results1[0]);
        assertEquals("<body>A GET request</body>", results1[1]);
        assertEquals("</html>", results1[2]);

        assertTrue("Should have returned 3 lines of text but returned ["
            + results2.length + "]", results2.length == 3);
        assertEquals("<html><head/>", results2[0]);
        assertEquals("<body>A GET request</body>", results2[1]);
        assertEquals("</html>", results2[2]);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that a test case can get the request body by calling
     * <code>HttpServletRequest.getReader()</code>. In other words, verify that
     * internal parameters that Cactus passes from its client side to the
     * server do not affect this ability.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGetReader(WebRequest theRequest)
    {
        theRequest.addParameter("test", "something", WebRequest.POST_METHOD);
    }

    /**
     * Verify that a test case can get the request body by calling
     * <code>HttpServletRequest.getReader()</code>. In other words, verify that
     * internal parameters that Cactus passes from its client side to the
     * server do not affect this ability.
     * 
     * @exception Exception on test failure
     */
    public void testGetReader() throws Exception
    {
        String buffer;
        StringBuffer body = new StringBuffer();

        BufferedReader reader = request.getReader();

        while ((buffer = reader.readLine()) != null)
        {
            body.append(buffer);
        }

        assertEquals("test=something", body.toString());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify we can set and retrieve the content type.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testSetContentType() throws Exception
    {
        // Note: We also specify the charset so that we are sure to known the
        // full content type string that will be returned on the client side.
        // Indeed, some containers will specify a charset even if we don't
        // specify one in the call to setContentType. This is normal and in
        // accordance with RFC2616, section 3.4.1.
        response.setContentType("text/xml;charset=ISO-8859-1");

        // Although we don't assert the written content, this is needed to make
        // the test succeed on some versions of Orion. If the content is left 
        // empty, Orion will somehow reset the content-type to text/html. Sigh.
        PrintWriter pw = response.getWriter();
        pw.println("<?xml version=\"1.0\"?>");
        pw.println("<test></test>");
    }

    /**
     * Verify we can set and retrieve the content type.
     * 
     * @param theResponse the response from the server side.
     */
    public void endSetContentType(WebResponse theResponse)
    {
        assertEquals("text/xml;charset=ISO-8859-1", 
            theResponse.getConnection().getContentType());
    }
    
    //-------------------------------------------------------------------------

    /**
     * Verify that we can assert HTTP status code when it is a redirect and
     * that the client side of Cactus does not follow the redirect.
     * 
     * @exception IOException on test failure
     */
    public void testRedirect() throws IOException
    {
        response.sendRedirect("http://jakarta.apache.org");
    }

    /**
     * Verify that we can assert HTTP status code when it is a redirect and
     * that the client side of Cactus does not follow the redirect.
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endRedirect(WebResponse theResponse) throws IOException
    {
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, 
            theResponse.getStatusCode());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can assert HTTP status code when it is an error
     * status code (> 400).
     *
     * Note: HttpURLConnection will return a FileNotFoundException if the
     * status code is > 400 and the request does not end with a "/" !
     */
    public void testStatusCode()
    {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Verify that we can assert HTTP status code when it is an error
     * status code (> 400).
     *
     * Note: HttpURLConnection will return a FileNotFoundException if the
     * status code is > 400 and the request does not end with a "/" !
     *
     * @param theResponse the response from the server side.
     * 
     * @exception IOException on test failure
     */
    public void endStatusCode(WebResponse theResponse) throws IOException
    {
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
            theResponse.getStatusCode());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can return a NO_CONTENT response.
     */
    public void testNoContentResponseCode()
    {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT); 
    }

    /**
     * Verify that we can return a NO_CONTENT response.
     *
     * @param theResponse the response from the server side.
     */
    public void endNoContentResponseCode(WebResponse theResponse) 
    {
        assertEquals(theResponse.getStatusCode(), 204);
    }
}
