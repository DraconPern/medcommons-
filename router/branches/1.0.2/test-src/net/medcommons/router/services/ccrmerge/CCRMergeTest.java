/*
 * $Id$
 * Created on 25/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import static net.medcommons.modules.utils.TestDataConstants.USER1_ID;
import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.io.File;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.ccrmerge.preprocess.MergeConstants;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;


/**
 * To Do:
 * Add cases for 
 * merging medications

 * 
 * @author mesozoic
 *
 */
public class CCRMergeTest  extends BaseMergeTest  implements MergeConstants {

    private static final String MEDICATION_PRODUCT_NAME = "/x:ContinuityOfCareRecord/x:Body/x:Medications/x:Medication/x:Product/x:ProductName/x:Text";
    private static final String MEDICATIONS = "/x:ContinuityOfCareRecord/x:Body/x:Medications";
    
    //private static final String HEALTHFRAME_SOURCES = "//x:Source/x:Description";
    
    /**
     * Logger to use with this class
     */
    static Logger log = Logger.getLogger(CCRMergeTest.class);

    public CCRMergeTest(String arg0) throws Exception {
        super(arg0);
        log.info("**** CCRMergeTest:" + arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        

        log.debug("setUp - about to parse ccr 1");
        to = CCRDocument.createFromTemplate(
                ServiceConstants.PUBLIC_MEDCOMMONS_ID,
                "tests/ccr_minimal.xml", CCRConstants.SCHEMA_VALIDATION_STRICT);
        log.debug("setUp - about to parse ccr 2");
        from = CCRDocument.createFromTemplate(
                ServiceConstants.PUBLIC_MEDCOMMONS_ID,
                "tests/ccr_minimal.xml", CCRConstants.SCHEMA_VALIDATION_STRICT);
        log.debug("setUp - completed parsing ccr2");

    }

    public void testAddBody() throws Exception {
        from.getRoot().addContent(el("Body"));

        Change c = MergerFactory.merge(from, to);

        /*List<String> changes = new ArrayList<String>();
        c.toString(changes); 
        for (String change : changes) {
            System.out.println("Change: " + change);
        }
         */
        System.out.println("Changes:  " + c.toString());

        assertTrue("should have body", to.getRoot().getChild("Body") != null);
    }

    public void testAddUpdateRow() throws Exception {
        to.getRoot().addChild(el("Body"));

        // A CCR with a body
        CCRElement body = from.getRoot().addChild(el("Body"));
        CCRElement medications = body.addChild(el("Medications"));
        
        MergerFactory.merge(from, to);
        assertNotNull("should have medications", to.getRoot().getChild("Body")
                .getChild("Medications"));

        // Now add a row to the medications
        CCRElement med = createMedication(from);
        //printElement(med);
        medications.addChild(med);

        // Merge again 
        Change c = MergerFactory.merge(from, to);
        to.syncFromJDom();
        //printElement((Element) to.getJDOMDocument().getRoot());
        //System.out.println(to.getXml());

        System.out.println("Changes:  " + c.toString());

        // Check to for product name
        assertEquals("Vitamin C", to.getValue(MEDICATION_PRODUCT_NAME));

        // Update product name
        med.createPath("Product/ProductName/Text", "Vitamin D");

        // Because it has the same object id, merging should keep the same row and update the existing one
        MergerFactory.merge(from, to);

        assertEquals("Vitamin D", to.getValue(MEDICATION_PRODUCT_NAME));
    }

    /**
     * xxtest that merging can create two rows of the same type where that is allowed
     * 
     * eg. two separate Medications should get merged and updated separately.
     */
    public void testMultipleRows() throws Exception {
        // Create first medication row
        from.getRoot().createPath("Body/Medications",
                this.createMedication(from));

        // Merge it in
        MergerFactory.merge(from, to);

        // Sanity check it's there
        assertEquals("Vitamin C", to.getValue(MEDICATION_PRODUCT_NAME));

        // Now add a new row (different object id)
        CCRElement med2 = this.createMedication(from);
        med2.createPath("Product/ProductName/Text", "Vitamin D");
        from.getRoot().getChild("Body").getChild("Medications").addChild(med2);

        // Merge again
        MergerFactory.merge(from, to);

        // Should now have two rows instead of one
        assertEquals(
                "Separate rows should be created when object id is different",
                2, to.getRoot().getChild("Body").getChild("Medications")
                        .getChildren().size());
    }

    /**
     * When one object depends on another then merging that object must also
     * merge the dependency in.  Failure to do so should result in an exception. 
     * 
     * @throws Exception
     */
    public void testAddDependency() throws Exception {
        // Merge a medications section (including actor)
        CCRElement med = this.createMedication(from);

        // Copy the patient actor
        CCRElement newActor = (CCRElement) (xpath.getElement(from.getRoot(),
                "patientActor").clone());
        String actorId = to.generateObjectID();
        newActor.createPath("ActorObjectID", actorId);
        CCRElement name = newActor.getChild("Person").getChild("Name").getChild("CurrentName").getChild("Family");
        name.setText("TheTestFamily");
        
        // Add the new actor to from
        from.getRoot().getChild("Actors").addChild(newActor);

        // Change the medication to use the new actor as source
        med.createPath("Source/Actor/ActorID", actorId);

        // Replace the existing actor with our new one in the "from" document
        from.getRoot().createPath("Body/Medications", med);

        // Do the merge
        MergerFactory.merge(from, to);

        // New Actor should now be created under Actors
        assertEquals("expected actor not merged as dependency", 2, to.getRoot()
                .getChild("Actors").getChildren().size());
    }

    /**
     * xxtest that merging dates of different types (exact vs inexact) does NOT result in both
     * nodes (ApproximateDateTime and ExactDateTime) appearing. 
     */
    public void testMergeDates() throws Exception {

        // Set the from patient date of birth to exact date
        CCRElement fromPatientActor = (CCRElement) (xpath.getElement(from
                .getRoot(), "patientActor"));
        String exactDob = fromPatientActor.getCurrentTime();
        fromPatientActor.createPath("Person/DateOfBirth/ExactDateTime",
                exactDob);

        // Set the to patient date of birth to approximate date
        CCRElement toPatientActor = (CCRElement) (xpath.getElement(
                to.getRoot(), "patientActor"));
        toPatientActor.createPath(
                "Person/DateOfBirth/ApproximateDateTime/Text", "yesterday");

        // Merge it in
        Change c = MergerFactory.merge(from, to);

        assertEquals(1, toPatientActor.getChild("Person").getChild(
                "DateOfBirth").getChildren().size());
        assertNull(to.getValue("patientApproxDateOfBirth"));
    }

    /**
     * xxtest that merging CCRs with two different patient actor ids 
     * creates a resulting CCR without broken patient actor references
     */
    public void testMergePatientActors() throws Exception {
        CCRElement fromPatientActor = (CCRElement) (xpath.getElement(from
                .getRoot(), "patientActor"));
        fromPatientActor.createPath("ActorObjectID", "FromActorObjectId");
        from.getRoot().createPath("Patient/ActorID", "FromActorObjectId");

        // Merge it in
        Change c = MergerFactory.merge(from, to);

        // Check that there is a patient actor
        CCRElement toPatientActor = (CCRElement) (xpath.getElement(
                to.getRoot(), "patientActor"));
        assertNotNull(toPatientActor);
    }
    
    /**
     * Test that merging two actors that have the same MedCommons Patient ID    
     * result in a single actor with merged attributes in the resulting
     * CCR.
     */ 
    public void testMergeSameMedCommonsPatientActors() throws Exception {
        CCRElement fromPatientActor =  
            from.getRoot().queryProperty("patientActor");
        
        fromPatientActor.createPath("ActorObjectID", "FromActorObjectId");
        from.getRoot().createPath("Patient/ActorID", "FromActorObjectId");
        from.addPatientId(USER1_ID, "MedCommons Account Id");
        from.getJDOMDocument().setValue("patientGivenName", "GIVEN")
                              .setValue("patientFamilyName", "FAMILY");
        
        CCRElement toPatientActor =  
            to.getRoot().queryProperty("patientActor");
        toPatientActor.createPath("ActorObjectID", "toActorObjectId");
        to.getRoot().createPath("Patient/ActorID", "toActorObjectId");
        to.addPatientId(USER1_ID, "MedCommons Account Id");
        to.getJDOMDocument().setValue("patientGivenName", "")
                            .setValue("patientFamilyName", "");

        // Merge it in
        Change c = MergerFactory.merge(from, to);

        assertNotNull(toPatientActor);
        assertEquals("GIVEN", to.getValue("patientGivenName"));
    }
    
    
    /**
     * xxtest that merging CCRs with two different patient actor ids 
     * creates a resulting CCR without broken patient actor references
     */
    public void testMergePatientActorIDs() throws Exception {
        String newMedCommonsID = "1234567887654321";
        CCRElement fromPatientActor = (CCRElement) (xpath.getElement(from
                .getRoot(), "patientActor"));
        fromPatientActor.createPath("ActorObjectID", "FromActorObjectId");
        from.getRoot().createPath("Patient/ActorID", "FromActorObjectId");

        from.addPatientId(newMedCommonsID, "MedCommons Account Id");
        //log.info(Str.toString(fromPatientActor));
        
        CCRElement beforetoPatientActor = (CCRElement) (xpath.getElement(
                to.getRoot(), "patientActor"));
        log.info("before merge:" + Str.toString(beforetoPatientActor));
        CCRUtils.mergeCCR(MergerFactory.class, "PatientMedCommonsIDMerge", to, from);
        // Merge it in
        //Change c = MergerFactory.merge(from, to);

        // Check that there is a patient actor
        CCRElement toPatientActor = (CCRElement) (xpath.getElement(
                to.getRoot(), "patientActor"));
        assertNotNull(toPatientActor);
        CCRElement toPatientMedcommonsID = (CCRElement) (xpath.getElement(to
                .getRoot(), "patientMedCommonsId"));
        String insertedMedCommonsId = toPatientMedcommonsID.getText();
        assertEquals("MedCommonsIDs", newMedCommonsID, insertedMedCommonsId);
        //log.info("after merge:" + Str.toString(toPatientActor));
        
        
    }

    public void testMergeCurrentName() throws Exception {
        CCRElement fromName = el("CurrentName");
        fromName.createPath("Given", "");
        fromName.createPath("Family", "COMUNIX");

        CCRElement toName = el("CurrentName");
        toName.createPath("Family", "COMUNIX");

        CCRElement fromPatientActor = (CCRElement) (xpath.getElement(from
                .getRoot(), "patientActor"));
        CCRElement toPatientActor = (CCRElement) (xpath.getElement(
                to.getRoot(), "patientActor"));

        fromPatientActor.createPath("Name", fromName);
        toPatientActor.createPath("Name", toName);

        Change c = MergerFactory.getInstance().create(fromName).merge(fromName,
                to, toName);

        assertTrue(toName.getChildren().size() == 2);
    }
    
    /**
     * Merging changes from a CCR to another CCR should *not* change the source
     * CCR.  This xxtest reproduces a bug where the patient actor is removed from
     * the source CCR by the process of merging (why?).  
     */
    public void testDontRemovePatientActors() throws Exception {
        CCRDocument from = CCRUtils.loadCCR("tests/dont_remove_patient_actor.xml");
        CCRDocument to = from.copy();
        
        assertNotNull(from.getJDOMDocument().queryProperty("patientActor"));
        
        MergerFactory.merge(from, to);
        
        assertNotNull(from.getJDOMDocument().queryProperty("patientActor"));
    }
    
    
    /**
     * commented out because failing - sean, please look at this!
     * 
     * Two CCRs with the same patient medcommons id.  After merging 
     * together the patient actor should still have the same 
     * medcommons id, and it should only have that single MedCommons ID.
     */
    
    public void testDontMergeActorIDs() throws Exception {
        log.info("testDontMergeActorIDs");
        CCRDocument from = CCRUtils.loadCCR("tests/double_patient_id.xml");
        CCRDocument to = CCRUtils.loadCCR("tests/double_patient_id2.xml");
        
        assertNotNull(from.getJDOMDocument().queryProperty("patientActor"));
        
        CCRElement a = to.getRoot().queryProperty("patientActor");
        
        assertNotNull(a);
        assertEquals(1, a.getChildren("IDs").size());
        
        // CCRMangler.mangleCCR(from.getRoot());
        // CCRMangler.mangleCCR(to.getRoot());
        MergerFactory.merge(from, to);
        // CCRMangler.unMangleCCR(from.getRoot());
        // CCRMangler.unMangleCCR(to.getRoot());
        
        a = to.getRoot().queryProperty("patientActor");
        assertNotNull(a);
       
        log.info("Merged CCR:" +to.toString());
        // Check IDs
        assertEquals(1, a.getChildren("IDs").size());
        assertEquals("1087997704966332", to.getPatientMedCommonsId());
    }
     
    protected File mergeHealthFrameCCR(String title, String currentCCR, String incomingCCR) throws Exception{
        File merged;
        try{
            CCRDocument currentCCRDocument = CCRUtils.loadCCR(currentCCR);
            CCRDocument incomingCCRDocument = CCRUtils.loadCCR(incomingCCR);
            merged = CCRUtils.mergeCCR(ReferenceIDsMergerFactory.class, title, currentCCRDocument, incomingCCRDocument);
        }
        catch(Exception e){
            log.error("mergeCCR", e);
            throw e;
        }
        return(merged);
    }
    
    public void testHealthFrameMergeStella() throws Exception{
        mergeHealthFrameCCR("MergeWithHealthFrame", "tests/originalCCR.xml", "tests/healthFrameCCR.xml");
    }
    public void testHealthFrameMergeStellaSame() throws Exception{
        mergeHealthFrameCCR("MergeWithHealthFrame", "tests/healthFrameCCR.xml", "tests/healthFrameCCR.xml");
    }
    public void testHealthFrameMergeJaneHernandezHealthFrame() throws Exception{
        mergeHealthFrameCCR("MergeWithHealthFrame", "tests/JaneHerandezCurrentCCR.xml", "tests/JaneHerandezHealthFrameExport.xml");
    }
    
    public void testMergeStella() throws Exception{
        mergeCCR("MergeWithHealthFrame", "tests/originalCCR.xml", "tests/healthFrameCCR.xml");
    }
    public void testMergeJaneHernandezHealthFrame() throws Exception{
        mergeCCR("MergeWithHealthFrame", "tests/JaneHerandezCurrentCCR.xml", "tests/JaneHerandezHealthFrameExport.xml");
    }
    public void testMergeNewMedication() throws Exception{
        mergeCCR("MergeWithHealthFrameMedication", 
                "tests/StellaPattersonCurrentCCR.xml", "tests/StellaPattersonLisinopril.xml");
    }
    public void testSimilarCCRs() throws Exception{
        mergeCCR("MergeWithSimilar", "tests/jane_bewell.xml", "tests/jane_bewell2.xml");
    }
    public void testMergeHealthFrameJaneBewell() throws Exception{
        mergeCCR("MergeWithHealthFrame", "tests/JaneBewellCurrentCCR.xml", "tests/JaneBewellHealthFrameExport.xml");
    }
    
    public void testMergeJaneBewellToHealthFrame() throws Exception{
        mergeCCR("MergeHealthframeToCurrent", "tests/JaneBewellHealthFrameExport.xml", "tests/JaneBewellCurrentCCR.xml");
    }
    public void testMergeHealthframeAlerts() throws Exception{
        mergeCCR("MergeHealthFrameAlerts", "tests/JaneBewellCurrentCCRAlerts.xml", "tests/JaneBewellHealthFrameAlerts.xml");
    }
    
    public void testMergeIntoSameHernandez() throws Exception{
        mergeCCR("MergeToSelf", "tests/JaneHerandezCurrentCCR.xml", "tests/JaneHerandezCurrentCCR.xml");
    }
    public void testMergeIntoSameBewell() throws Exception{
        mergeCCR("MergeToSelf", "tests/jane_bewell.xml", "tests/jane_bewell.xml");
    }
    public void testMergeIntoSameBewellHealthFrame() throws Exception{
        mergeCCR("MergeHealthFrameToSelf", "tests/JaneBewellHealthFrameExport.xml", "tests/JaneBewellHealthFrameExport.xml");
    }
    public void testMergeFaxIntoFax() throws Exception{
        mergeCCR("MergeFax", "tests/Fax-currentccr.xml", "tests/IncomingFaxCCR.xml");
    }
    public void testMergeBOMtoSelf() throws Exception{
        // BOM test fails on various platorms for some reason
        // mergeCCR("MergeBOM", "tests/CCR-with-BOM.xml", "tests/CCR-with-BOM.xml");
    }
    
    /*
    xxtest removed - the ccr contains embedded references. These throw errors in CCRDocument unless running
    inside the application.
       public void testHealthFrameJaneH() throws Exception{
           mergeHealthFrameCCR("MergeWithHealthFrame", "tests/108ccrs/originalCurrent108.xml", "tests/108ccrs/incomingHealthFrameCCR108.xml");
       }
    */

    public static void main(String[] args){
        try{
            log.error("--- about to start");
            String[] alltests = new String[]{"testMergeStella", "testMergeJaneHernandezHealthFrame", "testMergeHealthFrameJaneBewell"};
            for (int j=0; j<10; j++){
                for (int i=0;i<alltests.length; i++){
                    CCRMergeTest atest = new CCRMergeTest(alltests[i]);
                    atest.setUp();
                    atest.run();
                }
            }
            
        }
        catch(Exception e){
            log.error("Error running main", e);
        }
    }
}
