//
//  MCNetworkReachability.h
//  MCToolbox
//
//  Created by J. G. Pusey on 6/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
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

@protocol MCNetworkReachabilityDelegate <NSObject>

- (void) reachabilityDidChange: (MCNetworkReachability *) reachability;

@end

