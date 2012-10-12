/*
 * $Id: Poller.java 58 2004-04-22 22:31:27Z mquigley $
 */

package net.medcommons.router.control.poller;

import java.util.Date;

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.XMLType;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.command.RemoteCommand;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.log4j.Logger;
import org.jboss.varia.scheduler.Schedulable;

/**
 * The Poller is a class executed by the JBoss Scheduler service, which retrieves
 * any pending commands from MedCommons central and posts them to an internal
 * JMS queue for dispatching.
 * @author <a href="mailto:michael@quigley.com">Michael Quigley</a>
 */
public class Poller implements Schedulable {
  
  private Logger log = Logger.getLogger(Poller.class);

  /**
   * The perform method is the entrance point called by the JBoss Scheduler 
   * class at specified intervals.
   */
  public void perform(Date now, long remainingRepetitions) {
    try {
      log.info("Checking for pending commands.");

      RemoteCommand[] ret = getRemoteCommands();

      log.info("Found " + ret.length + " pending commands.");

      for(int i = 0; i < ret.length; i++) {
        postCommand(ret[i]);
      }
      
    } catch(Exception e) {
      e.printStackTrace();
      log.error("Error retrieving pending commands: " + e.toString()); 
    }
  }

  private RemoteCommand[] getRemoteCommands() throws Exception {
    String endpoint = (String) Configuration.getInstance().getConfiguredValue("CommandServiceUrl");
    String myGuid = (String) Configuration.getInstance().getConfiguredValue("DeviceGuid");
              
    Service service = new Service();
    Call call = (Call) service.createCall();
    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("getPendingCommands");
    call.addParameter("deviceGuid", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
    call.setReturnType(XMLType.SOAP_ARRAY); 
    
    QName qn = new QName("", "RemoteCommand");
    call.registerTypeMapping(RemoteCommand.class, qn,
                             new BeanSerializerFactory(RemoteCommand.class, qn),
                             new BeanDeserializerFactory(RemoteCommand.class, qn));                                         
    
    RemoteCommand[] ret = (RemoteCommand[]) call.invoke(new Object[] { myGuid });
    
    return ret;
  }

  private void postCommand(RemoteCommand cmd) throws Exception {
    InitialContext iniCtx = new InitialContext();
    Object tmp = iniCtx.lookup("java:/ConnectionFactory");
    QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    
    QueueConnection conn = qcf.createQueueConnection();
    Queue queue = (Queue) iniCtx.lookup("queue/RemoteCommandQueue");
    
    QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    conn.start();     
    
    QueueSender send = session.createSender(queue);
    ObjectMessage msg = session.createObjectMessage();
    msg.setObject(cmd);
    send.send(msg);    
    
    conn.stop();
    session.close();
    conn.close();
  }

}
