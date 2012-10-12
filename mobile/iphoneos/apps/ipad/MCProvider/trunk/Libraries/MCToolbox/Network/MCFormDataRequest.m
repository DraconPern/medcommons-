//
//  MCFormDataRequest.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  * Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of MedCommons, Inc. nor the names of its contributors
//    may be used to endorse or promote products derived from this software
//    without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

#import "JSON.h"                        // <JSON/JSON.h>
#import "MCFormDataRequest.h"
#import "NSError+MCToolbox.h"
#import "NSString+MCToolbox.h"

#pragma mark -
#pragma mark Private NSString Additions
#pragma mark -

@interface NSString (MCFormDataRequest)

- (NSDictionary *) dictionaryFromJSONFormat;

@end

@implementation NSString (MCFormDataRequest)

- (NSDictionary *) dictionaryFromJSONFormat
{
    SBJsonParser *parser = [[[SBJsonParser alloc] init]
                            autorelease];
    id            obj = [parser objectWithString: self];

    return ([obj isKindOfClass: [NSDictionary class]] ? obj : nil);
}

@end

#pragma mark -
#pragma mark Public Class MCFormDataRequest
#pragma mark -

#pragma mark Error Domains

NSString *const MCFormDataRequestErrorDomain = @"MCFormDataRequestErrorDomain";

@interface MCFormDataRequest ()

@property (assign, readwrite) NSTimeInterval finishTime;
@property (assign, readwrite) NSTimeInterval startTime;

- (NSError *) checkResponseMIMEType;

- (NSError *) checkResponseStatusCode;

@end

@implementation MCFormDataRequest

@synthesize finishTime    = finishTime_;
@synthesize startTime     = startTime_;
@synthesize supportedUTIs = supportedUTIs_;

#pragma mark Public Instance Methods

- (NSDictionary *) responseDictionary
{
    NSString *MIMEType = [self responseMIMEType];

    if (!MIMEType)
        return nil;

    BOOL isJSON = ([MIMEType caseInsensitiveCompare: @"application/json"] == NSOrderedSame);

    if (!isJSON)
    {
        NSArray *components = [MIMEType componentsSeparatedByString: @"/"];

        if ([[components objectAtIndex: 0] caseInsensitiveCompare: @"text"] != NSOrderedSame)
            return nil;
    }

    //
    // If here, response is either JSON format or some flavor of text:
    //
    NSString *strValue = [self responseString];

    if (!strValue)
        return nil;

    //
    // Always try parsing string from JSON format first:
    //
    NSDictionary *tmpValue = [strValue dictionaryFromJSONFormat];

    //
    // If it parsed successfully return; otherwise if response MIME type
    // explicitly claims JSON format return anyway:
    //
    if (tmpValue || isJSON)
        return tmpValue;

    //
    // Try parsing string from property list:
    //
    @try
    {
        id tmpObj = [strValue propertyList];

        if ([tmpObj isKindOfClass: [NSDictionary class]])
            return (NSDictionary *) tmpObj;
    }
    @catch (NSException *exc)
    {
        // keep going ...
    }

    //
    // Finally, try parsing string from property list in '.strings' file
    // format:
    //
    @try
    {
        return [strValue propertyListFromStringsFileFormat];
    }
    @catch (NSException *exc)
    {
        return nil;
    }
}

- (NSString *) responseMIMEType
{
    NSString *contentType = [[self responseHeaders] objectForKey: @"Content-Type"];

    if (contentType)
    {
        NSScanner *tmpScanner = [NSScanner scannerWithString: contentType];
        NSString  *MIMEType = nil;

        if ([tmpScanner scanUpToString: @";" intoString: &MIMEType])
            return [MIMEType stringByTrimmingWhitespace];
    }

    return nil;
}

#pragma mark Private Instance Methods

- (NSError *) checkResponseMIMEType
{
    NSString *tmpMIMEType = [self responseMIMEType];
    NSString *tmpUTI = [NSString preferredUTIForMIMEType: tmpMIMEType];

    for (NSString *UTI in self.supportedUTIs)
    {
        if ([tmpUTI conformsToUTI: UTI])
            return nil;
    }

#if TARGET_IPHONE_SIMULATOR
    //
    // Ignore simulator bug wherein MIME types are not correctly mapped to
    // "public" UTIs:
    //
    return nil;
#else
    NSString *locDesc = [NSString stringWithFormat:
                         NSLocalizedString (@"Unsupported MIME type: %@", @""),
                         tmpMIMEType];

    return [NSError errorWithDomain: MCFormDataRequestErrorDomain
                               code: MCFormDataRequestErrorUnsupportedMIMEType
               localizedDescription: locDesc];
#endif
}

- (NSError *) checkResponseStatusCode
{
    NSInteger tmpStatusCode = self.responseStatusCode;

    if ((tmpStatusCode / 100) != 2)    // 2xx = Successful
        return [NSError errorWithDomain: MCFormDataRequestErrorDomain
                                   code: tmpStatusCode
                   localizedDescription: self.responseStatusMessage];

    return nil;
}

#pragma mark Overridden ASIHTTPRequest Methods

- (void) requestFinished
{
    if (!self.error && !self.mainRequest)
        self.finishTime = [NSDate timeIntervalSinceReferenceDate];

    [super requestFinished];
}

- (void) requestReceivedResponseHeaders
{
    if (!self.error && !self.mainRequest)
    {
        NSError *tmpError = [self checkResponseStatusCode];

        if (!tmpError && self.supportedUTIs)
            tmpError = [self checkResponseMIMEType];

        if (tmpError)
            [self failWithError: tmpError];
    }

    [super requestReceivedResponseHeaders];
}

- (void) requestStarted
{
    if (!self.error && !self.mainRequest)
        self.startTime = [NSDate timeIntervalSinceReferenceDate];

    [super requestStarted];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->supportedUTIs_ release];

    [super dealloc];
}

#pragma mark Overridden NSCopying Methods

- (id) copyWithZone: (NSZone *) zone
{
    MCFormDataRequest *copy = [super copyWithZone: zone];

    copy.supportedUTIs = self.supportedUTIs;

    return copy;
}

@end
