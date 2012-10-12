/**
 * $Id$
 */
package net.medcommons.application.dicomclient.utils

import org.json.JSONObject
import net.medcommons.application.utils.JSONSimpleGETimport net.medcommons.application.dicomclient.ContextManagerimport org.apache.log4j.Loggerimport net.sourceforge.pbeans.Storeimport net.medcommons.application.dicomclient.DownloadHandlerimport net.medcommons.application.dicomclient.transactions.ContextStateimport net.medcommons.application.dicomclient.transactions.CxpDownloadimport net.medcommons.application.dicomclient.transactions.DownloadQueueimport java.lang.IllegalStateException
import java.util.concurrent.Futureimport net.medcommons.application.dicomclient.Configurationsimport groovy.xml.MarkupBuilder
import static net.medcommons.application.dicomclient.utils.ManagedTransaction.*


/**
 * Simple test command that places "hello world" in the out queue
 * 
 * @author ssadedin
 */
public class EchoCommand implements Command {
     
    private static Logger log = Logger.getLogger(EchoCommand.class);

    public Future<JSONObject> execute(CommandBlock params) {
        
        [ isDone: { true }, get: { new JSONObject().put("message", "hello world")} ] as Future<JSONObject>
    }
}

