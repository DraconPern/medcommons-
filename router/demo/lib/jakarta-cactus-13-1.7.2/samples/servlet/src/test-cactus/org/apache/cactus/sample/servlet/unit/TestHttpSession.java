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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.HttpSessionCookie;
import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests that manipulates the HTTP Session.
 *
 * @version $Id: TestHttpSession.java 239131 2005-01-29 15:49:49Z vmassol $
 */
public class TestHttpSession extends ServletTestCase
{
    /**
     * Save the session cookie for the testDependentTestUsingSession tests. 
     * The idea is to share the same session object on the server side.
     */
    private HttpSessionCookie sessionCookie;

    /**
     * Link this test to another test. This is to cleanly be able to pass a
     * parameter to another test (in our case the session cookie). 
     */
    private TestHttpSession dependentTest;

    /**
     * @see ServletTestCase#ServletTestCase(String)
     */
    public TestHttpSession(String theName)
    {
        super(theName);
    }

    /**
     * @param theName test name
     * @param theDependentest a dependent test to link to this test
     */
    public TestHttpSession(String theName, TestHttpSession theDependentest)
    {
        super(theName);
        this.dependentTest = theDependentest;
    }

    /**
     * We order the tests so that we are sure that testDependentTestUsingSession
     * is run before testDependentTestUsingSession2. This is because we wish to
     * verify it is possible to share session data between 2 tests.
     * 
     * @return the ordered suite to execute 
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestHttpSession("testNoAutomaticSessionCreation"));
        suite.addTest(new TestHttpSession("testVerifyJsessionid"));
        suite.addTest(new TestHttpSession("testCreateSessionCookie"));

        TestHttpSession test = 
            new TestHttpSession("testDependentTestUsingSession");
        suite.addTest(test);
        suite.addTest(
            new TestHttpSession("testDependentTestUsingSession2", test));
        
        return suite;
    }
    
    /**
     * Verify that it is possible to ask for no automatic session creation in
     * the <code>beginXXX()</code> method.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginNoAutomaticSessionCreation(WebRequest theRequest)
    {
        theRequest.setAutomaticSession(false);
    }

    /**
     * Verify that it is possible to ask for no automatic session creation in
     * the <code>beginXXX()</code> method.
     */
    public void testNoAutomaticSessionCreation()
    {
        assertNull("A valid session has been found when no session should "
            + "exist", session);
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that we can get hold of the jsessionid cookie returned by the
     * server.
     */
    public void testVerifyJsessionid()
    {
        // By default, Cactus will create an HTTP session.
    }
    
    /**
     * Verify that we can get hold of the jsessionid cookie returned by the
     * server.
     * 
     * @param theResponse the response from the server side.
     */
    public void endVerifyJsessionid(WebResponse theResponse)
    {
        assertNotNull(theResponse.getCookieIgnoreCase("jsessionid"));
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that Cactus can provide us with a real HTTP session cookie.
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginCreateSessionCookie(WebRequest theRequest)
    {
        HttpSessionCookie sessionCookie = theRequest.getSessionCookie();
        assertNotNull("Session cookie should not be null", sessionCookie);
        theRequest.addCookie(sessionCookie);
    }

    /**
     * Verify that Cactus can provide us with a real HTTP session cookie.
     */
    public void testCreateSessionCookie()
    {
        assertTrue("A session should have been created prior to "
            + "this request", !session.isNew());
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that it is possible to share the HTTP Session between 2 tests.
     * Please note that this is *NOT* recommended at all as unit tests must
     * be independent one from another. 
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginDependentTestUsingSession(WebRequest theRequest)
    {
        this.sessionCookie = theRequest.getSessionCookie();
        assertNotNull("Session cookie should not be null", sessionCookie);
        theRequest.addCookie(sessionCookie);
    }

    /**
     * Verify that it is possible to share the HTTP Session between 2 tests.
     * Please note that this is *NOT* recommended at all as unit tests must
     * be independent one from another. 
     */
    public void testDependentTestUsingSession()
    {
        session.setAttribute("dependentTestId", "dependentTestValue");
    }
    
    /**
     * Verify that it is possible to share the HTTP Session between 2 tests.
     * Please note that this is *NOT* recommended at all as unit tests must
     * be independent one from another. 
     *
     * @param theRequest the request object that serves to initialize the
     *                   HTTP connection to the server redirector.
     */
    public void beginDependentTestUsingSession2(WebRequest theRequest)
    {
        assertNotNull(this.dependentTest.sessionCookie);
        theRequest.addCookie(this.dependentTest.sessionCookie);        
    }

    /**
     * Verify that it is possible to share the HTTP Session between 2 tests.
     * Please note that this is *NOT* recommended at all as unit tests must
     * be independent one from another. 
     */
    public void testDependentTestUsingSession2()
    {
        assertEquals("dependentTestValue", 
            session.getAttribute("dependentTestId"));
    }
}
