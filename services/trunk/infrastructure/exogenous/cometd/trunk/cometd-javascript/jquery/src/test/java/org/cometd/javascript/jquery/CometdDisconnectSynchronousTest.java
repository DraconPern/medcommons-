package org.cometd.javascript.jquery;

import org.cometd.javascript.Latch;

/**
 * @version $Revision: 1036 $ $Date: 2010-03-22 13:17:18 -0400 (Mon, 22 Mar 2010) $
 */
public class CometdDisconnectSynchronousTest extends AbstractCometdJQueryTest
{
    public void testDisconnectSynchronous() throws Exception
    {
        defineClass(Latch.class);

        evaluateScript("var readyLatch = new Latch(1);");
        Latch readyLatch = get("readyLatch");
        evaluateScript("" +
                "$.cometd.configure({url: '" + cometdURL + "', logLevel: 'debug'});" +
                "$.cometd.addListener('/meta/connect', function(message) { readyLatch.countDown(); });" +
                "" +
                "$.cometd.handshake();");

        assertTrue(readyLatch.await(1000));

        evaluateScript("" +
                "var disconnected = false;" +
                "$.cometd.addListener('/meta/disconnect', function(message) { disconnected = true; });" +
                "$.cometd.disconnect(true);" +
                "window.assert(disconnected === true);" +
                "");
    }
}
