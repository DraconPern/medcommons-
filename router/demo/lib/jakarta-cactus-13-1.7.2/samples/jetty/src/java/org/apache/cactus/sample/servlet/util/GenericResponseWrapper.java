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
package org.apache.cactus.sample.servlet.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper around a <code>HttpServletResponse</code> that we use to easily
 * write filters that manipulate the output stream. Indeed, we cannot pass
 * the output stream of our filter direectly to the next filter in the chain
 * because then we won't be able to write to it (the response will have been
 * committed). Instead, we pass this wrapper class and then copy its data
 * to our filter output stream.
 *
 * Note: This code was adapted from the Filter tutorial found
 * {@link <a href="http://www.orionserver.com/tutorials/filters/lesson3/">
 * here</a>}
 *
 * @version $Id: GenericResponseWrapper.java 238816 2004-02-29 16:36:46Z vmassol $
 *
 * @see FilterServletOutputStream
 */
public class GenericResponseWrapper extends HttpServletResponseWrapper
{
    /**
     * Holder for the output data
     */
    private ByteArrayOutputStream output;

    /**
     * Save the content length so that we can query it at a later time
     * (otherwise it would not be possible as
     * <code>HttpServletResponseWrapper</code> does not have a method to get
     * the content length).
     */
    private int contentLength;

    /**
     * Save the content type so that we can query it at a later time
     * (otherwise it would not be possible as
     * <code>HttpServletResponseWrapper</code> does not have a method to get
     * the content type).
     */
    private String contentType;

    // Constructors ----------------------------------------------------------

    /**
     * @param theResponse the wrapped response object
     */
    public GenericResponseWrapper(HttpServletResponse theResponse)
    {
        super(theResponse);
        this.output = new ByteArrayOutputStream();
    }

    // New methods -----------------------------------------------------------

    /**
     * @return the data sent to the output stream
     */
    public byte[] getData()
    {
        return output.toByteArray();
    }

    // Overridden methods ----------------------------------------------------

    /**
     * @see HttpServletResponseWrapper#getOutputStream()
     */
    public ServletOutputStream getOutputStream()
    {
        return new FilterServletOutputStream(this.output);
    }

    /**
     * @see HttpServletResponseWrapper#setContentLength(int)
     */
    public void setContentLength(int theLength)
    {
        this.contentLength = theLength;
        super.setContentLength(theLength);
    }

    /**
     * @see HttpServletResponseWrapper#getContentLength()
     */
    public int getContentLength()
    {
        return this.contentLength;
    }

    /**
     * @see HttpServletResponseWrapper#setContentType(String)
     */
    public void setContentType(String theType)
    {
        this.contentType = theType;
        super.setContentType(theType);
    }

    /**
     * @see HttpServletResponseWrapper#getContentType()
     */
    public String getContentType()
    {
        return this.contentType;
    }

    /**
     * @see HttpServletResponseWrapper#getWriter()
     */
    public PrintWriter getWriter()
    {
        return new PrintWriter(getOutputStream(), true);
    }
}
