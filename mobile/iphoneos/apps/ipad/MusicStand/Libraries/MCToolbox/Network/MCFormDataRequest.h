//
//  MCFormDataRequest.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "ASIFormDataRequest.h"  // <ASIHTTPRequest/ASIFormDataRequest.h>

//
// Error domains:
//
extern NSString *const MCFormDataRequestErrorDomain;

//
// Error codes (returned in NSError):
//
enum
{
    MCFormDataRequestErrorUnknown             = 2000,
    MCFormDataRequestErrorUnsupportedMIMEType
};

@interface MCFormDataRequest : ASIFormDataRequest <NSCopying>
{
@private

    NSTimeInterval  finishTime_;
    NSTimeInterval  startTime_;
    NSSet          *supportedUTIs_;
}

@property (assign, readonly)  NSTimeInterval  finishTime;
@property (assign, readonly)  NSTimeInterval  startTime;
@property (copy,   readwrite) NSSet          *supportedUTIs;

@end

@protocol MCFormDataRequestDelegate <ASIHTTPRequestDelegate>

// Just so the delegate name matches that of the subclass ...

@end

