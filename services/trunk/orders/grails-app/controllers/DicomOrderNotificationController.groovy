class DicomOrderNotificationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		
		DicomOrder order = DicomOrder.get(Long.parseLong(params.id))
		
		if(order.groupAccountId != session.info.activeGroupAccid)
		    throw new Exception("Unauthorized Access")
		
        [
		 dicomOrderNotificationInstanceList: DicomOrderNotification.findAllByOrder(order),
	     dicomOrderNotificationInstanceTotal: DicomOrderNotification.count(),
		 dicomOrder: order
		]
    }

    def show = {
        def dicomOrderNotificationInstance = DicomOrderNotification.get(params.id)
		
		if(dicomOrderNotificationInstance.order.groupAccountId != session.info.activeGroupAccid)
		    throw new Exception("Unauthorized Access")
		
        if (!dicomOrderNotificationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dicomOrderNotification.label', default: 'DicomOrderNotification'), params.id])}"
            redirect(action: "list")
        }
        else {
            [dicomOrderNotificationInstance: dicomOrderNotificationInstance]
        }
    }
}
