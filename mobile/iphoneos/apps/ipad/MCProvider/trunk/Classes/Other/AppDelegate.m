//
//  AppDelegate.m
//  MCProvider
//
//  Created by Bill Donner on 3/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DataManager.h"
#import "DetailViewController.h"
#import "DocumentsManager.h"
#import "GroupDetailController.h"
#import "GroupRootController.h"
#import "ImageCache.h"
#import "InfoViewController.h"
#import "MasterViewController.h"
#import "MCCustomView.h"
//#import "ScenarioManager.h"
#import "SegmentedControl.h"
#import "SegmentMap.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"
#import "WebViewController.h"

#pragma mark -
#pragma mark Public Class AppDelegate
#pragma mark -

#pragma mark Internal Constants

//
// Assorted view tags:
//
enum
{
    ERROR_ALERT_VIEW_TAG    = 666
};

@interface AppDelegate () <MCNetworkReachabilityDelegate, UIAlertViewDelegate>

@property (nonatomic, retain, readwrite) MCNetworkReachability *hostReachability;
@property (nonatomic, retain, readwrite) MCNetworkReachability *internetReachability;
@property (nonatomic, retain, readwrite) MCNetworkReachability *localWiFiReachability;
@property (nonatomic, retain, readwrite) NSString              *sessionRandomID;

- (NSDictionary *) handleOpenURL: (NSURL *) url;

- (NSDictionary *) processLaunchOptions: (NSDictionary *) options;

- (NSDictionary *) readMasterConfig: (NSString *) str;

- (void) setupReachability;

- (NSString *) statusToDictionary: (NSString *) dictTextKey
                        imageView: (NSString *) dictImageKey
                     reachability: (MCNetworkReachability *) reachability;

- (void) updateInterfaceWithReachability: (MCNetworkReachability *) reachability;

- (void) writeLinksToLog;

@end

@implementation AppDelegate

@dynamic    baseDetailViewController;
@dynamic    dataManager;
@dynamic    detailNavigationController;
@dynamic    documentsManager;
@synthesize groupListViewController    = groupListViewController_;
@synthesize groupRootController        = groupRootController_;
@synthesize hostReachability           = hostReachability_;
@synthesize infoViewController         = infoViewController_;
@synthesize internetReachability       = internetReachability_;
@synthesize localWiFiReachability      = localWiFiReachability_;
@dynamic    masterNavigationController;
@dynamic    navigationController;
//@dynamic    scenarioManager;
@dynamic    sessionManager;
@synthesize sessionRandomID            = sessionRandomID_;
@dynamic    settingsManager;
@dynamic    styleManager;
@synthesize targetIdiom                = targetIdiom_;

#pragma mark Public Class Methods

+ (AppDelegate *) sharedInstance
{
    return (AppDelegate *) self.application.delegate;
}

#pragma mark Public Instance Methods

- (DataManager *) dataManager
{
    return [DataManager sharedInstance];
}

- (void) dieFromMisconfiguration: (NSString *) msg
{
    //
    // Show fatal error message, usually because of misconfiguration:
    //
    UIAlertView *av = [[[UIAlertView alloc] initWithTitle: @"MedCommons"
                                                  message: msg
                                                 delegate: self
                                        cancelButtonTitle: @"OK"
                                        otherButtonTitles: nil]
                       autorelease];

    av.tag = ERROR_ALERT_VIEW_TAG;

    [av show];
}

- (void) dismissMasterPopoverAnimated: (BOOL) animated
{
    // do nothing ...
}

- (DocumentsManager *) documentsManager
{
    return [DocumentsManager sharedInstance];
}

//- (ScenarioManager *) scenarioManager
//{
//    return [ScenarioManager sharedInstance];
//}

- (SessionManager *) sessionManager
{
    return [SessionManager sharedInstance];
}

- (SettingsManager *) settingsManager
{
    return [SettingsManager sharedInstance];
}

- (void) setupControllers
{
    SettingsManager *settings = self.settingsManager;
    SessionManager  *sm = self.sessionManager;
    DataManager     *dm = self.dataManager;

    dm.customViews = [[MCCustomView alloc] init];
    dm.imageCache = [[[ImageCache alloc] init]
                     autorelease];

    NSLog (@"> %@ %@ %@ %@ %@",
           settings.lastURL,
           settings.applicationName,
           settings.applicationVersion,
           settings.userID,
           settings.appliance);

    [self setupReachability];

    if  (!settings.connectAnonymously && !sm.isLoginExpired)
        [sm logInSessionToAppliance: settings.appliance
                             userID: settings.userID
                           password: settings.password
                            options: SessionManagerOptionNone];

    dm.nextMediaPathIndex = 9990000;

    InfoViewController     *glvc = [[[InfoViewController alloc] init]
                                    autorelease];
    UINavigationController *nc = [[[UINavigationController alloc]
                                   initWithRootViewController: glvc]
                                  autorelease];

    [self setInitialRootViewController: nc];
}


- (StyleManager *) styleManager
{
    return [StyleManager sharedInstance];
}

#pragma mark Private Instance Methods

- (NSDictionary *) handleOpenURL: (NSURL *) url
{
    if (!url)   // if URL is nil, nothing more to do
        return nil;

    NSString *URLString = [url absoluteString];

    if (!URLString) // if URL's absoluteString is nil, nothing more to do
        return nil;

    NSArray  *queryComponents = [[url query] componentsSeparatedByString: @"&"];
    NSString *value = nil;
    NSString *key = nil;

    for (NSString *queryComponent in queryComponents)
    {
        NSArray *query = [queryComponent componentsSeparatedByString: @"="];

        if ([query count] == 2)
        {
            key = [query objectAtIndex: 0];
            value = [query objectAtIndex: 1];
        }
    }

    NSDictionary *ret = [NSDictionary dictionaryWithObjectsAndKeys: value, key, nil];  // ??? JGP

    NSLog (@"handle OpenURL returning %@", ret);

    return ret;
}

- (NSDictionary *) processLaunchOptions: (NSDictionary *) options
{
    if (options)
    {
        // check for remote launch
        //    UIApplicationLaunchOptionsURLKey = x-medpad-services://prototyper?plist=foo.bar;
        //    UIApplicationLaunchOptionsSourceApplicationKey = "com.apple.webapp-F8C7BED646CF40ADBCB297C21380A32D";

        NSLog (@"LaunchOptions URL %@",
               [[options objectForKey: UIApplicationLaunchOptionsURLKey] absoluteString]);

        NSLog (@"LaunchOptions SourceApplicationKey %@",
               [options objectForKey: UIApplicationLaunchOptionsSourceApplicationKey]);

        return [self handleOpenURL: [options objectForKey: UIApplicationLaunchOptionsURLKey]];
    }

    return nil;
}

- (NSDictionary *) readMasterConfig: (NSString *) surl
{
    NSURL        *plistURL = [NSURL URLWithString: [surl stringByTrimmingWhitespace]];
    NSDictionary *tempDict = [[[NSDictionary alloc] initWithContentsOfURL: plistURL]
                              autorelease];

    if (!tempDict)
        return nil;
    //  [self dieFromMisconfiguration: [NSString stringWithFormat: @"Error reading plist %@", url]];

    self.settingsManager.lastURL = plistURL;

    return tempDict;
}

- (void) setupReachability
{
    DataManager *dm = self.dataManager;

    dm.hostReachabilityImage = @"still-initializing";
    dm.hostReachabilityStatus = @"still-initializing";
    dm.internetReachabilityImage = @"still-initializing";
    dm.internetReachabilityStatus = @"still-initializing";
    dm.summaryReachabilityImage = @"still-initializing";
    dm.summaryReachabilityStatus = @"still-initializing";
    dm.localWiFiReachabilityImage = @"still-initializing";
    dm.localWiFiReachabilityStatus = @"still-initializing";

    self.hostReachability = [MCNetworkReachability reachabilityWithName: @"www.apple.com"];

    self.hostReachability.delegate = self;

    [self updateInterfaceWithReachability: self.hostReachability];

    self.internetReachability = [MCNetworkReachability reachabilityForInternetConnection];

    self.internetReachability.delegate = self;

    [self updateInterfaceWithReachability: self.internetReachability];

    self.localWiFiReachability = [MCNetworkReachability reachabilityForLocalWiFi];

    self.localWiFiReachability.delegate = self;

    [self updateInterfaceWithReachability: self.localWiFiReachability];
}

- (BOOL) startupFromLaunchOptions: (UIApplication *) app
                          options: (NSDictionary *) options
{
    return NO;


}

- (NSString *) statusToDictionary: (NSString *) dictTextKey
                        imageView: (NSString *) dictImageKey
                     reachability: (MCNetworkReachability *) reachability
{
    BOOL      connectionRequired = reachability.connectionRequired;
    NSString *statusString = @"";

    switch (reachability.status)
    {
        case MCNetworkStatusNotReachable :
        {
            statusString = @"Access Not Available";
            // image =  @"stop-32.png" ;
            //Minor interface detail- connectionRequired may return yes, even when the host is unreachable.  We cover that up here...
            connectionRequired = NO;
            break;
        }

        case MCNetworkStatusReachableViaWWAN :
        {
            statusString = @"Reachable via WWAN";
            // image = @"WWAN5.png";
            break;
        }

        case MCNetworkStatusReachableViaWiFi :
        {
            statusString = @"Reachable via WiFi";
            //image = @"Airport.png";
            break;
        }
    }

    if (connectionRequired)
        statusString = [NSString stringWithFormat: @"%@, Connection Required", statusString];

    //[dict setObject: statusString forKey: dictTextKey];
    //[dict setObject: image forKey: dictImageKey];

    APD_LOG (@"Network Status Change -- %@ to %@ image %@",
             statusString,
             dictTextKey,
             dictImageKey);

    return statusString;
}

- (void) updateInterfaceWithReachability: (MCNetworkReachability *) reachability
{
    if (reachability == self.hostReachability)
    {
        //[self statusToDictionary: @"hostReachabilityStatus"
        //               imageView: @"hostReachabilityImage"
        //            reachability: reachability];

        if (reachability.status == MCNetworkStatusReachableViaWWAN)
        {
            NSString *labelText = (reachability.connectionRequired ?
                                   @"Cellular data network is not yet connected." :
                                   @"Cellular data network is active.");

            //summaryLabel.text = labelText;

            APD_LOG (@"Network Reach Summary -- %@", labelText);

            //[dict setObject: labelText
            //         forKey: @"summaryReachabilityStatus"];

            DataManager *dm = self.dataManager;

            dm.summaryReachabilityStatus = labelText;
        }
    }
    else if (reachability == self.internetReachability)
    {
        //[self statusToDictionary: @"internetReachabilityStatus"
        //               imageView: @"internetReachabilityImage"
        //            reachability: reachability];
    }
    else if (reachability == self.localWiFiReachability)
    {
        //[self statusToDictionary: @"localWiFiReachabilityStatus"
        //               imageView: @"localWiFiReachabilityImage"
        //            reachability: reachability];
    }
}

- (void) writeLinksToLog
{
    // called only from special build to make link farm
#define LOGLinks(format, ...) CFShow ([NSString stringWithFormat: format, ## __VA_ARGS__])

    DataManager *dm = self.dataManager;
    NSArray     *scenes = [dm allScenes];

    if (!scenes)
        return;

    for (NSDictionary *scene in scenes)
    {
        NSArray    *blocks = [scene objectForKey: @"blocks"];
        NSUInteger  blockCount = [blocks count];

        for (NSUInteger idx = 0; idx < blockCount; idx++)
        {
            NSDictionary *block = [dm currentBlock: idx
                                          forScene: scene];

            if (!block)
                return;

            NSString *blockType = [block objectForKey: @"Action"];

            if ([@"ShowDetail" isEqual: blockType])
                LOGLinks  (@"<li><a  title='%@ - %@' href='%@' >%@</a></li>\n\r",
                           [[block objectForKey: @"Info"] stringByTrimmingWhitespace],
                           [[block objectForKey: @"Label"] stringByTrimmingWhitespace],
                           [[block objectForKey: @"URL"] stringByTrimmingWhitespace],
                           [[block objectForKey: @"Title"] stringByTrimmingWhitespace]);
        }
    }
}

#pragma mark Overridden MCApplicationDelegate Methods

- (void) setActiveMasterViewController: (UIViewController *) mvc
                              animated: (BOOL) animated
{
    //
    // Disallow changing the active master view controller:
    //
    //    if (self.activeMasterViewController != mvc)
    //    {
    //    // raise holy hell!
    //    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->groupListViewController_ release];
    [self->groupRootController_ release];
    [self->hostReachability_ release];
    [self->infoViewController_ release];
    [self->internetReachability_ release];
    [self->localWiFiReachability_ release];
    [self->sessionRandomID_ release];

    [super dealloc];
}

#pragma mark MCNetworkReachabilityDelegate Methods

- (void) reachabilityDidChange: (MCNetworkReachability *) reachability
{
    [self.sessionManager checkNetworkStatus];  // ???

    [self updateInterfaceWithReachability: reachability];
}

#pragma mark UIAlertViewDelegate Methods

- (void) alertView: (UIAlertView *) av
clickedButtonAtIndex: (NSInteger) idx
{
    switch (av.tag)
    {
        case ERROR_ALERT_VIEW_TAG :
            exit (1);
            break;

        default :
            NSAssert1 (NO,
                       @"Unknown alert view tag: %d",
                       av.tag);
            break;
    }
}

#pragma mark UIApplicationDelegate Methods

- (BOOL) application: (UIApplication *) app
didFinishLaunchingWithOptions: (NSDictionary *) options
{
    //
    // We want to handle the network activity indictor ourselves:
    //
    [MCFormDataRequest setShouldUpdateNetworkActivityIndicator: NO];

    //
    // By default, our target UI idiom is the device's UI idiom; this can
    // be overridden elsewhere via preferences or plist:
    //
    self->targetIdiom_ = UI_USER_INTERFACE_IDIOM ();

    //  DataManager *dm = self.dataManager;

    // make a new random number each time we restart, it will get salted into filenames
    srand (time (NULL));

    self.sessionRandomID = [NSString stringWithFormat: @"%d", rand () % 13171543];
	
#define STDERR_OUT [NSHomeDirectory() stringByAppendingPathComponent:@"tmp/stderr.txt"]
	freopen([STDERR_OUT fileSystemRepresentation], "w", stderr);
    ////
    //// if started with options, its almost a different program, so handle separately, we will pay little attention to settings
    ////
    if (options)
        return [self startupFromLaunchOptions: app
                                      options: options];

    [app setStatusBarStyle: UIStatusBarStyleBlackOpaque];  // start as black if run straight up

    [self setupControllers];

    return YES;
}

@end

#pragma mark -
#pragma mark Public Class NSObject Additions
#pragma mark -

@implementation NSObject (AppDelegate)

@dynamic appDelegate;

#pragma mark Public Class Methods

+ (AppDelegate *) appDelegate
{
    return [AppDelegate sharedInstance];
}

#pragma mark Public Instance Methods

- (AppDelegate *) appDelegate
{
    return [AppDelegate sharedInstance];
}

@end
