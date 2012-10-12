/*
 * $Id: CCRElementTest.java 1954 2007-08-24 12:04:41Z ssadedin $
 * Created on 24/08/2007
 */
package net.medcommons.phr.ccr;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

public class CCRElementTest extends TestCase {

    public CCRElementTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetJSON() throws ScriptException {
        CCRElement e = new CCRElement("Foo");
        e.createPath("Bar/Baz/Reference", "123");
        CCRElement c = new CCRElement("Reference");
        c.setText("FiggleBox");
        e.getChild("Bar").getChild("Baz").addChild(c);
        e.createPath("Bar/Baz/Bingo", "456");
        System.out.println(e.getJSON());
        eval("var x = " + e.getJSON()+";");
    }
    
    public void eval(String json) throws ScriptException {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
        jsEngine.eval(json);
    }

}
