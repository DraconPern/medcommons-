package org.cometd.javascript.jquery;

import org.cometd.javascript.Latch;

/**
 * @version $Revision: 1036 $ $Date: 2010-03-22 13:17:18 -0400 (Mon, 22 Mar 2010) $
 */
public class CometdExtensionExceptionCallbackTest extends AbstractCometdJQueryTest
{
    public void testOutgoingExtensionExceptionCallback() throws Exception
    {
        defineClass(Latch.class);
        evaluateScript("var latch = new Latch(1);");
        Latch latch = (Latch)get("latch");
        evaluateScript("var connectLatch = new Latch(1);");
        Latch connectLatch = get("connectLatch");
        evaluateScript("" +
                "$.cometd.configure({url: '" + cometdURL + "', logLevel: 'debug'});" +
                "$.cometd.addListener('/meta/connect', function(message) { connectLatch.countDown(); });" +
                "$.cometd.registerExtension('testext', {" +
                "   outgoing: function(message) { throw 'test'; }" +
                "});" +
                "" +
                "$.cometd.onExtensionException = function(exception, extensionName, outgoing, message) " +
                "{" +
                "   if (exception === 'test' && extensionName === 'testext' && outgoing === true)" +
                "   {" +
                "       this.unregisterExtension(extensionName);" +
                "       latch.countDown();" +
                "   }" +
                "};" +
                "" +
                "$.cometd.handshake();");
        assertTrue(latch.await(1000));

        assertTrue(connectLatch.await(1000));

        evaluateScript("$.cometd.disconnect(true);");
    }

    public void testIncomingExtensionExceptionCallback() throws Exception
    {
        defineClass(Latch.class);
        evaluateScript("var latch = new Latch(1);");
        Latch latch = (Latch)get("latch");
        evaluateScript("var connectLatch = new Latch(1);");
        Latch connectLatch = get("connectLatch");
        evaluateScript("" +
                "$.cometd.configure({url: '" + cometdURL + "', logLevel: 'debug'});" +
                "$.cometd.addListener('/meta/connect', function(message) { connectLatch.countDown(); });" +
                "$.cometd.registerExtension('testext', {" +
                "   incoming: function(message) { throw 'test'; }" +
                "});" +
                "" +
                "$.cometd.onExtensionException = function(exception, extensionName, outgoing, message) " +
                "{" +
                "   if (exception === 'test' && extensionName === 'testext' && outgoing === false)" +
                "   {" +
                "       this.unregisterExtension(extensionName);" +
                "       latch.countDown();" +
                "   }" +
                "};" +
                "" +
                "$.cometd.handshake();");
        assertTrue(latch.await(1000));

        assertTrue(connectLatch.await(1000));

        evaluateScript("$.cometd.disconnect(true);");
    }
}
