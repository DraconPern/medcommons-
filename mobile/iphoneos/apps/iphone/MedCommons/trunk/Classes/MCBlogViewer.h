//
//  MCBlogViewer.h
//  MedCommons
//
//  Created by bill donner on 1/20/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//


@class DashboardPatient;
@interface MCBlogViewer : UIViewController {
	
	NSString *theurl;
	NSString *panetitle;
	DashboardPatient *wrapper;
	UIWebView *webView ;
	UIView *outerView;
	
}

-(MCBlogViewer *) init;
@end