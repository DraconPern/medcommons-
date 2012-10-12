﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using NHINDirect.Agent;
using NHINDirect.Certificates;
using System.Security.Cryptography.X509Certificates;

namespace AgentTests
{
    /// <summary>
    /// Helper Class that does Chores needed by actual tests...
    /// </summary>
    public class AgentTester
    {
        public const string DefaultDomainA = "redmond.hsgincubator.com";
        public const string DefaultDomainB = "nhind.hsgincubator.com";
        
        NHINDAgent m_agentA;
        NHINDAgent m_agentB;
        string m_messageFolder;
        
        public AgentTester(NHINDAgent agentA, NHINDAgent agentB)
        {
            if (agentA == null || agentB == null)
            {
                throw new ArgumentNullException();
            }
            
            m_agentA = agentA;
            m_agentB = agentB;
            m_messageFolder = Path.Combine(Directory.GetCurrentDirectory(), "TestMessages");
        }
        
        public NHINDAgent AgentA
        {
            get
            {
                return m_agentA;
            }
        }
        
        public NHINDAgent AgentB
        {
            get
            {
                return m_agentB;
            }
        }
        
        public string MessageFolder
        {
            get
            {
                return m_messageFolder;
            }
            set
            {
                if (string.IsNullOrEmpty(value))
                {
                    throw new ArgumentException();
                }
                
                if (!Directory.Exists(value))
                {
                    throw new DirectoryNotFoundException(value);
                }
                
                m_messageFolder = value;
            }
        }
        
        public void TestEndToEndFile(string messageFilePath)
        {
            this.ProcessEndToEnd(this.ReadMessageText(messageFilePath));
        }
        
        public string ProcessEndToEnd(string messageText)
        {
            string outgoingText = this.ProcessOutgoingToString(messageText);
            string incomingText = this.ProcessIncomingToString(outgoingText);            
            return incomingText;
        }

        public string ProcessOutgoingFileToString(string messageFilePath)
        {
            return this.ProcessOutgoingToString(this.ReadMessageText(messageFilePath));
        }

        public string ProcessOutgoingToString(string messageText)
        {
            return this.ProcessOutgoing(messageText).SerializeMessage();
        }

        public OutgoingMessage ProcessOutgoingFile(string messageFilePath)
        {
            return this.ProcessOutgoing(this.ReadMessageText(messageFilePath));
        }

        public OutgoingMessage ProcessOutgoing(string messageText)
        {
            return m_agentA.ProcessOutgoing(messageText);
        }

        public void ProcessIncomingFile(string messageFilePath)
        {
            this.ProcessIncomingToString(this.ReadMessageText(messageFilePath));
        }

        public string ProcessIncomingToString(string messageText)
        {
            return m_agentB.ProcessIncoming(messageText).SerializeMessage();
        }
        
        public string ReadMessageText(string messageFilePath)
        {
            if (!Path.IsPathRooted(messageFilePath))
            {
                messageFilePath = Path.Combine(m_messageFolder, messageFilePath);
            }
            
            return File.ReadAllText(messageFilePath);
        }
        
        public static AgentTester CreateDefault()
        {
            NHINDAgent agentA = new NHINDAgent(AgentTester.DefaultDomainA);
            NHINDAgent agentB = new NHINDAgent(AgentTester.DefaultDomainB);
            
            return new AgentTester(agentA, agentB);
        }
        
        public static AgentTester CreateTest()
        {
            return CreateTest(Directory.GetCurrentDirectory());
        }
        
        public static AgentTester CreateTest(string basePath)
        {
            NHINDAgent agentA = CreateAgent(AgentTester.DefaultDomainA, MakeCertificatesPath(basePath, "redmond"));
            NHINDAgent agentB = CreateAgent(AgentTester.DefaultDomainB, MakeCertificatesPath(basePath, "nhind"));
            return new AgentTester(agentA, agentB);
        }
        
        public static NHINDAgent CreateAgent(string domain, string certsBasePath)
        {
            MemoryX509Store privateCerts = LoadPrivateCerts(certsBasePath);
            MemoryX509Store publicCerts = LoadPublicCerts(certsBasePath);
            TrustAnchorResolver anchors = new TrustAnchorResolver(
                                                (IX509CertificateStore) LoadIncomingAnchors(certsBasePath),
                                                (IX509CertificateStore) LoadOutgoingAnchors(certsBasePath));

            return new NHINDAgent(domain, privateCerts.Index(), publicCerts.Index(), anchors);
        }
        
        static string MakeCertificatesPath(string basePath, string agentFolder)
        {
            return Path.Combine(basePath, Path.Combine("Certificates", agentFolder));
        }
        
        static MemoryX509Store LoadPrivateCerts(string certsBasePath)
        {
            return LoadCertificates(Path.Combine(certsBasePath, "Private"));
        }

        static MemoryX509Store LoadPublicCerts(string certsBasePath)
        {
            return LoadCertificates(Path.Combine(certsBasePath, "Public"));
        }
        
        static MemoryX509Store LoadIncomingAnchors(string certsBasePath)
        {
            return LoadCertificates(Path.Combine(certsBasePath, "IncomingAnchors"));
        }

        static MemoryX509Store LoadOutgoingAnchors(string certsBasePath)
        {
            return LoadCertificates(Path.Combine(certsBasePath, "OutgoingAnchors"));
        }
        
        public static MemoryX509Store LoadCertificates(string folderPath)
        {
            if (string.IsNullOrEmpty(folderPath))
            {
                throw new ArgumentException();
            }
            
            if (!Directory.Exists(folderPath))
            {
                throw new DirectoryNotFoundException("Directory not found: " + folderPath);
            }
            
            MemoryX509Store certStore = new MemoryX509Store();
            
            string[] files = Directory.GetFiles(folderPath);
            for (int i = 0; i < files.Length; ++i)
            {
                string file = files[i];
                string ext = Path.GetExtension(file) ?? string.Empty;
                ext = ext.ToLower();
                
                switch(ext)
                {
                    default:
                        certStore.ImportKeyFile(file, X509KeyStorageFlags.DefaultKeySet);
                        break;
                    
                    case ".pfx":
                        certStore.ImportKeyFile(file, "passw0rd!", X509KeyStorageFlags.DefaultKeySet);
                        break;
                }
            } 
            
            return certStore;           
        }
        
        /// <summary>
        /// Sets up standard stores for Testing
        /// WARNING: This may require elevated permissions
        /// </summary>
        public static void EnsureStandardMachineStores()
        {
            SystemX509Store.CreateAll();
            
            string basePath = Directory.GetCurrentDirectory();
            string redmondCertsPath = MakeCertificatesPath(basePath, "redmond");
            string nhindCertsPath = MakeCertificatesPath(basePath, "nhind");
            
            SystemX509Store store;
            using(store = SystemX509Store.OpenPrivateEdit())
            {
                InstallCerts(store, LoadPrivateCerts(redmondCertsPath));
                InstallCerts(store, LoadPrivateCerts(nhindCertsPath));
            }

            using (store = SystemX509Store.OpenExternalEdit())
            {
                InstallCerts(store, LoadPublicCerts(redmondCertsPath));
                InstallCerts(store, LoadPublicCerts(nhindCertsPath));
            }

            using (store = SystemX509Store.OpenAnchorEdit())
            {
                InstallCerts(store, LoadIncomingAnchors(redmondCertsPath));
                InstallCerts(store, LoadOutgoingAnchors(redmondCertsPath));

                InstallCerts(store, LoadIncomingAnchors(nhindCertsPath));
                InstallCerts(store, LoadOutgoingAnchors(nhindCertsPath));
            }
        }        
                
        static void InstallCerts(IX509CertificateStore store, IEnumerable<X509Certificate2> certs)
        {
            foreach(X509Certificate2 cert in certs)
            {
                if (!store.Contains(cert))
                {
                    store.Add(cert);
                }
            }
        }
    }
}