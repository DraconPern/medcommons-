package net.medcommons.fax.http;

import static net.medcommons.modules.utils.Str.blank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import net.medcommons.conversion.Base64Utility;
import net.medcommons.document.CCRParseErrorHandler;
import net.medcommons.document.CCRParseException;
import net.medcommons.document.ValidatingParserFactory;
import net.medcommons.document.ccr.ImportAttachmentException;
import net.medcommons.document.ccr.ImportFileAttachment;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.crypto.PIN;
import net.medcommons.modules.services.interfaces.ActivityEventType;
import net.medcommons.modules.services.interfaces.BillingCharge;
import net.medcommons.modules.services.interfaces.BillingEvent;
import net.medcommons.modules.services.interfaces.BillingEventType;
import net.medcommons.modules.services.interfaces.CoverInfo;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.ccr.CCRStoreException;
import net.medcommons.router.services.ccr.StorageModel;
import net.medcommons.router.services.ccr.StoreTransaction;
import net.medcommons.router.services.dicom.util.MCSeries;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.repository.RepositoryFactory;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;

/**
 * Handler for extracting fax pages from XML payload sent by DataOnCall and 
 * other providers which use the same format.
 * 
 * @author ssadedin
 */
public class DataOnCallServlet extends HttpServlet {

    /**
     * XPath to pull actual file contents from XML payload.
     * <p>
     * Note we allow any top level element because different providers
     * can use the same format but have different root elememnts.
     */
    private static final String FILE_CONTENTS_XPATH = "/*/FaxControl/FileContents";

    /**
     * XPath to pull page count from XML payload.
     * <p>
     * Note we allow any top level element because different providers
     * can use the same format but have different root elememnts.
     */
    private static final String PAGE_COUNT_XPATH = "/*/FaxControl/PageCount";

    /**
     * Barcode XPath - pulls fax key from XML payload
     * <p>
     * Note we allow any top level element because different providers
     * can use the same format but have different root elememnts.
     */
    private static final String BARCODE_KEY_XPATH = "/*/FaxControl/BarcodeControl/Barcodes/Barcode/Key";

    private ThreadLocal<Transformer> transformer = new ThreadLocal<Transformer>();

    private static String MEDCOMMONS = "MC";

    private static String DEFAULT_PIN = "11111";

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DataOnCallServlet.class);

    protected void doGet(HttpServletRequest request,
                    HttpServletResponse response) throws ServletException, IOException {

        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Extract the XML from the DataOnCall request
        String xml = getXML(request);

        // Generate a CCR
        try {
            Document xmlDocument = getUnvalidatedDocument(xml);

            // Get the page count
            int pageCount = getPageCount(xmlDocument);

            // Get extended information about the cover sheet
            // stored on the account server
            CoverInfo coverInfo = getCoverInfo(xmlDocument);

            log.info("Fax Account id is " + coverInfo.accountId);

            UserSession d = new UserSession(coverInfo.accountId, "Gateway");
            
            // Check billing information
            List<String> accounts = new ArrayList<String>();
            accounts.add(coverInfo.accountId);
            BillingEvent billingEvent = new BillingEvent(BillingEventType.INBOUND_FAX);
            billingEvent.setQuantity(pageCount);
            List<BillingCharge> charges = d.getServicesFactory().getBillingService().resolvePayer(accounts, Collections.singleton(billingEvent));
            
            // If there is no credit we quarantine the fax rather than rejecting it
            // This allows the user to view it if they pay up
            boolean quarantineFax = false;
            if(charges.isEmpty() && Configuration.getProperty("EnableBilling",false)) {
                quarantineFax = true;
                log.info("Insufficient credit available to accept inbound fax based on accounts " + Str.join(accounts,",") + ".  Fax will be quarantined.");
            }
            
            BillingCharge charge = charges.isEmpty() ? null : charges.get(0);
            
            // Convert whatever attachments are in the document into a series list
            List<MCSeries> seriesList = extractAttachments(xmlDocument, d, coverInfo, quarantineFax ? billingEvent : null);

            // Get a CCR from the fax to store
            CCRDocument ccr = getFaxCCR(d, xmlDocument, coverInfo, seriesList);

            // Store the CCR
            storeCCR(coverInfo, d, ccr, charge);

            // Send the notification email
            if(!blank(coverInfo.accountId))
                d.getServicesFactory().getNotifierService().sendFaxNotification(coverInfo.accountId, coverInfo.notification, ccr.getTrackingNumber(), "", "Inbound Fax Received");

            response.getOutputStream().print("Post Successful");
        }
        catch (JDOMException ex) {
            log.error("Problem generating attachment:", ex);
        }
        catch (NoSuchAlgorithmException ex) {
            log.error("Problem processing content", ex);
        }
        catch (ServiceException ex) {
            log.error("Unable to register billing event for transaction", ex);
            throw new ServletException("Unable to register billing event for transaction", ex);
        }
        catch (PHRException ex) {
            log.error("Problem processing content", ex);
        }
        catch (CCRStoreException ex) {
            log.error("Unable to store CCR for inbound fax",ex);
            throw new ServletException("Unable to register billing event for transaction", ex);
        }
        catch (FaxToCCRConversionException ex) {
            throw new ServletException("Unable to register billing event for transaction", ex);
        }
    }

    /**
     * Store the given CCR with appropriate activity log / CCR log events written.
     */
    private void storeCCR(CoverInfo coverInfo, UserSession d, CCRDocument ccr, BillingCharge charge) throws CCRStoreException, ServiceException,
                    PHRException {
        // Start a transaction
        StoreTransaction tx = d.tx(ccr);
        
        String pin = coverInfo.pin;
        if(blank(pin)) { // Do we know the unencrypted pin?
            // In this case the PIN is already hashed, so set a dummy hasher
            // to prevent it being double-hashed
            pin = coverInfo.encryptedPin;
            tx.setPinHasher(new PIN.Hasher() {
		            public String hash(String input) throws NoSuchAlgorithmException {
	                return input;
	            }
	        });
        }
        
        tx.registerDocument(pin);
        tx.storeDocument();
        if(d.hasAccount()) {
            StorageModel storageModel = Configuration.getBean("systemStorageModel");
            storageModel.replaceCurrentCCR(coverInfo.accountId, ccr);
        }
        
        if(charge != null) {
            d.getServicesFactory().getBillingService().charge(charge);
        }
            
        tx.writeActivity(ActivityEventType.ACOUNT_DOCUMENT_ADDED, "Inbound Fax", charge);
    }

    /**
     * Attempts to either create or locate a CCR to which the given fax can be added 
     * as a reference.  If no account exists for the CCR or if the account specified
     * does not have a Current CCR then a CCR will be generated from a default template.
     * <p>
     * If however an account exists and it has a Current CCR, the Current CCR will
     * be retrieved and the fax PDF will be added to it as a reference.
     * 
     * @param d
     * @param xmlDocument
     * @param coverInfo
     * @return
     * @throws FaxToCCRConversionException 
     */
    private CCRDocument getFaxCCR(UserSession d, Document xmlDocument, CoverInfo coverInfo, List<MCSeries> seriesList) throws FaxToCCRConversionException  {
        try {
            // Create a CCR from the series list
            Document ccrDocument = dataOnCallToCCR(xmlDocument);
            CCRDocument ccr = null;
            if(d.hasAccount()) {
                String guid = d.getAccountSettings().getCurrentCcrGuid();
                if(!blank(guid)) {
                    ccr = (CCRDocument) RepositoryFactory.getLocalRepository().queryDocument(d.getOwnerMedCommonsId(), guid);
                    if(ccr != null) {
                        // Add the incoming CCR as an attachment
                        for(MCSeries s : seriesList) {
                            ccr.addReference(s);
                        }
                    }
                }
            } 
            
            // Did we manage to find an existing CCR to attach to?
            if(ccr == null) { // no existing CCR
                ccr = generateCCR(coverInfo, seriesList, ccrDocument);
            }
            ccr.setCreateTimeMs(System.currentTimeMillis());
            return ccr;
        }
        catch (JDOMException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
        catch (IOException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
        catch (ConfigurationException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
        catch (TransformerException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
        catch (ServiceException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
        catch (PHRException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
        catch (ParseException e) {
            throw new FaxToCCRConversionException("Unable to create or acquire CCR with fax reference for cover sheet", e);
        }
    }

    private CoverInfo getCoverInfo(Document xmlDocument) throws JDOMException, NoSuchAlgorithmException {
        // Extract the coverId (key into central table) that will tell us the account, email and other details
        Long coverId = parseCoverId(xmlDocument);
        
        log.info("Extracted cover id = " + coverId + " from received fax");
        
        CoverInfo coverInfo = null;
        ServicesFactory factory = Configuration.getBean("systemServicesFactory");
        if(coverId != null) {
            try {
                coverInfo = factory.getAccountService().queryCoverInfo(coverId);
            }
            catch (ServiceException e1) {
                log.error("Unable to retrieve cover info for cover id " + coverId, e1);
            }                
        }
        
        if(coverInfo == null){
            coverInfo = new CoverInfo();
            coverInfo.accountId = ServiceConstants.PUBLIC_MEDCOMMONS_ID;
            coverInfo.encryptedPin = PIN.hash(DEFAULT_PIN);
            coverInfo.providerCode = null;
         }
        return coverInfo;
    }

    private String getXML(HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        String xml = null;
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String[] value = request.getParameterValues(key);
            if ("xml".equals(key)) {
                xml = value[0];
                log.info("XML value length: " + xml.length());
            } 
            else {
                log.debug(" Other (ignored) URL parameter:" + key);
                log.debug("Value is " + value[0]);
            }
        }
        return xml;
    }

    private int getPageCount(Document xmlDocument) throws JDOMException {
        Element pageCountElement = (Element)XPath.selectSingleNode(xmlDocument,PAGE_COUNT_XPATH);
        int pageCount = 1;
        if(pageCountElement != null) {
            pageCount = Integer.parseInt(pageCountElement.getTextTrim());
        }
        return pageCount;
    }

    private CCRDocument generateCCR(CoverInfo coverInfo, List<MCSeries> seriesList, Document ccrDocument)
                    throws ParseException, JDOMException, IOException, ConfigurationException, RepositoryException,
                    PHRException {
        CCRDocument ccr;
        ccr = CCRDocument.createCCRDocumentJDOM(coverInfo.accountId, ccrDocument, seriesList, null);            
        
        // Add notification emails
        if(coverInfo.notification!=null) {
            ccr.createPath("toEmail");
            ccr.getJDOMDocument().setValue("toEmail", coverInfo.notification);
        }
        
        // Set the account id as the patient account id
        ccr.getJDOMDocument().setValue("patientMedCommonsId", coverInfo.accountId);
        return ccr;
    }

    /**
     * Attempts to locate and extract the attached PDF from the XML
     * and convert it to a MedCommons style series list. 
     * 
     * @param xmlDocument
     * @param coverInfo
     * @param evt  billing event to register against extracted documents, if any
     * @return list of series created from embedded attachments
     * @throws FaxToCCRConversionException 
     */
    private List<MCSeries> extractAttachments(Document xmlDocument, UserSession d, CoverInfo coverInfo, BillingEvent evt) throws FaxToCCRConversionException {
        try {
            Element attachmentElement = (Element) XPath.selectSingleNode(xmlDocument, FILE_CONTENTS_XPATH);
            String contents = attachmentElement.getText();
                    
            //log.info("attachment contents is \n'" + contents + "'");
            log.info("Length of contents is " + contents.length());

            byte[] unencodedContents = Base64Utility.decode(contents);
            ImportFileAttachment importFile = new ImportFileAttachment(d.getServicesFactory());

            MCSeries attachment = importFile.exec(d.getOwnerMedCommonsId(), unencodedContents, coverInfo.title, CCRDocument.PDF_MIME_TYPE, evt);
            List<MCSeries> seriesList = new ArrayList<MCSeries>();
            log.info("description:" + attachment.getSeriesDescription() + ", "
                            + attachment.getMcGUID());
            seriesList.add(0, attachment);
            return seriesList;
        }
        catch (JDOMException e) {
            throw new FaxToCCRConversionException("Failed to extract attachments for inbound CCR", e);
        }
        catch (IOException e) {
            throw new FaxToCCRConversionException("Failed to extract attachments for inbound CCR", e);
        }
        catch (ServiceException e) {
            throw new FaxToCCRConversionException("Failed to extract attachments for inbound CCR", e);
        }
        catch (ImportAttachmentException e) {
            throw new FaxToCCRConversionException("Failed to extract attachments for inbound CCR", e);
        }
    }

    /**
     * Attempts to parse the cover id from the key element embedded in the document.
     */
    private Long parseCoverId(Document xmlDocument) throws JDOMException {
        Long coverId = null;
        Element keyElement = (Element) XPath.selectSingleNode(xmlDocument, BARCODE_KEY_XPATH);
        if (keyElement != null) {
            String key = keyElement.getTextTrim();
            log.info("Received fax with code = [" + key+"]");
            if (key.startsWith(MEDCOMMONS)) {
                key = key.substring(MEDCOMMONS.length());
                String [] tokens = key.split("/");
                if(tokens.length >= 2) {
                    // Query information from central for cover page
                    try {
                        coverId = Long.parseLong(tokens[1]);
                    }
                    catch (NumberFormatException e1) {
                        log.error("Incorrect format for cover id " + tokens[1], e1);
                    }
                }
                else
                    log.error("Invalid bar code key format " + key);
            } 
            else
                log.error("Unknown MedCommonsID format:" + key);
        }
        else {
            log.info("No key code found for document");
        }
        return coverId;
    }

    /**
     * @param xml
     * @throws IOException
     */
    private void dumpXMLtoFile(String xml) throws IOException {
        File temp = new File("data/temp/faxes.txt");
        if (!temp.exists())
            temp.mkdirs();
        FileWriter fw = new FileWriter(new File("data/temp/incoming_fax"
                        + System.currentTimeMillis() + ".xml"));
        fw.write(xml);
        fw.close();
    }

    private Document dataOnCallToCCR(Document xmlDocument)
    throws TransformerException {
        JDOMSource in = new JDOMSource(xmlDocument);
        JDOMResult out = new JDOMResult();
        this.getTransformer().transform(in, out);
        return (out.getDocument());

    }

    private Document getUnvalidatedDocument(String xml) throws JDOMException,
    IOException {
        SAXBuilder builder = new SAXBuilder();
        StringReader reader = new StringReader(xml);
        Document jdomDocument = null;
        jdomDocument = builder.build(reader);
        return (jdomDocument);
    }

    /**
     * Return a transformer for converting DataOnCall XML to CCR XML
     * <p>Note: because transformers are not thread safe, this method
     * caches  them on a per-thread basis.  Thus you can safely use the object returned
     * without worrying about MT access from other threads.</p>
     */
    private Transformer getTransformer() throws TransformerConfigurationException, TransformerFactoryConfigurationError {
        if(this.transformer.get()==null) {
            this.transformer.set(TransformerFactory.newInstance().newTransformer(
                            new StreamSource(new File(
                            "data/stylesheets/DataOnCallToCCR.xsl"))));
        }
        return this.transformer.get();
    }
}
