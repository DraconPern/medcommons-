package net.medcommons.router.selftests;

import java.net.InetAddress;

import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.selftest.SelfTest;
import net.medcommons.router.selftest.SelfTestResult;

/**
 * Simple test to see if the DNS is working properly.
 * @author sdoyle
 *
 */
public class HostnameResolution  implements SelfTest {

	 public SelfTestResult execute(ServicesFactory services) throws Exception {
	        InetAddress.getByName("www.google.com");
	        InetAddress.getByName("globals.medcommons.net");
	        return null;
	    }
}
