//
//  MedCommonsAppDelegate.m
//  ForensicFoto
//
//  Created by bill donner on 9/4/09.
//  Copyright MEDCOMMONS, INC. 2009. All rights reserved.
//
#import "MedCommons.h"
#import "MedCommonsAppDelegate.h"
#import "PatientStore.h"
#import "RESTComms.h"
#import "Reachability.h"
#import "DashboardMainController.h"
#import "MCCustom+View.h"
#import "DataManager.h"
#import "GPSDevice.h"

@implementation MedCommonsAppDelegate
#pragma mark NetworkStatus
- (void) statusToDictionary: (NSString*) dicttextkey imageView: (NSString*)dictimagekey reachability: (Reachability*) curReach
{
	
    NetworkStatus netStatus = [curReach currentReachabilityStatus];
    BOOL connectionRequired= [curReach connectionRequired];
    NSString* statusString= @"";
	NSString* image =@"";
    switch (netStatus)
    {
        case NotReachable:
        {
            statusString = @"Access Not Available";
            image =  @"stop-32.png" ;
            //Minor interface detail- connectionRequired may return yes, even when the host is unreachable.  We cover that up here...
            connectionRequired= NO;  
            break;
        }
            
        case ReachableViaWWAN:
        {
            statusString = @"Reachable via WWAN";
            image =  @"WWAN5.png";
            break;
        }
        case ReachableViaWiFi:
        {
			statusString= @"Reachable via WiFi";
			image = @"Airport.png";
            break;
		}
    }
    if(connectionRequired)
    {
        statusString= [NSString stringWithFormat: @"%@, Connection Required", statusString];
    }
	
	//[dict setObject:statusString  forKey:dicttextkey];  
	//[dict setObject:image  forKey:dictimagekey];  
	
	APD_LOG (@"Network Status Change -- %@ to %@ image %@",statusString,dicttextkey,dictimagekey);
}

- (void) updateInterfaceWithReachability: (Reachability*) curReach 
{
    if(curReach == hostReach)
	{
		[self statusToDictionary: @"ffHostReachStatus" imageView:@"ffHostReachImage" reachability: curReach];

        NetworkStatus netStatus = [curReach currentReachabilityStatus];
        BOOL connectionRequired= [curReach connectionRequired];
		NSString* baseLabel=  @"";
        if (netStatus == ReachableViaWWAN) {
			
			
			if(connectionRequired)
			{
				baseLabel=  @"Cellular data network is not yet connected.";
			}
			else
			{
				baseLabel=  @"Cellular data network is active.";
			}
			//summaryLabel.text= baseLabel;
			APD_LOG (@"Network Reach Summary -- %@",baseLabel);
			//[dict setObject:baseLabel forKey:@"ffSummaryReachStatus"];  //stash
			[DataManager sharedInstance].ffSummaryReachStatus = baseLabel;
		}
    }
	if(curReach == internetReach)
	{	
		[self statusToDictionary: @"ffInternetReachStatus" imageView: @"ffInternetReachImage" reachability: curReach];
	}
	if(curReach == wifiReach)
	{	
		[self statusToDictionary: @"ffWifiReachStatus" imageView: @"ffWifiReachImage" reachability: curReach];
	}
}

//Called by Reachability whenever status changes.
- (void) reachabilityChanged: (NSNotification* )note
{
	Reachability* curReach = [note object];
	NSParameterAssert([curReach isKindOfClass: [Reachability class]]);
	[self updateInterfaceWithReachability: curReach];
	
}

#pragma mark Remote Services Call 



#pragma mark restore history file or recreated
- (NSMutableArray *) readHistoryFromFile:(NSString *) filepath
{
	if ([[NSFileManager defaultManager] fileExistsAtPath:HISTORYPATH])
		return [[NSKeyedUnarchiver unarchiveObjectWithFile:HISTORYPATH] retain]; else
			return [[[NSMutableArray alloc] init ] retain];
}
-(void) alertView: (UIAlertView *) alertView clickedButtonAtIndex:(NSInteger) buttonIndex
{
	MY_ASSERT (1==0) ; // shud never get here
}

#pragma mark setup controllers based on settings bundle
-(UIViewController *) setupController
{
	DataManager *mcs = 	[DataManager sharedInstance];
	
	NSUserDefaults *uprefs = [NSUserDefaults standardUserDefaults] 	;
	// figure out our personality if any
	
	NSDictionary *environment =	   [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"LSEnvironment"];
	NSString *appkey = [environment objectForKey:@"appkey"];
	//NSString *fixedcontent = [environment objectForKey:@"fixedcontent"];
	NSString *uberservices = [environment objectForKey:@"servicesurl"];
	// if an appkey was defined then 
	
	mcs.ffOuterController = [[DashboardMainController alloc] init];
	mcs.ffRESTComms	= [[RESTComms alloc] init];

	mcs.ffCustomViews	= [[MCCustomViews alloc] init];
	mcs.ffImageCache = [[NSMutableDictionary alloc] init];
	mcs.ffAppServicesPath = uberservices;
	mcs.ffAppKey = appkey;
	mcs.ffVersion = APPLICATION_VERSION;
	mcs.ffClassPrefix = @"FF";
	mcs.ffAppLogoImage = @"icon_cross2.png";
	mcs.ffAppBitsPath = [environment objectForKey:@"bitsurl"];
	mcs.ffAppMetaPath = [environment objectForKey:@"metaurl"];
	mcs.ffAppDataStore =[environment objectForKey:@"mids"];
	
	
	;
	if (([uprefs boolForKey:@"reset"]) && (NO == [uprefs boolForKey:@"reset"]))
	{
		mcs.ffMCpassword =[uprefs objectForKey:@"password"];
		 mcs.ffMCusername =[uprefs objectForKey:@"username"];	 
		  mcs.ffMCappliance =[uprefs objectForKey:@"appliance"];
	}
	else
		
	{
		mcs.ffMCappliance=@"healthurl.medcommons.net";
		 mcs.ffMCusername =@"sigmundfranklin@medcommons.net";	 
		  mcs.ffMCpassword =@"tester";
	}
	
	
	APD_LOG (@">>restart %@ %@ %@ %@",mcs.ffMCappliance, mcs.ffMCusername,  
			 [uprefs valueForKey:@"breadcrumbs"] ,
			 [uprefs valueForKey:@"patientcrumbs"]);	

	APD_LOG (@"App %@  version %@ is calling remote web services %@ with appkey %@",				 
			 [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleName"],
			 [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"CFBundleVersion"],
			 uberservices,
			 appkey);
	
	NSOperationQueue * theQueue = [[NSOperationQueue alloc] init];
	mcs.ffSharedOperationQueue = theQueue; // set this up for background processing
	[theQueue release];
	
	GPSDevice *device =[[GPSDevice alloc] init]; // make a GPS thing, but the camera controller will turn it on and off to save power
	mcs.ffGPSDevice = device;
	[device release];
	
	BreadCrumbs *bc =[[BreadCrumbs alloc] init];
	mcs.ffBreadCrumbs = bc;
	[bc release];
	AsciiInt *ac = [[AsciiInt alloc] init];
	mcs.ffNextFileIndex = ac;
	[ac release];
	[[mcs ffNextFileIndex] initWithStringVal:(NSString *) @"99900000"  andWithTag:(NSString *)@"NFIDX" ]; // should be restored thru prefs
 // no one set yet
	[mcs setSelectedPatientIndex:	[uprefs valueForKey:@"patientcrumbs"]];
	
	mcs.ffHistory = [[NSMutableArray alloc] init];
	
  	return [mcs ffOuterController]; // dont put this 
}


- (void)applicationDidFinishLaunching:(UIApplication *)application {	
	
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleBlackTranslucent animated:NO];
	
	UIViewController  *v = [self setupController];
	
	// Observe the kNetworkReachabilityChangedNotification. When that notification is posted, the
	// method "reachabilityChanged" will be called. 
	[[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(reachabilityChanged:) name: kReachabilityChangedNotification object: nil];

		DataManager *mcs = 	[DataManager sharedInstance];
	
	
	mcs.ffHostReachImage = @"still-initializing";
		mcs.ffInternetReachStatus = @"still-initializing";
		mcs.ffWifiReachStatus = @"still-initializing";
		mcs.ffSummaryReachStatus = @"still-initializing";
		mcs.ffInternetReachImage = @"still-initializing";
		mcs.ffWifiReachImage = @"still-initializing";
		mcs.ffHostReachImage = @"still-initializing";
		mcs.ffSummaryReachImage = @"still-initializing";
	
	
	
	//close but no
	
	//Change the host name here to change the server your monitoring
	remoteHostLabel.text = [NSString stringWithFormat: @"Remote Host: %@", @"www.apple.com"];
	hostReach = [[Reachability reachabilityWithHostName: @"www.apple.com"] retain];
	[hostReach startNotifer];
	[self updateInterfaceWithReachability: hostReach ];
	
	internetReach = [[Reachability reachabilityForInternetConnection] retain];
	[internetReach startNotifer];
	[self updateInterfaceWithReachability: internetReach ];
	
	wifiReach = [[Reachability reachabilityForLocalWiFi] retain];
	[wifiReach startNotifer];
	[self updateInterfaceWithReachability: wifiReach ];
	
	
	// Set up the window and make it aviable 
	window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];	
	
	mcs.ffMainWindow = window;
	// navigation controller
	UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController: v ];
	[window addSubview:[nav view]];
	[window makeKeyAndVisible];
	
}

-(BOOL)parseIncomingURL
{	
	//************ Parse URL that started us
	BOOL wasStartedByMCU = NO;
	
	NSUserDefaults *uprefs = [NSUserDefaults standardUserDefaults] 	;
	NSString *querystring = [uprefs stringForKey:@"querystring"];
	[uprefs setObject:nil forKey:@"querystring"];	// once we've captured this throw it away
	
	NSArray *queryComponents = [querystring componentsSeparatedByString:@"&"];
	NSString *queryComponent;
	for (queryComponent in queryComponents){
		NSArray *query = [queryComponent componentsSeparatedByString:@"="];
		if ([query count]==2)
		{
			NSString *key =[query objectAtIndex:0];
			NSString *value = [query objectAtIndex:1];			
			wasStartedByMCU = YES;			
		[uprefs setObject: value forKey: key ];				
		}
	}
	return wasStartedByMCU;	
}


- (BOOL)application: (UIApplication *)application handleOpenURL:(NSURL *)url
{   
	
	if (!url) {
		// The URL is nil. There's nothing more to do.
		return NO;
	}
	
	NSString *URLString = [url absoluteString];
	
	if (!URLString) {
		// The URL's absoluteString is nil. There's nothing more to do.
		return NO;
	}
	
	// Your application is defining the new URL type, so you should know the maximum character
	// count of the URL. Anything longer than what you expect is likely to be dangerous.
	NSInteger maximumExpectedLength = 250;
	
	if ([URLString length] > maximumExpectedLength) {
		// The URL is longer than we expect. Stop servicing it.
		return NO;
	}
	//
	// shove this in preferences so we can pick this up in loadview, or wherever we need this
	//
	NSUserDefaults *uprefs = [NSUserDefaults standardUserDefaults];
	//write out prefs in case we get quit out
	
	APD_LOG(@"Querystring %@ prefstring %@", [url query], [url path]);
	// saving an NSString
	[uprefs setObject:[url query] forKey:@"querystring"];	
	[uprefs setObject:[url path] forKey:@"path"];
	
	[self parseIncomingURL];
	return YES;
}
@end