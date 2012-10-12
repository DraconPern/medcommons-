package net.medcommons.modules.services.interfaces;

import java.io.Serializable;

/**
 * <p>Mapping for table practice_patient</p>
 */
public class PracticePatient implements Serializable {

	/**
	 * Primary key
	 */
	private PracticePatientPK practicePatientPK;

	/**
	 * Get the primary key
	 */
	public PracticePatientPK getPracticePatientPK() {
		return this.practicePatientPK;
	}
	
	/**
	 * set the primary key
	 */
	public void setPracticePatientPK(PracticePatientPK practicePatientPK) {
		this.practicePatientPK = practicePatientPK;
	}
		
	/**
	 * <p>Composite primary key for table practice_patient</p>
 	 */
	public static class PracticePatientPK implements Serializable {

		/**
		 * Attribute ppPracticeId
		 */
		private int practiceId;

		/**
		 * Attribute ppName
		 */
		private String name;

		/**
		 * Attribute ppAccid
		 */
		private long accid;

		/**
		 * Return ppPracticeId
		 */
		public int getPracticeId() {
			return practiceId;
		}

		/**
		 * @param ppPracticeId new value for ppPracticeId 
		 */
		public void setPracticeId(int ppPracticeId) {
			this.practiceId = ppPracticeId;
		}
		/**
		 * Return ppName
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param ppName new value for ppName 
		 */
		public void setName(String ppName) {
			this.name = ppName;
		}
		/**
		 * Return ppAccid
		 */
		public long getAccid() {
			return accid;
		}

		/**
		 * @param ppAccid new value for ppAccid 
		 */
		public void setAccid(long ppAccid) {
			this.accid = ppAccid;
		}
	}
}