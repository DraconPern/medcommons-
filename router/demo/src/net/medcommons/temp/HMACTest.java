package net.medcommons.temp;

import net.medcommons.modules.crypto.HMAC;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.crypto.Utils;
/**
 * Very simple HMAC test.
 * [Need to move this to JUnit in modules.crypto]
 * This can be run from the router's build directory with the following arguments:
 * 
 * java -cp ".;../../lib/apache/axis/log4j-1.2.8.jar" net/medcommons/temp/HMACTest 10000 abcdefgh
 * 
 * Things I'm not sure about:
 * 1. Are the input HMAC keys the right size?
 * 2. I'm sure that there is something else.:-)
 * 
 * @author sean
 *
 */
public class HMACTest {

	public static void main(String[] args) {
		try {
			HMACTest test = new HMACTest();
			int nIterations = Integer.parseInt(args[0]);
			SHA1 sha1 = new SHA1();
			sha1.initializeHashStreamCalculation();

			byte[] hmacKey = args[1].getBytes();
			System.out.println("Starting run for " + nIterations);
			
			// Generate some really random set of inputs. SHA1 used here
			// just to generate things that look random - no other reason.
			String[] inputMessages = new String[nIterations];
			for (int i = 0; i < nIterations; i++) {
				String n = "iteration:" + i;
				String[] a = new String[1];
				a[0] = n;
				// The SHA-1 hash makes a String with 40 elements.
				String msg = sha1.calculateStringNameHash(a);
				// Make the messages be 120 characters. This is arbitrary.
				inputMessages[i] = msg + msg + msg;
			}

			HMAC hmac = new HMAC(hmacKey);
			long start = System.currentTimeMillis();
			for (int i = 0; i < nIterations; i++) {
				test.calculateHMAC(hmac, inputMessages[i]);
			}
			long end = System.currentTimeMillis();

			float elapsedSeconds = (float) ((end - start) / 1000.0);
			System.out.println("Elapsed time is " + elapsedSeconds);
			System.out.println("Time per hmac is " + elapsedSeconds
					/ nIterations);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

private void calculateHMAC(HMAC hmac, String input){
		byte [] digest = hmac.calculateDigest(input);
		boolean debug = false;
		if (debug){
			String digestString = Utils.encodeBytes(digest, 0, digest.length);
			System.out.println("Digest of " + input + " is " + digestString);
		}
	}}
