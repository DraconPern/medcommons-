package net.medcommons.router.services.ccrmerge;


import static net.medcommons.modules.utils.Str.blank;

import java.io.PrintStream;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRReferenceElement;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class ReferenceMergeTest extends BaseMergeTest {
    
    public ReferenceMergeTest() throws Exception {
        super();
    }

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ReferenceMergeTest.class);
    
    PrintStream out = System.out;
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testMergeIdenticalReference() throws Exception {
        
        log.info("testMergeIdenticalReference");
        
        to = CCRUtils.loadCCR("tests/ccr_minimal_dicom.xml", false);
        from = CCRUtils.loadCCR("tests/ccr_minimal_dicom.xml", false);
        
        CCRReferenceElement fromRef = (CCRReferenceElement) from.getReferences().getChild("Reference");
        assertNotNull(fromRef);
        assertFalse(blank(fromRef.getGuid()));
        
        CCRReferenceElement toRef = (CCRReferenceElement) to.getReferences().getChild("Reference");
        
        CCRElement fromDesc = fromRef.getPath("Locations/Location/Description");
        CCRElement toDesc = toRef.getPath("Locations/Location/Description");
        
        new AddMissingChildrenMerger().merge(fromDesc, to, toDesc);
        
        log.info("Output is " + Str.toString(toDesc));
        
        assertFalse(blank(toRef.getGuid()));
    }
}
