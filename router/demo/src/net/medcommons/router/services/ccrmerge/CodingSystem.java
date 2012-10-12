package net.medcommons.router.services.ccrmerge;

/**
 * Defines the universe of coding systems that are understood.
 * 
 * The main purpose of this class to narrow down the comparisons
 * between the different synonyms for the coding systems. Apparently
 * there are differences in how different standards define the codes.
 * UMLS defines ICD-9 as "ICD9CM" but HL7 defines it as "ICD9-CM".
 * 
 * @author sdoyle
 *
 */
public enum CodingSystem {
	ICD9CM(new String[]{"ICD9CM", "ICD9-CM"},"2004" ),
	CPT(new String[]{"CPT"}, null),
	UMLS(new String[]{"UMLS"}, "2005AC"),
	LNC(new String[]{"LNC", "LOINC"}, null),
	RXNORM(new String[]{"RXNORM"}, null),
	HL7V25(new String[]{"HL7V2.5"}, null),
	SNOMEDCT(new String[]{"SNOMEDCT"}, "2005"),
	UNKNOWN(null, null);
	
	
	String [] synonyms;
	String version;
	CodingSystem(String[] synonyms, String version){
		this.synonyms = synonyms;
	}
	
	public String toString(){
		if (version == null)
			return "CodingSystem[" + this.name() + "]";
		else
			return "CodingSystem[" + this.name() + "," + version + "]";
	}
	/**
	 * Returns the correct CodingSystem for a given code 
	 * and version.
	 * 
	 * If the CodingSystem has a 'null' version then it matches
	 * against all versions.
	 * 
	 * @param code
	 * @param version
	 * @return
	 */
	public static CodingSystem factory(String code, String version) {
		CodingSystem codeType = null;
		for (CodingSystem candidate : CodingSystem.values()) {
			if (!candidate.equals(UNKNOWN)) {
				String[] s = candidate.synonyms;
				for (int i = 0; i < s.length; i++) {
					if (s[i].equalsIgnoreCase(code)) {
						if (candidate.version == null) {
							codeType = candidate;
							break;
						} else if (candidate.version.equalsIgnoreCase(version)) {
							codeType = candidate;
							break;
						}
					}

				}
			}
		}
		if (codeType == null) {
			codeType = UNKNOWN;
		}
		return (codeType);
	}
}
