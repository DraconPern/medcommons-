//
//  TuneViewController.m
//  MusicStand
//
//  Created by bill donner on 10/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "TuneViewController.h"
#import "DataManager.h"
#import "DataStore.h"
#import "TitleNode.h"

#pragma mark Internal Constants

#define CONTENT_VIEW_EDGE_INSET    0.0f
#define DEFAULT_CONTENT_URL_STRING @"http://www.medcommons.net"
#define ANIMATION_DURATION           0.3f

#define NORMAL_EDGE_INSET            20.0f
#define NARROW_EDGE_INSET            8.0f
#define ZERO_EDGE_INSET              0.0f

#define NORMAL_TOOLBAR_HEIGHT        44.0f
#define NARROW_TOOLBAR_HEIGHT        32.0f
@interface TuneViewController () <UIWebViewDelegate>

- (void) startTrackingLoad;

- (NSTimeInterval) stopTrackingLoad;

@end
@implementation TuneViewController
@synthesize mainTitle;
@synthesize footerToolbar      = footerToolbar_;
@synthesize footerView         = footerView_;

@synthesize backgroundView         = backgroundView_;
@dynamic    needsFooterToolbar;

@dynamic    toolbarHeight;


@synthesize contentView                = contentView_;
@dynamic    hidesMasterViewInLandscape;

-(NSString *) displayRect : (CGRect ) r
{
	
	return [NSString stringWithFormat:@"x %4.2f y %4.2f width %4.2f height %4.2f",
			r.origin.x, r.origin.y, r.size.width,r.size.height];
	
}


- (BOOL) needsFooterToolbar
{ 
	return YES;
}

- (void) scrollToSelectedRange
{
	
}


- (void) setupSegmentedControl
{
    
		// get all of the nicknames for the archives
		
		TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if (!tn) {NSLog(@"could not find %@ in segmentedControl search of titlesDictionary", self->mainTitle); return;}
		
		NSUInteger countArchives = [tn.variants count];
	//	NSLog (@"tuneview %@ has %d variants",self->mainTitle, countArchives	);
		
        NSMutableArray *segmentItems = [[[NSMutableArray alloc]
                                         initWithCapacity: countArchives]
                                        autorelease];
		
  
	for (NSString *key in tn.variants) // first make a dummy array
	{ 
		[segmentItems addObject:@"Should Override"];
	}
	for (NSString *key in tn.variants)
	{
		//up to the first slash to get the archive part of the filepath;
		NSString *segmentlabel = [DataManager shortNameFromArchiveName: [DataManager archiveNameFromPath:key]];
		[segmentItems replaceObjectAtIndex:[[tn.variants objectForKey:key] unsignedIntValue] withObject: [NSString stringWithFormat:@"  %@  ",
								  segmentlabel
								  ]];
		
	}
		
        self->segmentedControl_ = [[[UISegmentedControl alloc] initWithItems: segmentItems] retain];
		
        self->segmentedControl_.backgroundColor = [UIColor clearColor];
        self->segmentedControl_.momentary = NO;
	    self->segmentedControl_.enabled = YES;
        self->segmentedControl_.segmentedControlStyle = UISegmentedControlStyleBar;
        self->segmentedControl_.selectedSegmentIndex = 0;
        self->segmentedControl_.tintColor = [UIColor lightGrayColor];
		
        [self->segmentedControl_ addTarget: self
                                    action: @selector (segmentedControlPushed)
                          forControlEvents: UIControlEventValueChanged];
    
	
}


-(void) segmentedControlPushedInner:(NSUInteger )i;
{
	
	TitleNode *tn = [[DataManager sharedInstance].titlesDictionary objectForKey:self->mainTitle];
	if (!tn) {NSLog(@"could not find %@ in segmentedControl search of titlesDictionary", self->mainTitle); return;}
	for (NSString *key in tn.variants)
		
	{
		NSUInteger value = [[tn.variants objectForKey:key] unsignedIntValue];
		NSLog(@"key %@ value %d",key,value);
		
		if (value  == i) // found it, change the view
		{
			
			NSString *url = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],key];
			NSURL    *docURL = [NSURL fileURLWithPath: url
										  isDirectory: NO];
			if (self->segmentedControl_) 
			{	// take this off the screen
				[self->segmentedControl_ release];
			}
			// add the right side segmentcontrol back in
			[self setupSegmentedControl];
			
			
			[self refreshWithURL: docURL ];
			break;
		}
	}
}

-(void) segmentedControlPushed{
	// one of the segmented buttons was pushed
	NSUInteger i = self->segmentedControl_.selectedSegmentIndex;
	NSLog (@"Segment %d was selected",i);
	[self segmentedControlPushedInner:i];
	
	
	
	
}
-(void) displayTune: (NSString *)title
{
	self->mainTitle = title;
	NSLog (@"Segment 0 was pseudo pushed with title %@",title);
	[self segmentedControlPushedInner:0];
}


-(void) segmentedRightSideControlPushed{
	// one of the segmented buttons was pushed
	NSUInteger i = self->segmentedRightSideControl_.selectedSegmentIndex;
	NSLog (@"Right side Segment %d was selected",i);
	switch (i)
	{
		case 0:
		{
			[self displayTune: [DataManager reWind: self->mainTitle]];
			break;
		}
			
		case 1:
		{
			[self displayTune: [DataManager goBack: self->mainTitle]];
			break;
		}
			
		case 2:
		{
			[self displayTune: [DataManager goForward: self->mainTitle]];
			break;
		}
			
		case 3:
		{
			[self displayTune: [DataManager fastForward: self->mainTitle]];
			break;
		}
			
	}
}

- (void) setupRightSideSegmentedControl
{
    
	NSMutableArray *segmentItems = [[[NSMutableArray alloc]
									 initWithCapacity: 4]
									autorelease];
	[segmentItems addObject:@"  <<  "];
	[segmentItems addObject:@"  <  "];
	[segmentItems addObject:@"  >  "];
	[segmentItems addObject:@"  >>  "];
	
	
	self->segmentedRightSideControl_ = [[[UISegmentedControl alloc] initWithItems: segmentItems] retain];
	
	self->segmentedRightSideControl_.backgroundColor = [UIColor clearColor];
	self->segmentedRightSideControl_.momentary = YES;
	self->segmentedRightSideControl_.enabled = YES;
	self->segmentedRightSideControl_.segmentedControlStyle = UISegmentedControlStyleBar;
	self->segmentedRightSideControl_.selectedSegmentIndex = 0;
	self->segmentedRightSideControl_.tintColor = [UIColor clearColor];
	
	[self->segmentedRightSideControl_ addTarget: self
										 action: @selector (segmentedRightSideControlPushed)
							   forControlEvents: UIControlEventValueChanged];
    
	
}


- (CGFloat) toolbarHeight
{
    UIDevice *device = [UIDevice currentDevice];
	
    //
    // If device is in landscape orientation AND device is iPhone, use narrow
    // toolbar height:
    //
    if (UIDeviceOrientationIsLandscape (device.orientation) &&
        (![device respondsToSelector: @selector (userInterfaceIdiom)] ||
         (device.userInterfaceIdiom == UIUserInterfaceIdiomPhone)))
        return NARROW_TOOLBAR_HEIGHT;
	
    return NORMAL_TOOLBAR_HEIGHT;
}



#pragma mark Private Instance Methods

- (void) startTrackingLoad
{
    self->depth_++;
	
    [self->activityIndicator_ startAnimating];
	
	// [self.appDelegate didStartNetworkActivity];
	
    self->startTime_ = [NSDate timeIntervalSinceReferenceDate];
}

- (NSTimeInterval) stopTrackingLoad
{
    NSTimeInterval stopTime = [NSDate timeIntervalSinceReferenceDate];
	
	//  [self.appDelegate didStopNetworkActivity];
	
    [self->activityIndicator_ stopAnimating];
	
    if (self->depth_ > 0)
        self->depth_--;
	
    return stopTime - self->startTime_;
}

#pragma mark Overridden UIViewController Methods



- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}
-(void) remakeContentView
{
	CGRect tmpFrame = self->contentFrame_;
	tmpFrame.size.height -= 44.0f+20.0f;
	
	//NSLog (@"remake contentView contentFrame_ %@",[self displayRect:tmpFrame]);
	
	// and adds it to the background
    self->contentView_ = [[UIWebView alloc] initWithFrame: tmpFrame];
	
    self->contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                           UIViewAutoresizingFlexibleWidth);
    self->contentView_.backgroundColor = [UIColor greenColor];
    self->contentView_.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
    self->contentView_.delegate =  self ;
    self->contentView_.scalesPageToFit = YES;
	
    [self->backgroundView_ addSubview: self->contentView_];
	
    //
    // Kick off  load of content view:
    //
    [self->contentView_ loadRequest: [NSURLRequest requestWithURL: self->contentURL_]];
}
-(void) makeContentView
{
	CGRect tmpFrame = self->contentFrame_;

	
//	NSLog (@"make contentView contentFrame_ %@",[self displayRect:tmpFrame]);
	
	// and adds it to the background
    self->contentView_ = [[UIWebView alloc] initWithFrame: tmpFrame];
	
    self->contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                           UIViewAutoresizingFlexibleWidth);
    self->contentView_.backgroundColor = [UIColor greenColor];
    self->contentView_.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers
    self->contentView_.delegate =  self ;
    self->contentView_.scalesPageToFit = YES;
	
    [self->backgroundView_ addSubview: self->contentView_];
	
    //
    // Kick off  load of content view:
    //
    [self->contentView_ loadRequest: [NSURLRequest requestWithURL: self->contentURL_]];
}
- (void) refreshWithURL : (NSURL *) URL
{//
	//NSLog(@"refreshWithURL with %@",URL);	
	self->contentView_.delegate = nil; //prevent freakout
	
	[self->contentView_ release];
	
	self->contentURL_ = [URL retain]; // new URL 

	// everylast thing was set up in init
	
	[self remakeContentView];
	
	
    self.view = self->backgroundView_;
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void) viewDidLoad {
	
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

-(void) setupTuneView
{
	//
	// Background view:
	//
	CGRect tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	
//	NSLog (@"frame parentViewController_ %@",[self displayRect:tmpFrame]);
	
	self->backgroundView_ = [[UIView alloc] initWithFrame: tmpFrame]
	;
	
////	NSLog (@"frame backgroundView_ %@",[self displayRect:tmpFrame]);
	self->backgroundView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
											  UIViewAutoresizingFlexibleWidth);
	self->backgroundView_.backgroundColor = [UIColor lightGrayColor];
	
	// Content Window (frame only) is the whole screen minus the toolbar at the bottom
	// Content view:
	tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	tmpFrame.size.height -= self.toolbarHeight;
	

	
	CGFloat edgeInset = 5.0f;
	
	//
	// If desired, inset content view frame a bit to help in debugging:
	//
	if (edgeInset > 0.0f)
		tmpFrame = UIEdgeInsetsInsetRect (tmpFrame,
										  UIEdgeInsetsMake (edgeInset,
															edgeInset,
															edgeInset,
															edgeInset));
	
	self->contentFrame_ = tmpFrame; // save this for later
	
	
//	NSLog (@"frame contentView_ %@",[self displayRect:tmpFrame]);
	
	//
	// Activity indicator view:
	//
	self->activityIndicator_= [[UIActivityIndicatorView alloc]
							   initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleGray];
	
	self->activityIndicator_.center = CGPointMake (100.0f, 100.0f); // sort of lines up with hurl
	
	// Stuff at the bottom
	
	//
	// Footer view frame uses coordinates relative to its containing view
	// (i.e., background view):
	//
	
	tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	tmpFrame.origin.y = (tmpFrame.size.height -  self.toolbarHeight);
	tmpFrame.size.height = self.toolbarHeight;
	
	//
	// Create footer view:
	//
//	NSLog (@"frame footerView_ %@",[self displayRect:tmpFrame]);
	self->footerView_ = [[UIView alloc] initWithFrame: tmpFrame];
	
	self.footerView.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
										UIViewAutoresizingFlexibleWidth);
	self.footerView.backgroundColor = [UIColor clearColor];
	
	tmpFrame  = CGRectStandardize (self.parentViewController.view.bounds);
	tmpFrame.origin.y = (tmpFrame.size.height -  self.toolbarHeight);
	tmpFrame.size.height = self.toolbarHeight;	
	self->footerToolbar_ = [[UIToolbar alloc] init];
	
	self.footerToolbar.autoresizingMask = (UIViewAutoresizingFlexibleTopMargin |
										   UIViewAutoresizingFlexibleWidth);
	self.footerToolbar.barStyle = UIBarStyleBlack;

	[self setupSegmentedControl];
	[self setupRightSideSegmentedControl];
	
	UIBarButtonItem *flexSpace = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemFlexibleSpace
																				target: nil
																				action: NULL] 
								  autorelease];
	UIBarButtonItem *segCtlBBI = [[[UIBarButtonItem alloc]
								   initWithCustomView: self->segmentedControl_] autorelease]
								 ;
	UIBarButtonItem *segRSCtlBBI = [[[UIBarButtonItem alloc]
								   initWithCustomView: self->segmentedRightSideControl_] autorelease]
	;
	
	self.footerToolbar.items = [NSArray arrayWithObjects:
								//  flexSpace,
								segCtlBBI,
								flexSpace,
								segRSCtlBBI,
								nil];	
	
	self.footerToolbar.frame = tmpFrame;
	
	//NSLog (@"frame footerToolbar_ %@",[self displayRect:tmpFrame]);
	// add this all into the background in precisely this order otherwise the butons are dead!
	
	[self->backgroundView_ addSubview:self->footerView_];
	[self->backgroundView_ addSubview:self->footerToolbar_];
	
	[self->backgroundView_ addSubview:self->activityIndicator_];
	
	
}

-(TuneViewController *) initWithURL:(NSURL *)URL andWithTitle:(NSString *) title

{
	self = [super init];
	
    if (self)
    {
        self->contentURL_ = [URL retain];
        self->depth_ = 0;
        self->sharedDocument_ = NO; //[URL.scheme isEqualToString: NSURLFileScheme];  // for now ...
		self->mainTitle = title; 
		self->first = YES;
	}
		
	return self;
}


- (void)dealloc {
	
	[self stopTrackingLoad];
	
	[self->footerToolbar_ release];
	[self->footerView_ release];
	[self->segmentedControl_ release];
	
	self->contentView_.delegate = nil;    // Apple docs say this is required
	
	[self->activityIndicator_ release];//loadview
	//[self->contentURL_ release];
	[self->contentView_ release];//loadview
	[self->backgroundView_ release]; //init
	
    [super dealloc];
}

- (void) loadView
{
	if (first) {
		[self setupTuneView]; 
		first = NO;
	}
	
	[self makeContentView];
	
    self.view = self->backgroundView_;
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
//	NSLog (@"Loading <%@>, depth: %d",
//		   webView.request.URL,
//		   self->depth_);
	
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

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}


@end
