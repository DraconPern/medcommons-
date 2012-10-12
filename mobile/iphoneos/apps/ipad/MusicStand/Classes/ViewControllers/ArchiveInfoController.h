//
//  ArchiveInfoController.h
//  MusicStand
//
//  Created by bill donner on 10/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ArchiveInfoController : UIViewController {
	
	
@private
	
	NSURL                   *contentURL_;
	UIWebView               *contentView_;
	NSUInteger               depth_;
	BOOL                     sharedDocument_;
	
	
	
	
	
}

@property (nonatomic, retain, readonly) UIWebView *contentView;
@property (nonatomic, assign, readonly) BOOL       hidesMasterViewInLandscape;

- (id) initWithURL: (NSURL *) URL;

- (void) injectJavaScript: (NSString *) jsString;


@end
