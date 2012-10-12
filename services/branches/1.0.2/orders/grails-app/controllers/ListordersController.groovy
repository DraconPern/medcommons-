import grails.converters.*

 class ListordersController {

    def ORDER_EXPIRY_DELAY = 7 * 60 * 60 * 24 * 1000

    def formats = [ 
      xml : { [text: it as XML, contentType: "text/xml" ]  },
      json: { [text: it as JSON, contentType: "text/json" ]  },
      html: { [view: 'orders', model: [orders:it] ]  }
    ]

    def index = { 

      def config = grailsApplication.config
      def orders
      def expiryThreshold = new Date( System.currentTimeMillis() - ORDER_EXPIRY_DELAY )
      if(session.security == "ip") {	      orders = DicomOrder.findAllByDateCreatedGreaterThan(
		    		  expiryThreshold, [sort:'dateCreated', order:'desc'])
      }
      else {
    	  
          def p = Camel.toCamel(params)
          orders = DicomOrder.findAllByDateCreatedGreaterThanAndGroupAccountId(
	        		  expiryThreshold, session.info.activeGroupAccid, [sort:'dateCreated', order:'desc']
                   )
      }
    
      // For later, when we also show the patient names
//      
//      def mcids = orders.grep { it.mcid != null }.collect { Long.parseLong(it.mcid) }
//      def users = User.getAll(mcids)
//
//      println "Found ${users.size()} patient accounts for orders"
    
      println "Got ${orders.size()} Orders"
      def format = params.fmt?:'html'
      render(formats[format](orders))
    }
}
