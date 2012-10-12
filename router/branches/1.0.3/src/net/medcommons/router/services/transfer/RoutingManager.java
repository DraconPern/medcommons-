/*
 * Created on Aug 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.transfer;

import java.util.Date;

import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.dicom.util.MCStudy;

import org.apache.log4j.Logger;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * Might need a factory method to generate a routing manager class.
 * Area where customization can be used.
 */
public class RoutingManager {

	private static Logger log = Logger.getLogger(RoutingManager.class);
	private static boolean usePinkBox = true;
	private static String pinkBoxURL = null;
    

	/**
	 * 
	 */
	public RoutingManager(String pinkURL) {
		super();
		pinkBoxURL = pinkURL;
		// TODO Auto-generated constructor stub
	}
	


	/**
	 * Resolves the recipent GUID into a destination GUID 
	 * for routing.
	 * 
	 * Current implementation is trivial (it's an identity
	 * transform). In the future a different data structure might 
	 * be returned - perhaps a path (sequence of destinations) might
	 * be relevant.
	 * 
	 * @param recipientGuid
	 * @return
	 */
	private String resolveRecipientToDestination(String recipientGuid) {
		String dest = recipientGuid;
		return (dest);
	}

	private String resolveNodeSource(String nodeGuid) {
		String node = nodeGuid;
		return (node);
	}

	public RoutingQueue createRoutingQueueEntry(
		MCOrder order, 
		String mcGuid, 
		String protocol,
		String itemType,
		String commandType) {
		if (order == null)
			throw new NullPointerException("Null order; can't create routing entry");
		RoutingQueue entry = new RoutingQueue();
		entry.setDestinationGatewayGuid(order.getRecipientGuid());
		entry.setOriginatorGatewayGuid(order.getOriginatorGuid());
		entry.setMcGuid(mcGuid);
		entry.setProtocol(protocol);
		entry.setOrderGuid(order.getOrderGuid());
		entry.setRestartCount(0);
		entry.setTimeEntered(new Date(0));
		entry.setGlobalStatus(RoutingQueue.STATUS_NEW);
		entry.setItemType(itemType);
		entry.setCommandType(commandType);

		return (entry);

	}

	public void init(){
		setQueueItemStatus(RoutingQueue.STATUS_IN_PROGRESS,
						RoutingQueue.STATUS_NEW);
	}
	/**
	 * Need to be careful about porting this over.
	 * Need to put 'where' clause in that sets the destination address.
	 * 
	 * @param oldStatus
	 * @param newStatus
	 */
	private  void setQueueItemStatus(String oldStatus, String newStatus){
	    log.info("Hack. setQueueItemStatus is now a no-op; needs to set all relevant queue items from status "
          + oldStatus + " to " + newStatus);
  }
  /*
		Session session = null;
		Transaction tx = null;
		MCSeries dbSeries = null;
		try {
			session = HibernateUtil.currentSession();

			tx = session.beginTransaction();
			Criteria workInProgress =
						session.createCriteria(RoutingQueue.class);
			EqExpression inProgress =
				new EqExpression(
					"globalStatus",
					oldStatus,
					true);
			workInProgress.add(inProgress);
			List items = workInProgress.list();
			if (!items.isEmpty()){
				log.info("Resetting " + items.size() + " RouteQueue items status from " +
					oldStatus + " => " + newStatus);
				Iterator iter = items.iterator();
				while (iter.hasNext()){
					RoutingQueue inProgressItem = (RoutingQueue) iter.next();
					inProgressItem.setGlobalStatus(newStatus);
					session.save(inProgressItem);
					
				}
				
			}
			tx.commit();
		}
		catch(Exception e){
			e.printStackTrace();
			log.error(e.toString());
		}
		finally{
			try{
			HibernateUtil.closeSession();
			}
			catch(Exception e2){
				e2.printStackTrace();
				log.error(e2.toString());
			}
		}
					
	}
  */
/*
	public void saveOrder(Session session, MCOrder order)throws HibernateException{
		session.save(order);
		if (usePinkBox){
			saveOrderOnPinkBox(order);
		}
	}
	
	private void saveOrderOnPinkBox(MCOrder order){
		
	}
	*/
	public static MCSeries getSeriesByGuid(String mcGuid) {
		if (true) throw new RuntimeException("Dead code");
		MCSeries dbSeries = null;
		return(dbSeries);
	/*
		Session session = null;
		Transaction tx = null;
		MCSeries dbSeries = null;
		try {
			session = HibernateUtil.currentSession();

			tx = session.beginTransaction();

			Criteria seriesCriteria = session.createCriteria(MCSeries.class);
			EqExpression eqGuid = new EqExpression("mcGUID", mcGuid, true);
			seriesCriteria.add(eqGuid);
			List series = seriesCriteria.list();
			dbSeries = null;
			if (!series.isEmpty()) {
				dbSeries = (MCSeries) series.get(0);
			}

			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());

		} finally {
			try {
				HibernateUtil.closeSession();

			} catch (Exception e) {
				log.error("Failed closing session:" + e);
				e.printStackTrace();
			}
		}
		return(dbSeries);
		*/
	}
  /**
   * Returns null if no routing is to be performed for this study.
   * 
   * Needs to incorporate logic for DF/HCC incorporation of 
   * DBM into routing rules. Need some type of hook here.
   * 
   * @param study
   * @return
   */
  public String[] resolveOrderDestination(MCStudy study){
      String[] dest = null;
      if (usePinkBox){ 
        dest = new String[1];
        dest[0] = "MGHB"; // Needs lookup logic/rules
      }
      
      return(dest);
    
  }
}
