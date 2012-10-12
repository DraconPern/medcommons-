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
//#import "DocumentsManager.h"

#import "SettingsManager.h"
#import "StyleManager.h"

#import "StartupController.h"

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

@interface AppDelegate () < UIAlertViewDelegate>
@property (nonatomic, retain, readwrite) NSString              *sessionRandomID;

- (NSDictionary *) handleOpenURL: (NSURL *) url;

- (NSDictionary *) processLaunchOptions: (NSDictionary *) options;


//
//- (NSString *) statusToDictionary: (NSString *) dictTextKey
//                        imageView: (NSString *) dictImageKey
//                     reachability: (MCNetworkReachability *) reachability;

@end

@implementation AppDelegate

@dynamic    dataManager;
//@dynamic    documentsManager;
@synthesize SettingsViewController         = SettingsViewController_;

//@dynamic    navigationController;
@synthesize sessionRandomID            = sessionRandomID_;
@dynamic    settingsManager;
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
    UIAlertView *av = [[[UIAlertView alloc] initWithTitle: @"Misconfigured"
                                                  message: msg
                                                 delegate: self
                                        cancelButtonTitle: @"OK"
                                        otherButtonTitles: nil]
                       autorelease];
	
    av.tag = ERROR_ALERT_VIEW_TAG;
	
    [av show];
}

//
//- (DocumentsManager *) documentsManager
//{
//    return [DocumentsManager sharedInstance];
//}


- (SettingsManager *) settingsManager
{
    return [SettingsManager sharedInstance];
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


- (BOOL)application:(UIApplication *)app handleOpenURL:(NSURL *)url 
{
	// Do something with the url here
	
	NSString *fullname = [NSString stringWithFormat:@"%@",url ];

	
	NSString *result = (NSString *)CFURLCreateStringByReplacingPercentEscapesUsingEncoding(kCFAllocatorDefault,
																						   (CFStringRef)fullname,
																						   CFSTR(""),
																						   kCFStringEncodingUTF8);
	NSString *name = [[result stringByDeletingPathExtension] lastPathComponent];
	//NSString *type = [result pathExtension];
	
	
	NSLog (@"application:handleOpenURL: %@ with full %@ name %@ result %@", app, fullname,name, result);
	
	[[DataManager sharedInstance ] newSetListFromFile:result
							  name:name];
	
	[result release];
	return NO;
}

- (BOOL) startupFromLaunchOptions: (UIApplication *) app
                          options: (NSDictionary *) options
{
    //
    // broken out as a completely separate case, these behaviors will be made even more different
    //
	
	
		
	NSString *filePath = [NSString stringWithFormat:@"%@",(NSURL *)[options objectForKey:UIApplicationLaunchOptionsURLKey]];

	NSString *name = [[filePath stringByDeletingPathExtension] lastPathComponent];

	
	NSLog (@"skipping startupFromLaunchOptions:options: %@ with file name %@ url %@", app, name, [options objectForKey:UIApplicationLaunchOptionsURLKey]);
	//[DataManager newSetListFromURL:(NSURL *)[options objectForKey:UIApplicationLaunchOptionsURLKey] 
//							  name: name];
	
    return NO;
}




#pragma mark Overridden MCApplicationDelegate Methods

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
	[self->SettingsViewController_ release];
	
    [super dealloc];
}

#pragma mark MCNetworkReachabilityDelegate Methods
/*
 - (void) reachabilityDidChange: (MCNetworkReachability *) reachability
 {
 [self.sessionManager checkNetworkStatus];  // ???
 
 [self updateInterfaceWithReachability: reachability];
 }
 */
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
	
    [app setStatusBarStyle: UIStatusBarStyleBlackOpaque];  // start as black if run straight up
	
    //
    //
    self->targetIdiom_ = UI_USER_INTERFACE_IDIOM ();
	
    // make a new random number each time we restart, it will get salted into filenames
    srand (time (NULL));
	
    self.sessionRandomID = [NSString stringWithFormat: @"%d", rand () % 13171543];
	
	if (!window)
	{
		// once only startup, even if re-entered here while running
	// redirect errors so we can see logfile here via settings, comment this out for normal logging

    #define STDERR_OUT [NSHomeDirectory() stringByAppendingPathComponent:@"tmp/stderr.txt"]
	//NSLog (@"Redirecting Trace to inapp display...");
	freopen([STDERR_OUT fileSystemRepresentation], "w", stderr);
	NSLog (@"Starting Controllers...");
    window = [[UIWindow alloc] initWithFrame: [UIScreen mainScreen].bounds];
	
	StartupController  *startupController = [[[StartupController alloc] initWithWindow:window] // let startup controller do dirty work
												autorelease];	
	
	
	UINavigationController *tnav = [[[UINavigationController alloc] initWithRootViewController:startupController] autorelease];
	
	
	
	[window addSubview:tnav.view];
	
	 
    [window makeKeyAndVisible];
		
	}
		
	
    ////
    //// if started with options, its almost a different program, so handle separately, we will pay little attention to settings
    ////
    if (options)
		// return 
		[self startupFromLaunchOptions: app
							   options: options];
	
	
	
    return YES;
}

//- (UINavigationController *) navigationController
//{
//    return self->navigationController_;
//}


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

// code to colorize the navbars

//- (void)drawRect:(CGRect)rect {
//	UIColor *color = [UIColor blackColor];
//	UIImage *img	= [UIImage imageNamed: @"nav.png"];
//	[img drawInRect:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
//	self.tintColor = color;
//}
@implementation UINavigationBar (UINavigationBarCategory)
- (void)drawRect:(CGRect)rect {
	UIColor *color = [DataManager sharedInstance].appColor;
	
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
	CGContextFillRect(context, rect);
	self.tintColor = color;
	
//	self.barStyle = UIBarStyleBlack;
//	self.translucent = YES;
}
@end
@implementation UIToolbar (UIToolbarCategory)
- (void)drawRect:(CGRect)rect {

	UIColor *color = [DataManager sharedInstance].appColor;
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
	CGContextFillRect(context, rect);
	self.tintColor = color;
	
}
@end

@implementation UISearchBar (UISearchBarCategory)
- (void)drawRect:(CGRect)rect {
	UIColor *color = [DataManager sharedInstance].appColor;
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
	CGContextFillRect(context, rect);
	self.tintColor = color;
	self.barStyle = UIBarStyleBlack;
	self.translucent = YES;
}
@end


