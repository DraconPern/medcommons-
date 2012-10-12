//
//  DataManager.h
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

//#define ENABLE_APD_LOGGING   1    //- AppDelegate
#define ENABLE_CACHE_LOGGING   0
//#define ENABLE_CAM_LOGGING   1    //- camera
//#define ENABLE_LLC_LOGGING   1  //- low level comms trace with medcommons server

#import <UIKit/UIKit.h>

#define PLEASE_SHOOT_MEMBER_IMAGE @"Silhouette.png"
#define TRASH_CAN_IMAGE            @"totrash.png"

#define MAIN_LOGO                  @"logoGray_310x65"
#define MAIN_LOGO_IMAGE            @"logoGray_310x65.png"
#define MAIN_LOGO_TYPE             @"png"

#if defined (APP_STORE_FINAL)
#define CONSOLE_LOG(format, ...)
#else
#define CONSOLE_LOG(format, ...) CFShow ([NSString stringWithFormat: format, ## __VA_ARGS__]);
#endif

#if !ENABLE_APD_LOGGING
#define APD_LOG(format, ...)
#else
#define APD_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_CACHE_LOGGING
#define CACHE_LOG(format, ...)
#else
#define CACHE_LOG(format, ...)       CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_CAM_LOGGING
#define CAM_LOG(format, ...)
#else
#define CAM_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_LANDSCAPE_LOGGING
#define LANDSCAPE_LOG(format, ...)
#else
#define LANDSCAPE_LOG(format, ...)   CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_LLC_LOGGING
#define LLC_LOG(format, ...)
#else
#define LLC_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

#if !ENABLE_PAN_LOGGING
#define PAN_LOG(format, ...)
#else
#define PAN_LOG(format, ...)         CONSOLE_LOG (format, ## __VA_ARGS__)
#endif

//singleton datamanager
@class CustomViews;
@class Group;
@class ImageCache;
@class Member;
@class MemberStore;
@class SegmentMap;

@interface DataManager : NSObject
{
@private
	
    NSUInteger            currentScene_;
    CustomViews          *customViews_;
    NSString             *hostReachabilityImage_;
    NSString             *hostReachabilityStatus_;
    ImageCache           *imageCache_;
    NSString             *internetReachabilityImage_;
    NSString             *internetReachabilityStatus_;
    NSString             *localWiFiReachabilityImage_;
    NSString             *localWiFiReachabilityStatus_;
    NSDictionary         *masterPlist_;
    NSUInteger            nextMediaPathIndex_;
    BOOL                  userHasConsented_;
    SegmentMap           *smBottomMaster_;
    SegmentMap           *smLeftDetail_;
    SegmentMap           *smLeftDetailReversed_;
    SegmentMap           *smLeftFull_;
    SegmentMap           *smLeftMaster_;
    SegmentMap           *smRightDetail_;
    SegmentMap           *smRightFull_;
    SegmentMap           *smRightMaster_;
    NSString             *summaryReachabilityImage_;
    NSString             *summaryReachabilityStatus_;
    NSString             *topLevelHomeTitle_;
    NSURL                *topLevelHomeURL_;
}

@property (nonatomic, assign, readwrite) NSUInteger            currentScene;
@property (nonatomic, retain, readwrite) CustomViews          *customViews;
@property (nonatomic, retain, readwrite) NSString             *hostReachabilityImage;
@property (nonatomic, retain, readwrite) NSString             *hostReachabilityStatus;
@property (nonatomic, retain, readwrite) ImageCache           *imageCache;
@property (nonatomic, retain, readwrite) NSString             *internetReachabilityImage;
@property (nonatomic, retain, readwrite) NSString             *internetReachabilityStatus;
@property (nonatomic, retain, readwrite) NSString             *localWiFiReachabilityImage;
@property (nonatomic, retain, readwrite) NSString             *localWiFiReachabilityStatus;
@property (nonatomic, retain, readwrite) NSDictionary         *masterPlist;
@property (nonatomic, assign, readwrite) NSUInteger            nextMediaPathIndex;
@property (nonatomic, assign, readwrite) BOOL                  userHasConsented;
@property (nonatomic, retain, readwrite) SegmentMap           *smBottomMaster;
@property (nonatomic, retain, readwrite) SegmentMap           *smLeftDetail;
@property (nonatomic, retain, readwrite) SegmentMap           *smLeftDetailReversed;
@property (nonatomic, retain, readwrite) SegmentMap           *smLeftFull;
@property (nonatomic, retain, readwrite) SegmentMap           *smLeftMaster;
@property (nonatomic, retain, readwrite) SegmentMap           *smRightDetail;
@property (nonatomic, retain, readwrite) SegmentMap           *smRightFull;
@property (nonatomic, retain, readwrite) SegmentMap           *smRightMaster;
@property (nonatomic, retain, readwrite) NSString             *summaryReachabilityImage;
@property (nonatomic, retain, readwrite) NSString             *summaryReachabilityStatus;
@property (nonatomic, retain, readwrite) NSString             *topLevelHomeTitle;
@property (nonatomic, retain, readwrite) NSURL                *topLevelHomeURL;

+ (DataManager *) sharedInstance;

- (NSArray *) allScenes;

- (NSDictionary *) currentBlock: (NSUInteger) blockNum
                       forScene: (NSDictionary *) scene;

- (NSDictionary *) currentSceneContext;

- (void) dieFromMisconfiguration: (NSString *) msg;

- (void) invokeActionBlockMethod: (NSDictionary *) block;

- (void) invokeSavedActionBlockMethod: (NSString *) selectorName
                              inClass: (NSString *) className
                           withObject: (id) obj;

- (void) performSelector: (SEL) selector
                 inClass: (Class) klass
              withObject: (NSDictionary *) obj;

@end
