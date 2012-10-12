package net.medcommons.modules.services.client.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;
import net.medcommons.modules.services.interfaces.DirectoryEntry;
import net.medcommons.modules.services.interfaces.DirectoryService;
import net.medcommons.modules.services.interfaces.SecondaryRegistryService;
import net.medcommons.modules.services.interfaces.ServiceException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;

/**
 * "REST" client proxy for the DirectoryService.  While other service urls are 
 * retrieved from gateway configuration, this service
 * requires the url of the service to be passed in the constructor.
 * 
 * @author ssadedin
 */
public class DirectoryServiceProxy implements DirectoryService {

	/**
	 * Client for which this proxy is currently being used
	 */
	private String authToken;
    
    /**
     * The base url of the service to query
     */
    private String url;

	/**
	 * @param id
	 * @param url - the url of the service
	 */
	public DirectoryServiceProxy(String id, String url) {
		super();
		authToken = id;
        this.url = url;
	}

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger .getLogger(DirectoryServiceProxy.class);

    public List<DirectoryEntry> query(String context, String externalId, String alias, String accid) throws ServiceException {
        try {
            
            String queryUrl = this.url;
            if(this.url.indexOf('?') < 0)
                queryUrl += "?";
            
            if(context!=null)
	            queryUrl+="&ctx="+context;
            
            if(externalId!=null)
	            queryUrl+="&xid="+externalId;
            
             if(alias!=null)
	            queryUrl+="&alias="+alias;
             
             if(accid!=null)
	            queryUrl+="&accid="+accid;
             
             Document doc = RESTUtil.executeUrl(queryUrl);
            
            List<DirectoryEntry> results = new ArrayList<DirectoryEntry>();
            for (Iterator iter = doc.getRootElement().getDescendants(new ElementFilter("todir_entry")); iter.hasNext();) {
                Element e = (Element) iter.next();
                results.add(new DirectoryEntry(
                                e.getChildTextTrim("name"),
                                e.getChildTextTrim("ctx"),
                                e.getChildTextTrim("xid"),
                                e.getChildTextTrim("alias"),
                                e.getChildTextTrim("contact"),
                                e.getChildTextTrim("accid")
                                ));
            }
            return results;
        }
        catch (JDOMException e) {
            throw new ServiceException("Unable to query todir for [" + accid+","+context+","+externalId+","+alias+"]",e);
        }
        catch (IOException e) {
            throw new ServiceException("Unable to query todir for [" + accid+","+context+","+externalId+","+alias+"]",e);
        }
    }
}
