import java.net.InetAddress;

import org.hsqldb.Session;

import grails.converters.*

/**
 * Handles creation of orders
 * 
 * @author ssadedin
 */
class OrderController {
	
	def form = {
	}
	
	def index = { 
		def p = Camel.toCamel(params)
		p.comments = p.orderComments
		
		OrderValidator validator
		if(request.ctx == 'timc') {
			validator = new timc.TIMCOrderValidator()
		}
		
		// We do not allow the caller to set the DDL status!
		p.remove('ddlStatus')
		
		p.email = false
		if(params.email in ['Y','N'])
		p.email = (params.email == 'Y')
		else
		if(params.email) {
			p.notificationEmail = params.email
		}
		
		if(params.baseline in ['Y','N'])
		p.baseline = (params.baseline == 'Y')
		
		p.modality = p.modality in ['PT','CT','MR'] ? p.modality : null
		
		log.info "Parsing date ${p.dueDate} ${p.dueTime}"
		if(p.dueTime && p.dueDate) 
		try { p.dueDateTime = Date.parse("MM/dd/yyyy HH:mm",p.dueDate + ' ' + p.dueTime) } catch(def e) {}
		
		if(p.scanTime && p.scanDate) 
		try {p.scanDateTime = Date.parse("MM/dd/yyyy HH:mm",p.scanDate + ' ' + p.scanTime) } catch(def e) {}
		
		DicomOrder order = new DicomOrder()
		try {
			// TODO: in a shared environment, need to make this check 
			// look for uniqueness wrt to group
			if(p.callersOrderReference && DicomOrder.findByCallersOrderReference(p.callersOrderReference)) {
				log.info "the order reference " + p.callersOrderReference + " not unique"
				throw new Exception("An order has already been created for reference ${p.callersOrderReference}.  Field 'callers_order_reference' must be unique.")
			}
			
			order.properties = p
			
			if(validator)
    			validator.validate(order)
			
			order.groupAccountId = params.groupAccountId ?: session.info.activeGroupAccid
			
			log.info "Group account id = $order.groupAccountId" 
			if(!order.save()) 
			throw new Exception(order.errors.allErrors.collect{it.toString()}.join('\n'))
			
			// Are there any custom field labels?
			DicomOrderLabel labels = null
			def foundLabels = [] // For debug only
			for(def i in 0..9) {
				def label = String.format("custom_%02d_label",i)
				if(!params[label])
    				continue
				
				labels = labels ?: new DicomOrderLabel()
				labels[String.format("label%02d",i)] = params[label]
				foundLabels << label
			}
			
			if(labels) {
				labels.dicomOrder = order
				log.info "Saving labels " + foundLabels
				if(!labels.save())
				throw new Exception("Failed to save custom order labels " + foundLabels)
			}
			
			def config = grailsApplication.config
						def url = config.appliance + "acct/dod.php?callers_order_reference=" +
			URLEncoder.encode(order.callersOrderReference)
			
			log.info "Security = $session.security"
			if(session.security != "ip") {
				url+="&accid="+order.groupAccountId
			}
			
			if(params.next) {
				url+="&next="+URLEncoder.encode(params.next)
			}
			
			redirect(url:url)
		}
		catch(Exception e) {
			render(view: 'error', model: [message: e.message, order: order])
		}
	}
	
	def notifications = {
	    CheckTimeoutJob.triggerNow()	
		Thread.sleep(1000)
	    SendOrderNotificationsJob.triggerNow()	
		render(text: 'Notifications scanned successfully')
	}
}
