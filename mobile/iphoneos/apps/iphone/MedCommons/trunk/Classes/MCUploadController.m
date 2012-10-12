//
//  MainInfoController.m
//  ForensicFoto
//
//  Created by bill donner on 9/9/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//


#import "MCUploadController.h"
#import "MedCommons.h"
#import "DashboardPatient.h"
#import "PatientStore.h"
#import "CustomViews.h"
#import "RESTComms.h"
#import "DataManager.h"

@implementation MCUploadController


-(void) showActionSheet
{
	
	[actionSheet showInView: self.view];
}
// This callback shows progress via setProgress:
-(void)incrementProgressBar:(float)_progress
{
	progresspct = _progress;
}
- (void) incrementProgressBarint: (id) timer
{
	[progressView setProgress: progresspct];
	
}

// Load the progress bar onto an actionsheet backing
-(void) displayActionSheet: (UIBarButtonItem *) item
{
	progresspct= 0.0f;
    uploadActionSheet = [[[UIActionSheet alloc] initWithTitle:@"Uploading to MedCommons. Please Wait\n\n\n" delegate:nil 
											cancelButtonTitle:nil destructiveButtonTitle: nil otherButtonTitles: nil] autorelease];
	progressView = [[UIProgressView alloc] initWithFrame:CGRectMake(0.0f, 40.0f, 220.0f, 90.0f)];
    [progressView setProgressViewStyle: UIProgressViewStyleDefault];
    [uploadActionSheet addSubview:progressView];
	
    // Keep a timer going throughout the whole upload
	ticktimer = [NSTimer scheduledTimerWithTimeInterval: 0.1f target: self selector:@selector(incrementProgressBarint:) userInfo: nil repeats: YES];
    [uploadActionSheet showInView:self.view];
	progressView.center = CGPointMake(uploadActionSheet.center.x, progressView.center.y);	
	[progressView setProgress:(0.0f)];
	[progressView release];
	//NSLog (@"actionsheet retain count %d",[actionSheet retainCount]);
}

#pragma mark	High Level Upload Workflow Runs As A Separate Thread

- (void) doUpload:(id)timer
{
	NSTimeInterval today =  [[NSDate date] timeIntervalSince1970];	
	
	NSString *unique = [[UIDevice currentDevice] uniqueIdentifier]; //get the iphones unique identifier
    [[DataManager sharedInstance].ffRESTComms doPosts:[customViews customMainViewUploadMetaString:(NSTimeInterval ) today]
					withId: unique 
			  andTimeStamp:today 
		   andGeneralAttrs: generalAttrs
	   andMasterController: self];
	
	
	UIApplication *myapp = [UIApplication sharedApplication];
	myapp.networkActivityIndicatorVisible = NO;
	[ticktimer invalidate];
	[uploadActionSheet dismissWithClickedButtonIndex:0 animated:YES];
	//[uploadActionSheet release]; 
	uploadActionSheet = nil;	
	isUploading = NO;
	//[self reset];
	
	alert_state = 4444;
	//actionSheet= [[UIActionSheet alloc] initWithTitle:@"Your Upload was Successful"
	//										 delegate:self cancelButtonTitle:  @"OK" destructiveButtonTitle:nil
	//								otherButtonTitles:nil];
	//[self showActionSheet];
	
	[cButton removeFromSuperview];
	[topSplash removeFromSuperview];
		[middleSplash removeFromSuperview];

	
	CGRect successViewFrame =  CGRectMake(13.0f,  160.0f, 295.0f, 40.0f);
	UIImageView *successView = [[UIImageView alloc] initWithFrame: successViewFrame];
	successView.image = [UIImage imageNamed:successImage];
	
	[outerView addSubview:successView]; 
	[successView release];
	
    self.navigationItem.title = @"Upload Complete";	
	// wipe out the soap entries
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
 [prefs setValue:@"" forKey:@"blogEntryA"];	
 [prefs setValue:@"" forKey:@"blogEntryP"];
 [prefs setValue:@"" forKey:@"blogEntryS"];
 [prefs setValue:@"" forKey:@"blogEntryD"];
 [prefs setValue:@"" forKey:@"blogEntryC"];
	//
	[[[DataManager sharedInstance] ffPatientStore] cleanup];
}

- (void)backgroundUploadThread
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    [self doUpload: nil];
	[NSThread  exit];
    [pool release];
}
- (void) upload : (id) any
{
	//NSDate *today = [NSDate date];
	// drop back a bit to allow the activity indicator to manifest itself
	if (isUploading == NO) { // only start once
		isUploading = YES;
		
		self.navigationItem.title = @"Upload in Progress";	
		UIApplication *myapp = [UIApplication sharedApplication];
		myapp.networkActivityIndicatorVisible = YES;
		[self displayActionSheet:nil]; // put up the action sheed and starts timer going		
		[NSThread detachNewThreadSelector:@selector(backgroundUploadThread) toTarget:self withObject:nil]; //background upload thread
		
		//[self.navigationController popViewControllerAnimated:YES]; // and unwind
		
	}
}

-(MCUploadController *) initWithTitle:(NSString *)_titl
							andWithTop:(NSString *)_top
						 andWithMiddle:(NSString *)_middle
						andWithSuccess:(NSString *)_success
						andWithFailure:(NSString *)_failure

{
	self = [super init];
	patientStore = [[DataManager sharedInstance] ffPatientStore];
	customViews = [[DataManager sharedInstance] ffCustomViews];
	ffInfoMsgTop = _top;
	ffInfoMsgMiddle = _middle;
	ffInfoTitle = _titl;
	successImage = [_success retain];
	failureImage = [_failure retain];
//	BREADCRUMBS_PUSH;  cant - too much context and who really cares?

	return self;
}


- (void)dealloc
{
	//BREADCRUMBS_POP;
    [super dealloc];
}


// override to allow orientations other than the default portrait orientation
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait); // support only portrait
}

#pragma mark setup MainInfoController layout

- (void)makeInfoWindow	
{
	
	CGRect appTitleFrame = CGRectMake(75.0f,48,240.0f,20);	
	
	CGRect applogoFrame = CGRectMake(4.0f,47,57,57);
	CGRect topSplashFrame =  CGRectMake(13.0f,  96.0f, 295.0f, 60.0f);
	
	CGRect middleSplashFrame =  CGRectMake(13.0f,  160.0f, 295.0f, 40.0f);
	
	CGRect buttonframe =  CGRectMake(103.0f,  264.0f, 115.0f, 70.0f);
	
	CGRect identityBlurbFrame1 =  CGRectMake(13.0f,  384.0f, 295.0f, 20.0f);
	
	CGRect identityBlurbFrame2 =  CGRectMake(13.0f,  400.0f, 295.0f, 20.0f);
	CGRect networkStatusFrame =  CGRectMake(13.0f,  420.0f, 295.0f, 20.0f);
	
	CGRect appVersionFrame = CGRectMake(13.0f,440,240.0f,20.0f);

	UILabel *networkStatus = [[UILabel alloc] initWithFrame:networkStatusFrame];
	networkStatus.textColor = [UIColor blackColor];
	networkStatus.textAlignment = UITextAlignmentLeft;
	networkStatus.font = [UIFont fontWithName:@"Arial" size:14];
	networkStatus.backgroundColor = [UIColor whiteColor];	
	networkStatus.numberOfLines = 1;	 // use whatever is necessary
	networkStatus.text = [NSString stringWithFormat:@"Network services: %@",
						  [[DataManager sharedInstance] ffHostReachStatus]];//ffHostReachStatus
	
	[outerView addSubview:networkStatus];
	[networkStatus release];
	
	
	topSplash = [[UILabel alloc] initWithFrame:topSplashFrame];
	topSplash.textColor = [UIColor blackColor];
    topSplash.textAlignment = UITextAlignmentLeft;
	topSplash.font = [UIFont fontWithName:@"Arial" size:14];
	topSplash.backgroundColor = [UIColor whiteColor];	
	topSplash.numberOfLines = 3;	 // use whatever is necessary
	topSplash.text = ffInfoMsgTop; 
	
	
	[outerView addSubview:topSplash];
	[topSplash release];
	
	
	middleSplash = [[UILabel alloc] initWithFrame:middleSplashFrame];
	middleSplash.textColor = [UIColor blackColor];
	middleSplash.textAlignment = UITextAlignmentLeft;
	middleSplash.font = [UIFont fontWithName:@"Arial" size:14];
    middleSplash.backgroundColor = [UIColor whiteColor];	
    middleSplash.numberOfLines = 0;	 // use whatever is necessary
	middleSplash.text = ffInfoMsgMiddle; 
	[outerView addSubview:middleSplash];
	[middleSplash release];
	
	//
	UILabel *identityBlurb1 = [[UILabel alloc] initWithFrame:identityBlurbFrame1];
	identityBlurb1.textColor = [UIColor blackColor];
	identityBlurb1.textAlignment = UITextAlignmentLeft;
	identityBlurb1.font = [UIFont fontWithName:@"Arial" size:14];
	identityBlurb1.backgroundColor = [UIColor whiteColor];	
	identityBlurb1.text = [[DataManager sharedInstance] ffMCusername] ; //@"www.medcommons.net/BigAppleReporter" ;//[ 	objectForKey:@"ffSummaryReachStatus"];	
	[outerView addSubview:identityBlurb1];
	[identityBlurb1 release];
	
	UILabel *identityBlurb2 = [[UILabel alloc] initWithFrame:identityBlurbFrame2];
	identityBlurb2.textColor = [UIColor blackColor];
	identityBlurb2.textAlignment = UITextAlignmentLeft;
	identityBlurb2.font = [UIFont fontWithName:@"Arial" size:14];
	identityBlurb2.backgroundColor = [UIColor whiteColor];	
	identityBlurb2.text = [[DataManager sharedInstance] ffMCappliance] ;//@"www.medcommons.net/BigAppleReporter" ;//[ 	objectForKey:@"ffSummaryReachStatus"];	
	[outerView addSubview:identityBlurb2];
	[identityBlurb2 release];
	
	UILabel *appTitle = [[UILabel alloc] initWithFrame:appTitleFrame];
	appTitle.textColor = [UIColor blackColor];
	appTitle.textAlignment = UITextAlignmentLeft;
	appTitle.font = [UIFont fontWithName:@"Arial" size:18];
	appTitle.backgroundColor = [UIColor whiteColor];	
	appTitle.text = ffInfoTitle; 
	
	[outerView addSubview:appTitle];
	[appTitle release];
	
	UILabel *appVersion = [[UILabel alloc] initWithFrame:appVersionFrame];
	appVersion.textColor = [UIColor blackColor];
	appVersion.textAlignment = UITextAlignmentLeft;
	appVersion.font = [UIFont fontWithName:@"Arial" size:14];
	appVersion.backgroundColor = [UIColor whiteColor];	
	appVersion.text =[NSString stringWithFormat:@"Version %@%@" , [[DataManager sharedInstance] ffVersion],@"MC",nil];
	[outerView addSubview:appVersion];
	[appVersion release];
	
	
	
	UIImageView *applogoView = [[UIImageView alloc] initWithFrame:applogoFrame];
	applogoView.image = [UIImage imageNamed:[[DataManager sharedInstance] ffAppLogoImage]];
	[outerView addSubview: applogoView];
	[applogoView release];
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"UPLOAD" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(upload:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];
	
	
	
}

- (void)loadView
{
	
	//THIS HAPPENS LAST, BOOT UP THE FRAME
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor whiteColor];  
	outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	self.view = outerView;  	
	
    [self makeInfoWindow]; // add the info window in the middle for now
	[outerView release];
	
    self.navigationItem.title = @"Confirm Upload";	
}
@end