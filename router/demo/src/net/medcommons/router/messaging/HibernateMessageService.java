package net.medcommons.router.messaging;

import java.util.*;

import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.HibernateUtil;
import net.medcommons.rest.RESTUtil;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.ExecuteUpdateResultCheckStyle;
import org.json.JSONObject;

/**
 * Implementation of message service that stores messages in hibernate.
 * Also stores in memory cache so that where message is sent / received
 * on same node there is no database poll latency.
 * 
 * @author ssadedin
 */
public class HibernateMessageService implements MessageService, Runnable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(HibernateMessageService.class);
    
    /**
     * Hash of messages waiting keyed on connection id
     */
    private static Map<String, List<JSONObject>> msgs = new HashMap<String, List<JSONObject>>();
   
   /**
    * Local identity of this node - we use this to ensure we don't read our own messages 
    * by mistake.
    */
    private final String sourceId = RESTUtil.getKey();
    
    /**
     * Whether to ONLY accept messages from MySQL and not use direct in-memory
     * message routing when the messages are sent to the local machine
     */
    private boolean remoteOnly = false;
    
    /**
     * The last processed message id 
     */
    private Long lastMessageId = 0L;
    
    /**
     * If set to false then commands will not be persisted
     * to the database
     */
    private boolean enableDB=true;
    
    public HibernateMessageService() {
        log.info("Starting HibernateMessageService");
        log.info("Memory messages enabled = " + (!remoteOnly));
        log.info("DB messages enabled = " + enableDB);
        Thread t = new Thread(this, "MessageQueue");
        t.setDaemon(true);
        t.start(); // naughty, but this really doesn't matter and s a pain to set up other ways
    }

    @Override
    public List<JSONObject> read(String connectionId, long timeoutMs) throws ServiceException {
        List<JSONObject> results = null; 
        synchronized(msgs) {
            List<JSONObject> cmds = msgs.get(connectionId);
            if(cmds == null || cmds.isEmpty()) {
                try { msgs.wait(timeoutMs); } catch (InterruptedException e) { }
                cmds = msgs.get(connectionId);
            }
            if(cmds != null && !cmds.isEmpty()) {
                results = new ArrayList<JSONObject>(cmds.size());
                results.addAll(cmds);
                cmds.clear();
            }
        }
        if(results != null) {
            log.info("Returning " + results.size() + " results for connection " + connectionId);
            for(JSONObject o : results) {
                log.info("Result: " + o.toString());
            }
        }
        return results;
    }
    
    public void run() {
        
        if(!enableDB)
            return;

        long count = 0;
        while(true) {
            try {
                Session s = HibernateUtil.currentSession();
                s.beginTransaction();
                try {
                    Date oldest = new Date(System.currentTimeMillis() - 3600 * 1000);
                    List<MessageQueue> ms = 
                        s.createQuery("from MessageQueue where queuetime > :oldest and id > :last order by id, queuetime  desc")
                        .setDate("oldest", oldest)
                        .setLong("last", lastMessageId)
                        .list();

                    if(!ms.isEmpty())
                        log.info("Received " + ms.size() + " messages in queue");

                    for(MessageQueue m : ms) {
                        log.debug("Got message: " + m);
                        // s.delete(m);
                    }
                    
                    if(++count % 10 == 0) {
                        s.createQuery("delete MessageQueue where m.queuetime < :oldest")
                         .setDate("oldest", oldest)
                         .executeUpdate();
                    }
                    
                    s.getTransaction().commit();

                    synchronized(msgs) {
                        for(MessageQueue m : ms) {
                            JSONObject obj;
                            
                            // Stop us from re-reading the same message again
                            if(lastMessageId < m.getId())
                                lastMessageId = m.getId();
                            
                            try {
                                obj = new JSONObject(m.getMessage());
                                if(remoteOnly || !m.getSource().equals(this.sourceId)) {
                                    put(m.getConnectionId(), obj);
                                    log.info("Got remote message: " + m.toString());
                                }
                            }
                            catch (Exception e) {
                                log.error("Invalid JSON message found in message queue: " + m.getMessage(),e);
                            }
                        }
                    }
                    try { Thread.sleep(2000); } catch (InterruptedException e) { }
                }
                finally {
                    HibernateUtil.closeSession();
                }
                
                ++count;
            }
            catch(Throwable t) {
                log.error("Failure in command poll loop", t);
            }        }
    }
    
    /**
     * Put message into internal memory cache
     * 
     * @param connectionId
     * @param cmd
     */
    protected void put(String connectionId, JSONObject cmd) {
        synchronized(msgs) {
            List<JSONObject> cmds = msgs.get(connectionId);
            if(cmds==null) {
                msgs.put(connectionId, cmds=new ArrayList<JSONObject>());       
            }
            cmds.add(cmd);
            msgs.notifyAll();
        }
    }

    @Override
    public void send(String connectionId, JSONObject message) throws ServiceException {
        try {
            if(enableDB) {
                Session s = HibernateUtil.currentSession();
                s.beginTransaction();
                try {
                    MessageQueue m = new MessageQueue(connectionId, message.toString());
                    s.save(m);
                    s.getTransaction().commit();
                }
                finally {
                    HibernateUtil.closeSession();
                }
            }
        }
        finally {
            if(!remoteOnly)
                put(connectionId, message);
        }
    }

    public boolean isRemoteOnly() {
        return remoteOnly;
    }

    public void setRemoteOnly(boolean remoteOnly) {
        this.remoteOnly = remoteOnly;
    }

    public boolean isEnableDB() {
        return enableDB;
    }

    public void setEnableDB(boolean enableDB) {
        this.enableDB = enableDB;
    }
}
