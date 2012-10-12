/*
 * $Id$
 * Created on 11/01/2007
 */
package net.medcommons.modules.activitylog;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import net.medcommons.modules.services.interfaces.AccountSpec;
import net.medcommons.modules.services.interfaces.ActivityEvent;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.ActivityLogService;
import net.medcommons.modules.services.interfaces.ServiceException;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * An ultra simple activity log service that simply writes the lines to a CSV
 * file in the user's account. 
 * 
 * @author ssadedin
 */
public class CSVActivityLogService implements ActivityLogService {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(CSVActivityLogService.class);

    private static final String ACTIVITY_LOG_FILE_NAME = "activity_log.csv";
    
    private String sessionId;
    
    protected CSVActivityLogService() {
        
    }
    
    /**
     * Create a CSVActivityLogService for the given session.
     * 
     * @param sessionId
     */
    public CSVActivityLogService(String sessionId) {
        super();
        this.sessionId = sessionId;
    }

    /**
     * Opens the CSV file and reads it from the local file system.
     */
    public Collection<ActivityEvent> load(String accountId, int begin, int limit) throws IOException {
        
        try {
            log.info("Querying activity log for user " + accountId + " from " + begin + " for " + limit + " records");
            Reader r = getReader(accountId);
            
            boolean fromTail = true;
            if(limit == READ_ALL) {
                limit = Integer.MAX_VALUE; 
            }
            
            CSVReader csv = new CSVReader(r, CSVReader.DEFAULT_SEPARATOR, CSVReader.DEFAULT_QUOTE_CHARACTER, begin);
            String [] nextLine = null;
            ArrayList<ActivityEvent> result = new ArrayList<ActivityEvent>();
            while( (nextLine = csv.readNext()) != null) {
                String sessionId = nextLine.length >= 5 ? nextLine[4] : null; 
                String type = nextLine.length >= 6 ? nextLine[5] : null; 
                String idType = nextLine.length >= 7 ? nextLine[6] : "";
                String fn = nextLine.length >= 8 ? nextLine[7] : "";
                String ln = nextLine.length >= 9 ? nextLine[8] : "";
                String mcid = nextLine.length >= 10 ? nextLine[9] : "";
                String pin = nextLine.length >= 11 ? nextLine[10] : ""; 
                ActivityEvent a = new ActivityEvent( Long.parseLong(nextLine[0])*1000,sessionId, nextLine[1], new AccountSpec(nextLine[2],idType, fn, ln), accountId, nextLine[3], pin);
                if(type != null) {
                    a.setType(ActivityEventType.valueOf(type));
                }
                if(mcid != null && !"".equals(mcid)) {
                    a.getSourceAccount().setMcId(mcid);
                }
                
                result.add(0,a);
                if(fromTail) { 
                    if(result.size()>limit) {
                        result.remove(result.size()-1);
                    }
                }
                else
                if(limit-- == 0) 
                    break;
            }
            csv.close();
            log.debug("Found " + result.size() + " activity log records for account " + accountId);
            return result;
        }
        catch(FileNotFoundException exNotFound) {
            log.info("Account " + accountId + " does not have an activity log");
            return new ArrayList<ActivityEvent>();
        }
    }

    /**
     * Appends a line to the user's current activity log (if they have one).
     */
    public void log(ActivityEvent event) throws IOException {        
        
        if(event.getSessionId() == null) {
            event.setSessionId(this.sessionId);
        }
        
        StringWriter w = new StringWriter();
        CSVWriter csv = new CSVWriter(w);
        String fn = event.getSourceAccount().getFirstName();
        String ln = event.getSourceAccount().getLastName();
        String mcid = event.getSourceAccount().getMcId();
        String pin = event.getPin();
        csv.writeNext(new String [] { String.valueOf(event.getTimeStampMs()/1000),
                        event.getDescription(),
                        event.getSourceAccountId(),
                        event.getTrackingNumber(),
                        event.getSessionId(),
                        event.getType().name(),
                        event.getSourceAccount().getIdType(),
                        fn == null ? "" : fn,
                        ln == null ? "" : ln,
                        mcid == null ? "" : mcid,
                        pin == null ? "" : pin
                      });        
        w.flush();
        csv.close();
        
        Writer out = getWriter(event);        
        out.write(w.toString());
        out.close();
        Logger.getLogger("activity."+event.getAffectedAccountId()).info(w.toString());
    }

    protected Reader getReader(String accountId) throws FileNotFoundException {
        return new FileReader(this.getAccountDirectory(accountId).getAbsolutePath() + File.separator + ACTIVITY_LOG_FILE_NAME);
    }

    private File getAccountDirectory(String accountId) {
        return new File("data/Repository/"+accountId); 
    }

    protected Writer getWriter(ActivityEvent event) throws IOException {
        File f = new File(getAccountLogFileName(event.getAffectedAccountId()));
        if(!f.exists()) { 
            f.getParentFile().mkdirs();
        }
        
        Writer w = new FileWriter( f, true);
        return w;
    }

    /**
     * @param event
     * @return
     */
    private String getAccountLogFileName(String accountId) {
        return this.getAccountDirectory(accountId).getAbsolutePath() + File.separator + ACTIVITY_LOG_FILE_NAME;
    } 

    public void clear(String accountId) throws ServiceException {
        File logFile = new File(this.getAccountLogFileName(accountId));
        logFile.delete();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
