/**
 * Sample CXP SOAP Client program in C#
 * 
 * This program demonstrates the use of two distinct APIs
 * (1) It demonstrates the SOAP CXP interface for 
 *       (a) sending a document via PUT
 *       (b) receiving a document via GET
 * (2) It demonstrates a metadata API for 
 *       (a) querying to get the CCRs associated with an account
 *       (b) setting a single CCR as the 'emergency' ccr for an account.
 * 
 * These APIs are completely separate - in the future we may use two different 
 * example programs to make this difference clearer. 
 * 
 * 
 * Details:
 * 
 * CXP
 * (1) A 'PUT' button that sends a CCR to the specified CXP server. On output
 *     the CommonsId, ConfirmationCode, RegistrySecret, and GUID are filled in. 
 *     The CommonsId and RegistrySecret may be filled in by the client - these
 *     same values are then returned. If blank values are created by the CXP 
 *     reciever.
 * (2) A 'Get' button that gets a CCR based on the RegistrySecret and ConfirmationCode. 
 *     The first 200 characters of the CCR are displayed on the screen. 
 * 
 * SOAP Query Client:
 * (1) QueryByAccid queries the MedCommons Account server for all CCRs for a given
 *     CommonsId.
 * (2) A single CCR can be set as the 'emergency' CCR for a specific CommonsId.
 * 
 * Known client problems:
 * - This code does not do any (well -very little) error checking.
 * - The GET button should probably offer to save the CCR to disk. Instead
 *   only the first 200 characters are displayed. Displaying the entire 
 *   CCR in a MessageBox seems to cause problems in C#.
 * - The comments need some work.
 * - The PUT command returns an array of RegistryParameters; only the first is
 *   examined in this program.
 * - SetEmergencyCCR is not implemented on the server. The API is implemented - 
 *   it simply returns a status code of 500.
 * 
 * */
using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Serialization;
using System.Security.Principal;

namespace cxpsoap2
{

    /// <summary>
    /// Summary description for ClientForm.
    /// </summary>
    public class ClientForm : System.Windows.Forms.Form
    {
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button button2;


        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Button getCCR;
        private System.Windows.Forms.TextBox accountId;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.TextBox emergencyAccountID;
        private System.Windows.Forms.TextBox emergencyGUID;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Button queryForCCRBUtton;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.TextBox MetadataServerWDSL;
        private System.Windows.Forms.TextBox CXPServerWDSL;
        private System.Windows.Forms.TextBox CommonsID;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.Label label13;
        private System.Windows.Forms.Label label14;
        private System.Windows.Forms.TextBox GUID;
        private System.Windows.Forms.TextBox CCRFileName;
        private System.Windows.Forms.Button browseButton;
        private System.Windows.Forms.TextBox SenderID;
        private System.Windows.Forms.Label label15;
        private System.Windows.Forms.TextBox NotificationSubject;
        private System.Windows.Forms.Label label16;

        private PatientQuery.PatientCCRServerService patientServer;
        private CXP.CXPServer2Service cxpServer;
        private System.Windows.Forms.TextBox ConfirmationCode;
        private System.Windows.Forms.TextBox RegistrySecret;
        private System.Windows.Forms.Button GET;



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
            init();
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
            this.accountId = new System.Windows.Forms.TextBox();
            this.queryForCCRBUtton = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.button2 = new System.Windows.Forms.Button();
            this.MetadataServerWDSL = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.emergencyAccountID = new System.Windows.Forms.TextBox();
            this.emergencyGUID = new System.Windows.Forms.TextBox();
            this.getCCR = new System.Windows.Forms.Button();
            this.label7 = new System.Windows.Forms.Label();
            this.label8 = new System.Windows.Forms.Label();
            this.CXPServerWDSL = new System.Windows.Forms.TextBox();
            this.label9 = new System.Windows.Forms.Label();
            this.CommonsID = new System.Windows.Forms.TextBox();
            this.label10 = new System.Windows.Forms.Label();
            this.label11 = new System.Windows.Forms.Label();
            this.CCRFileName = new System.Windows.Forms.TextBox();
            this.browseButton = new System.Windows.Forms.Button();
            this.button3 = new System.Windows.Forms.Button();
            this.label12 = new System.Windows.Forms.Label();
            this.label13 = new System.Windows.Forms.Label();
            this.label14 = new System.Windows.Forms.Label();
            this.ConfirmationCode = new System.Windows.Forms.TextBox();
            this.RegistrySecret = new System.Windows.Forms.TextBox();
            this.GUID = new System.Windows.Forms.TextBox();
            this.GET = new System.Windows.Forms.Button();
            this.SenderID = new System.Windows.Forms.TextBox();
            this.label15 = new System.Windows.Forms.Label();
            this.NotificationSubject = new System.Windows.Forms.TextBox();
            this.label16 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // accountId
            // 
            this.accountId.Location = new System.Drawing.Point(784, 152);
            this.accountId.Name = "accountId";
            this.accountId.Size = new System.Drawing.Size(312, 20);
            this.accountId.TabIndex = 70;
            this.accountId.Text = "";
            this.accountId.TextChanged += new System.EventHandler(this.ccrFileName_TextChanged);
            // 
            // queryForCCRBUtton
            // 
            this.queryForCCRBUtton.Location = new System.Drawing.Point(1104, 152);
            this.queryForCCRBUtton.Name = "queryForCCRBUtton";
            this.queryForCCRBUtton.Size = new System.Drawing.Size(112, 23);
            this.queryForCCRBUtton.TabIndex = 71;
            this.queryForCCRBUtton.Text = "Query For CCRs";
            this.queryForCCRBUtton.Click += new System.EventHandler(this.sendButton_Click);
            // 
            // label1
            // 
            this.label1.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label1.ForeColor = System.Drawing.Color.Green;
            this.label1.Location = new System.Drawing.Point(704, 32);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(312, 23);
            this.label1.TabIndex = 2;
            this.label1.Text = "MedCommons  SOAP Query Client";
            this.label1.Click += new System.EventHandler(this.label1_Click);
            // 
            // label2
            // 
            this.label2.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label2.Location = new System.Drawing.Point(704, 120);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(392, 23);
            this.label2.TabIndex = 3;
            this.label2.Text = "Enter a MedCommmons Account ID";
            this.label2.Click += new System.EventHandler(this.label2_Click);
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(1152, 344);
            this.button2.Name = "button2";
            this.button2.TabIndex = 5;
            this.button2.Text = "Close";
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // MetadataServerWDSL
            // 
            this.MetadataServerWDSL.Location = new System.Drawing.Point(800, 72);
            this.MetadataServerWDSL.Name = "MetadataServerWDSL";
            this.MetadataServerWDSL.Size = new System.Drawing.Size(400, 20);
            this.MetadataServerWDSL.TabIndex = 60;
            this.MetadataServerWDSL.Text = "";
            this.MetadataServerWDSL.TextChanged += new System.EventHandler(this.cxpServer_TextChanged);
            // 
            // label3
            // 
            this.label3.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label3.Location = new System.Drawing.Point(704, 152);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(72, 16);
            this.label3.TabIndex = 6;
            this.label3.Text = "AccountID";
            this.label3.Click += new System.EventHandler(this.label3_Click);
            // 
            // label4
            // 
            this.label4.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label4.Location = new System.Drawing.Point(696, 72);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(96, 16);
            this.label4.TabIndex = 7;
            this.label4.Text = "Metadata Server:";
            this.label4.Click += new System.EventHandler(this.label4_Click);
            // 
            // label5
            // 
            this.label5.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label5.Location = new System.Drawing.Point(704, 264);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(64, 16);
            this.label5.TabIndex = 8;
            this.label5.Text = "AccountID";
            this.label5.Click += new System.EventHandler(this.label5_Click);
            // 
            // label6
            // 
            this.label6.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label6.Location = new System.Drawing.Point(720, 288);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(40, 16);
            this.label6.TabIndex = 9;
            this.label6.Text = "GUID";
            this.label6.Click += new System.EventHandler(this.label6_Click);
            // 
            // emergencyAccountID
            // 
            this.emergencyAccountID.Location = new System.Drawing.Point(776, 264);
            this.emergencyAccountID.Name = "emergencyAccountID";
            this.emergencyAccountID.Size = new System.Drawing.Size(304, 20);
            this.emergencyAccountID.TabIndex = 80;
            this.emergencyAccountID.Text = "";
            // 
            // emergencyGUID
            // 
            this.emergencyGUID.Location = new System.Drawing.Point(776, 288);
            this.emergencyGUID.Name = "emergencyGUID";
            this.emergencyGUID.Size = new System.Drawing.Size(304, 20);
            this.emergencyGUID.TabIndex = 90;
            this.emergencyGUID.Text = "";
            // 
            // getCCR
            // 
            this.getCCR.Location = new System.Drawing.Point(1104, 288);
            this.getCCR.Name = "getCCR";
            this.getCCR.Size = new System.Drawing.Size(120, 23);
            this.getCCR.TabIndex = 91;
            this.getCCR.Text = "Set Emergency CCR";
            this.getCCR.Click += new System.EventHandler(this.button3_Click);
            // 
            // label7
            // 
            this.label7.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label7.Location = new System.Drawing.Point(704, 232);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(392, 23);
            this.label7.TabIndex = 13;
            this.label7.Text = "Set an Emergency CCR for a patient";
            this.label7.Click += new System.EventHandler(this.label7_Click);
            // 
            // label8
            // 
            this.label8.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label8.ForeColor = System.Drawing.Color.Green;
            this.label8.Location = new System.Drawing.Point(16, 24);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(248, 23);
            this.label8.TabIndex = 14;
            this.label8.Text = "CXP Demonstration Client";
            this.label8.Click += new System.EventHandler(this.label8_Click);
            // 
            // CXPServerWDSL
            // 
            this.CXPServerWDSL.Location = new System.Drawing.Point(104, 56);
            this.CXPServerWDSL.Name = "CXPServerWDSL";
            this.CXPServerWDSL.Size = new System.Drawing.Size(400, 20);
            this.CXPServerWDSL.TabIndex = 15;
            this.CXPServerWDSL.Text = "";
            // 
            // label9
            // 
            this.label9.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label9.Location = new System.Drawing.Point(16, 56);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(72, 16);
            this.label9.TabIndex = 16;
            this.label9.Text = "CXPServer:";
            // 
            // CommonsID
            // 
            this.CommonsID.Location = new System.Drawing.Point(120, 144);
            this.CommonsID.Name = "CommonsID";
            this.CommonsID.Size = new System.Drawing.Size(304, 20);
            this.CommonsID.TabIndex = 22;
            this.CommonsID.Text = "";
            // 
            // label10
            // 
            this.label10.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label10.Location = new System.Drawing.Point(16, 144);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(72, 16);
            this.label10.TabIndex = 18;
            this.label10.Text = "CommonsID";
            this.label10.Click += new System.EventHandler(this.label10_Click);
            // 
            // label11
            // 
            this.label11.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label11.Location = new System.Drawing.Point(16, 112);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(72, 16);
            this.label11.TabIndex = 19;
            this.label11.Text = "File:";
            this.label11.Click += new System.EventHandler(this.label11_Click);
            // 
            // CCRFileName
            // 
            this.CCRFileName.Location = new System.Drawing.Point(120, 112);
            this.CCRFileName.Name = "CCRFileName";
            this.CCRFileName.Size = new System.Drawing.Size(304, 20);
            this.CCRFileName.TabIndex = 20;
            this.CCRFileName.Text = "";
            this.CCRFileName.TextChanged += new System.EventHandler(this.textBox1_TextChanged);
            // 
            // browseButton
            // 
            this.browseButton.Location = new System.Drawing.Point(440, 112);
            this.browseButton.Name = "browseButton";
            this.browseButton.Size = new System.Drawing.Size(112, 23);
            this.browseButton.TabIndex = 21;
            this.browseButton.Text = "Browse";
            this.browseButton.Click += new System.EventHandler(this.button1_Click_1);
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(440, 248);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(112, 23);
            this.button3.TabIndex = 41;
            this.button3.Text = "PUT";
            this.button3.Click += new System.EventHandler(this.button3_Click_1);
            // 
            // label12
            // 
            this.label12.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label12.Location = new System.Drawing.Point(0, 280);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(120, 16);
            this.label12.TabIndex = 23;
            this.label12.Text = "ConfirmationCode";
            this.label12.Click += new System.EventHandler(this.label12_Click);
            // 
            // label13
            // 
            this.label13.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label13.Location = new System.Drawing.Point(16, 208);
            this.label13.Name = "label13";
            this.label13.Size = new System.Drawing.Size(88, 16);
            this.label13.TabIndex = 24;
            this.label13.Text = "RegistrySecret";
            // 
            // label14
            // 
            this.label14.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label14.Location = new System.Drawing.Point(16, 336);
            this.label14.Name = "label14";
            this.label14.Size = new System.Drawing.Size(72, 16);
            this.label14.TabIndex = 25;
            this.label14.Text = "GUID";
            // 
            // ConfirmationCode
            // 
            this.ConfirmationCode.Location = new System.Drawing.Point(112, 280);
            this.ConfirmationCode.Name = "ConfirmationCode";
            this.ConfirmationCode.Size = new System.Drawing.Size(304, 20);
            this.ConfirmationCode.TabIndex = 45;
            this.ConfirmationCode.Text = "";
            // 
            // RegistrySecret
            // 
            this.RegistrySecret.Location = new System.Drawing.Point(112, 208);
            this.RegistrySecret.Name = "RegistrySecret";
            this.RegistrySecret.Size = new System.Drawing.Size(320, 20);
            this.RegistrySecret.TabIndex = 35;
            this.RegistrySecret.Text = "";
            // 
            // GUID
            // 
            this.GUID.Location = new System.Drawing.Point(104, 336);
            this.GUID.Name = "GUID";
            this.GUID.Size = new System.Drawing.Size(320, 20);
            this.GUID.TabIndex = 50;
            this.GUID.Text = "";
            // 
            // GET
            // 
            this.GET.Location = new System.Drawing.Point(448, 336);
            this.GET.Name = "GET";
            this.GET.Size = new System.Drawing.Size(112, 23);
            this.GET.TabIndex = 51;
            this.GET.Text = "GET";
            this.GET.Click += new System.EventHandler(this.GET_Click);
            // 
            // SenderID
            // 
            this.SenderID.Location = new System.Drawing.Point(120, 176);
            this.SenderID.Name = "SenderID";
            this.SenderID.Size = new System.Drawing.Size(304, 20);
            this.SenderID.TabIndex = 30;
            this.SenderID.Text = "";
            // 
            // label15
            // 
            this.label15.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label15.Location = new System.Drawing.Point(16, 184);
            this.label15.Name = "label15";
            this.label15.Size = new System.Drawing.Size(72, 16);
            this.label15.TabIndex = 31;
            this.label15.Text = "SenderID";
            this.label15.Click += new System.EventHandler(this.label15_Click);
            // 
            // NotificationSubject
            // 
            this.NotificationSubject.Location = new System.Drawing.Point(112, 248);
            this.NotificationSubject.Name = "NotificationSubject";
            this.NotificationSubject.Size = new System.Drawing.Size(304, 20);
            this.NotificationSubject.TabIndex = 40;
            this.NotificationSubject.Text = "";
            // 
            // label16
            // 
            this.label16.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label16.Location = new System.Drawing.Point(0, 248);
            this.label16.Name = "label16";
            this.label16.Size = new System.Drawing.Size(104, 16);
            this.label16.TabIndex = 33;
            this.label16.Text = "NotificationSubject";
            this.label16.Click += new System.EventHandler(this.label16_Click);
            // 
            // ClientForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.ClientSize = new System.Drawing.Size(1272, 390);
            this.Controls.Add(this.label16);
            this.Controls.Add(this.NotificationSubject);
            this.Controls.Add(this.SenderID);
            this.Controls.Add(this.GUID);
            this.Controls.Add(this.RegistrySecret);
            this.Controls.Add(this.ConfirmationCode);
            this.Controls.Add(this.CCRFileName);
            this.Controls.Add(this.CommonsID);
            this.Controls.Add(this.CXPServerWDSL);
            this.Controls.Add(this.emergencyGUID);
            this.Controls.Add(this.emergencyAccountID);
            this.Controls.Add(this.accountId);
            this.Controls.Add(this.MetadataServerWDSL);
            this.Controls.Add(this.label15);
            this.Controls.Add(this.GET);
            this.Controls.Add(this.label14);
            this.Controls.Add(this.label13);
            this.Controls.Add(this.label12);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.browseButton);
            this.Controls.Add(this.label11);
            this.Controls.Add(this.label10);
            this.Controls.Add(this.label9);
            this.Controls.Add(this.label8);
            this.Controls.Add(this.label7);
            this.Controls.Add(this.getCCR);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.queryForCCRBUtton);
            this.Name = "ClientForm";
            this.Load += new System.EventHandler(this.ClientForm_Load);
            this.Activated += new System.EventHandler(this.OnActivated);
            this.ResumeLayout(false);

        }
        #endregion

        private void sendButton_Click(object sender, System.EventArgs e)
        {
           
            try
            {
            
                patientServer.Url = this.MetadataServerWDSL.Text;
            
                cxpsoap2.PatientQuery.MetadataResponse response =  patientServer.queryByAccid(this.accountId.Text);
                String output = "queryByAccid results:";
                output += "\n Status:" + response.status;
                output += "\n Reason:" + response.reason;
                int responseLength = 0;
                if (response.ccrResults!= null)
                    responseLength = response.ccrResults.Length;
                output += "\nNumber of CCRS:" + responseLength;
            
                for (int i=0;i<responseLength;i++)
                {
                    output += "\n " + response.ccrResults[i].guid + ",\t" + response.ccrResults[i].idp + ",\t" + response.ccrResults[i].subject + 
                        "\n\t" + response.ccrResults[i].src + ",\t" + response.ccrResults[i].dest + ",\t" + response.ccrResults[i].status;
                }
             
            
           
        
                MessageBox.Show(output);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Unknown error communicating with server " +patientServer.Url.ToString() 
                    + "\n" + ex.ToString()); 
                return;
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
            /*
            if(dlg.ShowDialog(this) == DialogResult.OK) 
            {
                this.ccrFileName.Text = dlg.FileName;
            }
            */

        }

        private void OnActivated(object sender, System.EventArgs e)
        {
        
            //CXPServer.CXPServerService server = new  cxpsoap.CXPServer.CXPServerService();
            // this.cxpServer.Text = server.Url;
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
            
            
            patientServer.Url = this.MetadataServerWDSL.Text;
            
            String accid = this.emergencyAccountID.Text;
            String guid = this.emergencyGUID.Text;
            cxpsoap2.PatientQuery.MetadataResponse response =  patientServer.setEmergencyCCR(accid, guid);
            
            String output = "Set Emergency CCR Response:";
            output += "\n Status:" + response.status;
            output += "\n Reason:" + response.reason;
           
            MessageBox.Show(this, output);
             
            
        }

        private void ccrFileName_TextChanged(object sender, System.EventArgs e)
        {
        
        }

        private void label3_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label4_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label1_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label2_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label7_Click(object sender, System.EventArgs e)
        {
        
        }

        private void ClientForm_Load(object sender, System.EventArgs e)
        {
        
        }

        private void label8_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label10_Click(object sender, System.EventArgs e)
        {
        
        }

        private void label11_Click(object sender, System.EventArgs e)
        {
        
        }

        private void textBox1_TextChanged(object sender, System.EventArgs e)
        {
        
        }

        private void label12_Click(object sender, System.EventArgs e)
        {
        
        }

        /**
         * Performs a CXP PUT on the specified receiver.
         * */
        private void button3_Click_1(object sender, System.EventArgs e)
        {
            
            try
            {
       
                System.IO.StreamReader reader = new System.IO.StreamReader(this.CCRFileName.Text);
                String ccrData = reader.ReadToEnd();
                reader.Close();
      
            
           
                cxpServer.Url = this.CXPServerWDSL.Text;
                // cxpsoap2.CXPServer.Response response = server.put(this.ccrData, "12345");
                // Set up the parameters
                cxpsoap2.CXP.RegistryParameters inputParameters = new cxpsoap2.CXP.RegistryParameters();
                inputParameters.registryName = "MedCommons";
                inputParameters.registryId = "medcommons.net";
            
   
                cxpsoap2.CXP.Parameter []parameterList = new cxpsoap2.CXP.Parameter[4];
                parameterList[0] = new cxpsoap2.CXP.Parameter();
                parameterList[0].name = "CommonsID";
                parameterList[0].value = this.CommonsID.Text;

                parameterList[1] = new cxpsoap2.CXP.Parameter();
                parameterList[1].name = "SenderID";
                parameterList[1].value = this.SenderID.Text;

                parameterList[2] = new cxpsoap2.CXP.Parameter();
                parameterList[2].name = "RegistrySecret";
                parameterList[2].value = this.RegistrySecret.Text;

                parameterList[3] = new cxpsoap2.CXP.Parameter();
                parameterList[3].name = "NotificationSubject";
                parameterList[3].value = this.NotificationSubject.Text;
            
                inputParameters.parameters = parameterList;
            
                cxpsoap2.CXP.PutResponse response = null;

                // Send the PUT message
                try
                {
                    response = cxpServer.put(ccrData, inputParameters);
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Unknown error communicating with server " +this.CXPServerWDSL.Text 
                        + "\n" + ex.ToString()); 
                    return;
                }

                clearDisplay();
            
                if (response.status == 200)
                {
                    handleOutputParameters(response.registryParameters);
                    this.GUID.Text = response.guid;
                    MessageBox.Show(this, "CXP Transfer Success!\nUUID:" + response.guid + 
                        "\nConfirmationCode:" + this.ConfirmationCode.Text +
                        "\nRegistrySecret:" + this.RegistrySecret.Text
                        );
                }
                else if (response.status < 299)
                {
                    handleOutputParameters(response.registryParameters);
                    this.GUID.Text = response.guid;
                    MessageBox.Show(this, "CXP Transfer Success (with warnings)\nUUID:" + response.guid+
                        "\nConfirmationCode:" + this.ConfirmationCode.Text +
                        "\nRegistrySecret:" + this.RegistrySecret.Text +
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
            catch(Exception ex)
            {
                MessageBox.Show("Unexpected error:" + ex.ToString());
                return;
            }

        }

        private void button1_Click_1(object sender, System.EventArgs e)
        {
            
            OpenFileDialog dlg = new OpenFileDialog();
            if(dlg.ShowDialog(this) == DialogResult.OK) 
            {
                this.CCRFileName.Text = dlg.FileName;
            }
        }

        private void label15_Click(object sender, System.EventArgs e)
        {
        
        }
        private void clearDisplay()
        {
            this.CommonsID.Text = "";
            this.RegistrySecret.Text = "";
            this.ConfirmationCode.Text = "";
           

        }
        private void handleOutputParameters(cxpsoap2.CXP.RegistryParameters []outputParameters)
        {
            
            if (outputParameters==null) return;
            String displayParameters = "Parameters:";
            // Note: according to the protocol there may be multiple sets of parameters from
            // different directories. For this demonstration program we're only using the 
            // first one.
            cxpsoap2.CXP.RegistryParameters output = outputParameters[0];
            cxpsoap2.CXP.Parameter [] list = output.parameters;
            for (int i=0;i<list.Length;i++)
            {
                cxpsoap2.CXP.Parameter parameter = list[i];
                if (parameter.name.Equals("CommonsId"))
                    this.CommonsID.Text = parameter.value;
                else if (parameter.name.Equals("RegistrySecret"))
                    this.RegistrySecret.Text = parameter.value;
                else if (parameter.name.Equals("ConfirmationCode"))
                    this.ConfirmationCode.Text = parameter.value;
                else if (parameter.name.Equals("SenderId"))
                    this.SenderID.Text = parameter.value;
                else
                    ; // Noop
                displayParameters += "\n name=" + parameter.name + ", value=" + parameter.value;
            }
           //MessageBox.Show(this, displayParameters);
        }
        
        private void init()
        {
            patientServer = new  cxpsoap2.PatientQuery.PatientCCRServerService();
            cxpServer = new cxpsoap2.CXP.CXPServer2Service();
            this.MetadataServerWDSL.Text = patientServer.Url;
            this.CXPServerWDSL.Text = cxpServer.Url;
            this.SenderID.Text =  System.Security.Principal.WindowsIdentity.GetCurrent().Name.ToString();
        }

        private void label16_Click(object sender, System.EventArgs e)
        {
        
        }

        /**
         * Handles the GET button click.
         * */
        private void GET_Click(object sender, System.EventArgs e)
        {
            cxpServer.Url = this.CXPServerWDSL.Text;
            // cxpsoap2.CXPServer.Response response = server.put(this.ccrData, "12345");
            // Set up the parameters
            cxpsoap2.CXP.RegistryParameters inputParameters = new cxpsoap2.CXP.RegistryParameters();
            inputParameters.registryName = "MedCommons";
            inputParameters.registryId = "medcommons.net";
            
   
            cxpsoap2.CXP.Parameter []parameterList = new cxpsoap2.CXP.Parameter[5];
            parameterList[0] = new cxpsoap2.CXP.Parameter();
            parameterList[0].name = "CommonsId";
            parameterList[0].value = this.CommonsID.Text;

            parameterList[1] = new cxpsoap2.CXP.Parameter();
            parameterList[1].name = "SenderId";
            parameterList[1].value = this.SenderID.Text;

            parameterList[2] = new cxpsoap2.CXP.Parameter();
            parameterList[2].name = "RegistrySecret";
            if (this.RegistrySecret.Text.Length == 0)
                parameterList[2].value = null;
            else if (this.RegistrySecret.Text.Length == 5)
            {
                parameterList[2].value = this.RegistrySecret.Text;
            }
            else
            {
                MessageBox.Show(this, "Invalid RegistrySecret: Must be a 5 integer string or blank, not '" + this.RegistrySecret.Text + "'");
                return;
            }

            parameterList[3] = new cxpsoap2.CXP.Parameter();
            parameterList[3].name = "NotificationSubject";
            parameterList[3].value = this.NotificationSubject.Text;

            parameterList[4] = new cxpsoap2.CXP.Parameter();
            parameterList[4].name = "ConfirmationCode";
            parameterList[4].value = this.ConfirmationCode.Text;
            
            inputParameters.parameters = parameterList;


           
            cxpsoap2.CXP.GetResponse response = cxpServer.get(inputParameters);
            String output = "GET Response:";
            if (response.status == 200)
            {
                output += "\n First 200 characters of CCR\n";
                output += response.content.Substring(0,200);
                
                
            }
            else
            {
                output += "\n Status = " + response.status;
                output += "\n Reason = " + response.reason;
            }
            
            MessageBox.Show(this, output);
        }

        
    }

  
}

