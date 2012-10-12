//
//  SettingsManager.m
//  MCProvider
//
//  Created by J. G. Pusey on 6/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
#import "SettingsManager.h"

#pragma mark -
#pragma mark Public Class SettingsManager
#pragma mark -

#pragma mark Internal Constants

//
// Keys for read/write NSUserDefaults settings:
//
#define APPLIANCE_KEY                   @"MCAppliance"
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
#define WIFI_WEBSERVER_KEY				@"WifiWebserver"
#define BONJOUR_WITHPEERS_KEY				@"BonjourWithPeers"
#define DEBUG_TRACE_KEY					@"DebugTrace"
#define DISABLE_MEDIA_PLAYER_KEY        @"DisableMediaPlayer"
#define BUILTIN_SAMPLES_KEY             @"BuiltinSamples"

//

@interface SettingsManager ()

@property (nonatomic, assign, readonly) NSDictionary   *bundleInfo;
@property (nonatomic, retain, readonly) NSDictionary   *environment;
@property (nonatomic, assign, readonly) NSUserDefaults *userDefaults;

@end

@implementation SettingsManager

@synthesize bundleInfo               = bundleInfo_;

@synthesize userDefaults             = userDefaults_;

@dynamic plistForSamples;
@dynamic debugTrace;
@dynamic wifiWebserver;
@dynamic bonjourWithPeers;
@dynamic disableMediaPlayer;

#pragma mark Public Class Methods

+ (SettingsManager *) sharedInstance
{
    static SettingsManager *SharedInstance;

    if (!SharedInstance)
        SharedInstance = [[SettingsManager alloc] init];

    return SharedInstance;
}

#pragma mark Public Instance Methods

- (NSDictionary *) readOnlySettingsDictionary
{
    return [NSDictionary dictionaryWithDictionary: self.environment];
}

- (NSDictionary *) readWriteSettingsDictionary
{
    return [self.userDefaults dictionaryRepresentation];
}



- (void) synchronizeReadWriteSettings
{
	[self.userDefaults synchronize];
}
- (NSString *) appliance
{
    return [self.userDefaults stringForKey: APPLIANCE_KEY];
}



- (BOOL) debugTrace
{
    return [self.environment boolForKey: DEBUG_TRACE_KEY];
}
- (BOOL) wifiWebserver
{
    return [self.environment boolForKey: WIFI_WEBSERVER_KEY];
}
- (BOOL) bonjourWithPeers
{
    return [self.environment boolForKey: BONJOUR_WITHPEERS_KEY];
}


- (BOOL) disableMediaPlayer
{
    return [self.environment boolForKey: DISABLE_MEDIA_PLAYER_KEY];
}
- (void) setAppliance: (NSString *) appliance
{
    if (!appliance)
        [self.userDefaults removeObjectForKey: APPLIANCE_KEY];
    else
        [self.userDefaults setObject: appliance
                              forKey: APPLIANCE_KEY];
}

- (NSString *) plistForSamples
{
    NSString *s = [self.environment stringForKey: BUILTIN_SAMPLES_KEY];
	//NSLog (@"plistForSamples is %@",s);
	return s;
}

#pragma mark Overridden NSObject MEthods

- (void) dealloc
{
    [self->environment_ release];

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
			
		
          //  self.plistForSamples = YES;

            [self synchronizeReadWriteSettings];
        }
    }

    return self;
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

@end
