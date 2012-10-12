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

import java.io.IOException;

import java.util.Enumeration;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Sample tag that implements simple tag logic.
 *
 * @version $Id: SampleTag.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class SampleTag extends TagSupport
{
    /**
     * Determines whether the tag's body should be shown.
     */
    private boolean showBody;

    /**
     * Determines whether page should continue after the tag.
     */
    private boolean stopPage;

    /** 
     * Determines whether the tag's body should be shown.
     * 
     * @param isBodyShown a String equaling 'true' will be taken as
     *                 <code>true</code>. Anything else will be
     *                 taken as <code>false</code>.
     */
    public void setShowBody(String isBodyShown)
    {
        this.showBody = "true".equals(isBodyShown.toLowerCase());
    }

    /** 
     * Determines whether page should stop after the tag.
     * 
     * @param isPageStopped a String equaling 'true' will be taken as
     *                 <code>true</code>. Anything else will be
     *                 taken as <code>false</code>.
     */
    public void setStopPage(String isPageStopped)
    {
        this.stopPage = "true".equals(isPageStopped);
    }

    /**
     * Prints the names and values of everything in page scope to the response,
     * along with the body (if showBody is set to <code>true</code>).
     * 
     * @return the return code
     * @exception JspTagException on failure
     */
    public int doStartTag() throws JspTagException
    {
        Enumeration names = pageContext.getAttributeNamesInScope(
            PageContext.PAGE_SCOPE);

        JspWriter out = pageContext.getOut();

        try
        {
            out.println("The following attributes exist in page scope: <BR>");

            while (names.hasMoreElements())
            {
                String name = (String) names.nextElement();
                Object attribute = pageContext.getAttribute(name);

                out.println(name + " = " + attribute + " <BR>");
            }

            if (this.showBody)
            {
                out.println("Body Content Follows: <BR>");

                return EVAL_BODY_INCLUDE;
            }
        }
        catch (IOException e)
        {
            throw new JspTagException(e.getMessage());
        }

        return SKIP_BODY;
    }

    /**
     * Does two things:
     * <ul>
     *      <li>Stops the page if the corresponding attribute has been set</li>
     *      <li>Prints a message another tag encloses this one.</li>
     * </ul>
     * 
     * @return the return code
     * @exception JspTagException on failure
     */
    public int doEndTag() throws JspTagException
    {
        //get the parent if any
        Tag parent = this.getParent();

        if (parent != null)
        {
            try
            {
                JspWriter out = this.pageContext.getOut();

                out.println("This tag has a parent. <BR>");
            }
            catch (IOException e)
            {
                throw new JspTagException(e.getMessage());
            }
        }

        if (this.stopPage)
        {
            return Tag.SKIP_PAGE;
        }

        return Tag.EVAL_PAGE;
    }
}
