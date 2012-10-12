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
 * Tests related to Cookies.
 *
 * @version $Id: TestCookie.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestCookie extends ServletTestCase
{
    /**
     * Verify that special characters in cookies are not URL encoded
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginCookieEncoding(WebRequest theRequest)
    {
        // Note: the pipe ('&') character is a special character regarding
        // URL encoding
        theRequest.addCookie("testcookie", "user&pwd");
    }

    /**
     * Verify that special characters in cookies are not encoded
     */
    public void testCookieEncoding()
    {
        javax.servlet.http.Cookie[] cookies = request.getCookies();

        assertNotNull("No cookies in request", cookies);

        for (int i = 0; i < cookies.length; i++)
        {
            if (cookies[i].getName().equals("testcookie"))
            {
                assertEquals("user&pwd", cookies[i].getValue());
                return;
            }
        }

        fail("No cookie named 'testcookie' found");
    }

}
