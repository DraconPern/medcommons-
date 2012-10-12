//
//  SplashViewController.m
//  gigstand
//
//  Created by bill donner on 4/3/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//
#import "DataManager.h"
#import "GigStandAppDelegate.h"
#import "SplashViewController.h"


// this is meant to be subclassed

@implementation SplashViewController
@synthesize contentView = contentView_;



#pragma mark - View lifecycle



- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return YES;
}




#pragma mark Public Instance Methods






- (id) initWithVerticalHTML: (NSString *)vhtml horitzontalHTML: (NSString *)hhtml title: (NSString *)title 
            leftButtonOrNil:(NSString *)lb rightButtonOrNil:(NSString *)rb
             target: (id) obj leftaction: (SEL) lselector  rightaction: (SEL) rselector;
{
    self = [super init];
    
    if (self)
    {
        self->depth_ = 0;
        self->leftbuttonlabel = lb? [lb copy]:nil;
        self->rightbuttonlabel = rb? [rb copy]:nil;
        self->titlelabel = [title copy];
        self->verticalHTML = [vhtml copy];
        self->horizontalHTML = [hhtml copy];
        self->target = obj;
        self->leftaction = lselector;
        self->rightaction = rselector;
    }
    
    return self;
}

- (void) injectJavaScript: (NSString *) jsString
{
    [self.contentView stringByEvaluatingJavaScriptFromString: jsString];
}

- (void) refresh
{
    
    [self setColorForNavBar];
    
    
    NSString *resourcePath = [[NSBundle mainBundle] resourcePath]; 
    NSURL *baseURL = [[[NSURL alloc] initFileURLWithPath:resourcePath isDirectory:YES] autorelease];
    
   // NSLog (@"basURL is %@",baseURL);
    
    NSString *html = ((UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))?
                      self->horizontalHTML:self->verticalHTML);
    
    [self->contentView_ loadHTMLString: html
                               baseURL: baseURL];    

    
    
    
}



#pragma mark Overridden UIViewController Methods

- (void) loadView
{
//    SEL la = @selector(donePressed:); // default in case no left action supplied
//    id ta = self;
//    if (self->target) ta = self->target;
//    if (self->leftaction) la = self->leftaction;
    
	if (self->leftbuttonlabel)
	self.navigationItem.leftBarButtonItem =
	[[[UIBarButtonItem alloc] initWithTitle:self->leftbuttonlabel style:UIBarButtonItemStylePlain 
                                     target:self->target action:self->leftaction] autorelease];
    
    
	if (self->rightbuttonlabel)
        self.navigationItem.rightBarButtonItem =
        [[[UIBarButtonItem alloc] initWithTitle:self->rightbuttonlabel style:UIBarButtonItemStylePlain 
                                         target:self->target action:self->rightaction] autorelease];
	
    
    
    self.navigationItem.title = self->titlelabel;
    
    
    CGRect tmpFrame;
    
    //
    // Background view:
    //
    tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
    
    UIView *backgroundView = [[[UIView alloc] initWithFrame: tmpFrame]
                              autorelease];
    
    backgroundView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                       UIViewAutoresizingFlexibleWidth);
    backgroundView.backgroundColor = [DataManager applicationColor];//[UIColor lightGrayColor];
    
    //
    // Content view:
    //
    
    tmpFrame.origin.y += [DataManager navBarHeight];
    tmpFrame.size.height -= [DataManager navBarHeight];
//tmpFrame = CGRectStandardize (backgroundView.bounds);
    
    
    self->contentView_ = [[UIWebView alloc] initWithFrame: tmpFrame];
    
    self->contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                           UIViewAutoresizingFlexibleWidth);
    self->contentView_.backgroundColor = [DataManager applicationColor];
    self->contentView_.dataDetectorTypes = UIDataDetectorTypeLink; // no phone numbers

    self->contentView_.scalesPageToFit = YES;
    
    [backgroundView addSubview: self->contentView_];

    
    //
    // Kick off initial load of content view:
    //
    [self refresh];

    // puthis on the screen
    
    self.view = backgroundView;
}


#pragma mark Overridden NSObject Methods

-(void) dealloc
{
    [self->contentView_ release];
    [self->rightbuttonlabel release];
    [self->leftbuttonlabel release];
    [self->titlelabel release];
    [self->verticalHTML release];
    
    [self->horizontalHTML release];
    
    [super dealloc];
}

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{	
    // leaks
    
    [self loadView];
}



@end
