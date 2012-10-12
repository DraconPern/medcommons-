//
//  SettingsManager.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "DictionaryAdditions.h"
#import "SettingsManager.h"

#pragma mark -
#pragma mark Public Class SettingsManager
#pragma mark -

#pragma mark Internal Constants

//
// Keys for read/write NSUserDefaults settings:
//
//
//#define FAVORITE_DICT_KEY                   @"FAVORITE_DICT_KEY"
//#define FAVORITE_SECT_KEY                   @"FAVORITE_SECT_KEY"
//
//#define RECENT_DICT_KEY                   @"RECENT_DICT_KEY"
//#define RECENT_SECT_KEY                   @"RECENT_SECT_KEY"
//
//
//#define SETLIST1_DICT_KEY                   @"SETLIST1_DICT_KEY"
//#define SETLIST1_SECT_KEY                   @"SETLIST1_SECT_KEY"
//
//
//#define SETLIST2_DICT_KEY                   @"SETLIST2_DICT_KEY"
//#define SETLIST2_SECT_KEY                   @"SETLIST2_SECT_KEY"
//
//#define SETLIST3_DICT_KEY                   @"SETLIST3_DICT_KEY"
//#define SETLIST3_SECT_KEY                   @"SETLIST3_SECT_KEY"
//
//#define SETLIST4_DICT_KEY                   @"SETLIST4_DICT_KEY"
//#define SETLIST4_SECT_KEY                   @"SETLIST4_SECT_KEY"


#define APPLIANCE_KEY                   @"MCAppliance"
#define CONNECT_ANONYMOUSLY_KEY         @"MCConnectAnonymously"
#define HEALTH_URL_FORMAT_KEY           @"MCHealthURLFormat"
#define LAST_DETAIL_ITEM_KEY            @"MCLastDetailItem"
#define LAST_SCENE_KEY                  @"MCLastScene"
#define LAST_URL_KEY                    @"MCLastURL"
#define LOGIN_EXPIRATION_KEY            @"MCLoginExpiration"
#define PASSWORD_KEY                    @"MCPassword"
#define RECENT_SEARCHES_KEY             @"MCRecentSearches"
#define REMEMBER_LOGIN_KEY              @"MCRememberLogin"
#define SAVED_USER_ID_KEY               @"MCSavedUserID"
#define SIMULATE_PHONE_UI_KEY           @"MCSimulatePhoneUI"
#define USE_CAMERA_KEY                  @"MCUseCamera"
#define USE_NOTES_KEY                   @"MCUseNotes"
#define USE_SAMPLES_KEY                 @"MCUseSamples"
#define USER_ID_KEY                     @"MCUserID"
//
// Keys for read-only Info.plist settings:
//
#define BUNDLE_DISPLAY_NAME_KEY         @"CFBundleDisplayName"
#define BUNDLE_NAME_KEY                 @"CFBundleName"
#define BUNDLE_VERSION_KEY              @"CFBundleVersion"
#define ENVIRONMENT_VARIABLES_KEY       @"LSEnvironment"
//
// Keys for read-only Info.plist (LSEnvironment) settings:
//
#define CAN_ADD_MEMBERS_KEY             @"CanAddMembers"
#define CAN_MERGE_MEMBERS_KEY           @"CanMergeMembers"
#define CAN_USE_NEW_VIEWER_KEY          @"CanUseNewViewer"
#define CAN_USE_NEW_FEATURES_KEY        @"CanUseNewFeatures"
#define CAN_USE_SETTINGS_KEY            @"CanUseSettings"
#define DEFAULT_APPLIANCE_KEY           @"DefaultAppliance"
#define DEFAULT_HEALTH_URL_FORMAT_KEY   @"DefaultHealthURLFormat"
#define DEFAULT_PASSWORD_KEY            @"DefaultPassword"
#define DEFAULT_USER_ID_KEY             @"DefaultUserID"
#define FEATURE_LEVEL_IPAD_KEY          @"FeatureLevel~ipad"
#define FEATURE_LEVEL_KEY               @"FeatureLevel"
#define INFO_PAGE_IMAGE_NAME_KEY        @"InfoPageImageName"
#define MEMBER_LIST_DISPLAY_OPTIONS_KEY @"MemberListDisplayOptions"
#define SPLASH_URL_IPAD_KEY             @"SplashURL~ipad"
#define SPLASH_URL_KEY                  @"SplashURL"
#define TOP_LEVEL_GROUP_TITLE_KEY       @"TopLevelGroupTitle"
#define TOP_LEVEL_HOME_TITLE_KEY        @"TopLevelHomeTitle"
#define TOP_LEVEL_HOME_URL_KEY          @"TopLevelHomeURL"
//
// Subkeys for MEMBER_LIST_DISPLAY_OPTIONS_KEY:
//
#define HIDDEN_ITEMS_LABEL_SUBKEY       @"HiddenItemsLabel"
#define VISIBLE_ITEMS_LABEL_SUBKEY      @"VisibleItemsLabel"

@interface SettingsManager ()

@property (nonatomic, assign, readonly) NSDictionary   *bundleInfo;
@property (nonatomic, retain, readonly) NSDictionary   *environment;
@property (nonatomic, assign, readonly) NSUserDefaults *userDefaults;

@end

@implementation SettingsManager//
//@dynamic favorite_dict;
//@dynamic favorite_sect;
//@dynamic recent_dict;
//@dynamic recent_sect;
//
//@dynamic setlist1_dict;
//@dynamic setlist1_sect;
//
//
//@dynamic setlist2_dict;
//@dynamic setlist2_sect;
//
//
//@dynamic setlist3_dict;
//@dynamic setlist3_sect;
//
//
//@dynamic setlist4_dict;
//@dynamic setlist4_sect;


@dynamic    appliance;
@dynamic    applicationName;
@dynamic    applicationVersion;
@synthesize bundleInfo               = bundleInfo_;
@dynamic    canAddMembers;
@dynamic    canMergeMembers;
@dynamic    canUseNewViewer;
@dynamic    canUseSettings;
@dynamic    canUseNewFeatures;
@dynamic    connectAnonymously;
@dynamic    defaultAppliance;
@dynamic    defaultHealthURLFormat;
@dynamic    defaultPassword;
@dynamic    defaultUserID;
@dynamic    environment;
@dynamic    featureLevel;
@dynamic    healthURLFormat;
@dynamic    infoPageImageName;
@dynamic    knownAppliances;
@dynamic    lastDetailItem;
@dynamic    lastScene;
@dynamic    lastURL;
@dynamic    loginExpiration;
@dynamic    memberListDisplayOptions;
@dynamic    password;
@dynamic    recentSearches;
@dynamic    rememberLogin;
@dynamic    savedUserID;
@dynamic    simulatePhoneUI;
@dynamic    splashURL;
@dynamic    topLevelGroupTitle;
@dynamic    topLevelHomeTitle;
@dynamic    topLevelHomeURL;
@dynamic    useCamera;
@dynamic    useNotes;
@synthesize userDefaults             = userDefaults_;
@dynamic    userID;
@dynamic    useSamples;

#pragma mark Public Class Methods

+ (SettingsManager *) sharedInstance
{
    static SettingsManager *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[SettingsManager alloc] init];

    return SharedInstance;
}

#pragma mark Public Instance Methods
//- (NSMutableDictionary *) favorite_dict
//{
//    return [self.userDefaults objectForKey: FAVORITE_DICT_KEY];
//}
//- (NSMutableArray *) favorite_sect
//{
//    return [self.userDefaults objectForKey: FAVORITE_SECT_KEY];
//}
//
//- (void) setFavorite_dict: (NSMutableDictionary *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: FAVORITE_DICT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: FAVORITE_DICT_KEY];
//}
//- (void) setFavorite_sect: (NSMutableArray *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: FAVORITE_SECT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: FAVORITE_SECT_KEY];
//}
//
//- (NSDictionary *) recent_dict 
//{
//    return [self.userDefaults objectForKey: RECENT_DICT_KEY];
//}
//- (NSArray *) recent_sect 
//{
//    return [self.userDefaults objectForKey: RECENT_SECT_KEY];
//}
//
//- (void) setRecent_dict: (NSDictionary *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: RECENT_DICT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: RECENT_DICT_KEY];
//}
//- (void) setRecent_sect: (NSArray *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: RECENT_SECT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: RECENT_SECT_KEY];
//}
////
//- (NSMutableDictionary *) setlist1_dict  
//{
//    return [self.userDefaults objectForKey: SETLIST1_DICT_KEY];
//}
//- (NSMutableArray *) setlist1_sect 
//{
//    return [self.userDefaults objectForKey: SETLIST1_SECT_KEY];
//}
//
//- (void) setSetlist1_dict: (NSMutableDictionary *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST1_DICT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST1_DICT_KEY];
//}
//- (void) setSetlist1_sect: (NSMutableArray *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST1_SECT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST1_SECT_KEY];
//}
////
////
//- (NSMutableDictionary *) setlist2_dict  
//{
//    return [self.userDefaults objectForKey: SETLIST2_DICT_KEY];
//}
//- (NSMutableArray *) setlist2_sect 
//{
//    return [self.userDefaults objectForKey: SETLIST2_SECT_KEY];
//}
//
//- (void) setSetlist2_dict: (NSMutableDictionary *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST2_DICT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST2_DICT_KEY];
//}
//- (void) setSetlist2_sect: (NSMutableArray *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST2_SECT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST2_SECT_KEY];
//}
////
////
//- (NSMutableDictionary *) setlist3_dict  
//{
//    return [self.userDefaults objectForKey: SETLIST3_DICT_KEY];
//}
//- (NSMutableArray *) setlist3_sect 
//{
//    return [self.userDefaults objectForKey: SETLIST3_SECT_KEY];
//}
//
//- (void) setSetlist3_dict: (NSMutableDictionary *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST3_DICT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST3_DICT_KEY];
//}
//- (void) setSetlist3_sect: (NSMutableArray *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST3_SECT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST3_SECT_KEY];
//}
////
////
//- (NSMutableDictionary *) setlist4_dict  
//{
//    return [self.userDefaults objectForKey: SETLIST4_DICT_KEY];
//}
//- (NSMutableArray *) setlist4_sect 
//{
//    return [self.userDefaults objectForKey: SETLIST4_SECT_KEY];
//}
//
//- (void) setSetlist4_dict: (NSMutableDictionary *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST4_DICT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST4_DICT_KEY];
//}
//- (void) setSetlist4_sect: (NSMutableArray *) dict 
//{
//    if (!dict)
//        [self.userDefaults removeObjectForKey: SETLIST4_SECT_KEY];
//    else
//        [self.userDefaults setObject: dict
//                              forKey: SETLIST4_SECT_KEY];
//}
////
//




- (NSString *) appliance
{
    return [self.userDefaults stringForKey: APPLIANCE_KEY];
}

- (NSString *) applicationName
{
    NSString *appName = [self.bundleInfo stringForKey: BUNDLE_DISPLAY_NAME_KEY];

    if (!appName)
        appName = [self.bundleInfo stringForKey: BUNDLE_NAME_KEY];

    return appName;
}

- (NSString *) applicationVersion
{
    return [self.bundleInfo stringForKey: BUNDLE_VERSION_KEY];
}

- (BOOL) canAddMembers
{
    return [self.environment boolForKey: CAN_ADD_MEMBERS_KEY];
}

- (BOOL) canMergeMembers
{
    return [self.environment boolForKey: CAN_MERGE_MEMBERS_KEY];
}

- (BOOL) canUseNewViewer
{
    return [self.environment boolForKey: CAN_USE_NEW_VIEWER_KEY];
}
- (BOOL) canUseNewFeatures
{
    return [self.environment boolForKey: CAN_USE_NEW_FEATURES_KEY];
}
- (BOOL) canUseSettings
{
    return [self.environment boolForKey: CAN_USE_SETTINGS_KEY];
}

- (BOOL) connectAnonymously
{
    return [self.userDefaults boolForKey: CONNECT_ANONYMOUSLY_KEY];
}

- (NSString *) defaultAppliance
{
    return [self.environment stringForKey: DEFAULT_APPLIANCE_KEY];
}

- (NSString *) defaultHealthURLFormat
{
    return [self.environment stringForKey: DEFAULT_HEALTH_URL_FORMAT_KEY];
}

- (NSString *) defaultPassword
{
    return [self.environment stringForKey: DEFAULT_PASSWORD_KEY];
}

- (NSString *) defaultUserID
{
    return [self.environment stringForKey: DEFAULT_USER_ID_KEY];
}

- (NSDictionary *) environment
{
    if (!self->environment_)
    {
        self->environment_ = [[self.bundleInfo dictionaryForKey: ENVIRONMENT_VARIABLES_KEY]
                              retain];

        if (!self->environment_)
            self->environment_ = [[NSDictionary alloc] init];
    }

    return self->environment_;
}

- (NSInteger) featureLevel
{
    return ((UI_USER_INTERFACE_IDIOM () == UIUserInterfaceIdiomPad) ?
            [self.environment integerForKey: FEATURE_LEVEL_IPAD_KEY] :
            [self.environment integerForKey: FEATURE_LEVEL_KEY]);
}

- (NSString *) healthURLFormat
{
    NSString *value = [self.userDefaults stringForKey: HEALTH_URL_FORMAT_KEY];

    if (!value)
        value = [self.environment stringForKey: DEFAULT_HEALTH_URL_FORMAT_KEY];

    return value;
}

- (NSString *) infoPageImageName
{
    return [self.environment stringForKey: INFO_PAGE_IMAGE_NAME_KEY];
}

- (NSArray *) knownAppliances
{
    //
    // Hard-coded for now -- eventually these may come out of environment or
    // user defaults or somewhere else ...
    //
    if (!self->knownAppliances_)
        self->knownAppliances_ = [[NSArray alloc] initWithObjects:
                                  @"ci.myhealthespace.com",
                                  @"healthurl.medcommons.net",
                                  @"portal.medcommons.net",
                                  nil];

    return self->knownAppliances_;
}

- (NSString *) lastDetailItem
{
    return [self.userDefaults stringForKey: LAST_DETAIL_ITEM_KEY];
}

- (NSUInteger) lastScene
{
    return (NSUInteger) [self.userDefaults integerForKey: LAST_SCENE_KEY];
}

- (NSURL *) lastURL
{
    NSString *URLString = [self.userDefaults stringForKey: LAST_URL_KEY];

    if (URLString)
        URLString = [URLString stringByTrimmingWhitespace];

    return ((URLString && ([URLString length] > 0)) ?
            [NSURL URLWithString: URLString] :
            nil);
}

- (NSTimeInterval) loginExpiration
{
    return [self.userDefaults doubleForKey: LOGIN_EXPIRATION_KEY];
}

- (NSDictionary *) memberListDisplayOptions
{
    return [self.environment dictionaryForKey: MEMBER_LIST_DISPLAY_OPTIONS_KEY];
}

- (NSString *) password
{
    return [self.userDefaults stringForKey: PASSWORD_KEY];
}

- (NSDictionary *) readOnlySettingsDictionary
{
    return [NSDictionary dictionaryWithDictionary: self.environment];
}

- (NSDictionary *) readWriteSettingsDictionary
{
    return [self.userDefaults dictionaryRepresentation];
}

- (NSArray *) recentSearches
{
    return [self.userDefaults stringArrayForKey: RECENT_SEARCHES_KEY];
}

- (BOOL) rememberLogin
{
    return [self.userDefaults boolForKey: REMEMBER_LOGIN_KEY];
}

- (NSString *) savedUserID
{
    return [self.userDefaults stringForKey: SAVED_USER_ID_KEY];
}

- (void) setAppliance: (NSString *) appliance
{
    if (!appliance)
        [self.userDefaults removeObjectForKey: APPLIANCE_KEY];
    else
        [self.userDefaults setObject: appliance
                              forKey: APPLIANCE_KEY];
}

- (void) setConnectAnonymously: (BOOL) connectAnonymously
{
    [self.userDefaults setBool: connectAnonymously
                        forKey: CONNECT_ANONYMOUSLY_KEY];
}

- (void) setHealthURLFormat: (NSString *) healthURLFormat
{
    if (!healthURLFormat)
        [self.userDefaults removeObjectForKey: HEALTH_URL_FORMAT_KEY];
    else
        [self.userDefaults setObject: healthURLFormat
                              forKey: HEALTH_URL_FORMAT_KEY];
}

- (void) setLastDetailItem: (NSString *) lastDetailItem
{
    if (!lastDetailItem)
        [self.userDefaults removeObjectForKey: LAST_DETAIL_ITEM_KEY];
    else
        [self.userDefaults setObject: lastDetailItem
                              forKey: LAST_DETAIL_ITEM_KEY];
}

- (void) setLastScene: (NSUInteger) lastScene
{
    [self.userDefaults setInteger: (NSInteger) lastScene
                           forKey: LAST_SCENE_KEY];
}

- (void) setLastURL: (NSURL *) lastURL
{
    if (!lastURL)
        [self.userDefaults removeObjectForKey: LAST_URL_KEY];
    else
        [self.userDefaults setObject: [lastURL absoluteString]
                              forKey: LAST_URL_KEY];
}

- (void) setLoginExpiration: (NSTimeInterval) loginExpiration
{
    [self.userDefaults setDouble: loginExpiration
                          forKey: LOGIN_EXPIRATION_KEY];
}

- (void) setPassword: (NSString *) password
{
    if (!password)
        [self.userDefaults removeObjectForKey: PASSWORD_KEY];
    else
        [self.userDefaults setObject: password
                              forKey: PASSWORD_KEY];
}

- (void) setRememberLogin: (BOOL) rememberLogin
{
    [self.userDefaults setBool: rememberLogin
                        forKey: REMEMBER_LOGIN_KEY];
}

- (void) setSavedUserID: (NSString *) savedUserID
{
    if (!savedUserID)
        [self.userDefaults removeObjectForKey: SAVED_USER_ID_KEY];
    else
        [self.userDefaults setObject: savedUserID
                              forKey: SAVED_USER_ID_KEY];
}

- (void) setRecentSearches: (NSArray *) recentSearches
{
    if (!recentSearches)
        [self.userDefaults removeObjectForKey: RECENT_SEARCHES_KEY];
    else
        [self.userDefaults setObject: recentSearches
                              forKey: RECENT_SEARCHES_KEY];
}

- (void) setSimulatePhoneUI: (BOOL) simulatePhoneUI
{
    [self.userDefaults setBool: simulatePhoneUI
                        forKey: SIMULATE_PHONE_UI_KEY];
}

- (void) setUseCamera: (BOOL) useCamera
{
    [self.userDefaults setBool: useCamera
                        forKey: USE_CAMERA_KEY];
}

- (void) setUseNotes: (BOOL) useNotes
{
    [self.userDefaults setBool: useNotes
                        forKey: USE_NOTES_KEY];
}

- (void) setUserID: (NSString *) userID
{
    if (!userID)
        [self.userDefaults removeObjectForKey: USER_ID_KEY];
    else
        [self.userDefaults setObject: userID
                              forKey: USER_ID_KEY];
}

- (void) setUseSamples: (BOOL) useSamples
{
    [self.userDefaults setBool: useSamples
                        forKey: USE_SAMPLES_KEY];
}

- (BOOL) simulatePhoneUI
{
    return [self.userDefaults boolForKey: SIMULATE_PHONE_UI_KEY];
}

- (NSURL *) splashURL
{
    NSURL *URL = ((UI_USER_INTERFACE_IDIOM () == UIUserInterfaceIdiomPad) ?
                  [self.environment URLForKey: SPLASH_URL_IPAD_KEY] :
                  nil);

    if (!URL)
        URL = [self.environment URLForKey: SPLASH_URL_KEY];

    return URL;
}

- (void) synchronizeReadWriteSettings
{
    [self.userDefaults synchronize];
}

- (NSString *) topLevelGroupTitle
{
    return [self.environment stringForKey: TOP_LEVEL_GROUP_TITLE_KEY];
}

- (NSString *) topLevelHomeTitle
{
    return [self.environment stringForKey: TOP_LEVEL_HOME_TITLE_KEY];
}

- (NSURL *) topLevelHomeURL
{
    return [self.environment URLForKey: TOP_LEVEL_HOME_URL_KEY];
}

- (BOOL) useCamera
{
    return [self.userDefaults boolForKey: USE_CAMERA_KEY];
}

- (BOOL) useNotes
{
    return [self.userDefaults boolForKey: USE_NOTES_KEY];
}

- (NSString *) userID
{
    return [self.userDefaults stringForKey: USER_ID_KEY];
}

- (BOOL) useSamples
{
    return [self.userDefaults boolForKey: USE_SAMPLES_KEY];
}

#pragma mark Overridden NSObject MEthods

- (void) dealloc
{
    [self->environment_ release];
    [self->knownAppliances_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
    {
        self->bundleInfo_ = [[NSBundle mainBundle] infoDictionary];
        self->userDefaults_ = [NSUserDefaults standardUserDefaults];

        //
        // Initialize some read/write settings if necessary:
        //
        if (!self.appliance)
        {
            self.appliance = self.defaultAppliance;
            self.password = self.defaultPassword;
            self.userID = self.defaultUserID;
            self.connectAnonymously = NO;
            self.loginExpiration = 0.0f;
            self.rememberLogin = NO;
            self.useCamera = YES;
            self.useNotes = YES;
            self.useSamples = YES;

            [self synchronizeReadWriteSettings];
        }
    }

    return self;
}

@end
