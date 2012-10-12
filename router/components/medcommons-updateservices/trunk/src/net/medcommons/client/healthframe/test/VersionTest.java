package net.medcommons.client.healthframe.test;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;



import net.medcommons.updateservice.client.healthframe.CheckForUpdates;
import net.medcommons.updateservice.client.healthframe.CheckForUpdatesSoap;
import junit.framework.TestCase;

public class VersionTest extends TestCase {
	public void testVersion() throws Exception{
		
	
		URL wsdlURL = new URL("http://www.RecordsForLiving.com/HealthFrame/XMLMessaging_20/CheckForUpdates/AnyUpdateForVersion");
		QName serviceName = new QName("http://www.RecordsForLiving.com/HealthFrame/XMLMessaging_20/CheckForUpdates/", "SOAPService");
		/*CheckForUpdates updateService = new CheckForUpdates(wsdlURL, serviceName);
		
		CheckForUpdatesSoap check = updateService.getCheckForUpdatesSoap12();
		boolean isCurrent = check.anyUpdateForVersion("1.3", "medcommons");
		*/
		
		Service service = Service.create(wsdlURL, serviceName);
		
		CheckForUpdatesSoap client = service.getPort(CheckForUpdatesSoap.class);
		boolean isCurrent = client.anyUpdateForVersion("1.3", "medcommons");
		
		assertEquals(isCurrent, true);
	}
}
