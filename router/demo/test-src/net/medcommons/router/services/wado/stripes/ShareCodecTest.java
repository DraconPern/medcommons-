package net.medcommons.router.services.wado.stripes;

import static org.junit.Assert.*;

import org.junit.Test;

public class ShareCodecTest {
    
    ShareCodec codec = new ShareCodec();

    @Test
    public void testEncode() {
        Object [][] testCases = new Object[][] {
                { "112345678912", "12345", null },
                { "112345678912", "00000", null },
                { "012345678912", "54321", null },
                { "112345678910", "02345", null },
                { "112345678910", "02345", 0L },
                { "112345678910", "02345", 12345L },
                { "112345678910", "02345", 123456789L },
                { "012345678910", "02345", null },
                { "002345678910", "00000", 12L }
        };
        
        for(Object [] pair : testCases) {
            
            String tracking = (String) pair[0];
            String pin = (String) pair[1];
            Long esId = (Long) pair[2];
        
            String encoded = codec.encode(tracking, pin, esId);
            
            assertTrue(encoded.length() >= 6);
            
            System.out.print("Encoded = " + encoded + "  => ");
            
            // Check that we can decode
            String [] decoded = codec.decode(encoded);
            
            System.out.println("Decoded tn = " + decoded[0] + " pin = " + decoded[1]);
            assertEquals(tracking, decoded[0]);
            assertEquals(pin, decoded[1]);
            
            Long decodedEsId = decoded[2] == null ? null : new Long(decoded[2]);
            assertEquals(esId, decodedEsId);
        }
    }
}
