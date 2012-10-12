import java.util.Date;import org.mortbay.log.Log;import org.springframework.core.annotation.Order;import grails.converters.*import org.codehaus.groovy.grails.commons.ConfigurationHolder
 class OrderstatusController {

    def formats = [ 
      xml : { [text: it as XML, contentType: "text/xml" ]  },
      json: { [text: it as JSON, contentType: "text/json" ]  },
      html: { it,h -> [view: 'orderDetail', model: [order:it,history:h] ]  }
    ]        def index = {             log.info "Order status"            def p = Camel.toCamel(params)
      DicomOrder order = DicomOrder.findByCallersOrderReference(p.callersOrderReference)      
      log.info "Found order " + order            def format = params.fmt?:'html'
      if(order) {        if(order.mcid && order.mcid != "null") 			order.healthurl = grailsApplication.config.appliance + order.mcid	    else    	    order.healthurl = ""              	def ctx = request.ctx.replaceAll("^/","")        def base = ConfigurationHolder.config.grails.serverURL.replaceAll('/*$',"")        def path = "/$ctx/orderstatus?callers_order_reference=${order.callersOrderReference}".replaceAll("^//","/")		order.statusurl = base + path
        order.save()        	if(format == 'html')  {
		    def history = DicomOrderHistory.findAllByDicomOrder(order);		    log.info "Found ${history.size()} history items for order ${order.id}"		    history.each { DicomOrderHistory h ->		    	try {			    	if(!h.geoIp) {			    		log.info "Locating ip address " + h.remoteIp			    		h.locateIp()			    		h.save()			    	}		    	}		    	catch(Exception e) {		    		log.warn("Failed to query / update geo ip for dicom order history " + h.id)		    	}		    }	        render(formats[format](order,history))
    	}    	else
	        render(formats[format](order))
      }
      else {
        if(format != 'html')
          response.status = 404
        render(text: "Order ${p.callersOrderReference} could not be located.", contentType:"text/plain")
      }
    }
    /**     * Reset an order back to UPLOAD_COMPLETE status.  This allows DDLs that may have     * missed it to download it again.      */
    def reset = {
      def order = DicomOrder.findById(params.id);

      if(order.ddlStatus == 'DDL_ORDER_COMPLETE') {
        order.ddlStatus = 'DDL_ORDER_UPLOAD_COMPLETE'
        def h = new DicomOrderHistory(
        		  ddlStatus: order.ddlStatus,
        		  description: "reset from link on status page",
        		  remoteIp: request.remoteAddr,
        		  remoteHost: request.remoteHost,        		  dicomOrder: order        		)                order.save()
        h.save()
      }

      redirect(action: index,params:[callers_order_reference: order.callersOrderReference])
    }
    
    def history = {
      def history = DicomOrderHistory.findByDicomOrderId(params.id);
      render(text: history as JSON, contentType: "text/plain")
    }
}

