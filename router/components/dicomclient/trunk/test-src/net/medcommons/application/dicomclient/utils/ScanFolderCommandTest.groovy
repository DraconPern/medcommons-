package net.medcommons.application.dicomclient.utils;

import static org.junit.Assert.*;

import org.json.*;
import org.junit.Before;
import org.junit.Test;

class ScanFolderCommandTest {
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void testMixedPatients() {
        
        def sfc = new ScanFolderCommand()
        JSONObject results = sfc.scanDir("test-data/mixed" as File)

        JSONArray patientNames = results.getJSONArray("patientNames")
        println patientNames
        assert patientNames.length() == 2
        assert patientNames.getString(0) == "COMUNIX" || patientNames.getString(1) == "COMUNIX"
    }
    
}
