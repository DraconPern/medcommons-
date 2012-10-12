/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.dicom.util;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class GetStudyGUID {

	static String DEFAULT_ENDPOINT =
		"http://medcommons.net:8080/jboss-net/services/GuidService";
	static String endpoint = null;

	public static void setEndpoint(String newEndpoint) {
		endpoint = newEndpoint;
	}
	public static void init() {
		setEndpoint(DEFAULT_ENDPOINT);
	}
	public String getNewGuid() {
		String ret = null;
		try {

			Service service = new Service();
			Call call = (Call) service.createCall();

			call.setTargetEndpointAddress(new java.net.URL(endpoint));
			call.setOperationName("allocateGuid");
			call.setReturnType(org.apache.axis.Constants.XSD_STRING);

			ret = (String) call.invoke(new Object[] {
			});

			

		} catch (Exception e) {
			System.err.println(e.toString());
		}
		return (ret);
	}
}
