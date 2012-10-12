package org.cometd.javascript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @version $Revision: 1035 $ $Date: 2010-03-22 06:59:52 -0400 (Mon, 22 Mar 2010) $
 */
public class XMLHttpRequestExchange extends ScriptableObject
{
    private CometdExchange exchange;

    public XMLHttpRequestExchange()
    {
    }

    public void jsConstructor(Object threadModel, Scriptable scope, Scriptable thiz, Function function, String method, String url, boolean async)
    {
        exchange = new CometdExchange((ThreadModel)threadModel, scope, thiz, function, method, url, async);
    }

    public String getClassName()
    {
        return "XMLHttpRequestExchange";
    }

    public HttpExchange getHttpExchange()
    {
        return exchange;
    }

    public boolean isAsynchronous()
    {
        return exchange.isAsynchronous();
    }

    public void await() throws InterruptedException
    {
        exchange.waitForDone();
        exchange.notifyReadyStateChange();
    }

    public void jsFunction_addRequestHeader(String name, String value)
    {
        exchange.addRequestHeader(name, value);
    }

    public void jsFunction_setOnReadyStateChange(Scriptable thiz, Function function)
    {
        exchange.setOnReadyStateChange(thiz, function);
    }

    public String jsGet_method()
    {
        return exchange.getMethod();
    }

    public void jsFunction_setRequestContent(String data) throws UnsupportedEncodingException
    {
        exchange.setRequestContent(data);
    }

    public int jsGet_readyState()
    {
        return exchange.getReadyState();
    }

    public String jsGet_responseText()
    {
        return exchange.getResponseText();
    }

    public int jsGet_responseStatus()
    {
        return exchange.getResponseStatus();
    }

    public String jsGet_responseStatusText()
    {
        return exchange.getResponseStatusText();
    }

    public void jsFunction_cancel()
    {
        exchange.cancel();
    }

    public String jsFunction_getAllResponseHeaders()
    {
        return exchange.getAllResponseHeaders();
    }

    public String jsFunction_getResponseHeader(String name)
    {
        return exchange.getResponseHeader(name);
    }

    public static class CometdExchange extends ContentExchange
    {
        public enum ReadyState
        {
            UNSENT, OPENED, HEADERS_RECEIVED, LOADING, DONE
        }

        private final ThreadModel threads;
        private final Scriptable scope;
        private volatile Scriptable thiz;
        private volatile Function function;
        private final boolean async;
        private volatile boolean aborted;
        private volatile ReadyState readyState = ReadyState.UNSENT;
        private volatile String responseText;
        private volatile String responseStatusText;

        public CometdExchange(ThreadModel threads, Scriptable scope, Scriptable thiz, Function function, String method, String url, boolean async)
        {
            super(true);
            this.threads = threads;
            this.scope = scope;
            this.thiz = thiz;
            this.function = function;
            setMethod(method == null ? "GET" : method.toUpperCase());
            setURL(url);
            this.async = async;
            aborted = false;
            readyState = ReadyState.OPENED;
            responseStatusText = null;
            getRequestFields().clear();
            if (async)
                notifyReadyStateChange();
        }

        public boolean isAsynchronous()
        {
            return async;
        }

        private void setOnReadyStateChange(Scriptable thiz, Function function)
        {
            this.thiz = thiz;
            this.function = function;
        }

        private void notifyReadyStateChange()
        {
            threads.execute(scope, thiz, function);
        }

        @Override
        public void cancel()
        {
            super.cancel();
            aborted = true;
            responseText = null;
            getRequestFields().clear();
            if (!async || readyState == ReadyState.HEADERS_RECEIVED || readyState == ReadyState.LOADING)
            {
                readyState = ReadyState.DONE;
                notifyReadyStateChange();
            }
            readyState = ReadyState.UNSENT;
        }

        public int getReadyState()
        {
            return readyState.ordinal();
        }

        public String getResponseText()
        {
            return responseText;
        }

        public String getResponseStatusText()
        {
            return responseStatusText;
        }

        public void setRequestContent(String content) throws UnsupportedEncodingException
        {
            setRequestContent(new ByteArrayBuffer(content, "UTF-8"));
        }

        public String getAllResponseHeaders()
        {
            return getResponseFields().toString();
        }

        public String getResponseHeader(String name)
        {
            return getResponseFields().getStringField(name);
        }

        @Override
        protected void onResponseStatus(Buffer version, int status, Buffer statusText) throws IOException
        {
            super.onResponseStatus(version, status, statusText);
            this.responseStatusText = new String(statusText.asArray(), "UTF-8");
        }

        @Override
        protected void onResponseHeaderComplete() throws IOException
        {
            if (!aborted)
            {
                if (async)
                {
                    readyState = ReadyState.HEADERS_RECEIVED;
                    notifyReadyStateChange();
                }
            }
        }

        @Override
        protected void onResponseContent(Buffer buffer) throws IOException
        {
            super.onResponseContent(buffer);
            if (!aborted)
            {
                if (async)
                {
                    if (readyState != ReadyState.LOADING)
                    {
                        readyState = ReadyState.LOADING;
                        notifyReadyStateChange();
                    }
                }
            }
        }

        @Override
        protected void onResponseComplete() throws IOException
        {
            if (!aborted)
            {
                responseText = getResponseContent();
                readyState = ReadyState.DONE;
                if (async)
                    notifyReadyStateChange();
            }
        }
    }
}