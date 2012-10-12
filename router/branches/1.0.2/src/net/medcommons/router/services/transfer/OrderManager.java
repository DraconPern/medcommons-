/*
 * Created on Aug 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import javax.naming.NamingException;

import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.router.services.dicom.util.DICOMUtils;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.wado.OrderNotFoundException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Manages the creation of orders and the spawning of routing
 * 
 * @author sean
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OrderManager {

  private MessageDigest sha = null;

  private File rootDirectory = null;

  final static Logger log = Logger.getLogger(OrderManager.class);

  private static OrderManager orderManager = null;

  /**
   * Hard coded recipient
   */
  private String recipientGuid = "MGHB";

  /**
   * Hard coded affiliate
   *  
   */

  private String currentAffiliate = "Hospital_R_US";

  private GuidGenerator guidGenerator = null;

  private static String nodeIdentity = null;

  /**
   *  
   */
  public OrderManager(File rootDirectory, String nodeID) {
    super();
    this.rootDirectory = rootDirectory;
    nodeIdentity = nodeID;
    try {
      guidGenerator = new GuidGenerator();
      sha = MessageDigest.getInstance("SHA-1");
      orderManager = this;
    } catch (Exception e) {
      log.error(e.toString());
      e.printStackTrace();
    }

  }

  public static OrderManager getOrderManager() {
    return (orderManager);
  }

  public static String getNodeIdentity() {
    return (nodeIdentity);
  }

  public String getCurrentAffiliate() {
    return (currentAffiliate);
  }

  /**
   * Placeholder mechanism for order GUID generation.
   * 
   * @param input
   * @return
   */
  public String generateOrderGuid(byte[] input) {
    String guid = null;
    try {

      guid = guidGenerator.generateGuid(input);
    } catch (IOException e) {
      log.error(e.toString());
      e.printStackTrace();
    }
    return (guid);
  }

  /**
   * Placeholder mechanism for generating tracking numbers.
   * 
   * Stupid algorithm. Generates the SHA1 hash of the current time; then encodes
   * the number as hex; then truncates to 12 characters.
   * 
   * @return
   */
  /*
   * private synchronized String generateTrackingNumber() { Date now = new
   * Date(); String sNow = now.toString(); sha.update(sNow.getBytes()); byte[]
   * hash = sha.digest(); String trackingNumberRoot =
   * DICOMDataManager.encodeBytes(hash, 0, hash.length); return
   * (trackingNumberRoot.substring(0, 11)); }
   */
  public String generateTrackingNumber() {
    Date now = new Date();
    String sNow = now.toString();
    sha.update(sNow.getBytes());
    byte[] hash = sha.digest();

    // Returns a 20 byte array (160 bits)

    String trackingNumberRoot = DICOMUtils.encodeBytes(hash, 0, hash.length);
    String trackingNumberTruncated = trackingNumberRoot.substring(0, 12);

    return (trackingNumberTruncated);
  }


  public MCOrderSeriesLink createOrderSeriesLink(MCOrder order, MCSeries series) {
    MCOrderSeriesLink mclink = new MCOrderSeriesLink();
    mclink.setMcGuid(series.getMcGUID());
    mclink.setOrderGuid(order.getOrderGuid());
    return (mclink);
  }
  

  /**
   * Kludge function. This mapping should be managed by Hibernate, not by nested
   * Hibernate calls. Returns the study given a study GUID.
   * 
   * @param guid
   * @return
   * @throws HibernateException
   * @throws NamingException
   */

  public static MCOrder getOrderViaGuid(String guid) throws HibernateException,
      NamingException {
	  MCOrder order = null;
	  /*
    Session session = null;
    MCOrder order = null;

    try {
      session = HibernateUtil.currentSession();

      Criteria orderCriteria = session.createCriteria(MCOrder.class);
      EqExpression eqOrderGuid = new EqExpression("orderGuid", guid, true);
      orderCriteria.add(eqOrderGuid);
      List orders = orderCriteria.list();
      if (!orders.isEmpty()) {
        order = (MCOrder) orders.get(0);
        // Should test here for multiple matches
      }
      log.warn("Got order for guid " + guid + "," + order);

      // ssadedin - eventually longer needed - hibernate will get them
      //getOrderSeries(order, session);

    } finally {

      HibernateUtil.closeSession();
    }
    return (order);
    */
	  if(true) throw new RuntimeException("Dead code");
	  return (order);
  }

  /**
   * Returns a List of MCSeries objects associated with an order GUID.
   * 
   * @param orderGuid
   * @return
   * @throws HibernateException
   * @throws NamingException
   */
  public static List getOrderSeries(String orderGuid)
      throws HibernateException, NamingException, OrderNotFoundException {
	  List series = null;
	  if (true) throw new RuntimeException("Dead code!");
	  /*
    Session session = null;
    List seriesLinks = null;
    List series = null;
    try {
      session = HibernateUtil.currentSession();

      Criteria guidCriteria = session.createCriteria(MCOrderSeriesLink.class);
      guidCriteria.add(new EqExpression("orderGuid", orderGuid, true));
      seriesLinks = guidCriteria.list();
      log.debug("Number of seriesLinks: " + seriesLinks.size());
      if (seriesLinks.size() == 0){
        // HACK. Need to create new order exception.
        // We have to stop processing here - if we don't then
        // the Disjunction below has no constraints and will
        // match *all* series present in the database.
        throw new OrderNotFoundException("No images have yet arrived for order  "
            + orderGuid);
      }
      Criteria seriesCriteria = session.createCriteria(MCSeries.class);

      Disjunction any = Expression.disjunction();

      for (int i = 0; i < seriesLinks.size(); i++) {
        MCOrderSeriesLink link = (MCOrderSeriesLink) seriesLinks.get(i);
        any.add(new EqExpression("mcGUID", link.getMcGuid(), true));
      }
      seriesCriteria.add(any);
      series = seriesCriteria.list();
      log.debug("Number of matching series:" + series.size());
      Collections.sort(series, new Comparator() {
        public int compare(Object o1, Object o2) {
          MCSeries series1 = (MCSeries) o1;
          MCSeries series2 = (MCSeries) o2;
          return series1.getSeriesNumber() - series2.getSeriesNumber();
        }
      });

    } finally {

      HibernateUtil.closeSession();
    }
    */
    return (series);
  }
/*
  public void saveOrderx(MCOrder order) throws IOException {
    XStream xstream = new XStream(new DomDriver());
    xstream.alias("MCOrder", MCOrder.class);

    //String stringXML = xstream.toXML(dbSeries);
    File dataDir = FileUtils.resolveGUIDAddress(rootDirectory, order
        .getOrderGuid());
    dataDir.mkdirs();
    File dataMetadataFile = new File(dataDir, order.getOrderGuid() + ".xml");

    FileWriter out = new FileWriter(dataMetadataFile);
    //out.write(stringXML);
    xstream.toXML(order, out);
    out.close();
    log.info("order metadata written to " + dataMetadataFile.getAbsolutePath());
  }
  */
}