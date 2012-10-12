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

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.server.ServletContextWrapper;

/**
 * Tests that exercise the Cactus Servlet Config wrapper.
 *
 * @version $Id: TestServletConfig.java 239054 2004-10-24 01:30:23Z felipeal $
 */
public class TestServletConfig extends ServletTestCase
{
    /**
     * Verify that we can add parameters to the config list of parameters
     * programatically, without having to define them in <code>web.xml</code>.
     */
    public void testSetConfigParameter()
    {
        config.setInitParameter("testparam", "test value");

        assertEquals("test value", config.getInitParameter("testparam"));

        boolean found = false;
        Enumeration en = config.getInitParameterNames();

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
    public void testSetConfigParameterOverrideWebXmlParameter()
    {
        // Note: "param1" is a parameter that must be defined on the Servlet
        // redirector, with a value different than "testoverrideparam1".
        assertTrue(
            config.getOriginalConfig().getInitParameter("param1") != null);
        assertTrue(
            !config.getOriginalConfig().getInitParameter("param1").equals(
            "testoverrideparam1"));

        config.setInitParameter("param1", "testoverrideparam1");

        Enumeration en = config.getInitParameterNames();
        int count = 0;
        
        while (en.hasMoreElements())
        {
            String name = (String) en.nextElement();

            if (name.equals("param1"))
            {
                assertEquals("testoverrideparam1",
                    config.getInitParameter(name));
                count++;
            }
        }

        assertTrue("[param1] was found " + count + " times. Should have "
            + "been found once.", count == 1);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can override the
     * <code>ServletConfig.getServletName()</code> method.
     */
    public void testGetServletNameOverriden()
    {
        config.setServletName("MyServlet");
        assertEquals("MyServlet", config.getServletName());
        assertTrue(!config.getOriginalConfig().getServletName().equals(
            config.getServletName()));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that if we don't override the servlet name we get the original
     * name (i.e. Cactus is effectively transparent).
     */
    public void testGetServletNameNoOverride()
    {
        assertEquals(config.getOriginalConfig().getServletName(),
            config.getServletName());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that calls to <code>ServletContext.log()</code> methods can
     * be retrieved and asserted.
     */
    public void testGetLogs()
    {
        String message = "some test log";
        ServletContext context = config.getServletContext();

        context.log(message);

        Vector logs = ((ServletContextWrapper) context).getLogs();

        assertEquals("Found more than one log message", logs.size(), 1);
        assertTrue("Cannot find expected log message : [" + message + "]", 
            logs.contains("some test log"));
    }

}
