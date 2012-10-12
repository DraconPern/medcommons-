package net.medcommons.emcbridge;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.emc.solution.osa.client.dao.MasterClientData;

public class MasterClientUtil {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(MasterClientUtil.class.getName());
	
	public static void dumpMasterClient(MasterClientData masterClientData){
		Map<String, List<String>> imageMap = masterClientData.getImageMap();
		dumpMap("ImageMap",  imageMap);
		Map<String, List<String>> seriesMap = masterClientData.getSeriesMap();
		dumpMap("SeriesMap",  seriesMap);
		String orderFolderId = masterClientData.getOrderFolderId();
		log.info("Order folderid = " + orderFolderId);
		String faxOrderId =masterClientData.getFaxOrderId();
		log.info("faxOrderId = " + faxOrderId);
		
	}
	public static void dumpMap(String title, Map<String, List<String>> aMap){
		log.info("Title:" + title);
		//Map<String, List<String>> imageMap = masterClientData.getImageMap();
		Set<String> keySet = aMap.keySet();
		Iterator<String> keys = keySet.iterator();
		while (keys.hasNext()){
			String key = keys.next();
			Object obj = aMap.get(key);
			log.info("Key:" + key + "=> " + obj );
		}
	}
		

}
