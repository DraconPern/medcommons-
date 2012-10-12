package org.cometd.javascript;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.eclipse.jetty.client.HttpClient;
import org.mozilla.javascript.ScriptableObject;

/**
 * Implementation of the XMLHttpRequest functionality using Jetty's HttpClient.
 *
 * @version $Revision: 1035 $ $Date: 2010-03-22 06:59:52 -0400 (Mon, 22 Mar 2010) $
 */
public class XMLHttpRequestClient extends ScriptableObject
{
    private HttpClient httpClient;

    public XMLHttpRequestClient()
    {
    }

    public void jsConstructor(int maxConnections) throws Exception
    {
        httpClient = new HttpClient();
        httpClient.setMaxConnectionsPerAddress(maxConnections);
        httpClient.setSoTimeout(0);
        httpClient.setIdleTimeout(300000);
        httpClient.setTimeout(300000);
        httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        httpClient.start();
    }

    public String getClassName()
    {
        return "XMLHttpRequestClient";
    }

    public void jsFunction_send(XMLHttpRequestExchange exchange) throws IOException
    {
        httpClient.send(exchange.getHttpExchange());
        try
        {
            if (!exchange.isAsynchronous())
                exchange.await();
        }
        catch (InterruptedException x)
        {
            throw new InterruptedIOException();
        }
    }
}
