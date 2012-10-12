/**
 * $Id$
 */
package net.medcommons.application.dicomclient.utils

import org.json.JSONObject
import net.medcommons.application.utils.JSONSimpleGETimport net.medcommons.application.dicomclient.ContextManagerimport org.apache.log4j.Loggerimport net.sourceforge.pbeans.Storeimport net.medcommons.application.dicomclient.DownloadHandlerimport net.medcommons.application.dicomclient.transactions.ContextStateimport net.medcommons.application.dicomclient.transactions.CxpDownloadimport net.medcommons.application.dicomclient.transactions.DownloadQueueimport java.lang.IllegalStateException
import java.util.concurrent.Futureimport net.medcommons.application.dicomclient.Configurationsimport groovy.xml.MarkupBuilder
import static net.medcommons.application.dicomclient.utils.ManagedTransaction.*


/**
 * Sends back the ID of this DDL if the security context 
 * allows it. 
 * <p>
 * For a new DDL, the ID will be sent back the first time it is queried and
 * for a period of time thereafter.
 * <p>
 * For an existing DDL the user will be prompted to allow the DDL 
 * to communicate with the page.
 * 
 * @author ssadedin
 */
public class QueryIDCommand implements Command {
     
    private static Logger log = Logger.getLogger(QueryIDCommand.class);
    
    /**
     * The maximum time in ms during which the DDL will return it's ID to a query
     * without prompting the user.
     */
    private static MAX_ID_QUERY_TIME_MS = 300000;

    public Future<JSONObject> execute(CommandBlock params) {
        
        Configurations cfg = ContextManager.get().getConfigurations()
        
        // If the configuration file is less than 5 minutes old then 
        // we allow the action implicitly because this is a new DDL
        File ddldbFile = new File(cfg.configurationFile.parentFile,"jetty")
        
        boolean showWarning = true;
        
        if(cfg.DDLIdentity == params.getProperty("ddlid")) {
            log.info "DDLID matches query value: warning suppressed"
            showWarning = false;
        }
        else
            log.info "Provided DDLID " + params.getProperty("ddlid") + " does not match stored ID ${cfg.DDLIdentity}"
        
        if(ddldbFile.lastModified() > System.currentTimeMillis()-MAX_ID_QUERY_TIME_MS) {
            log.info "DDL is new (installed: " + (new Date(ddldbFile.lastModified())) + "): warning suppressed"
            showWarning = false;
        }
        
        if(showWarning)
            UI.get().verifyIdRequest(); // throws exception if user cancels
        
        if(params.getProperty("cxphost"))
            ContextManager.get().setCurrentContextState(params.toContextState())
            
        [ isDone: { true }, get: { new JSONObject().put("id",cfg.DDLIdentity)} ] as Future<JSONObject>
    }
}

