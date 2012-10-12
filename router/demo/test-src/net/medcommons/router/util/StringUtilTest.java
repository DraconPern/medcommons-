/*
 * $Id$
 * Created on 5/12/2005
 */
package net.medcommons.router.util;

import net.medcommons.modules.utils.Str;
import junit.framework.TestCase;

public class StringUtilTest extends TestCase {

    public StringUtilTest(String arg0) {
        super(arg0);
    }
    
    public void testEscapeHTMLEntities() throws Exception {
        assertEquals(Str.escapeHTMLEntities("hello"),"hello");
        assertEquals(Str.escapeHTMLEntities("&"),"&amp;");
        assertEquals(Str.escapeHTMLEntities("hello&wor>ld"),"hello&amp;wor&gt;ld");
        
    }

}
