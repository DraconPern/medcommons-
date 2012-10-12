package org.cometd.javascript;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * @version $Revision: 1036 $ $Date: 2010-03-22 13:17:18 -0400 (Mon, 22 Mar 2010) $
 */
public interface ThreadModel
{
    void init() throws Exception;

    void destroy() throws Exception;

    Object evaluate(URL url) throws IOException;

    Object evaluate(String scriptName, String script);

    Object execute(Scriptable scope, Scriptable thiz, Function function, Object... arguments);

    void define(Class clazz) throws InvocationTargetException, IllegalAccessException, InstantiationException;

    Object get(String name);
}
