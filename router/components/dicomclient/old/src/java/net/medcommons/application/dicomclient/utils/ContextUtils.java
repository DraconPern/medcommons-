package net.medcommons.application.dicomclient.utils;

public class ContextUtils {
	private static boolean jdk6OorLater = false;
	
	public static boolean isJDK6Orlater(){
		return(true);
	}
	// On Mac using SoyLatte- this stalls. Need to find out where it doesn't return.
	/*
	public static boolean isJDK6Orlater(){
		if (true) return (false);// Temp hack to work on 32 bit mac.
    	if (jdk6OorLater)
    		return(jdk6OorLater);
    	else{
	    	String jvm_version = System.getProperty("java.version");
	    	System.out.println("Java version is " + jvm_version);
	    	String[] versionInfo = jvm_version.split("\\.");
	    	for (int i=0;i<versionInfo.length;i++){
	    		System.out.println(i + " " + versionInfo[i]);
	    	}
	    	int versionNumber = Integer.parseInt(versionInfo[1]);
	    	System.out.println("Version is " + versionNumber);
	    	if (versionNumber > 5){
	    		jdk6OorLater=true;
	    	}
    	}
    	return(jdk6OorLater);
    }
    */
}
