/*
 * $Id$
 * Created on 4/08/2005
 */
package net.medcommons.modules.services.interfaces;


/**
 * IdentityService is responsible for verifying user credentials, tracking
 * numbers and PINs and mapping these to medcommons ids.
 * 
 * @author ssadedin
 */
public interface NotifierService {    
    
    public void notify(String mcId, String recipientAddress, String trackingNumber, String message) throws ServiceException;

    /**
     * Sends a CXP Notification
     * 
     * @param mcId             medcommons id on behalf of whom the notification should be sent
     * @param recipientAddress email address/fax number to notify
     * @param trackingNumber   tracking number to notify about
     * @param message          optional message template specifier.  If blank or null then
     *                         a default template is used. 
     * @param subject          optional subject line, replaces standard subject if present.
     * @param comments TODO
     * @throws ServiceException  if an error occurs sending the notification or handling the call
     */
    public void sendEmailCXP(String mcId, String recipientAddress, String trackingNumber, String message, String subject, String comments) throws ServiceException;
    
    
    /**
     * Send notificationa about a link that shares a PHR
     * 
     * @param recipientAddress
     * @param subject
     * @param link
     * @param comments TODO
     * @throws ServiceException 
     */
    public void sendLinkShareEmail(String recipientAddress, String subject, String link, String comments) throws ServiceException;
    
    /**
     * Sends a notification for an inbound fax
     * 
     * @param mcId - medcommons id on behalf of whom the notification should be sent
     * @param recipientAddress - email address/fax number to notify
     * @param trackingNumber - tracking number to notify about
     * @param message - optional message, for current case this is ignored, I think
     * @param subject - optional subject line, replaces standard subject if present.
     * 
     * @throws ServiceException - if an error occurs sending the notification or handling the call
     */
    public void sendFaxNotification(String mcId, String recipientAddress, String trackingNumber, String message, String subject) throws ServiceException;
    
    /**
     * Searches for notifications for the given tracking number and returns 
     * the subject line of first such notification.
     * 
     * @param trackingNumber
     * @return
     * @throws ServiceException
     */
    public String querySubject(String trackingNumber) throws ServiceException;
}
