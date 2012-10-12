/*
 * $Id: $
 * Created on Jan 12, 2005
 */
package net.medcommons;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.medcommons.router.services.ccrmerge.*;
import net.medcommons.router.services.wado.DesktopTest;
import net.medcommons.router.services.wado.stripes.AccountImportActionTest;
import net.medcommons.router.services.wado.stripes.BillingActionTest;
import net.medcommons.router.services.wado.stripes.ShareCodecTest;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocumentTest;
import net.medcommons.router.util.StringUtilTest;
import net.medcommons.router.util.xml.XPathMappingsTest;
import net.medcommons.router.web.taglib.MedCommonsIdTagTest;
import net.medcommons.security.OAuthVerifierTest;
import net.medcommons.security.SessionFilterTest;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

/**
 * @author ssadedin
 */
public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for net.medcommons");    
    BasicConfigurator.configure();
    org.apache.log4j.Category.getRoot().setLevel(Level.WARN);
    //suite.addTest(net.medcommons.router.util.metrics.AllTests.suite());
    suite.addTestSuite(StringUtilTest.class);    
    suite.addTestSuite(CCRMergeTest.class);   
    suite.addTestSuite(CCRMergeComponentsTest.class);
    suite.addTestSuite(PatientDemoGraphicMatchMergePolicyTest.class);
    //    suite.addTestSuite(MonsterMergeTest.class);   
    suite.addTestSuite(DesktopTest.class);    
    suite.addTestSuite(CCRDocumentTest.class);    
    suite.addTestSuite(SessionFilterTest.class);    
    suite.addTestSuite(OAuthVerifierTest.class);    
    suite.addTest(MedCommonsIdTagTest.suite());    
    suite.addTest(AccountImportActionTest.suite());    
    suite.addTestSuite(XPathMappingsTest.class);    
    suite.addTest(BillingActionTest.suite());    
    suite.addTestSuite(ReferenceMergeTest.class);
    suite.addTest(new JUnit4TestAdapter(ShareCodecTest.class));
    return suite;
  }
}
