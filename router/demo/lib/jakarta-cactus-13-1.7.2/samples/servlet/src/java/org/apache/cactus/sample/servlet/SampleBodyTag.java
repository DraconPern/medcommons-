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

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Sample tag that interacts with its body. The tag acts as a filter for its
 * body. "Target" and "Replacement" Strings are defined by the tag's attributes
 * and each "occurrence" of the target is replaced by the "replacement".
 *
 * @version $Id: SampleBodyTag.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class SampleBodyTag extends BodyTagSupport
{
    /**
     * The substring to be replaced in the body.
     */
    private String target;

    /**
     * The substring that will replace the target in the body.
     */
    private String replacement;

    /**
     * Sets the substring to be replaced in the body.
     *
     * @param theTarget the substring to be replaced in the body
     */
    public void setTarget(String theTarget)
    {
        this.target = theTarget;
    }

    /**
     * Sets the substring that will replace the target in the body.
     * 
     * @param theReplacement the replacement string
     */
    public void setReplacement(String theReplacement)
    {
        this.replacement = theReplacement;
    }

    /**
     * @see BodyTagSupport#doAfterBody()
     */
    public int doAfterBody() throws JspTagException
    {
        String contentString = this.bodyContent.getString();
        StringBuffer contentBuffer = new StringBuffer(contentString);

        int beginIndex = -1;
        int targetLength = this.target.length();

        // while instances of target still exist
        while ((beginIndex = contentString.indexOf(this.target)) > -1)
        {
            int endIndex = beginIndex + targetLength;

            contentBuffer.replace(beginIndex, endIndex, this.replacement);

            contentString = contentBuffer.toString();
        }

        // write out the changed body
        JspWriter pageWriter = this.bodyContent.getEnclosingWriter();

        try
        {
            pageWriter.write(contentString);
        }
        catch (IOException e)
        {
            throw new JspTagException(e.getMessage());
        }

        return SKIP_BODY;
    }

    /**
     * @see BodyTagSupport#release()
     */
    public void release()
    {
        this.target = null;
        this.replacement = null;
    }
}
