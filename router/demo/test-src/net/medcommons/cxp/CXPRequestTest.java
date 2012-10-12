/*
 * $Id$
 * Created on 26/09/2005
 */
package net.medcommons.cxp;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.crypto.GuidGenerator;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.BasicConfigurator;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;


public class CXPRequestTest extends TestCase implements ServicesFactory, NotifierService, CCRConstants {
    
    private CXPRequest cxp = new CXPRequest(ServiceConstants.PUBLIC_MEDCOMMONS_ID,"","","",null,null,"LENIENT");
    
    private Set<String> notifications = new HashSet<String>();
    
    private GuidGenerator guidGenerator = null;
    
    private String ccrXml = null;
    
    private CCRDocument ccr = null;

    private Element to;
    
    private Namespace ccrns = Namespace.getNamespace(CCR_NAMESPACE_URN);
    
    public static void main(String[] args) {
    }

    public CXPRequestTest(String arg0) throws NoSuchAlgorithmException, Exception {
        super(arg0);
        this.guidGenerator = new GuidGenerator();
        FileInputStream in = new FileInputStream("test-src/net/medcommons/services/rest/testCcr.xml");
        byte [] buffer = new byte[20000];
        int read = in.read(buffer);
        this.ccrXml = new String(buffer,0,read);        
        
        // Prevent logging trying to read the normal config by configuring it early
        BasicConfigurator.configure(); 
        Configuration.load("etc/configurations/config.xml",
                           "etc/configurations/MedCommonsBootParameters.properties",
                           "etc/configurations/medcommons-test-config.xml");
    }

    protected void setUp() throws Exception {
        super.setUp();        
        notifications.clear();
        this.ccr = getCcr();
        to = this.ccr.getJDOMDocument().getRootElement().getChild("To", ccrns);
    }
    
    public void testNotifyNobody() throws Exception {
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(0, notifications.size());
    }
    
    public void test1MC() throws Exception {
        ccr.addActorLink(to, "MC1");
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(1, notifications.size());
    }
    
    public void test1MC1Other() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "MC1");
        ccr.syncFromJDom();        
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(1, notifications.size());
        assertTrue("When MC mixed with non-MC mc's should be notified.  Expected mc1@medcommons.org", notifications.contains("mc1@medcommons.org"));
    }

    public void test2MC() throws Exception {
        ccr.addActorLink(to, "MC2");
        ccr.addActorLink(to, "MC1");
        ccr.syncFromJDom();        
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(2, notifications.size());
        assertTrue("When multiple MCs exist first should be notified. Expected mc1@medcommons.org", notifications.contains("mc1@medcommons.org"));
    }
    
    public void test2OtherWithEmail() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "AA3");
        ccr.syncFromJDom();        
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals("All non-mc addresses should generate no notification", 0, notifications.size());
    }

    public void test1OtherWithEmail() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.syncFromJDom();        
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(1, notifications.size());
        assertTrue("Expected mail to jsmith@ccrdemohealthclinic.com", notifications.contains("jsmith@ccrdemohealthclinic.com"));
    }
    
    public void test2Other1WithEmail() throws Exception {
        ccr.addActorLink(to, "AA6");
        ccr.addActorLink(to, "AA2");
        ccr.syncFromJDom();        
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(1, notifications.size());
        assertTrue("Expected mail to jsmith@ccrdemohealthclinic.com", notifications.contains("jsmith@ccrdemohealthclinic.com"));
    }
    
    public void test2Other1WithEmailReversed() throws Exception {
        ccr.addActorLink(to, "AA2");
        ccr.addActorLink(to, "AA6");
        ccr.syncFromJDom();        
        this.cxp.notify("0000000000000000", this, ccr, "CXP Test", "", "pops");
        assertEquals(1, notifications.size());
        assertTrue("Expected mail to jsmith@ccrdemohealthclinic.com", notifications.contains("jsmith@ccrdemohealthclinic.com"));
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
        return new CCRDocument(ServiceConstants.PUBLIC_MEDCOMMONS_ID, this.guidGenerator.generateGuid((new Date()).toString().getBytes()), "000000000000",  this.ccrXml, "LENIENT");
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
        return null;
    }

    public void notify(String mcId, String recipientAddress, String trackingNumber, String message) throws ServiceException {
        this.notifications.add(recipientAddress);        
    }

    public void sendEmailCXP(String mcId, String recipientAddress, String trackingNumber, String message, String subject, String comments) throws ServiceException {
        this.notifications.add(recipientAddress);        
    }

    public String querySubject(String trackingNumber) throws ServiceException {
        return null;
    }

    public AccountService getAccountService() {
        // TODO Auto-generated method stub
        return null;
    }

    public SecondaryRegistryService getSecondaryRegistryService() {
        // TODO Auto-generated method stub
        return null;
    }

    public AccountCreationService getAccountCreationService() throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public DirectoryService getDirectoryService() {
        // TODO Auto-generated method stub
        return null;
    }

    public DirectoryService getDirectoryService(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public ActivityLogService getActivityLogService() {
        // TODO Auto-generated method stub
        return null;
    }

    public BillingService getBillingService() throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public void sendFaxNotification(String mcId, String recipientAddress, String trackingNumber, String message,
                    String subject) throws ServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendLinkShareEmail(String recipientAddress, String subject, String link, String comments)
            throws ServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setAuthContext(String contextAuth) {
            // TODO Auto-generated method stub
            
    }

    /*
    @Override
    public ExpireService getExpiryService() throws ServiceException {
            // TODO Auto-generated method stub
            return null;
    }
    */

}
