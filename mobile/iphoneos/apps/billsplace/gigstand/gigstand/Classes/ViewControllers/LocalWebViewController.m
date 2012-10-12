//
//  LocalWebViewController.m
//  gigstand
//
//  Created by bill donner on 4/28/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "LocalWebViewController.h"
#import "GigStandAppDelegate.h"
#import "DataManager.h"

#pragma mark Internal Constants

#define CONTENT_VIEW_EDGE_INSET    0.0f
#define DEFAULT_CONTENT_URL_STRING @"http://www.medcommons.net"

@interface LocalWebViewController () <UIWebViewDelegate>

- (void) startTrackingLoad;

- (NSTimeInterval) stopTrackingLoad;

@end

@implementation LocalWebViewController

@synthesize contentView                = contentView_;


-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
    //[self.navigationController popViewControllerAnimated:YES];
}



#pragma mark Public Instance Methods

- (id) initWithHTML: (NSString *) html title:(NSString *) title;
{
    self = [super init];
    
    if (self)
    {
        self->html_ = [html copy ];
        self->title_ = [title copy];
        self->depth_ = 0;
        self->sharedDocument_ = YES; // for now ...
    }
    
    return self;
}

- (void) injectJavaScript: (NSString *) jsString
{
    [self.contentView stringByEvaluatingJavaScriptFromString: jsString];
}

- (void) refresh
{
    [self.contentView loadHTMLString: self->html_ baseURL:[[NSBundle mainBundle] bundleURL]];
}


#pragma mark Private Instance Methods

- (void) startTrackingLoad
{
    self->depth_++;
    
    [self->activityIndicator_ startAnimating];
    
    //    [self.appDelegate didStartNetworkActivity];
    
    self->startTime_ = [NSDate timeIntervalSinceReferenceDate];
}

- (NSTimeInterval) stopTrackingLoad
{
    NSTimeInterval stopTime = [NSDate timeIntervalSinceReferenceDate];
    
    //   [self.appDelegate didStopNetworkActivity];
    
    [self->activityIndicator_ stopAnimating];
    
    if (self->depth_ > 0)
        self->depth_--;
    
    return stopTime - self->startTime_;
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
	
	self.navigationItem.leftBarButtonItem =
	[[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(donePressed)] autorelease];
    
    self.navigationItem.titleView = [DataManager makeTitleView:self->title_ ];	
    
    [self setColorForNavBar ];
    
	//NSLog (@"webview loadView with %@",self->contentURL_);
    CGRect tmpFrame;
    
    //
    // Background view:
    //
    tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
    
    UIView *backgroundView = [[[UIView alloc] initWithFrame: tmpFrame]
                              autorelease];
    
    backgroundView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                       UIViewAutoresizingFlexibleWidth);
    backgroundView.backgroundColor = [UIColor lightGrayColor];
    
    //
    // Content view:
    //
    tmpFrame = CGRectStandardize (backgroundView.bounds);
    
    float fudge = [DataManager navBarHeight];
	tmpFrame.origin.y+=fudge;
	tmpFrame.size.height-=fudge;
    
    CGFloat edgeInset = CONTENT_VIEW_EDGE_INSET;
    
    //
    // If desired, inset content view frame a bit to help in debugging:
    //
    if (edgeInset > 0.0f)
        tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
                                          UIEdgeInsetsMake (edgeInset,
                                                            edgeInset,
                                                            edgeInset,
                                                            edgeInset));
    
    self->contentView_ = [[UIWebView alloc] initWithFrame: tmpFrame];
    
    self->contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                           UIViewAutoresizingFlexibleWidth);
    self->contentView_.backgroundColor = [UIColor blueColor];
    self->contentView_.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
    self->contentView_.delegate =  nil ; // no delegate for string stuff
    self->contentView_.scalesPageToFit = YES;
    
    [backgroundView addSubview: self->contentView_];
    
    //
    // Activity indicator view:
//    //
//    self->activityIndicator_= [[UIActivityIndicatorView alloc]
//                               initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleGray];
//    
//    self->activityIndicator_.center = CGPointMake (100.0f, 100.0f); // sort of lines up with hurl
//    // display in non-offensive
//    // manner
//    
//    [backgroundView addSubview: self->activityIndicator_];
    
    self.view = backgroundView;
    
    //
    // Kick off initial load of content view:
    //
    [self->contentView_ //loadRequest: [NSURLRequest requestWithURL: self->contentURL_]];
     loadHTMLString: self->html_ baseURL: [[NSBundle mainBundle] bundleURL]];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

#pragma mark Overridden NSObject Methods

-(void) dealloc
{
    [self stopTrackingLoad];
    
    self->contentView_.delegate = nil;    // Apple docs say this is required
    

    [self->contentView_ release];
    [self->html_ release];
    [self->title_ release];
    
    [super dealloc];
}

@end

