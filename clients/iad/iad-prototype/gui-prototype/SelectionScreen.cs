using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using System.Threading;
using System.Diagnostics;
using MedCommons;
using Microsoft.Win32;
using System.Security.Permissions;

namespace gui_prototype
{
	/// <summary>
	/// Summary description for SelectionScreenForm.
	/// </summary>
	public class SelectionScreenForm : System.Windows.Forms.Form
	{
		private System.Windows.Forms.ComboBox patientSearchKey;
		private System.Windows.Forms.ComboBox idSearchKey;
		private System.Windows.Forms.ComboBox dateSearchKey;
		private System.Windows.Forms.ComboBox statusSearchKey;
		private System.Windows.Forms.ComboBox descriptionSearchKey;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.Label label3;
		private System.Windows.Forms.Label label4;
		private System.Windows.Forms.Label label5;
		private System.Windows.Forms.Label label6;
		private System.Windows.Forms.Label label8;
		private System.Windows.Forms.ListView searchResults;
		private System.Windows.Forms.ColumnHeader header1;
		private System.Windows.Forms.ColumnHeader header2;
		private System.Windows.Forms.ColumnHeader header3;
		private System.Windows.Forms.ColumnHeader header4;
		private System.Windows.Forms.ColumnHeader header5;
		private System.Windows.Forms.ListView selectedItems;
		private System.Windows.Forms.ColumnHeader columnHeader1;
		private System.Windows.Forms.ColumnHeader columnHeader2;
		private System.Windows.Forms.ColumnHeader columnHeader3;
		private System.Windows.Forms.ColumnHeader columnHeader4;
		private System.Windows.Forms.ColumnHeader columnHeader5;
		private System.Windows.Forms.Button orderButton;
		private System.Windows.Forms.Button exit;
		private System.Windows.Forms.PictureBox pictureBox1;
		private System.Windows.Forms.Panel selectionPanel;
		private System.Windows.Forms.Label searchResultsLabel;
		private System.Windows.Forms.Label selectedItemsLabel;


		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		/// <summary>
		/// The current search criteria
		/// </summary>
		SearchCriteria searchCriteria;

		/// <summary>
		/// The current search results
		/// </summary>
		SearchResult searchResult;

		/// <summary>
		/// Thread that asynchronously polls for changes in the router
		/// </summary>
		Thread routerUpdateThread;

		/// <summary>
		/// Hidden items, filtered by search criteria
		/// </summary>
		private ArrayList hidden = new ArrayList(20);
		private System.Windows.Forms.ComboBox locationComboBox;

		/// <summary>
		/// Whether we will exit when this screen is closed.
		/// </summary>
		private bool exitOnClose = false;

		/// <summary>
		/// Constructs a new SelectionScreenForm
		/// </summary>
		public SelectionScreenForm(bool exitOnClose)
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			// Adjust height
			this.Height = Screen.PrimaryScreen.WorkingArea.Height - 30;

			// Position just near the top of the screen
			this.Location = new Point(this.Location.X, 10);

			// Layout the panels how we like them
			this.SizePanels();

			// Initialize search results	
			this.searchCriteria = new SearchCriteria();

			// Add the list of connections to the Location dropdown
			this.InitializeLocations();

			this.exitOnClose = exitOnClose;
		}

		protected void InitializeLocations()
		{
			ArrayList connections = ConnectionConfiguration.loadAll();
			this.locationComboBox.Items.Add("All");
			foreach(ConnectionConfiguration connection in connections) 
			{
				this.locationComboBox.Items.Add(connection.ConnectionName);
			}

			RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\MedCommons");
			if((key != null) && (key.GetValue("Default Connection")!=null))
			{
				string defaultConnection = key.GetValue("Default Connection").ToString();
				this.locationComboBox.SelectedItem = defaultConnection;
			}
			else {
				this.locationComboBox.SelectedItem = "All";
			}

			this.searchCriteria.ConfigurationName = this.locationComboBox.SelectedItem.ToString();
		}

		protected void SizePanels() 
		{
			// Resize selection list to take excess space
			this.selectionPanel.Location = 
				new Point(this.selectionPanel.Location.X, this.Height - this.selectionPanel.Height - 30);

			this.searchResults.Height = 
				this.selectionPanel.Location.Y - this.searchResults.Location.Y - 10;
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			this.routerUpdateThread = null;

			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		/// <summary>
		/// Polls for updates to the selection screen
		/// </summary>
		public void UpdateStudies(ArrayList studies)
		{
			SearchResult newSearchResult = new SearchResult(new ArrayList());
			foreach(Study study in studies)
			{
				newSearchResult.studies.Add(study);
			}

			// For each study already found, see if we already have it,
			// and if not, add it to our list
			for(int i=0; i<newSearchResult.studies.Count; ++i) 
			{
				Study study = (Study)newSearchResult.studies[i];

				// If the item is already listed, just update it
				if(this.ContainsStudy(study)) 	{

				}
				else {
					if(this.searchCriteria.matches(study)) 
					{
						this.FillStudyData(study);
						this.searchResults.Items.Add(this.CreateListItem(study));
					}
					else {
						this.hidden.Add(this.CreateListItem(study));
					}
				}
			}
			this.searchResult = newSearchResult;
			this.searchResultsLabel.Text = 
				"Total:  " + this.searchResults.Items.Count + " Items";
				
				/* - "
				+ (this.searchResults.Items.Count * 635) + " Images - "
				+ (this.searchResults.Items.Count * 0.73) + " GB";
				*/
		}

		/// <summary>
		/// Returns true if this study already exists in the selection list
		/// </summary>
		/// <param name="searchFor"></param>
		/// <returns></returns>
		private bool ContainsStudy(Study searchFor) {			
			if (searchResult == null) {
			  return false;
			}

			for(int i=0; i<searchResult.studies.Count; ++i) 
			{
				Study study = (Study)searchResult.studies[i];
				if(study.guid.Equals(searchFor.guid))
				{
					return true;
				}
			}
			return false;
		}

		ListViewItem CreateListItem(Study study) {
			ListViewItem item = 
				new ListViewItem(  new string [] { 
					study.patientName, 
					study.id.ToString(), 
				    study.timeStamp.ToString(), 
					study.description, 
					study.status 
	         });
			item.Tag = study;
			return item;
		}

		/// <summary>
		/// Checks if the study has data and if not, attempts it to fill the
		/// data from the study's configured source router.
		/// </summary>
		/// <param name="study"></param>
		private void FillStudyData(Study study) 
		{
			try {
				MedCommonsStudyService.StudyServiceService studyService = 
					new MedCommonsStudyService.StudyServiceService();
				studyService.Url = study.Config.ServiceUrl;
				string data = studyService.retrieveStudyData(study.guid);
				string [] parts = data.Split('|');
				study.patientName = parts[2];
				study.id = parts[3];
				study.description = "Study of " + parts[2];
				study.studyUID = parts[1];			
			}
			catch(Exception ex) {
				study.patientName = "Unknown";
				study.description = "Error retrieving data";
				study.id = "Unknown";
			}
		}

		private void OnItemCheck(object sender, System.Windows.Forms.ItemCheckEventArgs e)
		{
			Study checkedStudy =
				(Study)this.searchResults.Items[e.Index].Tag;

			if (e.NewValue.Equals(CheckState.Unchecked)) 
			{
				for (int i=0; i<selectedItems.Items.Count; ++i) {
					Study study = (Study)selectedItems.Items[i].Tag;
					if (study.id.Equals(checkedStudy.id)) {
						this.selectedItems.Items.RemoveAt(i);
						break;
					}
				}
			}
			else 
				this.selectedItems.Items.Add(this.CreateListItem(checkedStudy));

			this.selectedItemsLabel.Text = 
				"Total:  " + this.selectedItems.Items.Count + " Items."; /* - "
				+ (this.selectedItems.Items.Count * 635) + " Images - "
				+ (this.selectedItems.Items.Count * 0.73) + " GB"; */

		}

		/// <summary>
		/// Checks that there is at least one item selected or displays an error message to the user otherwise.
		/// </summary>
		private bool CheckSelected() {
			// Set the selection string for first selected study on the clipboard
			if(this.selectedItems.Items.Count == 0) 
			{
				MessageBox.Show("Please select an item to perform this operation", "Error", System.Windows.Forms.MessageBoxButtons.OK, System.Windows.Forms.MessageBoxIcon.Exclamation);
				return false;
			}
			return true;
		}

		private void Refilter() {
			// Filter list according to patient
			for(int i=0; i<searchResults.Items.Count; ++i) 
			{
				Study study = (Study)searchResults.Items[i].Tag;
				if (!this.searchCriteria.matches(study)) 
				{
					ListViewItem item = searchResults.Items[i];
				    searchResults.Items.RemoveAt(i);
					this.hidden.Add(item);
					i=-1;
				}
			}

			// Iterate over hidden items and add back any that now belong
			for (int i=0; i<this.hidden.Count;++i) {
				ListViewItem item = (ListViewItem)this.hidden[i];
				Study study = (Study)item.Tag;
				if(this.searchCriteria.matches(study)) {
					this.hidden.RemoveAt(i);
					this.searchResults.Items.Add(item);
					i=0;
				}
			}
		}

		private void OnSearchCriteriaChanged(object sender, System.EventArgs e)
		{
			this.searchCriteria.keys.patientName = this.patientSearchKey.Text;
			this.searchCriteria.keys.id = this.idSearchKey.Text;
			this.searchCriteria.dateSearchKey = this.dateSearchKey.Text;
			this.searchCriteria.keys.description = this.descriptionSearchKey.Text;
			this.searchCriteria.keys.status = this.statusSearchKey.Text;
			this.searchCriteria.ConfigurationName = locationComboBox.SelectedItem.ToString();
			this.Refilter();	
		}

		private void exit_Click(object sender, System.EventArgs e)
		{
			// Save the connection to the registry
			RegistryPermission rp = new RegistryPermission(PermissionState.Unrestricted);
			rp.Assert();
			RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\MedCommons", true);
			if(key != null)
			{
				key.SetValue("Default Connection",this.locationComboBox.SelectedItem);
			}

			this.Close();
			if (exitOnClose)
			{
				Application.Exit();
			}
		}

		private void OnSizeChanged(object sender, System.EventArgs e)
		{
			this.SizePanels();
		}

		private void orderButton_Click(object sender, System.EventArgs e)
		{
			if (!this.CheckSelected()) {
				return;
			}

			// Gather the selected studies into an arraylist
			ArrayList selected = new ArrayList();
			foreach(ListViewItem item in this.selectedItems.Items)
			{
				selected.Add(item.Tag);
			}

			// TextViewer viewer = new TextViewer((Study)this.selectedItems.Items[0].Tag);
			Viewer viewer = new WADOViewer(selected);
			viewer.display();		
		}


		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(SelectionScreenForm));
			this.patientSearchKey = new System.Windows.Forms.ComboBox();
			this.idSearchKey = new System.Windows.Forms.ComboBox();
			this.dateSearchKey = new System.Windows.Forms.ComboBox();
			this.statusSearchKey = new System.Windows.Forms.ComboBox();
			this.descriptionSearchKey = new System.Windows.Forms.ComboBox();
			this.label1 = new System.Windows.Forms.Label();
			this.label2 = new System.Windows.Forms.Label();
			this.label3 = new System.Windows.Forms.Label();
			this.label4 = new System.Windows.Forms.Label();
			this.label5 = new System.Windows.Forms.Label();
			this.orderButton = new System.Windows.Forms.Button();
			this.label6 = new System.Windows.Forms.Label();
			this.searchResultsLabel = new System.Windows.Forms.Label();
			this.locationComboBox = new System.Windows.Forms.ComboBox();
			this.label8 = new System.Windows.Forms.Label();
			this.selectedItemsLabel = new System.Windows.Forms.Label();
			this.searchResults = new System.Windows.Forms.ListView();
			this.header1 = new System.Windows.Forms.ColumnHeader();
			this.header2 = new System.Windows.Forms.ColumnHeader();
			this.header3 = new System.Windows.Forms.ColumnHeader();
			this.header4 = new System.Windows.Forms.ColumnHeader();
			this.header5 = new System.Windows.Forms.ColumnHeader();
			this.selectedItems = new System.Windows.Forms.ListView();
			this.columnHeader1 = new System.Windows.Forms.ColumnHeader();
			this.columnHeader2 = new System.Windows.Forms.ColumnHeader();
			this.columnHeader3 = new System.Windows.Forms.ColumnHeader();
			this.columnHeader4 = new System.Windows.Forms.ColumnHeader();
			this.columnHeader5 = new System.Windows.Forms.ColumnHeader();
			this.exit = new System.Windows.Forms.Button();
			this.pictureBox1 = new System.Windows.Forms.PictureBox();
			this.selectionPanel = new System.Windows.Forms.Panel();
			this.selectionPanel.SuspendLayout();
			this.SuspendLayout();
			// 
			// patientSearchKey
			// 
			this.patientSearchKey.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.patientSearchKey.Location = new System.Drawing.Point(40, 112);
			this.patientSearchKey.Name = "patientSearchKey";
			this.patientSearchKey.Size = new System.Drawing.Size(128, 24);
			this.patientSearchKey.TabIndex = 2;
			this.patientSearchKey.TextChanged += new System.EventHandler(this.OnSearchCriteriaChanged);
			// 
			// idSearchKey
			// 
			this.idSearchKey.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.idSearchKey.Location = new System.Drawing.Point(176, 112);
			this.idSearchKey.Name = "idSearchKey";
			this.idSearchKey.Size = new System.Drawing.Size(128, 24);
			this.idSearchKey.TabIndex = 2;
			this.idSearchKey.TextChanged += new System.EventHandler(this.OnSearchCriteriaChanged);
			// 
			// dateSearchKey
			// 
			this.dateSearchKey.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.dateSearchKey.Location = new System.Drawing.Point(312, 112);
			this.dateSearchKey.Name = "dateSearchKey";
			this.dateSearchKey.Size = new System.Drawing.Size(128, 24);
			this.dateSearchKey.TabIndex = 2;
			this.dateSearchKey.TextChanged += new System.EventHandler(this.OnSearchCriteriaChanged);
			// 
			// statusSearchKey
			// 
			this.statusSearchKey.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.statusSearchKey.Location = new System.Drawing.Point(584, 112);
			this.statusSearchKey.Name = "statusSearchKey";
			this.statusSearchKey.Size = new System.Drawing.Size(128, 24);
			this.statusSearchKey.TabIndex = 2;
			this.statusSearchKey.TextChanged += new System.EventHandler(this.OnSearchCriteriaChanged);
			// 
			// descriptionSearchKey
			// 
			this.descriptionSearchKey.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.descriptionSearchKey.Location = new System.Drawing.Point(448, 112);
			this.descriptionSearchKey.Name = "descriptionSearchKey";
			this.descriptionSearchKey.Size = new System.Drawing.Size(128, 24);
			this.descriptionSearchKey.TabIndex = 2;
			this.descriptionSearchKey.TextChanged += new System.EventHandler(this.OnSearchCriteriaChanged);
			// 
			// label1
			// 
			this.label1.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label1.Location = new System.Drawing.Point(40, 88);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(100, 16);
			this.label1.TabIndex = 3;
			this.label1.Text = "Patient";
			// 
			// label2
			// 
			this.label2.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label2.Location = new System.Drawing.Point(184, 88);
			this.label2.Name = "label2";
			this.label2.Size = new System.Drawing.Size(100, 16);
			this.label2.TabIndex = 3;
			this.label2.Text = "Id";
			// 
			// label3
			// 
			this.label3.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label3.Location = new System.Drawing.Point(320, 88);
			this.label3.Name = "label3";
			this.label3.Size = new System.Drawing.Size(100, 16);
			this.label3.TabIndex = 3;
			this.label3.Text = "Date/Time";
			// 
			// label4
			// 
			this.label4.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label4.Location = new System.Drawing.Point(456, 88);
			this.label4.Name = "label4";
			this.label4.Size = new System.Drawing.Size(100, 16);
			this.label4.TabIndex = 3;
			this.label4.Text = "Description";
			// 
			// label5
			// 
			this.label5.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label5.Location = new System.Drawing.Point(592, 88);
			this.label5.Name = "label5";
			this.label5.Size = new System.Drawing.Size(100, 16);
			this.label5.TabIndex = 3;
			this.label5.Text = "Status";
			// 
			// orderButton
			// 
			this.orderButton.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
			this.orderButton.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.orderButton.Location = new System.Drawing.Point(200, 120);
			this.orderButton.Name = "orderButton";
			this.orderButton.Size = new System.Drawing.Size(136, 24);
			this.orderButton.TabIndex = 4;
			this.orderButton.Text = "ORDER";
			this.orderButton.Click += new System.EventHandler(this.orderButton_Click);
			// 
			// label6
			// 
			this.label6.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label6.Location = new System.Drawing.Point(0, 24);
			this.label6.Name = "label6";
			this.label6.Size = new System.Drawing.Size(100, 16);
			this.label6.TabIndex = 3;
			this.label6.Text = "Selection:";
			// 
			// searchResultsLabel
			// 
			this.searchResultsLabel.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.searchResultsLabel.Location = new System.Drawing.Point(0, 0);
			this.searchResultsLabel.Name = "searchResultsLabel";
			this.searchResultsLabel.Size = new System.Drawing.Size(296, 16);
			this.searchResultsLabel.TabIndex = 6;
			this.searchResultsLabel.Text = "Total:  0 Items - 0 Images - 0.0 GB";
			// 
			// locationComboBox
			// 
			this.locationComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
			this.locationComboBox.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.locationComboBox.Location = new System.Drawing.Point(40, 48);
			this.locationComboBox.Name = "locationComboBox";
			this.locationComboBox.Size = new System.Drawing.Size(208, 24);
			this.locationComboBox.TabIndex = 2;
			this.locationComboBox.SelectedValueChanged += new System.EventHandler(this.OnSearchCriteriaChanged);
			// 
			// label8
			// 
			this.label8.Font = new System.Drawing.Font("Arial Black", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.label8.Location = new System.Drawing.Point(40, 16);
			this.label8.Name = "label8";
			this.label8.Size = new System.Drawing.Size(100, 16);
			this.label8.TabIndex = 3;
			this.label8.Text = "Location";
			// 
			// selectedItemsLabel
			// 
			this.selectedItemsLabel.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.selectedItemsLabel.Location = new System.Drawing.Point(8, 160);
			this.selectedItemsLabel.Name = "selectedItemsLabel";
			this.selectedItemsLabel.Size = new System.Drawing.Size(296, 16);
			this.selectedItemsLabel.TabIndex = 6;
			this.selectedItemsLabel.Text = "Selected:  0 Item - 0 Images - 0.0 GB";
			// 
			// searchResults
			// 
			this.searchResults.BackColor = System.Drawing.Color.WhiteSmoke;
			this.searchResults.CheckBoxes = true;
			this.searchResults.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
																							this.header1,
																							this.header2,
																							this.header3,
																							this.header4,
																							this.header5});
			this.searchResults.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.searchResults.Location = new System.Drawing.Point(32, 160);
			this.searchResults.Name = "searchResults";
			this.searchResults.Size = new System.Drawing.Size(696, 336);
			this.searchResults.TabIndex = 7;
			this.searchResults.View = System.Windows.Forms.View.Details;
			this.searchResults.ItemCheck += new System.Windows.Forms.ItemCheckEventHandler(this.OnItemCheck);
			// 
			// header1
			// 
			this.header1.Text = "PATIENT";
			this.header1.Width = 145;
			// 
			// header2
			// 
			this.header2.Text = "ID";
			this.header2.Width = 91;
			// 
			// header3
			// 
			this.header3.Text = "DATE/TIME";
			this.header3.Width = 157;
			// 
			// header4
			// 
			this.header4.Text = "DESCRIPTION";
			this.header4.Width = 195;
			// 
			// header5
			// 
			this.header5.Text = "STATUS";
			this.header5.Width = 82;
			// 
			// selectedItems
			// 
			this.selectedItems.BackColor = System.Drawing.Color.WhiteSmoke;
			this.selectedItems.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
																							this.columnHeader1,
																							this.columnHeader2,
																							this.columnHeader3,
																							this.columnHeader4,
																							this.columnHeader5});
			this.selectedItems.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.selectedItems.HeaderStyle = System.Windows.Forms.ColumnHeaderStyle.None;
			this.selectedItems.Location = new System.Drawing.Point(0, 56);
			this.selectedItems.MultiSelect = false;
			this.selectedItems.Name = "selectedItems";
			this.selectedItems.Size = new System.Drawing.Size(696, 56);
			this.selectedItems.TabIndex = 8;
			this.selectedItems.View = System.Windows.Forms.View.Details;
			// 
			// columnHeader1
			// 
			this.columnHeader1.Text = "PATIENT";
			this.columnHeader1.Width = 200;
			// 
			// columnHeader2
			// 
			this.columnHeader2.Text = "ID";
			this.columnHeader2.Width = 116;
			// 
			// columnHeader3
			// 
			this.columnHeader3.Text = "DATE/TIME";
			this.columnHeader3.Width = 134;
			// 
			// columnHeader4
			// 
			this.columnHeader4.Text = "DESCRIPTION";
			this.columnHeader4.Width = 134;
			// 
			// columnHeader5
			// 
			this.columnHeader5.Text = "STATUS";
			this.columnHeader5.Width = 87;
			// 
			// exit
			// 
			this.exit.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
			this.exit.Font = new System.Drawing.Font("Arial", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
			this.exit.Location = new System.Drawing.Point(352, 120);
			this.exit.Name = "exit";
			this.exit.Size = new System.Drawing.Size(136, 24);
			this.exit.TabIndex = 4;
			this.exit.Text = "EXIT";
			this.exit.Click += new System.EventHandler(this.exit_Click);
			// 
			// pictureBox1
			// 
			this.pictureBox1.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox1.Image")));
			this.pictureBox1.Location = new System.Drawing.Point(432, 8);
			this.pictureBox1.Name = "pictureBox1";
			this.pictureBox1.Size = new System.Drawing.Size(288, 72);
			this.pictureBox1.TabIndex = 9;
			this.pictureBox1.TabStop = false;
			// 
			// selectionPanel
			// 
			this.selectionPanel.Controls.Add(this.exit);
			this.selectionPanel.Controls.Add(this.label6);
			this.selectionPanel.Controls.Add(this.selectedItemsLabel);
			this.selectionPanel.Controls.Add(this.selectedItems);
			this.selectionPanel.Controls.Add(this.orderButton);
			this.selectionPanel.Controls.Add(this.searchResultsLabel);
			this.selectionPanel.Location = new System.Drawing.Point(32, 520);
			this.selectionPanel.Name = "selectionPanel";
			this.selectionPanel.Size = new System.Drawing.Size(712, 208);
			this.selectionPanel.TabIndex = 10;
			// 
			// SelectionScreenForm
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.BackColor = System.Drawing.Color.White;
			this.ClientSize = new System.Drawing.Size(760, 781);
			this.ControlBox = false;
			this.Controls.Add(this.selectionPanel);
			this.Controls.Add(this.pictureBox1);
			this.Controls.Add(this.searchResults);
			this.Controls.Add(this.label1);
			this.Controls.Add(this.patientSearchKey);
			this.Controls.Add(this.idSearchKey);
			this.Controls.Add(this.dateSearchKey);
			this.Controls.Add(this.statusSearchKey);
			this.Controls.Add(this.descriptionSearchKey);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.label3);
			this.Controls.Add(this.label4);
			this.Controls.Add(this.label5);
			this.Controls.Add(this.locationComboBox);
			this.Controls.Add(this.label8);
			this.ForeColor = System.Drawing.SystemColors.ControlText;
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.MaximizeBox = false;
			this.MinimizeBox = false;
			this.Name = "SelectionScreenForm";
			this.Text = "MedCommons";
			this.SizeChanged += new System.EventHandler(this.OnSizeChanged);
			this.selectionPanel.ResumeLayout(false);
			this.ResumeLayout(false);

		}
		#endregion

	}
}
