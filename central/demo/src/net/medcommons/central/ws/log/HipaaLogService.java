/*
 * $Id: HipaaLogService.java 72 2004-05-14 02:26:17Z mquigley $
 */

package net.medcommons.central.ws.log;

import net.medcommons.central.log.Log;

public class HipaaLogService {

  public void write(String logRecord) throws HipaaLogServiceException {
    try {
      Log l = Log.getInstance();
      l.writeLog(logRecord);
      
    } catch(Exception e) {
      throw new HipaaLogServiceException("Unable to write to the log: " + e.toString(), e); 
    }
  }
  
  public String[] read(String filter) throws HipaaLogServiceException {
    try {
      Log l = Log.getInstance();
      return l.readLog(filter);
       
    } catch(Exception e) {
      throw new HipaaLogServiceException("Unable to read the log: " + e.toString(), e); 
    }
  }

}
