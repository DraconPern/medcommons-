//
//  TuneViewController.h
//  MusicStand
//
//  Created by bill donner on 10/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
@interface TuneViewController : UIViewController {
	
    UIActivityIndicatorView *activityIndicator_;
    NSURL                   *contentURL_;
    UIWebView               *contentView_;
    NSUInteger               depth_;
    BOOL                     sharedDocument_;
    NSTimeInterval           startTime_;
	
	NSString		*mainTitle;
    UIToolbar          *footerToolbar_;
    UIView             *footerView_;
	UIView				*backgroundView_;
    UISegmentedControl *segmentedControl_;
	UISegmentedControl *segmentedRightSideControl_;
	CGFloat             toolbarHeight_;
	CGRect				contentFrame_;
	//
    // Flags:
    //
    BOOL                isShowingToolbar_;
	BOOL				first;
	
}


@property (nonatomic, retain, readonly) NSString			*mainTitle;
@property (nonatomic, retain, readonly) UIToolbar          *footerToolbar;
@property (nonatomic, retain, readonly) UIView             *footerView;

@property (nonatomic, retain, readonly) UIView             *backgroundView;
@property (nonatomic, assign, readonly) BOOL                needsFooterToolbar;
@property (nonatomic, assign, readonly) CGFloat             toolbarHeight;
@property (nonatomic, retain, readonly) UIWebView *contentView;
@property (nonatomic, assign, readonly) BOOL       hidesMasterViewInLandscape;

//- (id) initWithURL: (NSURL *) URL;

//- (void) injectJavaScript: (NSString *) jsString;

//- (void) refresh;


- (void) refreshWithURL : (NSURL *) URL;

-(TuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) title;


@end
