

class DicomOrderController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
    	
    	def accid = session.info.activeGroupAccid
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ dicomOrderInstanceList: DicomOrder.findAllByGroupAccountId( accid, params),
          dicomOrderInstanceTotal: DicomOrder.countByGroupAccountId(accid) ]
    }

    def show = {
        def dicomOrderInstance = DicomOrder.get( params.id )
	    if(dicomOrderInstance.groupAccountId != session.info.activeGroupAccid)
	    	throw new IllegalArgumentException("You are not authorized to access information about this order")

        if(!dicomOrderInstance) {
            flash.message = "DicomOrder not found with id ${params.id}"
            redirect(action:list)
        }
        else {
        	
        	return [ dicomOrderInstance : dicomOrderInstance ] 
        }
    }

    def delete = {
        def dicomOrderInstance = DicomOrder.get( params.id )
        if(dicomOrderInstance) {
		    if(dicomOrderInstance.groupAccountId != session.info.activeGroupAccid)
		    	throw new IllegalArgumentException("You are not authorized to access information about this order")
            try {
                dicomOrderInstance.delete()
                flash.message = "DicomOrder ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "DicomOrder ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "DicomOrder not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def dicomOrderInstance = DicomOrder.get( params.id )

        if(!dicomOrderInstance) {
            flash.message = "DicomOrder not found with id ${params.id}"
            redirect(action:list)
        }
        else {
		    if(dicomOrderInstance.groupAccountId != session.info.activeGroupAccid)
		    	throw new IllegalArgumentException("You are not authorized to access information about this order")
            return [ dicomOrderInstance : dicomOrderInstance ]
        }
    }

    def update = {
        def dicomOrderInstance = DicomOrder.get( params.id )
        if(dicomOrderInstance) {
        	
		    if(dicomOrderInstance.groupAccountId != session.info.activeGroupAccid)
		    	throw new IllegalArgumentException("You are not authorized to access information about this order")
        	
            if(params.version) {
                def version = params.version.toLong()
                if(dicomOrderInstance.version > version) {
                    
                    dicomOrderInstance.errors.rejectValue("version", "dicomOrder.optimistic.locking.failure", "Another user has updated this DicomOrder while you were editing.")
                    render(view:'edit',model:[dicomOrderInstance:dicomOrderInstance])
                    return
                }
            }
            dicomOrderInstance.properties = params
            if(!dicomOrderInstance.hasErrors() && dicomOrderInstance.save()) {
                flash.message = "DicomOrder ${params.id} updated"
                redirect(action:show,id:dicomOrderInstance.id)
            }
            else {
                render(view:'edit',model:[dicomOrderInstance:dicomOrderInstance])
            }
        }
        else {
            flash.message = "DicomOrder not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def dicomOrderInstance = new DicomOrder()
        dicomOrderInstance.properties = params
        
        return ['dicomOrderInstance':dicomOrderInstance]
    }

    def save = {
        def dicomOrderInstance = new DicomOrder(params)
        
	    dicomOrderInstance.groupAccountId = session.info.activeGroupAccid
	    
        if(!dicomOrderInstance.hasErrors() && dicomOrderInstance.save()) {
            flash.message = "DicomOrder ${dicomOrderInstance.id} created"
            redirect(action:show,id:dicomOrderInstance.id)
        }
        else {
            render(view:'create',model:[dicomOrderInstance:dicomOrderInstance])
        }
    }
}
