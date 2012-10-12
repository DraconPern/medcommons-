using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using System.Threading;
using Microsoft.Win32;
using System.Security.Permissions;
using System.Diagnostics;
using System.Net;

using MedCommons;

namespace gui_prototype
{

	/// <summary>
	/// Runs a System Tray app that monitors connection status and also allows display 
	/// of configuration information and also the Selection Screen.
	/// </summary>
	public class RouterMonitor : System.Windows.Forms.Form
	{
		public enum Status 
		{
			Connected,
			Connecting,
			NotConnected
		};

		/// <summary>
		/// Table of status of each connection
		/// </summary>
		private Hashtable statuses = new Hashtable(10);

		private System.Windows.Forms.NotifyIcon notificationIcon;
		private System.Windows.Forms.ContextMenu contextMenu;
		private System.Windows.Forms.MenuItem menuItemStatus;
		private System.Windows.Forms.MenuItem menuItemExit;
		private System.ComponentModel.IContainer components;

		/// <summary>
		/// If a router has been started from the RouterMonitor then it's process
		/// is stored here.
		/// </summary>
		Process routerProcess = null;

    /// <summary>
    /// If a database process has been started by the system monitor then
    /// the Process object is stored here.
    /// </summary>
    Process databaseProcess = null;

		/// <summary>
		/// Thread which occasionally switches the status around to simulate
		/// the connection coming and going
		/// </summary>
		Thread statusUpdateThread;

		/// <summary>
		/// The known connections to MedCommons.  These are read from the registry.
		/// </summary>
		private ArrayList configs = new ArrayList(10);

		/// <summary>
		/// 0 - no connections to anything
		/// 1 - connection to local router
		/// 2 - connection to LAN router
		/// 3 - connection to central
		/// </summary>
		int status;

		private System.Windows.Forms.ImageList imageList1;
		private System.Windows.Forms.Button buttonDelete;
		private System.Windows.Forms.Button buttonClose;
		private System.Windows.Forms.ColumnHeader headerConnection;
		private System.Windows.Forms.ColumnHeader headerRouter;
		private System.Windows.Forms.ColumnHeader columnStatus;
		private System.Windows.Forms.Button buttonAdd;
		private System.Windows.Forms.ListView routerList;
		private System.Windows.Forms.MenuItem menuItem1;

		/// <summary>
		/// Set to false when we quit so other components can exit safely
		/// </summary>
		bool running = true;

		/// <summary>
		/// The list of connections that we are polling
		/// </summary>
		private ArrayList connections = null;

		/// <summary>
		/// The one and only SelectionScreen Form.
		/// </summary>
		private static SelectionScreenForm selectionScreenForm;

		/// <summary>
		/// Thread that asynchronously polls for changes in routers
		/// </summary>
		Thread routerUpdateThread;

		/// <summary>
		/// The list of studies that we have found
		/// </summary>
		private ArrayList studies = new ArrayList(100);
		private System.Windows.Forms.Button configureButton;
		private System.Windows.Forms.MenuItem menuItemStartRouter;
    private System.Windows.Forms.MenuItem menuItemStopRouter;

		/// <summary>
		/// Service from which we will poll for studies
		/// </summary>
		MedCommonsStudyService.StudyServiceService studyService = 
			new MedCommonsStudyService.StudyServiceService();

		/// <summary>
		/// Creates the RouterMonitor
		/// </summary>
		public RouterMonitor(bool bShow)
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			if (!bShow)
			{
				this.notificationIcon.Visible = false;
				SelectionScreenForm selScreen = new SelectionScreenForm(true);
				selScreen.Show();
				selectionScreenForm = selScreen;
			}

			// Fill the connections from the registry
			this.refreshConnections();

			// Refresh the configurations from the registry
			this.connections = ConnectionConfiguration.loadAll();

			routerUpdateThread = new Thread(new ThreadStart(this.Poll));
			routerUpdateThread.Start();

      // start off in "bad" status
      this.status = 0;

      this.UpdateIcon();
		}


		/// <summary>
		/// Polls for updates to the selection screen
		/// </summary>
		void Poll()
		{
			int pollingInterval = 1000;
			while((this.routerUpdateThread != null) && this.running)
			{
				bool errors = false;
				SearchResult newSearchResult = new SearchResult(new ArrayList());

				// Poll each connection
				ArrayList newStudies = new ArrayList(100);
				foreach(ConnectionConfiguration config in this.connections) 
				{
					string url = config.ServiceUrl;
					Trace.WriteLine("Url = " + url);
					try 
					{
						// studyService.Url = url;
						// string[] guids = studyService.selectStudyGuids(config.UserName);

            HttpWebRequest httpWebRequest = 
              (HttpWebRequest) WebRequest.Create(url);

            WebResponse response = httpWebRequest.GetResponse();
            if (response != null) {
              this.statuses[config.ConnectionName] = Status.Connected;
            }
            else {
              this.statuses[config.ConnectionName] = Status.NotConnected;
            }

						/*if (guids != null)
						{
							foreach(string guid in guids) 
							{
								Trace.WriteLine(guid + " received");					
								if (guid != null)
								{
									newStudies.Add(new Study(config, guid));
								}
							}					
						}*/

					}
					catch(Exception ex) 
					{ 
						errors = true;
						this.statuses[config.ConnectionName] = Status.NotConnected;
						Trace.WriteLine("Unable to poll connection '" + config.ConnectionName + "': " + ex.Message);
					}
				}
				this.studies = newStudies;

				if (selectionScreenForm != null)
				{
					selectionScreenForm.UpdateStudies(studies);
				}

				if (errors)
					this.status = 0;
				else
					this.status = 3;

				if (this.running)
				{
					this.UpdateConnectionStatus();
					this.UpdateIcon();
				}
				Thread.Sleep(pollingInterval);
			}
			Trace.WriteLine("Router update thread exiting");			
		}

		private void UpdateConnectionStatus()
		{
			foreach(ListViewItem item in routerList.Items)
			{
				if (this.statuses[item.Text] != null)
				{
					if((Status)this.statuses[item.Text] == Status.Connected)
					{
						item.SubItems[2].Text = "Connected";
					}
					else
					{
						item.SubItems[2].Text = "Not Connected";
					}

				}
			}
		}

    /// <summary>
    /// Returns true if the local router is running
    /// </summary>
    public bool IsLocalRouterRunning() 
    {
      try 
      {      
        if (this.connections == null) 
        {
          return false;
        }

        foreach(ConnectionConfiguration config in this.connections) 
        {        
          if (config == null) 
          {
            continue;
          }

          if (config.Host == "localhost") 
          {
            if((Status)this.statuses[config.ConnectionName] == Status.Connected)
            {
              return true;
            }
          }
        }
      }
      catch(Exception ex) {
        // do nothing (!!)
      }
      return false;
    }

		private void refreshConnections() 
		{
			this.configs.Clear();

			ArrayList connections = ConnectionConfiguration.loadAll();

			foreach(ConnectionConfiguration config in connections)
			{
				this.configs.Add(config);
				this.routerList.Items.Add(this.createListViewItem(config));				
			}
		}

		public ListViewItem createListViewItem(ConnectionConfiguration config) 
		{
			ListViewItem item = new ListViewItem(config.ConnectionName);
			if(config.Type == "central")
				item.ImageIndex = 2;				
			else
			if(config.Type == "router")
				item.ImageIndex = 1;				
			else
				item.ImageIndex = 0;

			item.Tag = config;
			item.SubItems.Add(config.Host);
			item.SubItems.Add("Connected");			
			return item;
		}

		public void UpdateIcon() 
		{
			switch(status) {
			case 0:
   				this.notificationIcon.Icon = new Icon(this.GetType(), "bad.ico");
				this.notificationIcon.Text = "MedCommons - Unavailable";
				break;
			case 1:
				this.notificationIcon.Icon = new Icon(this.GetType(), "bad.ico");
				this.notificationIcon.Text = "MedCommons - Unavailable";
				break;
			default:
			case 2:
				this.notificationIcon.Icon = new Icon(this.GetType(), "localonly.ico");
				this.notificationIcon.Text = "MedCommons - Local";
				break;
			case 3:
				this.notificationIcon.Icon = new Icon(this.GetType(), "good.ico");
				this.notificationIcon.Text = "MedCommons - Connected";
				break;				
			}
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			this.running = false;

			if( disposing )
			{
				if (components != null) 
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
      this.components = new System.ComponentModel.Container();
      System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(RouterMonitor));
      this.notificationIcon = new System.Windows.Forms.NotifyIcon(this.components);
      this.contextMenu = new System.Windows.Forms.ContextMenu();
      this.menuItemStartRouter = new System.Windows.Forms.MenuItem();
      this.menuItemStatus = new System.Windows.Forms.MenuItem();
      this.menuItem1 = new System.Windows.Forms.MenuItem();
      this.menuItemExit = new System.Windows.Forms.MenuItem();
      this.imageList1 = new System.Windows.Forms.ImageList(this.components);
      this.routerList = new System.Windows.Forms.ListView();
      this.headerConnection = new System.Windows.Forms.ColumnHeader();
      this.headerRouter = new System.Windows.Forms.ColumnHeader();
      this.columnStatus = new System.Windows.Forms.ColumnHeader();
      this.buttonDelete = new System.Windows.Forms.Button();
      this.buttonClose = new System.Windows.Forms.Button();
      this.buttonAdd = new System.Windows.Forms.Button();
      this.configureButton = new System.Windows.Forms.Button();
      this.menuItemStopRouter = new System.Windows.Forms.MenuItem();
      this.SuspendLayout();
      // 
      // notificationIcon
      // 
      this.notificationIcon.ContextMenu = this.contextMenu;
      this.notificationIcon.Icon = ((System.Drawing.Icon)(resources.GetObject("notificationIcon.Icon")));
      this.notificationIcon.Text = "MedCommons";
      this.notificationIcon.Visible = true;
      this.notificationIcon.DoubleClick += new System.EventHandler(this.OnNotifyIconDoubleClick);
      // 
      // contextMenu
      // 
      this.contextMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
                                                                                this.menuItemStartRouter,
                                                                                this.menuItemStopRouter,
                                                                                this.menuItemStatus,
                                                                                this.menuItem1,
                                                                                this.menuItemExit});
      this.contextMenu.Popup += new System.EventHandler(this.contextMenu_Popup);
      // 
      // menuItemStartRouter
      // 
      this.menuItemStartRouter.Index = 0;
      this.menuItemStartRouter.Text = "Start Router";
      this.menuItemStartRouter.Click += new System.EventHandler(this.menuItemStartRouter_Click);
      // 
      // menuItemStatus
      // 
      this.menuItemStatus.Index = 2;
      this.menuItemStatus.Text = "Configure";
      this.menuItemStatus.Click += new System.EventHandler(this.menuItemStatus_Click);
      // 
      // menuItem1
      // 
      this.menuItem1.Index = 3;
      this.menuItem1.Text = "Selection Screen";
      this.menuItem1.Click += new System.EventHandler(this.menuItem1_Click);
      // 
      // menuItemExit
      // 
      this.menuItemExit.Index = 4;
      this.menuItemExit.Text = "Exit";
      this.menuItemExit.Click += new System.EventHandler(this.menuItemExit_Click);
      // 
      // imageList1
      // 
      this.imageList1.ImageSize = new System.Drawing.Size(16, 16);
      this.imageList1.ImageStream = ((System.Windows.Forms.ImageListStreamer)(resources.GetObject("imageList1.ImageStream")));
      this.imageList1.TransparentColor = System.Drawing.Color.Transparent;
      // 
      // routerList
      // 
      this.routerList.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
                                                                                 this.headerConnection,
                                                                                 this.headerRouter,
                                                                                 this.columnStatus});
      this.routerList.Dock = System.Windows.Forms.DockStyle.Top;
      this.routerList.FullRowSelect = true;
      this.routerList.LargeImageList = this.imageList1;
      this.routerList.Location = new System.Drawing.Point(0, 0);
      this.routerList.Name = "routerList";
      this.routerList.Size = new System.Drawing.Size(368, 240);
      this.routerList.SmallImageList = this.imageList1;
      this.routerList.TabIndex = 17;
      this.routerList.View = System.Windows.Forms.View.Details;
      this.routerList.ItemActivate += new System.EventHandler(this.OnItemActivate);
      // 
      // headerConnection
      // 
      this.headerConnection.Text = "Connection";
      this.headerConnection.Width = 150;
      // 
      // headerRouter
      // 
      this.headerRouter.Text = "Host";
      this.headerRouter.Width = 111;
      // 
      // columnStatus
      // 
      this.columnStatus.Text = "Status";
      this.columnStatus.Width = 90;
      // 
      // buttonDelete
      // 
      this.buttonDelete.Location = new System.Drawing.Point(96, 256);
      this.buttonDelete.Name = "buttonDelete";
      this.buttonDelete.Size = new System.Drawing.Size(64, 23);
      this.buttonDelete.TabIndex = 16;
      this.buttonDelete.Text = "Delete";
      this.buttonDelete.Click += new System.EventHandler(this.buttonDelete_Click);
      // 
      // buttonClose
      // 
      this.buttonClose.Location = new System.Drawing.Point(256, 256);
      this.buttonClose.Name = "buttonClose";
      this.buttonClose.Size = new System.Drawing.Size(64, 23);
      this.buttonClose.TabIndex = 16;
      this.buttonClose.Text = "Close";
      this.buttonClose.Click += new System.EventHandler(this.buttonClose_Click);
      // 
      // buttonAdd
      // 
      this.buttonAdd.Location = new System.Drawing.Point(16, 256);
      this.buttonAdd.Name = "buttonAdd";
      this.buttonAdd.Size = new System.Drawing.Size(64, 23);
      this.buttonAdd.TabIndex = 16;
      this.buttonAdd.Text = "Add...";
      this.buttonAdd.Click += new System.EventHandler(this.buttonAdd_Click);
      // 
      // configureButton
      // 
      this.configureButton.Location = new System.Drawing.Point(176, 256);
      this.configureButton.Name = "configureButton";
      this.configureButton.Size = new System.Drawing.Size(64, 23);
      this.configureButton.TabIndex = 16;
      this.configureButton.Text = "Configure";
      this.configureButton.Click += new System.EventHandler(this.configureButton_Click);
      // 
      // menuItemStopRouter
      // 
      this.menuItemStopRouter.Enabled = false;
      this.menuItemStopRouter.Index = 1;
      this.menuItemStopRouter.Text = "Stop Router";
      this.menuItemStopRouter.Click += new System.EventHandler(this.menuItem2_Click);
      // 
      // RouterMonitor
      // 
      this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
      this.ClientSize = new System.Drawing.Size(368, 293);
      this.Controls.Add(this.routerList);
      this.Controls.Add(this.buttonDelete);
      this.Controls.Add(this.buttonClose);
      this.Controls.Add(this.buttonAdd);
      this.Controls.Add(this.configureButton);
      this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
      this.Name = "RouterMonitor";
      this.ShowInTaskbar = false;
      this.Text = "Med Commons Connections";
      this.WindowState = System.Windows.Forms.FormWindowState.Minimized;
      this.Resize += new System.EventHandler(this.OnResize);
      this.Closed += new System.EventHandler(this.OnClosed);
      this.ResumeLayout(false);

    }
		#endregion

		private void OnResize(object sender, System.EventArgs e)
		{
			if (FormWindowState.Minimized == WindowState)
				Hide();
		}

		private void OnNotifyIconDoubleClick(object sender, System.EventArgs e)
		{
			Show();
			WindowState = FormWindowState.Normal;		
		}

		private void menuItemStatus_Click(object sender, System.EventArgs e)
		{
			// Update the connection configurations
			Show();
			WindowState = FormWindowState.Normal;
		}

		private void menuItemExit_Click(object sender, System.EventArgs e)
		{
      if(this.IsLocalRouterRunning()) {
        DialogResult result = 
          MessageBox.Show("Your local router is still running.  Do you want to stop it?", 
                          "MedCommons Router",
                           MessageBoxButtons.YesNo);
        if(result == DialogResult.Yes) {
          this.StopLocalRouter();
        }
      }

			this.running = false;
			this.Close();
		}

		private void OnClosed(object sender, System.EventArgs e)
		{
			this.running = false;
		}

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			// Depending on command line arguments either launch 
			// just the selection screen or show the router monitor as well
			String [] args = System.Environment.GetCommandLineArgs();
			if (args.Length>1)
			{
				if (args[1] == "-tray")
				{
					RouterMonitor routerMonitor = new RouterMonitor(true);

          if((args.Length > 2) && (args[2] == "-router"))
          {
            routerMonitor.StartRouter();
          }

					Application.Run(routerMonitor);
				}

			}
			else 
			{
				Application.Run(new RouterMonitor(false));
			}
		}

		private void buttonAdd_Click(object sender, System.EventArgs e)
		{
			ConnectionConfiguration newConnection = 
				new ConnectionConfiguration("New Connection", "host", "port","router",System.Environment.UserName,"");

			ConnectionConfigurationForm form = new ConnectionConfigurationForm(newConnection);
			form.ShowDialog();
			if (!form.Canceled)
			{
				this.configs.Add(newConnection);
				this.routerList.Items.Add(this.createListViewItem(newConnection));
				this.save();
				this.connections = ConnectionConfiguration.loadAll();
			}
		}

		private void buttonClose_Click(object sender, System.EventArgs e)
		{
			this.Hide();
		}

		private void OnItemActivate(object sender, System.EventArgs e)
		{
			this.ConfigureSelected();
		}

		private void save() 
		{
			foreach(ConnectionConfiguration conn in this.configs)
			{
				conn.save();
			}
		}

		private void buttonDelete_Click(object sender, System.EventArgs e)
		{
			RegistryPermission rp = new RegistryPermission(PermissionState.Unrestricted);
			rp.Assert();
			if (this.routerList.SelectedItems.Count == 0) 
			{
				return;
			}

			ConnectionConfiguration conn =
				(ConnectionConfiguration) this.routerList.SelectedItems[0].Tag;

			RegistryKey key = Registry.CurrentUser.OpenSubKey("Software\\MedCommons\\Connections",true);		
			if(key.OpenSubKey(conn.ConnectionName)!=null) 
			{
				key.DeleteSubKey(conn.ConnectionName);
			}
			this.routerList.Items.Remove(this.routerList.SelectedItems[0]);
			this.connections = ConnectionConfiguration.loadAll();
		}

		private void menuItem1_Click(object sender, System.EventArgs e)
		{
			selectionScreenForm = new SelectionScreenForm(false);
			selectionScreenForm.Show();	
		}

		private void configureButton_Click(object sender, System.EventArgs e)
		{
			this.ConfigureSelected();
		
		}

		private void ConfigureSelected() 
		{
			// If nothing selected, return
			if(this.routerList.SelectedItems.Count == 0) 
			{
				return;
			}

			ConnectionConfiguration conn =
				(ConnectionConfiguration) this.routerList.SelectedItems[0].Tag;

			ConnectionConfiguration newConn = new ConnectionConfiguration(conn.ConnectionName, conn.Host, conn.Port, conn.Type, conn.UserName, conn.Password);
			ConnectionConfigurationForm newConnForm = new ConnectionConfigurationForm(newConn);
			newConnForm.ShowDialog();

			if(!newConnForm.Canceled) 
			{
				this.routerList.Items.Remove(this.routerList.SelectedItems[0]);
				this.routerList.Items.Add(this.createListViewItem(newConn));
				this.configs.Remove(conn);
				this.configs.Add(newConn);
			}

			this.save();
			this.connections = ConnectionConfiguration.loadAll();
		}

		private void menuItemStartRouter_Click(object sender, System.EventArgs e)
		{
      StartRouter();
		}


    /// <summary>
    /// Causes the router to be started.  Shows a progress dialog and checks that the router
    /// has good status at the end.
    /// </summary>
    public void StartRouter() 
    {
      string workingDir = System.Environment.CurrentDirectory;

      // Start Derby

      /*String classPath = 
        "lib/db2jcc.jar;lib/db2jcc_license_c.jar;lib/derby-hibernate.jar;lib/derby.jar;lib/derbynet.jar;lib/derbytools.jar";

      this.databaseProcess = 
        this.RunProcess(workingDir + @"\Router\tomcat\router", "java.exe", 
          "-classpath " + classPath
        + "-Dderby.system.home=" + workingDir + @"\Router\jboss\server\router\data\derby"
        );
        
      */


      // Start the router
      this.routerProcess = 
        this.RunProcess(workingDir + @"\Router\tomcat", workingDir+ @"\Router\tomcat\bin\catalina.bat", "run");

      this.menuItemStopRouter.Enabled = true;
      this.menuItemStartRouter.Enabled = false;
      
      new StartRouterProgressForm(this).ShowDialog();

      if (this.IsLocalRouterRunning()) 
      {
        MessageBox.Show("Local Router Started!", "MedCommons");
      }
      else 
      {
        MessageBox.Show("Your Local Router did not appear to start correctly.  Please contact MedCommons for technical support.", "Problem");
      }
    }

    private Process RunProcess(string workingDir, string command, string args) {
      ProcessStartInfo info = new ProcessStartInfo( command, args);
      info.WorkingDirectory = workingDir;
      info.CreateNoWindow = true;
      info.WindowStyle =	 ProcessWindowStyle.Hidden;
      // info.RedirectStandardOutput
      return Process.Start(info);			
    }

    private void contextMenu_Popup(object sender, System.EventArgs e)
    {
    
    }

    private void menuItem2_Click(object sender, System.EventArgs e)
    {
      this.StopLocalRouter();
    }

    private void StopLocalRouter()
    {
      string workingDir = System.Environment.CurrentDirectory;
      this.RunProcess(workingDir + @"\Router\jboss", workingDir+ @"\Router\jboss\bin\shutdown.bat", "-s localhost:2099");
      this.menuItemStopRouter.Enabled = false;
      this.menuItemStartRouter.Enabled = true;      
    }
	}
}
