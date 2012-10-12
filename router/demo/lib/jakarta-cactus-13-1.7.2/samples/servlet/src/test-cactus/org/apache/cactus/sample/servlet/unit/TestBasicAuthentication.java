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
import org.apache.cactus.client.authentication.BasicAuthentication;

/**
 * Test running some test using BASIC authentication.
 *
 * @version $Id: TestBasicAuthentication.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestBasicAuthentication extends ServletTestCase
{
    /**
     * Verify basic authentication.
     * 
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginBasicAuthentication(WebRequest theRequest)
    {
        theRequest.setRedirectorName("ServletRedirectorSecure");
        theRequest.setAuthentication(
            new BasicAuthentication("testuser", "testpassword"));
    }

    /**
     * Verify basic authentication. Note: This method is protected in the
     * <code>web. xml</code> deployment descriptor.
     */
    public void testBasicAuthentication()
    {
        assertEquals("testuser", request.getUserPrincipal().getName());
        assertEquals("testuser", request.getRemoteUser());
        assertTrue("User not in 'test' role", request.isUserInRole("test"));
    }
}
