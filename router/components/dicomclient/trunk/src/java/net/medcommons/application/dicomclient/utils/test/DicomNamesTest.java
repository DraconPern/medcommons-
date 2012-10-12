package net.medcommons.application.dicomclient.utils.test;

import net.medcommons.application.dicomclient.test.BaseTest;
import net.medcommons.modules.utils.dicom.DicomNameParser;

public class DicomNamesTest extends BaseTest {

	public void testParseGivenName(){
		DicomNameParser dicomNameParser = new DicomNameParser();

		 for (TestNames t : TestNames.values()){
			// System.err.println("given name = '" + dicomNameParser.givenName(t.dicomName) + "'");
			 assertEquals(t.dicomName, dicomNameParser.givenName(t.dicomName), t.givenName);
		 }
	}
	public void testParseFamilyName(){
		DicomNameParser dicomNameParser = new DicomNameParser();
		 for (TestNames t : TestNames.values()){
			 //System.err.println("family name = '" + dicomNameParser.familyName(t.dicomName) + "'");
		      assertEquals(t.dicomName, dicomNameParser.familyName(t.dicomName), t.familyName);
		 }
	}
	public void testParseMiddleName(){
		DicomNameParser dicomNameParser = new DicomNameParser();
		 for (TestNames t : TestNames.values()){
			 //System.err.println("middle name = '" + dicomNameParser.middleName(t.dicomName) + "'");
		      assertEquals(t.dicomName, dicomNameParser.middleName(t.dicomName), t.middleName);
		 }
	}
	/**
	 * Names extracted from real DICOM files.
	 * Note the following variations:
	 * <ul>
	 * <li> No middle name
	 * <li> Middle initial
	 * <li> Ends with ^
	 * <li> Uses blank as a delimiter.
	 * </ul>
	 * @author mesozoic
	 *
	 */
	enum TestNames {
		// Source - Gordon Harris (3D study);
		MGH_ANONYMIZED_DOCTOR1("CAMBRIA^RICHARD^P", "RICHARD", "P", "CAMBRIA"),
		MGH_ANONYMIZED_DOCTOR2("PERRY^WAYNE^ROBERT", "WAYNE", "ROBERT","PERRY"),
		MGH_ANONYMIZED_PATIENT_NAME("Alpha  Test-38615", "Test-38615", null, "Alpha"),
		MGH_GE_CR_DOCTOR3("MUDGAL^CHAITANYA^","CHAITANYA" ,null,"MUDGAL"),
		EMERSON_CR("Belanich^Susan", "Susan", null, "Belanich"),
		RIPV("VAN WINKLE^RIP^","RIP" ,null,"VAN WINKLE"),
		NUTHOUSE("NUTHOUSE^ERASMUS^TIBERIOUS^","ERASMUS" ,"TIBERIOUS","NUTHOUSE"),
		MGH_GE_CR_PATIENT("BATCHELDER^KEITH^F", "KEITH", "F","BATCHELDER"),
		MT_AUBURN_CR_PATIENT("DOYLE,SEAN", "SEAN", null,"DOYLE");
		String dicomName;
		String givenName;
		String middleName;
		String familyName;

		TestNames(String dicomName, String givenName, String middleName, String familyName){
			this.dicomName = dicomName;
			this.givenName = givenName;
			this.middleName = middleName;
			this.familyName = familyName;
		}

	}
}
