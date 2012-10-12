/**
 * 
 */
package net.medcommons.application.dicomclient.transactions

import org.junit.Before
import org.junit.BeforeClass
import org.apache.log4j.BasicConfiguratorimport net.medcommons.application.dicomclient.utils.StatusDisplayManagerimport java.util.logging.Levelimport org.junit.Testimport net.medcommons.application.dicomclient.utils.PixDemographicDataimport net.medcommons.application.dicomclient.utils.PixDemographicDataimport net.sourceforge.pbeans.Storeimport net.medcommons.application.dicomclient.utils.DBimport net.medcommons.application.dicomclient.utils.PixIdentifierDataimport net.medcommons.modules.services.interfaces.PatientDemographicsimport static net.medcommons.modules.utils.TestDataConstants.*import net.medcommons.modules.services.interfaces.GroupPatientQueryServiceimport net.medcommons.modules.services.interfaces.GroupPatientQueryServiceimport net.medcommons.application.dicomclient.ContextManagerimport net.medcommons.modules.utils.TestDataConstants

/**
 * @author ssadedin
 *
 */
public class PatientMatchTest {
    
    static {
        BasicConfigurator.configure()
        StatusDisplayManager.testMode = true
        // Logger.getLogger("net.sourceforge.pbeans").level = Level.WARNING
     }
     
    def patientParams = [
                         givenName: "JOHN",
                         middleName: "Q",
                         familyName: "TEST",
                         dob: new Date()
                        ]
 
    def patientIdParams = [
                            affinityDomain: "UTEST",
                            affinityIdentifier: "12345",
                            creationDate: new Date()
                          ]
        
    def Store db
    
    /*
    
    @Before
    void setUp() throws Exception {
        DB.testMode()
        db = DB.get() 
    }
    
    @Test
    void testGetIdentifier() {
        
        PixDemographicData p = new PixDemographicData(patientParams)
        db.insert(p)
        
        def id = PatientMatch.getIdentifier(p.id, "UTEST")
        assert !id : "Patient should not have identifier in UTEST domain yet"
        
        id = new PixIdentifierData(patientIdParams)
        id.pixDemographicDataId = p.id
        db.insert(id)
        
        id = PatientMatch.getIdentifier(p.id, "UTEST")
        assert id != null
        assert id.affinityIdentifier == "12345"
        
        // Try getting by affinity id
        id = PatientMatch.getIdentifier("UTEST", "12345")
        assert id != null
        assert id.affinityIdentifier == "12345"
        
        // Query for nonexistent value
        id = PatientMatch.getIdentifier("XXX", "12345")
        assert id == null
        
        id = PatientMatch.getIdentifier("UTEST", "99999")
        assert id == null
    }
    
    @Test
    void testGetPatient() {
        
        // Before there any patients we should not have
        // any matches, or any problems doing matches
        def patients = PatientMatch.getPatient("foo", "bar", "baz")
        assert !patients : "no patients should match"
        
        PixDemographicData p = new PixDemographicData(patientParams)
        db.insert(p)
        
        patients = PatientMatch.getPatient("foo", "bar", "baz")
        assert !patients : "no patients should match"
        
        // null family name should not match
        patients = PatientMatch.getPatient("John", null, null)
        assert !patients : "no patients should match"
        
        // null in middle name acts as wild card
        patients = PatientMatch.getPatient("John", null, "Test")
        assert patients.size() == 1 : "no patients should match"
        assert patients[0].givenName == "JOHN"
        
        // null in first name acts as wild card
        patients = PatientMatch.getPatient(null, null, "Test")
        assert patients.size() == 1 : "no patients should match"
        assert patients[0].givenName == "JOHN"
        
        // null in first name with conflicting middle does not find patient
        patients = PatientMatch.getPatient(null, "Z", "Test")
        assert !patients : "no patients should match"
        
    }
    */
    
    @Test
    void testSearchCache() {
        
        def patient = new PatientDemographics(accountId: DOCTOR_ID, givenName:"John", familyName:"Smith", sex: "Male")
        PatientMatch.cache(patient)
        
        // should not match on first name
        def result = PatientMatch.searchCache([givenName:"John"])
        println "result = " + result*.accountId
        assert result == null
        
        // should match on last name
        result = PatientMatch.searchCache([familyName:"Smith"])
        assert result.accountId == DOCTOR_ID
        
        // multiple attribute search
        result = PatientMatch.searchCache([givenName: "John",familyName:"Smith"])
        assert result?.accountId == DOCTOR_ID
        
        // should not be case sensitive
        result = PatientMatch.searchCache([familyName:"SMITH"])
        assert result?.accountId == DOCTOR_ID
        
        // should return null if no match 
        result = PatientMatch.searchCache([familyName:"SwITH"])
        assert result == null
        
        // Incorrect sex will not match
        result = PatientMatch.searchCache([familyName:"SMITH", sex:"F"])
        assert result == null
        
        result = PatientMatch.searchCache([familyName:"SMITH", sex:"Female"])
        assert result == null
        
        // sex match
        result = PatientMatch.searchCache([familyName:"SMITH", sex:"Male"])
        assert result?.accountId == DOCTOR_ID
    }
    
    @Test
    void testResolveContext() {
        
        def queryResult = null
        boolean called = false
        def p1 = new PatientDemographics(accountId: DOCTOR_ID, givenName:"John", familyName:"Smith", sex: "Male")
        def p2 = new PatientDemographics(accountId: USER1_ID, givenName:"Jane", familyName:"Smith", sex: "Female")
        
        PatientMatch.contextManager = [ 
                getQueryService:  { return { gn, mn, ln, sex ->
                    called = true
                    println "Returning query result " + queryResult
                    return queryResult ? [queryResult] as PatientDemographics[] : null
		        } as GroupPatientQueryService }
        ] as ContextManager
        
        // No patients, should call service but return null
        def baseCtx = new ContextState(cxpHost:"some.host.com", id: 102)
        def result = PatientMatch.resolveContextState("Jane", null, "Smith", "F", baseCtx)
        assert result == null
        assert called
        println "---------------1-----------------"
        
        // Add patient jane
        queryResult = p2; called = false
        result = PatientMatch.resolveContextState("Jane", null, "Smith", "F", baseCtx)
        assert called
        assert result
        assert result.cxpHost == baseCtx.cxpHost
        assert result.id != baseCtx.id
        
        println "---------------2-----------------"
        
        // Query again does not call query service
        queryResult = p2; called = false
        result = PatientMatch.resolveContextState("Jane", null, "Smith", "F", baseCtx)
        assert !called
        assert result
        assert result.cxpHost == baseCtx.cxpHost
        assert result.id != baseCtx.id
        
        println "---------------3-----------------"
        
        // Query with mismatch does call query service
        queryResult = p2; called = false
        result = PatientMatch.resolveContextState("Jane", null, "Smith", "M", baseCtx)
        assert called
        assert result
        assert result.cxpHost == baseCtx.cxpHost
        assert result.id != baseCtx.id
         
        println "---------------4-----------------"
        
        // Query with mismatch + no patient match does call query service
        queryResult = null; called = false
        result = PatientMatch.resolveContextState("Jane", null, "Smythe", "F", baseCtx)
        assert called
        assert !result
      }
}
