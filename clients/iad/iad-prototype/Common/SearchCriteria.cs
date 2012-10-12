using System;

namespace MedCommons
{
	/// <summary>
	/// Summary description for SearchCriteria.
	/// </summary>
	public class SearchCriteria
	{
		// Hacky, for convenience we keep the search keys as a study object themselves.
		public Study keys = new Study();

		// date doesn't fit with the others because it is not compatible with arbitrary strings
		public string dateSearchKey = null;

		private string configName;

		public string ConfigurationName {
			get {
				return configName;
			}

			set {
				configName = value;
			}
		}

		public SearchCriteria()
		{
		}

		/// <summary>
		/// Returns true if the given study matches all of this SearchCriteria's search keys
		/// </summary>
		/// <param name="study">The study to compare</param>
		/// <returns></returns>
		public bool matches(Study study) {
			return this.match(this.keys.patientName,study.patientName)
				&& this.match(this.keys.id,study.id)
				&& this.match(this.dateSearchKey,study.timeStamp)
				&& this.match(this.keys.description,study.description)
				&& this.match(this.keys.status,study.status)
				&& ((this.configName == "All") || this.match(this.configName,study.Config.ConnectionName))
				;
		}

		private bool match(object key, object value) {
			return ((key==null) || (key.ToString().Length==0) || (value.ToString().IndexOf(key.ToString())>=0));
		}
	}
}
