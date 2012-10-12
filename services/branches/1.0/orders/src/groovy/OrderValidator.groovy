
interface OrderValidator {
	
	/**
	 * Do any custom conversions on the format of the 
	 * properties prior to binding with the order
	 */
    void preprocess(def properties)    
	
	/**
	 * Do any custom checks on validity of data after binding
	 */
	void validate(DicomOrder o)
}
