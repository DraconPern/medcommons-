package net.medcommons.modules.crypto;


import javax.crypto.spec.SecretKeySpec;


/**
 * Implements a dummy placeholder key. This key is used to represent the case of 'no encryption'.
 * @author sean
 *
 */
public class DummyKeySpec extends SecretKeySpec{		
	public final static byte [] NONE = AES.SERIALIZED_DUMMY_KEY.getBytes();
	//public final static BigInteger NONE_BIGINT = new BigInteger(NONE);
	
	public DummyKeySpec(){
		super(NONE,"AES");
	}
	
	public String getAlgorithm(){
		return("Dummy");
	}
	public String getFormat(){
		return(null);
	}
	public byte[] getEncoded(){
		return(NONE);
	}
}