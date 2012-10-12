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

import java.util.Map;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;

/**
 * Test HTTP request methods specific to Servlet API 2.3.
 *
 * @version $Id: TestHttpRequestSpecific.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestHttpRequestSpecific extends ServletTestCase
{
    /**
     * Verify that <code>HttpServletRequest.getParameterMap()</code> works.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginGetParameterMap(WebRequest theRequest)
    {
        theRequest.addParameter("multivalue", "value 1");
        theRequest.addParameter("multivalue", "value 2");
    }

    /**
     * Verify that <code>HttpServletRequest.getParameterMap()</code> works.
     */
    public void testGetParameterMap()
    {
        Map parameters = request.getParameterMap();
        assertTrue(parameters.containsKey("multivalue"));
        String[] values = (String[]) parameters.get("multivalue");
        assertEquals(2, values.length);        
        assertEquals("value 1", values[0]);        
        assertEquals("value 2", values[1]);        
    }

    //-------------------------------------------------------------------------

    /**
     * Verifies that the wrapped HTTP request is a simple pass through when no
     * simulation URL is defined.
     */
    public void testRequestURL()
    {
        StringBuffer realURL = request.getOriginalRequest().getRequestURL();
        StringBuffer wrappedURL = request.getRequestURL();

        assertEquals(realURL.toString(), wrappedURL.toString());
    }

}
