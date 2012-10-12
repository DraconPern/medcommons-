using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using mshtml;
using MedCommons;

namespace gui_prototype
{
	/// <summary>
	/// Summary description for Viewer.
	/// </summary>
	public class TextViewer : System.Windows.Forms.Form, Viewer
	{
		private AxSHDocVw.AxWebBrowser webBrowser;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		/// <summary>
		/// The study that this viewer is showing
		/// </summary>
		Study study;

		public TextViewer(Study study)
		{
			this.study = study;

			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			object url = "about:blank";
			object flags = 0;
			object postData = "";
			object targetFrame = "";
			object headers = "";
			this.webBrowser.Navigate2(ref url, ref flags, ref targetFrame,  ref postData , ref headers);

			IHTMLDocument2 doc = (IHTMLDocument2)this.webBrowser.Document;

			while (doc.body == null) {
				Application.DoEvents();
			}

			doc.body.innerHTML = "<p>Loading...</p>";

			if(study.guid != null) {
				MedCommonsStudyService.StudyServiceService studyService = 
					new MedCommonsStudyService.StudyServiceService();

				string data = studyService.retrieveStudyData(this.study.guid);

				string content = "<pre>Study ID:  " + study.id + "\n\n" 
					+ "Description: " + study.description 
					+ "\n\nData:\n\n" + data
					+ "</pre>";

				doc.body.innerHTML = content;
			}
			else {
				string content = "<pre>Study ID:  " + study.id + "\n\n" + "Description: " + study.description 
					+ "</pre>";
				doc.body.innerHTML = content;
			}

		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if(components != null)
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(TextViewer));
			this.webBrowser = new AxSHDocVw.AxWebBrowser();
			((System.ComponentModel.ISupportInitialize)(this.webBrowser)).BeginInit();
			this.SuspendLayout();
			// 
			// webBrowser
			// 
			this.webBrowser.Dock = System.Windows.Forms.DockStyle.Fill;
			this.webBrowser.Enabled = true;
			this.webBrowser.Location = new System.Drawing.Point(0, 0);
			this.webBrowser.OcxState = ((System.Windows.Forms.AxHost.State)(resources.GetObject("webBrowser.OcxState")));
			this.webBrowser.Size = new System.Drawing.Size(656, 733);
			this.webBrowser.TabIndex = 0;
			// 
			// Viewer
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(656, 733);
			this.Controls.Add(this.webBrowser);
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.Name = "Viewer";
			this.Text = "Viewer";
			((System.ComponentModel.ISupportInitialize)(this.webBrowser)).EndInit();
			this.ResumeLayout(false);

		}
		#endregion

		private void axMsie1_Enter(object sender, System.EventArgs e)
		{
		
		}

		public void display() 
		{
			this.ShowDialog();
		}
	}
}
