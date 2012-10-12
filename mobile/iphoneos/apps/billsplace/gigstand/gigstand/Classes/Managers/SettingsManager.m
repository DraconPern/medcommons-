//
//  SettingsManager.m
//
//  Original Template Created by J. G. Pusey on 6/18/10.
//
#import "SettingsManager.h"
#import "DictionaryAdditions.h"


#pragma mark -
#pragma mark Public Class SettingsManager
#pragma mark -

#pragma mark Internal Constants

//
// Keys for read/write NSUserDefaults settings:
//
#define COLLABORATION_FEATURES_KEY      @"GSCollabFeatures"
#define PERFORMANCE_MODE_KEY		@"GSPerformanceMode"
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
#define BONJOUR_WITHPEERS_KEY			@"BonjourWithPeers"
#define DEBUG_TRACE_KEY					@"DebugTrace"
#define DISABLE_MEDIA_PLAYER_KEY        @"DisableMediaPlayer"
#define SMALL_SAMPLES_KEY               @"BuiltinSamples"
#define FULL_SAMPLES_KEY                @"FullSamples"
#define VIDEO_HELPERS_KEY                @"VideoHelpers"
#define ZIP_ARCHIVES_KEY				@"ZipArchives"
#define GALLERY_TRIGGER_KEY             @"GalleryTrigger"
#define RECENTS_TO_KEEP_KEY             @"RecentsToKeep"
#define SNAPSHOT_GALLERY_COUNT_KEY      @"SnapshotGalleryCount"
//

@interface SettingsManager ()

@property (nonatomic, assign, readonly) NSDictionary   *bundleInfo;
@property (nonatomic, retain, readonly) NSDictionary   *environment;
@property (nonatomic, assign, readonly) NSUserDefaults *userDefaults;

@end

@implementation SettingsManager

@synthesize bundleInfo               = bundleInfo_;

@synthesize userDefaults             = userDefaults_;

@dynamic plistForSmallSampleSet;

@dynamic plistForFullSampleSet;

@dynamic plistForVideoHelperSet;

@dynamic debugTrace;
@dynamic wifiWebserver;
@dynamic zipArchives;
@dynamic bonjourWithPeers;
@dynamic disableMediaPlayer;
@dynamic collabFeatures;
@dynamic normalMode;

@dynamic   galleryTrigger;
@dynamic recentsToKeep;
@dynamic  snapshotGalleryCount;

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

- (BOOL) collabFeatures
{
    return [self.userDefaults boolForKey: COLLABORATION_FEATURES_KEY];
}

- (BOOL) normalMode
{
    return ![self.userDefaults boolForKey: PERFORMANCE_MODE_KEY];
}

- (BOOL) debugTrace
{
    return [self.environment boolForKey: DEBUG_TRACE_KEY];
}
- (BOOL) wifiWebserver
{
    return [self.environment boolForKey: WIFI_WEBSERVER_KEY];
}
- (BOOL) zipArchives
{
    return [self.environment boolForKey: ZIP_ARCHIVES_KEY];
}
- (BOOL) bonjourWithPeers
{
    return [self.environment boolForKey: BONJOUR_WITHPEERS_KEY];
}

- (NSUInteger) galleryTrigger
{
    return [self.environment unsignedIntegerForKey: GALLERY_TRIGGER_KEY];
}
- (NSUInteger) recentsToKeep
{
    return [self.environment unsignedIntegerForKey: RECENTS_TO_KEEP_KEY];
}
- (NSUInteger) snapshotGalleryCount
{
    return [self.environment unsignedIntegerForKey: SNAPSHOT_GALLERY_COUNT_KEY];
}


- (BOOL) disableMediaPlayer
{
    return [self.environment boolForKey: DISABLE_MEDIA_PLAYER_KEY];
}
- (void) setCollabFeatures:( BOOL) collabFeatures
{
    if (!collabFeatures)
        [self.userDefaults removeObjectForKey: COLLABORATION_FEATURES_KEY];
    else
        [self.userDefaults setBool:collabFeatures
                              forKey: COLLABORATION_FEATURES_KEY];
}
- (void) setNormalMode:( BOOL) normalMode
{
    if (normalMode)
    {   
        NSLog (@"removing PERFORMANCE_MODE_KEY");
        [self.userDefaults removeObjectForKey: PERFORMANCE_MODE_KEY];
    }
    else
    { 
        NSLog (@"adding PERFORMANCE_MODE_KEY");
        [self.userDefaults setBool:YES forKey: PERFORMANCE_MODE_KEY];
    }
}
- (NSString *) plistForSmallSampleSet
{
    NSString *s = [self.environment stringForKey: SMALL_SAMPLES_KEY];
	return s;
}
- (NSString *) plistForFullSampleSet
{
    NSString *s = [self.environment stringForKey: FULL_SAMPLES_KEY];
	return s;
}
- (NSString *) plistForVideoHelperSet
{
    NSString *s = [self.environment stringForKey: VIDEO_HELPERS_KEY];
	return s;
}
#pragma mark Overridden NSObject MEthods

- (void) dealloc
{
    [self->environment_ release];
    [self->bundleInfo_ release];
    [self->userDefaults_ release];

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
        if (!self.collabFeatures)
        {
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
