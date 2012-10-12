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

import org.apache.cactus.FilterTestCase;

import com.meterware.httpunit.WebResponse;

/**
 * Tests HTTP headers set in Filter code.
 * 
 * @version $Id: TestFilterHttpHeaders.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestFilterHttpHeaders extends FilterTestCase
{
    /**
     * Verify headers can be set in a Filter test case and retrieved using
     * HttpUnit.
     */
    public void testHeaders()
    {
        response.setHeader("xparevcount", "xparevcount");
        response.setHeader("xxparevcount", "xxparevcount");
    }

    /**
     * Verify headers can be set in a Filter test case and retrieved using
     * HttpUnit.
     * 
     * @param theResponse the HTTP response
     */
    public void endHeaders(WebResponse theResponse)
    {
        String header1 = theResponse.getHeaderField("xxparevcount");
        String header2 = theResponse.getHeaderField("xparevcount");
        assertNotNull("Header 1 should not be null", header1);
        assertNotNull("Header 2 should not be null", header2);
        assertEquals("xxparevcount", header1);
        assertEquals("xparevcount", header2);
       }

}
