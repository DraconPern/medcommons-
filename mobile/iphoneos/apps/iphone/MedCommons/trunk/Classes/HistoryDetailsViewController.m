//
//  Created by bill donner on 9/4/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//

#import "MedCommons.h"
#import "HistoryDetailsViewController.h"
#import "HistoryCase.h"
#import "PatientStore.h"
#import "HistoryPanningController.h"
#import "MapController.h"
#import "AsyncImageView.h"
#import "DataManager.h"

@implementation HistoryDetailsViewController
#pragma mark concrete methods
- (void)viewWillDisappear:(BOOL)animated
{
  //  [[UIApplication sharedApplication] setStatusBarStyle:oldStatusBarStyle animated:NO];    
}
// override to allow orientations other than the default portrait orientation
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{	
    return UIInterfaceOrientationIsPortrait(interfaceOrientation);
}
-(id)  initWithCase:(HistoryCase *)_hcase 
{
	if (self = [super init] )
	{	
		hcase = _hcase;
		patientStore = [[DataManager sharedInstance] ffPatientStore];
		imageCache = [[DataManager sharedInstance] ffImageCache];	

	}
	BREADCRUMBS_PUSH;

    return self;
}
-(id)  init
{
	if (self = [super init] )
	{	
		patientStore = [[DataManager sharedInstance] ffPatientStore];
		imageCache = [[DataManager sharedInstance] ffImageCache];	
		hcase = [[DataManager sharedInstance] historyCase:0 ];
	}
	//BREADCRUMBS_PUSH;

    return self;
}
- (void) dealloc
{
	//BREADCRUMBS_POP;
	[super dealloc];
}

-(void) showMap
{
	for (int j = 0; j <kHowManySlots-1; j++)
	{
		NSDictionary *adicts = [[hcase attrdicts] objectAtIndex:j]; //take first object - not quite right
		if ([adicts count] > 0) {
			MapController *mapController = [(MapController *)[MapController alloc] 
											initWithConfig:adicts];
			MY_ASSERT (mapController !=nil);
			[self.navigationController pushViewController:(UIViewController *)mapController 	animated:YES];
			[mapController release];
			return;
		}
	}
	
}

//-(void)gotoExternalURL:(id)ob
//{
//	NSString *url = [hcase pickupurl];	
//	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]]; // just jump off
//}


-(void) pushtoPanning: (NSInteger) j
{	
	HistoryPanningController *historyPanningController = [[HistoryPanningController alloc] initWithIndex: j andWithCase: (HistoryCase *)hcase
																							  ];
	MY_ASSERT(historyPanningController!=nil);
	[self.navigationController pushViewController:(UIViewController *)historyPanningController 	animated:YES];
	[historyPanningController release];
}




#pragma mark touch handling
// Checks to see which view, or views,  the point is in and then calls a method to perform the closing animation,
// which is to return the piece to its original size, as if it is being put down by the user.
-(BOOL)dispatchTouchEndEvent:(UIView *)theView toPosition:(CGPoint)position
{   
	//	NSLog (@"count of tinyPics is %d",[tinyPics count]);
	// Check to see which view, or views,  the point is in and then animate to that position.
	for (int j=0; j<[tinyPics count]-1; j++)
	{
		if (CGRectContainsPoint([[tinyPics objectAtIndex:j] frame], position))
		{
			NSMutableDictionary *adicts = [[hcase attrdicts] objectAtIndex:j];
			BOOL havepic =[@"photo" isEqualToString: [adicts objectForKey:@"media-type"]];
			if (havepic) [self pushtoPanning:j];
			return YES;			
		}
	}	
	return NO;
}
// Handles one end of a touch event.
-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
	//	if (disableTouchesSemaphore==0)
	{ 
		// Enumerates through all touch object
		for (UITouch *touch in touches) {
			// Sends to the dispatch method, which will make sure the appropriate subview is acted upon
			if ([self dispatchTouchEndEvent:[touch view] toPosition:[touch locationInView:self.view]] == YES) return;
		}
	}
}

#pragma mark standard workflow
-(void) loadView
{	// this is very similar to the main portrait controller layout, but it pulls pictures from the web
	
	
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	appFrame.origin.y = appFrame.origin.y + 44.0f;
	appFrame.size.height = appFrame.size.height - 44.0f ;	
	UIView *outerView = [[UIView alloc] initWithFrame:appFrame];  	
	
	float pics_row_1_start = -4.0f+327.0f-44;
	float pics_row_2_start = -4.0f +392.0f-44;
	
	CGRect pictureframe =  CGRectMake(4.0f+4.0f,-4.0f+56.0f, 80.0f, 80.0f);	
	CGRect logoframe =  CGRectMake(2.0f+194.0f,-4.0f+299.f-44, 118.1f, 24.0f);
	
	//CGRect pickupFrame = CGRectMake(4,162,320,30);
	CGRect nameFrame  = CGRectMake(100,44,200,30);
	CGRect timestampFrame = CGRectMake(100,84,200,20);	
	
	CGRect sha1Frame = CGRectMake(100,110,200,20);
	
	
	//CGRect pinFrame = CGRectMake(90,96,230,20);
	
	
	CGRect mapButtonFrame = CGRectMake(10,190,80,50);	
//	CGRect shareButtonFrame = CGRectMake(120,190,80,50);	
	//CGRect servicesButtonFrame = CGRectMake(230,190,80,50);
	
	//CGRect mapFrame = CGRectMake(270,190,100,50);
	
	outerView.backgroundColor = [UIColor blackColor];
    //oldStatusBarStyle = [[UIApplication sharedApplication] statusBarStyle];
    //[[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleBlackTranslucent animated:NO];	
	
	UIImageView *logoView = [[UIImageView alloc] initWithFrame:logoframe];
	logoView.image = [UIImage imageNamed:BLACK_LOGO_IMAGE];
	[outerView addSubview: logoView];
	[logoView release];
	
	tinyPics  = [[NSMutableArray alloc] init];
	
	AsyncImageView *oneImageView = [[AsyncImageView alloc] initWithFrame:pictureframe andImageCache:imageCache
									];
	
	oneImageView.tag = 992;
	
	NSArray *arr = [hcase attrdicts];
	NSURL *url = nil;
	NSDictionary *dict = [arr objectAtIndex:0];// slot zero always for the patient
	
	NSString *path = [dict objectForKey:@"remoteurl"];
	//NSLog (@"remoteurl is %@",path);
	if (!path)	
		oneImageView.image = [UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];	else
		{			
			
			NSURL *url = [NSURL URLWithString:path];
			if (url)
				[oneImageView loadImageFromURL:url];
		}
	
	[tinyPics addObject: oneImageView]; // remember this, we need it to show the uploads
	[outerView addSubview: oneImageView ];
	[oneImageView  release];
	
	///
	for (int j=0;j<5;j++)
	{
		int xpos = 4.0f+j*63.0f;
		CGRect tf = CGRectMake(xpos, pics_row_1_start, 60.0f, 60.0f);
		oneImageView = [[AsyncImageView alloc] initWithFrame:tf andImageCache:imageCache
						];
		[tinyPics addObject: oneImageView]; // remember this, we need it to show the uploads
		[outerView addSubview: oneImageView ];
		[oneImageView  release];
	}
	for (int j=5;j<10;j++)
	{
		int xpos = 4.0f+(j-5)*63.0f;
		CGRect tf = CGRectMake(xpos, pics_row_2_start, 60.0f, 60.0f);
		oneImageView = [[AsyncImageView alloc] initWithFrame:tf andImageCache:imageCache
						];
		[tinyPics addObject: oneImageView]; // remember this, we need it to show the uploads
		[outerView addSubview: oneImageView ];
		[oneImageView  release];
	}
	
	
	for (int i=1; i<=10; i++)
	{
		; // get the next possible file
		AsyncImageView* oneImageView = [tinyPics objectAtIndex:i]; //compensate for photo pic
		url = nil;
		NSDictionary *dict = [[hcase attrdicts] objectAtIndex:i];// slot zero always for the patient
		if(dict) 
		{
			NSString *ss = [dict objectForKey:@"remoteurl"];
			//NSLog (@"Slot %d ss is %@",i,ss);
			if (!ss)
				oneImageView.image = nil;//[UIImage imageNamed:PLEASE_SHOOT_PATIENT_IMAGE];	
			else
			{
				NSURL *url = [NSURL URLWithString:ss];
				if (url)
					[oneImageView loadImageFromURL:url];
			}
		}	
	}
	
	tLabel = [[UILabel alloc] initWithFrame:timestampFrame];
	tLabel.textColor = [UIColor whiteColor];
	tLabel.textAlignment = UITextAlignmentLeft;
	tLabel.font = [UIFont fontWithName:@"Arial" size:12];
	tLabel.backgroundColor = [UIColor blackColor];
	tLabel.text =[NSString stringWithFormat:@"stored %@", 
				  [NSDate dateWithTimeIntervalSince1970:[[hcase timestamp] doubleValue]]];
	[outerView addSubview:tLabel];
	[tLabel release];
	
	aLabel = [[UILabel alloc] initWithFrame:sha1Frame];
	aLabel.textColor = [UIColor whiteColor];
	aLabel.textAlignment = UITextAlignmentLeft;
	aLabel.font = [UIFont fontWithName:@"Arial" size:12];
	aLabel.backgroundColor = [UIColor blackColor];
	aLabel.text =[NSString stringWithFormat:@"sha1 %@", 
				  [hcase sha1]];
	[outerView addSubview:aLabel];
	[aLabel release]; 
 
	nLabel = [[UILabel alloc] initWithFrame:nameFrame];
	nLabel.textColor = [UIColor whiteColor];
	nLabel.textAlignment = UITextAlignmentLeft;
	nLabel.font = [UIFont fontWithName:@"Arial" size:20];
	nLabel.backgroundColor = [UIColor blackColor];
	nLabel.text =[NSString stringWithFormat:@"%@", 
				  [hcase name] ];
	[outerView addSubview:nLabel];
	[nLabel release]; 	
#if defined (ENABLE_BIGAPP_PLACES)
	UIButton *cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"Services" forState:UIControlStateNormal];
	[cButton setFrame:servicesButtonFrame];
	[cButton addTarget:self action:@selector(showServicesPickList) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];
#endif	
	
	aButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
	[aButton setFrame:mapButtonFrame];
	[aButton addTarget:self action:@selector(showMap) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:aButton];
	
	if ([hcase timestamp]!=nil)
	{
		[aButton setTitle:@"Map" forState:UIControlStateNormal];
	
	}
	else
	{
		[aButton setTitle:@"First User - No Saved Vouchers" forState:UIControlStateNormal];		
		aButton.enabled = NO;
	}
	
	self.view = outerView;
	self.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;
	self.view.backgroundColor = [UIColor blackColor];
	
	self.navigationItem.title =@"History";	
	self.navigationItem.backBarButtonItem = [[[UIBarButtonItem alloc] initWithTitle:@"Back"
														style:UIBarButtonItemStylePlain
																						target:nil action:nil] autorelease];
	//NSLog (@"loadview tinypics is %d",[tinyPics count]);
	//[aButton release]; [bButton release]; [cButton release];
	[outerView release];	
	
}
@end




