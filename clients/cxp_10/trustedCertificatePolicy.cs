//====================================================================
// trustedCertificatePolicy.cs
// A bit of a hack to get around SSL certs that don't have
// a trusted root or are otherwise invalid.
//
// Note: this is useful for testing purposes; a real system 
// should provide some tests here.
//====================================================================
using System;
using System.Net;
using System.Security;
using System.Security.Cryptography.X509Certificates;

namespace cxp
{
    public class trustedCertificatePolicy : System.Net.ICertificatePolicy
    {
        public trustedCertificatePolicy() {}

        public bool CheckValidationResult
            (
            System.Net.ServicePoint sp,
            System.Security.Cryptography.X509Certificates.X509Certificate certificate,
            System.Net.WebRequest request, int problem)
        {
            return true;
        }
    }
}