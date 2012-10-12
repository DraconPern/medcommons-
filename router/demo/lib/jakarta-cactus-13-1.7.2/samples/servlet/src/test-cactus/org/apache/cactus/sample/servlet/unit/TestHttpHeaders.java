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
import org.apache.cactus.WebResponse;

/**
 * Tests manipulating HTTP headers.
 *
 * @version $Id: TestHttpHeaders.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestHttpHeaders extends ServletTestCase
{
    /**
     * Verify that we can simulate several HTTP header values with the same
     * header name.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSendMultivaluedHeader(WebRequest theRequest)
    {
        theRequest.addHeader("testheader", "value1");
        theRequest.addHeader("testheader", "value2");
    }

    /**
     * Verify that we can simulate several HTTP header values with the same
     * header name.
     */
    public void testSendMultivaluedHeader()
    {
        // Note: I am not sure how to retrieve multi valued headers. The
        // problem is that I use
        // URLConnection.setRequestProperty("testheader", "value1,value2") in
        // JdkConnectionHelper to send the headers but request.getHeaders() does
        // not seem to separate the different header values.
        // The RFC 2616 says :
        // message-header = field-name ":" [ field-value ]
        // field-name     = token
        // field-value    = *( field-content | LWS )
        // field-content  = <the OCTETs making up the field-value
        //                  and consisting of either *TEXT or combinations
        //                  of token, separators, and quoted-string>
        // [...]
        // Multiple message-header fields with the same field-name MAY be
        // present in a message if and only if the entire field-value for that
        // header field is defined as a comma-separated list [i.e., #(values)].
        // It MUST be possible to combine the multiple header fields into one
        // "field-name: field-value" pair, without changing the semantics of
        // the message, by appending each subsequent field-value to the first,
        // each separated by a comma. The order in which header fields with the
        // same field-name are received is therefore significant to the
        // interpretation of the combined field value, and thus a proxy MUST
        // NOT change the order of these field values when a message is
        // forwarded.
        // ... so it should be ok ...
        assertEquals("value1,value2", request.getHeader("testheader"));

        // Here is commented out what I would have thought I should have
        // written to verify this test but it does not seem to work this way ...

        /*
        Enumeration values = request.getHeaders("testheader");
        int count = 0;
        while (values.hasMoreElements()) {
            String value = (String)values.nextElement();
            if (!(value.equals("value1") || value.equals("value2"))) {
                fail("unknown value [" + value + "] for header [testheader]");
            }
            count++;
        }
        assertEquals("Should have received 2 values for header [testheader]",
            2, count);
        */
    }

    //-------------------------------------------------------------------------

    /**
     * Verify we can set the content type by setting an HTTP header.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginSetContentTypeHeader(WebRequest theRequest)
    {
        theRequest.addHeader("Content-type", "text/xml");
    }

    /**
     * Verify we can set the content type by setting an HTTP header.
     */
    public void testSetContentTypeHeader()
    {
        assertEquals("text/xml", request.getContentType());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can set several headers in the response and
     * assert them in endXXX().
     */
    public void testResponseAddHeaders()
    {
        response.addHeader("X-Test-Header1", "value1");
        response.addHeader("X-Test-Header2", "value2");
    }

    /**
     * Verify that we can set several headers in the response and
     * assert them in endXXX().
     *
     * @param theResponse the response from the server side.
     */
    public void endResponseAddHeaders(WebResponse theResponse) 
    {
        String value1 = 
            theResponse.getConnection().getHeaderField("X-Test-Header1");
        String value2 = 
            theResponse.getConnection().getHeaderField("X-Test-Header2");

        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

}
