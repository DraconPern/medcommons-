using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using System.Threading;

namespace RouterMonitor
{
	/// <summary>
	/// Summary description for Form1.
	/// </summary>
	public class RouterMonitor : System.Windows.Forms.Form
	{
		private System.Windows.Forms.NotifyIcon notificationIcon;
		private System.Windows.Forms.ContextMenu contextMenu;
		private System.Windows.Forms.MenuItem menuItemStatus;
		private System.Windows.Forms.MenuItem menuItemExit;
		private System.ComponentModel.IContainer components;

		/// <summary>
		/// Thread which occasionally switches the status around to simulate
		/// the connection coming and going
		/// </summary>
		Thread statusUpdateThread;

		/// <summary>
		/// 0 - no connections to anything
		/// 1 - connection to local router
		/// 2 - connection to LAN router
		/// 3 - connection to central
		/// </summary>
		int status;
		private System.Windows.Forms.Button button1;
		private System.Windows.Forms.Label label4;
		private System.Windows.Forms.Label label3;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.PictureBox pictureBox1;
		private System.Windows.Forms.PictureBox pictureBox2;
		private System.Windows.Forms.PictureBox pictureBox3;
		private System.Windows.Forms.Label label5;
		private System.Windows.Forms.Label label6;
		private System.Windows.Forms.Button button2;
		private System.Windows.Forms.Button button3;

		/// <summary>
		/// Set to false when we quit so other components can exit safely
		/// </summary>
		bool running = true;

		public RouterMonitor()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			// Attach to Router and 
			//statusUpdateThread = new Thread(new ThreadStart(this.CheckConnection));
			//statusUpdateThread.Start();
			// Hide();
		}


		public void CheckConnection() {

			/*while (running)
			{
				Thread.Sleep(5000);

				Random random = new Random();
				status = random.Next(4);

				if (!running)
					break;

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
			*/

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
			this.menuItemStatus = new System.Windows.Forms.MenuItem();
			this.menuItemExit = new System.Windows.Forms.MenuItem();
			this.button1 = new System.Windows.Forms.Button();
			this.label4 = new System.Windows.Forms.Label();
			this.label3 = new System.Windows.Forms.Label();
			this.label2 = new System.Windows.Forms.Label();
			this.label1 = new System.Windows.Forms.Label();
			this.pictureBox1 = new System.Windows.Forms.PictureBox();
			this.pictureBox2 = new System.Windows.Forms.PictureBox();
			this.pictureBox3 = new System.Windows.Forms.PictureBox();
			this.label5 = new System.Windows.Forms.Label();
			this.label6 = new System.Windows.Forms.Label();
			this.button2 = new System.Windows.Forms.Button();
			this.button3 = new System.Windows.Forms.Button();
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
																						this.menuItemStatus,
																						this.menuItemExit});
			// 
			// menuItemStatus
			// 
			this.menuItemStatus.Index = 0;
			this.menuItemStatus.Text = "Status ...";
			this.menuItemStatus.Click += new System.EventHandler(this.menuItemStatus_Click);
			// 
			// menuItemExit
			// 
			this.menuItemExit.Index = 1;
			this.menuItemExit.Text = "Exit";
			this.menuItemExit.Click += new System.EventHandler(this.menuItemExit_Click);
			// 
			// button1
			// 
			this.button1.Location = new System.Drawing.Point(164, 74);
			this.button1.Name = "button1";
			this.button1.TabIndex = 18;
			this.button1.Text = "Configure...";
			// 
			// label4
			// 
			this.label4.Location = new System.Drawing.Point(164, 50);
			this.label4.Name = "label4";
			this.label4.Size = new System.Drawing.Size(144, 24);
			this.label4.TabIndex = 15;
			this.label4.Text = "Status:   Connected";
			// 
			// label3
			// 
			this.label3.Location = new System.Drawing.Point(44, 82);
			this.label3.Name = "label3";
			this.label3.Size = new System.Drawing.Size(80, 32);
			this.label3.TabIndex = 12;
			this.label3.Text = "MedCommons Central";
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(44, 170);
			this.label2.Name = "label2";
			this.label2.Size = new System.Drawing.Size(56, 32);
			this.label2.TabIndex = 11;
			this.label2.Text = "Local Network";
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(44, 266);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(56, 32);
			this.label1.TabIndex = 10;
			this.label1.Text = "Local Computer";
			// 
			// pictureBox1
			// 
			this.pictureBox1.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox1.Image")));
			this.pictureBox1.Location = new System.Drawing.Point(44, 34);
			this.pictureBox1.Name = "pictureBox1";
			this.pictureBox1.Size = new System.Drawing.Size(48, 40);
			this.pictureBox1.TabIndex = 7;
			this.pictureBox1.TabStop = false;
			// 
			// pictureBox2
			// 
			this.pictureBox2.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox2.Image")));
			this.pictureBox2.Location = new System.Drawing.Point(44, 122);
			this.pictureBox2.Name = "pictureBox2";
			this.pictureBox2.Size = new System.Drawing.Size(64, 56);
			this.pictureBox2.TabIndex = 8;
			this.pictureBox2.TabStop = false;
			// 
			// pictureBox3
			// 
			this.pictureBox3.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox3.Image")));
			this.pictureBox3.Location = new System.Drawing.Point(44, 218);
			this.pictureBox3.Name = "pictureBox3";
			this.pictureBox3.Size = new System.Drawing.Size(64, 56);
			this.pictureBox3.TabIndex = 9;
			this.pictureBox3.TabStop = false;
			// 
			// label5
			// 
			this.label5.Location = new System.Drawing.Point(164, 138);
			this.label5.Name = "label5";
			this.label5.Size = new System.Drawing.Size(144, 24);
			this.label5.TabIndex = 13;
			this.label5.Text = "Status:   Connected";
			// 
			// label6
			// 
			this.label6.Location = new System.Drawing.Point(164, 234);
			this.label6.Name = "label6";
			this.label6.Size = new System.Drawing.Size(144, 24);
			this.label6.TabIndex = 14;
			this.label6.Text = "Status:   Connected";
			// 
			// button2
			// 
			this.button2.Location = new System.Drawing.Point(164, 162);
			this.button2.Name = "button2";
			this.button2.TabIndex = 17;
			this.button2.Text = "Configure...";
			// 
			// button3
			// 
			this.button3.Location = new System.Drawing.Point(164, 258);
			this.button3.Name = "button3";
			this.button3.TabIndex = 16;
			this.button3.Text = "Configure...";
			// 
			// RouterMonitor
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(352, 333);
			this.Controls.Add(this.button1);
			this.Controls.Add(this.label4);
			this.Controls.Add(this.label3);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.label1);
			this.Controls.Add(this.pictureBox1);
			this.Controls.Add(this.pictureBox2);
			this.Controls.Add(this.pictureBox3);
			this.Controls.Add(this.label5);
			this.Controls.Add(this.label6);
			this.Controls.Add(this.button2);
			this.Controls.Add(this.button3);
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.Name = "RouterMonitor";
			this.ShowInTaskbar = false;
			this.Text = "Med Commons Connections";
			this.WindowState = System.Windows.Forms.FormWindowState.Minimized;
			this.Resize += new System.EventHandler(this.OnResize);
			this.Load += new System.EventHandler(this.Form1_Load);
			this.Closed += new System.EventHandler(this.OnClosed);
			this.ResumeLayout(false);

		}
		#endregion

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			RouterMonitor monitor = new RouterMonitor();
			Application.Run(monitor);
			//monitor.WindowState = FormWindowState.Minimized;
			// monitor.Hide();
		}

		private void Form1_Load(object sender, System.EventArgs e)
		{
		 
		}

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
			Show();
			WindowState = FormWindowState.Normal;				
		}

		private void menuItemExit_Click(object sender, System.EventArgs e)
		{
			this.running = false;
			this.Close();
		}

		private void OnClosed(object sender, System.EventArgs e)
		{
			this.running = false;
		}

		private void label3_Click(object sender, System.EventArgs e)
		{
		
		}
	}
}
