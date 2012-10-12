//
//  MCNetworkReachability.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/15/10.
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
//  Based on parts of Apple's Reachability sample class as presented in
//  <http://developer.apple.com/iphone/library/samplecode/Reachability>. It
//  has been extensively modified.
//

#import <netinet/in.h>

#import "MCNetworkReachability.h"

#define IS_FLAG_SET(flags, flag) (((flags) & (flag)) == (flag))

#pragma mark -
#pragma mark Public Class MCNetworkReachability
#pragma mark -

#pragma mark Internal Function Prototypes

static void ReachabilityCallback (SCNetworkReachabilityRef target,
                                  SCNetworkReachabilityFlags flags,
                                  void *info);

@interface MCNetworkReachability ()

@property (nonatomic, assign, readwrite)                       SCNetworkReachabilityFlags flags;
@property (nonatomic, assign, readwrite, getter = isLocalWiFi) BOOL                       localWiFi;
@property (nonatomic, assign, readwrite)                       SCNetworkReachabilityRef   ref;
@property (nonatomic, assign, readwrite, getter = isScheduled) BOOL                       scheduled;

- (SCNetworkReachabilityFlags) flagsSynchronously;

- (BOOL) scheduleCallback;

- (BOOL) unscheduleCallback;

@end

#pragma mark Internal Functions

static void ReachabilityCallback (SCNetworkReachabilityRef target,
                                  SCNetworkReachabilityFlags flags,
                                  void *info)
{
    NSCAssert (info != NULL,
               @"Null info!");

    if  ([(NSObject *) info isKindOfClass: [MCNetworkReachability class]])
    {
        MCNetworkReachability *reach = (MCNetworkReachability *) info;

        reach.flags = [reach flagsSynchronously];   // set updated flags

        if ([reach.delegate respondsToSelector: @selector (reachabilityDidChange:)])
            [reach.delegate reachabilityDidChange: reach];
    }
    else
        NSCAssert (NO,
                   @"Invalid class for info!");
}

@implementation MCNetworkReachability

@dynamic    connectionOnDemand;
@dynamic    connectionOnTraffic;
@dynamic    connectionRequired;
@synthesize delegate             = delegate_;
@synthesize flags                = flags_;
@dynamic    formattedFlags;
@dynamic    formattedStatus;
@dynamic    interventionRequired;
@dynamic    isDirect;
@dynamic    isLocalAddress;
@dynamic    isWWAN;
@dynamic    localWiFi;
@dynamic    reachable;
@synthesize ref                  = ref_;
@dynamic    scheduled;
@dynamic    status;
@dynamic    transientConnection;

#pragma mark Public Class Methods

+ (MCNetworkReachability *) reachabilityForInternetConnection
{
    struct sockaddr_in zeroAddress;

    bzero (&zeroAddress, sizeof (zeroAddress));

    zeroAddress.sin_family = AF_INET;
    zeroAddress.sin_len = sizeof (zeroAddress);

    return [self reachabilityWithAddress: (struct sockaddr *) &zeroAddress];
}

+ (MCNetworkReachability *) reachabilityForLocalWiFi
{
    struct sockaddr_in localWifiAddress;

    bzero (&localWifiAddress, sizeof(localWifiAddress));

    localWifiAddress.sin_family = AF_INET;
    localWifiAddress.sin_len = sizeof (localWifiAddress);

    //
    // IN_LINKLOCALNETNUM is defined in <netinet/in.h> as 169.254.0.0:
    //
    localWifiAddress.sin_addr.s_addr = htonl (IN_LINKLOCALNETNUM);

    MCNetworkReachability *reach = [self reachabilityWithAddress: (struct sockaddr *) &localWifiAddress];

    if (reach != NULL)
        reach.localWiFi = YES;

    return reach;
}

+ (MCNetworkReachability *) reachabilityWithAddress: (const struct sockaddr *) address
{
    SCNetworkReachabilityRef  ref = SCNetworkReachabilityCreateWithAddress (kCFAllocatorDefault,
                                                                            address);
    MCNetworkReachability    *reach = nil;

    if (ref != NULL)
    {
        reach = [[[self alloc] init] autorelease];

        if (reach != nil)
        {
            reach.ref = ref;
            reach.localWiFi = NO;
        }
    }

    return reach;
}

+ (MCNetworkReachability *) reachabilityWithLocalAddress: (const struct sockaddr *) localAddress
                                           remoteAddress: (const struct sockaddr *) remoteAddress
{
    SCNetworkReachabilityRef  ref = SCNetworkReachabilityCreateWithAddressPair (kCFAllocatorDefault,
                                                                                localAddress,
                                                                                remoteAddress);
    MCNetworkReachability    *reach = nil;

    if (ref != NULL)
    {
        reach = [[[self alloc] init] autorelease];

        if (reach != nil)
        {
            reach.ref = ref;
            reach.localWiFi = NO;
        }
    }

    return reach;
}

+ (MCNetworkReachability *) reachabilityWithName: (NSString *) name
{
    SCNetworkReachabilityRef  ref = SCNetworkReachabilityCreateWithName (kCFAllocatorDefault,
                                                                         [name UTF8String]);
    MCNetworkReachability    *reach = nil;

    if (ref != NULL)
    {
        reach = [[[self alloc] init] autorelease];

        if (reach != nil)
        {
            reach.ref = ref;
            reach.localWiFi = NO;
        }
    }

    return reach;
}

#pragma mark Public Instance Methods

- (BOOL) connectionOnDemand
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsConnectionOnDemand);
}

- (BOOL) connectionOnTraffic
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsConnectionOnTraffic);
}

- (BOOL) connectionRequired
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsConnectionRequired);
}

- (SCNetworkReachabilityFlags) flags
{
    if (!self.isScheduled)  // update flags synchronously
        self->flags_ = [self flagsSynchronously];

    return self->flags_;
}

- (NSString *) formattedFlags
{
    SCNetworkReachabilityFlags tmpFlags = self.flags;

    return [NSString stringWithFormat:
            @"[%@|%@|%@|%@|%@|%@|%@|%@|%@]",
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsTransientConnection)  ? @"TC"   : @"--"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsReachable)            ? @"R"    : @"-"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsConnectionRequired)   ? @"CR"   : @"--"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsConnectionOnTraffic)  ? @"COT"  : @"---"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsInterventionRequired) ? @"IR"   : @"--"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsConnectionOnDemand)   ? @"COD"  : @"---"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsIsLocalAddress)       ? @"LA"   : @"--"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsIsDirect)             ? @"D"    : @"-"),
            (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsIsWWAN)               ? @"WWAN" : @"----")];
}

- (NSString *) formattedStatus
{
    switch (self.status)
    {
        case MCNetworkStatusNotReachable :
            return NSLocalizedString (@"Not reachable", @"");

        case MCNetworkStatusReachableViaWiFi :
            return NSLocalizedString (@"Reachable via Wi-Fi", @"");

        case MCNetworkStatusReachableViaWWAN :
            return NSLocalizedString (@"Reachable via WWAN", @"");

        default :
            break;
    }

    return @"Unknown";
}

- (BOOL) interventionRequired
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsInterventionRequired);
}

- (BOOL) isDirect
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsIsDirect);
}

- (BOOL) isLocalAddress
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsIsLocalAddress);
}

- (BOOL) isLocalWiFi
{
    return self->localWiFi_;
}

- (BOOL) isScheduled
{
    return self->scheduled_;
}

- (BOOL) isWWAN
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsIsWWAN);
}

- (BOOL) reachable
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsReachable);
}

- (void) setDelegate: (id <MCNetworkReachabilityDelegate>) delegate
{
    if (self->delegate_ != delegate)
    {
        if (self->delegate_)
            [self unscheduleCallback];

        self->delegate_ = delegate;

        if (self->delegate_)
            [self scheduleCallback];
    }
}

- (void) setLocalWiFi: (BOOL) localWiFi
{
    self->localWiFi_ = localWiFi;
}

- (void) setScheduled: (BOOL) scheduled
{
    self->scheduled_ = scheduled;
}

- (MCNetworkStatus) status
{
    SCNetworkReachabilityFlags tmpFlags = self.flags;

    if (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsReachable))
    {
        if (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsIsWWAN))
            return MCNetworkStatusReachableViaWWAN;

        if ((self.isLocalWiFi &&
             IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsIsDirect)) ||
            !IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsConnectionRequired) ||
            (!IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsInterventionRequired) &&
             (IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsConnectionOnDemand) ||
              IS_FLAG_SET (tmpFlags, kSCNetworkReachabilityFlagsConnectionOnTraffic))))
            return MCNetworkStatusReachableViaWiFi;
    }

    return MCNetworkStatusNotReachable;
}

- (BOOL) transientConnection
{
    return IS_FLAG_SET (self.flags,
                        kSCNetworkReachabilityFlagsTransientConnection);
}

#pragma mark Private Instance Methods

- (SCNetworkReachabilityFlags) flagsSynchronously
{
    SCNetworkReachabilityFlags tmpFlags = 0;

    SCNetworkReachabilityGetFlags (self.ref, &tmpFlags);

    return tmpFlags;
}

- (BOOL) scheduleCallback
{
    if (!self.isScheduled)
    {
        SCNetworkReachabilityContext context = { 0, self, NULL, NULL, NULL };

        Boolean ok = SCNetworkReachabilitySetCallback (self.ref,
                                                       ReachabilityCallback,
                                                       &context);

        if (ok)
        {
            ok = SCNetworkReachabilityScheduleWithRunLoop (self.ref,
                                                           CFRunLoopGetCurrent (),
                                                           kCFRunLoopDefaultMode);

            if (!ok)
                SCNetworkReachabilitySetCallback (self.ref,
                                                  NULL,
                                                  NULL);
            else
                self.flags = [self flagsSynchronously]; // prime the pump ...
        }

        self.scheduled = ok;
    }

    return self.isScheduled;
}

- (BOOL) unscheduleCallback
{
    if (self.isScheduled)
    {
        Boolean ok = SCNetworkReachabilityUnscheduleFromRunLoop (self.ref,
                                                                 CFRunLoopGetCurrent (),
                                                                 kCFRunLoopDefaultMode);

        if (ok)
        {
            SCNetworkReachabilitySetCallback (self.ref,
                                              NULL,
                                              NULL);

            self.scheduled = NO;
        }
    }

    return !self.isScheduled;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self unscheduleCallback];

    if (self->ref_ != NULL)
        CFRelease (self->ref_);

    [super dealloc];
}

@end
