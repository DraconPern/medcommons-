//
//  DataManager.m
//  MCProvider
//
//  Created by Bill Donner on 4/11/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "DataManager.h"
#import "ImageCache.h"
#import "SettingsManager.h"

#pragma mark -
#pragma mark Public Class DataManager
#pragma mark -

@implementation DataManager

@synthesize currentScene                 = currentScene_;
@synthesize customViews                  = customViews_;
@synthesize hostReachabilityImage        = hostReachabilityImage_;
@synthesize hostReachabilityStatus       = hostReachabilityStatus_;
@synthesize imageCache                   = imageCache_;
@synthesize internetReachabilityImage    = internetReachabilityImage_;
@synthesize internetReachabilityStatus   = internetReachabilityStatus_;
@synthesize localWiFiReachabilityImage   = localWiFiReachabilityImage_;
@synthesize localWiFiReachabilityStatus  = localWiFiReachabilityStatus_;
@synthesize masterPlist                  = masterPlist_;
@synthesize nextMediaPathIndex           = nextMediaPathIndex_;
@synthesize userHasConsented                    = userHasConsented_;
@synthesize smBottomMaster               = smBottomMaster_;
@synthesize smLeftDetail                 = smLeftDetail_;
@synthesize smLeftDetailReversed         = smLeftDetailReversed_;
@synthesize smLeftFull                   = smLeftFull_;
@synthesize smLeftMaster                 = smLeftMaster_;
@synthesize smRightDetail                = smRightDetail_;
@synthesize smRightFull                  = smRightFull_;
@synthesize smRightMaster                = smRightMaster_;
@synthesize summaryReachabilityImage     = summaryReachabilityImage_;
@synthesize summaryReachabilityStatus    = summaryReachabilityStatus_;
@synthesize topLevelHomeTitle            = topLevelHomeTitle_;
@synthesize topLevelHomeURL              = topLevelHomeURL_;

#pragma mark Public Class Methods

+ (DataManager *) sharedInstance
{
    static DataManager *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[DataManager alloc] init];

    return SharedInstance;
}

#pragma mark Public Instance Methods

- (NSArray *) allScenes
{
    NSDictionary *environment = self.masterPlist;
    NSArray      *scenes = [environment objectForKey: @"scenes"];

    if (!scenes)
        [self dieFromMisconfiguration: @"No scenes in plist file"];

    return scenes;
}

- (NSDictionary *) currentSceneContext
{
    NSArray *scenes = [self allScenes];

    if (self.currentScene >= [scenes count])
        [self dieFromMisconfiguration: [NSString stringWithFormat:
                                        @"Current scene %d is out of range",
                                        self.currentScene]];

    return [scenes objectAtIndex: self.currentScene];
}

- (NSDictionary *) currentBlock: (NSUInteger) blockNum
                       forScene: (NSDictionary *) scene
{
    NSArray *blocks = [scene objectForKey: @"blocks"];

    if (!blocks)
        [self dieFromMisconfiguration: [NSString stringWithFormat:
                                        @"No blocks in scene %@",
                                        [scene objectForKey: @"name"]]];

    if (blockNum >= [blocks count])
        blockNum = 0; // hack but wrong

    return [blocks objectAtIndex: blockNum];
}

- (void) dieFromMisconfiguration: (NSString *) msg
{
    [self.appDelegate dieFromMisconfiguration: msg];
}

- (void) invokeActionBlockMethod: (NSDictionary *) block
{
    SEL           selector = NSSelectorFromString ([block objectForKey: @"method"]);
    Class         klass = NSClassFromString ([block objectForKey: @"class"]);
    NSDictionary *arg = [block objectForKey: @"args"];

    [self performSelector: selector
                  inClass: klass
               withObject: arg];
}

- (void) invokeSavedActionBlockMethod: (NSString *) selectorName
                              inClass: (NSString *) className
                           withObject: (id) arg
{
    SEL   selector = NSSelectorFromString (selectorName);
    Class klass = NSClassFromString (className);

    [self performSelector: selector
                  inClass: klass
               withObject: arg];
}

- (void) performSelector: (SEL) selector
                 inClass: (Class) klass
              withObject: (NSDictionary *) arg
{
    if (selector &&
        klass &&
        [klass instancesRespondToSelector: selector])
    {
        NSObject *action = [[klass alloc] init];

        if (action)
        {
            [action performSelector: selector
                         withObject: arg
                         afterDelay: 0.0f];

            [action release];
        }
    }
}

- (void) setCurrentScene: (NSUInteger) scene
{
    self->currentScene_ = scene;

    self.appDelegate.settingsManager.lastScene = scene;
}

@end
