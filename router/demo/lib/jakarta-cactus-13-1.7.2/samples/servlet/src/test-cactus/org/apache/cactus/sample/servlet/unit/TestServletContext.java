/* 
 * ========================================================================
 * 
 * Copyright 2004 The Apache Software Foundation.
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

import java.util.Enumeration;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.server.ServletContextWrapper;

/**
 * Tests that exercise the Cactus Servlet Context wrapper.
 *
 * @version $Id: TestServletContext.java 239054 2004-10-24 01:30:23Z felipeal $
 */
public class TestServletContext extends ServletTestCase
{
    /**
     * The Cactus servlet context wrapper. 
     */
    private ServletContextWrapper context;

    /**
     * Common initialization steps for all tests.
     */
    public void setUp()
    {
        context = (ServletContextWrapper) config.getServletContext();
    }
    
    /**
     * Verify that we can add parameters to the context list of parameters
     * programatically, without having to define them in <code>web.xml</code>.
     */
    public void testSetContextInitParameterUsingApi()
    {
        context.setInitParameter("testparam", "test value");

        assertEquals("test value", context.getInitParameter("testparam"));

        boolean found = false;
        Enumeration en = context.getInitParameterNames();

        while (en.hasMoreElements())
        {
            String name = (String) en.nextElement();

            if (name.equals("testparam"))
            {
                found = true;

                break;
            }
        }

        assertTrue("[testparam] not found in parameter names", found);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that calling <code>setInitParameter()</code> with a parameter
     * already defined in <code>web.xml</code> will override it.
     */
    public void testSetContextInitParameterOverrideWebXmlParameter()
    {
        // Note: "param1" is a parameter that must be already defined in
        // web.xml (in the context-param element), with a value different
        // than "testoverrideparam1".
        assertTrue("'param' context-param should been defined in web.xml",
            context.getOriginalContext().getInitParameter("param") != null);
        assertTrue(
            !context.getOriginalContext().getInitParameter("param").equals(
            "testoverrideparam"));

        context.setInitParameter("param", "testoverrideparam");

        Enumeration en = context.getInitParameterNames();
        int count = 0;
        
        while (en.hasMoreElements())
        {
            String name = (String) en.nextElement();

            if (name.equals("param"))
            {
                assertEquals("testoverrideparam",
                    context.getInitParameter(name));
                count++;
            }
        }

        assertTrue("[param] was found " + count + " times. Should have "
            + "been found once.", count == 1);
    }

}
