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
 * Polls the database to check if any orders were created and have not progressed
 * to a final state in the maximum allowed time.  Any orders found in a non-final
 * state are transitioned to the final state OrderState.DDL_ORDER_TIMEOUT.
 *
 * @author ssadedin
 */
class CheckTimeoutJob {
	
	private static Logger log = Logger.getLogger(CheckTimeoutJob.class)
	
	// static triggers = { simple startDelay: 10000L, repeatInterval: 15000L }
	
	def grailsApplication = ApplicationHolder.getApplication()
	
	def timeout = 300000L
	
	SettingsService settingsService
	
	ApplicationContext applicationContext = ApplicationHolder.getApplication().getParentContext()
	
	def settings
	
	/**
	 * Default timeout period - 1 hours
	 */
	static final long DEFAULT_TIMEOUT_MS = 60 * 60 *  1000; // 1 hour
	
	/**
	 * Checks for unprocessed order history change and for each one sends any necessary
	 * notifications.
	 */
	def execute() {
		
		settings = settingsService.get()
	 
		long timeoutMs = settings.acOrderTimeoutMs?:DEFAULT_TIMEOUT_MS
		
		log.info "Checking for order timeouts (orders older than $timeoutMs seconds) ..."
		
		// def orders = DicomOrder.findAllByDdlStatusIn([OrderState.DDL_ORDER_ACCEPTED, OrderState.DDL_ORDER_NEW])
		def c = DicomOrder.createCriteria()
		def orders = c {
			'in'("ddlStatus", OrderState.SCAN_FOR_TIMEOUT as List ) 
			lt("dateCreated", new Date(System.currentTimeMillis() - timeoutMs))
		} 
		
		log.info "Found ${orders.size()} orders to time out"
		 
		orders.each { DicomOrder order ->
			log.info "Timing out order $order.id"
			
			def oldState = order.ddlStatus
			
			// Add a order history row
			order.transition(OrderState.DDL_ORDER_UPLOAD_TIMEOUT, "Timed out by system due to inactivity for "+(int)(timeoutMs/60000) + " mins in stage $oldState", settings)
			
			if(!order.save())
    			throw new RuntimeException("Unable to save order")
		}
	}
}
