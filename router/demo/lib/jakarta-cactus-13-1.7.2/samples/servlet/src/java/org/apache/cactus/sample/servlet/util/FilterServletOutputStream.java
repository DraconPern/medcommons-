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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * Helper class to help write filters that manipulates the output stream. This
 * is because normally, the <code>ServletOutputStream</code> cannot be
 * modified after a resource has committed it.
 *
 * Note: This code was adapted from the Filter tutorial found
 * {@link <a href="http://www.orionserver.com/tutorials/filters/lesson3/">
 * here</a>}
 *
 * @version $Id: FilterServletOutputStream.java 238816 2004-02-29 16:36:46Z vmassol $
 *
 * @see GenericResponseWrapper
 */
public class FilterServletOutputStream extends ServletOutputStream
{
    /**
     * The stream where all the data will get written to
     */
    private DataOutputStream stream;

    /**
     * Constructor.
     *
     * @param theOutput the output stream that we wrap in a
     *        <code>DataOutputStream</code> in order to hold the data
     */
    public FilterServletOutputStream(OutputStream theOutput)
    {
        stream = new DataOutputStream(theOutput);
    }

    // Overriden methods from ServletOutputStream ----------------------------

    /**
     * @see ServletOutputStream#write(int)
     */
    public void write(int theData) throws IOException
    {
        stream.write(theData);
    }

    /**
     * @see ServletOutputStream#write(byte[])
     */
    public void write(byte[] theData) throws IOException
    {
        stream.write(theData);
    }

    /**
     * @see ServletOutputStream#write(byte[], int, int)
     */
    public void write(byte[] theData, int theOffset, int theLength) 
        throws IOException
    {
        stream.write(theData, theOffset, theLength);
    }
}
