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

import javax.servlet.http.HttpServlet;

/**
 * Sample servlet to show how to unit test a servlet that makes calls to
 * <code>getServletConfig()</code>, <code>getServletContext()</code>,
 * <code>log()</code>, ... (i.e. methods inherited from
 * <code>GenericServlet</code>).
 *
 * @version $Id: SampleServletConfig.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class SampleServletConfig extends HttpServlet
{
    /**
     * Simulate a method that calls <code>getServletConfig()</code>.
     *
     * @return some data retrieved from the servlet configuration.
     */
    public String getConfigData()
    {
        return getServletConfig().getInitParameter("param1");
    }
}
