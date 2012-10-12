package net.medcommons.modules.crypto;

import java.security.Key;
/**
 * Implements a dummy placeholder key. This key is used to represent the case of 'no encryption'.
 * @author sean
 *
 */
public class DummyKey implements Key{
	byte [] value = AES.SERIALIZED_DUMMY_KEY.getBytes();
	public String getAlgorithm(){
		return("Dummy");
	}
	public String getFormat(){
		return(null);
	}
	public byte[] getEncoded(){
		return(value);
	}
}