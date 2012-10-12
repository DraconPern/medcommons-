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
 * Test passing HTTP parameters to the server side.
 *
 * @version $Id: TestHttpParameters.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestHttpParameters extends ServletTestCase
{
    /**
     * Verify that multi value parameters can be sent in the
     * <code>beingXXX()</code> method to the server redirector.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginMultiValueParameters(WebRequest theRequest)
    {
        theRequest.addParameter("multivalue", "value 1");
        theRequest.addParameter("multivalue", "value 2");
    }

    /**
     * Verify that multi value parameters can be sent in the
     * <code>beingXXX()</code> method to the server redirector.
     */
    public void testMultiValueParameters()
    {
        String[] values = request.getParameterValues("multivalue");

        if (values[0].equals("value 1"))
        {
            assertEquals("value 2", values[1]);
        }
        else if (values[0].equals("value 2"))
        {
            assertEquals("value 1", values[1]);
        }
        else
        {
            fail("Shoud have returned a vector with the "
                + "values \"value 1\" and \"value 2\"");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Verify we can set and retrieve several parameters.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSeveralParameters(WebRequest theRequest)
    {
        theRequest.addParameter("PostParameter1", "EMPLOYEE0145", 
            WebRequest.POST_METHOD);
        theRequest.addParameter("PostParameter2", "W", WebRequest.GET_METHOD);
        theRequest.addParameter("PostParameter3", "07/08/2002", 
            WebRequest.POST_METHOD);
        theRequest.addParameter("PostParameter4", "/tas/ViewSchedule.esp", 
            WebRequest.GET_METHOD);
    }

    /**
     * Verify we can set and retrieve several parameters.
     */
    public void testSeveralParameters()
    {
        assertEquals("parameter4", "/tas/ViewSchedule.esp", 
            request.getParameter("PostParameter4"));
        assertEquals("parameter1", "EMPLOYEE0145", 
            request.getParameter("PostParameter1"));
        assertEquals("parameter2", "W", request.getParameter("PostParameter2"));
        assertEquals("parameter3", "07/08/2002", 
            request.getParameter("PostParameter3"));
    }

}
