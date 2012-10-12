/**
 * Copyright 2006; Some Rights Reserved
 * 
 * This sample program is licensed under the Creative Commons Attribution
 * license. For more details see:
 *  http://creativecommons.org/licenses/by/2.5/
 * We request attribution to both MedCommons and the CCR Accelerator group.
 * 
 * Sample CXP 1.0 SOAP Client program in C#
 * 
 * This program demonstrates the SOAP CXP interface for 
 *   (a) sending a document via PUT
 *   (b) receiving a document via GET
 * 
 *
 * 
 * How to use the program:
 * 
 * The sample program consists of some text fields for input and output parameters;
 * the URL of the service endpoint can also be changed. 
 * 
 * At a minimum the service endpoint needs to be present (the default is
 * https://gateway001.medcommons.net:8443/router/services/CXP) and a CCR file 
 * needs to be located with the 'Browse' button. 
 * 
 * 
 * (1) The 'PUT' button that sends a CCR to the specified CXP server. On output
 *     the CommonsId, ConfirmationCode, RegistrySecret, and GUID are filled in. 
 *     The CommonsId and RegistrySecret may be filled in by the client - these
 *     same values are then returned. If blank, values are created by the CXP 
 *     reciever.
 * 
 * (2) A 'Get' button that gets a CCR based on the RegistrySecret and ConfirmationCode. 
 *     The first 200 characters of the CCR are displayed on the screen. 
 * 
 *
 * Known client problems:
 * - This code does not do any (well -very little) error checking.
 * - Invalid SSL certificates are accepted - see the class trustedCertificatePolicy. 
 *   This is extremely useful for testing purposes; it's not what you'd want in 
 *   a production environment.
 * - The GET button should probably offer to save the CCR to disk. Instead
 *   only the first 200 characters are displayed in a message box.
 * - The comments need some work.
 * - The PUT command returns an array of RegistryParameters; only the first is
 *   examined in this program. A more general sample would look at all returned
 *   values.

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

namespace cxp
{

    /// <summary>
    /// Summary description for ClientForm.
    /// </summary>
    public class ClientForm : System.Windows.Forms.Form
    {
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.TextBox CXPServerWDSL;
        private System.Windows.Forms.TextBox CommonsID;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label label11;
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

        
        private CXP.CXP_10Service cxpServer;
        private System.Windows.Forms.TextBox ConfirmationCode;
        private System.Windows.Forms.TextBox RegistrySecret;
        private System.Windows.Forms.Button GET;
        private System.Windows.Forms.Button putButton;



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
            this.button2 = new System.Windows.Forms.Button();
            this.label8 = new System.Windows.Forms.Label();
            this.CXPServerWDSL = new System.Windows.Forms.TextBox();
            this.label9 = new System.Windows.Forms.Label();
            this.CommonsID = new System.Windows.Forms.TextBox();
            this.label10 = new System.Windows.Forms.Label();
            this.label11 = new System.Windows.Forms.Label();
            this.CCRFileName = new System.Windows.Forms.TextBox();
            this.browseButton = new System.Windows.Forms.Button();
            this.putButton = new System.Windows.Forms.Button();
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
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(448, 384);
            this.button2.Name = "button2";
            this.button2.TabIndex = 5;
            this.button2.Text = "Close";
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // label8
            // 
            this.label8.Font = new System.Drawing.Font("Arial", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label8.ForeColor = System.Drawing.Color.Green;
            this.label8.Location = new System.Drawing.Point(16, 24);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(248, 23);
            this.label8.TabIndex = 14;
            this.label8.Text = "CXP 1.0 Demonstration Client";
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
            // 
            // label11
            // 
            this.label11.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label11.Location = new System.Drawing.Point(16, 112);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(72, 16);
            this.label11.TabIndex = 19;
            this.label11.Text = "File:";
            // 
            // CCRFileName
            // 
            this.CCRFileName.Location = new System.Drawing.Point(120, 112);
            this.CCRFileName.Name = "CCRFileName";
            this.CCRFileName.Size = new System.Drawing.Size(304, 20);
            this.CCRFileName.TabIndex = 20;
            this.CCRFileName.Text = "";
            // 
            // browseButton
            // 
            this.browseButton.Location = new System.Drawing.Point(448, 112);
            this.browseButton.Name = "browseButton";
            this.browseButton.Size = new System.Drawing.Size(112, 23);
            this.browseButton.TabIndex = 21;
            this.browseButton.Text = "Browse";
            this.browseButton.Click += new System.EventHandler(this.browseButton_Click);
            // 
            // putButton
            // 
            this.putButton.Location = new System.Drawing.Point(448, 248);
            this.putButton.Name = "putButton";
            this.putButton.Size = new System.Drawing.Size(112, 23);
            this.putButton.TabIndex = 41;
            this.putButton.Text = "PUT";
            this.putButton.Click += new System.EventHandler(this.putButton_Click);
            // 
            // label12
            // 
            this.label12.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((System.Byte)(0)));
            this.label12.Location = new System.Drawing.Point(0, 280);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(120, 16);
            this.label12.TabIndex = 23;
            this.label12.Text = "ConfirmationCode";
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
            this.ConfirmationCode.Location = new System.Drawing.Point(120, 280);
            this.ConfirmationCode.Name = "ConfirmationCode";
            this.ConfirmationCode.Size = new System.Drawing.Size(304, 20);
            this.ConfirmationCode.TabIndex = 45;
            this.ConfirmationCode.Text = "";
            // 
            // RegistrySecret
            // 
            this.RegistrySecret.Location = new System.Drawing.Point(120, 208);
            this.RegistrySecret.Name = "RegistrySecret";
            this.RegistrySecret.Size = new System.Drawing.Size(304, 20);
            this.RegistrySecret.TabIndex = 35;
            this.RegistrySecret.Text = "";
            // 
            // GUID
            // 
            this.GUID.Location = new System.Drawing.Point(120, 336);
            this.GUID.Name = "GUID";
            this.GUID.Size = new System.Drawing.Size(304, 20);
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
            // 
            // NotificationSubject
            // 
            this.NotificationSubject.Location = new System.Drawing.Point(120, 248);
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
            // 
            // ClientForm
            // 
            this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
            this.ClientSize = new System.Drawing.Size(592, 430);
            this.Controls.Add(this.label16);
            this.Controls.Add(this.NotificationSubject);
            this.Controls.Add(this.SenderID);
            this.Controls.Add(this.GUID);
            this.Controls.Add(this.RegistrySecret);
            this.Controls.Add(this.ConfirmationCode);
            this.Controls.Add(this.CCRFileName);
            this.Controls.Add(this.CommonsID);
            this.Controls.Add(this.CXPServerWDSL);
            this.Controls.Add(this.label15);
            this.Controls.Add(this.GET);
            this.Controls.Add(this.label14);
            this.Controls.Add(this.label13);
            this.Controls.Add(this.label12);
            this.Controls.Add(this.putButton);
            this.Controls.Add(this.browseButton);
            this.Controls.Add(this.label11);
            this.Controls.Add(this.label10);
            this.Controls.Add(this.label9);
            this.Controls.Add(this.label8);
            this.Controls.Add(this.button2);
            this.Name = "ClientForm";
            this.Activated += new System.EventHandler(this.OnActivated);
            this.ResumeLayout(false);

        }
        #endregion

        private void browseButton_Click(object sender, System.EventArgs e)
        {
            OpenFileDialog dlg = new OpenFileDialog();
            if(dlg.ShowDialog(this) == DialogResult.OK) 
            {
                this.CCRFileName.Text = dlg.FileName;
            }
        }

       

        public static void Main(string [] args) 
        {
            ClientForm cf = new ClientForm();
            // cf.InitializeComponent();
            System.Net.ServicePointManager.CertificatePolicy = new cxp.trustedCertificatePolicy();
            Application.Run(cf);
        }

        /**
         * Handles the GET button click.
         * */
        private void GET_Click(object sender, System.EventArgs e)
        {
            cxpServer.Url = this.CXPServerWDSL.Text;
            // cxp.CXPServer.Response response = server.put(this.ccrData, "12345");
            // Set up the parameters
            cxp.CXP.RegistryParameters []inputParameters = new cxp.CXP.RegistryParameters[1];
            cxp.CXP.RegistryParameters inputParameter = new cxp.CXP.RegistryParameters();
            inputParameters[0] = inputParameter;
            inputParameter.registryName = "MedCommons";
            inputParameter.registryId = "medcommons.net";
            
   
            cxp.CXP.Parameter []parameterList = new cxp.CXP.Parameter[5];
            parameterList[0] = new cxp.CXP.Parameter();
            parameterList[0].name = "CommonsId";
            parameterList[0].value = this.CommonsID.Text;

            parameterList[1] = new cxp.CXP.Parameter();
            parameterList[1].name = "SenderId";
            parameterList[1].value = this.SenderID.Text;

            parameterList[2] = new cxp.CXP.Parameter();
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

            parameterList[3] = new cxp.CXP.Parameter();
            parameterList[3].name = "NotificationSubject";
            parameterList[3].value = this.NotificationSubject.Text;

            parameterList[4] = new cxp.CXP.Parameter();
            parameterList[4].name = "ConfirmationCode";
            parameterList[4].value = this.ConfirmationCode.Text;
            
            inputParameter.parameters = parameterList;


           
            cxp.CXP.GetResponse response = cxpServer.get(inputParameters);
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

        private void putButton_Click(object sender, System.EventArgs e)
        {
            try
            {
       
                System.IO.StreamReader reader = new System.IO.StreamReader(this.CCRFileName.Text);
                String ccrData = reader.ReadToEnd();
                reader.Close();
      
            
           
                cxpServer.Url = this.CXPServerWDSL.Text;
                // cxp.CXPServer.Response response = server.put(this.ccrData, "12345");
                // Set up the parameters
                cxp.CXP.RegistryParameters[] inputParameters = new cxp.CXP.RegistryParameters[1];
                cxp.CXP.RegistryParameters inputParameter = new cxp.CXP.RegistryParameters();
                inputParameters[0] = inputParameter;
                inputParameter.registryName = "MedCommons";
                inputParameter.registryId = "medcommons.net";
            
   
                cxp.CXP.Parameter []parameterList = new cxp.CXP.Parameter[4];
                parameterList[0] = new cxp.CXP.Parameter();
                parameterList[0].name = "CommonsID";
                parameterList[0].value = this.CommonsID.Text;

                parameterList[1] = new cxp.CXP.Parameter();
                parameterList[1].name = "SenderID";
                parameterList[1].value = this.SenderID.Text;

                parameterList[2] = new cxp.CXP.Parameter();
                parameterList[2].name = "RegistrySecret";
                parameterList[2].value = this.RegistrySecret.Text;

                parameterList[3] = new cxp.CXP.Parameter();
                parameterList[3].name = "NotificationSubject";
                parameterList[3].value = this.NotificationSubject.Text;
            
                inputParameter.parameters = parameterList;
            
                cxp.CXP.PutResponse response = null;

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

        private void button2_Click(object sender, System.EventArgs e)
        {
            Application.Exit();
        }

        

        private void OnActivated(object sender, System.EventArgs e)
        {
        
          
        }

        private void cxpServer_TextChanged(object sender, System.EventArgs e)
        {
        
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
        private void handleOutputParameters(cxp.CXP.RegistryParameters []outputParameters)
        {
            
            if (outputParameters==null) return;
            String displayParameters = "Parameters:";
            // Note: according to the protocol there may be multiple sets of parameters from
            // different directories. For this demonstration program we're only using the 
            // first one.
            cxp.CXP.RegistryParameters output = outputParameters[0];
            cxp.CXP.Parameter [] list = output.parameters;
            for (int i=0;i<list.Length;i++)
            {
                cxp.CXP.Parameter parameter = list[i];
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
           
            cxpServer = new cxp.CXP.CXP_10Service();
            this.CXPServerWDSL.Text = cxpServer.Url;
            this.SenderID.Text =  System.Security.Principal.WindowsIdentity.GetCurrent().Name.ToString();
        }

        private void label16_Click(object sender, System.EventArgs e)
        {
        
        }

       

       

      
        

       

       

        

        
    }

  
}

