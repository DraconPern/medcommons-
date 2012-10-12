/*
 * Created on Sep 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.medcommons.router.services.transfer.MCOrder;
import net.medcommons.router.services.transfer.RoutingQueue;
import net.medcommons.router.services.transfer.RoutingQueueUnavailableException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.sun.mail.iap.ConnectionException;

/**
 * Defines the purple box client interface to the pink box database via 
 * web services.
 * 
 * Basic strategy: (a) For inserts into the system just have a single insertXXX
 * method. Maybe have a generic insert with different implementations for
 * different classes. (b) have constructors that can take a JDOM node as input.
 * (c) Create a structure that is parallel to the XML for response/rows/&etc.
 * 
 * 
 * Need to do: 
 * Make some constant strings for the web services, document them here.
 * Make this more stream-centric for parsing XML instead of creating a string,
 * then parsing.s
 * It would be great if we could use standard serialization/marshalling here 
 * instead of our custom XML.
 * 
 * @author sean
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RemoteController {

  // HACK. username/password needs to come from a configuration file.
  private String username = "fatcat";

  private String password = "123";

  private String rootURL = null;

  private static Logger log = Logger.getLogger(RemoteController.class);

  private final static String ROWS = "rows";

  private final static String COLS = "cols";

  private final static String ROW = "row";

  private final static String TABLE = "table";

  private final static String NAME = "name";

  private final static String SUMMARY_STATUS = "summary_status";

  private final static String ELAPSED = "elapsed";

  private final static String RESPONSE = "RESPONSE";

  private final static String OR_ID = "OR_ID";

  private final static String OR_ORDERGUID = "OR_ORDERGUID";

  private final static String OR_TRACKING = "OR_TRACKING";

  private final static String OR_VR_VAETITLEORIGIN = "OR_VR_VAETITLEORIGIN";

  private final static String OR_VR_VAETITLEDEST = "OR_VR_VAETITLEDEST";

  private final static String OR_TIME = "OR_TIME";

  private final static String OR_GLOBALSTATUS = "OR_GLOBALSTATUS";

  private final static String OR_DESCRIPTION = "OR_DESCRIPTION";

  private final static String OR_PATIENTNAME = "OR_PATIENTNAME";

  private final static String OR_PATIENTID = "OR_PATIENTID";
  
  private final static String OR_MODALITY   = "OR_MODALITY";
  
  private final static String OR_SERIES     = "OR_SERIES";
  
  private final static String OR_IMAGES     = "OR_IMAGES";
  
  private final static String OR_AGE        = "OR_AGE";
  
  private final static String OR_DOB        = "OR_DOB";
  
  private final static String OR_SEX        = "OR_SEX";
  


  private final static String TYPE_ROUTING_QUEUE = "routing_queue";

  private final static String TYPE_ORDER = "orders";
  
  private  static boolean automaticTransfersActive = false;
  private static boolean transfersActive = true;
  private  static boolean insert_update_orders = false;
  private static RemoteController remoteController = null;
  // <OR_TIME>20041110141752</OR_TIME> 
  // 2004 11 10 14 17 52

  private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

  /**
   *  
   * if transfer is false, then don't notify pink box of transfers (no inserts
   * into routing table).
   * If orders is false then don't insert, update orders on pink box.
   * HACK: need a more coherent configuration system.
   */
  public RemoteController(String rootURL, boolean transfer, boolean orders) {
    super();
    if (rootURL.equals(""))
      rootURL = null;
    this.rootURL = rootURL;
    automaticTransfersActive = transfer;
    insert_update_orders = orders;
    remoteController = this;
    // TODO Auto-generated constructor stub
  }
  
  
  public static RemoteController getRemoteController(){
    return(remoteController);
  }

  /**
   * Inserts order (typically automatically generated) into
   * pink box.
   * 
   * Sample URL:
   * http://virtual01.medcommons.net/pink0/xmlws/wsinsertorder.php?orderGuid=0EFAB9D7F965252C070C0983F6E8B361491E3B67a&vAETitleOrigin=MGHA&vAETitleDest=MGHB&description=TEST_DESCRIPTION&patientName=Doe%2C+John&patientId=1234
   * http://virtual01.medcommons.net/pink0/xmlws/wsinsertorder.php?
   * orderGuid=lkjlkj&vAETitleOrigin=ljlkjlkjl&vAETitleDest=adfadfad&
   * description=Testing&patientName=Sean+Doyle&patientId=12323
   * 
   * @param order
   */
  public void insertOrder(MCOrder order) throws HttpException, IOException {
    if (rootURL == null)
      return;
    if (!insert_update_orders)
      return;
    StringBuffer buff = new StringBuffer(rootURL);
    buff.append("xmlws/");
    buff.append("wsinsertorder.php?");
    NameValuePair pairs[] = new NameValuePair[8];
    pairs[0] = new NameValuePair("orderGuid", order.getOrderGuid());
    pairs[1] = new NameValuePair("vAETitleOrigin", order.getOriginatorGuid());
    pairs[2] = new NameValuePair("vAETitleDest", order.getRecipientGuid());
    pairs[3] = new NameValuePair("description", order.getDescription());
    pairs[4] = new NameValuePair("patientName", order.getPatientName());
    pairs[5] = new NameValuePair("patientId", order.getPatientId());
    pairs[6] = new NameValuePair("tracking", order.getTrackingNumber());
    pairs[7] = new NameValuePair("modality", order.getModality());
    buff.append(EncodingUtil.formUrlEncode(pairs,
        HttpConstants.HTTP_ELEMENT_CHARSET));
    String url = buff.toString();
    log.info("posting new order on pink box:" + url);
    httpResponse response = postURL(url);
    
    //log.info("Testing to see if order is on pink box");
    // Perhaps should remove this code - simply tests that the 
    // order is retrievable from the server.
    try {
      getOrder(order.getOrderGuid());
      //log.info("No error returned");
    } catch (Exception e) {
      log.error("insertOrder - failure trying to retrieve newly-inserted order " + e.toString());
      e.printStackTrace();
    }
  }
  /**
   * Updates order on pink box.
   * 
   * Does this just replace the specified fields?
   * Example:<BR>
   * <pre>
   * http://localhost/pink7/xmlws/wsupdateorderstatus.php?gateway=MGH01&OrderGuid=8989&OrderStatus=DONE&series=1&images=666
   * </pre>
   * @param order
   * @throws HttpException
   * @throws IOException
   */
  //
  public void updateOrder(MCOrder order, String status) throws HttpException, IOException {
    if (rootURL == null)
      return;
    if (!insert_update_orders)
      return;
    StringBuffer buff = new StringBuffer(rootURL);
    buff.append("xmlws/");
    buff.append("wsupdateorderstatus.php?");
    NameValuePair pairs[] = new NameValuePair[4];
    pairs[0] = new NameValuePair("OrderGuid", order.getOrderGuid());
    pairs[1] = new NameValuePair("OrderStatus", status);// HACK
    pairs[2] = new NameValuePair("images", order.getNimages()+"");
    pairs[3] = new NameValuePair("series", order.getNseries() + "");

    buff.append(EncodingUtil.formUrlEncode(pairs,
        HttpConstants.HTTP_ELEMENT_CHARSET));
    String url = buff.toString();
    log.info("posting order update on pink box:\n" + url);
    httpResponse response = postURL(url);
    // Should test response here.
    /*
    log.info("Testing to see if order is on pink box");
    try {
      getOrder(order.getOrderGuid());
      log.info("No error returned");
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    */
  }

  
  
  /* Inserts routing queue entry in pink box.
   * 
   * http://virtual01.medcommons.net/pink2/xmlws/wsinsertroutingqueueinfo.php?
   * orderGuid=ABC8989898& dataGuid=OUOUOIUOIU&vAETitleOrigin=GHI7878&
   * vAETitleDest=123& protocol=http& globalStatus=NEW& itemType=TRANSFER
   */
  public void insertRoutingQueue(RoutingQueue routingQueue)
      throws HttpException, IOException {
    if (rootURL == null)
      return;
    if (!automaticTransfersActive) return;
    StringBuffer buff = new StringBuffer(rootURL);

    buff.append("xmlws/");
    buff.append("wsinsertroutingqueueinfo.php?");
    NameValuePair pairs[] = new NameValuePair[8];

    pairs[0] = new NameValuePair("orderGuid", routingQueue.getOrderGuid());
    pairs[1] = new NameValuePair("globalStatus", routingQueue.getGlobalStatus());
    pairs[2] = new NameValuePair("vAETitleDest", routingQueue
        .getDestinationGatewayGuid());
    pairs[3] = new NameValuePair("vAETitleOrigin", routingQueue
        .getOriginatorGatewayGuid());
    pairs[4] = new NameValuePair("itemType", routingQueue.getItemType());
    pairs[5] = new NameValuePair("protocol", routingQueue.getProtocol());
    pairs[6] = new NameValuePair("dataGuid", routingQueue.getMcGuid());
    pairs[7] = new NameValuePair("requestID", routingQueue
        .getOriginatorGatewayGuid()
        + System.currentTimeMillis());
    // Need to add commandType
    buff.append(EncodingUtil.formUrlEncode(pairs,
        HttpConstants.HTTP_ELEMENT_CHARSET));
    String url = buff.toString();
    log.info("posting routing queue on pink box:\n" + url);
    httpResponse response = postURL(url);
    // TO DO: Test here to see that it worked.
    
    //log.info("routing queue response=\n" + response.contents);
    //log.info("Testing to see if queue entry is on pink box");
    //log.info(getRoutingQueue(routingQueue.getDestinationGatewayGuid()));
  }

  /**
   * Updates routing queue entry on pink box.
   * @param routingQueue
   * @throws HttpException
   * @throws IOException
   */
  public void updateRoutingQueue(RoutingQueue routingQueue)
      throws HttpException, IOException {
    if (rootURL == null)
      return;
    if (!transfersActive) return;
    StringBuffer buff = new StringBuffer(rootURL);
    buff.append("xmlws/");
    buff.append("wsupdateroutingqueueinfo.php?");
    NameValuePair pairs[] = new NameValuePair[12];

    pairs[0] = new NameValuePair("orderGuid", routingQueue.getOrderGuid());
    pairs[1] = new NameValuePair("globalStatus", routingQueue.getGlobalStatus());
    pairs[2] = new NameValuePair("vAETitleDest", routingQueue
        .getDestinationGatewayGuid());
    pairs[3] = new NameValuePair("vAETitleOrigin", routingQueue
        .getOriginatorGatewayGuid());
    pairs[4] = new NameValuePair("itemType", routingQueue.getItemType());
    pairs[5] = new NameValuePair("protocol", routingQueue.getProtocol());
    pairs[6] = new NameValuePair("dataGuid", routingQueue.getMcGuid());
    pairs[7] = new NameValuePair("requestID", routingQueue.getId());
    pairs[8] = new NameValuePair("bytesTotal", Long.toString(routingQueue.getBytesTotal()));
    pairs[9] = new NameValuePair("bytesTransferred", Long.toString(routingQueue.getBytesTransferred()));
    if (routingQueue.getTimeStarted() != null)
      pairs[10] = new NameValuePair("timeStarted", dateFormat.format(routingQueue.getTimeStarted()));
    else
      pairs[10] = new NameValuePair("timeStarted", "");
    if (routingQueue.getTimeCompleted() != null)
      pairs[11] = new NameValuePair("timeCompleted",dateFormat.format(routingQueue.getTimeCompleted()));
    else
      pairs[11]= new NameValuePair("timeCompleted", "");

    
    // Need to add commandType
    buff.append(EncodingUtil.formUrlEncode(pairs,
        HttpConstants.HTTP_ELEMENT_CHARSET));
    String url = buff.toString();
    log.info("Routing Queue Update:" + url);
    httpResponse response = postURL(url);
    // Need to read back by ID, not by just destination
    //log.info("routing queue response=\n" + response.contents);
    //log.info("Testing to see if updated routing queue item is on box");
    //log.info(getRoutingQueue(routingQueue.getDestinationGatewayGuid()));
  }

  /**
   * Note: ClassCastException thrown if response is not correct.
   * 
   * @param arg
   * @return
   * @throws RoutingQueueUnavailableException
   */
  public RoutingQueue getRoutingQueue(String arg) throws RoutingQueueUnavailableException {
    RoutingQueue rq = null;
    httpResponse response = null;
    wsResponse resp = null;
    if (!transfersActive) return null;
    try {
      if (rootURL == null)
        return (null);
      NameValuePair pairs[] = new NameValuePair[2];
      //http://virtual01.medcommons.net/pink2/xmlws/wsgetroutingqueueitems.php?deatvAETitleDest=MGHB
      pairs[0] = new NameValuePair("destvAETitle", "*");
      pairs[1] = new NameValuePair("actor", arg);
      StringBuffer buff = new StringBuffer(rootURL);
      buff.append("xmlws/");
      buff.append("wsgetroutingqueueitems.php?");
      buff.append(EncodingUtil.formUrlEncode(pairs,
          HttpConstants.HTTP_ELEMENT_CHARSET));
      String url = buff.toString();
      //log.debug("getRoutingQueue URL:" + url);
      response = getURL(url);
      //log.info("RoutingQueue:\n" + response.contents);
      resp = makeWSResponse(response);
    } 
    catch(ConnectionException e){
      throw new RoutingQueueUnavailableException("Cannot reach routing queue at " + rootURL,e);
    }
    catch (Exception e) {
      log.error("getRoutingQueue:" + arg + ": " +e.toString());
      throw new RoutingQueueUnavailableException("Error contacting " + rootURL + ", getRoutingQueue:" + arg + ": " +e.toString(),e);
    }

    // Get the first element with NEW as the state
    if(response != null)  {
      //log.info("rows = " + resp.rows);
      //log.info("cols = " + resp.columns);

      if(resp != null) {      
        if (resp.table.elements.size() > 0) {
          for (int i = 0; i < resp.table.elements.size(); i++) {
            RoutingQueue candidate = (RoutingQueue) resp.table.elements.get(i);
            if (candidate.getGlobalStatus().equals(RoutingQueue.STATUS_NEW)) {
              rq = candidate;
              break;
            }
          }
        } 
        else {
          ;//log.debug("No items in queue");
        }
      }
      else {
        log.warn("Unable to parse response from " + rootURL);
      }
    }
    return (rq);
  }
  
  /**
   * Note: ClassCastException thrown if response is not correct.
   * 
   * @param arg
   * @return
   */
  public RoutingQueue getRoutingQueueWithID(String id) {
    RoutingQueue rq = null;
    httpResponse response = null;
    wsResponse resp = null;
    if (!transfersActive) return null;
    try {
      if (rootURL == null)
        return (null);
      NameValuePair pairs[] = new NameValuePair[1];
      //http://virtual01.medcommons.net/pink2/xmlws/wsgetroutingqueueitems.php?deatvAETitleDest=MGHB
      pairs[0] = new NameValuePair("requestID", id);
      StringBuffer buff = new StringBuffer(rootURL);
      buff.append("xmlws/");
      buff.append("wsgetroutingqueueitem.php?");
      buff.append(EncodingUtil.formUrlEncode(pairs,
          HttpConstants.HTTP_ELEMENT_CHARSET));
      String url = buff.toString();
      //log.debug("getRoutingQueue URL:" + url);
      response = getURL(url);
      //log.info("RoutingQueue:\n" + response.contents);
      resp = makeWSResponse(response);
    } 
    catch(ConnectionException e){
      log.error("getRoutingQueueWithID:" + id + ": Can't reach pink box:" + rootURL);
    }
    catch (Exception e) {
      log.error("getRoutingQueueWithID:" + id + ":" + e.toString());
      e.printStackTrace();
    }

    // Get the first element with NEW as the state
    if (response != null) {
      //log.info("rows = " + resp.rows);
      //log.info("cols = " + resp.columns);
      if (resp.table.elements.size()>1)
        throw new RuntimeException("More than one routing queue item matches requestid "
            + id);
      rq = (RoutingQueue) resp.table.elements.get(0);
      } 
      else {
        ;//log.debug("No items in queue");
      }

    
    return (rq);

  }

  /**
   * Returns an MCOrder object from the pink box.
   * http://virtual01.medcommons.net/pink2/xmlws/wsgetorderinfo.php?OrderGuid=C99B33FF27085624022BDB94DF372EFD
   * 
   * @param orderGuid
   * @return
   * @throws IOException
   * @throws JDOMException
   */
  public MCOrder getOrder(String orderGuid) throws ConnectionException, IOException, JDOMException {
    MCOrder order = new MCOrder();
    if (rootURL == null)
      return (null);

    StringBuffer buff = new StringBuffer(rootURL);
    buff.append("xmlws/");
    buff.append("wsgetorderinfo.php?");
    NameValuePair pairs[] = new NameValuePair[1];
    pairs[0] = new NameValuePair("OrderGuid", orderGuid);
    buff.append(EncodingUtil.formUrlEncode(pairs,
        HttpConstants.HTTP_ELEMENT_CHARSET));
    String url = buff.toString();
    wsResponse resp = null;
    //log.info("getOrder URL = " + url);
    httpResponse response = getURL(url);
    if (response.status == HttpStatus.SC_OK) {
      //log.info("getOrder response is " + response.contents);
      resp = makeWSResponse(response);
    }
    if (resp != null) {
      // Bug. Why resp.rows is zero
      //log.info("rows = " + resp.rows);
      //log.info("cols = " + resp.columns);
      if (resp.table.elements.size() > 0) {
        order = (MCOrder) resp.table.elements.get(0);
        //log.info("first entry = " + order);

      }

    }

    return (order);
  }

  private wsResponse makeWSResponse(httpResponse response) throws IOException,
      JDOMException {
    wsResponse resp = null;
    SAXBuilder builder = new SAXBuilder();
    try {

      if (response.status == HttpStatus.SC_OK) {
        //log.info("getOrder response is " + response.contents);
        InputStream is = new ByteArrayInputStream(response.contents.trim()
            .getBytes());
        // Yuk.
        Document document = builder.build(is);
        Element root = document.getRootElement();

        List properties = root.getChildren();

        Iterator i = properties.iterator();
        //log.info("getOrder There are " + properties.size() + " elements");

        while (i.hasNext()) {
          Element attribute = (Element) i.next();
          String name = attribute.getName();
          String value = attribute.getTextTrim();
          if (name.equals("response")) {
            resp = makeResponse(attribute);

          } else
            ;//log.info(" Unknown node: (name: " + name + ")(value: " + value   + ")");
          //}
        }
      }
    } finally {
      if (resp == null)
        log.info("Can't parse contents:" + response.contents);
    }
    return (resp);
  }

  /**
   * Returns the contents of a document in a String.
   * 
   * In general this is a poor design- it would be better to return an
   * InputStream. However - we need to release the connection and this can't be
   * done without closing the InputStream.
   * 
   * @param url
   * @return
   */
  public httpResponse getURL(String url) throws ConnectionException, HttpException, IOException {
    httpResponse response = new httpResponse();
    GetMethod get = null;
    try {
      HttpClient client = new HttpClient();
      client.getState().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(username,
          password);
      // much too broad use of credentials.
      client.getState().setCredentials(null, null, defaultcreds);

      get = new GetMethod(url);
      get.setDoAuthentication(true);

      response.status = client.executeMethod(get);
     

      if (response.status != HttpStatus.SC_OK) {
        log.error("Unexpected failure with URL: " + url + "\n status is "
            + HttpStatus.getStatusText(response.status) + "\n "
            + response.contents);
        response.contents = null;

      }
      else{
        // Ugly hack.
        // The previous ugly hack of response.contents = get.getResponseBodyAsString();
        // was also ugly - but it was only a single line.
        // The problem with getResponseBodyAsString() is that it generated 
        // warning messages if the responses were too large (1K bytes?).
        // A much better setup would be to parse the XML in an inputstream instead
        // of what we're doing here. However - the PHP code's XML has a leading whitespace
        // and that throws off the XML parser; other XML errors aren't reported very well
        // from the stream so having the whole thing in a buffer makes error messages
        // more coherent. It is a latency/memory hit to read in the whole thing
        // first, then parse it.
        
        // Read the stream and put it into a StringBuffer; at the end 
        // put the results into a String.
        StringBuffer buff = new StringBuffer();
        byte[] scratch = new byte[1024];
        InputStream in = get.getResponseBodyAsStream();
        int nBytes = in.read(scratch);
        while(nBytes != -1){
          buff.append(new String(scratch,0, nBytes));
          nBytes = in.read(scratch);
          
        }
        response.contents = buff.toString();
      }

    } finally {
      get.releaseConnection();
    }
    return (response);

  }

  /**
   * General utility for posting URLs to pink boxes. Currently uses HTTP GET;
   * will use HTTP POST eventually.
   * 
   * @param url
   * @return
   * @throws HttpException
   * @throws IOException
   */
  private httpResponse postURL(String url) throws HttpException, IOException {
    GetMethod get = null;
    httpResponse response = new httpResponse();
    try {
      HttpClient client = new HttpClient();
      client.getState().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(username,
          password);
      // much too broad use of credentials.
      client.getState().setCredentials(null, null, defaultcreds);

      get = new GetMethod(url);
      get.setDoAuthentication(true);

      response.status = client.executeMethod(get);
      response.contents = get.getResponseBodyAsString();
      if (response.status != HttpStatus.SC_OK) {
        log.error("Unexpected failure with URL: " + url + "\n status is "
            + HttpStatus.getStatusText(response.status) + "\n "
            + response.contents);
      }
    } finally {
      get.releaseConnection();
    }
    return (response);

  }

  wsResponse makeResponse(Element element) {
    wsResponse response = new wsResponse();
    List properties = element.getChildren();

    Iterator i = properties.iterator();
    while (i.hasNext()) {
      Element attribute = (Element) i.next();
      String name = attribute.getName();
      String value = attribute.getTextTrim();
      if (name.equals(ROWS)) {
        response.rows = Integer.parseInt(value);
        //log.info("setting response.rows =  " + response.rows);
      } else if (name.equals(COLS))
        response.columns = Integer.parseInt(value);
      else if (name.equals(TABLE)) {
        response.table = makeTable(attribute, response.rows);

        //log.info("response is " + response.table);
      } else
        ;//log.info("makeResponse: ignore:" + name + ", " + value);
    }
    return (response);
  }

  private wsTable makeTable(Element element, int nElements) {
    wsTable table = new wsTable();
    List properties = element.getChildren();
    table.elements = new ArrayList(nElements);
    //log.info("makeTable: nElements=" + nElements);
    //String temp = element.getAttributeValue(NAME);
    //log.info("temp is " + temp);

    // Depending on the name - dispatch to different
    // constructors for the types.
    Iterator i = properties.iterator();
    String tableName = null;
    while (i.hasNext()) {
      Element attribute = (Element) i.next();
      String name = attribute.getName();
      if (name.equals(ROW)) {
        if (tableName.equals(TYPE_ORDER))
          table.elements.add(makeOrder(attribute));
        else if (tableName.equals(TYPE_ROUTING_QUEUE))
          table.elements.add(makeRoutingQueue(attribute));
        else
          log.error("Unknown table type:" + tableName);
      } else if (name.equals(NAME)) {
        tableName = attribute.getTextTrim();
        //log.info("make table: NAME is " + tableName);
      } else
        ;//log.info("make table: ignoring " + name);
    }
    return (table);
  }

  /**
   * @param element
   * @return
   */

  MCOrder makeOrder(Element element) {
    MCOrder order = new MCOrder();
    List properties = element.getChildren();

    Iterator i = properties.iterator();
    //log.info("make order: There are " + properties.size() + " elements");
    while (i.hasNext()) {
      Element attribute = (Element) i.next();
      String name = attribute.getName();
      String value = attribute.getTextTrim();
      //log.info("make order: name=" + name);
      if (name.equals(OR_ID))
        order.setId(Long.getLong(value.trim()));
      else if (name.equals(OR_ORDERGUID))
        order.setOrderGuid(value);
      else if (name.equals(OR_TRACKING))
        order.setTrackingNumber(value);
      else if (name.equals(OR_PATIENTNAME))
        order.setPatientName(value);
      else if (name.equals(OR_PATIENTID))
        order.setPatientId(value);
      else if (name.equals(OR_TIME)){
        try{
          Date date = dateFormat.parse(value);
          order.setTimeCreated(date);
        }
        catch(Exception e){
          e.printStackTrace();
          order.setTimeCreated(null);
        }
      }
      else if (name.equals(OR_GLOBALSTATUS))
        log.info("Ignoring global status: " + value + " (harmless)");
      else if (name.equals(OR_DESCRIPTION))
        order.setDescription(value);
      else if (name.equals(OR_MODALITY))
        order.setModality(value);
      else if (name.equals(OR_SERIES))
        order.setNseries(Integer.parseInt(value.trim()));
      else if (name.equals(OR_IMAGES))
        order.setNimages(Integer.parseInt(value.trim()));
      else if (name.equals(OR_AGE))
        order.setPatientAge(value);
      else if (name.equals(OR_SEX))
        order.setPatientSex(value);
      else if (name.equals(OR_DOB))
        order.setPatientDob(value);

        ;//log.info("Ignoring time in order object:" + value);
    }
    return (order);

  }

  final static String RQ_OR_ORDERGUID = "RQ_OR_ORDERGUID";

  final static String RQ_VR_DESTINATIONVAETITLE = "RQ_VR_DESTINATIONVAETITLE";

  final static String RQ_VR_ORIGINVAETITLE = "RQ_VR_ORIGINVAETITLE";

  final static String RQ_PROTOCOL = "RQ_PROTOCOL";

  final static String RQ_BYTESTOTAL = "RQ_BYTESTOTAL";

  final static String RQ_OS_DATAGUID = "RQ_OS_DATAGUID";

  final static String RQ_BYTESTRANSFERRED = "RQ_BYTESTRANSFERRED";

  final static String RQ_GLOBALSTATUS = "RQ_GLOBALSTATUS";

  final static String RQ_RESTARTCOUNT = "RQ_RESTARTCOUNT";

  final static String RQ_TIMEENTERED = "RQ_TIMEENTERED";

  final static String RQ_TIMESTARTED = "RQ_TIMESTARTED";

  final static String RQ_TIMECOMPLETED = "RQ_TIMECOMPLETED";

  final static String RQ_ITEMTYPE = "RQ_ITEMTYPE";

  final static String RQ_ID = "RQ_ID";

  final static String RQ_COMMANDTYPE = "RQ_COMMAND_TYPE";

  /**
   * 
   * @param element
   * @return
   */
  RoutingQueue makeRoutingQueue(Element element) {
    RoutingQueue rq = new RoutingQueue();
    List properties = element.getChildren();

    Iterator i = properties.iterator();
    //log.info("make routing queue: There are " + properties.size()
    //        + " elements");
    while (i.hasNext()) {
      Element attribute = (Element) i.next();
      String name = attribute.getName();
      String value = attribute.getTextTrim();
      //log.info("makeRoutingQueue: name=" + name + ", value=" + value);
      if (name.equals(RQ_OR_ORDERGUID))
        rq.setOrderGuid(value);
      else if (name.equals(RQ_VR_DESTINATIONVAETITLE))
        rq.setDestinationGatewayGuid(value);
      else if (name.equals(RQ_VR_ORIGINVAETITLE))
        rq.setOriginatorGatewayGuid(value);
      else if (name.equals(RQ_PROTOCOL))
        rq.setProtocol(value);
      else if (name.equals(RQ_BYTESTOTAL))
        rq.setBytesTotal(Long.parseLong(value));
      else if (name.equals(RQ_OS_DATAGUID))
        rq.setMcGuid(value);
      else if (name.equals(RQ_BYTESTRANSFERRED))
        rq.setBytesTransferred(Long.parseLong(value));
      else if (name.equals(RQ_GLOBALSTATUS))
        rq.setGlobalStatus(value);
      else if (name.equals(RQ_RESTARTCOUNT))
        rq.setRestartCount(Integer.parseInt(value));
      else if (name.equals(RQ_TIMEENTERED))
        ;//log.info("Ignoring timeentered"); //rq.setTimeEntered(value);
      else if (name.equals(RQ_TIMESTARTED))
        ;//log.info("Ignoring timestarted");
      //rq.setPatientName(value);
      else if (name.equals(RQ_TIMECOMPLETED))
        ;//log.info("Ignoring timecompleted");
      //rq.setPatientName(value);
      else if (name.equals(RQ_ITEMTYPE))
        rq.setItemType(value);
      else if (name.equals(RQ_COMMANDTYPE))
        rq.setCommandType(value);
      else if (name.equals(RQ_ID)) {
        rq.setId(value);
      } else
        ;//log.info("Ignoring routing_queue attribute " + name);
    }

    return (rq);

  }

  class wsTable {
    List elements;
  }

  final static int RESPONSETYPE_ORDER = 1;

  final static int RESPONSETYPE_ORDERSERIES = 2;

  class wsResponse {
    int responseType;

    int rows;

    int columns;

    wsTable table;

  }

  class httpResponse {
    // Default status is failure.
    int status = HttpStatus.SC_METHOD_FAILURE;

    String contents;

  }

}