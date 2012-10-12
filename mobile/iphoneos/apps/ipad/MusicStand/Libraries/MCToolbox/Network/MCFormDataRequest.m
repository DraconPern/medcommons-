//
//  MCFormDataRequest.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "ASIHTTPRequest+MCToolbox.h"
#import "JSON.h"                        // <JSON/JSON.h>
#import "MCFormDataRequest.h"
#import "NSError+MCToolbox.h"
#import "NSString+MCToolbox.h"

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
