//
//  WebPaneController.m
//  MedCommons
//
//  Created by bill donner on 12/28/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//


#import "MedCommons.h"
#import "DataManager.h"
#import "WebPaneController.h"


@implementation WebPaneController


-(WebPaneController *) initWithURL: (NSString *)_urlx andWithTitle: (NSString *)_titley
{
	self = [super init];
	theurl = [_urlx copy]; // important  - else crashes
	panetitle = [_titley copy] ;

//BREADCRUMBS_PUSH;
	
	return self;
}
-(void) dealloc 
{
	//BREADCRUMBS_POP;
	[webView release];
	[outerView release];
	[theurl release];
	[panetitle release];
	[super dealloc];
}
- (void) loadView
{	
	
	CGRect appFrame = [[UIScreen mainScreen] applicationFrame];  

	outerView = [[UIView alloc] initWithFrame:appFrame];  
	outerView.backgroundColor = [UIColor lightGrayColor];  
	//outerView.autoresizingMask = UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;  
	self.view = outerView;  
	appFrame.size.height = appFrame.size.height-24.0;
	appFrame.origin.y	= appFrame.origin.y + 24.0;
	//CONSOLE_LOG (@"webpane webframe %f %f %f %f %@" , appFrame.origin.x,appFrame.origin.y,appFrame.size.width,appFrame.size.height, theurl);
    webView = [[UIWebView alloc] initWithFrame:appFrame];  
	webView.scalesPageToFit=YES;
	webView.backgroundColor = [UIColor redColor];  
//	[[[webView subviews] lastObject] setScrollingEnabled:NO];
	
	self.navigationItem.title = panetitle; 
	self.navigationItem.hidesBackButton = NO;
	
	webView.autoresizingMask = 0;//UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth;
	 
	NSURL *url = [NSURL URLWithString:theurl];

	NSURLRequest *requestObj = [NSURLRequest requestWithURL:url];
	[webView loadRequest:requestObj];
	
	[self.view addSubview:webView];
	
	
}


@end

