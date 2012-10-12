/*
 * $Id: Log.java 77 2004-05-22 01:09:20Z mquigley $
 */

package net.medcommons.central.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

public class Log {

  public static final String JNDI_NAME = "medcommons/Log";
  
  private static final String LOG_PATH = "../server/central/data/main.log";
  
  private static Logger log = Logger.getLogger(Log.class);
  
  public Log() throws LogServiceException {
    init();
    log.info("Created new Log instance."); 
  }
  
  public void truncateLog() throws LogServiceException {
    try {
      File f = new File(LOG_PATH);
      f.delete();
       
    } catch(Exception e) {
      throw new LogServiceException("Unable to truncate the log: " + e.toString());
    }
  }
  
  public void writeLog(String logRecord) throws LogServiceException {
    try {
      FileOutputStream logStream = new FileOutputStream(LOG_PATH, true);
      
      logStream.write(new String(logRecord + "\n").getBytes());
      
      logStream.close();
      
      log.info("Wrote Log: " + logRecord);
      
    } catch(Exception e) {
      throw new LogServiceException("Unable to write log: " + e.toString(), e);
    }
  }
  
  public String[] readLog(String filter) throws LogServiceException {
    try {
      ArrayList logLines = new ArrayList();
      
      BufferedReader reader = new BufferedReader(new FileReader(new File(LOG_PATH)));
      
      String logLine = null;
      while((logLine = reader.readLine()) != null) {
        if(logLine.indexOf(filter) != -1) {
          logLines.add(logLine); 
        } 
      }

      reader.close();
      
      String[] retLines = new String[logLines.size()];
      for(int i = 0; i < logLines.size(); i++) {
        retLines[i] = (String) logLines.get(i);
      }

      return retLines;

    } catch(Exception e) {
      throw new LogServiceException("Unable to read log: " + e.toString(), e); 
    } 
  }
  
  public static Log getInstance() throws NamingException {
    return (Log) new InitialContext().lookup(JNDI_NAME);
  }
  
  private void init() throws LogServiceException {
    // Ensure that the data directory exists.
    File dataRoot = new File("../server/central/data");
    if(dataRoot.mkdirs()) {
      log.info("Created data directory: " + dataRoot.getPath()); 
    }
  }

}
