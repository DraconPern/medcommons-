using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using mshtml;
using MedCommons;
using System.Diagnostics;

namespace gui_prototype
{
	/// <summary>
	/// Summary description for Viewer.
	/// </summary>
	public class WADOViewer : System.Windows.Forms.Form, Viewer
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		/// <summary>
		/// The Internet Explorer browser object
		/// </summary>
		private AxSHDocVw.AxWebBrowser webBrowser;

		/// <summary>
		/// The studies that this viewer is showing
		/// </summary>
		ArrayList studies = null;

		public WADOViewer(ArrayList studies)
		{
			this.studies = studies;
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();
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
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(WADOViewer));
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
			// WADOViewer
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(656, 733);
			this.Controls.Add(this.webBrowser);
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.Name = "WADOViewer";
			this.Text = "Viewer";
			this.WindowState = System.Windows.Forms.FormWindowState.Maximized;
			((System.ComponentModel.ISupportInitialize)(this.webBrowser)).EndInit();
			this.ResumeLayout(false);

		}
		#endregion

		public void display() 
		{
			Study study = (Study)this.studies[0];
			string url;
			if(System.Environment.GetEnvironmentVariable("MEDCOMMONS_NO_ORDER_FORMP") != null)
			{
				url = "http://" 
						+ study.Config.Host + ":" + study.Config.Port + "/router/SimpleViewer?studyUID=" 
						+ study.studyUID + "&fname=DICOMDIR&stylesheet=/SimpleViewer.xsl";
			}
			else
		    if(System.Environment.GetEnvironmentVariable("MEDCOMMONS_ORIGINAL_ORDER_FORM")==null)
			{
				String orderFormName = System.Environment.GetEnvironmentVariable("MEDCOMMONS_ORDER_FORM");
				if (orderFormName == null)
				{
					orderFormName = "TestForm.jsp";
				}

				url = "http://" 
					+ study.Config.Host + ":" + study.Config.Port + "/router/" + orderFormName + "?guid=" 
					+ study.guid
					+ "&user=" 
					+ study.Config.UserName.Replace(" ","+");  // HACK, replace space with + instead of url encoding (ahem)
			}
			else
			{
				String orderFormName = System.Environment.GetEnvironmentVariable("MEDCOMMONS_ORDER_FORM");
				if (orderFormName == null)
				{
					orderFormName = "TestForm.jsp";
				}

				url = "http://" 
					+ study.Config.Host + ":" + study.Config.Port + "/router/" + orderFormName + "?guid=" 
					+ study.guid
					+ "&name=" 
					+ study.Config.UserName.Replace(" ","+")  // HACK, replace space with + instead of url encoding (ahem)
					+ "&tracking=771EF&address=123%20Lucky%20St&state=MT&city=Butte&zip=83132&cardnumber=7817574478133225&amount=150.00&tax=12.00&charge=162.00&expiration=12/09&copyto=agropper@medcommons.org&comments=%20CERVICAL%20SPINE%20&history=%3cunknown%3e&signature1=MedCommons%20%3confile%3e&signature2=Joes%20Imaging%20Centres%20Inc%20%3confile%3e";
			}

			// Add guids parameters for each guid
			string guids = "";
			foreach(Study guidsStudy in this.studies)
			{
				guids += "&guids=" + guidsStudy.guid;
			}

			url += guids;

			Trace.WriteLine("Starting WADO viewer with URL: " + url);
			object urlObj = url;
			object flags = 0;
			object postData = "";
			object targetFrame = "";
			object headers = "";
			this.webBrowser.Navigate2(ref urlObj, ref flags, ref targetFrame,  ref postData , ref headers);

			this.ShowDialog();
		}

	}
}
