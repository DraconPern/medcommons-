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

import java.io.Serializable;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.internal.client.AssertionFailedErrorWrapper;
import org.apache.cactus.internal.client.ServletExceptionWrapper;
import org.apache.cactus.internal.util.JUnitVersionHelper;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;

/**
 * Verifies the correct handling of exceptions that happen when running
 * inside the server. Specifically verifies that serializable,
 * non-serializable and {@link AssertionFailedError} exceptions are 
 * correctly propagated to the client side.
 *
 * @version $Id: TestServerSideExceptions.java 238900 2004-04-10 16:11:26Z vmassol $
 */
public class TestServerSideExceptions extends ServletTestCase
{
    /**
     * Not serializable exception.
     */
    public class NotSerializableException extends Exception
    {
        /**
         * @param theMessage the exception message
         */
        public NotSerializableException(String theMessage)
        {
            super(theMessage);
        }
    }

    /**
     * Serializable exception.
     */
    public class SerializableException extends Exception 
        implements Serializable
    {
        /**
         * @param theMessage the exception message
         */
        public SerializableException(String theMessage)
        {
            super(theMessage);
        }
    }

    /**
     * @param theTestName the test name to verify
     * @return true if the test name to verify corresponds to the currently
     *         executing test
     */
    private boolean checkName(String theTestName)
    {
        return JUnitVersionHelper.getTestCaseName(this).equals(
            theTestName);
    }

    /**
     * Intercepts running test cases to check for normal exceptions.
     * 
     * @exception Throwable on test failure
     */
    public void runBare() throws Throwable
    {
        try
        {
            super.runBare();
        }
        catch (AssertionFailedErrorWrapper e)
        {
            // If the test case is "testAssertionFailedError" and the exception
            // is of type AssertionFailedError and contains the text
            // "test assertion failed error", then the test is ok.
            if (checkName("testAssertionFailedError"))
            {
                assertEquals(AssertionFailedError.class.getName(), 
                    e.getWrappedClassName());
                assertEquals("test assertion failed error", e.getMessage());
                return;
            }

            // If the test case is "testComparisonFailure" and the exception
            // is of type ComparisonFailure and contains the text
            // "test comparison failure", then the test is ok.
            else if (checkName("testComparisonFailure"))
            {
                assertEquals(ComparisonFailure.class.getName(), 
                    e.getWrappedClassName());
                assertEquals("test comparison failure expected:<some...> "
                    + "but was:<other...>", e.getMessage());
                return;
            }
        }
        catch (ServletExceptionWrapper e)
        {
            // If the test case is "testExceptionNotSerializable" and the
            // exception is of type
            // TestServletTestCaseHelper1_ExceptionNotSerializable
            // and contains the text "test non serializable exception", then
            // the test is ok.
            if (checkName("testExceptionNotSerializable"))
            {
                assertEquals(NotSerializableException.class.getName(), 
                    e.getWrappedClassName());
                assertEquals("test non serializable exception", 
                    e.getMessage());
                return;
            }

            // If the test case is "testExceptionSerializable" and the exception
            // is of type TestServletTestCaseHelper1_ExceptionSerializable
            // and contains the text "test serializable exception", then
            // the test is ok.
            else if (checkName("testExceptionSerializable"))
            {
                assertEquals(SerializableException.class.getName(), 
                    e.getWrappedClassName());
                assertEquals("test serializable exception", e.getMessage());
                return;
            }
        }

        throw new AssertionFailedError("Unexpected test ["
            + JUnitVersionHelper.getTestCaseName(this) + "]");
    }

    //-------------------------------------------------------------------------

    /**
     * Raises an <code>AssertionFailedError</code> exception. The exception is
     * caught in
     * <code>TestServletTestCase_InterceptorServletTestCase.runTest()</code>.
     * This is to verify that <code>AssertionFailedError</code> raised on the
     * server side are properly propagated on the client side.
     */
    public void testAssertionFailedError()
    {
        throw new AssertionFailedError("test assertion failed error");
    }

    //-------------------------------------------------------------------------

    /**
     * Raises a non serializable exception. The exception is
     * caught in
     * <code>TestServletTestCase_InterceptorServletTestCase.runTest()</code>.
     * This is to verify that non serializable exceptions raised on the
     * server side are properly propagated on the client side.
     *
     * @exception NotSerializableException the non serializable exception to
     *             throw
     */
    public void testExceptionNotSerializable()
        throws NotSerializableException
    {
        throw new NotSerializableException("test non serializable exception");
    }

    //-------------------------------------------------------------------------

    /**
     * Raises a serializable exception. The exception is
     * caught in
     * <code>TestServletTestCase_InterceptorServletTestCase.runTest()</code>.
     * This is to verify that serializable exceptions raised on the
     * server side are properly propagated on the client side.
     *
     * @exception SerializableException the serializable exception to throw
     */
    public void testExceptionSerializable() throws SerializableException
    {
        throw new SerializableException("test serializable exception");
    }

    //-------------------------------------------------------------------------

    /**
     * Verify that the new {@link ComparisonFailure} introduced in JUnit 3.8.1
     * is correctly reported as a failure and not as an error in the Test 
     * Runner when Cactus is used.
     */
    public void testComparisonFailure()
    {
        throw new ComparisonFailure("test comparison failure", "some value", 
            "other value");
    }

}
