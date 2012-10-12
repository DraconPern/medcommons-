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

import javax.servlet.ServletOutputStream;

import org.apache.cactus.ServletTestCase;

/**
 * Verify that the Cactus client side only reads the test result *after* the
 * test is finished (ie after the test result has been saved in the application
 * scope). This JUnit test need to be the first one to be run. Otherwise, the
 * test result might be that of the previous test and not the current test one,
 * thus proving nothing !!
 *
 * @version $Id: TestClientServerSynchronization.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestClientServerSynchronization extends ServletTestCase
{
    /**
     * Verify that the test result can be returned correctly even when the
     * logic in the method to test takes a long time and thus it verifies that
     * the test result is only returned after it has been written in the
     * application scope on the server side.
     * 
     * @exception Exception on test failure
     */
    public void testLongProcess() throws Exception
    {
        ServletOutputStream os = response.getOutputStream();

        os.print("<html><head><Long Process></head><body>");
        os.flush();

        // do some processing that takes a while ...
        Thread.sleep(3000);
        os.println("Some data</body></html>");
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that when big amount of data is returned by the servlet output
     * stream, it does not io-block.
     * 
     * @exception Exception on test failure
     */
    public void testLotsOfData() throws Exception
    {
        ServletOutputStream os = response.getOutputStream();

        os.println("<html><head>Lots of Data</head><body>");
        os.flush();

        for (int i = 0; i < 5000; i++)
        {
            os.println("<p>Lots and lots of data here");
        }

        os.println("</body></html>");
    }
}
