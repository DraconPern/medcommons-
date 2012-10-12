package net.medcommons.modules.dicom.test;



import net.medcommons.modules.dicom.ParseFile;

import junit.framework.TestCase;

public class ParseTest extends TestCase{
	
	
	
	public void testFormatInt(){
		
		assertEquals("40", ParseFile.formatIntegerValue("40"));
		assertEquals(null, ParseFile.formatIntegerValue(null));
		assertEquals("13590", ParseFile.formatIntegerValue("13590.500"));
		assertEquals("25831", ParseFile.formatIntegerValue("25831.000"));
		assertEquals("0", ParseFile.formatIntegerValue("0.0"));
		assertEquals("0", ParseFile.formatIntegerValue("0"));
		assertEquals("-1024", ParseFile.formatIntegerValue("-1024"));
		assertEquals("1024", ParseFile.formatIntegerValue("1024"));
	}
	
}
