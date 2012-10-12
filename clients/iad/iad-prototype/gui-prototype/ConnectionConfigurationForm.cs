using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;

using MedCommons;

namespace gui_prototype
{
	/// <summary>
	/// Summary description for ConnectionConfiguration.
	/// </summary>
	public class ConnectionConfigurationForm : System.Windows.Forms.Form
	{
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.Label label3;
		private System.Windows.Forms.TextBox host;
		private System.Windows.Forms.TextBox port;
		private System.Windows.Forms.Label label4;
		private System.Windows.Forms.TextBox name;
		private System.Windows.Forms.Button buttonOK;
		private System.Windows.Forms.Button buttonCancel;


		private bool canceled = false;

		public bool Canceled {
			get {
				return this.canceled;
			}
		}

		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;
		private System.Windows.Forms.TextBox userNameTextBox;
		private System.Windows.Forms.Label label5;
		private System.Windows.Forms.TextBox passwordTextBox;
		private System.Windows.Forms.Label label6;

		private ConnectionConfiguration config = null;

		public ConnectionConfigurationForm(ConnectionConfiguration config)
		{
			//	
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			this.name.Text = config.ConnectionName;
			this.host.Text = config.Host;
			this.port.Text = config.Port;
			this.name.Focus();
			this.name.SelectAll();
			this.config = config;
			this.userNameTextBox.Text = config.UserName;
			this.passwordTextBox.Text = config.Password;
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
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(ConnectionConfigurationForm));
			this.buttonOK = new System.Windows.Forms.Button();
			this.host = new System.Windows.Forms.TextBox();
			this.label1 = new System.Windows.Forms.Label();
			this.port = new System.Windows.Forms.TextBox();
			this.label2 = new System.Windows.Forms.Label();
			this.label3 = new System.Windows.Forms.Label();
			this.buttonCancel = new System.Windows.Forms.Button();
			this.name = new System.Windows.Forms.TextBox();
			this.label4 = new System.Windows.Forms.Label();
			this.userNameTextBox = new System.Windows.Forms.TextBox();
			this.label5 = new System.Windows.Forms.Label();
			this.passwordTextBox = new System.Windows.Forms.TextBox();
			this.label6 = new System.Windows.Forms.Label();
			this.SuspendLayout();
			// 
			// buttonOK
			// 
			this.buttonOK.Location = new System.Drawing.Point(184, 208);
			this.buttonOK.Name = "buttonOK";
			this.buttonOK.TabIndex = 5;
			this.buttonOK.Text = "OK";
			this.buttonOK.Click += new System.EventHandler(this.buttonOK_Click);
			// 
			// host
			// 
			this.host.Location = new System.Drawing.Point(72, 80);
			this.host.Name = "host";
			this.host.Size = new System.Drawing.Size(160, 20);
			this.host.TabIndex = 2;
			this.host.Text = "";
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(8, 80);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(32, 16);
			this.label1.TabIndex = 2;
			this.label1.Text = "Host:";
			// 
			// port
			// 
			this.port.Location = new System.Drawing.Point(72, 112);
			this.port.Name = "port";
			this.port.Size = new System.Drawing.Size(160, 20);
			this.port.TabIndex = 3;
			this.port.Text = "";
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(8, 112);
			this.label2.Name = "label2";
			this.label2.Size = new System.Drawing.Size(32, 16);
			this.label2.TabIndex = 2;
			this.label2.Text = "Port:";
			// 
			// label3
			// 
			this.label3.Location = new System.Drawing.Point(8, 8);
			this.label3.Name = "label3";
			this.label3.Size = new System.Drawing.Size(240, 32);
			this.label3.TabIndex = 3;
			this.label3.Text = "Enter details for your MedCommons connection here.";
			// 
			// buttonCancel
			// 
			this.buttonCancel.Location = new System.Drawing.Point(96, 208);
			this.buttonCancel.Name = "buttonCancel";
			this.buttonCancel.TabIndex = 4;
			this.buttonCancel.Text = "Cancel";
			this.buttonCancel.Click += new System.EventHandler(this.buttonCancel_Click);
			// 
			// name
			// 
			this.name.Location = new System.Drawing.Point(72, 48);
			this.name.Name = "name";
			this.name.Size = new System.Drawing.Size(160, 20);
			this.name.TabIndex = 1;
			this.name.Text = "";
			// 
			// label4
			// 
			this.label4.Location = new System.Drawing.Point(8, 48);
			this.label4.Name = "label4";
			this.label4.Size = new System.Drawing.Size(40, 16);
			this.label4.TabIndex = 2;
			this.label4.Text = "Name:";
			// 
			// userNameTextBox
			// 
			this.userNameTextBox.Location = new System.Drawing.Point(72, 144);
			this.userNameTextBox.Name = "userNameTextBox";
			this.userNameTextBox.Size = new System.Drawing.Size(160, 20);
			this.userNameTextBox.TabIndex = 6;
			this.userNameTextBox.Text = "";
			this.userNameTextBox.TextChanged += new System.EventHandler(this.textBox1_TextChanged);
			// 
			// label5
			// 
			this.label5.Location = new System.Drawing.Point(8, 147);
			this.label5.Name = "label5";
			this.label5.Size = new System.Drawing.Size(32, 16);
			this.label5.TabIndex = 7;
			this.label5.Text = "User:";
			// 
			// passwordTextBox
			// 
			this.passwordTextBox.Location = new System.Drawing.Point(72, 176);
			this.passwordTextBox.Name = "passwordTextBox";
			this.passwordTextBox.PasswordChar = '*';
			this.passwordTextBox.Size = new System.Drawing.Size(160, 20);
			this.passwordTextBox.TabIndex = 8;
			this.passwordTextBox.Text = "";
			// 
			// label6
			// 
			this.label6.Location = new System.Drawing.Point(8, 179);
			this.label6.Name = "label6";
			this.label6.Size = new System.Drawing.Size(56, 16);
			this.label6.TabIndex = 9;
			this.label6.Text = "Password:";
			// 
			// ConnectionConfigurationForm
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(264, 237);
			this.Controls.Add(this.label6);
			this.Controls.Add(this.passwordTextBox);
			this.Controls.Add(this.label5);
			this.Controls.Add(this.userNameTextBox);
			this.Controls.Add(this.label3);
			this.Controls.Add(this.label1);
			this.Controls.Add(this.host);
			this.Controls.Add(this.buttonOK);
			this.Controls.Add(this.port);
			this.Controls.Add(this.label2);
			this.Controls.Add(this.buttonCancel);
			this.Controls.Add(this.name);
			this.Controls.Add(this.label4);
			this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
			this.Name = "ConnectionConfigurationForm";
			this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Hide;
			this.Text = "Connection Details";
			this.Activated += new System.EventHandler(this.OnActivate);
			this.ResumeLayout(false);

		}
		#endregion

		private void buttonOK_Click(object sender, System.EventArgs e)
		{
			this.config.ConnectionName = this.name.Text;
			this.config.Host = this.host.Text;
			this.config.Port = this.port.Text;
			this.config.UserName = this.userNameTextBox.Text;
			this.config.Password = this.userNameTextBox.Text;
			this.Close();
		}

		private void buttonCancel_Click(object sender, System.EventArgs e)
		{
			this.canceled = true;
			this.Close();
		}

		private void OnActivate(object sender, System.EventArgs e)
		{
			this.name.Focus();
			this.name.SelectAll();		
		}

		private void textBox1_TextChanged(object sender, System.EventArgs e)
		{
		
		}
	}
}
