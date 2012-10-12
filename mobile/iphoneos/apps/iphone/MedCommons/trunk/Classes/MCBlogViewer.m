//
//  MCBlogViewer.m
//  MedCommons
//
//  Created by bill donner on 1/20/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import "MedCommons.h"
#import "MCBlogViewer.h"
#import "MCSOAPController.h"
#import "DashboardPatient.h"
#import "DataManager.h"
@implementation MCBlogViewer
- (void) soap:(id) foo
{

MCSOAPController *soapController;	


soapController = 
(MCSOAPController *)[[MCSOAPController alloc] init];

[self.navigationController pushViewController:(UIViewController *)soapController 	animated:YES];
[soapController release];
}

-(void) alldone  : (NSObject *)  id
{
	[self.navigationController popViewControllerAnimated:YES];
	
}


 - (void)viewWillAppear:(BOOL)animated {
	 CONSOLE_LOG (@"Blog coming back into view %@",theurl);
	 
	 NSURL *url = [NSURL URLWithString:theurl];
	// NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];//setCachePolicy:NSURLRequestReloadIgnoringCacheData
	 // turn off the caching
	 NSURLRequest * requestObj = [NSURLRequest  
										requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringCacheData  
										 timeoutInterval:100];
	 
	 
	 [webView loadRequest:requestObj];
 [super viewWillAppear:animated];
 }



-(MCBlogViewer *) init{
	self = [super init];
	
	theurl= [[NSString stringWithFormat:@"http://ci.myhealthespace.com/midatastore/blogshowiphone.php?pmcid=%@&password=%@&from=%@&mcid=%@&name=%@",	 
						  [[DataManager sharedInstance] ffMCprovidermcid],	  
						  [[DataManager sharedInstance] ffMCpassword],
						  [[DataManager sharedInstance] ffMCusername], 
						   [[[DataManager sharedInstance] ffPatientWrapper] patientID],
						  [[[DataManager sharedInstance] ffPatientWrapper] name]] retain];




	BREADCRUMBS_PUSH;
	
	return self;
}
-(void) dealloc 
{
	BREADCRUMBS_POP;
	[webView release];
	[outerView release];
	
	[theurl release];
	//[panetitle release];
	[super dealloc];
}
- (void) loadView
{	
	
	//[super loadView];
	self.navigationItem.leftBarButtonItem = 
	[[UIBarButtonItem alloc] initWithTitle:@"Done" style:UIBarButtonItemStyleBordered
									target:self  action:@selector(alldone:)];
	
	self.navigationItem.rightBarButtonItem = 
	[[UIBarButtonItem alloc] initWithTitle:@"SOAP" style:UIBarButtonItemStyleBordered
									target:self  action:@selector(soap:)];
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  

	outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor lightGrayColor];  
	self.view = outerView;
	appFrame.size.height = appFrame.size.height-24.0;
	appFrame.origin.y	= appFrame.origin.y + 24.0;
	CONSOLE_LOG (@"blogviewer webframe %f %f %f %f %@" , appFrame.origin.x,appFrame.origin.y,appFrame.size.width,appFrame.size.height, theurl);
	webView = [[UIWebView alloc] initWithFrame:appFrame];  
	webView.scalesPageToFit=YES;
	webView.backgroundColor = [UIColor lightGrayColor];  
	
	self.navigationItem.title = [[[DataManager sharedInstance] ffPatientWrapper] nameForTitle]; 
	self.navigationItem.hidesBackButton = NO;
	
	webView.autoresizingMask = 0;//UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;
	;
	

	
	[self.view addSubview:webView];
	TRY_RECOVERY;
}


@end

