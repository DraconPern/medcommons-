//
//  WebViewController.h
//  MCProvider
//
//  Created by Bill Donner on 12/28/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WebViewController : UIViewController
{
@private

    UIActivityIndicatorView *activityIndicator_;
    NSURL                   *contentURL_;
    UIWebView               *contentView_;
    NSUInteger               depth_;
    BOOL                     sharedDocument_;
    NSTimeInterval           startTime_;
	
	
	
	
	
}

@property (nonatomic, retain, readonly) UIWebView *contentView;
@property (nonatomic, assign, readonly) BOOL       hidesMasterViewInLandscape;

- (id) initWithURL: (NSURL *) URL;

- (void) injectJavaScript: (NSString *) jsString;

- (void) refresh;

@end
