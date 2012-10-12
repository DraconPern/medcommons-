import java.util.Date;


    def formats = [ 
      xml : { [text: it as XML, contentType: "text/xml" ]  },
      json: { [text: it as JSON, contentType: "text/json" ]  },
      html: { it,h,cfg -> [view: 'orderDetail', model: [order:it,history:h,cfg: cfg] ]  }
    ]
      DicomOrder order = DicomOrder.findByCallersOrderReference(p.callersOrderReference)
      log.info "Found order " + order
      if(order) {
        order.save()
		    def history = DicomOrderHistory.findAllByDicomOrder(order);
    	}
	        render(formats[format](order))
      }
      else {
        if(format != 'html')
          response.status = 404
        render(text: "Order ${p.callersOrderReference} could not be located.", contentType:"text/plain")
      }
    }

    def reset = {
      def order = DicomOrder.findById(params.id);

      if(order.ddlStatus == 'DDL_ORDER_COMPLETE') {
        order.ddlStatus = 'DDL_ORDER_UPLOAD_COMPLETE'
        def h = new DicomOrderHistory(
        		  ddlStatus: order.ddlStatus,
        		  description: "reset from link on status page",
        		  remoteIp: request.remoteAddr,
        		  remoteHost: request.remoteHost,
        h.save()
      }

      redirect(action: index,params:[callers_order_reference: order.callersOrderReference])
    }
    
    def history = {
      def history = DicomOrderHistory.findByDicomOrderId(params.id);
      render(text: history as JSON, contentType: "text/plain")
    }
}
