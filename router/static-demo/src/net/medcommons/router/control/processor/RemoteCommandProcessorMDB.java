/* 
 * $Id: RemoteCommandProcessorMDB.java 93 2004-05-12 03:28:47Z mquigley $
 */

package net.medcommons.router.control.processor;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.XMLType;

import net.medcommons.router.configuration.Configuration;
import net.medcommons.command.RemoteCommand;
import net.medcommons.command.RemoteCommandResult;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.log4j.Logger;

public class RemoteCommandProcessorMDB implements MessageDrivenBean, MessageListener {

  private MessageDrivenContext ctx = null;
  private QueueConnection conn;
  private QueueSession session;
  
  private static Logger log = Logger.getLogger(RemoteCommandProcessorMDB.class);
  
  public void setMessageDrivenContext(MessageDrivenContext ctx) {
    this.ctx = ctx;
  }
  
  public void ejbCreate() {
    try {
      initJMS();
    } catch(Exception e) {
      throw new EJBException("Failed to init RemoteCommandProcessorMDB", e);  
    }
  }
  
  public void ejbRemove() {
    ctx = null;
    try {
      if(session != null) session.close();
      if(conn != null) conn.close();
    } catch(JMSException e) {
      // Do something.
    }
  }
  
  public void onMessage(Message msg) {
    try {
      RemoteCommand cmd = (RemoteCommand) ((ObjectMessage) msg).getObject();
      log.info("Received: " + cmd.toString());
      
      RemoteCommandHandler handler = RemoteCommandHandlerFactory.getHandler(cmd);
      RemoteCommandResult result = handler.handle();
      
      postResult(result);

    } catch(Throwable t) {
      t.printStackTrace();
    }
  }    
  
  private void initJMS() throws JMSException, NamingException {
    InitialContext iniCtx = new InitialContext();
    Object tmp = iniCtx.lookup("java:/ConnectionFactory");
    QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
    conn = qcf.createQueueConnection();
    session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
    conn.start();
  }
  
  private void postResult(RemoteCommandResult result) throws Exception {
    String endpoint = (String) Configuration.getInstance().getConfiguredValue("CommandServiceUrl");      
      
    Service service = new Service();
    Call call = (Call) service.createCall();
    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("postCommandResult");
    call.addParameter("deviceGuid", XMLType.SOAP_STRING, ParameterMode.IN);
    call.addParameter("result", new QName("", "RemoteCommandResult"), RemoteCommandResult.class, ParameterMode.IN);
    call.setReturnType(XMLType.SOAP_STRING);
    
    QName qn = new QName("", "RemoteCommandResult");
    call.registerTypeMapping(RemoteCommandResult.class, qn,
                             new BeanSerializerFactory(RemoteCommandResult.class, qn),
                             new BeanDeserializerFactory(RemoteCommandResult.class, qn));                                         
    
    String deviceGuid = (String) Configuration.getInstance().getConfiguredValue("DeviceGuid");
    
    call.invoke(new Object[] { deviceGuid, result });
    
    log.info("Posted command result: " + result.toString());  
  }
  
}