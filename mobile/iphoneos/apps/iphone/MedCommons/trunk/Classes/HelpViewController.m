//
//  WebViewController.m
//  ForensicFoto
//
//  Created by bill donner on 9/4/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//
#import "MedCommons.h"
#import "HelpViewController.h"

#import "DataManager.h"


@implementation HelpViewController


-(HelpViewController *) init
{

	self = [super init];
	
	NSDictionary *environment =	   [[[NSBundle mainBundle] infoDictionary]   objectForKey:@"LSEnvironment"];
	
	NSString *fixedcontent = [environment objectForKey:@"fixedcontent"];

	urladdress = [[NSString alloc] initWithFormat:@"%@/mc/iPhone329ip.mov",fixedcontent,nil];
	
	BREADCRUMBS_PUSH;
				
	return self;
}
-(void) dealloc 
{
	BREADCRUMBS_POP;
	[super dealloc];
}
- (void) loadView
{	

	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  
	
	CONSOLE_LOG (@"help webframe %f %f %f %f" , appFrame.origin.x,appFrame.origin.y,appFrame.size.width,appFrame.size.height);
	UIWebView *webView = [[UIWebView alloc] initWithFrame:appFrame];  
	//webView.scalesPageToFit=YES;
	webView.backgroundColor = [UIColor whiteColor];  
	
	webView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  

	self.view = webView; 
	NSURL *url = [NSURL URLWithString:urladdress];
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
	[webView loadRequest:requestObj];
	[webView release];
}
- (void)viewWillAppear:(BOOL)animated
{		[super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{ 
	
}

@end

