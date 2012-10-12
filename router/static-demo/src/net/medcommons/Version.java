/*
 * $Id: $
 */
package net.medcommons;

import java.io.DataInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 */
public class Version {
    
    private static Logger log = Logger.getLogger(Version.class); 
    
    /**
     * Cached version string
     */
    private static String version = null;
    
    /**
     * Cached time stamp string
     */
    private static String timeStamp = null;
    
    /**
     * Returns a version string reflecting the version of the current code.
     */
    public static String getVersionString() {
        if (version == null) {
            try {
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("net/medcommons/version.txt");
                DataInputStream stream = new DataInputStream(inputStream);
                version = stream.readLine().trim();
                stream.close();
                inputStream.close();
            }
            catch(Throwable t) {
                log.error("Unable to determine version",t);
                if(version == null)
                    version = "Unknown";
            }
        }
        return version;
    }
    
    /**
     * Returns a version string reflecting the version of the current code.
     */
    public static String getBuildTime() {
        if (timeStamp == null) {
            try {
                InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("net/medcommons/timestamp.txt");
                DataInputStream stream = new DataInputStream(inputStream);
                timeStamp = stream.readLine().trim();
                stream.close();
                inputStream.close();
            }
            catch(Throwable t) {
                log.error("Unable to determine build time stamp",t);
                if(timeStamp == null)
                    timeStamp = "Unknown";
            }
        }
        return timeStamp;
    }    
}
