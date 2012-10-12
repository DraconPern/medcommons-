package net.medcommons.modules.cxp;
/**
 * Constants used in CXP parameter block names.
 * @author sean
 *
 */
public interface CXPConstants {


        public final static String MEDCOMMMONS_REGISTRY = "MedCommons, Inc";
        public final static String MEDCOMMMONS_REGISTRY_ID = "medcommons.net";
        public final static String CXP_VERSION = "2.0";

        public final static String REGISTRY_SECRET = "RegistrySecret";
        public final static String AUTHORIZATION_TOKEN = "AuthorizationToken";
        public final static String CONFIRMATION_CODE = "ConfirmationCode";
        public final static String STORAGE_ID = "StorageId";
        public final static String NOTIFICATION_SUBJECT = "NotificationSubject";
        public final static String VERSION = "Version";
        public final static String SENDER_ID = "SenderId";
        public final static String EMERGENCY = "Emergency";
        public final static String RETRIEVE_DATA = "RetrieveData";
        public final static String LENGTH = "Length";
        public final static String SenderProviderId = "SenderProviderId";
        public final static String ReceiverProviderId = "ReceiverProviderId";
        public final static String PatientGivenName = "PatientGivenName";
        public final static String PatientFamilyName  = "PatientFamilyName";
        public final static String PatientIdentifier  = "PatientIdentifier";
        public final static String PatientIdentifierSource  = "PatientIdentifierSource";
        public final static String Guid  = "Guid";
        public final static String Purpose  = "Purpose";
        public final static String DOB  = "DOB";
        public final static String CXPServerURL  = "CXPServerURL";
        public final static String CXPServerVendor  = "CXPServerVendor";
        public final static String ViewerURL  = "ViewerURL";
        public final static String Comment  = "Comment";
        public final static String TRUE = "TRUE";
        public final static String FALSE = "FALSE";
        public final static String POPS_MEDCOMMONS_ID = "0000000000000000";

        /**
         * If the CCR is being posted as a response to an original
         * CCR, this attribute may contain a GUID identifying the original
         * CCR.
         */
        public final static String REFERRER_GUID = "ReferrerGuid";

        /**
         * Token for payment bypass (for example -DICOM) for a class of transactions where 
         * payment mechanisms can be bypassed.
         */
        public final static String PaymentBypassToken = "PaymentBypassToken";
        
        /**
         * Value for indicating how CCRs are to be merged upon upload.
         */
        public final static String MergeCCR = "MergeCCR";
        
        /**
         * States that the MergeCCR value can have in CXP. This list will grow with time.
         * 
         * @author sean
         *
         */
        public  enum MergeCCRValues{
            ALL, NONE, ONLY_REFERENCES
        }

}
