//
//  AppDelegate.m
//  GigStand
//
//  Created by Bill Donner on 3/4/10.
//
//  The HTTP Server class was created by Nonnus,
//  who graciously decided to share it with the CocoaHTTPServer community.
//

#import "HTTPServer.h"
#import "MyHTTPConnection.h"
#import "GigStandAppDelegate.h"
#import "DataManager.h"
#import "DataStore.h"
#import "SettingsManager.h"
#import "StartupController.h"
#import "ModalAlert.h"
#import "BonJourManager.h"
#import "TunesManager.h"
#import "ArchivesManager.h"

#pragma mark -
#pragma mark Public Class AppDelegate
#pragma mark -

#pragma mark Internal Constants

//
// Assorted view tags:
//
enum
{
    ERROR_ALERT_VIEW_TAG    = 666,
	
};

@interface GigStandAppDelegate () < UIAlertViewDelegate>

@end

@implementation GigStandAppDelegate

@synthesize window,myLocalIP,myLocalPort;
#pragma mark Public Instance Methods
+ (GigStandAppDelegate *) sharedInstance
{
	static GigStandAppDelegate *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[GigStandAppDelegate alloc] init];
	}
	
	return SharedInstance;
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


#pragma mark Private Instance Methods

- (NSDictionary *) parseURL: (NSURL *) url
{
	//this is where we come if started by a hyperlink, which is not really used right now
	
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
	
    NSLog (@"parseURL %@", ret);
	
    return ret;
}



#pragma mark Overridden NSObject Methods


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
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
	// The incoming URL must be manipulated to produce a similar URL in the top level Documents directory
	//  The URL is copied over literally
	NSString *v = [NSString stringWithFormat:@"%@",url ];
	
	NSString *filePath = (NSString *)CFURLCreateStringByReplacingPercentEscapesUsingEncoding(kCFAllocatorDefault,(CFStringRef)v,CFSTR(""),kCFStringEncodingUTF8);
	NSString *ext = [filePath pathExtension];
	NSString *name = [[filePath stringByDeletingPathExtension] lastPathComponent];
	[filePath release];
	NSError *error; 
	NSData *data = [NSData dataWithContentsOfURL:url options:0 error:&error];
	NSString *dest =  [[DataStore pathForItunesInbox] 
					   stringByAppendingString:[NSString stringWithFormat:@"/%@.%@",name,ext]];
	[data writeToFile: dest
		   atomically:NO];
	
	[[NSFileManager defaultManager] removeItemAtURL: url error: &error]; // get rid of incoming file once its been copied
	
	NSLog (@"openURLfrom %@ copied incoming to %@ len %d",  sourceApplication,  dest, [data length]);//,[error localizedDescription]);
	
	return NO;
}

- (BOOL) startupFromLaunchOptions: (UIApplication *) app options: (NSDictionary *) options
{
    //
    // broken out as a completely separate case, these behaviors will be made even more different
    //
	// at this point there is no reason to do anything here, since openURL seems to get called in every case 	
    return NO;
}
-(void) screenDidConnectNotification: (NSNotification *)notification
{
	NSLog (@"screenDidConnectNotification %@", notification);
}
-(void) screenDidDisconnectNotification: (NSNotification *)notification
{
	NSLog (@"screenDidDisconnectNotification %@", notification);
}

-(void) contextDidSaveNotification: (NSNotification *)notification
{
	NSLog (@"contextDidSaveNotification %@"); // too much
}
-(void) contextObjectsDidChangeNotification: (NSNotification *)notification
{
	//NSLog (@"contextObjectsDidChangeNotification"); //too much changed
}
-(void) contextWillSaveNotification: (NSNotification *)notification
{
	NSLog (@"contextWillSaveNotification");// %@", notification);
}



- (BOOL) application: (UIApplication *) app didFinishLaunchingWithOptions: (NSDictionary *) options
{//
	//	NSLog(@"Touching %@ and %@",self.managedObjectContext,self.managedObjectModel); // construct coredata stuff
	//	
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(contextDidSaveNotification:)
												 name:	NSManagedObjectContextDidSaveNotification
											   object:self.managedObjectContext];
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(contextObjectsDidChangeNotification:)
												 name:	NSManagedObjectContextObjectsDidChangeNotification
											   object:self.managedObjectContext];
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(contextWillSaveNotification:)
												 name:	NSManagedObjectContextWillSaveNotification
											   object:self.managedObjectContext];
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(screenDidConnectNotification:)
												 name:	UIScreenDidConnectNotification
											   object:nil];
	
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(screenDidDisconnectNotification:)
												 name:	UIScreenDidDisconnectNotification
											   object:nil];
	
    [app setStatusBarStyle: UIStatusBarStyleBlackOpaque];  // start as black if run straight up
	
    // make a new random number each time we restart, it will get salted into filenames
    srand (time (NULL));
	
    self->sessionRandomID = [NSString stringWithFormat: @"%d", rand () % 13171543];
	
	if (!window)
	{
		// once only startup, even if re-entered here while running
		// redirect errors so we can see logfile here via settings, comment this out for normal logging
		
		BOOL debugtrace  = [[SettingsManager sharedInstance] debugTrace];
		BOOL wifiwebserver  = [[SettingsManager sharedInstance] wifiWebserver];
		
		BOOL bonjourWithPeers  = [[SettingsManager sharedInstance] bonjourWithPeers];
		BOOL sim=NO;
		
#if TARGET_IPHONE_SIMULATOR
		
		sim=YES;
		
#else
		
		//		if (debugtrace == YES) 
		//		
		{
			NSLog (@"<<<<<<<<<<<Redirecting Device Trace to inapp display...>>>>>>>>>>>>>>>");
			
			freopen([STDERR_OUT fileSystemRepresentation], "a", stderr); // dont wipe out
		}
#endif
		NSLog (@"<<<<<<<<<<<<%@ %@ Starting Controllers (sim=%D,trace=%D,wifiws=%D,bjwpeers=%D) ...>>>>>>>>>>>>>>>>>>",
			   
			   [DataManager sharedInstance].applicationName,
			   [DataManager sharedInstance].applicationVersion, sim,debugtrace,wifiwebserver,bonjourWithPeers);
		
		
		[TunesManager setup:self.managedObjectContext]; // very important, must pass in by property
		[ArchivesManager setup:self.managedObjectContext]; // very important, must pass in by property
		
		
		
		self->window = [[UIWindow alloc] initWithFrame: [UIScreen mainScreen].bounds];
		
		StartupController  *startupController = [[[StartupController alloc] initWithWindow:self->window] // let startup controller do dirty work
												 autorelease];	
		UINavigationController *tnav = [[[UINavigationController alloc] 
										 initWithRootViewController:startupController] autorelease];
		[self->window addSubview:tnav.view];
		[self->window makeKeyAndVisible];
		
		
		[BonJourManager starting]; // call whatever the startup is for bonjour, might be missing
		
		
	}
	
	
    ////
    //// if started with options, its almost a different program, so handle separately, we will pay little attention to settings
    ////
    if (options)
		// return 
		[self startupFromLaunchOptions: app options: options];

    return YES;
}




//- (IBAction)startStopServer:(id)sender
//{
//	if ([sender isOn])
//	{
//		// You may OPTIONALLY set a port for the server to run on.
//		// 
//		// If you don't set a port, the HTTP server will allow the OS to automatically pick an available port,
//		// which avoids the potential problem of port conflicts. Allowing the OS server to automatically pick
//		// an available port is probably the best way to do it if using Bonjour, since with Bonjour you can
//		// automatically discover services, and the ports they are running on.
//		//	[httpServer setPort:8080];
//		
//		NSError *error;
//		if(![httpServer start:&error])
//		{
//			NSLog(@"Error starting HTTP Server: %@", error);
//		}
//		
//		[self localhostAdressesResolved:nil];
//	}
//	else
//		[httpServer stop];
//}





- (void)applicationDidBecomeActive:(UIApplication *)application {
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
	//	NSLog(@"/////////////////////////GSA applicationDidBecomeActive on %@:%d /////////////////////////",
	//		  [GigStandAppDelegate sharedInstance].myLocalIP,
	//		  [GigStandAppDelegate sharedInstance].myLocalPort);
	
	BOOL wifiwebserver  = [[SettingsManager sharedInstance] wifiWebserver];
	
	if (wifiwebserver)
	{ 
		if (!httpServer)
		{
			//NSLog(@"/////////starting internal http server");
			// optionally start the webserver
			NSString *root = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask,YES) objectAtIndex:0];
			httpServer = [HTTPServer new];
			[httpServer setType:@"_http._tcp."];
			[httpServer setDomain:@""]; // must set something
			//	[httpServer setName:[[UIDevice currentDevice] name]];
			[httpServer setConnectionClass:[MyHTTPConnection class]];
			[httpServer setDocumentRoot:[NSURL fileURLWithPath:root]];
			
			
			// start the webserver
			
			NSError *error;
			if(![httpServer start:&error])
			{
				NSLog(@"Error starting HTTP Server: %@", error);
			}
			else 
			{
				//NSLog(@"///////restarting http server");
			}
			
		}
		else {
			// kick bonjour in the butt a bit
			[self->httpServer kickstart];
			
		}
		
		
	} 
	
}




#pragma mark TunesManager
- (void)applicationWillResignActive:(UIApplication *)application {
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
	
	/*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, called instead of applicationWillTerminate: when the user quits.
     */
	NSLog(@"applicationDidEnterBackground  saveContext......");
    [self saveContext];
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    /*
     Called as part of the transition from the background to the inactive state: here you can undo many of the changes made on entering the background.
     */
	
	[[BonJourManager sharedInstance] publishTXTFromLastTitle];
}



/**
 applicationWillTerminate: saves changes in the application's managed object context before the application terminates.
 */
- (void)applicationWillTerminate:(UIApplication *)application {
	NSLog(@"applicationWillTerminate saveContext");
    [self saveContext];
}


- (void)saveContext {
    
	//NSLog (@"saveContext called....");
    NSError *error = nil;
	NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) {
        if (//[managedObjectContext hasChanges] && 
			
			![managedObjectContext save:&error]) 
		{
			
            NSLog(@">>>>>>>>>Error in saveContext %@, %@", error, [error userInfo]);
            //abort();
        } 
		//else NSLog(@"saveContext completed ...");
    }
	
}    


#pragma mark -
#pragma mark Core Data stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext {
    
    if (managedObjectContext_ != nil) {
        return managedObjectContext_;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil) {
        managedObjectContext_ = [[NSManagedObjectContext alloc] init];
        [managedObjectContext_ setPersistentStoreCoordinator:coordinator];
		
    }
	
    return managedObjectContext_;
}


/**
 Returns the managed object model for the application.
 If the model doesn't already exist, it is created from the application's model.
 */
- (NSManagedObjectModel *)managedObjectModel {
    
    if (managedObjectModel_ != nil) {
        return managedObjectModel_;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"GigStand" withExtension:@"momd"];
    managedObjectModel_ = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];    
    return managedObjectModel_;
}


/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 */
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator {
    
    if (persistentStoreCoordinator_ != nil) {
        return persistentStoreCoordinator_;
    }
    
	//	
	NSError *error;
	NSString *dndDir = [DataStore pathForSharedDocuments];
	
	if ([[NSFileManager defaultManager] fileExistsAtPath:dndDir]==NO) 
	{
		NSLog (@"persistentStoreCoordinator creating dnd directory");
		[[NSFileManager defaultManager] createDirectoryAtPath:dndDir
								  withIntermediateDirectories:NO attributes:nil error:nil];
		if (![[NSFileManager defaultManager] fileExistsAtPath:dndDir])
		{
			NSLog (@"persistentStoreCoordinator could not create directory %@",dndDir);
			abort();
		}
	}
		else
			NSLog (@"persistentStoreCoordinator dnd directory existed");
		// setup sqlite db			
		NSURL *dndurl = [self applicationDocumentsDirectory];
		NSURL *storeURL = [dndurl  URLByAppendingPathComponent:@"/DONOTDISTURB/GigBase.sqlite"];			
		error = nil;
		persistentStoreCoordinator_ = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
	
	NSMutableDictionary *pragmaOptions = [NSMutableDictionary dictionary];
	[pragmaOptions setObject:@"FULL" forKey:@"synchronous"]; // also try full
	[pragmaOptions setObject:@"1" forKey:@"fullfsync"];
	
	NSDictionary *storeOptions =
    [NSDictionary dictionaryWithObject:pragmaOptions forKey:NSSQLitePragmasOption];
		if (![persistentStoreCoordinator_ addPersistentStoreWithType:NSSQLiteStoreType 
													   configuration:nil URL:storeURL options:storeOptions error:&error])
		{
			/*
			 Replace this implementation with code to handle the error appropriately.
			 
			 If you encounter schema incompatibility errors during development, you can reduce their frequency by:
			 * Simply deleting the existing store:
			 [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil]
			 
			 * Performing automatic lightweight migration by passing the following dictionary as the options parameter: 
			 [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES],
			 NSMigratePersistentStoresAutomaticallyOption, 
			 [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
			 
			 Lightweight migration will only work for a limited set of schema changes; consult "Core Data Model Versioning and Data Migration Programming Guide" for details.
			 
			 */
			NSLog(@"Error setting up sqlite  %@, %@", error, [error userInfo]);
			
		}
		else NSLog (@"persistentStoreCoordinator Sqlite successfully setup"); 

	return persistentStoreCoordinator_;
}


#pragma mark -
#pragma mark Application's Documents directory

/**
 Returns the URL to the application's Documents directory.
 */
- (NSURL *)applicationDocumentsDirectory {
	return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}


#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
	/*
	 Free up as much memory as possible by purging cached data objects that can be recreated (or reloaded from disk) later.
	 */
}


- (void)dealloc {
	[managedObjectContext_ release];
	[managedObjectModel_ release];
	[persistentStoreCoordinator_ release];
	[httpServer release];
	[window release];
	[super dealloc];
}

@end

#pragma mark Extensions to Colorize Components 

@implementation UINavigationBar (UINavigationBarCategory)
- (void)drawRect:(CGRect)rect {
	UIColor *color = [DataManager applicationColor];
	
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
	CGContextFillRect(context, rect);
	self.tintColor = color;

}
@end
@implementation UIToolbar (UIToolbarCategory)
- (void)drawRect:(CGRect)rect {
	
	UIColor *color = [DataManager applicationColor];
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
	CGContextFillRect(context, rect);
	self.tintColor = color;
	
}
@end

@implementation UISearchBar (UISearchBarCategory)
- (void)drawRect:(CGRect)rect {
	UIColor *color = [DataManager applicationColor];
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
	CGContextFillRect(context, rect);
	self.tintColor = color;
	self.barStyle = UIBarStyleBlack;
	self.translucent = YES;
}
@end
