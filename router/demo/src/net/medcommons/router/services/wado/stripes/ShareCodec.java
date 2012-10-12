package net.medcommons.router.services.wado.stripes;

import java.math.BigInteger;

import net.medcommons.modules.services.interfaces.TrackingReference;

/**
 * Encodes a tracking number,pin, and optionally external share as a string 
 * <p>
 * The encoding is not secure and can be easily reversed back to a 
 * tracking number and in by anybody who receives it.
 * <p>
 * The current algorithm simply concatenates the tracking number and PIN and
 * then encodes the resulting number in hex.   If there is an external share
 * associated with the tracking number then the share id is appended 
 * padded with zeros to 9 digits, and the whole sequence is prepended with 
 * an 'x'.
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
     * @param esId 
     * @return
     */
    public String encode(String trackingNumber, String pin, Long esId) { 
        if(esId != null) 
            return String.format("x%x",new BigInteger(trackingNumber+pin+String.format("%09d",esId)));        
        else
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
        
        if(reference.startsWith("x")) {
            BigInteger code = new BigInteger(reference.substring(1),16);
            String decoded = String.format("%026d",code);
            return new String[] { decoded.substring(0,12), decoded.substring(12,17), decoded.substring(17,26)};
        }
        else {
            BigInteger code = new BigInteger(reference,16);
            String decoded = String.format("%017d",code);
            return new String[] { decoded.substring(0,12), decoded.substring(12), null};
        }
        
    }

}
