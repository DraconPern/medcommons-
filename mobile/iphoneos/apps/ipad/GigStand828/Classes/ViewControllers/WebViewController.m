//
//  WebViewController.m
//  MCProvider
//
//  Created by Bill Donner on 12/28/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//

#import "WebViewController.h"

#pragma mark -
#pragma mark Public Class WebViewController
#pragma mark -

#pragma mark Internal Constants

#define CONTENT_VIEW_EDGE_INSET    0.0f
#define DEFAULT_CONTENT_URL_STRING @"http://www.medcommons.net"

@interface WebViewController () <UIWebViewDelegate>

- (void) startTrackingLoad;

- (NSTimeInterval) stopTrackingLoad;

@end

@implementation WebViewController

@synthesize contentView                = contentView_;


-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}



#pragma mark Public Instance Methods

- (id) initWithURL: (NSURL *) URL
{
    self = [super init];

    if (self)
    {
        self->contentURL_ = [URL retain];
        self->depth_ = 0;
        self->sharedDocument_ = [URL.scheme isEqualToString: NSURLFileScheme];  // for now ...
    }

    return self;
}

- (void) injectJavaScript: (NSString *) jsString
{
    [self.contentView stringByEvaluatingJavaScriptFromString: jsString];
}

- (void) refresh
{
    [self.contentView loadRequest: [NSURLRequest requestWithURL: self->contentURL_]];
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
    self->contentView_.delegate =  self ;
    self->contentView_.scalesPageToFit = YES;

    [backgroundView addSubview: self->contentView_];

    //
    // Activity indicator view:
    //
    self->activityIndicator_= [[UIActivityIndicatorView alloc]
                               initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleGray];

    self->activityIndicator_.center = CGPointMake (100.0f, 100.0f); // sort of lines up with hurl
                                                                    // display in non-offensive
                                                                    // manner

    [backgroundView addSubview: self->activityIndicator_];

    self.view = backgroundView;

    //
    // Kick off initial load of content view:
    //
    [self->contentView_ loadRequest: [NSURLRequest requestWithURL: self->contentURL_]];
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

    [self->activityIndicator_ release];
    [self->contentURL_ release];
    [self->contentView_ release];

    [super dealloc];
}

- (id) init
{
    return [self initWithURL: [NSURL URLWithString: DEFAULT_CONTENT_URL_STRING]];
}



#pragma mark UIWebViewDelegate Methods

- (void) webView: (UIWebView *) webView
didFailLoadWithError: (NSError *) error
{
    NSTimeInterval elapsedTime = [self stopTrackingLoad];

    if (![@"" isEqual: [webView.request.URL description]])  // avoid spurious errors ...
        NSLog (@"Failed to load <%@>, error code: %d, elapsed time: %.3fs, depth: %d",
               //self->contentURL_,
               webView.request.URL,
               error.code,
               elapsedTime,
               self->depth_);

    if (error.code != -999) // PDFs return this error code!
    {
        //
        // Report error inside web view:
        //
        NSString *errorString = [NSString stringWithFormat:
                                 @"<html><center><font size='+5' color='red'>Failed to load &lt;%@&gt;:<br>%@</font></center></html>",
                                 //self->contentURL_,
                                 webView.request.URL,
                                 error.localizedDescription];

        [self.contentView loadHTMLString: errorString
                                 baseURL: nil];
    }
}

- (void) webViewDidFinishLoad: (UIWebView *) webView
{
    NSTimeInterval elapsedTime = [self stopTrackingLoad];

    NSLog (@"Loaded <%@>, elapsed time: %.3fs, depth: %d",
           //self->contentURL_,
           webView.request.URL,
           elapsedTime,
           self->depth_);
}

- (void) webViewDidStartLoad: (UIWebView *) webView
{
//        NSLog (@"Loading <%@>, depth: %d",
//               webView.request.URL,
//             self->depth_);

    [self startTrackingLoad];
}

- (BOOL) webView: (UIWebView *) webView
shouldStartLoadWithRequest: (NSURLRequest *) request
  navigationType: (UIWebViewNavigationType) navType
{    return YES;
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}


- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}

@end
