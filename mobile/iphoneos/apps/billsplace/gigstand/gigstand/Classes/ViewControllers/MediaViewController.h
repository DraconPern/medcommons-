//
//  MediaViewController.h
//  GigStand
//
//  Created by bill donner on 2/4/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface MediaViewController : UIViewController {
	UIWebView *webView;
	NSURL *url;
	NSString *tune;

}

-(MediaViewController *) initWithURL: (NSURL *)urlx andWithTune: (NSString *)tunex;
@end
