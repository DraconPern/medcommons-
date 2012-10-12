

class TIMCOrderValidator implements OrderValidator {

    void preprocess(def properties) {
		
	}
	
	/**
	 * Do any custom checks on validity of data after binding
	 */
	void validate(DicomOrder o) { 
        if(!o.scanDateTime)
		    o.errors.rejectValue('scan_date_time','default.field.missing' )   
			
        if(!o.dueDateTime)
		    o.errors.rejectValue('due_date_time', 'default.field.missing')   
			
	    if(!o.baseline) 
		    o.errors.rejectValue('baseline', 'default.field.missing')   
			
	    if(!o.protocolId) 
		    o.errors.rejectValue('protocolId', 'default.field.missing')   
	}
}
