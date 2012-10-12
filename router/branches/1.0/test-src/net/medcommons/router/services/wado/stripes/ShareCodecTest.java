package net.medcommons.router.services.wado.stripes;

import static org.junit.Assert.*;

import org.junit.Test;

public class ShareCodecTest {
    
    ShareCodec codec = new ShareCodec();

    @Test
    public void testEncode() {
        String [][] testCases = new String[][] {
                { "112345678912", "12345" },
                { "112345678912", "00000" },
                { "012345678912", "54321" },
                { "112345678910", "02345" }
        };
        
        for(String [] pair : testCases) {
            
            String tracking = pair[0];
            String pin = pair[1];
        
            String encoded = codec.encode(tracking, pin);
            
            assertTrue(encoded.length() >= 6);
            
            System.out.print("Encoded = " + encoded + "  => ");
            
            // Check that we can decode
            String [] decoded = codec.decode(encoded);
            
            System.out.println("Decoded tn = " + decoded[0] + " pin = " + decoded[1]);
            assertEquals(tracking, decoded[0]);
            assertEquals(pin, decoded[1]);
        }
    }
}
