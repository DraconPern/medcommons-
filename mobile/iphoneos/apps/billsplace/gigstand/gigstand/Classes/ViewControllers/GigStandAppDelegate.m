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
#import "ModalAlert.h"
#import "BonJourManager.h"
#import "TunesManager.h"
#import "ArchivesManager.h"
#import "SetListsManager.h"
#import "LogManager.h"
#import "ipadHomeSplashController.h"
#import "iphoneHomeController.h"


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

- (NSPersistentStoreCoordinator *)persistentStoreCoordinator:(NSString *)backtrace;




@end

@implementation GigStandAppDelegate

@synthesize window;

#pragma mark Public Instance Methods

- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
	return [self persistentStoreCoordinator:@"satisfacation-of-property"];
}
- (NSManagedObjectContext *)managedObjectContext
{
	
	return [self managedObjectContext:@"satisfacation-of-property"];
}
- (id) init
{
	self = [super init];
	if (self)
	{
		
	}
	return self;
}
+ (GigStandAppDelegate *) sharedInstance
{
	static GigStandAppDelegate *SharedInstance;
	
	if (!SharedInstance)
	{
		SharedInstance = [[GigStandAppDelegate alloc] init];
	}
	
	return SharedInstance;
}
//+(void) dump:(NSString *) prompt
//{
//	NSLog (@"%@ dump -- guardStart %d  guardEnd %d ",prompt,[GigStandAppDelegate sharedInstance].guardStart,[GigStandAppDelegate sharedInstance].guardEnd);
//}

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





#pragma mark Overridden NSObject Methods



#pragma mark UIApplicationDelegate Methods


-(void) finishSetup
{
	//[[GigStandAppDelegate sharedInstance] dump:@"from finishSetup"];
	
    
}



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
	NSString *dest =  [[DataStore pathForItunesInbox] stringByAppendingString:[NSString stringWithFormat:@"/%@.%@",name,ext]];
	[data writeToFile: dest atomically:NO];
	
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


-(void) contextObjectsDidChangeNotification: (NSNotification *)notification
{
	//NSLog (@"contextObjectsDidChangeNotification %@",notification); //too much changed
}
-(void) contextWillSaveNotification: (NSNotification *)notification
{
	//NSLog (@"contextWillSaveNotification %@",notification); // %D", [[[notification.userInfo] objectForKey:@"inserted" ]count]);
}
-(void) contextDidSaveNotification: (NSNotification *)notification
{
	NSLog (@"contextDidSaveNotification");//,notification);// [[[notification.userInfo] objectForKey:@"inserted" ] count]);
}

- (BOOL) application: (UIApplication *) app didFinishLaunchingWithOptions: (NSDictionary *) options
{
	
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(screenDidConnectNotification:)
												 name:	UIScreenDidConnectNotification
											   object:nil];
	
	[[NSNotificationCenter defaultCenter] addObserver:self 
											 selector:@selector(screenDidDisconnectNotification:)
												 name:	UIScreenDidDisconnectNotification
											   object:nil];
	
    [app setStatusBarStyle: UIStatusBarStyleBlackOpaque];  // start as black if run straight up
	
	
	if (!window)
	{
		// once only startup, even if re-entered here while running
		// redirect errors so we can see logfile here via settings, comment this out for normal logging
		
		BOOL debugtrace  = [[SettingsManager sharedInstance] debugTrace];
		BOOL wifiwebserver  = [[SettingsManager sharedInstance] wifiWebserver];
		BOOL ziparchives  = [[SettingsManager sharedInstance] zipArchives];
		BOOL bonjourWithPeers  = [[SettingsManager sharedInstance] bonjourWithPeers];
		BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
		BOOL performanceMode = ![[SettingsManager sharedInstance] normalMode];
        NSUInteger gallerycount = [[SettingsManager sharedInstance] snapshotGalleryCount];
		BOOL sim=NO;
		
#if TARGET_IPHONE_SIMULATOR
		
		sim=YES;
		
#endif
		[DataManager sharedInstance].inSim = sim; 
		
		if ((sim == NO)|| (debugtrace==NO))
		{	 
			NSLog (@"<<<<<<<<<<<Redirecting Device Trace to inapp display...>>>>>>>>>>>>>>>");			
			freopen([[LogManager pathForCurrentLog] fileSystemRepresentation], "a", stderr); // dont wipe out
		}
		[LogManager rotateLogs];
		//
        //		NSPersistentStoreCoordinator *psc = [self persistentStoreCoordinator:@"didFinishLaunching"];
        
		NSLog (@"<<<<<<<<<<<<%@ %@ Starting Controllers (sim=%d,trace=%d,wifiws=%d,bjwpeers=%d,zipa=%d,gc=%d, gccollaba=%d,perform=%d) ...>>>>>>>>>>>>>>>>>>",
			   
			   [DataManager sharedInstance].applicationName,
			   [DataManager sharedInstance].applicationVersion, sim,debugtrace,wifiwebserver,bonjourWithPeers,ziparchives,gallerycount,
			   
			   collabfeatures,performanceMode
			   );
		
		
		[TunesManager setup]; // very important, must pass in by property
		[ArchivesManager setup]; // very important, must pass in by property
		[SetListsManager setup]; // very important, must pass in by property
		
		
		
		self->window = [[UIWindow alloc] initWithFrame: [UIScreen mainScreen].bounds];
        
	 
        
        NSInteger acount = [ArchivesManager archivesCount];
        NSInteger tcount = [TunesManager tuneCount];
        NSInteger icount = [TunesManager instancesCount];
        
        NSLog(@"CoreData SQLite DB has %d archives with %d tunes from %d files",acount, tcount,icount);
        
        
        
        if (acount==0 )
            
            // if we've got nothing in the db then build a new one
            [ArchivesManager buildNewDB];
        
        else 
            
            [TunesManager updateGigBaseInfo]; // otherwise just flip around some times
        
        
        
        
        // finally lets start the main controller while dismissing anything that may be running

        
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) 
        {
            
            self->vc = [[ipadHomeSplashController alloc] init ];
            

        }
        else
        {
            self->vc = [[iphoneHomeController alloc] init] ;
            
            
            // if these are released, the program hangs in startup
          
        }
        ///////
        
        self->nav = [[[UINavigationController alloc] initWithRootViewController:self->vc] retain]; // if autoreleased, it doesnt rotate        
        
   
        
        [self->window addSubview:self->nav.view];
        
        
        [self->window makeKeyAndVisible];
        
		
		if (bonjourWithPeers||collabfeatures)
			[BonJourManager starting]; // call whatever the startup is for bonjour, might be missing
		
		
	}
	
	
    ////
    //// if started with options, its almost a different program, so handle separately, we will pay little attention to settings
    ////
    if (options)
		// return 
		[self startupFromLaunchOptions: app options: options];
	
	
	//NSPersistentStoreCoordinator *psc2 = [self persistentStoreCoordinator:@"end of didFinishLaunching"];
	//NSLog(@"psc2 end of appdelegate %@",psc2);
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
	NSLog(@"/////////////////////////GSA applicationDidBecomeActive on %@:%d  psc=%@ /////////////////////////",
		  [DataManager sharedInstance].myLocalIP,
		  [DataManager sharedInstance].myLocalPort,
		  persistentStoreCoordinator_);
	
	BOOL wifiwebserver  = [[SettingsManager sharedInstance] wifiWebserver];	
	BOOL collabfeatures = [[SettingsManager sharedInstance] collabFeatures];
	if (wifiwebserver&&collabfeatures) [[DataManager sharedInstance] httpServerGo];
	
	
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
	NSLog(@"applicationDidEnterBackground saving managed object context......");
    [self saveContext:@"applicationDidEnterBackground"];
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    /*
     Called as part of the transition from the background to the inactive state: here you can undo many of the changes made on entering the background.
     */
	NSLog(@"applicationWillEnterForeground restarting......");
	[[BonJourManager sharedInstance] publishTXTFromLastTitle];
    // get the UI repainted
    [DataManager worldViewPulse];
}



/**
 applicationWillTerminate: saves changes in the application's managed object context before the application terminates.
 */
- (void)applicationWillTerminate:(UIApplication *)application {
	NSLog(@"applicationWillTerminate saveContext");
    [self saveContext:@"applicationWillTerminate"];
}


- (void)saveContext:(NSString *)backtrace {
    
	//NSLog (@"saveContext called....");
    NSError *error = nil;
	NSManagedObjectContext *managedObjectContext = [self managedObjectContext:backtrace];
    if (managedObjectContext != nil) {
		//[managedObjectContext processPendingChanges];  // worth a shot
		//      if ([managedObjectContext hasChanges] == YES) 
		//			NSLog(@"saveContext called from %@ with no changes",backtrace);
		//		//else
		{
			
			BOOL saveok = [managedObjectContext save:&error] ;
			if (saveok==NO)
			{
				
				NSLog(@">>>>>>>>>Error in saveContext from %@ is %@, %@", backtrace,error, [error localizedDescription]);
				//abort();
			} 
			else {
				
				NSLog(@"save: completed ok from %@",backtrace);
             
                
			}
			
		}
    }
	
}    


#pragma mark -
#pragma mark Core Data stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext:(NSString *)backtrace;
{
    
    if (managedObjectContext_ != nil) {
        return managedObjectContext_;
    }
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator:@"from managedObjectContext"];
    if (coordinator != nil) {
        managedObjectContext_ = [[NSManagedObjectContext alloc] init];
        [managedObjectContext_ setPersistentStoreCoordinator:coordinator];
		
    }
	
	//	NSLog (@"guardStart %d, guardEnd %d, psc %@ moc %@",
	//		   [GigStandAppDelegate sharedInstance].guardStart,
	//		   [GigStandAppDelegate sharedInstance].guardEnd,
	//		   coordinator,managedObjectContext_);
	
	NSLog (@"managedObjectContext is created from: %@",backtrace);
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
	//	
    NSURL *modelURL = [[NSBundle mainBundle] 
					   URLForResource:@"GigStand" 
					   withExtension:@"momd"];
	
    managedObjectModel_ = [[NSManagedObjectModel alloc] 
						   initWithContentsOfURL:modelURL];  
	
	
    return managedObjectModel_;
}


/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 */
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator:(NSString *)backtrace
{
    if (persistentStoreCoordinator_ != nil) {
		
		NSLog (@"persistentStoreCoordinator %@ probed and exists exists with %@",backtrace,persistentStoreCoordinator_);
		
        return persistentStoreCoordinator_;
    }
	//	how can we ever get here multiple times
	
	
	NSLog (@"persistentStoreCoordinator %@ first time with %@",backtrace,persistentStoreCoordinator_);
	
	NSError *error;
	NSString *dndDir = [DataStore pathForSharedDocuments];
	
	if ([[NSFileManager defaultManager] fileExistsAtPath:dndDir]==NO) 
	{
		NSLog (@"persistentStoreCoordinator %@ creating dnd directory",backtrace);
		[[NSFileManager defaultManager] createDirectoryAtPath:dndDir
								  withIntermediateDirectories:NO attributes:nil error:nil];
		if (![[NSFileManager defaultManager] fileExistsAtPath:dndDir])
		{
			NSLog (@"persistentStoreCoordinator %@ could not create directory %@",backtrace,dndDir);
			abort();
		}
	}
	//	else
	//		NSLog (@"persistentStoreCoordinator dnd directory existed");
	// setup sqlite db	
	NSURL *dndurl =  [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
	NSURL *storeURL = [dndurl  URLByAppendingPathComponent:@"/DONOTDISTURB/GigBase.sqlite"];			
	
	error = nil;
	//NSLog (@"prior to NSPersistentStoreCoordinator alloc %@",persistentStoreCoordinator_);
	persistentStoreCoordinator_ = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
	
	//NSLog (@"post NSPersistentStoreCoordinator alloc %@",persistentStoreCoordinator_);
	//NSMutableDictionary *pragmaOptions = [NSMutableDictionary dictionary];
	//	[pragmaOptions setObject:@"FULL" forKey:@"synchronous"]; // also try full
	//	[pragmaOptions setObject:@"1" forKey:@"fullfsync"];
	
	NSDictionary *storeOptions =nil;
	//  [NSDictionary dictionaryWithObject:pragmaOptions forKey:NSSQLitePragmasOption];
	if (![persistentStoreCoordinator_ addPersistentStoreWithType:NSSQLiteStoreType 
												   configuration:nil 
															 URL:storeURL 
														 options:storeOptions 
														   error:&error])
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
	NSLog (@"created new persistentStoreCoordinator %@ %@ for db %@",backtrace,persistentStoreCoordinator_,storeURL);
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


#pragma mark UIAlertViewDelegate Methods

- (void) alertView: (UIAlertView *) av clickedButtonAtIndex: (NSInteger) idx
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
#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
	/*
	 Free up as much memory as possible by purging cached data objects that can be recreated (or reloaded from disk) later.
	 */
}


- (void)dealloc {
	NSLog (@"gigstandappdelegate dealloc");
	[managedObjectContext_ release];
	[managedObjectModel_ release];
	[persistentStoreCoordinator_ release];
	[window release];
	[super dealloc];
}

-(void) dump:(NSString *) tag;
{
	NSLog (@"%@ psc %@",tag,[self persistentStoreCoordinator:@"dmp"]);
}

@end

#pragma mark Extensions to Colorize Components 

@implementation UINavigationBar (UINavigationBarCategory)
//- (void)drawRect:(CGRect)rect {
//	UIColor *color = [DataManager applicationColor];
//	
//	CGContextRef context = UIGraphicsGetCurrentContext();
//	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
//	CGContextFillRect(context, rect);
//	self.tintColor = color;
//	
//}
@end
@implementation UIToolbar (UIToolbarCategory)
//- (void)drawRect:(CGRect)rect {
//	
//	UIColor *color = [DataManager applicationColor];
//	CGContextRef context = UIGraphicsGetCurrentContext();
//	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
//	CGContextFillRect(context, rect);
//	self.tintColor = color;
//	
//}
@end

@implementation UISearchBar (UISearchBarCategory)
//- (void)drawRect:(CGRect)rect {
//	UIColor *color = [DataManager applicationColor];
//	CGContextRef context = UIGraphicsGetCurrentContext();
//	CGContextSetFillColor(context, CGColorGetComponents( [color CGColor]));
//	CGContextFillRect(context, rect);
//	self.tintColor = color;
//	self.barStyle = UIBarStyleBlack;
//	self.translucent = YES;
//}
@end

@implementation UIViewController (UIViewControllerCategory)
-(void) setColorForNavBar;
{
    self.navigationController.navigationBar.barStyle = UIBarStyleBlack ;
	self.navigationController.navigationBar.translucent = YES;
}
-(void) setColorForOneTuneNavBar;
{
    
    self.navigationController.navigationBar.barStyle = UIBarStyleBlack ;
	self.navigationController.navigationBar.translucent = NO;
    
}
-(void) popOrNot;
{
    if ((UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) && 
        ([DataManager modalPopOversEnabled ]== YES))
        
        [self.parentViewController dismissModalViewControllerAnimated:YES];
    else
        [self.navigationController popViewControllerAnimated:YES];
    
}
@end

@implementation NSString (NSStringCategory)
-(NSComparisonResult) reverseCompare : (NSString *) a;
{
    NSComparisonResult x = [self compare: a];
    if (x == NSOrderedAscending) return NSOrderedDescending; 
    else if (x== NSOrderedDescending) return NSOrderedAscending;
    else return NSOrderedSame;
}
@end

