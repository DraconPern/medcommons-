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

import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;

/**
 * Tests of the <code>SampleBodyTag</code> class.
 *
 * @version $Id: TestSampleBodyTag.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestSampleBodyTag extends JspTestCase
{
    /**
     * Our tag instance being unit tested
     */
    private SampleBodyTag tag;

    /**
     * The tag body content to which we can write to in our unit tests
     * to simulate a content.
     */
    private BodyContent tagContent;

    /**
     * In addition to creating the tag instance and adding the pageContext to
     * it, this method creates a BodyContent object and passes it to the tag.
     */
    public void setUp()
    {
        this.tag = new SampleBodyTag();
        this.tag.setPageContext(this.pageContext);

        //create the BodyContent object and call the setter on the tag instance
        this.tagContent = this.pageContext.pushBody();
        this.tag.setBodyContent(this.tagContent);
    }

    /**
     * @see TestCase#tearDown()
     */
    public void tearDown()
    {
        //necessary for tag to output anything on most servlet engines.
        this.pageContext.popBody();
    }

    //-------------------------------------------------------------------------

    /**
     * Sets the replacement target and replacement String on the tag, then calls
     * doAfterBody(). Most of the assertion work is done in endReplacement().
     * 
     * @exception Exception if the test fails for an unexpected reason
     */
    public void testReplacement() throws Exception
    {
        //set the target and the String to replace it with
        this.tag.setTarget("@target@");
        this.tag.setReplacement("replacement");

        //add the tag's body by writing to the BodyContent object created in
        //setUp()
        this.tagContent.println("@target@ is now @target@");
        this.tagContent.println("@target@_@target@");

        //none of the other life cycle methods need to be implemented, so they
        //do not need to be called.
        int result = this.tag.doAfterBody();

        assertEquals(BodyTag.SKIP_BODY, result);
    }

    /**
     * Verifies that the target String has indeed been replaced in the tag's
     * body.
     * 
     * @param theResponse the response from the server side.
     */
    public void endReplacement(WebResponse theResponse)
    {
        String content = theResponse.getText();

        assertTrue("Response should have contained the ["
            + "replacement is now replacement] string", 
            content.indexOf("replacement is now replacement") > -1);
        assertTrue("Response should have contained the ["
            + "replacement_replacement] string", 
            content.indexOf("replacement") > -1);
    }
}
