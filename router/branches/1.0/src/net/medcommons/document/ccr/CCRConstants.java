/*
 *  Copyright 2005 MedCommons Inc.   All Rights Reserved.
 * 
 * Created on Feb 8, 2005
 *
 * 
 */
package net.medcommons.document.ccr;

import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.phr.ccr.Constants;

/**
 * Constants used in CCR processing.
 * 
 * @author sean
 * 
 * 
 */
public interface CCRConstants extends DocumentTypes{
	public String MEDCOMMONS_AFFINITY_DOMAIN = "MedCommons Patient Identifier";

    public String CCR_CHANGE_NOTIFICATION_STATUS_PENDING = "Pending";
    
    public String CCR_CHANGE_NOTIFICATION_STATUS_NOTIFIED = "Notified";
    
    /**
     * Type of ID for MedCommons Account IDs when specified for actors in the CCR
     */
    public static final String MEDCOMMONS_ACCOUNT_ID_TYPE = Constants.MEDCOMMONS_ACCOUNT_ID_TYPE;
    
    /**
     * Type of ID for MedCommons Patients when specified in the CCR
     */
    public static final String MEDCOMMONS_PATIENT_ID_TYPE = MEDCOMMONS_ACCOUNT_ID_TYPE;
	
	/**
	 * The URN of the namespace for CCR documents. This is the value that appears
	 * in the XML header.
	 */
	public static final String CCR_NAMESPACE_URN = "urn:astm-org:CCR";

	/**
	 * Location of the XSD on disk
	 */
	public static String XSD_LOCATION = "conf/CCR_20051109.xsd";
	
	/**
	 * The URI of the namespace for CCR documents. This is the value that is returned by 
	 * the JDOM getNamespaceURI() method.
	 */
	public static final String CCR_NAMESPACE_URI = "uri:" + CCR_NAMESPACE_URN;
	
	/**
	 * Schema validation OFF - only unvalidated parsing performed.
	 */
	public final static String SCHEMA_VALIDATION_OFF = "OFF";
	/**
	 * Schema validation LENIENT - CCRs are parsed; validation errors returned as warning messages but
	 * processing proceeds.
	 */
	public final static String SCHEMA_VALIDATION_LENIENT = "LENIENT";
	
	/**
	 * Schema validation STRICT - Any schema validation failures return an error.
	 */
	public final static String SCHEMA_VALIDATION_STRICT = "STRICT";

}
