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
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.cactus.sample.servlet.util.GenericResponseWrapper;

/**
 * Sample filter that implements some very simple business logic. The goal is
 * to provide some functional tests for Cactus and examples for Cactus users.
 * This filter simply adds a header and a footer to the returned HTML.
 *
 * @version $Id: SampleFilter.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class SampleFilter implements Filter
{
    /**
     * We need to save the filter config as the Fitler API does not offer
     * a means to get the filter config ... except in the <code>init()</code>
     */
    private FilterConfig config;

    /**
     * Filter initialisation. Called by the servlet engine during the life
     * cycle of the filter.
     *
     * @param theConfig the filter config
     * 
     * @exception ServletException on failure
     */
    public void init(FilterConfig theConfig) throws ServletException
    {
        this.config = theConfig;
    }

    /**
     * Perform the filter function. Called by the container upon a request
     * matching the filter pattern defined in <code>web.xml</code>.
     *
     * @param theRequest the incmoing HTTP request
     * @param theResponse the returned HTTP response
     * @param theChain the chain of filters extracted from the definition
     *        given in <code>web.xml</code> by the container.
     * 
     * @exception ServletException on failure
     * @exception IOException on failure
     */
    public void doFilter(ServletRequest theRequest, 
        ServletResponse theResponse, FilterChain theChain) throws IOException,
        ServletException
    {
        OutputStream out = theResponse.getOutputStream();

        addHeader(out);

        // Create a wrapper of the response so that we can later write to
        // the response (add the footer). If we did not do this, we would
        // get an error saying that the response has already been
        // committed.
        GenericResponseWrapper wrapper = 
            new GenericResponseWrapper((HttpServletResponse) theResponse);

        theChain.doFilter(theRequest, wrapper);

        out.write(wrapper.getData());
        addFooter(out);
        out.close();
    }

    /**
     * Write the header to the output stream. The header text is extracted
     * from a filter initialisation parameter (defined in
     * <code>web.xml</code>). Don't write anything if no parameter is defined.
     *
     * @param theOutputStream the output stream
     * 
     * @exception IOException on failure
     */
    protected void addHeader(OutputStream theOutputStream) throws IOException
    {
        String header = this.config.getInitParameter("header");

        if (header != null)
        {
            theOutputStream.write(header.getBytes());
        }
    }

    /**
     * Write the footer to the output stream. The footer text is extracted
     * from a filter initialisation parameter (defined in
     * <code>web.xml</code>). Don't write anything if no parameter is defined.
     *
     * @param theOutputStream the output stream
     * 
     * @exception IOException on failure
     */
    protected void addFooter(OutputStream theOutputStream) throws IOException
    {
        String footer = this.config.getInitParameter("footer");

        if (footer != null)
        {
            theOutputStream.write(footer.getBytes());
        }
    }

    /**
     * Filter un-initialisation. Called by the servlet engine during the life
     * cycle of the filter.
     */
    public void destroy()
    {
    }
}
