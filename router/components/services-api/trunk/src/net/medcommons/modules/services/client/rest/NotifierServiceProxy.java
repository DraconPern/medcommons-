/*
 * $Id$
 * Created on 8/08/2005
 */
package net.medcommons.modules.services.client.rest;

import static net.medcommons.modules.utils.Str.nvl;

import java.util.Iterator;

import net.medcommons.modules.services.interfaces.NotifierService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.rest.RESTException;
import net.medcommons.rest.RESTUtil;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 * Implements a simple REST (aka HTTP POST) client for 
 * the MedCommons Notifier Service
 * 
 * @author ssadedin
 */
public class NotifierServiceProxy implements NotifierService {
    
    /**
     * Client for which this proxy is currently being used
     */
    private String authToken;

    /**
     * @param id
     */
    public NotifierServiceProxy(String id) {
        super();
        authToken = id;
    }

    public void notify(String mcId, String recipientAddress, String trackingNumber, String message) throws ServiceException {
        if((mcId == null) && (trackingNumber != null)) {
            mcId = "0000" + trackingNumber;
        }
        
         try {
            RESTUtil.call(authToken, "NotifierService.notify", "mcid", mcId, "to1", recipientAddress, "trackingNumber", trackingNumber, "message", message);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to send notification for mcId=" + mcId + " to recipient=" + recipientAddress, e);
        }
    }

    public void sendEmailCXP(String mcId, String recipientAddress, String trackingNumber, String message, String subject, String comments) throws ServiceException {
        if((mcId == null) && (trackingNumber != null)) {
            mcId = "0000" + trackingNumber;
        }
        
         try {
            RESTUtil.call(authToken, "NotifierService.sendEmailCXP", "mcid", mcId, 
                    "to1", recipientAddress, 
                    "trackingNumber", trackingNumber, 
                    "template", message, 
                    "subject", subject,
                    "message",nvl(comments, "")
                    );
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to send notification for mcId=" + mcId + " to recipient=" + recipientAddress, e);
        }
    }
    
    public String querySubject(String trackingNumber) throws ServiceException {
        if(trackingNumber == null)
            throw new ServiceException("Attempt to query for null tracking number");
        
        try {
            Document doc = RESTUtil.call(authToken, "NotifierService.querySubject", "trackingNumber",trackingNumber);
            
            Iterator iter = doc.getRootElement().getDescendants(new ElementFilter("subject"));
            if(iter.hasNext())
                return ((Element)iter.next()).getTextTrim();
            else
                return null;            
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to query subject for tracking number " + trackingNumber);
        }
    }

    public void sendFaxNotification(String mcId, String recipientAddress, String trackingNumber, String message, String subject) throws ServiceException {
        if((mcId == null) && (trackingNumber != null)) {
            mcId = "0000" + trackingNumber;
        }
        try {
            RESTUtil.call(authToken, "NotifierService.sendFaxEmail", 
                            "mcid", mcId, 
                            "to1",  recipientAddress, 
                            "trackingNumber",  trackingNumber, 
                            "message",  message, 
                            "subject", subject,
                            "comments", subject
                            );
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to send notification for mcId=" + mcId + " to recipient=" + recipientAddress, e);
        }
    }

    @Override
    public void sendLinkShareEmail(String recipientAddress, String subject, String link, String comments) throws ServiceException {
        try {
            RESTUtil.callJSON(authToken, "NotifierService.sendLinkShareEmail", 
                                "to", recipientAddress, 
                                "subject", subject, 
                                "link", link, 
                                "comments", comments);
        }
        catch (RESTException e) {
            throw new ServiceException("Unable to send link share notification to recipient=" + recipientAddress, e);
        }
    }


}
