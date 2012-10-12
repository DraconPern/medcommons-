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

import javax.servlet.jsp.tagext.Tag;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;

/**
 * Tests of the <code>SampleTag</code> class.
 *
 * @version $Id: TestSampleTag.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSampleTag extends JspTestCase
{
    /**
     * Our tag instance to unit test
     */
    private SampleTag tag;

    /**
     * @see TestCase#setUp()
     */
    public void setUp()
    {
        this.tag = new SampleTag();
        this.tag.setPageContext(this.pageContext);
    }

    //-------------------------------------------------------------------------

    /**
     * Tests whether doStartTag() will skip the body if the corresponding tag
     * attribute is set. Also tests whether an attribute put into page scope
     * before the tag executes will be output to the response.
     * 
     * @exception Exception if the test fails for an unexpected reason
     */
    public void testDoStartTag() throws Exception
    {
        //put something in page scope to see if it shows up in the response...
        this.pageContext.setAttribute("test-key", "test-value");

        this.tag.setShowBody("false");

        int result = this.tag.doStartTag();

        //body should not show up
        assertEquals(Tag.SKIP_BODY, result);
    }

    /**
     * Verifies that the output includes the output from doStartTag (a message
     * from the tag and the attribute set into page scope).
     * 
     * @param theResponse the response from the server side.
     */
    public void endDoStartTag(WebResponse theResponse)
    {
        // check that two of the lines output by the tag showed up in
        // the response
        assertContains(theResponse, 
            "The following attributes exist in page scope: <BR>");

        assertContains(theResponse, "test-key = test-value <BR>");
    }

    //-------------------------------------------------------------------------

    /**
     * Test whether the tag's body will be shown if the corresponding attribute
     * is set.
     * 
     * @exception Exception if the test fails for an unexpected reason
     */
    public void testDoStartTagInclude() throws Exception
    {
        this.tag.setShowBody("true");

        int result = this.tag.doStartTag();

        //body should show up
        assertEquals(Tag.EVAL_BODY_INCLUDE, result);
    }

    /**
     * The tag prints a message before the body is included, here we check that
     * the message shows up.
     * 
     * @param theResponse the response from the server side.
     */
    public void endDoStartTagInclude(WebResponse theResponse)
    {
        // check that the pre-body message printed by the tag shows up
        assertContains(theResponse, "Body Content Follows: <BR>");
    }

    //-------------------------------------------------------------------------

    /**
     * Checks if the tag will continue the page correctly if its stopPage
     * property is set to false.
     * 
     * @exception Exception if the test fails for an unexpected reason
     */
    public void testDoEndTagContinue() throws Exception
    {
        this.tag.setParent(new SampleTag());
        this.tag.setStopPage("false");

        int result = this.tag.doEndTag();

        assertEquals(Tag.EVAL_PAGE, result);
    }

    /**
     * Checks whether the tag has printed a message indicating that it has a
     * parent tag.
     * 
     * @param theResponse the response from the server side.
     */
    public void endDoEndTagContinue(WebResponse theResponse)
    {
        assertContains(theResponse, "This tag has a parent. <BR>");
    }

    //-------------------------------------------------------------------------

    /**
     * Checks if the tag will signal that page processing should stop if
     * stopPage is set to "true"
     * 
     * @exception Exception if the test fails for an unexpected reason
     */
    public void testDoEndTagStop() throws Exception
    {
        //no parent set
        this.tag.setStopPage("true");

        int result = this.tag.doEndTag();

        assertEquals(Tag.SKIP_PAGE, result);
    }

    /**
     * Checks whether the tag has printed a message indicating that it has a
     * parent tag. (In this case it should not.)
     * 
     * @param theResponse the response from the server side.
     */
    public void endDoEndTagStop(WebResponse theResponse)
    {
        String target = theResponse.getText();
        boolean containsMessage = 
            target.indexOf("This tag has a parent. <BR>") > 0;
        assertTrue(!containsMessage);
    }

    //--------------------------------------------------------------------------

    /**
     * Convenience function that asserts that a substring can be found in a
     * the returned HTTP response body.
     * 
     * @param theResponse the response from the server side.
     * @param theSubstring the substring to look for
     */
    public void assertContains(WebResponse theResponse, String theSubstring)
    {
        String target = theResponse.getText();

        if (target.indexOf(theSubstring) < 0)
        {
            fail("Response did not contain the substring: [" + theSubstring
                + "]");
        }
    }
}
