//
//  MCNetworkReachability.h
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

#import <Foundation/Foundation.h>
#import <SystemConfiguration/SystemConfiguration.h>

typedef enum
{
    MCNetworkStatusNotReachable     = 0,
    MCNetworkStatusReachableViaWiFi,
    MCNetworkStatusReachableViaWWAN
} MCNetworkStatus;

@protocol MCNetworkReachabilityDelegate;

/**
 * The MCNetworkReachability class ...
 */
@interface MCNetworkReachability : NSObject
{
@private

    id <MCNetworkReachabilityDelegate> delegate_;
    SCNetworkReachabilityFlags         flags_;
    SCNetworkReachabilityRef           ref_;
    //
    // Flags:
    //
    BOOL                               localWiFi_;
    BOOL                               scheduled_;
}

@property (nonatomic, assign, readonly)  BOOL                                connectionOnDemand;
@property (nonatomic, assign, readonly)  BOOL                                connectionOnTraffic;
@property (nonatomic, assign, readonly)  BOOL                                connectionRequired;
@property (nonatomic, assign, readwrite) id <MCNetworkReachabilityDelegate>  delegate;
@property (nonatomic, retain, readonly)  NSString                           *formattedFlags;
@property (nonatomic, retain, readonly)  NSString                           *formattedStatus;
@property (nonatomic, assign, readonly)  BOOL                                interventionRequired;
@property (nonatomic, assign, readonly)  BOOL                                isDirect;
@property (nonatomic, assign, readonly)  BOOL                                isLocalAddress;
@property (nonatomic, assign, readonly)  BOOL                                isWWAN;
@property (nonatomic, assign, readonly)  BOOL                                reachable;
@property (nonatomic, assign, readonly)  MCNetworkStatus                     status;
@property (nonatomic, assign, readonly)  BOOL                                transientConnection;

+ (MCNetworkReachability *) reachabilityForInternetConnection;

+ (MCNetworkReachability *) reachabilityForLocalWiFi;

+ (MCNetworkReachability *) reachabilityWithAddress: (const struct sockaddr *) address;

+ (MCNetworkReachability *) reachabilityWithLocalAddress: (const struct sockaddr *) localAddress
                                           remoteAddress: (const struct sockaddr *) remoteAddress;

+ (MCNetworkReachability *) reachabilityWithName: (NSString *) name;

@end

/**
 * The MCNetworkReachabilityDelegate protocol ...
 */
@protocol MCNetworkReachabilityDelegate <NSObject>

- (void) reachabilityDidChange: (MCNetworkReachability *) reachability;

@end

