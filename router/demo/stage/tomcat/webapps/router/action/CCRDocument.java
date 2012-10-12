/*
 * $Id$
 * Created on 1/02/2005
 */
package net.medcommons.router.services.xds.consumer.web.action;

import static java.util.Arrays.asList;
import static net.medcommons.modules.utils.Str.blank;
import static net.medcommons.modules.utils.Str.escapeForJavaScript;
import static net.medcommons.modules.utils.Str.join;
import static net.medcommons.modules.utils.Str.nvl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import net.medcommons.Version;
import net.medcommons.document.CCRParseErrorHandler;
import net.medcommons.document.CCRParseException;
import net.medcommons.document.ValidatingParserFactory;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.documentum.DocumentRetrievalService;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.cxp.server.RepositoryMetadataHandler;
import net.medcommons.modules.filestore.RepositoryFileProperties;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.services.interfaces.AccountDocumentType;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.DateFormats;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.Str;
import net.medcommons.modules.utils.event.EventManager;
import net.medcommons.modules.utils.event.Listener;
import net.medcommons.modules.xml.MultipleElementException;
import net.medcommons.modules.xml.RegistryDocument;
import net.medcommons.modules.xml.XPathUtils;
import net.medcommons.phr.PHRDocument;
import net.medcommons.phr.PHRElement;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRBuilder;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRElementFactory;
import net.medcommons.phr.ccr.CCRReferenceElement;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.phr.resource.Spring;
import net.medcommons.router.services.ccr.CCRChangeElement;
import net.medcommons.router.services.ccr.StorageMode;
import net.medcommons.router.services.dicom.util.DICOMUtils;
import net.medcommons.router.services.dicom.util.MCInstance;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.dicom.util.WindowLevelPreset;
import net.medcommons.router.services.repository.DocumentNotFoundException;
import net.medcommons.router.services.repository.DocumentRepository;
import net.medcommons.router.services.repository.LocalFileRepository;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.transfer.MCOrder;
import net.medcommons.router.services.xds.consumer.web.InvalidCCRException;
import net.medcommons.router.util.CustomJDOMFactory;
import net.medcommons.router.util.xml.CCRLoader;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.hibernate.HibernateException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Wraps a CCR document for user interface operations
 * 
 * @author ssadedin
 */
public class CCRDocument implements Serializable, CCRConstants, RegistryDocument {


    /**
     * public to allow unit tests to override
     */
    public static String TEMPLATE_PATH = "data/xds-templates"; 
    
    /**
     * Pattern used to parse javabean property style parameters
     */
    public static final Pattern INDEX_PATTERN = Pattern.compile(".*\\[([0-9]*)\\]$");
    
    public static final Pattern PROPERTY_PART_REGEX = Pattern.compile("\\.");
    
    public static final Pattern DATE_TIME_REGEX = Pattern.compile("datetime$", Pattern.CASE_INSENSITIVE);

	private static final Pattern COMMA_SPLITTER = Pattern.compile("[ ,]+");
	
	private static final int CHUNK = 1024 * 4;

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRDocument.class);
    
    /**
     * XML Log - for verbosely logging XML.
     */
	private static Logger xmlLog = Logger.getLogger("CCRXML");

	/**
	 * The guid of the order that is attached to this CCRDocument
	 */
	private String guid;
	
	
	/**
	 * The guid of the document this CCR was loaded from (if any)
	 */
	private String loadedFromGuid;

	/**
	 * Storage id that this CCR is to be stored under
	 */
	private String storageId = null;
	
	/**
	 * The name of the document. This can be anything, it is displayed in
	 * tooltips and rollovers in the user interface when showing the CCR.
	 */
	private String name;

	/**
	 * XML Content
	 */
	private String xml;

	/**
	 * Location of the data
	 */
	private String uri; 

	/**
	 * List of related series
	 */
	private List<MCSeries> seriesList;

	/**
	 * The jdomDocument - initialized lazily with the xml
	 */
	private Document jdomDocument;

	/**
	 * The time in milliseconds when the document was created.
	 */
	private long createTimeMs = -1; // -1 == uninitialized

	/**
	 * Lazily initialized list of contacts embedded in this CCR
	 */
	private List contacts;

	/**
	 * Information about the patient
	 */
	private Contact patient;
    
	/**
	 * Namespace that the document is in.
	 */
	private Namespace namespace = null;
	/**
	 * Order which may be associated with this CCRDocument. Lazily loaded,
	 * access via getOrder().
	 * 
	 * @deprecated
	 */
	private MCOrder order = null;

	/**
	 * ZULU Date Format
	 */
	private SimpleDateFormat ZULU_DATE_FORMAT;

	/**
	 * Tracking number assigned to this CCR (if any)
	 */
	private String trackingNumber;
    
    /**
     * PIN assigned to this CCR, corresponding to the tracking number (if any)
     */
    private String accessPin;

	/**
	 * Specific subject text used for this CCR (if any)
	 */
	private String subjectText;

	/**
	 * Schema validation setting (OFF, LENIENT, STRICT)
	 */
	private String schemaValiationSetting = null;

	/**
	 * Attributions which may be associated with this CCR
	 */
	private HashMap<String, String> attributions = new HashMap<String, String>();
    
    /**
     * Storage Mode for this document
     */
    private StorageMode storageMode = StorageMode.FIXED; 
    
    /**
     * The logical name / type of this CCR Document, if any
     */
    private AccountDocumentType logicalType;
    
    /**
     * Set to true if this CCR was created as a "new" CCR (ie. created from scratch).
     */
    private boolean isNewCcr;
    
    /**
     * Set to true if this CCR is provisional content
     */
    private boolean provisionalCcr;  
    
    /**
     * Display mode can be set to allow editing under different situations to
     * be handled different.  For example, when editing the Emergency CCR
     * we wish to display slightly differently and offer different commands.
     */
    private String displayMode = "";
        
	/**
	 * True if there were warning/error/fatal error messages
	 * parsing CCR, false otherwise.
	 */
	private boolean schemaValidationFailure = false;

	/** 
	 * Schema validation messsages. Null if validation is OFF or
	 * if there were no warning/error/fatal messages generated.
	 */
	private String schemaValiationMessages = null;
        
	private static SAXBuilder saveValidationBuilder = null;
    
	private static LocalFileRepository localFileRepository = null;
    
    /**
     * JDOM Document containing change history of this CCR, if such exists
     */
    private Document changeHistory;
    
    /**
     * Event manager for this document
     */
    private EventManager<CCRDocument> events = new EventManager<CCRDocument>();
    
    /**
     * Guid of change history for this document
     */
    private String changeHistoryGuid;
    
    /**
     * For production systems - this should be true.
     * Without this variable it's hard to test merges 
     * in JUnit
     */
	final static boolean throwErrorOnMissingReference = true;
	
	private boolean parseReferenceElements = true;
    /**
     * Pool of parsers for validating CCRs
     */
	private ObjectPool validatingParserPool =
        new GenericObjectPool(
                        new ValidatingParserFactory(Configuration.getProperty("CCRXSDLocation",XSD_LOCATION),CCR_NAMESPACE_URN));    
	
	private void init(){
		if(localFileRepository == null) // static field
			localFileRepository = (LocalFileRepository) RepositoryFactory.getLocalRepository();
        
        this.ZULU_DATE_FORMAT = new SimpleDateFormat(CCRElement.EXACT_DATE_TIME_ZULU_FORMAT);
        this.ZULU_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.ZULU_DATE_FORMAT.setLenient(true);
        this.schemaValiationSetting = CCRConstants.SCHEMA_VALIDATION_STRICT;
        this.utfOutputFormat = Format.getPrettyFormat();
        utfOutputFormat.setEncoding("UTF-8");
	}
    
	private Format utfOutputFormat = null;
    
    /**
     * Total size of this CCR including all attachments.  This is cached at load time
     * and not updated afterwards.   Do not rely on this value in any way if a CCR
     * is modified after loading.   
     */
    private long totalLoadedSizeBytes = -1;
	
	private String documentType = DocumentTypes.CCR_MIME_TYPE;
	
	private DocumentRetrievalService service = null;
	
	/**
	 * Blank constructor - for use by unit tests only!
	 */
	public CCRDocument() {
	    this.events.subscribe("save", this, new Listener<CCRDocument>() {
            public void onEvent(CCRDocument obj) throws Exception {
                XMLPHRDocument d = getJDOMDocument();
                if(d != null)
	                getJDOMDocument().setModified(false);
            }
	    });
	}
    
	/**
	 * Wraps given XML in a CCRDocument
	 * <p>
	 * The created document will have no guid set (essentially making it
	 * behave as an "unsaved" CCR) and no tracking number.
	 * 
	 * @param storageId storage id of document
	 * @param xml - xml of document
	 * @param schemaValiationSetting schema validation setting, see values enumerated
	 *                                in {@link CCRConstants}  eg. {@link CCRConstants#SCHEMA_VALIDATION_LENIENT}
	 */
	public CCRDocument(String storageId, String xml, String schemaValiationSetting) 
    	throws JDOMException, IOException, ParseException, RepositoryException, PHRException 
	{
	    
	    this(storageId, null, null, xml, schemaValiationSetting);
	}
			
	/**
	 * Creates a CCR with default parameters from the given XML
	 * 
	 * @param guid -
	 *            the guid of the CCR, if it has one
	 * @param xml -
	 *            the xml for the CCR
	 * @throws RepositoryException
	 * @throws PHRException 
	 */
	public CCRDocument(String storageId, String guid, String trackingNumber, String xml,
			String schemaValiationSetting) throws JDOMException, IOException,
			ParseException, RepositoryException, PHRException {
		this(storageId, guid, "CCR", "session://currentCcr", trackingNumber, xml,
				new ArrayList(), schemaValiationSetting);
	}

	/**
	 * @param name -
	 *            a user readable name for the CCR. This is displayed in the
	 *            user interface here and there but has no other significance.
	 * @param uri -
	 *            url for accessing the CCR. This is a holdover from days when
	 *            CCRs lived in XDS repositories and could be accessed by URL
	 *            form. This can mostly be defaulted to "session://currentCcr"
	 *            although this itself is something of a hack.
	 * @param xml -
	 *            the XML for the CCR
	 * @param seriesList
	 * @param storageId
	 * @param guid
	 * @param trackingNumber
	 * @param schemaValiationSetting
	 * 			OFF, LENIENT, or STRICT
	 * @throws IOException
	 * @throws JDOMException
	 * @throws ParseException
	 * @throws RepositoryException
	 * @throws PHRException 
	 */
	public CCRDocument(String storageId, String guid, String name, String uri,
			String trackingNumber, String xml, List<MCSeries> seriesList,
			String schemaValiationSetting) throws  ParseException, RepositoryException, PHRException {
		this(storageId,guid,name,uri,trackingNumber,xml,seriesList,schemaValiationSetting,true);
	}

	public CCRDocument(String storageId, String guid, String name, String uri,
	        String trackingNumber, String xml, List<MCSeries> seriesList,
	        String schemaValiationSetting,boolean parseReferenceElements) throws  ParseException, RepositoryException, PHRException {
	    this();
	    init();
	    log.info("CCRDocument constructor for:" + storageId + "," + guid);
	    this.guid = guid;
	    this.name = name;
	    this.uri = uri;
	    this.xml = xml;
	    this.seriesList = seriesList;
	    this.trackingNumber = trackingNumber;
	    this.schemaValiationSetting = schemaValiationSetting;  
	    this.parseReferenceElements = parseReferenceElements;
	    
	    String patientId = this.getPatientMedCommonsId();
	    
	    this.storageId = Str.blank(patientId) ? storageId : patientId;
	    assert storageId != null : "Storage id must not be null";
	    
	    // Cheating!  The size in bytes depends on the encoding, but we don't even know that here.
	    // So, we'll assume it's UTF-8 and mostly single byte chars.  This field is only an estimate.
	    this.totalLoadedSizeBytes = this.xml.length();
	    
	    //log.info("xml is:" + xml);
	    
	    log.debug("About to extract references from CCR");
	    
	    if(schemaValiationSetting.equals(CCRConstants.SCHEMA_VALIDATION_LENIENT)) {
	        log.info("Validation for this CCR is set to LENIENT - needs to be replaced with STRICT");
	    }
	    
	    // Get the References from the CCR
	    try {
	        parseReferences();
	    }
	    catch (TransactionException e) {
	        throw new PHRException("Unable to parse references",e);
	    }
	}
	
    /**
     * Adds a series for this CCR itself as the first series belonging to the document.  
     * 
     * @param storageId
     * @param guid
     * @throws PHRException 
     */
    private void addSelfSeries() throws PHRException {
        String newCcrDocId = generateObjectID();
        MCInstance instance = new MCInstance(
        		"showReport.jsp?stylesheet=ccr2htm&source=currentCcr.xml",
        		CCR_MIME_TYPE, storageId, guid);
        
        instance.setDocumentObj(this); // prevents querying the repository again later on
        
        MCSeries series = new MCSeries("CCR", storageId, newCcrDocId, instance);
        series.setMcGUID(newCcrDocId);
        this.seriesList.add(0, series);
    }
    

    /**
     * Create a CCRDocument representing the given PHR for the 
     * given account. 
     * 
     * @param accid
     * @param d
     * @throws PHRException 
     */
	public CCRDocument(String accid, PHRDocument d) throws PHRException {
        init();
        this.storageId = accid;
        this.jdomDocument = d.getDocument();
        this.guid =this.getJDOMDocument().getMetaData(getRoot(), "documentName");  
        this.seriesList = new ArrayList<MCSeries>();
        try {
            parseReferences();
        }
        catch (RepositoryException e) {
            throw new PHRException("Unable to parse references for document with guid = " + this.guid + " for account " + accid,e);
        }
        catch (PHRException e) {
            throw new PHRException("Unable to parse references for document with guid = " + this.guid + " for account " + accid,e);
        }
    }

    /**
     * Parses each reference from the XML and adds each one as a series object
     * 
	 * @param refs
	 * @throws JDOMException
	 * @throws IOException
	 * @throws TransactionException 
	 * @throws PHRException 
	 */
	public void parseReferences() throws RepositoryException, TransactionException, PHRException {
	   
	    this.seriesList.clear();
	    
		// Add ourself as the first series
        addSelfSeries();
        
        CCRElement refs = this.getRoot().queryProperty("references");        
        if (refs == null) {
            log.info("No references in this CCR");
            return;
        } 
        
        // ssadedin: I don't understand this logic - seems like when this is set 
        // to true it won't try to load references at all
        if (!throwErrorOnMissingReference)
            return;
        
        List<MCSeries> series = new ArrayList<MCSeries>();
		for (Iterator iter = refs.getDescendants(new ElementFilter("Reference", namespace)); iter.hasNext();) {
		    CCRReferenceElement ref = null;
			try {
				ref = (CCRReferenceElement) iter.next();
				
				MCSeries s = this.loadReference(ref);
				if(s != null) {
				    series.add(s);
				}
			}
			catch (RepositoryException e) {
				// For the moment - just ignore that there are missing elements.
				// This has to change - but if the CCR arrives before the 
				// <References> then an error will be thrown.
			    String msg = "Unable to load Reference: storage id "
								+ storageId + ", guid " + guid
								+ ", content-type " + getReferenceContentType(ref);
                if(parseReferenceElements)
					log.warn(msg, e);
                else
                    log.info(msg);
			}
            catch (IOException e) {
                if (parseReferenceElements)
                    throw new PHRException("Unable to load references for CCR " + this.getGuid(), e);
                else{
                    log.info("Ignoring missing reference element for " + this.getGuid() + " because parseRefernceElements is set to false");
                }
            }
		}
		
		verifyPayments(series);
		
		this.seriesList.addAll(series);
	}
    
	/**
	 * Verify whether the elements of the given series that have 
	 * payment required flag set have had the payments made.
	 * 
	 * @param series
	 * @throws PHRException
	 */
	private void verifyPayments(List<MCSeries> series) throws PHRException {
	    ServicesFactory f = Configuration.getBean("systemServicesFactory");
	    List<String> guids = new ArrayList<String>();
	    for(MCSeries s : series) {
	        if(s.getPaymentRequired())
	            guids.add(s.getMcGUID());
	    }
	    if(guids.isEmpty()) {
	        log.debug("No series with payment flag set");
	        return;
	    }
	    
	    log.info(guids.size() + " series have payment required flag set (" + join(guids, ",") + ")");
	    
	    try {
            Map<String, Boolean> statuses = f.getDocumentService().verifyPaymentStatus(this.storageId, guids.toArray(new String[] {}));
            for(MCSeries s : series) {
                if(statuses.containsKey(s.getMcGUID()) && statuses.get(s.getMcGUID())) {
                    log.debug("Series " + s.getMcGUID() + " has payment made: reverting payment flag");
                    s.setPaymentRequired(false);
                }
            }
        }
        catch (ServiceException e) {
            throw new PHRException("Unable to verify payment for documents", e);
        }
    }

    /**
	 * Attempt to load the given reference from the local repository and to add it to this
	 * CCR's series list.
	 * <p>
	 * Throws exception if reference cannot be found, unless the reference is a dicom one since
	 * dicom may be streaming in real time while / after CCR is stored.
	 */
    private MCSeries loadReference(CCRReferenceElement ref) throws PHRException, RepositoryException, IOException {
        
        DocumentRepository repository = RepositoryFactory.getLocalRepository();
        
        String id = ref.getChildText("ReferenceObjectID", namespace);
        log.info("Parsing CCR references");
        
        // Add this reference
        String contentType = getReferenceContentType(ref);

        // Check the form of the reference to identify if it is a
        // medcommons compatible one ...
        if (blank(contentType)) { // No content type - not
            // valid, ignore this entry
            log.info("Ignoring reference " + id + " - no content type specified.");
            return null;
        }

        String guid = ref.getGuid();
        
        // SS: Note - the guid may be null / blank here for some kinds of references
     
        String referenceFileID = guid; 
        if (!blank(guid) && !repository.inRepository(storageId, guid)){
            // If not in the local repository - test to see if the data can be retrieved 
            // from another source.
            long start = System.currentTimeMillis();
            log.error("Missing from repository :" + storageId + ", " + guid);
        }
        /* 
         Important code for document retrieval from Documentum.
         
        if (!blank(guid) && !repository.inRepository(storageId, guid)){
            // If not in the local repository - test to see if the data can be retrieved 
            // from another source.
            long start = System.currentTimeMillis();
            log.error("Missing from repository :" + storageId + ", " + guid);
            ReferenceParser refparser  = new ReferenceParser();
            Map<String,String> parameters = refparser.getStorageServiceParameters(ref);
            if (parameters == null){
                log.info("No storagehandler found in " + ref.toXml());
                throw new RepositoryException("Missing document from repository " + storageId + " , " + guid);
            }
            String serviceName = parameters.get("StorageHandler");
            if (serviceName == null){
                throw new NullPointerException("No StorageHandler service name is defined");
            }
            if (service == null){
                service = new DocumentRetrievalService();
                service.startTransaction(parameters);
            }
            String documentIdentifier = parameters.get("DocumentIdentifier");
         
            long startedTransaction = System.currentTimeMillis();
            localFileRepository.setDocumentRetrievalService(service); // Possible race condition.
            try{
                
                
                Map<String, SeriesObject> series =  service.retrieveMetadata();
                long readMetadata = System.currentTimeMillis();
                log.info("Documentum metadata  " + series + ", contains " + series.size() + " series");
                SeriesObject seriesObj = series.get(documentIdentifier);
                if (seriesObj == null){
                    throw new NullPointerException("Missing DocumentIdentifier in " + ref.toString());
                }
                log.info("Series " + seriesObj.getIdentifier() + "/" + documentIdentifier + 
                        "/" + guid +
                        " has " + seriesObj.getImages().size() + " images");
                SeriesMetadata.CreateSeriesMetadata(storageId, guid, contentType, seriesObj);
                /*
                // String seriesUID = parameters.get("DocumentIdentifier");
                Set<String> seriesKeys = series.keySet();
                Iterator<String> keys = seriesKeys.iterator();
                while(keys.hasNext()){
                    String identifier = keys.next();
                    SeriesObject seriesObj = series.get(identifier);
                    log.info("Series " + seriesObj.getIdentifier() + ", " + identifier + " has " + seriesObj.getImages().size() + " images");
                    SeriesMetadata.CreateSeriesMetadata(storageId, guid, contentType, seriesObj);
                }     
                 * /
                long finishedCreatingMetadata = System.currentTimeMillis();
                log.info("[timing] Total time to create medcommons metadata = " + (finishedCreatingMetadata - start));
                log.info("[timing] Time to start transaction                = " + (startedTransaction - start));
                log.info("[timing] Time to read metadata                    = " + (readMetadata - startedTransaction));
                log.info("[timing] Time to create MedCommons metadata       = " + (finishedCreatingMetadata -readMetadata));
               
                
            }
            catch(Exception e){
                throw new RepositoryException("Exception retrieving object from storage service " + serviceName, e);
                
            }
        }
*/
        if ("application/dicom".equals(contentType)) { // dicom content
            if(guid == null) {
                throw new NullPointerException("Null guid reference in " + Str.toString(ref));
            }

            try {
                MCSeries seriesReference = getDICOMSeriesReference(ref, storageId, guid);
                if (seriesReference != null) {
                    log.info("Just added DICOM series with guid " + guid + " to series list");
                    this.totalLoadedSizeBytes += repository.getContentLength(storageId, seriesReference.getMcGUID());
                    return seriesReference;
                } 
                else
                    log.warn("Series reference for DICOM attachment could not be parsed");
            }
            catch (DocumentNotFoundException exNotFound) {
                log.error("Unable to resolve dicom reference " + guid, exNotFound);
            }
            return null;
        } 
        else if (CCRConstants.CCR_CHANGE_HISTORY_MIME_TYPE.equals(contentType)) { // change history
            if(guid == null) { 
                throw new NullPointerException("Null guid reference in " + Str.toString(ref));
            }
            
            try {
	            this.loadChangeHistory(guid);
            }
            catch(Exception e) {
                log.warn("Unable to load change history " + guid + " due to error " + e.getMessage());
            }
            return null;
        } 
        else { // other content type
            Properties properties = repository.getProperties(storageId,guid); 
            if(!"URL".equals(contentType)) {
                String repositoryContentType = properties.getProperty(RepositoryFileProperties.CONTENT_TYPE);
                if (!Str.blank(repositoryContentType)) {
                    if (!repositoryContentType.equals(contentType)) {
                        log.warn("Repository content type ["
                                        + repositoryContentType
                                        + "] differs from that of CCR Reference + ["
                                        + contentType + "]");

                        // We let the repo content type /override/ the
                        // one from the CCR.
                        //
                        // Why? Because this lets us do content-type
                        // translation on-store without modifying
                        // the CCR. This is used in the case where
                        // people send us CCRs with text/xml type.
                        // In this case "sniff" the xml to see it is
                        // really a CCR and then store it with
                        // CCR mime-type in that case.
                        contentType = repositoryContentType;
                    }
                }
            }
            else
                referenceFileID = ref.queryTextProperty("referenceURL");

            MCInstance instance = new MCInstance(referenceFileID,contentType, storageId, guid);
            MCSeries series = new MCSeries(ref.queryTextProperty("referenceDisplayName"), storageId, guid, instance);
            series.setMcGUID(guid);
            log.info("Just added non-DICOM series with guid " + guid
                    + " to series list");

            // Check if the series has been validated
            String confirmationRequired = ref.queryTextProperty("referenceConfirmed");
            if ("true".equals(confirmationRequired)) {
                series.setValidationRequired(true);
            } 
                                 
            
            String paymentRequired = properties.getProperty(RepositoryFileProperties.PAYMENT_REQUIRED);
            if ("true".equals(paymentRequired)) {
                series.setPaymentRequired(true);
            } 
            
            if(!"URL".equals(contentType)) {
                this.totalLoadedSizeBytes += repository.getContentLength(storageId, series.getMcGUID());
            }
            
            return series;
        } 
    }

    private String getReferenceContentType(CCRElement ref) {
        CCRElement type = ref.getChild("Type");
        String contentType = null;
        if(type != null)
            contentType =  type.getChildText("Text");
        return contentType;
    }

    /**
	 * Attempts to load the requested guid as change history for this CCR.
     * @throws PHRException 
	 */
    private void loadChangeHistory(String guid) throws PHRException {
        try {
            this.changeHistoryGuid = guid;
            InputStream stream = localFileRepository.getDocument(storageId, guid);
            SAXBuilder builder = new SAXBuilder();
            builder.setFactory(new CustomJDOMFactory(CCRChangeElement.class));
            this.changeHistory = builder.build(stream); 
            if(xmlLog.isDebugEnabled()) {
                xmlLog.debug("Change history " + guid + ":\n" + Str.toString(this.changeHistory));
            }
        }
        catch (IOException e) {
            throw new PHRException("Unable to load change history " + guid);
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to load change history " + guid);
        }
    }
    
    /**
	 * Returns a list of pending change notifications from this CCR's change
	 * history.
	 */
    public List<CCRChangeElement> getChangeNotifications() {
        List<CCRChangeElement> result = new ArrayList<CCRChangeElement>();
        if(this.changeHistory == null)
            return result;
        
        for (Iterator iter = this.changeHistory.getRootElement().getChildren("ChangeSet").iterator(); iter.hasNext();) {
            Element changeSet = (Element) iter.next();
            if(CCRConstants.CCR_CHANGE_NOTIFICATION_STATUS_PENDING.equals(changeSet.getChildTextTrim("NotificationStatus"))) {
                for (Iterator changeIter = changeSet.getChild("Changes").getChildren("Change").iterator(); changeIter.hasNext();) {
                    result.add((CCRChangeElement) changeIter.next());
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the most recent change set 
     * 
     * @return a list of CCRChangeElements (empty if no change history available)
     * @throws IOException 
     */
    public List<CCRChangeElement> getLastChangeNotifications() throws IOException {
        List<CCRChangeElement> result = new ArrayList<CCRChangeElement>();
        if(this.changeHistory == null)
            return result;  
        
        Iterator iter = this.changeHistory.getRootElement().getChildren("ChangeSet").iterator();
        if(iter.hasNext()) {
            Element changeSet = (Element) iter.next();
            for (Iterator changeIter = changeSet.getChild("Changes").getChildren("Change").iterator(); changeIter.hasNext();) {
                result.add((CCRChangeElement) changeIter.next());
            }
        }
        if(xmlLog.isDebugEnabled()) {
	        xmlLog.debug(Str.toString(this.changeHistory));
        }
         
        return result;
    }
    
	private String getReferenceDisplayName(CCRElement ref) throws PHRException {
        String displayName = ref.queryTextProperty("referenceDisplayName");
        if (Str.blank(displayName)) {// No display name - not valid, ignore this entry
            return null;
        }
        
        return (displayName);
    }

    /**
	 * Returns the current CCR as a string.
	 */
	public String toString() {
		try {
			StringWriter sw = new StringWriter();
			
			new XMLOutputter(utfOutputFormat).output(this.getJDOMDocument(), sw);
			return super.toString() + ": " + (sw.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        catch (PHRException e) {
			throw new RuntimeException(e);
        }
	}

	public void syncFromJDom() throws PHRException {
		try {
            StringWriter sw = new StringWriter();
            new XMLOutputter(utfOutputFormat).output(this.getJDOMDocument(), sw);
            this.xml = sw.toString();
            this.patient = null;
            this.createTimeMs = -1;
            this.contacts = null;
            
        }
        catch (IOException e) {
            throw new PHRException(e);
        }
	}

	/**
	 * Returns a clone of this CCRDocument.
	 * TODO: Clones the document with the same storage id; not sure if this is correct.
	 * <p>
	 * PIN and Tracking Number are not copied to the clone.
	 * 
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws JDOMException
	 * @throws RepositoryException
	 * @throws PHRException 
	 */
	public CCRDocument copy() throws  ParseException, RepositoryException, PHRException {
		CCRDocument theCopy = new CCRDocument(this.storageId, this.guid, this.name,
				this.uri, this.trackingNumber, this.xml, new ArrayList(),
				SCHEMA_VALIDATION_OFF);
        theCopy.setStorageMode(this.getStorageMode());
        theCopy.setLogicalType(this.getLogicalType());
		return theCopy;
	}

	/**
	 * Attempts to parse the date from the XDS document. Because dates seem to
	 * come in slightly varying formats, it tries a couple of different things
	 * to parse the date. TODO Nasty,nasty HACK. Now truncating the string
	 * before parsing because of values like "2005-02-12T17:42:49-06:00Z" The
	 * dates were generated in the CCREditor.ccrFinalize() - it really needs to
	 * be fixed there too.
	 */
	public Date parseDate(String inputDate) throws ParseException {
        
        Matcher m = CCRElement.CCR_DATE_PATTERN_WITH_TIME_ZONE.matcher(inputDate);
        if(m.matches()) {
            log.info("Input date " + inputDate + " matches ISO pattern");
            String ccrDate = m.group(1) + m.group(2);
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(ccrDate);
            log.info("Parsed as " + d);
            return d;
        }
        
		if (inputDate.indexOf("Z") == -1)
			inputDate = inputDate + "Z";

		// Examplesof input
		// 2005-02-12T07:41:08.056Z (includes milliseconds)
		// 2005-02-12T19:12:50-06:00 (includes time zone offset)
		//  

		Date returnDate = null;

		try {
			String input = inputDate;
			log.debug("input=" + inputDate);
			if (inputDate.length() >= 20) {
				input = inputDate.substring(0, 19) + "Z";
			}
			log.debug("     =" + input);

			try {
				returnDate = ZULU_DATE_FORMAT.parse(input);
			} catch (Exception e) {
				log.warn("***Date format error for date " + inputDate + ":"
						+ e.toString());
				try {
					SimpleDateFormat zulu2 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm'Z'");
					zulu2.setTimeZone(TimeZone.getTimeZone("GMT"));
					returnDate = zulu2.parse(inputDate);
				} catch (ParseException e1) {
					SimpleDateFormat zulu3 = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					zulu3.setTimeZone(TimeZone.getTimeZone("GMT"));
					returnDate = zulu3.parse(inputDate);
				}
			}
		} catch (Exception e) {
			log.warn("***Date format error for date " + inputDate + ":"
					+ e.toString());
		}

		if (returnDate == null)
			returnDate = new Date(0);
		return (returnDate);
	}

	/**
	 * Returns a List containing Contact objects for all the contacts in this
	 * CCR (eg. Source, To, Patient etc.)
	 * 
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 * @throws PHRException 
	 */
	public List getContacts() throws PHRException {

		if (this.contacts == null) {
			this.contacts = new ArrayList();
			Document doc = this.getJDOMDocument();
			Iterator iter = doc.getDescendants(new ElementFilter("Person",
					namespace));
			while (iter.hasNext()) {
				CCRElement personElement = (CCRElement) iter.next();
				log.info("Found contact " + personElement.toString());
				Contact contact = new Contact(personElement);
				if ((contact.getGivenName() != null)
						|| (contact.getFamilyName() != null)) { // Do not add
					// contact with
					// no name
					contacts.add(contact);
				}
			}
		}

		return contacts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXml() {
        if(this.xml == null) {
            try {
                this.syncFromJDom();
            }
            catch (PHRException e) {
                throw new RuntimeException("Unable to create XML of CCR",e);
            }
        }
		return xml;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public List<MCSeries> getSeriesList() {
		return seriesList;
	}

	public void setSeriesList(List<MCSeries> seriesList) {
		this.seriesList = seriesList;
	}

	/**
	 * Returns the last calculated guid for this document.
	 * This guid is typically null if the document is not saved / fixed content
	 * and contains a value once it is saved.  However this is not guaranteed
	 * to be the case - there is no automatic mechanism that nulls out this
	 * value when a CCR gets modified.
	 */
	public String getGuid() {
		return guid;
	}

	public void setGuid(String orderGuid) {
		this.guid = orderGuid;
	}

	/**
	 * Returns JDOM instance of CCR; generates lazily if instance doesn't exist.
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public XMLPHRDocument getJDOMDocument() throws PHRException {
		
			try {
                if (jdomDocument == null) {
                    if(xmlLog.isDebugEnabled())
                        xmlLog.debug("Creating with xml " + this.xml);
                	long startTime = System.currentTimeMillis();
                	if ((CCRConstants.SCHEMA_VALIDATION_LENIENT.equals(schemaValiationSetting))
                			|| (CCRConstants.SCHEMA_VALIDATION_STRICT.equals(schemaValiationSetting))) {
                		this.jdomDocument = getValidatedJDCOMDocument();
                	}
                	else if (CCRConstants.SCHEMA_VALIDATION_OFF.equals(schemaValiationSetting)) {
                		this.jdomDocument = new CCRBuilder().build(new CCRByteArrayReader(this.xml.getBytes("UTF-8")));					
                	}
                	List list = this.jdomDocument.getRootElement().getAdditionalNamespaces();
                	Iterator iter = list.iterator();
                	int ccrNsCount = 0;
                	while(iter.hasNext()){
                		
                		Namespace aNs = (Namespace) iter.next();
                		String nsUri = aNs.getURI();
                		if (nsUri.equals(CCR_NAMESPACE_URN)){
                			ccrNsCount++;
                			log.info("CCR Namespace " + ccrNsCount + " detected:" + nsUri + ", prefix: " + aNs.getPrefix());
                		}
                	}
                	
                	namespace = this.jdomDocument.getRootElement().getNamespace();
                	String nsUri = namespace.getURI();
                	if (!nsUri.equals(CCR_NAMESPACE_URN))
                	log.debug("CCR document namespace is: prefix: " + namespace.getPrefix() + 
                			", uri:" + namespace.getURI());
                	
                	long endTime = System.currentTimeMillis();
                	log.debug("CCR Parsing time:" + (endTime - startTime) + "msec");
                }
            }
            catch (JDOMException e) {
                throw new PHRException(e);  
            }
            catch (IOException e) {
                throw new PHRException(e);  
            }
		
		return (XMLPHRDocument) jdomDocument;
	}
    
    /**
     * Returns the JDOM Root ContinuityOfCareRecord element
     * @throws IOException 
     * @throws JDOMException 
     */
    public CCRElement getRoot() throws PHRException {
        return (CCRElement) this.getJDOMDocument().getRootElement();
    }

	public Namespace getRootNamespace(){
		return(namespace);
	}
    
	public long getCreateTimeMs() throws ParseException, PHRException {

		if (this.createTimeMs == -1) {
			// Find the child DateTime that is the Creation time
			String dateTimeString = this.getJDOMDocument().getValue("ccrDateTime");
			if (dateTimeString != null) {
				this.createTimeMs = this.parseDate(dateTimeString).getTime();
			}
		}
		return createTimeMs;
	}
    
    public Date getCreateTime() throws ParseException, PHRException {
        return new Date(this.getCreateTimeMs());
    }

	public void setCreateTimeMs(long createTimeMs) throws PHRException {
		this.createTimeMs = createTimeMs;
        this.getRoot().getOrCreate("DateTime").setExactDateTime(createTimeMs);
	}

	public Contact getPatient() throws PHRException {

		// Get the patient contact info
		if (this.patient == null) {
			XMLPHRDocument doc = this.getJDOMDocument();
			CCRElement personElement = (CCRElement) doc.queryProperty("patientPerson");
			if (personElement != null) {
				this.patient = new Contact(personElement);
			} else {
				log.warn("Patient/Person element not found.");
			}
		}
		return patient;
	}

	public void setPatient(Contact patient) {
		this.patient = patient;
	}

	public String getTrackingNumber() {
		return this.trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	
	/**
	 * Loads the order attached to this CCRDocument from the database. This
	 * method relies on orderGuid being filled and will return null otherwise.
	 * 
	 * @return
	 */
	public MCOrder getOrder() throws HibernateException {
		if (true) throw new RuntimeException("Dead code!");
		// If order is loaded and consistent with guid, return it
		//if ((order != null) && order.getOrderGuid().equals(this.guid))
		//	return this.order;
/*
		try {
			Session session = HibernateUtil.currentSession();
			
			List orders = session.find("from MCOrder o where o.orderGuid='"
					+ this.guid + "'");
			if (!orders.isEmpty()) {
				this.order = (MCOrder) orders.get(0);
				this.setGuid(order.getOrderGuid());
			}
		} finally {
			HibernateUtil.closeSession();
		}
*/
		return this.order;
	}

	/**
	 * Merges the contact lists from all the given CCRs to create a single
	 * master list without overlapping names.
	 */
	public static List createMasterContactList(List ccrs) throws PHRException {
		HashMap results = new HashMap();
		for (Iterator iter = ccrs.iterator(); iter.hasNext();) {
			CCRDocument doc = (CCRDocument) iter.next();
			for (Iterator contactIter = doc.getContacts().iterator(); contactIter
					.hasNext();) {
				Contact contact = (Contact) contactIter.next();
				String key = contact.getGivenName() + contact.getFamilyName();
				if (results.get(key) != null) {
					Contact existing = (Contact) results.get(key);
					existing.getPhoneNumbers()
							.putAll(contact.getPhoneNumbers());
					existing.getEmails().putAll(contact.getEmails());
				} else
					results.put(key, contact);
			}
		}
		return new ArrayList(results.values());
	}

	/**
	 * Creates and loads a CCR from a default template stored as part of server
	 * configuration.
	 * 
	 * @throws PHRException 
	 */
	public static CCRDocument createFromTemplate(String storageId) throws JDOMException,
			IOException, ParseException, RepositoryException, NoSuchAlgorithmException, PHRException {
		log.info("createFromTemplate");
		String templatePath = "data/xds-templates/templateCcr1_0.xml";
		CCRDocument ccr = createFromTemplate(storageId,templatePath);
         
        // Generate the Patient Actor ID and the From Actor ID
        String patientActorID = ccr.generateObjectID();
        ccr.getPatientActor().createPath("ActorObjectID", patientActorID);
        ccr.getRoot().createPath("Patient/ActorID",patientActorID);
        String fromActorID = ccr.generateObjectID();
        ccr.getRoot().queryProperty("fromActor").createPath("ActorObjectID", fromActorID);
        ccr.getRoot().createPath("From/ActorLink/ActorID", fromActorID);
        return ccr;
	}

	/**
	 * Creates and loads a CCR from the given path.
	 * 
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @throws ParseException
	 * @throws RepositoryException
	 * @throws NoSuchAlgorithmException 
	 * @throws PHRException 
	 */
	public static CCRDocument createFromTemplate(String storageId, String templatePath)
			throws JDOMException, IOException, ParseException,
			RepositoryException, NoSuchAlgorithmException, PHRException {
		return(createFromTemplate(storageId, templatePath, SCHEMA_VALIDATION_STRICT));
		
	}
	
	public static CCRDocument createFromTemplate(String storageId, String templatePath, String schemaValiationSetting)
	throws JDOMException, IOException, ParseException,
		RepositoryException, NoSuchAlgorithmException, PHRException {
	    log.info("createFromTemplate:" + templatePath);
	    return createFromTemplate(storageId,templatePath,schemaValiationSetting,true);
	}
	
	public static CCRDocument createFromTemplate(String storageId, String templatePath, String schemaValiationSetting, boolean parseReferenceElements)
    throws JDOMException, IOException, ParseException,
        RepositoryException, NoSuchAlgorithmException, PHRException {
	    log.info("createFromTemplate:" + templatePath);
	    // Open the file as a stream and read it
	    FileInputStream inputStream = new FileInputStream(templatePath);
	    StringBuffer ccrXml = new StringBuffer();
	    byte[] buffer = new byte[4096];
	    int read = -1;
	    while ((read = inputStream.read(buffer)) >= 0) {
	        ccrXml.append(new String(buffer, 0, read));
	    }
	    inputStream.close();
	    
	    CCRDocument ccr = new CCRDocument(storageId, null, "New CCR",
	            "session://currentCcr", null, ccrXml.toString().trim(),
	            new ArrayList<MCSeries>(), schemaValiationSetting,parseReferenceElements);
	    
	    String docId = 
	        new GuidGenerator().generateGuid(String.valueOf(System.currentTimeMillis()+Thread.currentThread().getId()).getBytes("UTF-8"));         
	    
	    XPathUtils.setValue(ccr.getJDOMDocument(),"CCRDocumentObjectID", docId);         
	    
	    return ccr;
    }

	/**
	 * Saves the CCR into the specified location.
	 * 
	 * @param file
	 * @throws IOException
	 * @throws JDOMException
	 * @throws PHRException 
	 * @throws IOException 
	 */
	public void saveCCR(File file) throws PHRException, IOException {

		FileWriter f = new FileWriter(file);
		XMLOutputter out = new XMLOutputter(utfOutputFormat);
		out.output(this.getJDOMDocument(), f);
		f.flush();
		f.close();
	}

	/**
	 * Similar to the routine in InitializeWADOAction - hopefully this will
	 * replace it eventually.
	 * 
	 * @param request
	 * @param order
	 * @param seriesList
	 * @return
	 */
	public static CCRDocument createCCRDocumentFromOrder(MCOrder order,
			String storageId,
			List<MCSeries> seriesList, String notificationEmail)
			throws ParseException, JDOMException, IOException,
			ConfigurationException, RepositoryException, NoSuchAlgorithmException, PHRException {
	    
		log.info("createCCRDocumentFromOrder");
		CCRDocument currentCcr = CCRDocument.createFromTemplate(storageId, "data/xds-templates/templateDICOMCcr.xml");
		
		currentCcr.setGuid(order.getOrderGuid());

		Document newCcr = currentCcr.getJDOMDocument();
		//log.error("Loaded from template:\n" + currentCcr.dumpXML(newCcr.getRootElement()) + "\n");

		// Set the Patient Info in the CCR
		StringTokenizer tokens = new StringTokenizer(order.getPatientName());
		if (tokens.hasMoreTokens()) {
			XPathUtils.setValue(currentCcr.getJDOMDocument(),"patientGivenName", tokens.nextToken()); // xpath.getElement(newCcr,
			// "patientGivenName").setText(tokens.nextToken());
		}
		if (tokens.hasMoreTokens()) {
			XPathUtils.setValue(currentCcr.getJDOMDocument(),"patientFamilyName", tokens.nextToken()); // xpath.getElement(newCcr,
			// "patientFamilyName").setText(tokens.nextToken());
		}

		// Set the date of birth, if we can
		// TODO: Does ExactDateOfBirth make sense here? This is a change from
		// the earlier DOB
		// The CCR implentation guide suggests ExactDateTime when possible for
		// DOB.
		// But we typically don't have the information here -and a blank
		// <ExactDateTime> throws
		// an error in the AAFP validator. Commenting out for now.
		// xpath.getElement(newCcr,
		// "patientExactDateOfBirth").setText(order.getPatientDob());

		// Set the name
        XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
		
		xpath.getElement(newCcr, "sourceTitle").setText(
				Configuration.getProperty("DefaultAuthorTitle"));
		xpath.getElement(newCcr, "sourceGivenName").setText(
				Configuration.getProperty("DefaultAuthorGivenName"));
		xpath.getElement(newCcr, "sourceFamilyName").setText(
				Configuration.getProperty("DefaultAuthorFamilyName"));

		// Set the patient age
		// TODO: Note that DICOM ages can be years, months, days,or hours (the
		// latter for newborns).
		XPathUtils.setValue(currentCcr.getJDOMDocument(),"patientAge", order.getPatientAge());

		XPathUtils.setValue(currentCcr.getJDOMDocument(),"ccrDateTime", DICOMUtils.formatDate(System
				.currentTimeMillis()));
		XPathUtils.setValue(currentCcr.getJDOMDocument(),"patientDICOMId", order.getPatientId());

		// Set the Sex/Gender
		Element gender = xpath.getElement(currentCcr.getJDOMDocument(),
				"patientGender");
		assert gender != null : "gender element not found";
		if (gender != null) {
			if ("M".equals(order.getPatientSex())) {
				gender.setText("Male");
			} else if ("F".equals(order.getPatientSex())) {
				gender.setText("Female");
			} else {
				gender.setText("Unknown");
			}
		} else {
			log
					.warn("Template CCR has no gender element.  Treating gender as Unknown.");
			gender.setText("Unknown");
		}
		
		/*
		 * Element refs = xpath.getElement(newCcr, "references"); if (refs ==
		 * null){ refs = new Element("References", ccrns);
		 * newCcr.getRootElement().addContent(refs); //newCcr.addContent(refs); }
		 */

		Iterator iter = seriesList.iterator();
		while (iter.hasNext()) {

			MCSeries series = (MCSeries) iter.next();
			if (MCSeries.STATUS_CLOSED.equals(series.getSeriesStatus()))
				currentCcr.addReference(series);
			else
				log.info("Skipping series not yet fixed content:"
						+ series.getMcGUID() + " " + series.getSeriesDescription());

		}

		try {
			log.info("Notification email is:" + notificationEmail);
			if (notificationEmail != null){
				//log.error("Entire document:\n" + currentCcr.dumpXML( currentCcr.getJDOMDocument().getRootElement()) + "\n");
				currentCcr.setPrimaryNotificationEmail(notificationEmail);
			}
		} catch (MultipleElementException e) {
			log.error("Error adding notification to template CCR for "
					+ notificationEmail, e);
		}
		currentCcr.syncFromJDom();

		// Add the CCR to the series list so that it shows in WADO
		// TODO: What should the docId really be?
		// TODO: Should all of thie be removed? The series references should
		// already be in the CCR.
		String newCcrDocId = "mc-ccr-new-"
				+ (System.currentTimeMillis() % 10000);
		MCInstance instance = new MCInstance(
				"showReport.jsp?stylesheet=ccr2htm&source=currentCcr.xml",
				CCRDocument.CCR_MIME_TYPE, storageId, newCcrDocId);
		MCSeries series = new MCSeries("New CCR", storageId, newCcrDocId, instance);
		series.setMcGUID(newCcrDocId);
		seriesList.add(0, series);
		currentCcr.setSeriesList(seriesList);
		return currentCcr;
	}
    
	public static CCRDocument createCCRDocumentJDOM(String storageId, Document xmlDoc,
			List<MCSeries> seriesList, String notificationEmail)
			throws ParseException, JDOMException, IOException,
			ConfigurationException, RepositoryException, PHRException {
		log.info("createCCRDocumentFromOrder");
		
		StringWriter sw = new StringWriter();
		Format outFormat = Format.getPrettyFormat();
		outFormat.setEncoding("UTF-8");
		new XMLOutputter(outFormat).output(xmlDoc, sw);
		CCRDocument currentCcr = new CCRDocument(storageId, null, "New CCR",
				"session://currentCcr", null, sw.toString().trim(),
				new ArrayList(), SCHEMA_VALIDATION_OFF);
		
		//currentCcr.setGuid(order.getOrderGuid());

		//Document newCcr = currentCcr.getJDOMDocument();
		//log.error("Loaded from template:\n" + currentCcr.dumpXML(newCcr.getRootElement()) + "\n");

		

		
		/*
		 * Element refs = xpath.getElement(newCcr, "references"); if (refs ==
		 * null){ refs = new Element("References", ccrns);
		 * newCcr.getRootElement().addContent(refs); //newCcr.addContent(refs); }
		 */

		Iterator iter = seriesList.iterator();
		while (iter.hasNext()) {

			MCSeries series = (MCSeries) iter.next();
			if (MCSeries.STATUS_CLOSED.equals(series.getSeriesStatus()))
				currentCcr.addReference(series);
			else
				log.info("Skipping series not yet fixed content:"
						+ series.getMcGUID() + " " + series.getSeriesDescription());

		}

		try {
			log.info("Notification email is:" + notificationEmail);
			if (notificationEmail != null){
				//log.error("Entire document:\n" + currentCcr.dumpXML( currentCcr.getJDOMDocument().getRootElement()) + "\n");
				currentCcr.setPrimaryNotificationEmail(notificationEmail);
			}
		} catch (MultipleElementException e) {
			log.error("Error adding notification to template CCR for "
					+ notificationEmail, e);
		}
		currentCcr.syncFromJDom();

		// Add the CCR to the series list so that it shows in WADO
		// TODO: What should the docId really be?
		// TODO: Should all of thie be removed? The series references should
		// already be in the CCR.
		String newCcrDocId = "mc-ccr-new-" + (System.currentTimeMillis() % 10000);
		MCInstance instance = new MCInstance(
				"showReport.jsp?stylesheet=ccr2htm&source=currentCcr.xml",
				CCRDocument.CCR_MIME_TYPE, storageId, newCcrDocId);
		MCSeries series = new MCSeries("New CCR", storageId, newCcrDocId, instance);
		series.setMcGUID(newCcrDocId);
		seriesList.add(0, series);
		currentCcr.setSeriesList(seriesList);
		return currentCcr;
	}
    
    /**
     * Creates a series for the given reference and returns it.
     * <p>
     * <i>Note: this does not create XML / DOM elements on the encapsulated
     * JDOM document.  See {@link #addReferenceXML(MCSeries)}</i>
     * 
     * @param fileName
     * @param documentGuid
     * @param contentType
     * @return
     */
    public MCSeries createReferenceSeries(String fileName, String storageId, String documentGuid, String contentType) {
        // Create a new instance
        MCInstance instance =  
            new MCInstance(fileName, contentType, storageId, documentGuid);
        
        // Create a new series
        MCSeries series = new MCSeries(fileName, storageId, "", null);        
       
        // Add the instance to the series
        series.addInstance(instance);
        series.setMcGUID(documentGuid);
        instance.setMimeType(contentType);
        return series;
    }
    
    /**
     * Adds the given URL as a reference to this CCR
     * @param url
     * @throws IOException 
     * @throws JDOMException 
     * @throws PHRException 
     */
    public void addURLReference(String url) throws PHRException {
        try {
            // Load the reference from the template
            Document referenceDoc = CCRLoader.loadTemplate(TEMPLATE_PATH+"/referenceTemplate.xml");
            CCRElement reference = (CCRElement) referenceDoc.getRootElement();
            reference.getChild("ReferenceObjectID").setText(generateObjectID());
            Element references = getOrCreate((CCRElement)this.getJDOMDocument().getRootElement(), "References");        
            reference.queryProperty("referenceType").setText("URL");
            reference.queryProperty("referenceURL").setText(url);
            reference.queryProperty("referenceDisplayName").setText("Web Reference");        
            referenceDoc.removeContent(reference);
            reference.setNamespace(namespace);
            references.addContent(reference);
            
            // Create a new series 
            MCSeries series = new MCSeries(url, storageId, "", null);
            MCInstance instance = new MCInstance(url, "URL", storageId, null);
            series.addInstance(instance);
            this.getSeriesList().add(series);
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to add URL Reference",e);
        }
        catch (IOException e) {
            throw new PHRException("Unable to add URL Reference",e);
        }
    }

	/**
	 * Adds a reference (ie. an attached document) to this CCR.
	 * @return 
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 * @throws PHRException 
	 */
	public CCRReferenceElement addReference(MCSeries series) throws PHRException {
	    this.getSeriesList().add(series);
        return this.addReferenceXML(series);
    }
    
	/**
	 * Adds XML for the given reference to this DOM for this CCR
	 * @return 
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 * @throws PHRException 
	 */
    public CCRReferenceElement addReferenceXML(MCSeries series) throws PHRException {
        try {

            String displayName = series.getSeriesDescription();
            if ((displayName == null) || "".equals(displayName))
            	displayName = "DICOM " + series.getModality() + " Images";
            String type = series.getMimeType();
            String guid = series.getMcGUID(); 

            log.info("Added reference to ccr " + displayName + " , " + type
            		+ ", guid= " + guid);
            Document referenceDoc = CCRLoader
            		.loadTemplate(TEMPLATE_PATH+"/referenceTemplate.xml");
            
            CCRReferenceElement reference = (CCRReferenceElement) referenceDoc.getRootElement();
            reference.getChild("ReferenceObjectID").setText(generateObjectID());
            
            //log.error("Reference (before):" + dumpXML(reference));
            CCRElement references = this.getRoot().getOrCreate("References");

            reference.queryProperty("referenceType").setText(type);
            reference.queryProperty("referenceURL").setText("mcid://" + guid);
            reference.queryProperty("referenceDisplayName").setText(displayName);
            
            // If the series is validated, add the attribute to the reference
            if(!series.isValidationRequired()) {
                addValidationAttribute(reference);
            }
            
            referenceDoc.removeContent(reference);
            references.addContent(reference);
            return reference;
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to add reference for series " + series.getMcGUID(),e);
        }
        catch (IOException e) {
            throw new PHRException("Unable to add reference for series " + series.getMcGUID(),e);
        }
	}
    
    /**
     * Finds the given series in the CCR XML and adds the attribute
     * to it to indicate that it requires confirmation by the user.
     * 
     * @param series
     * @throws PHRException 
     */
    public void addConfirmationRequiredFlag(MCSeries series) throws PHRException {
        series.setValidationRequired(true);
        CCRReferenceElement reference = this.findSeriesReferenceElement(series);
        assert reference != null : "Reference for existing series not found";        
        CCRElement confirmed = reference.queryProperty("referenceConfirmed");
        if(confirmed == null) {
	        this.addValidationAttribute(reference);
        }
        else {
	        confirmed.setText("true");
        }
    }
    
    /**
     * Finds the given series in the CCR XML and removes the attribute
     * to it to indicate that it requires confirmation by the user.
     * 
     * @param series
     * @throws PHRException 
     */
    public void removeConfirmationFlag(MCSeries series) throws PHRException {
        series.setValidationRequired(false);
        CCRElement reference = this.findSeriesReferenceElement(series);
        assert reference != null : "Reference for existing series not found";
        this.removeConfirmationFlag(reference);
        
    }
    
    private void removeConfirmationFlag(CCRElement reference) throws PHRException {        
        if(log.isInfoEnabled())
            log.info("Removing confirmation flag on CCR reference " + reference.queryTextProperty("referenceURL"));
        Element confirmed = reference.queryProperty("referenceConfirmed");
        if(confirmed == null) {
            log.warn("Unable to remove reference confirmation attribute: confirmation flag not found");
            return;
        }        
        confirmed.setText("false");
    }

    /**
     * Searches through the references in the XML to find the element
     * that matches the given series.
     * 
     * This is done by finding the series with the same guid that is
     * at the corresponding index for all series with that guid in 
     * the current series list.
     * 
     * @param series
     * @return
     * @throws IOException 
     * @throws JDOMException 
     * @throws PHRException 
     */
    public CCRReferenceElement findSeriesReferenceElement(MCSeries series) throws PHRException {
        
        CCRElement refs = this.getJDOMDocument().queryProperty("references");        
        if (refs == null) {
            return null;
        } 
        
        Iterator seriesIter = this.getSeriesList().iterator();
        MCSeries matchSeries = null;
        
        // Try to find the guid
		for (Iterator iter = refs.getDescendants(new ElementFilter("Reference",namespace)); iter.hasNext();) {
			CCRReferenceElement ref = (CCRReferenceElement) iter.next();
            String guid = ref.getGuid();
            if(guid == null) {
                continue;
            }
            
            if(series.getMcGUID().equals(guid)) {
                // Find the next series with this guid
                while(seriesIter.hasNext()) {
                    matchSeries = (MCSeries) seriesIter.next();
                    if(series.getMcGUID().equals(matchSeries.getMcGUID()))
                        break;
                }
                if(matchSeries == series)
                    return (CCRReferenceElement)ref;
            }
        }
        return null;
    }

    /**
     * Adds an ObjectAttribute to the given reference indicating that
     * it requires validation.
     * 
     * @param reference
     */
    private void addValidationAttribute(CCRReferenceElement reference) {
        reference.addAttribute("ConfirmationRequired", "true", "ConfirmationStatus", Version.getVersionString());
    }
    
    

	public static class MetaDataParser extends DefaultHandler {

		public String metadataStr = null;
		/*
		 * public void startElement(String ns, String elementName, String qName,
		 * Attributes atts) throws SAXException {
		 * if("property".equals(elementName)) {
		 * if("medcommons".equals(atts.getValue("name"))) { metadataStr =
		 * atts.getValue("value"); } } }
		 */
	};

	/**
	 * Returns MCSeries objects for each DICOM reference in the CCR.
	 * 
	 * @param guid
	 * @return
	 */
	private MCSeries getDICOMSeriesReference(CCRElement ref, String storageId, String guid) throws RepositoryException {
		try {
            List<DicomMetadata> manifestObjects = localFileRepository.loadMetadatas(storageId, guid);
            DicomMetadata firstObj = manifestObjects.get(0);
            MCSeries series = new MCSeries();
            series.setMcGUID(guid);
            series.setStorageId(storageId);
            series.setModality(firstObj.getModality());
            
            // Use CCR displayName here.
            String displayName = getReferenceDisplayName(ref);
            displayName = firstObj.getDisplayName();
            series.setSeriesDescription(displayName);
            series.setSeriesInstanceUID(firstObj.getSeriesInstanceUid());
            series.setSeriesNumber(firstObj.getSeriesNumber());
            Date seriesDate = firstObj.getSeriesDate();
            if (seriesDate == null){
            	
            	seriesDate = firstObj.getStudyDate();
            	log.info("Null series date in CCR " + guid + ", replacing with study date " +
            			seriesDate);
            }
            try{
            	series.setSeriesDate(DICOMUtils.formatDate(seriesDate));// TODO: HACK. 
            }
            catch(Exception e){
            	// Note: there may have been a different parsing error other than the 
            	// seriesDate being null.
            	log.error("Error parsing series date for storage id " + storageId + ", guid "  + guid 
            			+ " date= "+ firstObj.getSeriesDate(), e);
            	try{
            		series.setSeriesDate(DICOMUtils.formatDate(firstObj.getStudyDate()));
            	}
            	catch(Exception e2){
            		log.error("Error parsing study date (as series date) for storage id " + storageId + ", guid " + guid
            				+ " date= "+ firstObj.getStudyDate(), e);
            	}
            }
            // Need to sort the list. Currently this is in order
            // by the attribute displayOrder - whose default value is
            // the DICOM InstanceNumber. But if the metadata file
            // is edited with new values the order will change.
            Collections.sort(manifestObjects, new Comparator<DicomMetadata>() {
                public int compare(DicomMetadata o1, DicomMetadata o2) {
                    int order1 = o1.getDisplayOrder();
                    int order2 = o2.getDisplayOrder();
                    if (order1==order2) return(0);
                    else if (order1 > order2) return(1);
                    else return(-1);
                }});
            Iterator<DicomMetadata> iter = manifestObjects.iterator();
            while(iter.hasNext()){
            	DicomMetadata instanceMetadata = iter.next();
            	MCInstance instance = new MCInstance(instanceMetadata.getDocumentName(), 
            			DocumentTypes.DICOM_MIME_TYPE, storageId, guid );
            	instance.setInstanceNumber(instanceMetadata.getInstanceNumber());
            	instance.setNumFrames(instanceMetadata.getFrames());
            	
            	instance.setWindow(instanceMetadata.getWindowWidth());
            	instance.setLevel(instanceMetadata.getWindowCenter());
            	instance.setSOPInstanceUID(instanceMetadata.getSopInstanceUid());
            	instance.setReferencedFileID(instanceMetadata.getDocumentName());
            	instance.setSeriesInstanceUID(instanceMetadata.getSeriesInstanceUid());
            	instance.setStudyInstanceUID(instanceMetadata.getStudyInstanceUid());
            	series.addInstance(instance);
            	
            }
            
            // Check for any presets that may be stored in the CCR as object attributes
            parseWindowLevel(ref, series);
            
           
            return (series);
        }
        catch (FileNotFoundException e) {
            throw new RepositoryException("Unable to resolve series meta data for guid"+guid,e);
        }
        catch (IOException e) {
            throw new RepositoryException("Unable to resolve series meta data for guid"+guid,e);
        }
        catch (JDOMException e) {
            throw new RepositoryException("Unable to resolve series meta data for guid "+guid,e);
        }
        catch (PHRException e) {
            throw new RepositoryException("Unable to query window level presets for guid " + guid,e);
        }
	}

	void parseWindowLevel(CCRElement ref, MCSeries series) throws PHRException {
        CCRElement presets = ref.queryProperty("referencePresets");
        if(presets != null) {
            Iterator<CCRElement> presetIter = presets.getDescendants(new ElementFilter("ObjectAttribute"));
            while(presetIter.hasNext()) {
                WindowLevelPreset p = new WindowLevelPreset(); 
                CCRElement att = presetIter.next();
                p.setName(att.getChildText("Attribute"));
                String [] windowLevel = att.getChild("AttributeValue").getChildText("Value").split(",");
                p.setWindow(Integer.parseInt(windowLevel[0]));
                p.setLevel(Integer.parseInt(windowLevel[1]));
                series.getPresets().add(p);
            }
            
        }
    }

	// Probably obsolete
	/*
	 * private Element getMedCommonsMetadata(Document jdomDocument) { for
	 * (Iterator iter = jdomDocument.getDescendants(new
	 * ElementFilter("property")); iter.hasNext();) { Element property =
	 * (Element) iter.next();
	 * 
	 * if (property != null) { log.info("property: " + property.getName());
	 * String name = property.getAttributeValue("name");
	 * 
	 * if (WebDAVConstants.MEDCOMMONS_METADATA_PROPERTYNAME.equals(name)) return
	 * (property); } } return (null); }
	 */

	public boolean isValidated() {
        // The CCR is validated if all of the attachments are validated
        for (MCSeries series : this.getSeriesList()) {
            if(series.isValidationRequired()) {
                return false;
            }
        }
		return true;
	}

	public void setValidated(boolean isValidated) {
        boolean first = true;
        for (MCSeries series : this.getSeriesList()) {
            if(!first) {
                series.setValidationRequired(!isValidated);
            }
            else
                first = false;
        }
	}

	/**
	 * Generates a unique object id for this document.
	 * 
	 * @return - a new, unique object id
	 * @throws PHRException 
	 */
	public String generateObjectID() throws PHRException {
	    return this.getRoot().generateObjectID();
	}

	/**
	 * Sets the MedCommons Comment field, creating it if it does not already
	 * exist.
	 * 
	 * @param message
	 * @throws JDOMException
	 * @throws IOException
	 * @throws PHRException 
	 */
	public void setMedCommonsComment(String message) throws PHRException {
	    
		// See if a medcommons comment field exists, if so populate it
		PHRElement purposeText = this.getRoot().queryProperty("purposeText");
		if (purposeText == null) {
			purposeText = this.getRoot().createPurposeText("User Comment");
		}
		purposeText.setElementValue(message);
	}

	public String getPurposeText() throws PHRException{
		Element purposeText = this.getJDOMDocument().queryProperty("ccrPurpose");
		if (purposeText == null){
			log.info("Purpose is null");
			return(null);
		}
			else{
			//log.info("Purpose is " + dumpXML(purposeText));
			//log.info("getText = " + purposeText.getText());
			//log.info("getValue = " + purposeText.getValue());
			
			return(purposeText.getText());
		}
			
	}


    public CCRElement addComment(String text) throws PHRException {
        CCRElement element = el("Comment");
        element.createPath("CommentObjectID", generateObjectID());
        element.createPath("DateTime/ExactDateTime", CCRElement.getCurrentTime());
        element.createPath("Type/Text", "User Comment");
        element.createPath("Description/Text", text);
        element.createPath("Source/ActorID", this.getJDOMDocument().getValue("patientActorID"));
        ((CCRElement) getJDOMDocument().getRootElement()).getOrCreate("Comments").addContent(element); 
        return element;
    }
	
	public CCRElement createPath(String path) throws PHRException {
        return (CCRElement) this.getRoot().createProperty(path); 
	}

	/**
	 * Finds or creates the Patient "Person" element and returns it.
	 * @throws InvalidCCRException 
	 * @throws PHRException 
	 */
	private CCRElement getPatientPerson() throws InvalidCCRException, PHRException {
        CCRElement patientActor = this.getPatientActor();
        if(patientActor == null) {
            throw new InvalidCCRException("Unable to locate Patient Actor.  Patient Actor is required by CCR Standard.");
        }
        CCRElement patientPerson = patientActor.getOrCreate("Person"); 
        //getOrCreate(patientActor, "Person", new String[]{ "Source" });
		return patientPerson;
	}

	/**
	 * Finds and returns the patient Actor for this CCR
	 * @throws PHRException 
	 */
	public CCRActorElement getPatientActor() throws PHRException {
		CCRActorElement patientActor = (CCRActorElement) this.getJDOMDocument().queryProperty("patientActor");
		return patientActor;
	}
	/**
	 * 
	 * 
 <IDs>
<Type>
	<Text>SSN</Text>
</Type>
<ID>888-88-8888</ID>
 <Source>
	<Actor>
				<ActorID>AA0002</ActorID>
			</Actor>
		</Source>
	</IDs>
	
	<IDs>
				<Type>
					<Text>DICOM Patient Id</Text>
				</Type>
				<ID/>
				<Source>
					<Actor>
						<ActorID>AA0002</ActorID>
					</Actor>
				</Source>
			</IDs>
	 * @param fromNode
	 * @param idType
	 * @return
	 */
	private Element findTypedID(Element fromNode, String idType){
		Element found = null;
		Iterator ids = fromNode.getDescendants(new ElementFilter(
				"IDs", namespace));
		while(ids.hasNext()){
			Object obj = ids.next();
			if (obj instanceof Element){
				Element element = (Element) obj;
				Element eType = element.getChild("Type");
				
			}
		}
		return(found);
	}
/*
	public static CCRElement getOrCreate(CCRElement parent, String name, String after) {
       return parent.getOrCreate(parent,name, new String[] { after }); 
    }
    */
	/**
	 * Attempts to retrieve a child element of the given name from the parent.
	 * If the child is not found, creates it.
	 * 
	 * @param parent -
	 *            element to retrieve/create child from/in
	 * @param name -
	 *            name of child element (assumed to be in CCR namespace)
	 *            ###### Assumed to be in the namespace of the parent?
	 * @return
	 */
	public static CCRElement getOrCreate(CCRElement parent, String name) {
		CCRElement child = parent.getChild(name);
		if (child == null) {
			child = CCRElementFactory.instance.element(name, parent.getNamespace());
			parent.addChild(child);   
		}
		return child;
	}    
	
	public static Element create(Element parent, String name){
		Element child  = new Element(name, parent.getNamespace());
		parent.addContent(child);
		return(child);
	}
    
    /**
     * Retrieves the given field, creating it if it does not already exist, while
     * keeping order consistent with the given array of element names.  Any elements 
     * prior in the array are ensured to be prior in the result, any elements 
     * after are ensured to be after in the result.
     * 
     * @param parent
     * @param name
     * @param afterName
     * @return
     */
	public static Element createAfter(Element parent, String name, String afterName[]){
		Element child = null;
		
		int index = -1;
		int i = 0;
		
		child = new Element(name, parent.getNamespace());
		log.debug("createAfter:New child created for " + name);
		
		List parentContents = parent.getContent();
		Iterator iter =parentContents.iterator();
		while (iter.hasNext()){
			Object obj = iter.next();
			if (obj instanceof Element){
				Element element = (Element) obj;
				log.debug("createAfter:Found element " + element.getName());
			}
			else
				log.debug("createAfter:Non-element value:" + obj.getClass().getCanonicalName());
		}
		
		Element insertAfter = null;
		for (i=0;i<afterName.length;i++){
			insertAfter = parent.getChild(afterName[i], parent.getNamespace());
			
            log.debug("createAfter:Insert after is " + insertAfter);
            
            // If we found the name of element to be inserted, break here
            if(afterName[i].equals(name))
                break;
            
			if (insertAfter != null){
				index = parent.indexOf(insertAfter);
				log.debug("createAfter: found match with " + insertAfter.getName());
			}
		}
		
		if (index == -1){
			//log.error("Appending " + name + " at end of " + parent.getName());
			parent.addContent(child);
		}
		else{
			//log.error("Inserting " + name + " after index :" + index + "(" + afterName[i] + ")");
			parent.addContent(index+1, child);
		}
		return(child);
	}
	/*
	public static CCRElement getOrCreate(CCRElement parent, String name, String afterName[]) {
		CCRElement child = parent.getChild(name);
		log.info("getOrCreate " + name);
		int index = -1;
		if (child == null) {
			child = el(name, parent.getNamespace());
			log.debug("New child created for " + name);
			
			List parentContents = parent.getContent();
			Iterator iter =parentContents.iterator();
            if(log.isDebugEnabled()){
                while (iter.hasNext()){
                    Object obj = iter.next();
                    if (obj instanceof Element){
                        Element element = (Element) obj;
                        log.debug("Found element " + element.getName());
                    }
                    else
                        log.debug("Non-element value:" + obj.getClass().getCanonicalName());
                }
            }
			
			List<CCRElement> insertAfter = null;
			for (int i=0;i<afterName.length;i++){
                if(afterName[i].equals(name))
                    break;
                
				//insertAfter = parent.getChild(afterName[i], parent.getNamespace());
                insertAfter = parent.getChildren(afterName[i], parent.getNamespace());
				//log.info("!!!Insert after is " + insertAfter);
				if ((insertAfter != null) &&(insertAfter.size() > 0)){
					int insertIndex = -1;
					for (int ii=0;ii<insertAfter.size(); ii++){
						
						int iIndex = parent.indexOf(insertAfter.get(ii));
						
						if (iIndex > insertIndex){
							insertIndex = iIndex;
							
						}
							
					}
					
					
					
                    if(insertIndex >= index)
                        index = insertIndex;
				}
			}
            
            if (index == -1)
                parent.addContent(0,child);
            else
                parent.addContent(index+1, child);
		}
		else {
			log.debug("child " + name + " already exists..");
		}
		return(child);
	}
		
		*/

	/**
	 * Creates a default MedCommons actor. If the Actors node does not exist it
	 * will be created.
	 * 
	 * @param type - one of To or From
	 * @throws JDOMException
	 * @throws IOException
     * @return the Actor element created. (note, not the link, the actual Actor)
	 * @throws PHRException 
	 */
	public CCRElement createDefaultActor(String type) throws PHRException {
        CCRElement root = (CCRElement)this.getJDOMDocument().getRootElement();
        CCRElement actor = root.getChild(type);
		if (actor == null) {
			actor = CCRElementFactory.instance.element(type, namespace);
			root.addChild(actor);
		}
		return this.createActorForLink(actor);
	}

	/**
	 * Creates an Actor initialized with default values corresponding to a
	 * MedCommons actor.
	 * 
	 * @param actorLinkParent
	 * @throws JDOMException
	 * @throws IOException
	 * @throws PHRException 
	 */
	private CCRElement createActorForLink(CCRElement actorLinkParent) throws PHRException {
        CCRElement root = (CCRElement)this.getJDOMDocument().getRootElement();

        CCRElement actor = this.createActor();
        
		getOrCreate(getOrCreate(actorLinkParent, "ActorLink"), "ActorID")
				.setText(actor.getChildText("ActorObjectID",namespace));

        /*
		// Now create actual actor
		Document actorDoc = xpath.loadTemplate(TEMPLATE_PATH
				+ "/actorTemplate.xml");
		Element actor = actorDoc.getRootElement();
		actor.setNamespace(actorLinkParent.getNamespace());
		actorDoc.removeContent(actor);
		
		Element actors = getOrCreate(root, "Actors");
		
		actors.addContent(actor);
		actor.getChild("ActorObjectID", namespace).setText(actorId);
		actor.getChild("Source", namespace).getChild("Actor", namespace).getChild(
				"ActorID", namespace).setText(actorId);
                */
        
        return actor;
		//log.error("Actors are:" + dumpXML(actors));
	}
    
    public CCRElement createActor() throws PHRException {
        
        try {
            // Allocate an actor ID
            String actorId = generateObjectID();
            CCRElement root = (CCRElement)this.getRoot();
            Document actorDoc = CCRLoader.loadTemplate(TEMPLATE_PATH + "/actorTemplate.xml");
            CCRElement actor = (CCRElement) actorDoc.getRootElement();
            actor.setNamespace(this.getNamespace());
            actorDoc.removeContent(actor);
            
            CCRElement actors = getOrCreate(root, "Actors");
            
            actors.addContent(actor);
            actor.getChild("ActorObjectID", namespace).setText(actorId);
            actor.getChild("Source", namespace).getChild("Actor", namespace).getChild(
                    "ActorID", namespace).setText(actorId);
            
            return actor;
        }
        catch (JDOMException e) {
            throw new PHRException("Unable to create new actor",e);
        }
        catch (IOException e) {
            throw new PHRException("Unable to create new actor",e);
        }        
    }

	/**
	 * Adds a new ActorLink to the existing actorLinkParent passed as a
	 * parameter.
	 * 
	 * @param actorLinkParent
	 * @param actorId
	 */
	public void addActorLink(Element actorLinkParent, String actorId) {
		CCRElement actorLink = el("ActorLink");
		actorLinkParent.addContent(actorLink.addContent(el("ActorID").setText(actorId)));
	}

	/**
	 * Searches for the first From actorLink that links to an actor with an
	 * email address and returns the ActorLink element.
	 * 
	 * @return
	 * @throws PHRException 
	 */
	public CCRElement getPrimaryFromActor() throws PHRException {
        CCRElement root = (CCRElement)this.getJDOMDocument().getRootElement();

		// Get the from node, if it exists
		CCRElement fromNode = (CCRElement) root.getChild("From", namespace);

		if (fromNode == null)
			return null;

		// For each actorlink
		for (Iterator iter = fromNode.getDescendants(new ElementFilter("ActorLink", namespace)); iter.hasNext();) {
            CCRElement actorLink = (CCRElement) iter.next();
			// Does this actor have an email address
			String actorObjId = actorLink.getChildText("ActorID", namespace);
			if (actorObjId != null) {
				CCRElement emailElement = 
                    this.getJDOMDocument().queryProperty("emailFromActorID",  new String[]{ "actorId",actorObjId });
				if (emailElement != null) {
					if (!Str.blank(emailElement.getText())) {
						return actorLink;
					}
				}
			}
		}

		return null;
	}

	public String getSubjectText() {
		return subjectText;
	}

	public void setSubjectText(String subjectText) {
		this.subjectText = subjectText;
	}

	public static void setTemplatePath(String templatePath) {
		TEMPLATE_PATH = templatePath;
	}

	/**
	 * Finds the primary notification Email for this CCR
	 * 
	 * @param allowMultiNonMc -
	 *            set this to true if it is allowable to return a non-MedCommons
	 *            To where there are multiple such to choose from.
	 * @throws CCRMultipleElementException
	 * @throws IOException
	 * @throws JDOMException
	 * @throws PHRException 
	 */
	public Element getPrimaryNotificationEmail(boolean allowMultiNonMc) throws PHRException {
		Element toEmail = null;
		XPathCache xpaths = Configuration.getBean("ccrXPathCache");
		try {
            List firstMcToEmail = (List) xpaths.getXPathResults(getJDOMDocument(),"firstMcToEmail");
            if (firstMcToEmail != null && firstMcToEmail.size() >= 1) {
            	log.info("Found medcommons email. Non-MedCommons emails will be ingored");
            	toEmail = (Element) (firstMcToEmail.get(0));
            } else { // no MedCommons email. Use the first email /only/ if it is
            	// the only one available
            	List nonMcEmails = (List) xpaths.getXPathResults(getJDOMDocument(),"nonMcToEmails");
            	if ((nonMcEmails != null) && !nonMcEmails.isEmpty()) {
            		if (nonMcEmails.size() == 1) {
            			toEmail = (Element) nonMcEmails.get(0);
            			log.info("Found single non-MedCommons Email " + toEmail);
            		} else {
            			if (allowMultiNonMc && (nonMcEmails.size() >= 1)) {
            				toEmail = (Element) nonMcEmails.get(0);
            				log.info("Found multiple non-MedCommons emails.  Returning first.");
            			} 
            			else
            				log.info("Found multiple non-MedCommons emails.  Ignoring all.");
            		}
            	}
            }

            return toEmail;
        }
        catch (JDOMException e) {
            throw new PHRException(e);
        }
	}

	/*
	 * in <To>
	 * 
	 * <ActorLink> <ActorID>AA3413956485</ActorID> <ActorRole>
	 * <Text>Notification</Text> </ActorRole> </ActorLink>
	 * 
	 * <Actor> <ActorObjectID>AA3413956485</ActorObjectID> <InformationSystem>
	 * <Name>MedCommons Notification</Name> <Type>Repository</Type>
	 * <Version>0.9.10</Version> </InformationSystem> <EMail>
	 * <Value>sdoyle@gmail.com</Value> </EMail> <Source> <Actor>
	 * <ActorID>AA3413956485</ActorID> </Actor> </Source> </Actor>
	 */
	/**
	 * This should only be invoked when creating from a known template.
	 * @throws PHRException 
	 * @throws IOException 
	 */
	private void setPrimaryNotificationEmail(String toEmailNotification)
			throws PHRException, IOException {
		Element mcTo = getPrimaryNotificationEmail(false);
		if (mcTo == null) {
			createDefaultActor("To");
			XPathUtils.setValue(this.getJDOMDocument(),"toEmail", toEmailNotification);
			mcTo = getPrimaryNotificationEmail(false);
			StringWriter sw = new StringWriter();
			new XMLOutputter(utfOutputFormat).output(mcTo, sw);
			//log.error("New notification actor is: " + sw.toString());
		} else {
			StringWriter sw = new StringWriter();
			new XMLOutputter(utfOutputFormat).output(mcTo, sw);
			//log.error("Current notification actor is: " + sw.toString());

		}
	}

	/**
	 * Finds the primary notification Email for this CCR
	 * 
	 * @throws CCRMultipleElementException
	 * @throws PHRException 
	 */
	public List<String> getNotificationEmails() throws PHRException {
		XPathCache xpaths = Configuration.getBean("ccrXPathCache");
		
		List<String> results = new ArrayList<String>();
		try {
            List firstMcToEmail = xpaths.getXPathResults(getJDOMDocument(),"mcToEmails");
            if(firstMcToEmail != null && firstMcToEmail.size() >= 1) {
            	log.info("Found medcommons email. Non-MedCommons emails will be ignored");
            	// TODO Should this rule stay in? This seems like a demo thing that should be removed.
            	for (Iterator iter = firstMcToEmail.iterator(); iter.hasNext();) {
            		Element element = (Element) iter.next();
            		results.add(element.getText());
            	}
            } else { // no MedCommons email. Use the first email /only/ if it is
            	// the only one available
            	Element toEmail = null; 
            	List nonMcEmails = xpaths.getXPathResults(getJDOMDocument(),"nonMcToEmails");
            	if ((nonMcEmails != null) && !nonMcEmails.isEmpty()) {
            		if (nonMcEmails.size() == 1) {
            			toEmail = (Element) nonMcEmails.get(0);
            			log.info("Found single non-MedCommons Email " + toEmail);
            		} else {
            			log
            					.info("Found multiple non-MedCommons emails.  Ignoring all.");
            		}
            	}

            	if (toEmail != null)
            		results.add(toEmail.getText());
            }

            return results;
        }
        catch (JDOMException e) {
            throw new PHRException(e);
        }
	}

	/**
	 * Probes the given bytes to see if they are a CCR. If they appear to be a
	 * CCR, returns true, otherwise throws an exception.
	 * 
	 * @param fileData
	 * @return
	 * @throws InvalidCCRException 
	 */
	public static boolean isCcr(byte[] fileData) throws InvalidCCRException {

		try {
			Document doc = new CCRBuilder().build(new ByteArrayInputStream(fileData));

			if ("ContinuityOfCareRecord".equals(doc.getRootElement().getName())) {
				if (CCR_NAMESPACE_URN.equals(
						doc.getRootElement().getNamespaceURI())) {
					return true;
				}
                else
                    throw new InvalidCCRException("CCR has invalid namespace " + doc.getRootElement().getNamespaceURI());
			}
            else
	            throw new InvalidCCRException("CCR has incorrect node at root level");

		} 
        catch (Exception e) {
			//log.warn("Exception while probing content type for uploaded document: " + e.getMessage(), e);
            throw new InvalidCCRException(e.getMessage());
		}

	}

	public HashMap<String, String> getAttributions() {
		return attributions;
	}

	public void setAttributions(HashMap<String, String> attributions) {
		this.attributions = attributions;
	}

	/**
	 * Clears attributions for the current CCR. <i>This is a hack, implemented
	 * as a getter so that it is easy to clear attributions from JSP</i>
	 * 
	 * @return
	 */
	public boolean getClearAttributions() {
		if (this.attributions != null)
			this.attributions.clear();

		return true;
	}

	public static char[] read(Reader reader) throws IOException {
		char[] chunk;
		char[] all;
		int len;
		List chunks;
		int i;
		int last;

		chunks = new ArrayList();
		do {
			chunk = new char[CHUNK];
			chunks.add(chunk);
			len = read(reader, chunk);
		} while (len == CHUNK);
		last = chunks.size() - 1;
		all = new char[last * CHUNK + len];
		for (i = 0; i <= last; i++) {
			System.arraycopy(chunks.get(i), 0, all, CHUNK * i,
					(i == last) ? len : CHUNK);
		}
		return all;
	}

	/**
	 * * * Reads until EOF or the buffer is filled. * *
	 * 
	 * @return chars actually read; != buffer.length for EOF
	 */
	private static int read(Reader dest, char[] buffer) throws IOException {
		int ofs;
		int len;

		for (ofs = 0; ofs < buffer.length; ofs += len) {
			len = dest.read(buffer, ofs, buffer.length - ofs);
			if (len == -1) {
				break;
			}
		}
		return ofs;
	}

	public boolean getSchemaValidationFailure(){
		return(schemaValidationFailure);
	}
	
	public String getSchemaValidationMessages(){
		return(schemaValiationMessages);
	}
	
	/**
	 * Parses CCR with schema validation. Uses object pool for storing parsers.
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	
	public Document getValidatedJDCOMDocument() throws JDOMException,
			IOException {

		SAXBuilder builder = null;
		try {
			builder = (SAXBuilder) validatingParserPool.borrowObject();
		} catch (Exception e) {

			throw new JDOMException("Validating Parser Pool Exception", e);
		}
		try {
			
            builder.setFactory(new CCRElementFactory());
			this.jdomDocument = builder.build(new CCRByteArrayReader(this.xml.getBytes("UTF-8")));
			
			CCRParseErrorHandler errorHandler = (CCRParseErrorHandler) builder
					.getErrorHandler();
			// Not sure if warnings should be handled the same as errors here.
			if (errorHandler.getParseWarnings()
					|| errorHandler.getParseErrors()
					|| errorHandler.getParseFatal()) {
				schemaValidationFailure = true;
				StringBuffer buff = new StringBuffer();
				if (errorHandler.getParseWarnings()) {
					buff.append(errorHandler.getWarningMessages());
				}
				if (errorHandler.getParseErrors()) {
					buff.append(errorHandler.getErrorMessages());
				}
				if (errorHandler.getParseFatal()) {
					buff.append(errorHandler.getFatalMessages());
				}
				schemaValiationMessages = buff.toString();
				
				
				if (CCRConstants.SCHEMA_VALIDATION_STRICT
						.equals(this.schemaValiationSetting)){
					throw new CCRParseException(schemaValiationMessages);
				}

				// Even if LENIENT, a fatal error still needs to be thrown.
				if (errorHandler.getParseFatal()) {
					throw new CCRParseException("Fatal errors:"
							+ errorHandler.getFatalMessages());
				}
				
				

			}
			
		}
		catch(Exception e){
			throw new JDOMException("Error parsing CCR:", e);
		}
		
		finally {
			try {
				validatingParserPool.returnObject(builder);
			} catch (Exception e) {
				throw new JDOMException(
						"Validating parser pool return exception", e);
			}
		}
		return (this.jdomDocument);
	}
		
    public boolean isNewCcr() {
        return isNewCcr;
    }

    public void setNewCcr(boolean isNewCcr) {
        this.isNewCcr = isNewCcr;
        if(isNewCcr)
            this.storageMode = StorageMode.SCRATCH;
    } 

    /**
     * Calculates the GUID (SHA-1 hash) of the current version of the CCR XML.
     * 
     * @return
     */
    public String calculateGuid(){
        String calculatedGuid = null;
        try {
            syncFromJDom();    	
            String strDocument = getXml();
            byte[] byteDocument = strDocument.getBytes("UTF-8");
            SHA1 sha1 = new SHA1();
            sha1.initializeHashStreamCalculation();
            
            calculatedGuid = sha1.calculateByteHash(byteDocument);
        }
        catch(Exception e){
            log.error("Error calculating guid of CCR", e);
        }
        return(calculatedGuid);
    }
    
    /**
     * Parses the date of birth from the CCR and returns it.
     * <p>
     * This function uses a heuristic to find a date to display.  It will try and parse
     * a date from the CCR's ExactDateTime and if one can not be parsed, it will
     * try to parse one from the ApproximateDateTime.   If neither can be parsed
     * then it will return null.
     * <p>
     * <em>ssadedin - changed 7/21/2008 to return null if date unparseable.  Previously returned today's date</em>
     * 
     * @return the parsed date or null
     */
    public Date getPatientDateOfBirth() throws PHRException {
        XMLPHRDocument doc = this.getJDOMDocument();
        for(String path : asList("patientExactDateOfBirth","patientApproxDateOfBirth")) {
            CCRElement dobElement = doc.queryProperty(path);
            if(dobElement == null)
                return null;
            
            Calendar result = DateFormats.parse(DateFormats.all(), dobElement.getTextTrim());
            if(result != null)
                return result.getTime();
        }
        // Not found
        return null;
    }
    
    /**
     * Attempts to find one of exact date of birth or approx date of birth
     * and returns the first non-null entry respectively.  If both are null
     * returns null.
     * @throws PHRException 
     */
    public String getDisplayPatientDateOfBirth() throws PHRException {
        String result = XPathUtils.getValue(this.getJDOMDocument(),"patientExactDateOfBirth");
        if(!Str.blank(result)) {
            return result;
        }
        return XPathUtils.getValue(this.getJDOMDocument(),"patientApproxDateOfBirth");
    }
    
    /**
     * Sets the patient Date of Birth.  An attempt is made to parse the
     * given string as a date.  If the string can be parsed as a proper date
     * then it is translated to Zulu time and entered into the ExactDateTime
     * field of the CCR.
     * 
     * However if it cannot be parsed as a date, it is simply entered into the
     * ApproximateDateTime field.
     * 
     * @param dob
     * @throws IOException 
     * @throws JDOMException 
     * @throws InvalidCCRException 
     * @throws PHRException 
     */
    public void setPatientDateOfBirth(String dob) throws InvalidCCRException, PHRException {        
        CCRElement personElement = getPatientPerson(); // xpath.getElement(this.getJDOMDocument(), "patientPerson");
        
        // HACK: if Unknown, just remove the whole DateOfBirth element
        if(dob == "Unknown") {
            personElement.removeChild("DateOfBirth", namespace);
            return;
        }
        
        // If already set, don't do anything
        if(dob.equals(this.getDisplayPatientDateOfBirth()))
            return;
            
        CCRElement dobElement = this.getOrCreate(personElement,"DateOfBirth");        
        dobElement.setDate(dob);
    }

    public JSONObject getEmergencyInfo() throws PHRException {
        // Create JSON object
        JSONObject einfo = new JSONObject();
        String [] vals = { 
                        "patientGivenName",
                        "patientMiddleName",
                        "patientFamilyName",
                        "patientAddress1",
                        "patientCity",
                        "patientState",
                        "patientPostalCode",
                        "patientApproxDateOfBirth",
                        "patientExactDateOfBirth",
                        "patientEmail",
                        "patientAge",
                        "patientPhoneNumber",
                        "patientCountry",
                        "patientGender"
        };
        for (int i = 0; i < vals.length; i++) {
          String name = String.valueOf(vals[i].charAt(7)).toLowerCase() + vals[i].substring(8);
          einfo.put(name, this.getJDOMDocument().getValue(vals[i]));
        }
                
        return einfo;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }
    
    public CCRElement el(String name) {
        return CCRElementFactory.instance.element(name);
    }

    /**
     * Adds an ID of given Type and Value to the Patient Actor of this CCR
     * 
     * @param patientId
     * @param patientIdType
     * @throws JDOMException
     * @throws IOException
     */
    public void addPatientId(String patientId, String patientIdType) throws PHRException {
        CCRElement patientActor = this.getJDOMDocument().queryProperty("patientActor");
        if(patientActor == null) {
            throw new PHRException("Unable to locate Patient Actor.  CCR appears to be invalid.");
        }
        CCRElement IDs = el("IDs");
        IDs.addContent(el("Type").addContent(el("Text").setText(patientIdType)));
        IDs.addContent(el("ID").setText(patientId));
        IDs.addContent(el("Source").addContent(
                        el("Actor").addContent(
                            el("ActorID").setText(XPathUtils.getValue(this.getJDOMDocument(),"patientActorID")))));

        patientActor.addChild(IDs);
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public Document getChangeHistory() {
        return this.changeHistory;
    }


    /**
     * Attempts to find the Reference element for the given guid
     * @throws PHRException 
     */
    public CCRElement findReference(String findGuid) throws PHRException {
        Element refs = getRoot().getChild("References");
        if(refs == null)
            return null;
        
        CCRElement found = null;
        for(Iterator iter = refs.getDescendants(new ElementFilter("Reference",this.getNamespace())); iter.hasNext();) {
          CCRElement ref = (CCRElement) iter.next();
          
          String url = ref.queryTextProperty("referenceURL"); 
          if (Str.blank(url)) {// No URL - not valid, ignore this entry
              continue;
          }
          Element refType = ref.getChild("Type", this.getNamespace());
          String type = (refType != null) ? refType.getChildTextTrim("Text",this.getNamespace()) : null;
          if (!"URL".equals(type) && !url.startsWith("mcid://")) {// bad format url
              continue;
          }
          
          String guid = url.substring(7);
          if("URL".equals(type) || guid.equals(findGuid)) {
              found = ref;
              break;
          }
      }
      return found;
    }
    /**
     * Attempts to remove the given guid as a reference.  Returns true if successful.
     * @throws PHRException 
     */
    public CCRElement removeReference(String removeGuid) throws PHRException {
        Element refs = getRoot().getChild("References");
        CCRElement found = findReference(removeGuid);
        refs.removeContent(found);
        if(refs.getChildren().isEmpty())
            getRoot().removeChild("References");
        return found;
    }

    public String getChangeHistoryGuid() {
        return changeHistoryGuid;
    }
    
    /**
     * Convenience method to get value for given path in CCR
     * 
     * @param path
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws MultipleElementException
     */
    public String getValue(String path) throws PHRException, MultipleElementException {
        return XPathUtils.getSingleValue(this.getJDOMDocument(), path);
    }
    public String getDocumentType(){
    	return(this.documentType);
    }
	public void setDocumentType(String documentType){
		this.documentType = documentType;
	}
	
	public String getPatientGivenName()throws PHRException {
		return(XPathUtils.getValue(getJDOMDocument(),"patientGivenName"));
	}
	public String getPatientFamilyName()throws PHRException {
		return(XPathUtils.getValue(getJDOMDocument(),"patientFamilyName"));
	}
	public String getPatientMedCommonsId()throws PHRException {
		return(XPathUtils.getValue(getJDOMDocument(),"patientMedCommonsId"));
	}
	public String getPatientGender()throws PHRException {
		return(XPathUtils.getValue(getJDOMDocument(),"patientGender"));
	}
	public String getPatientAge()throws PHRException{
		return(XPathUtils.getValue(getJDOMDocument(),"patientAge"));
	}
	public String getPatientEmail()throws PHRException {
		return(XPathUtils.getValue(getJDOMDocument(),"patientEmail"));
		
	}
	public String getToEmail()throws PHRException {
		return(XPathUtils.getValue(getJDOMDocument(),"toEmail"));  
	}
		    
    public String getDocumentPurpose()throws PHRException {
    	return(XPathUtils.getValue(getJDOMDocument(),"ccrPurpose"));
    }
    
    public String getDocumentPurposeText()throws PHRException {
    	return(XPathUtils.getValue(getJDOMDocument(),"purposeText"));
    }
    
    public void setStorageId(String storageId){
    	this.storageId = storageId;
        // ssadedin:  have been running into bugs because the storage id of instances and series get out
        // of sync with the storage id of the main document.  To try and avoid that 
        // the code below sets them for all the child series.   Because of this kind of issue
        // I'm uncertain about whether the storage id should really be cascaded to every series / instance, 
        // or whether it should be something that is computed at the last minute before storage: 
        // 
        // (for example:  LocalFileRepository.getRepository( <storageId> ) .putDocument(...) )
        //
        for (MCSeries series : this.seriesList) {
            series.setStorageId(storageId);
        }
    }
    
    public String getStorageId(){
    	return(this.storageId);
    }
   
    /**
     * In the case of the CCR - this is CCRDocumentObjectID
     * @param documentObjectId
     */
    public void setDocumentObjectId(String documentObjectId) throws PHRException {
        this.getJDOMDocument().setValue("CCRDocumentObjectID", documentObjectId);
    }

    public long getTotalLoadedSizeBytes() {
        return totalLoadedSizeBytes;
    }

    /**
     * Creates a list of elements based on the properties specified in the given
     * map.   Each property will only be used if it is prefixed by the given prefix,
     * which must be followed by an array index to specify where in the list the 
     * element should be created.  The remaining parts of the property will be
     * converted to XPath form and used to create the hierarchy implied.
     * 
     * For example:
     * 
     *   foo[3].bar.fubar=fu
     *   
     * Will create an array with 3 elements, the third having form "Name/Bar/Fubar" containing 
     * text "fu".
     * 
     * Some special handling is added based on naming conventions:
     * <ul>
     *   <li> Fields ending in DateTime will be treated as dates and will be probed to determine
     *     whether they are exact or approximate and the appropriate structure created.
     *     
     *   <li> Fields called "comment" will create a Comment section in the CCR Footer and instead a 
     *     reference by CommentID will be created rather than a literal "Comment" element
     * </ul>
     * 
     * @param params
     * @return
     * @throws PHRException 
     */
    public List<CCRElement> create(String prefix, String name, Map params) throws PHRException {
        List<CCRElement> result = new ArrayList<CCRElement>();
        update(prefix,name,params,result);
        return result;
    }
    
    /**
     * Creates or updates elements in the provided list according to properties
     * specified in the given parameter map.  See {@link create} for the expected
     * format of properties in the map and the meaning of the "prefix"
     */
    public void update(String prefix, String name, Map params, List<CCRElement> result) throws PHRException {
        for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
            String property = iter.next();
            String [] values = (String[]) params.get(property);
            
            if(!property.startsWith(prefix))
                continue;
            
            // check if the name matches expected pattern
            String [] parts = PROPERTY_PART_REGEX.split(property);                
            if((parts.length>0) && (parts[0].matches("^.*\\[[0-9]*\\]$"))) {
                Matcher m = INDEX_PATTERN.matcher(parts[0]);
                if(m.find()) {
                    int index = Integer.parseInt(m.group(1));
                    log.debug("Found param with " + parts.length + " pieces with index " + index);                    
    
                    while(index>=result.size())
                        result.add(null);
                    
                    CCRElement element = (CCRElement)result.get(index);
                    if(element==null) {
                        element = el(name);
                        element.createObjectID();
                        result.set(index, element);
                    }
                    log.info("Setting path " + property + " on element " + element.toXml());
                    
                    for(int i=1; i<parts.length;++i) {
                        String partName = Character.toUpperCase(parts[i].charAt(0))+parts[i].substring(1);                        
                        element = element.getOrCreate(partName);
                    }
                    
                    // Special handling for Comment fields - they have to be handled as references
                    if(element.getName().equals("Comment")) {
                        // HACK:  do not add a comment if it is "Type Here" - this is the default text placed in the field
                        CCRElement parent = (CCRElement)element.getParent();
                        parent.removeContent(element);
                        if(!Str.blank(values[0]) && !"Type Here".equals(values[0])) {
                            parent.createPath("CommentID",
                                            this.addComment(values[0]).getChildText("CommentObjectID",element.getNamespace()));                        
                        }
                    }
                    else                    
                    if(DATE_TIME_REGEX.matcher(element.getName()).find(0)) {
                        element.setDate(values[0]);
                    }
                    else
                        element.setText(values[0]);
                }
            } 
            log.info("Created element " + ((CCRElement)result.get(result.size()-1)).toXml());
        }        
    }
    
    public String getActorsJSON() throws PHRException {
        CCRElement actors = getRoot().getChild("Actors");
        if(actors == null)
            return "[]";
        
        StringBuilder result = new StringBuilder();
        for (ListIterator<CCRElement> iterator = actors.getChildren("Actor", actors.getNamespace()).listIterator(); iterator.hasNext();) {
            CCRElement a = iterator.next();
            
            String email = "";
            CCRElement emailNode = a.getChild("EMail");
            if(emailNode != null ) 
                email = emailNode.getChildText("Value");                
            
            CCRElement p = a.getChild("Person");
            
            if(result.length()!=0) 
                result.append(",\n");
            
            
            // Only consider actors who are people for now
            if(p == null) {
                result.append(
                  String.format("{ actorObjectID: '%s', medCommonsId: '',  givenName: '', middleName: '', familyName: '', dateOfBirth: '', email: '%s'}",
                                  a.getChildTextTrim("ActorObjectID"), escapeForJavaScript(email)));
                continue;
            }
            
            result.append(
            String.format("{ actorObjectID: '%s', medCommonsId: '%s',  givenName: '%s', middleName: '%s', familyName: '%s', dateOfBirth: '%s', email: '%s' }",
                            a.getChildTextTrim("ActorObjectID"),
                            escapeForJavaScript(nvl(p.queryTextProperty("personMedCommonsId"),"")),
                            escapeForJavaScript(nvl(p.queryTextProperty("personGivenName"),"")),
                            escapeForJavaScript(nvl(p.queryTextProperty("personMiddleName"),"")),
                            escapeForJavaScript(nvl(p.queryTextProperty("personFamilyName"),"")),
                            escapeForJavaScript(nvl(p.queryTextProperty("personMedCommonsId"),"")),
                            email
                         ));            
        }
        
        return String.format("[%s]", result.toString());
    }
    
    /**
     * Returns the References section of the CCR as a CCRElement.  
     * This is primarily used by JSP
     */
    public CCRElement getReferences() throws PHRException {
        return getRoot().getChild("References");
        
    }
    
    public CCRElement getFrom() throws PHRException {
        return this.getRoot().getChild("From");
    }

    public boolean isProvisionalCcr() {
        return provisionalCcr;
    }

    public void setProvisionalCcr(boolean provisionalCcr) {
        this.provisionalCcr = provisionalCcr;
    }

    public StorageMode getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(StorageMode storageMode) {
        this.storageMode = storageMode;
    }

    public String getAccessPin() {
        return accessPin;
    }

    public void setAccessPin(String accessPin) {
        this.accessPin = accessPin;
    }

    public AccountDocumentType getLogicalType() {
        return logicalType;
    }

    public void setLogicalType(AccountDocumentType logicalType) {
        this.logicalType = logicalType;
    }

    public String getLoadedFromGuid() {
        return loadedFromGuid;
    }

    public void setLoadedFromGuid(String loadedFromGuid) {
        this.loadedFromGuid = loadedFromGuid;
    }

    public EventManager<CCRDocument> getEvents() {
        return events;
    }

    /**
     * Sets the content of the given element with ActorLinks corresponding to the given emails.
     * <p>
     * The given from value may be a comma separated list of email addresses.  For each email address,
     * if there is an actor in the CCR with matching email address then an ActorLink tot he existing 
     * actor will be created.  If there is no corresponding actor then a new actor will be created with
     * the email address as their email.
     * @throws PHRException 
     */
    public void setActorLinks(CCRElement parent, String emails) throws PHRException {
        String[] addresses = COMMA_SPLITTER.split(emails);
        parent.removeContent();
        for(String email : addresses) {
            CCRElement actor = getJDOMDocument().queryProperty("actorFromEmail", new String[]{"email",email});
            if(actor == null) {
                actor = this.createActor();
                actor.createPath("EMail/Value",email);
            }
            
            CCRElement actorLink = el("ActorLink");
            actorLink.createPath("ActorID", actor.getChildTextTrim("ActorObjectID"));
            parent.addChild(actorLink);
        }
    }
    
    /**
     * Returns a list of the references attached to this CCR in the order
     * that they should be displayed and ignoring non-display items such as the
     * self-series that actually refers to the primary CCR itself.
     * @return
     */
    public List<MCSeries> getDisplayReferences() {
        ArrayList<MCSeries> result = new ArrayList<MCSeries>();
        for(int i=this.seriesList.size()-1; i>=0; --i) {
            if(i != 0) {
                result.add(this.seriesList.get(i));
            }
        }
        return result;
    }
}
