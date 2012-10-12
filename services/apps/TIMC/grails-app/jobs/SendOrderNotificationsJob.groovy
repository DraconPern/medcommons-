import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import groovy.text.SimpleTemplateEngine;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailSender;
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Polls the dicom_order_history table and checks if there are new entries
 * that have not been checked for notifications.
 * <p>
 * There are three types of notifications that may be sent:
 * <ul>
 * <li>Emails - when download is completed and order has a notification email set
 * <li>HTTP   - when order has a status callback URL set
 * <li>Timeout warning email - if order is in TIMEOUT_WARNING state and warnings
 *                             for timed out orders are configured.
 * 
 * @author ssadedin
 */
class SendOrderNotificationsJob {
    
    private static Logger log = Logger.getLogger(SendOrderNotificationsJob.class)
    
    // static triggers = { simple startDelay: 10000L, repeatInterval: 15000L }
    
    def grailsApplication = ApplicationHolder.getApplication()
    
    
    def localAddress = InetAddress.getLocalHost().hostAddress
    
    def timeout = 150000L 
    
    SettingsService settingsService
    
    MailSender mailSender
    
    ApplicationContext applicationContext = ApplicationHolder.getApplication().getParentContext()
    
    def settings
    
    /**
     * Checks for unprocessed order history change and for each one sends any necessary 
     * notifications.
     */
    def execute() {
    	
        settings = settingsService.getSettings(grailsApplication.config)  
    	
        log.info "Checking for order history notifications ..." 
        
        def orders = DicomOrderHistory.findAllByNotificationSent(false)
        
        log.info "Found ${orders.size()} notifications"
        
        orders.each { DicomOrderHistory h ->
            try {
                h.notificationSent = true
                if(!h.save())
                    throw new RuntimeException("Unable to update order history" + h.id)
                
                if(h.dicomOrder.ddlStatus == 'DDL_ORDER_DOWNLOAD_COMPLETE' && h.dicomOrder.notificationEmail) { 
                    log.info "Need to send notification for order " + h.dicomOrder + " / ${h.dicomOrder.callersOrderReference}" 
                    notifyOrderStatus(h.dicomOrder, "Image Download Complete", h.dicomOrder.notificationEmail)
                }
                
                if(h.dicomOrder.ddlStatus == 'DDL_ORDER_UPLOAD_COMPLETE' && h.dicomOrder.uploadNotificationEmail) { 
                    log.info "Need to send notification for order " + h.dicomOrder + " / ${h.dicomOrder.callersOrderReference}" 
                    notifyOrderStatus(h.dicomOrder,"Image Upload Received", h.dicomOrder.uploadNotificationEmail)
                }
                
                sendCallbackNotification(h.dicomOrder)

                log.info "timeout notification to $settings.acNotifyDDLTimeout ,  order status = $h.dicomOrder.ddlStatus" 
                if(settings.acNotifyDDLTimeout && h.ddlStatus == "DDL_ORDER_TIMEOUT_WARNING") {
                    log.info "Sending timeout warning for order $h.dicomOrder.callersOrderReference" 
                    sendTimeoutWarningNotification(h.dicomOrder) 
                } 
            }
            catch(Exception e) {
                log.error("Unable to send notification for order history item " +  h.id,e)
            }
        }
    }

    /**
     * Checks if the specified order has a notification URL set and if so, 
     * executes an outbound HTTP call to the specified URL including details
     * of the new order status.
     */
    def sendCallbackNotification(DicomOrder order) {
        
        if(!order.statusCallback) 
            return
        
        log.info "Notifying callback URL ${order.statusCallback} of order ${order.callersOrderReference} status change to ${order.ddlStatus}"
        try {
            URL url = new URL(order.statusCallback)
            if(url.host.toLowerCase().indexOf("localhost")>=0)
                throw new IllegalArgumentException("Bad host name in URL supplied for callback notifications: " + order.statusCallback)
            
            InetAddress addr = InetAddress.getByName(url.host)
            if(addr.hostAddress == "127.0.0.1" || addr.hostAddress == localAddress)
                throw new IllegalArgumentException("Illegal host name in URL supplied for callback notifications: " + order.statusCallback + " - host resolves to local address")
            
            def callbackUrl = order.statusCallback 
            if(callbackUrl.indexOf("?") < 0)
                callbackUrl += "?"
            
            log.info "Server URL = " + ConfigurationHolder.config.grails.serverURL
            
            String statusUrl = ConfigurationHolder.config.grails.serverURL  + 
                            "/${grailsApplication.config.context}/orderstatus?callers_order_reference=${order.callersOrderReference}"
            def params = [ 
                            callers_order_reference: order.callersOrderReference,
                            patient_id: order.patientId,
                            status: order.ddlStatus,
                            healthurl: grailsApplication.config.appliance + order.mcid,
                            status_url: statusUrl
                         ]
            callbackUrl += params.collect { k,v ->
                k + "=" + URLEncoder.encode(v)
            }.join("&")
            
            log.info "Invoking callback URL: $callbackUrl"
            def output = new URL(callbackUrl).text
            if(output.size()>200)
                output = output.substring(0,200)
            
            log.debug "Received result from callback: " + output
        }
        catch(Exception e) {
            log.warn("Unable to execute order status callback for order " + order.id,e)
        }
    }
    
    /**
     * Send a notification about a DICOM upload to the specified address
     * for the specified order
     */
    def notifyOrderStatus(DicomOrder order, String subject, String emailAddress) {
        
        log.info "Notifying about order status change: $order.callersOrderReference"
        
        def binding = [
                        "order": order, 
                        "settings": settings
                      ]
        def emailSubject = "$settings.acCommonName - $subject"
        sendNotificationEmail(emailAddress, emailSubject, "orderNotificationEmail", binding)
    }
    
    def sendTimeoutWarningNotification(DicomOrder order) { 
        def to = settings.acNotifyDDLTimeout?:settings.acFromEmail
        sendNotificationEmail(to,  "WARNING: Order $order.callersOrderReference Exceeded Timeout Threshold",
        		"orderDelayNotificationEmail",
        		[order: order, settings: settings ] )
    }
    
    /**
     * Sends a MIME email containing both plain text and HTML renderings
     * to the specified addresses with the given subject.
     * 
     * @param to
     * @param subject
     * @param template 		the prefix of the templates. Expects to find these
     * 						in the web root with Html.gtpl and Text.gtpl suffixes.
     * @param model			data to be passed through to the templates
     */
    def sendNotificationEmail(String to, String subject, String template, Map model) {
        log.info "Sending notification email to $to: $subject (template = $template)"
        def tpl = applicationContext.getResource("/"+template+"Text.gtpl").file.text
        def htmlTpl = applicationContext.getResource("/"+template+"Html.gtpl").file.text
        
        model.esc = { StringEscapeUtils.escapeHtml(it) }
        
        def engine = new SimpleTemplateEngine()
        def textContent = engine.createTemplate(tpl).make(model).toString()
        def htmlContent = engine.createTemplate(htmlTpl).make(model).toString()
        
        MimeMessage msg = mailSender.createMimeMessage();
        to.split(",").each { 
            msg.addRecipient(RecipientType.TO, new InternetAddress(it.trim()))
        }
        msg.from = new InternetAddress(settings.acFromEmail) 
        msg.subject = subject
        
        MimeMultipart part = new MimeMultipart("alternative")
        MimeBodyPart textPart = new MimeBodyPart()
        textPart.setText(textContent)
        part.addBodyPart(textPart)
        
        MimeBodyPart htmlPart = new MimeBodyPart()
        htmlPart.setContent(htmlContent, "text/html")
        part.addBodyPart(htmlPart)
        msg.setContent(part)
        mailSender.send(msg)    }
}
