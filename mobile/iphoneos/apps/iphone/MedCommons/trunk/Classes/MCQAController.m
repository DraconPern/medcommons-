//
//  MCQAController.m
//  MedCommons
//
//  Created by bill donner on 1/17/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MCQAController.h"
#import "MCSharingController.h"
#import "DataManager.h"
#import "MedCommons.h"
#import "DashboardPatient.h"
#import "PatientStore.h"

@implementation MCQAController


-(void) showActionSheet
{
	
	//[actionSheet showFromTabBar:(UITabBar *) self.view];
}
-(void) cancel  : (NSObject *)  id
{
	[self.navigationController popViewControllerAnimated:YES];

	
	//alert_state = 21;
//	
//	
//	
//	actionSheet = [[UIActionSheet alloc] initWithTitle:@"You Will Cancel and Return Back to the Invoking Page" 
//											  delegate:self cancelButtonTitle: @"OK"
//								destructiveButtonTitle: nil
//									 otherButtonTitles: nil];
//	[self showActionSheet];
//	
}
-(void) details : (NSObject *)  id
{
//	alert_state = 22;
//	
//	
//	
//	actionSheet = [[UIActionSheet alloc] initWithTitle:@"A More Detailed Page Might Be Prexented Here" 
//											  delegate:self cancelButtonTitle: @"OK"
//								destructiveButtonTitle: nil
//									 otherButtonTitles: nil];
//	[self showActionSheet];
	
}
-(void) pass : (NSObject *) id
{	
	outerView.backgroundColor = [UIColor colorWithRed:0 green:.76f blue:0 alpha:0.5]; 

		NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
		
			[prefs setObject:@"QA Green" forKey:@"QAState"]; 

		[[[DataManager sharedInstance] ffPatientStore] writePatientStore]; // take snapshot <+++++++++++++++
	
	qastate = @"QA Green";

	
}
-(void) fail :(NSObject *)  id
{

	
	outerView.backgroundColor = [UIColor colorWithRed:.76f green:0 blue:0 alpha:0.5]; 
	
	NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
	[prefs setObject:@"QA Red" forKey:@"QAState"]; 
	
	[[[DataManager sharedInstance] ffPatientStore] writePatientStore]; // take snapshot <+++++++++++++++
	qastate = @"QA Red";
	
}




-(MCQAController *) init
{
	self = [super init];
	
	
	wrapper = [[DataManager sharedInstance] ffPatientWrapper];	
	
   NSMutableDictionary *prefs = [[DataManager sharedInstance] ffPatientStore].prefs;
	
qastate = @"";
	if ( [prefs objectForKey:@"QAState"]) 
		qastate =  [prefs objectForKey:@"QAState"];
	
	theurl= [[NSString stringWithFormat:@"http://%@/%@/?auth=%@",
						  [[DataManager sharedInstance] ffMCappliance],
						 [[[DataManager sharedInstance] ffPatientWrapper] patientID],
						  [[DataManager sharedInstance] ffMCauth]
			  ] retain];
	BREADCRUMBS_PUSH;
	
	return self;
}
-(void) dealloc 
{
	BREADCRUMBS_POP;
	[outerView release];
	[webView release];
	[theurl release];
	[super dealloc];
}

// put buttons towards the bottom
-(void) makebottombuttons
{

	CGRect buttonframe = CGRectMake(4, 410, 47.0f, 47.0f); // button is 30 wide for now
	
	float deltaX = 266.0f;
	
	
	
	
	UIButton *cButton;
	//UIView *bview = [[[UIView alloc] initWithFrame:	bframe ] retain];
	
	
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"Y" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(pass:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];
	
	buttonframe.origin.x += deltaX; 
	
	
	cButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];	
	[cButton setTitle:@"N" forState:UIControlStateNormal];
	[cButton setFrame:buttonframe];
	[cButton addTarget:self action:@selector(fail:) forControlEvents:UIControlEventTouchUpInside];
	[outerView addSubview:cButton];
	
		
}	
- (void) loadView
{	
	
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  //
//	appFrame.origin.y = appFrame.origin.y +44.0f;  
//	appFrame.size.height = appFrame.size.height - 44.0f ;
	
	CONSOLE_LOG (@"outerview %f %f %f %f" , appFrame.origin.x,appFrame.origin.y,appFrame.size.width,appFrame.size.height);
	outerView = [[UIView alloc] initWithFrame:appFrame];  
	
	//outerView.backgroundColor = [UIColor lightGrayColor]; //[UIColor colorWithRed:.76f green:.76f blue:.76f alpha:1.0]; 
	
	
	if ([@"QA Red" isEqualToString:qastate])	
		 outerView.backgroundColor = [UIColor colorWithRed:.88f green:.0f blue:.0f alpha:.20f];
	else 
		if ([@"QA Green" isEqualToString:qastate])	
		 outerView.backgroundColor = [UIColor colorWithRed:.0f green:.88f blue:.0f alpha:.20f];
		else 
		 outerView.backgroundColor = [UIColor lightGrayColor];
	
	
	
	
	
	self.view = outerView;
	//outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	self.view = outerView;  
	appFrame.origin.y = appFrame.origin.y +36.0f;  
	appFrame.size.height = appFrame.size.height - 70.0f ;
	appFrame.origin.x = appFrame.origin.x + 10.0f;
	appFrame.size.width = appFrame.size.width - 20.0f ;
	
	CONSOLE_LOG (@"webview%f %f %f %f" , appFrame.origin.x,appFrame.origin.y,appFrame.size.width,appFrame.size.height);

	webView = [[UIWebView alloc] initWithFrame:appFrame];  
	webView.scalesPageToFit=YES;	webView.backgroundColor = [UIColor lightGrayColor];  

	
    self.navigationItem.title = [NSString stringWithFormat:@"     QA     "]; 
	

	webView.autoresizingMask = 0;//UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;
	
	NSURL *url = [NSURL URLWithString:theurl];
		
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
	[webView loadRequest:requestObj];
	
	[self.view addSubview:webView];
	[self makebottombuttons]; // now throw the buttons on top
	TRY_RECOVERY;
	
}


@end

