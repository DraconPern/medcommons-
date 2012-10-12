/**
 * Set of valid DDL Order States 
 * 
 * @author ssadedin
 */
enum OrderState {
	
	/**
	 * Nothing has happened to the order - it has been created
	 */
	DDL_ORDER_NEW,
	
	/**
	 * This is not currently used, instead the order stays in NEW
	 */
	DDL_ORDER_ACCEPTED,
	
	/**
	 * DDL has started uploading data, but not finished
	 */
	DDL_ORDER_XMITING,
	
	/**
	 * DDL has finished uploading image data for the order
	 */
	DDL_ORDER_UPLOAD_COMPLETE,
	
	/**
	 * The download or export phase for the order took longer than the configured
	 * threshold (this status is issued by the poller).   The order may continue
	 * processing and transition to DDL_ORDER_DOWNLOAD_COMPLETE or DDL_ORDER_COMPLETE.
	 */
	DDL_ORDER_TIMEOUT_WARNING,
	
	/**
	 * The download of imaging for the order has completed
	 */
	DDL_ORDER_DOWNLOAD_COMPLETE,
	
	/**
	 * Download and export (if configured) of imaging data have completed
	 */
	DDL_ORDER_COMPLETE,
	
	/**
	 * Can occur at any point in the lifecycle, the order has been cancelled
	 */
	DDL_ORDER_CANCELLED,
	
	/**
	 * The order timed out
	 */
	DDL_ORDER_UPLOAD_TIMEOUT,
	
	/**
	 * The order experienced an unrecoverable error
	 */
	DDL_ORDER_ERROR
	
	/**
	 * Set of states considered 'final'.  When these states
	 * are reached, notifications are sent to users who are
	 * subscribed to receive notifications of order outcomes
	 */
	public static EnumSet<OrderState> UPLOAD_FINAL = EnumSet.of(
		DDL_ORDER_UPLOAD_COMPLETE, 
    	DDL_ORDER_UPLOAD_TIMEOUT,
		DDL_ORDER_ERROR
	)
	
	public static EnumSet<OrderState> SCAN_FOR_TIMEOUT = EnumSet.of(
		DDL_ORDER_NEW,
		DDL_ORDER_ACCEPTED,
		DDL_ORDER_XMITING
	)
}
