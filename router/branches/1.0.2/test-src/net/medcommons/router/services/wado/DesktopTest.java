/*
 * $Id$
 * Created on 26/09/2005
 */
package net.medcommons.router.services.wado;

import static net.medcommons.document.ccr.CCRConstants.CCR_NAMESPACE_URN;
import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.modules.services.interfaces.AccountCreationService;
import net.medcommons.modules.services.interfaces.AccountService;
import net.medcommons.modules.services.interfaces.ActivityLogService;
import net.medcommons.modules.services.interfaces.DirectoryService;
import net.medcommons.modules.services.interfaces.DocumentRegistration;
import net.medcommons.modules.services.interfaces.DocumentService;
import net.medcommons.modules.services.interfaces.HipaaService;
import net.medcommons.modules.services.interfaces.NotifierService;
import net.medcommons.modules.services.interfaces.SecondaryRegistryService;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.modules.services.interfaces.TrackingAccessConstraint;
import net.medcommons.modules.services.interfaces.TrackingReference;
import net.medcommons.modules.services.interfaces.TrackingService;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.BaseTestCase;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.Namespace;


public class DesktopTest extends BaseTestCase implements ServicesFactory, NotifierService, TrackingService {
    
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(DesktopTest.class);
	
    private UserSession desktop = null;
    
    private Set<String> notifications = new HashSet<String>();
    
    private GuidGenerator guidGenerator = null;
    
    private String ccrXml = null;
    
    private CCRDocument ccr = null;
    
    private Namespace ccrns = Namespace.getNamespace(CCR_NAMESPACE_URN);

    private CCRElement to;
    private CCRElement from;
    
    public static void main(String[] args) {
    }

    public DesktopTest(String arg0) throws NoSuchAlgorithmException, Exception {
        super(arg0);
        this.guidGenerator = new GuidGenerator();
        FileInputStream in = new FileInputStream("test-src/net/medcommons/services/rest/testCcr.xml");
        byte [] buffer = new byte[20000];
        int read = in.read(buffer);
        this.ccrXml = new String(buffer,0,read);        
        CCRDocument.setTemplatePath("etc/static-files/xds-templates");
    }

    protected void setUp() throws Exception {
        super.setUp();        
        notifications.clear();
        this.ccr = getCcr();
        to = this.ccr.getRoot().addChild(el("To"));
        from = this.ccr.getRoot().getChild("From");
        ArrayList<CCRDocument> ccrs = new ArrayList<CCRDocument>();
        ccrs.add(ccr);
        this.desktop = new UserSession("0000000000000000", null, ccrs);
        this.desktop.setServicesFactory(this);
        this.desktop.setAccessPin("00000");
    }
    
    public void testNobody() throws Exception {
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        
        // Should have blank To and From
        assertEquals("",reply.getValue("toEmail"));
        assertEquals(0, notifications.size());
    }
     
    public void test1MC() throws Exception {
        ccr.addActorLink(to, "MC2");
        ccr.addActorLink(from, "MC1");
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("mc1@medcommons.org", reply.getValue("firstMcToEmail"));
        assertEquals("mc2@medcommons.org", reply.getValue("sourceEmail"));
    }
    
    public void test1MC1Other() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "MC1");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        
        assertEquals("Should have blank to because no From in original CCR", "", reply.getValue("firstMcToEmail"));
        assertEquals("mc1@medcommons.org", reply.getValue("sourceEmail"));
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
    }

    public void test2MC() throws Exception {
        ccr.addActorLink(to, "MC2");
        ccr.addActorLink(to, "MC1");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("Should have blank to because no From in original CCR", "", reply.getValue("firstMcToEmail"));
        assertEquals("mc2 was added first so should be primary", "mc2@medcommons.org", reply.getValue("sourceEmail"));
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
    }
    
    public void test2OtherWithEmail() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "AA3");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("Should have blank to because no From in original CCR", "", reply.getValue("firstMcToEmail"));
        // assertEquals("AA2 was added first so should be primary", "jsmith@ccrdemohealthclinic.com", reply.getValue("sourceEmail"));
        String sourceEmail = reply.getValue("sourceEmail");
        assertNotNull(sourceEmail);
        assertFalse("".equals(sourceEmail));        
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
    }

    public void test2OtherWithEmailToAndFrom() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "AA3");
        ccr.addActorLink(from, "MC1");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("mc1@medcommons.org", reply.getValue("firstMcToEmail"));
        assertEquals("AA2 was added first so should be primary", "jsmith@ccrdemohealthclinic.com", reply.getValue("sourceEmail"));
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
    }
    
    public void test1OtherWithEmail() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("jsmith@ccrdemohealthclinic.com", reply.getValue("sourceEmail"));
        assertEquals("", reply.getValue("firstMcToEmail"));
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
    }
    
    public void test2Other1WithEmail() throws Exception {
        ccr.addActorLink(to, "AA6");
        ccr.addActorLink(to, "AA2");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("jsmith@ccrdemohealthclinic.com", reply.getValue("sourceEmail"));
        assertEquals("", reply.getValue("firstMcToEmail"));
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
    }
    
    public void test2Other1WithEmailReversed() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "AA6");
        ccr.syncFromJDom();        
        CCRDocument reply = this.desktop.getReplyCcr(this.ccr);
        assertEquals("jsmith@ccrdemohealthclinic.com", reply.getValue("sourceEmail"));
        assertEquals("", reply.getValue("firstMcToEmail"));
        
        reply.syncFromJDom();
        reply.getValidatedJDCOMDocument();
        
    }
    
    /**
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws ParseException
     * @throws RepositoryException 
     * @throws PHRException 
     */
    private CCRDocument getCcr() throws JDOMException, IOException, ParseException, RepositoryException, PHRException {
        return new CCRDocument(ServiceConstants.PUBLIC_MEDCOMMONS_ID,
        		this.guidGenerator.generateGuid((new Date()).toString().getBytes()), 
        		"000000000000",  this.ccrXml, CCRConstants.SCHEMA_VALIDATION_STRICT);
    }

    public NotifierService getNotifierService() {
        return this;
    }

    public HipaaService getHipaaService() {
        return null;
    }

    public DocumentService getDocumentService() {
        return null;
    }

    public TrackingService getTrackingService() {
        return this;
    }

    public void notify(String mcId, String recipientAddress, String trackingNumber, String message) throws ServiceException {
        this.notifications.add(recipientAddress);        
    }

    public void sendEmailCXP(String mcId, String recipientAddress, String trackingNumber, String message, String subject) throws ServiceException {
        this.notifications.add(recipientAddress);        
    }

    public String querySubject(String trackingNumber) throws ServiceException {
        return null;
    }

    public String trackDocument(String guid, String pinHash) throws ServiceException {
        return "000000000000";
    }

    public DocumentRegistration registerTrackDocument(String mcId, String guid, String pinHash, String rights) throws ServiceException {
        return null;
    }

    public TrackingReference validate(String trackingNumber, String pinHash) throws ServiceException {
        return null;
    }

    public void revokeTrackingNumber(String trackingNumber, String pinHash) throws ServiceException {
    }

    public String allocateTrackingNumber() throws ServiceException {
        return null;
    }

    public void reviseTrackedDocument(String trackingNumber, String pinHash, String guid) throws ServiceException {
    }

    public SecondaryRegistryService getSecondaryRegistryService() {
        return null;
    }

    public void updatePIN(String trackingNumber, String oldPinHash, String newPinHash) throws ServiceException {
    }

    public AccountCreationService getAccountCreationService() throws ServiceException {
        return null;
    }

    public DirectoryService getDirectoryService() {
        return null;
    }

    public DirectoryService getDirectoryService(String url) {
        return null;
    }

    public DocumentRegistration registerTrackDocument(String mcId, String guid, String pinHash, String pin, Long expirySeconds, TrackingAccessConstraint accessConstraint, String[]... additionalRights) throws ServiceException {
        return null;
    }

    public AccountService getAccountService() {
        return null;
    }

    public ActivityLogService getActivityLogService() {
        return null;
    }

    public String queryGuid(String trackingNumber) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public void sendFaxNotification(String mcId, String recipientAddress, String trackingNumber, String message,
                    String subject) throws ServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendLinkShareEmail(String recipientAddress, String subject, String link)
            throws ServiceException {
        // TODO Auto-generated method stub
        
    }
}
