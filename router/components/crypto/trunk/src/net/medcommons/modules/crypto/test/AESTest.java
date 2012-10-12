package net.medcommons.modules.crypto.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import junit.framework.TestCase;
import net.medcommons.modules.crypto.AES;


public class AESTest extends TestCase {

	
	/**
	 * Tests encryption.
	 * @throws Exception
	 */
	public void testEncryptDecrypt() throws Exception {
		
		
		// Should test a existing file by calculating a SHA1 hash, 
		// encrypting it, testing that the SHA-1 hash is different,
		// then decrypting and seeing that the SHA-1 hash is the same
		// as at the start.
	}
	public void testDecrypt(){
		// Should decrypt a previously encrypted file with a known key.
		// This is a test to see that we're backward compatible.
	}
	
	public void testEncryptStream() throws Exception {
	    AES aes = new AES();
	    String text = "Hello World!";
	    
	    // Encrypt the text
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    Key key = aes.generateKey();
	    OutputStream out = aes.createOutputStream(bytes, key);
	    out.write(text.getBytes("UTF-8"));
	    out.flush();
	    out.close();
	    
	    assert bytes.toByteArray().length > 0;
	    
	    // Decrypt the text
	    ByteArrayInputStream encrypted = new ByteArrayInputStream(bytes.toByteArray());
	    InputStream in = aes.createInputStream(encrypted, key);
	    byte[] decrypted = new byte[1024];
	    int read = in.read(decrypted);
	    
	    assertEquals("Hello World!", new String(decrypted, 0, read, "UTF-8"));
	}
}
