package org.cometd.javascript.jquery;

import java.util.Arrays;
import java.util.List;

import org.cometd.javascript.Latch;

/**
 * @version $Revision: 1091 $ $Date: 2010-04-09 06:15:42 -0400 (Fri, 09 Apr 2010) $
 */
public class CometdPublishHeldUntilHandshookTest extends AbstractCometdJQueryTest
{
    public void testPublishHeldUntilHandshook() throws Exception
    {
        defineClass(Latch.class);
        evaluateScript("" +
                "$.cometd.configure({url: '" + cometdURL + "', logLevel: 'debug'});" +
                "var latch = new Latch(2);" +
                "var savedChannels;" +
                "var channels = [];" +
                "$.cometd.registerExtension('test', {" +
                "    outgoing: function(message) " +
                "    {" +
                "        channels.push(message.channel);" +
                "    }" +
                "});" +
                "$.cometd.addListener('/meta/handshake', function(message)" +
                "{" +
                "    $.cometd.publish('/bar', {});" +
                "    $.cometd.batch(function()" +
                "    {" +
                "        $.cometd.subscribe('/foo', function(msg) { latch.countDown(); });" +
                "        $.cometd.publish('/foo', {});" +
                "    });" +
                "});" +
                "$.cometd.addListener('/meta/connect', function(message)" +
                "{" +
                "   /* Copy the array so that from now on it is not modified anymore */" +
                "   savedChannels = channels.slice(0);" +
                "   latch.countDown();" +
                "});" +
                "");
        Latch latch = (Latch)get("latch");
        evaluateScript("$.cometd.handshake();");

        assertTrue(latch.await(1000));

        Object jsChannels = get("savedChannels");
        Object[] channels = (Object[])jsToJava(jsChannels);
        assertNotNull(channels);
        List<Object> expectedChannels = Arrays.<Object>asList("/meta/handshake", "/bar", "/meta/subscribe", "/foo", "/meta/connect");
        assertEquals(expectedChannels, Arrays.asList(channels));

        evaluateScript("$.cometd.disconnect(true);");
    }
}