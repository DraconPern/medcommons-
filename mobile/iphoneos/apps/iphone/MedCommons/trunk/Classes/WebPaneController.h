//
//  WebPaneController.h
//  MedCommons
//
//  Created by bill donner on 12/28/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

@interface WebPaneController : UIViewController {
	
	NSString *theurl;
	NSString *panetitle;
	UIWebView *webView;
	UIView *outerView;

}

-(WebPaneController *) initWithURL: (NSString *)_urlc andWithTitle: (NSString *)_tit;
@end
