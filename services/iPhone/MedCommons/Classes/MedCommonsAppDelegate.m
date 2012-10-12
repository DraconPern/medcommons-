//
//  MedCommonsAppDelegate.m
//  MedCommons
//
//  Created by bill donner on 5/7/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//


#import "MedCommonsAppDelegate.h"
#import "MedCommonsViewController.h"
#import "mcXmlReader.h"
#import "Person.h"
#import "mcUrlAlert.h"
#import <SystemConfiguration/SystemConfiguration.h>

static NSString *feedURLString = @"http://fb01.medcommons.net/facebook/001/wsx.php?export=1229257778";
@implementation MedCommonsAppDelegate
@synthesize window;
@synthesize navigationController;
@synthesize list;
@synthesize dataPath;
@synthesize filePath;



- init {
	if (self = [super init]) {
		// Initialization code
	}
	return self;
}
- (void) initCache
{
	
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    self.dataPath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"FamilyCareTeam"];
	
	if ([[NSFileManager defaultManager] fileExistsAtPath:dataPath]) {
		return;
	}
	
	
	if (![[NSFileManager defaultManager] createDirectoryAtPath:dataPath 
								   withIntermediateDirectories:NO
													attributes:nil 
														 error:&error]) {
		mcUrlAlertWithError(error);
		return;
	}
	
}
- (void)showPersonInfo:(Person *)PersonInfo
{
    // When the user taps a row in the table, display the associated medcommons healthurl
	NSString *webLink = [PersonInfo webLink];
	NSLog(@"opening medcommons healthurl %@",webLink);
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:webLink]];
}

// Use the SystemConfiguration framework to determine if the host that provides
// the RSS feed is available.
- (BOOL)isDataSourceAvailable
{
	/* static BOOL checkNetwork = YES;
	 if (checkNetwork) { // Since checking the reachability of a host can be expensive, cache the result and perform the reachability check once.
	 checkNetwork = NO;
	 
	 Boolean success;    
	 const char *host_name = "fb01.medcommons.net";
	 
	 SCNetworkReachabilityRef reachability = SCNetworkReachabilityCreateWithName(NULL, host_name);
	 SCNetworkReachabilityFlags flags;
	 success = SCNetworkReachabilityGetFlags(reachability, &flags);
	 _isDataSourceAvailable = success && (flags & kSCNetworkFlagsReachable) && !(flags & kSCNetworkFlagsConnectionRequired);
	 CFRelease(reachability);
	 }
	 return _isDataSourceAvailable;
	 */
	return YES;
}

- (void)getPersonData
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    
	NSError *parseError = nil;
	
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    
    mcXmlReader *streamingParser = [[mcXmlReader alloc] init];
    [streamingParser parseXMLFileAtURL:[NSURL URLWithString:feedURLString] parseError:&parseError];
	if (parseError != 0) NSLog(@"XMLparseError %@ is %x",feedURLString,parseError);
    [streamingParser release];        
    [pool release];
    
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
}

- (void)reloadTable
{
    [[(MedCommonsViewController *)[self.navigationController topViewController] tableView] reloadData];
}

- (void)addToPersonList:(Person *)newPerson
{
    [self.list addObject:newPerson];
    // The XML parser calls addToPersonList: each time it creates an earthquake object.
    // The table needs to be reloaded to reflect the new content of the list.
    [self reloadTable];
}

- (void)applicationDidFinishLaunching:(UIApplication *)application {
	
    self.list = [NSMutableArray array];
	
	application.networkActivityIndicatorVisible = YES;
	
	/* By default, the Cocoa URL loading system uses a small shared memory cache. 
	 We don't need this cache, so we set it to zero when the application launches. */
	
    /* turn off the NSURLCache shared cache 	*/
    
    NSURLCache *sharedCache = [[NSURLCache alloc] initWithMemoryCapacity:0 
                                                            diskCapacity:0 
                                                                diskPath:nil];
    [NSURLCache setSharedURLCache:sharedCache];
    [sharedCache release];
	// create our own cache up above
	[self initCache];
	
	
	// Create the navigation and view controllers
	//RootViewController *rootViewController = [[RootViewController alloc] initWithStyle:UITableViewStylePlain];

	
	// Create the navigation and view controllers
	MedCommonsViewController *medCommonsViewController = [[MedCommonsViewController alloc] initWithStyle:UITableViewStylePlain];
	UINavigationController *aNavigationController = [[UINavigationController alloc] initWithRootViewController:medCommonsViewController];
	self.navigationController = aNavigationController;
	[aNavigationController release];
	[medCommonsViewController release];
	
	// Configure and show the window
	[window addSubview:[navigationController view]];
    
    if ([self isDataSourceAvailable] == NO) {
        return;
    }
    
    
	// [NSThread detachNewThreadSelector:@selector(getPersonData) toTarget:self withObject:nil];
	[self getPersonData];
	
	/* set the view's background to gray pinstripe */
    window.backgroundColor = [UIColor groupTableViewBackgroundColor];
	
	/* set initial state of network activity indicators */
	//[self stopAnimation];
    
	application.networkActivityIndicatorVisible = NO;
	/* initialize the user interface */
	//[self initUI];
	
	/* If the window's "Visible at Launch" setting is checked in Interface Builder, 
	 this message is not needed. In this case, however, the setting is unchecked 
	 because we don't want the window to be visible until we set the background color. 
	 */
	[window makeKeyAndVisible];
}

- (void)applicationWillTerminate:(UIApplication *)application {
}

- (NSUInteger)countOfList {
	return [list count];
}

- (id)objectInListAtIndex:(NSUInteger)theIndex {
	return [list objectAtIndex:theIndex];
}

- (void)getList:(id *)objsPtr range:(NSRange)range {
	[list getObjects:objsPtr range:range];
}

// The following are from the template but are not used in this sample.
//- (void)insertObject:(id)obj inListAtIndex:(NSUInteger)theIndex {
//	[list insertObject:obj atIndex:theIndex];
//}
//
//- (void)removeObjectFromListAtIndex:(NSUInteger)theIndex {
//	[list removeObjectAtIndex:theIndex];
//}
//
//- (void)replaceObjectInListAtIndex:(NSUInteger)theIndex withObject:(id)obj {
//	[list replaceObjectAtIndex:theIndex withObject:obj];
//}


- (void)dealloc {
	[navigationController release];
	[window release];
	[list release];
	[super dealloc];
}

@end

