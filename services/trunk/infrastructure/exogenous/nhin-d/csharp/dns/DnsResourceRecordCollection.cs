﻿/* 
 Copyright (c) 2010, NHIN Direct Project
 All rights reserved.

 Authors:
    Umesh Madan     umeshma@microsoft.com
    Sean Nolan      seannol@microsoft.com
 
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

namespace DnsResolver
{
    public class DnsResourceRecordCollection : List<DnsResourceRecord>
    {
        public DnsResourceRecordCollection()
        {
        }
        
        public DnsResourceRecordCollection(int capacity)
            : base(capacity)
        {
        }
        
        public IEnumerable<RawRecord> Raw
        {
            get
            {
                for (int i = 0, count = this.Count; i < count; ++i)
                {
                    RawRecord raw = this[i] as RawRecord;
                    if (raw != null)
                    {
                        yield return raw;
                    }
                }
            }
        }
        
        public IEnumerable<AddressRecord> A
        {
            get
            {
                return this.Enumerate<AddressRecord>(Dns.RecordType.ANAME);
            }
        }
        
        public IEnumerable<PtrRecord> PTR
        {
            get
            {
                return this.Enumerate<PtrRecord>(Dns.RecordType.PTR);
            }
        }
        
        public IEnumerable<NSRecord> NS
        {
            get
            {
                return this.Enumerate<NSRecord>(Dns.RecordType.NS);
            }
        }
        
        public IEnumerable<MXRecord> MX
        {
            get
            {
                return this.Enumerate<MXRecord>(Dns.RecordType.MX);
            }
        }
        
        public IEnumerable<TextRecord> TXT
        {
            get
            {
                return this.Enumerate<TextRecord>(Dns.RecordType.TXT);
            }
        }

        public IEnumerable<CertRecord> CERT
        {
            get
            {
                return this.Enumerate<CertRecord>(Dns.RecordType.CERT);
            }
        }
        
        public IEnumerable<SOARecord> SOA
        {
            get
            {
                return this.Enumerate<SOARecord>(Dns.RecordType.SOA);
            }
        }
        
        public IEnumerable<T> Enumerate<T>(Dns.RecordType type)
            where T : DnsResourceRecord
        {
            foreach(DnsResourceRecord record in this)
            {
                if (record.Type == type)
                {
                    T typedRecord = record as T;
                    if (typedRecord != null)
                    {
                        yield return typedRecord;
                    }
                }
            }
        }
        
        internal void Deserialize(int recordCount, ref DnsBufferReader reader)
        {
            if (recordCount < 0)
            {
                throw new DnsProtocolException(DnsProtocolError.InvalidRecordCount);
            }

            if (recordCount > 0)
            {
                this.EnsureCapacity(recordCount);
                for (int irecord = 0; irecord < recordCount; ++irecord)
                {
                    this.Add(DnsResourceRecord.Deserialize(ref reader));
                }
            }
        }
        
        internal void EnsureCapacity(int capacity)
        {
            if (capacity > this.Capacity)
            {
                this.Capacity = capacity;
            }
        }
    }
}