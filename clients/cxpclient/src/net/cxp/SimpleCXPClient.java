/* 
 * Copyright 2006; Some Rights Reserved
 * 
 * This sample program is licensed under the Creative Commons Attribution
 * license. For more details see:
 *  http://creativecommons.org/licenses/by/2.5/
 * We request attribution to both MedCommons and the CCR Accelerator group.
 */
package net.cxp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import cxp.CXPException;
import cxp.CXP_10;
import cxp.CXP_10ServiceLocator;
import cxp.GetResponse;
import cxp.Parameter;
import cxp.PutResponse;
import cxp.RegistryParameters;

/**
 * A trivial CXP client written in Java. There are two messages demonstrated:
 * 
 * PUT places the CCR on the designated server GET reads a CCR via the
 * RegistrySecret/Confirmation code arguments.
 * 
 * There are three types of class files needed for this example to run:
 * <ul>
 * <li> The Apache Axis distribution. This code was tested with Axis 1.3. See
 *      http://ws.apache.org/axis/ to get the distribution.</li>
 * <li> Compiled source files generated from the CXP WSDL. See Ant build.xml
 *      file included with this sample file - the call to &gt;axis-wsdl2java&lt;
 *      builds the glue source code in the cxp package.</li>
 * <li> This file - a very simple command-line application.</li>
 * </ul>
 * 
 * Use: java <classpath stuff> SimpleCXPClient <CXP url> [PUT <cxp_file> [CommonsID] | [GET <confirmation code>
 * 
 * @author sean
 * 
 */
public class SimpleCXPClient {

	/**
	 * Takes a variable number of arguments: 
	 * <UL>
	 * <li>The URL of the SOAP endpoint </li>
	 * <li>The CXP command (PUT or GET) </li>
	 * <li>
	 * <UL> If the command is PUT then 
     *     <li> a file reference to the CCR </li>
     *     <li> [optional] the 'CommonsID' of the user (the data
     *     will be put into that user's account)</li>
     * </UL>
     * </li>
     * <li>
     * <UL>If the command is GET two arguments are needed:
	 *   <li> The confirmation code </li>
	 *   <li> The registry secret.</li>
	 * </UL>
	 * </UL>
	 * 
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		// A real program would test the validity of the input arguments.

		if (args.length < 2) {
			usage();
			return;
		}

		String command = args[0];
		String url = args[1];

		SimpleCXPClient client = new SimpleCXPClient();
		URL serviceURL = new URL(url);

		/**
		 * Initialize the SOAP service
		 */
		CXP_10 cxpService = new CXP_10ServiceLocator().getCXP(serviceURL);

		/**
		 * Initialize some variables to constants. A real program would use real
		 * values here.
		 */
		String commonsID = "1234567890123456"; // 16 digits
		String senderID = System.getProperty("user.name"); // In the future
															// this might be a
															// SAML assertion
		
		// A RegistrySecret (a PIN). Note that if the PIN isn't specified that one
		// will be created and returned in response.
		String registrySecret = "12345"; 
		String notificationSubject = "Java SOAP CXP Client Notification";

		RegistryParameters[] inParameters = new RegistryParameters[1];
		inParameters[0] = new RegistryParameters();
		inParameters[0].setRegistryId("medCommons.net");
		inParameters[0].setRegistryName("MedCommons");
		if ("PUT".equalsIgnoreCase(command)) {
			String ccrFilename = args[2];
			if (args.length > 3) {
				commonsID = args[3];
			}
			Parameter[] params = new Parameter[4];
			params[0] = new Parameter("CommonsID", commonsID);
			params[1] = new Parameter("SenderID", senderID);
			params[2] = new Parameter("RegistrySecret", registrySecret);
			params[3] = new Parameter("NotificationSubject",
					notificationSubject);
			inParameters[0].setParameters(params);
			client.cxp_put(cxpService, ccrFilename, inParameters);
		} else if ("GET".equalsIgnoreCase(command)) {
			String confirmationCode = args[2];
			registrySecret = args[3];

			Parameter[] params = new Parameter[4];
			params[0] = new Parameter("CommonsID", commonsID);
			params[1] = new Parameter("SenderID", senderID);
			params[2] = new Parameter("RegistrySecret", registrySecret);
			params[3] = new Parameter("ConfirmationCode", confirmationCode);
			inParameters[0].setParameters(params);
			client.cxp_get(cxpService, inParameters);

		}

	}

	public static void usage() {
		StringBuffer buff = new StringBuffer("Usage:\n");
		buff
				.append("java -cp <path to jar files plus sample .class files> net.cxp.SimpleCXPClient ");
		buff
				.append(" <CXP url> [PUT <cxp_file> [Commons ID]] | [GET <confirmation code> <registry secret>");

		System.out.println(buff.toString());

	}

	public void cxp_get(CXP_10 cxpService, RegistryParameters[] inParameters)
			throws CXPException, RemoteException {
		GetResponse response = cxpService.get(inParameters);
		int status = response.getStatus();
		if (status == 200) {
			System.out.println("GET Success:");
			String ccr = response.getContent();
			System.out.println(ccr);
		} else {
			System.out.println("GET Failure:" + status);
			System.out.println("  Reason:" + response.getReason());

		}

	}

	public void cxp_put(CXP_10 cxpService, String ccrFilename,
			RegistryParameters[] inParameters) throws IOException {

		// Read the CCR file into a String
		StringBuffer ccrBuff = new StringBuffer();

		File ccrFile = new File(ccrFilename);
		if (!ccrFile.exists()) {
			throw new FileNotFoundException("CCR file does not exist: "
					+ ccrFile.getAbsolutePath());

		}
		FileInputStream ccrIn = new FileInputStream(ccrFile);

		byte[] buffer = new byte[10 * 1024];
		int bytesRead;
		while ((bytesRead = ccrIn.read(buffer, 0, buffer.length)) != -1) {
			ccrBuff.append(new String(buffer, 0, bytesRead));
		}
		String ccr = ccrBuff.toString();

		// Send message
		PutResponse response = cxpService.put(ccr, inParameters);

		// Print the response
		System.out.println("Response:");
		System.out.println(" Status=" + response.getStatus());
		System.out.println(" Reason=" + response.getReason());
		System.out.println(" UUID  =" + response.getGuid());
		RegistryParameters[] outParameters = response.getRegistryParameters();
		if (outParameters != null) {
			for (int i = 0; i < outParameters.length; i++) {
				System.out.println("Returned Registry Parameters: "
						+ outParameters[i].getRegistryName());
				Parameter[] outParams = outParameters[i].getParameters();
				for (int j = 0; j < outParams.length; j++) {
					System.out.println("    " + outParams[j].getName() + " = "
							+ outParams[j].getValue());
				}
			}
		}
	}

}
