package net.medcommons.modules.crypto.test;

import junit.framework.TestCase;
import net.medcommons.modules.crypto.SHA1;



public class SHA1Test extends TestCase {
	
	private SHA1 sha1 = null;
	
	
	/**
	 * Value via Tests for SHA-1 - from http://en.wikipedia.org/wiki/SHA-1
	 * 
	 * This is a simple test that the SHA-1 implementation matches the
	 * values in the public spec.
	 *
	 * @throws Exception
	 */
	public void testQuickBrownFox() throws Exception{
		sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();
		String input = "The quick brown fox jumps over the lazy dog";
		String knownHash = "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12";
		String inputs[]=new String[1];
		inputs[0] = input;
		
		String sha1Hash = sha1.calculateStringNameHash(inputs);
		assertEquals("Does not match known value:", knownHash, sha1Hash);
		
		
	}
	
	/**
	 * Value via Tests for SHA-1 - from http://en.wikipedia.org/wiki/SHA-1
	 *
	 * @throws Exception
	 */
	public void testEmptyString() throws Exception{
		sha1 = new SHA1();
		sha1.initializeHashStreamCalculation();
		String input = "";
		String knownHash = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
		String inputs[]=new String[1];
		inputs[0] = input;
		
		String sha1Hash = sha1.calculateStringNameHash(inputs);
		assertEquals("Does not match known value:", knownHash, sha1Hash);
		
	}
	
	//TODO
	// Tests for the SHA1 hash of a file 
	// Tests for the SHA-1 hash of a stream.
	// 

}
