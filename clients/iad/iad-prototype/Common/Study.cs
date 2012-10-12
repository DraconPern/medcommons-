using System;

namespace MedCommons
{
	/// <summary>
	/// Summary description for Study.
	/// </summary>
	public class Study
	{
		public Study(string patientId,string patientName,
			DateTime modifiedTimeStamp,string description,string status)
		{
			this.id = patientId;
			this.patientName = patientName;
			this.timeStamp = timeStamp;
			this.status = status;			
		}

		public Study(ConnectionConfiguration config, string guid)
		{
			this.id = guid.Substring(0,12);
			this.guid = guid;
			this.patientName = null;;
			this.description = null;
			this.timeStamp = System.DateTime.Now;
			this.status = "New";
			this.config = config;
		}

		public Study()
		{
		}

		public Study(Study other) {
			this.id = other.id;
			this.guid = other.guid;
			this.patientName = other.patientName;
			this.timeStamp = other.timeStamp;
			this.status = other.status;
			this.description = other.description;
		}

		public override string ToString()
		{
			return id + "|" + patientName + "|" + timeStamp + "|" + description + "|" + status;
		}

		
		public string id;
		public string guid;
		public string patientName;
		public DateTime timeStamp;
		public string description;
		public string status;
		public string studyUID;
	

		/// <summary>
		/// The connection that this study is linked to
		/// </summary>
		private ConnectionConfiguration config;

		public ConnectionConfiguration Config {
			get {
				return this.config;
			}
		}
	}
}
