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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Tests that exercise the HTTP request.
 *
 * @version $Id: TestHttpRequest.java 239170 2005-05-06 12:22:24Z vmassol $
 */
public class TestHttpRequest extends ServletTestCase
{
    /**
     * Verify that <code>HttpServletRequestWrapper.getPathTranslated()</code>
     * takes into account the simulated URL (if any).
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGetPathTranslated(WebRequest theRequest)
    {
        theRequest.setURL("jakarta.apache.org", "/mywebapp", "/myservlet", 
            "/test1/test2", "PARAM1=value1");
    }

    /**
     * Verify that <code>HttpServletRequestWrapper.getPathTranslated()</code>
     * takes into account the simulated URL (if any) or null in situations
     * where the servlet container cannot determine a valid file path for
     * these methods, such as when the web application is executed from an
     * archive, on a remote file system not accessible locally, or in a
     * database (see section SRV.4.5 of the Servlet 2.3 spec).
     */
    public void testGetPathTranslated()
    {
        String nativePathInfo = File.separator + "test1" + File.separator
            + "test2";

        String pathTranslated = request.getPathTranslated();

        // Should be null if getRealPath("/") is null
        if (request.getRealPath("/") == null)
        {
            assertNull("Should have been null", pathTranslated);
        }
        else
        {
            assertNotNull("Should not be null", pathTranslated);
            assertTrue("Should end with [" + nativePathInfo + "] but got ["
                + pathTranslated + "] instead", 
                pathTranslated.endsWith(nativePathInfo));
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can send arbitrary data in the request body and read the
     * data back on the server side using a Reader.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendUserDataAndReadWithReader(WebRequest theRequest)
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(
            "<data>some data to send in the body</data>".getBytes());

        theRequest.setUserData(bais);
        theRequest.setContentType("text/xml");
    }

    /**
     * Verify that we can send arbitrary data in the request body and read the
     * data back on the server side using a Reader.
     * 
     * @exception Exception on test failure
     */
    public void testSendUserDataAndReadWithReader() throws Exception
    {
        String buffer;
        StringBuffer body = new StringBuffer();

        BufferedReader reader = request.getReader();

        while ((buffer = reader.readLine()) != null)
        {
            body.append(buffer);
        }

        assertEquals("<data>some data to send in the body</data>", 
            body.toString());
        assertEquals("text/xml", request.getContentType());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can send arbitrary data in the request body and read the
     * data back on the server side using an ObjectInputStream.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     * @exception Exception on test failure
     */
    public void beginSendUserDataAndReadWithObjectInputStream(
        WebRequest theRequest) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();        
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject("Test with a String object");
        oos.flush();

        theRequest.setUserData(new ByteArrayInputStream(baos.toByteArray()));
        theRequest.setContentType("application/octet-stream");
    }

    /**
     * Verify that we can send arbitrary data in the request body and read the
     * data back on the server side using an ObjectInputStream.
     * 
     * @exception Exception on test failure
     */
    public void testSendUserDataAndReadWithObjectInputStream() throws Exception
    {
        InputStream in = request.getInputStream();
        ObjectInputStream  ois = new ObjectInputStream(in);
        String data = (String) ois.readObject();
        assertNotNull(data);
        assertEquals("Test with a String object", data);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can simulate the client remote IP address and the client
     * remote host name.
     */
    public void testRemoteClientCheck()
    {
        request.setRemoteIPAddress("192.168.0.1");
        request.setRemoteHostName("atlantis");
        request.setRemoteUser("george");

        assertEquals("192.168.0.1", request.getRemoteAddr());
        assertEquals("atlantis", request.getRemoteHost());
        assertEquals("george", request.getRemoteUser());
    }

}
