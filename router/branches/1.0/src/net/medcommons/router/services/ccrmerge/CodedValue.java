package net.medcommons.router.services.ccrmerge;

/**
 * Represents a coded value:
 * &lt;Code&gt;
 *     &lt;Value&gt;244.9&lt;/Value&gt;
 *     &lt;CodingSystem&gt;ICD9CM&lt;/CodingSystem&gt;
 *     &lt;Version&gt;2004&lt;/Version&gt;
 *  &lt;/Code&gt;
 *
 * @author sdoyle
 *
 */
public class CodedValue {
	CodingSystem codingSystem;
	String value;
	
	public CodedValue(CodingSystem codingSystem, String value){
		this.codingSystem = codingSystem;
		this.value = value;
	}
	/**
	 * Returns true if the coded value matches the 
	 * current one. In the future the matches might be more
	 * fuzzy (different versions of ICD9, perhaps there might
	 * be dictionary lookups? Don't know).
	 * 
	 * 
	 * @param codedValue
	 * @return
	 */
	public boolean matches(CodedValue codedValue){
		boolean match = false;
		if (codedValue.codingSystem == codingSystem){
			if (value.equalsIgnoreCase(codedValue.value))
				match = true;
		}
		return(match);
	}
	
	public String toString(){
		return new String("CodedValue[" + codingSystem + ", " + value + "]");
	}
}
