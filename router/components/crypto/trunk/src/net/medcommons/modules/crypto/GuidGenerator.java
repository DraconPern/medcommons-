/*
 * Created on Sep 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.modules.crypto;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.medcommons.modules.crypto.Base64Coder;

/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GuidGenerator {
	private MessageDigest sha = null;

	public GuidGenerator() throws NoSuchAlgorithmException{
		super();

		sha = MessageDigest.getInstance("SHA-1");

	}


	/**
	 * Generates an encoded 160 bit GUID from the byte input.
	 *
	 * The GUID is the SHA-1 transform of the byte input.
	 * The encoding is the "URL and Filename safe" variant
	 * of Base64: character 62 (0x3E, aka "+") is replaced
	 * with "-"; character 63 (0x3F, aka "/") is replaced with
	 * "_".
	 *
	 * See http://www.rfc-editor.org/rfc/rfc3548.txt, section 4.
	 *
	 * @param input
	 * @return
	 */
	public  String generateGuid(byte[] input) throws IOException{
		String guid = null;
		sha.update(input);
		byte[] hash = sha.digest();
		sha.reset();


		guid = new String(Base64Coder.encode(hash));
		guid = guid.replace('/', '_');
		guid = guid.replace('+', '-');


		return (guid);
	}

	public static void main(String[] args) {
	}
}
