package net.medcommons.modules.crypto.test;


import java.security.Key;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import junit.framework.TestCase;
import net.medcommons.modules.crypto.AES;

public class KeySerializationTest extends TestCase{

	/**
	 * Tests to see that the dummy key generated (the case when encryption 
	 * is turned off for new documents) is a dummy key.
	 *
	 * @throws Exception
	 */
	public void testDummyKey() throws Exception{
		AES aes = new AES();
		
		Key key = aes.generateDummyKey();

		assertTrue("Dummy key generation failed", aes.isDummyKey(key));
	
	}
	
	/**
	 * Test to see that non-dummy keys are not detected as dummy keys.
	 * @throws Exception
	 */
	public void testNonDummyKey() throws Exception{
		AES aes = new AES();
		
		Key key = aes.generateKey();

		assertFalse("Generated key incorrectly detected as a dummy key", aes.isDummyKey(key));
	
	}

	/**
	 * Test to see that serialization and deserialization of a dummy
	 * key results in a dummy key.
	 * @throws Exception
	 */
	public void testKeySerializationDummy() throws Exception{
		AES aes = new AES();
		
		Key key1 = aes.generateDummyKey();
		
		String serializedKey = aes.keyToString(key1);
		assertEquals("Serialized dummy key failed", AES.SERIALIZED_DUMMY_KEY, serializedKey);
		
		SecretKeySpec key2 = aes.getKeyFromString(serializedKey);
		assertTrue("Dummy key de-serialization failed", aes.isDummyKey(key2));
		
		
		
		
	}
	
	/**
	 * Test of serialization/deserialization to make sure that the key's
	 * encoded bytes are preserved.
	 * @throws Exception
	 */
	public void testKeySerialization() throws Exception{
		AES aes = new AES();
		
		Key key1 = aes.generateKey();
		
		
		String serializedKey1 = aes.keyToString(key1);
		byte [] encoded1 = key1.getEncoded();
		
		
		SecretKeySpec key2 = aes.getKeyFromString(serializedKey1);
		
		String serializedKey2 = aes.keyToString(key2);
		
		assertEquals("Serialization/Deserialization failure", serializedKey1, serializedKey2);
		
		byte [] encoded2 = key2.getEncoded();
		assertTrue("Encoded key mismatch after serialization", 
				Arrays.equals(encoded1, encoded2));
		
		
		
		
		
	}
	
}
