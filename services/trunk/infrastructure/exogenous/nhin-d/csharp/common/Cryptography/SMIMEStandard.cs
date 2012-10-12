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
using System.Net.Mime;

using NHINDirect.Mail;
using NHINDirect.Mime;

namespace NHINDirect.Cryptography
{
    public class SMIMEStandard : MailStandard
    {
        //
        // MIME Types
        //
        public const string MultiPartTypeSigned = "multipart/signed";
        public const string ProtocolParameterKey = "protocol";
        public const string SignatureProtocol = "application/pkcs7-signature";
        public const string MICAlgorithmKey = "micalg"; // Message Integrity Check Protocol        
        //
        // Cryptography
        //
        public const string CmsEnvelopeMediaType = "application/pkcs7-mime";
        public const string CmsEnvelopeMediaTypeAlt = "application/x-pkcs7-mime";   // we are forgiving when we receive messages
        
        public const string EncryptedEnvelopeContentTypeHeaderValue = "application/pkcs7-mime; smime-type=enveloped-data; name=\"smime.p7m\"";
        public const string SignatureEnvelopeContentTypeHeaderValue = "application/pkcs7-mime; smime-type=signed-data; name=\"smime.p7\"";
        
        public const string SignatureContentTypeHeaderValue = "application/pkcs7-signature; name=\"smime.p7s\"";
        public const string SignatureContentMediaType = "application/pkcs7-signature";
        public const string SignatureContentMediaTypeAlternative = "application/x-pkcs7-signature"; // we are forgiving when we receive messages
        public const string SignatureDisposition = "attachment; filename=\"smime.p7s\"";
        
        public const string SmimeTypeParameterKey = "smime-type";
        public const string EnvelopedDataSmimeType = "enveloped-data";
        public const string SignedDataSmimeType = "signed-data";
        public const string DefaultFileName = "smime.p7m";
        
        public static bool IsContentCms(ContentType contentType)
        {
            if (contentType == null)
            {
                throw new ArgumentNullException();
            }

            return (   contentType.IsMediaType(CmsEnvelopeMediaType) 
                    || contentType.IsMediaType(CmsEnvelopeMediaTypeAlt));
        }
        
        public static bool IsContentEncrypted(ContentType contentType)
        {
            if (contentType == null)
            {
                throw new ArgumentNullException();
            }
            
            return (IsContentCms(contentType)
                    &&  contentType.IsParameter(SmimeTypeParameterKey, EnvelopedDataSmimeType));
        }
        
        public static bool IsContentEnvelopedSignature(ContentType contentType)
        {
            if (contentType == null)
            {
                throw new ArgumentNullException();
            }

            return (IsContentCms(contentType)
                    &&  contentType.IsParameter(SmimeTypeParameterKey, SignedDataSmimeType));
        }
        
        public static bool IsContentMultipartSignature(ContentType contentType)
        {
            if (contentType == null)
            {
                throw new ArgumentNullException();
            }

            return (contentType.IsMediaType(MultiPartTypeSigned));
        }
        
        public static bool IsContentDetachedSignature(ContentType contentType)
        {
            return (    contentType.IsMediaType(SignatureContentMediaType) 
                    ||  contentType.IsMediaType(SignatureContentMediaTypeAlternative));
        }
        
        public static bool IsEncrypted(MimeEntity entity)
        {
            return (IsContentEncrypted(entity.ParsedContentType) && VerifyEncoding(entity));
        }
                        
        public static bool IsSignedEnvelope(MimeEntity entity)
        {
            return (IsContentEnvelopedSignature(entity.ParsedContentType) && VerifyEncoding(entity));
        }

        public static bool IsDetachedSignature(MimeEntity entity)
        {
            return (IsContentDetachedSignature(entity.ParsedContentType) && VerifyEncoding(entity));
        }
                
        static bool VerifyEncoding(MimeEntity entity)
        {
            return entity.HasHeader(ContentTransferEncodingHeader, TransferEncodingBase64);
        }
    }
}