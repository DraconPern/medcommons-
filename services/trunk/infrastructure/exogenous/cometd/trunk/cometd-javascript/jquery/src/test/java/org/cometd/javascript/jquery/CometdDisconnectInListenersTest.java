package org.cometd.javascript.jquery;

import org.cometd.javascript.Latch;

/**
 * @version $Revision: 1035 $ $Date: 2010-03-22 06:59:52 -0400 (Mon, 22 Mar 2010) $
 */
public class CometdDisconnectInListenersTest extends AbstractCometdJQueryTest
{
    public void testDisconnectInHandshakeListener() throws Exception
    {
        defineClass(Latch.class);

        evaluateScript("var connectLatch = new Latch(1);");
        Latch connectLatch = get("connectLatch");
        evaluateScript("var disconnectLatch = new Latch(1);");
        Latch disconnectLatch = get("disconnectLatch");

        evaluateScript("" +
                "$.cometd.configure({url: '" + cometdURL + "', logLevel: 'debug'});\n" +
                "$.cometd.addListener('/meta/handshake', function(message)" +
                "{\n" +
                "   $.cometd.disconnect();\n" +
                "});\n" +
                "$.cometd.addListener('/meta/connect', function(message)" +
                "{" +
                "   connectLatch.countDown();" +
                "});\n" +
                "$.cometd.addListener('/meta/disconnect', function(message)" +
                "{" +
                "   disconnectLatch.countDown();" +
                "});\n" +
                "" +
                "$.cometd.handshake();\n" +
                "");

        // Connect must not be called
        assertFalse(connectLatch.await(1000));

        assertTrue(disconnectLatch.await(1000));
    }

    public void testDisconnectInConnectListener() throws Exception
    {
        defineClass(Latch.class);

        evaluateScript("var connectLatch = new Latch(2);");
        Latch connectLatch = get("connectLatch");
        evaluateScript("var disconnectLatch = new Latch(1);");
        Latch disconnectLatch = get("disconnectLatch");

        evaluateScript("" +
                "$.cometd.configure({url: '" + cometdURL + "', logLevel: 'debug'});\n" +
                "$.cometd.addListener('/meta/connect', function(message)" +
                "{" +
                "   if (connectLatch.count == 2) " +
                "       $.cometd.disconnect();" +
                "   connectLatch.countDown();" +
                "});\n" +
                "$.cometd.addListener('/meta/disconnect', function(message)" +
                "{" +
                "   disconnectLatch.countDown();" +
                "});\n" +
                "" +
                "$.cometd.handshake();\n" +
                "");

        // Connect must be called only once
        assertFalse(connectLatch.await(1000));
        assertEquals(1L, connectLatch.jsGet_count());

        assertTrue(disconnectLatch.await(1000));
    }
}
