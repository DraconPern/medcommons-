/**
 * Sample CXP SOAP Client program in C#
 * 
 * This program has two functions:
 * (1) A 'Send' button that sends a CCR to the specified CXP server. A tracking 
 *     number and PIN are returned.
 * (2) A 'Get' button that gets a CCR from the tracking # and PIN. The first
 *     200 characters of the CCR are displayed on the screen. 
 * 
 * Known problems:
 * - This code does not do any error checking.
 * - The 'Send' button uses a hard-coded PIN in the code below.
 *   This same value is returned by the SOAP call.  The server
 *   can generate a PIN if none is specified but this hasn't been
 *   tested with this interface.
 * - The Get button should probably offer to save the CCR to disk. Instead
 *   only the first 200 characters are displayed. Displaying the entire 
 *   CCR in a MessageBox seems to cause problems in C#.
 * 
 * */
using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Serialization;

namespace cxpsoap
{

    /// <summary>
    /// Summary description for ClientForm.
    /// </summary>
    public class ClientForm : System.Windows.Forms.Form
    {
        private System.Windows.Forms.Button sendButton;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button button2;

        private String ccrData;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox ccrFileName;
        private System.Windows.Forms.TextBox cxpServer;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.TextBox trackingNumber;
        private System.Windows.Forms.TextBox pin;
        private System.Windows.Forms.Button getCCR;



        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.Container components = null;

        public ClientForm()
        {
            //
            // Required for Windows Form Designer support
            //
            InitializeComponent();

            //
            // TODO: Add any constructor code after InitializeComponent call
            //
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
            this.ccrFileName = new System.Windows.Forms.TextBox();
            this.sendButton = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.button1 = new System.Windows.Forms.Button();
            this.button2 = new System.Windows.Forms.Button();
            this.cxpServer = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.trackingNumber = new System.Windows.Forms.TextBox();
            this.pin = new System.Windows.Forms.TextBox();
            this.getCCR = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // ccrFileName
            // 
            this.ccrFileName.Location = new System.Drawing.Point(80, 88);
            this.ccrFileName.Name = "ccrFileName";
            this.ccrFileName.Size = new System.Drawing.Size(304, 20);
            this.ccrFileName.TabIndex = 0;
            this.ccrFileName.Text = "";
            // 
            // sendButton
            // 
            this.sendButton.Location = new System.Drawing.Point(408, 120);
            this.sendButton.Name = "sendButton";
            this.sendButton.TabIndex = 1;
            this.sendButton.Text = "Send";
            this.sendButton.Click += new System.EventHandler(this.sendButton_Click);
            // 
            // label1
            // 
            this.label1.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label1.ForeColor = System.Drawing.Color.Green;
            this.label1.Location = new System.Drawing.Point(16, 16);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(312, 23);
            this.label1.TabIndex = 2;
            this.label1.Text = "MedCommons CXP SOAP Client";
            // 
            // label2
            // 
            this.label2.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label2.Location = new System.Drawing.Point(16, 56);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(392, 23);
            this.label2.TabIndex = 3;
            this.label2.Text = "Choose a CCR and click Send:";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(408, 88);
            this.button1.Name = "button1";
            this.button1.TabIndex = 4;
            this.button1.Text = "Browse";
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(448, 336);
            this.button2.Name = "button2";
            this.button2.TabIndex = 5;
            this.button2.Text = "Close";
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // cxpServer
            // 
            this.cxpServer.Location = new System.Drawing.Point(80, 120);
            this.cxpServer.Name = "cxpServer";
            this.cxpServer.Size = new System.Drawing.Size(304, 20);
            this.cxpServer.TabIndex = 0;
            this.cxpServer.Text = "http://127.0.0.1:9080/router/services/CCRServiceSoap";
            this.cxpServer.TextChanged += new System.EventHandler(this.cxpServer_TextChanged);
            // 
            // label3
            // 
            this.label3.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label3.Location = new System.Drawing.Point(48, 89);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(24, 16);
            this.label3.TabIndex = 6;
            this.label3.Text = "File:";
            // 
            // label4
            // 
            this.label4.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label4.Location = new System.Drawing.Point(8, 122);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(72, 16);
            this.label4.TabIndex = 7;
            this.label4.Text = "CXP Server:";
            // 
            // label5
            // 
            this.label5.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label5.Location = new System.Drawing.Point(8, 168);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(64, 16);
            this.label5.TabIndex = 8;
            this.label5.Text = "Tracking #:";
            this.label5.Click += new System.EventHandler(this.label5_Click);
            // 
            // label6
            // 
            this.label6.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label6.Location = new System.Drawing.Point(40, 192);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(32, 16);
            this.label6.TabIndex = 9;
            this.label6.Text = "PIN:";
            this.label6.Click += new System.EventHandler(this.label6_Click);
            // 
            // trackingNumber
            // 
            this.trackingNumber.Location = new System.Drawing.Point(80, 168);
            this.trackingNumber.Name = "trackingNumber";
            this.trackingNumber.Size = new System.Drawing.Size(304, 20);
            this.trackingNumber.TabIndex = 10;
            this.trackingNumber.Text = "";
            // 
            // pin
            // 
            this.pin.Location = new System.Drawing.Point(80, 192);
            this.pin.Name = "pin";
            this.pin.Size = new System.Drawing.Size(304, 20);
            this.pin.TabIndex = 11;
            this.pin.Text = "";
            // 
            // getCCR
            // 
            this.getCCR.Location = new System.Drawing.Point(408, 192);
            this.getCCR.Name = "getCCR";
            this.getCCR.TabIndex = 12;
            this.getCCR.Text = "Get CCR";
            this.getCCR.Click += new System.EventHandler(this.button3_Click);
            // 
            // ClientForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.ClientSize = new System.Drawing.Size(528, 374);
            this.Controls.Add(this.getCCR);
            this.Controls.Add(this.pin);
            this.Controls.Add(this.trackingNumber);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.sendButton);
            this.Controls.Add(this.ccrFileName);
            this.Controls.Add(this.cxpServer);
            this.Name = "ClientForm";
            this.Activated += new System.EventHandler(this.OnActivated);
            this.ResumeLayout(false);

        }
        #endregion

        private void sendButton_Click(object sender, System.EventArgs e)
        {
            this.pin.Text = "";
            this.trackingNumber.Text = "";
       
            System.IO.StreamReader reader = new System.IO.StreamReader(this.ccrFileName.Text);
            this.ccrData = reader.ReadToEnd();
      
      
            CXPServer.CXPServerService server = new  cxpsoap.CXPServer.CXPServerService();
            server.Url = this.cxpServer.Text;
            cxpsoap.CXPServer.Response response = server.put(this.ccrData, "12345");
      
        
       
            if (response.status == 200)
            {
                this.trackingNumber.Text = response.trackingNumber;
                this.pin.Text = response.pin;
                MessageBox.Show(this, "CXP Transfer Success!\nUUID:" + response.uid + 
                    "\nTracking Number:" + response.trackingNumber +
                    "\nPIN:" + response.pin 
                    );
            }
            else if (response.status < 299)
            {
                this.trackingNumber.Text = response.trackingNumber;
                this.pin.Text = response.pin;
                MessageBox.Show(this, "CXP Transfer Success (with warnings)\nUUID:" + response.uid+
                    "\nTracking Number:" + response.trackingNumber +
                    "\nPIN:" + response.pin +
                    "\nErrorCode:" + response.status +
                    "\nErrorDescription:" + response.reason );
            }
            else
            {
                MessageBox.Show(this, "CXP Transfer Failed:\nErrorCode:" +
                    response.status +
                    "\nErrorDescription:" + response.reason);
            }

        }

        public static void Main(string [] args) 
        {
            ClientForm cf = new ClientForm();
            // cf.InitializeComponent();
            Application.Run(cf);
        }

        private void button2_Click(object sender, System.EventArgs e)
        {
            Application.Exit();
        }

        private void button1_Click(object sender, System.EventArgs e)
        {
            OpenFileDialog dlg = new OpenFileDialog();
            if(dlg.ShowDialog(this) == DialogResult.OK) 
            {
                this.ccrFileName.Text = dlg.FileName;
            }

        }

        private void OnActivated(object sender, System.EventArgs e)
        {
        
            CXPServer.CXPServerService server = new  cxpsoap.CXPServer.CXPServerService();
            this.cxpServer.Text = server.Url;
        }

        private void cxpServer_TextChanged(object sender, System.EventArgs e)
        {
        
        }

        private void label5_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label6_Click(object sender, System.EventArgs e)
        {
        
        }

        /**
         * Returns a CCR 
         **/
        private void button3_Click(object sender, System.EventArgs e)
        {
            CXPServer.CXPServerService server = new  cxpsoap.CXPServer.CXPServerService();
            server.Url = this.cxpServer.Text;
            String pin =  this.pin.Text;
            String trackingNumber = this.trackingNumber.Text;
            String ccrText  = server.get(trackingNumber, pin);
            MessageBox.Show(this, "Get returned:\n" + ccrText.Substring(0,200));
             
            
        }
    }
}
