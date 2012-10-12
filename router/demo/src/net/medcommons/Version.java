/*
 * $Id$
 */
package net.medcommons;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Returns revision information about the current codebase
 */
public class Version {
    
    private static Logger log = Logger.getLogger(Version.class); 

    /**
     * Cached revision string
     */
    private static String revision = null;
    
    /**
     * Cached time stamp string
     */
    private static String timeStamp = null;
    
    /**
     * The public version string
     */
    private static String versionString = null;
    
    /**
     * The release notes
     */
    private static String releaseNotes = null;
   
    /**
     * The revision of the schema (last modification of files in schema dir)
     * when this codebase was built.
     */
    private static String codeSchemaRevision = null;
    
    /**
     * The revision of the schema of the installed database
     */
    private static String dbSchemaRevision = null;
         
    /**
     * Returns a revision string reflecting the revision of the current code.
     */
    public static String getRevision() {
        if (revision == null) {
            try {
              revision = readResource("net/medcommons/version.txt");
              if (revision != null) {
                revision = revision.trim();                
              }
            }
            catch(Throwable t) {
                log.error("Unable to determine revision",t);
                if(revision == null)
                    revision = "Unknown";
            }
        }
        return revision;
    }
    
    /**
     * Returns a revision string reflecting the revision of the current code.
     */
    public static String getBuildTime() {
      if (timeStamp == null) {
        try {
          timeStamp = readResource("net/medcommons/timestamp.txt");
          if (timeStamp != null) {
            timeStamp = timeStamp.trim();
          }
        }
        catch (Throwable t) {
          log.error("Unable to determine build time stamp", t);
          if (timeStamp == null)
            timeStamp = "Unknown";
        }
      }
      return timeStamp;
    }

    /**
     * Returns a revision string reflecting the revision of the current code.
     */
    public static String getVersionString() {
      if (versionString == null) {
        try {
          versionString = readResource("net/medcommons/public_version.txt");
          if(versionString != null) {
            versionString = versionString.trim();
          }
        }
        catch (Throwable t) {
          log.error("Unable to determine public revision", t);
          if (versionString == null)
            versionString = "Unknown";
        }
      }
      return versionString;
    }
    
    /**
     * Returns a revision string reflecting the revision of the current code.
     */
    public static String getCodeSchemaVersion() {
      if (codeSchemaRevision == null) {
        try {
          codeSchemaRevision = readResource("net/medcommons/schema_version.txt");
        }
        catch (Throwable t) {
            log.info("Unable to determine code schema version (" +t.getMessage() + ")");
            codeSchemaRevision = null;
        }
      }
      return codeSchemaRevision;
    }
    
    /**
     * Returns a revision string reflecting the revision of the current code.
     */
    public static String getDbSchemaVersion() {
      if (dbSchemaRevision == null) {
        try {
          dbSchemaRevision = readFile("data/derby/schema_version.txt");
        }
        catch (Throwable t) {
            // If no db schema version is present, initialize with the schema that the code expects.
            String codeShema = getCodeSchemaVersion();
            if(codeShema != null) {
                log.info("DB Schema version file not found: initializing with version " + codeShema);           
                try {
                    PrintStream out = new PrintStream("data/derby/schema_version.txt");
                    out.print(codeShema);
                    out.close();
                }
                catch (FileNotFoundException e) {
                    log.warn("Unable to initialize schema version", e);
               }
            }
            else {
                log.warn("No db schema version file was found and no code schema was specified. DB schema version not initialized.");
            }
        }
      }
      return dbSchemaRevision;
    }
    
    /**
     * Returns a revision string reflecting the revision of the current code.
     */
    public static String getReleaseNotes() {
      if (releaseNotes == null) {
        try {
          releaseNotes = readResource("net/medcommons/ReleaseNotes.txt");
        }
        catch (Throwable t) {
          log.error("Unable to determine public revision", t);
          if (releaseNotes == null)
            releaseNotes = "Unknown";
        }
      }
      return releaseNotes;
    }
    
    /**
     * Reads a resource from the classpath
     * 
     * @param string
     * @return
     * @throws IOException
     */
    private static String readResource(String path) throws IOException {
      StringBuffer buffer = new StringBuffer(100);
      InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
      if(inputStream == null) {
          log.error("Unable to read stream from path " + path);
          return null;
      }
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
          buffer.append(line + "\n");
      }
      reader.close();
      inputStream.close();
      return buffer.toString();
    }
    
    /**
     * Reads a resource from the classpath
     * 
     * @param string
     * @return
     * @throws IOException
     */
    private static String readFile(String path) throws IOException {
      StringBuffer buffer = new StringBuffer(100);
      InputStream inputStream = new FileInputStream(path);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
          buffer.append(line + "\n");
      }
      reader.close();
      inputStream.close();
      return buffer.toString();
    }}
