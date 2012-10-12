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

import junit.framework.AssertionFailedError;

/**
 * Test that <code>tearDown()</code> is called even when an exception
 * occurs during the test.
 *
 * @version $Id: TestTearDownException.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestTearDownException extends ServletTestCase
{
    /**
     * Intercepts running test cases to check for normal exceptions.
     */
    public void runBare()
    {
        try
        {
            super.runBare();
        }
        catch (Throwable e)
        {
            assertEquals("testTearDown() worked", e.getMessage());
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that the <code>tearDown()</code> is always called even when there
     * is an exception raised during the test.
     * 
     * @exception Exception on test failure
     */
    public void testTearDown() throws Exception
    {
        // Provoke an exception
        fail("provoked error");
    }

    /**
     * Verify that the <code>tearDown()</code> is always called even when there
     * is an exception raised during the test.
     */
    public void tearDown()
    {
        throw new AssertionFailedError("testTearDown() worked");
    }
}
