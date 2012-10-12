package net.medcommons.router.services.wado.stripes;

import java.math.BigInteger;

import net.medcommons.modules.services.interfaces.TrackingReference;

/**
 * Encodes a tracking number and pin as a string 
 * 
 * @author ssadedin
 */
public class ShareCodec {
    
    /**
     * Returns an encoded reference to the tracking number and pin
     * which can be decoded with {{@link #decode(String)}
     * 
     * @param trackingNumber
     * @param pin
     * @return
     */
    public String encode(String trackingNumber, String pin) {
        return String.format("%x",new BigInteger(trackingNumber+pin));        
    }
    
    /**
     * Decodes the specified tracking reference and returns 
     * the tracking number and pin as elements in the array.
     * 
     * @param reference
     * @return
     */
    public String[] decode(String reference) {
        BigInteger code = new BigInteger(reference,16);
        String decoded = String.format("%017d",code);
        return new String[] { decoded.substring(0,12), decoded.substring(12)  };
    }

}
