package net.medcommons.application.dicomclient.utils;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public class ContextUtils {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ContextUtils.class);


	private static Boolean jdk60orLater = null;
	
	/*
	public static boolean isJDK6Orlater(){
		return(true);
	}
	*/
	
	public static boolean isJDK6Orlater(){
    	if(jdk60orLater != null)
    		return jdk60orLater;
    	
    	jdk60orLater = false;
    	try {
            Method m = java.io.File.class.getDeclaredMethod("setWritable", boolean.class, boolean.class);
            if(m != null) {
	            jdk60orLater = true;
	            System.out.println("JDK 1.6 available!");
            }
            else
                System.out.println("Java 1.6 not available due to no java.io.File.setWritable");
        }
    	catch (SecurityException e) {
    	    System.out.println("Java 1.6 not available due exception introspecting java.io.File.setWritable: " + e.getMessage());
        }
    	catch (NoSuchMethodException e) {
    	    System.out.println("Java 1.6 not available due exception introspecting java.io.File.setWritable" + e.getMessage());
        }
    	
    	// System property override is useful for testing
    	if("true".equals(System.getProperty("ddl.nojre16"))) {
    	    jdk60orLater = false;
    	}
    	    
    	return jdk60orLater;
    }
}
