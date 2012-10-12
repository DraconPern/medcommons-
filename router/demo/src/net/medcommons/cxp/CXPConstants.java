/*
 * $Id$
 * Created on 4/10/2005
 */
package net.medcommons.cxp;

public class CXPConstants {
    
    public final static String COMMAND_TRANSFER = "TRANSFER";// obsolete - should be replaced by PUT

    public final static String COMMAND_QUERY = "QUERY"; // Obsoleted - should be replaced by GET.
    
    public final static String COMMAND_PUT = "PUT";
    
    public final static String COMMAND_GET = "GET";
    
    /**
     * A MedCommons Extensions - delete the specified CCR
     */
    public final static String COMMAND_DELETE = "DELETE";

    public final static String COMMAND_UNDEFINED = "UNDEFINED";
    
    public final static String STATUS_MISSING_CCR = "MISSING_CCR";

    public final static String STATUS_MISSING_OPCODE = "MISSING_OPCODE";

    public final static String STATUS_UNSUPPORTED = "UNSUPPORTED";

    public final static String STATUS_MISSING_QUERY_STRING = "MISSING_QUERY_STRING";
    
    public final static String STATUS_MISSING_TRACKING_NUMBER = "MISSING_TRACKING_NUMBER";
    
    public final static String STATUS_MISSING_PIN = "MISSING_PIN";
    
    public final static String STATUS_INVALID_QUERY_STRING = "INVALID_QUERY_STRING";
    
    public final static String STATUS_REPOSITORY_ERROR = "REPOSITORY_ERROR";

    public final static String CXP = "CXP";
    
    public final static String CXP_FILES = "Files";
    
    public final static String CXP_FILENAME = "FileName";

    public final static String CXP_FILETYPE = "FileType";

    public final static String CXP_FILECONTENTS = "FileContents";

    public final static String CXP_SHA1 = "SHA1";

    public final static String CXP_OPCODE = "OperationCode";
    
    public final static String CXP_QUERYSTRING = "QueryString";
    
    public final static String CXP_REASON = "Reason";
    
    public final static String CXP_STATUS = "Status";
    
    public final static String CXP_UID = "UID";
    
    public final static String CXP_TXID = "TXID";
    
    public final static String CXP_Version = "CXPVersion";
    
    public final static String CXP_INFORMATION_SYSTEM = "InformationSystem";
    
    public final static String CXP_INFORMATION_SYSTEM_NAME = "Name";
    
    public final static String CXP_INFORMATION_SYSTEM_VERSION = "Version";
    
    public final static String CXP_INFORMATION_SYSTEM_TYPE = "Type";
    
    public final static String CXP_RegistrySecret = "RegistrySecret";
    public final static String CXP_ConfirmationCode = "ConfirmationCode";
    public final static String CXP_GUID = "Guid";
    
    /**
     * All 2xx errors are successful.
     */
    public final static int CXP_STATUS_SUCCESS = 200;
    public final static int CXP_STATUS_SUCCESS_MULTIPLEEMAIL = 210;
    public final static int CXP_STATUS_SUCCESS_VALIDATION_WARNINGS = 220;
    
    /**
     * All 4xx errors denote a Client Error
     */
    public final static int CXP_STATUS_BAD_REQUEST = 400;
    
    public final static int CXP_STATUS_BAD_CXP_VERSION = 420;
    
    public final static int CXP_STATUS_BAD_CLIENT_VERSION = 430;
    
    /**
     * All 5xx errors denote a Server Error
     */
    public final static int CXP_STATUS_SERVER_ERROR = 500;
}
