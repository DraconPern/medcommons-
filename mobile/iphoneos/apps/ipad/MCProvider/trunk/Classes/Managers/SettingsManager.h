//
//  SettingsManager.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SettingsManager : NSObject
{
@private

    NSDictionary   *bundleInfo_;
    NSDictionary   *environment_;
    NSArray        *knownAppliances_;
    NSArray        *knownThemes_;
    NSUserDefaults *userDefaults_;
}

@property (nonatomic, copy,   readwrite) NSString       *appliance;
@property (nonatomic, copy,   readonly)  NSString       *applicationName;
@property (nonatomic, copy,   readonly)  NSString       *applicationVersion;
@property (nonatomic, assign, readonly)  BOOL            canAddMembers;
@property (nonatomic, assign, readonly)  BOOL            canMergeMembers;
@property (nonatomic, assign, readonly)  BOOL            canUseNewFeatures;
@property (nonatomic, assign, readonly)  BOOL            canUseNewViewer;
@property (nonatomic, assign, readonly)  BOOL            canUseSettings;
@property (nonatomic, assign, readwrite) BOOL            connectAnonymously;
@property (nonatomic, copy,   readonly)  NSString       *defaultAppliance;
@property (nonatomic, copy,   readonly)  NSString       *defaultHealthURLFormat;
@property (nonatomic, copy,   readonly)  NSString       *defaultPassword;
@property (nonatomic, copy,   readonly)  NSString       *defaultTheme;
@property (nonatomic, copy,   readonly)  NSString       *defaultUserID;
@property (nonatomic, assign, readonly)  NSInteger       featureLevel;
@property (nonatomic, copy,   readwrite) NSString       *healthURLFormat;
@property (nonatomic, copy,   readonly)  NSString       *infoPageImageName;
@property (nonatomic, retain, readonly)  NSArray        *knownAppliances;
@property (nonatomic, retain, readonly)  NSArray        *knownThemes;
@property (nonatomic, copy,   readwrite) NSString       *lastDetailItem;
@property (nonatomic, copy,   readwrite) NSString       *lastGroupID;
@property (nonatomic, assign, readwrite) NSUInteger      lastScene;
@property (nonatomic, retain, readwrite) NSURL          *lastURL;
@property (nonatomic, assign, readwrite) NSTimeInterval  loginExpiration;
@property (nonatomic, retain, readonly)  NSDictionary   *memberListDisplayOptions;
@property (nonatomic, copy,   readwrite) NSString       *password;
@property (nonatomic, retain, readwrite) NSArray        *recentSearches;
@property (nonatomic, assign, readwrite) BOOL            rememberLogin;
@property (nonatomic, copy,   readwrite) NSString       *savedUserID;
@property (nonatomic, assign, readwrite) BOOL            simulatePhoneUI;
@property (nonatomic, retain, readonly)  NSURL          *splashURL;
@property (nonatomic, copy,   readwrite) NSString       *theme;
@property (nonatomic, copy,   readonly)  NSString       *topLevelGroupTitle;
@property (nonatomic, copy,   readonly)  NSString       *topLevelHomeTitle;
@property (nonatomic, retain, readonly)  NSURL          *topLevelHomeURL;
@property (nonatomic, assign, readwrite) BOOL            useCamera;
@property (nonatomic, assign, readwrite) BOOL            useNotes;
@property (nonatomic, copy,   readwrite) NSString       *userID;
@property (nonatomic, assign, readwrite) BOOL            useSamples;

+ (SettingsManager *) sharedInstance;

- (NSDictionary *) readOnlySettingsDictionary;

- (NSDictionary *) readWriteSettingsDictionary;

- (void) synchronizeReadWriteSettings;

@end
