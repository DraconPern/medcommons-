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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.jstl.core.LoopTagStatus;

import org.apache.cactus.extension.jsp.JspTagLifecycle;
import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;
import org.apache.taglibs.standard.tag.common.core.ChooseTag;
import org.apache.taglibs.standard.tag.common.core.OtherwiseTag;
import org.apache.taglibs.standard.tag.el.core.ForEachTag;
import org.apache.taglibs.standard.tag.el.core.IfTag;
import org.apache.taglibs.standard.tag.el.core.OutTag;
import org.apache.taglibs.standard.tag.el.core.SetTag;
import org.apache.taglibs.standard.tag.el.core.WhenTag;

/**
 * Tests for the <code>JspTagLifecycle</code> extension.
 * 
 * <p>
 *   The lifecycle helper is tested here by testing the core tags of the 
 *   reference implementation of the JSP standard tag library (JSTL), available
 *   at <a href="http://jakarta.apache.org/taglibs/">.
 * </p>
 * 
 * @version $Id: TestJspTagLifecycle.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class TestJspTagLifecycle extends JspTestCase
{   
    // Test Methods ------------------------------------------------------------
    
    /**
     * Tests whether the constructor throws a <code>NullPointerException</code>
     * when passed a <code>null</code> <code>PageContext</code> reference.
     */
    public void testConstructorWithNullPageContext()
    {
        try
        {
            new JspTagLifecycle(null, new OutTag());
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the constructor throws a <code>NullPointerException</code>
     * when passed a <code>null</code> <code>Tag</code> reference.
     */
    public void testConstructorWithNullTag()
    {
        try
        {
            new JspTagLifecycle(pageContext, null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>addInterceptor()</code> method throws a 
     * <code>NullPointerException</code> when passed a <code>null</code>
     * <code>Interceptor</code> reference.
     */
    public void testAddInterceptorWithNull()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.addInterceptor(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>addNestedTag()</code> method throws a 
     * <code>NullPointerException</code> when passed a <code>null</code>
     * <code>Interceptor</code> reference.
     */
    public void testAddNestedTagWithNull()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.addNestedTag(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>addNestedText()</code> method throws a 
     * <code>NullPointerException</code> when passed a <code>null</code>
     * <code>Interceptor</code> reference.
     */
    public void testAddNestedTextWithNull()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.addNestedText(null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>assertScopedVariableExposed()</code> method
     * throws a <code>NullPointerException</code> when passed a
     * <code>null</code> name.
     */
    public void testAssertScopedVariableExposedWithNullName()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.expectScopedVariableExposed(null, new Object[] {"value"});
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>assertScopedVariableExposed()</code> method
     * throws a <code>NullPointerException</code> when passed a
     * <code>null</code> reference as expected values array.
     */
    public void testAssertScopedVariableExposedWithNullExpectedValues()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.expectScopedVariableExposed("name", null);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>assertScopedVariableExposed()</code> method
     * throws a <code>IllegalArgumentException</code> when passed an empty
     * expected values array.
     */
    public void testAssertScopedVariableExposedWithEmptyExpectedValues()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.expectScopedVariableExposed("name", new Object[0]);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests whether the <code>assertScopedVariableExposed()</code> method
     * throws a <code>IllegalArgumentException</code> when passed an invalid
     * scope identifier.
     */
    public void testAssertScopedVariableExposedWithIllegalScope()
    {
        try
        {
            JspTagLifecycle lifecycle =
                new JspTagLifecycle(pageContext, new OutTag());
            lifecycle.expectScopedVariableExposed(
                "name", new Object[]{"value"}, 0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // expected
        }
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with a proper, literal value for
     * it's <code>value</code> attribute.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTag()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue("Value");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endOutTag(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Value", output);
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with a proper, literal value for
     * it's <code>value</code> attribute that contains special XML characters
     * that need to be escaped.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagEscapeXml()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue("<value/>");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endOutTagEscapeXml(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("&lt;value/&gt;", output);
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with a proper, literal value for
     * it's <code>value</code> attribute that contains special XML characters
     * that need to be escaped.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagNoEscapeXml()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        tag.setEscapeXml("false");
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue("<value/>");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endOutTagNoEscapeXml(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("<value/>", output);
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with <code>null</code> for
     * it's <code>value</code> attribute, and a proper, literal value for it's
     * <code>default</code> attribute.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagDefaultAttribute()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue(null);
        tag.setDefault("Default");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     *  
     * @param theResponse The HTTP response
     */
    public void endOutTagWithDefaultAttribute(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Default", output);
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with a proper, literal value
     * for it's <code>value</code> attribute, as well as a proper, literal 
     * value for it's <code>default</code> attribute. In this case, the value
     * of the <code>default</code> attribute should be ignored, which is 
     * asserted.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagDefaultAttributeIgnored()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue("Value");
        tag.setDefault("Default");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     *  
     * @param theResponse The HTTP response
     */
    public void endOutTagWithDefaultAttributeIgnored(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Value", output);
    }
    
    /**
     * Tests the &lt;c:out&gt;-Tag with a value that evaluates to
     * <code>null</code>, and the default value specified in the tag's body.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagDefaultBody()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue(null);
        lifecycle.addNestedText("Default");
        lifecycle.expectBodyEvaluated();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endOutTagDefaultBody(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Default", output);
    }
    
    /**
     * Tests the <code>&lt;c:out&gt;</code>-tag with a proper, literal value
     * for it's <code>value</code> attribute, as well the default value
     * specified in the tag's body. In this case, the tag's body content
     * should be ignored, which is asserted.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testOutTagDefaultBodyIgnored()
        throws JspException, IOException
    {
        OutTag tag = new OutTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setValue("Value");
        lifecycle.addNestedText("Default");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:out&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endOutTagDefaultBodyIgnored(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Value", output);
    }
    
    /**
     * Tests the &lt;c:set&gt;-tag with a proper, literal values for it's
     * <code>var</code> and <code>value</code> attributes. Verification is done
     * by checking the scoped variable stored by the tag.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testSetTag()
        throws JspException, IOException
    {
        SetTag tag = new SetTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setVar("Var");
        tag.setValue("Value");
        lifecycle.invoke();
        assertEquals("Value", pageContext.findAttribute("Var"));
    }
    
    /**
     * Tests the tag &lt;c:forEach&gt; by providing a comma-delimited list of 
     * string to it's <code>items</code> attributes, and checking the exposed
     * scoped variable on every iteration step.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testForEachTag()
        throws JspException, IOException
    {
        ForEachTag tag = new ForEachTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setVar("Item");
        tag.setItems("One,Two,Three");
        lifecycle.expectBodyEvaluated(3);
        lifecycle.expectScopedVariableExposed(
            "Item", new Object[] {"One", "Two", "Three"});
        lifecycle.invoke();
    }
    
    /**
     * Tests the tag &lt;c:forEach&gt; by providing a comma-delimited list of 
     * string to it's <code>items</code> attributes, and checking the exposed
     * scoped variable on every iteration step.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testForEachTagStatus()
        throws JspException, IOException
    {
        ForEachTag tag = new ForEachTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setVarStatus("status");
        tag.setBegin("0");
        tag.setEnd("2");
        lifecycle.expectBodyEvaluated(3);
        lifecycle.addInterceptor(new JspTagLifecycle.Interceptor()
        {
            public void evalBody(int theIteration, BodyContent theBody)
            {
                LoopTagStatus status = (LoopTagStatus)
                    pageContext.findAttribute("status");
                assertNotNull(status);
                if (theIteration == 0)
                {
                    assertEquals(0, status.getIndex());
                    assertEquals(1, status.getCount());
                    assertTrue(status.isFirst());
                    assertTrue(!status.isLast());
                }
                else if (theIteration == 1)
                {
                    assertEquals(1, status.getIndex());
                    assertEquals(2, status.getCount());
                    assertTrue(!status.isFirst());
                    assertTrue(!status.isLast());
                }
                else if (theIteration == 2)
                {
                    assertEquals(2, status.getIndex());
                    assertEquals(3, status.getCount());
                    assertTrue(!status.isFirst());
                    assertTrue(status.isLast());
                }
            }
        });
        lifecycle.invoke();
    }
    
    /**
     * Tests the conditional tag &lt;c:if&gt; by providing a proper, literal 
     * value to it's <code>test</code> attribute that evaluates to
     * the boolean value <code>true</code>. The test verifies the correct
     * behaviour by asserting that the tag's body is not skipped.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testIfTagTrue()
        throws JspException, IOException
    {
        IfTag tag = new IfTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setTest("true");
        lifecycle.addNestedText("Value");
        lifecycle.expectBodyEvaluated();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:if&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endIfTagTrue(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("Value", output);
    }
    
    /**
     * Tests the conditional tag &lt;c:if&gt; by providing a proper, literal 
     * value to it's <code>test</code> attribute that evaluates to
     * the boolean value <code>false</code>. The test verifies the correct
     * behaviour by asserting that the tag's body is not evaluated.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testIfTagFalse()
        throws JspException, IOException
    {
        IfTag tag = new IfTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setTest("false");
        lifecycle.addNestedText("Value");
        lifecycle.expectBodySkipped();
        lifecycle.invoke();
    }
    
    /**
     * Verifies that the response has been correctly rendered by the 
     * <code>&lt;c:if&gt;</code>-tag.
     * 
     * @param theResponse The HTTP response
     */
    public void endIfTagFalse(WebResponse theResponse)
    {
        String output = theResponse.getText();
        assertEquals("", output);
    }
    
    /**
     * Tests the &lt;c:when&gt;-tag correctly nested inside a &lt;c:choose&gt;
     * tag, and providing a proper, literal value to it's <code>test</code>
     * attribute that evaluates to the boolean value <code>true</code>. The test
     * verifies the correct behaviour by asserting that the tag's body is not
     * skipped.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testWhenTag()
        throws JspException, IOException
    {
        ChooseTag chooseTag = new ChooseTag();
        JspTagLifecycle chooseLifecycle =
            new JspTagLifecycle(pageContext, chooseTag);
        
        WhenTag whenTag = new WhenTag();
        JspTagLifecycle whenLifecycle =
            chooseLifecycle.addNestedTag(whenTag);
        whenTag.setTest("true");
        whenLifecycle.expectBodyEvaluated();
        
        chooseLifecycle.invoke();
    }
    
    /**
     * Tests the &lt;c:when&gt;-tag correctly nested inside a &lt;c:choose&gt;
     * tag, and providing a proper, literal value to it's <code>test</code>
     * attribute that evaluates to the boolean value <code>true</code>. However,
     * an earlier instance of the <code>&lt;c:when&gt;</code> tag nested in the
     * parent has already succeeded, so this test asserts that the body of the
     * later <code>&lt;c:when&gt;</code> does not get evaluated.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testWhenTagNoPermission()
        throws JspException, IOException
    {
        ChooseTag chooseTag = new ChooseTag();
        JspTagLifecycle chooseLifecycle =
            new JspTagLifecycle(pageContext, chooseTag);
        
        WhenTag whenTag = new WhenTag();
        JspTagLifecycle whenLifecycle =
            chooseLifecycle.addNestedTag(whenTag);
        whenTag.setTest("false");
        whenLifecycle.expectBodySkipped();
        
        OtherwiseTag otherwiseTag = new OtherwiseTag();
        JspTagLifecycle otherwiseLifecycle =
            chooseLifecycle.addNestedTag(otherwiseTag);
        otherwiseLifecycle.expectBodyEvaluated();
        
        chooseLifecycle.invoke();
    }
    
    /**
     * Tests te <code>&lt;c:when&gt;</code> tag not nested inside a 
     * <code>&lt;c:choose&gt;</code> tag. The test expects a
     * <code>JspException</code> to be thrown.
     * 
     * @throws JspException If the tag throws a JSPException
     * @throws IOException If the tag throws an IOException
     */
    public void testWhenTagWithoutChooseTag()
        throws JspException, IOException
    {
        WhenTag tag = new WhenTag();
        JspTagLifecycle lifecycle = new JspTagLifecycle(pageContext, tag);
        tag.setTest("true");
        try
        {
            lifecycle.invoke();
            fail("Expected JSPTagException");
        } 
        catch (JspTagException expected)
        {
            // expected
        }
    }
    
}
