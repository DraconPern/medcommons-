﻿/* 
 Copyright (c) 2010, NHIN Direct Project
 All rights reserved.

 Authors:
    Umesh Madan     umeshma@microsoft.com
  
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of the The NHIN Direct Project (nhindirect.org). nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
*/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Security.Cryptography.X509Certificates;
using NHINDirect.Certificates;

namespace NHINDirect.Agent
{
    public class TrustChainValidator
    {
        //
        // The following collection of status flags indicates a trust problem with the certificate
        //            
        public static readonly X509ChainStatusFlags DefaultProblemFlags =
                X509ChainStatusFlags.NotTimeValid |
                X509ChainStatusFlags.Revoked |
                X509ChainStatusFlags.NotSignatureValid |
                X509ChainStatusFlags.InvalidBasicConstraints |
                X509ChainStatusFlags.CtlNotTimeValid |
                X509ChainStatusFlags.CtlNotSignatureValid;
        
        X509ChainPolicy m_policy;
        X509ChainStatusFlags m_problemFlags;
        
        public TrustChainValidator()
        {
            m_policy = new X509ChainPolicy();
            m_policy.VerificationFlags = (X509VerificationFlags.IgnoreWrongUsage);            
            m_problemFlags = TrustChainValidator.DefaultProblemFlags;
        }
        
        public X509ChainPolicy ValidationPolicy
        {
            get
            {
                return m_policy;
            }
        }
        
        public X509ChainStatusFlags ProblemFlags
        {
            get
            {
                return m_problemFlags;
            }
            set
            {
                m_problemFlags = value;
            }
        }
        
        public bool IsTrustedCertificate(X509Certificate2 certificate, X509Certificate2Collection anchors)
        {
            if (certificate == null)
            {
                throw new ArgumentNullException();
            }
            //
            // if there are no anchors ... then they haven't configured anything so we should always fail
            //
            if (anchors == null)
            {
                return false;
            }

            X509Chain chainBuilder = new X509Chain();
            chainBuilder.ChainPolicy = m_policy.Clone();
            chainBuilder.ChainPolicy.ExtraStore.Add(anchors);
            
            try
            {
                //
                // We're using the system class as a helper to merely build the chain
                // However, we will review each item in the chain ourselves, because we have our own rules...
                //
                chainBuilder.Build(certificate);
                //
                // If we don't have a trust chain, then we obviously have a problem...
                //
                X509ChainElementCollection chainElements = chainBuilder.ChainElements;
                if (chainElements == null || chainElements.Count < 1)
                {
                    return false;
                }
                //
                // walk the chain starting at the leaf and see if we hit any issues before the anchor
                //
                for (int i = 0; i < chainElements.Count; ++i)
                {
                    X509ChainElement chainElement = chainBuilder.ChainElements[i];
                
                    if (this.ChainElementHasProblems(chainElement))
                    {
                        //
                        // Whoops... problem with at least one cert in the chain. Stop immediately
                        //
                        return false;
                    }

                    bool isAnchor = (anchors.FindByThumbprint(chainElement.Certificate.Thumbprint) != null);
                    if (isAnchor)
                    {
                        //
                        // Found a valid anchor!
                        // Because we found an anchor we trust, we can now trust the entire trust chain
                        //
                        return (true);
                    }
                }
            }
            catch
            {
                // just eat it and drop out to return false
            }

            return (false);
        }
                
        bool ChainElementHasProblems(X509ChainElement chainElement)
        {
            //
            // If the builder finds problems with the cert, it will provide a list of "status" flags for the cert
            // If the list is empty or the list is null, then there were NO problems with the cert
            //
            X509ChainStatus[] chainElementStatus = chainElement.ChainElementStatus;
            if (chainElementStatus == null)
            {
                return false;
            }

            for (int i = 0; i < chainElementStatus.Length; ++i)
            {
                X509ChainStatus status = chainElementStatus[i];

                if ((status.Status & m_problemFlags) != 0)
                {
                    return true;
                }
            }

            return false;
        }
    }
}