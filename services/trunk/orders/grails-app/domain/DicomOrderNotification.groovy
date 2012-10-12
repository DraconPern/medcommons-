
/**
 * Tracks notifications that are sent for a particular order 
 * 
 * @author ssadedin
 */
class DicomOrderNotification {
	
    static mapping = {
      columns {
        sentDateTime(column: 'sent_date_time')
      }
    }
	
    static constraints = {
      error(nullable: true)
    }
 	
	String recipient 
	
	String subject
	
	Date sentDateTime = new Date()
	
	String status
	
	String error

	DicomOrder order
}
