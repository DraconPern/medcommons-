//
//  DataManager.m
//  MedCommons
//
//  Created by bill donner on 1/29/10.

//
#import "MedCommons.h"
#import "DataManager.h"
#import "HistoryCase.h"
#import	"PatientStore.h"

@implementation DataManager

static DataManager *_sharedInstance;

- (id) init
{
	if (self = [super init])
	{
		// custom initialization
		//memset(board, 0, sizeof(board));
	}
	
	return self;
	
}


+ (DataManager *) sharedInstance
{
	if (!_sharedInstance)
	{
		_sharedInstance = [[DataManager alloc] init];
	}
	
	return _sharedInstance;
}
-(void) setSelfMode:(BOOL) _sm
{
	inSelfMode = _sm;
}
-(BOOL) selfMode
{
	return inSelfMode;
}

-(BOOL) tryRecovery:(UIViewController *)who
{
	// pop off a breadcrumb, if we have one then go to that class
	NSString *s = [ffBreadCrumbs popRecoveryCrumb];
	if (s)
	{
		UIViewController *viewc = [[NSClassFromString(s) alloc] init];
		[who.navigationController pushViewController:viewc 	animated:NO];
		[viewc release];
		return YES;
	}
	return NO;
}
-(void) setSelectedPatientIndex:(NSString *) row
{
	selectedPatientIdx = row;
}
-(NSString *)selectedPatientIndex
{
	return selectedPatientIdx;
}

#pragma mark history  
-(HistoryCase *) historyCase:(int)casenum
{
	return [[self.ffHistory objectAtIndex:casenum] retain];
}
-(int) historyCount
{
	return [self.ffHistory count];
}
- (void) saveToHistory:(NSDictionary *) dict  withGeneralAttrs: (NSDictionary *) attrs  andWithPhotoAttributes :(NSMutableArray *) attrdicts
{
	// store the returned JSON dict  along with photo Metadata dict
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	NSString *name = [[NSString stringWithFormat:@"%@ %@", [prefs objectForKey:@"firstname"], [prefs objectForKey:@"lastname"]] retain];
	
	NSMutableDictionary *mut = [NSMutableDictionary dictionaryWithDictionary:dict];
	
	
	[mut addEntriesFromDictionary:attrs]; //merge and pass
	
	HistoryCase *hcase = [[HistoryCase alloc] initWithName: name 		andSlot:[NSString stringWithFormat:@"%d", [self.ffHistory count]	]							
								  andWithGeneralAttributes: mut
								   andWithPhotoAttributes :(NSMutableArray *) attrdicts ] ;
	[self.ffHistory insertObject: hcase    atIndex: 0];
	[hcase release];
	[NSKeyedArchiver archiveRootObject:self.ffHistory  toFile:HISTORYPATH]; // write entire tree in one go
}
	
@synthesize  ffAppKey;
@synthesize  ffVersion;
@synthesize  ffClassPrefix;
@synthesize  ffAppServicesPath;
@synthesize  ffOuterController;
@synthesize  ffPatientStore;
@synthesize  ffImageCache;
@synthesize  ffRESTComms;
@synthesize  ffCustomViews;
@synthesize  ffAppLogoImage;
@synthesize  ffAppBitsPath;
@synthesize  ffAppMetaPath;
@synthesize  ffAppDataStore;
@synthesize  ffHostReachStatus;
@synthesize  ffInternetReachStatus;
@synthesize  ffWifiReachStatus;
@synthesize  ffSummaryReachStatus;
@synthesize  ffHostReachImage;
@synthesize  ffInternetReachImage;
@synthesize  ffWifiReachImage;
@synthesize ffSummaryReachImage;
@synthesize ffMCusername;
@synthesize ffMCpassword;
@synthesize ffMCauth;
@synthesize ffMCpracticename;
@synthesize ffMCpatientlist;
@synthesize ffMCappliance;
@synthesize ffMCpracticecursor;
@synthesize ffMCprovidermcid;
@synthesize	ffMCmcid;
@synthesize ffMainWindow;
@synthesize ffSharedOperationQueue;
@synthesize ffGPSDevice;
@synthesize ffBreadCrumbs;
@synthesize ffPatientWrapper;
@synthesize ffNextFileIndex;
@synthesize ffHistory;
@end


@implementation AsciiInt
-(void) initWithStringVal:(NSString *) s andWithTag:(NSString *) t
{
	self = [super init];
	myint = [s integerValue];
	mytag = t;
	NSString *old = [[NSUserDefaults standardUserDefaults] valueForKey:t];
	if (old ) myint = [old integerValue];
	//NSLog (@"reloaded with %d",myint);
	[[NSUserDefaults standardUserDefaults] setValue:s forKey:t];//remember this
}
-(NSString *) bump
{
	myint++;
	//NSLog (@"bumped to %d",myint);
	NSString *s = [NSString stringWithFormat:@"%d",myint];
	
	[[NSUserDefaults standardUserDefaults] setValue:s forKey:mytag];//remember this
	return s;
}
@end

@implementation BreadCrumbs
-(BreadCrumbs *) init
{
	self = [super init];
	
	inrecoverycrumbs =[NSMutableArray arrayWithArray:[[NSUserDefaults standardUserDefaults] objectForKey:@"breadcrumbs"] ]; // this drives the restart process
	[[NSUserDefaults standardUserDefaults] setObject:nil forKey:@"breadcrumbs"]; // start clean
	crumbs = [[NSMutableArray alloc] initWithCapacity:10];
	[inrecoverycrumbs retain];
	[crumbs retain];
	patientIndex = @"-1";
	BREADCRUMBS_LOG (@"initially>> crumbs %@ recover %@",crumbs,inrecoverycrumbs);
	return self;
}
- (void) dealloc
{
	//[crumbs release];
	[super dealloc];
}

-(NSString *)popRecoveryCrumb
{
	// this will be called from each view controller to get the name of a controller to run
	if ([inrecoverycrumbs count]<1) return nil;

	// get first controller off the inrecovery crumbs trail
	// rewrite the crumbs trail 

		id ob = [inrecoverycrumbs objectAtIndex:0]; //last
		[inrecoverycrumbs removeObjectAtIndex:0];
		BREADCRUMBS_LOG (@"poprecoverycrumb>>> %@ patient %@ crumbs %@ recover %@",[ob description], [[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"],
			   crumbs,inrecoverycrumbs);
		return [ob description];
	
}

-(id) pop
{

	id ob = [crumbs objectAtIndex:[crumbs count]-1]; //last
	[crumbs removeLastObject];


	BREADCRUMBS_LOG (@"popped>>> %@ patient %@ crumbs %@ recover %@",[ob description], [[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"],
		   crumbs,inrecoverycrumbs);

	[[NSUserDefaults standardUserDefaults] setObject:crumbs forKey:@"breadcrumbs"];
	return ob;
}

-(void) push:(NSString *)s
{

	[crumbs addObject:s];
	
	BREADCRUMBS_LOG (@"pushed>>> %@ patient %@ crumbs %@ recover %@",[s description],[[NSUserDefaults standardUserDefaults] valueForKey:@"patientcrumbs"],crumbs,inrecoverycrumbs);

	
	[[NSUserDefaults standardUserDefaults] setObject :crumbs forKey:@"breadcrumbs"];
}
@end