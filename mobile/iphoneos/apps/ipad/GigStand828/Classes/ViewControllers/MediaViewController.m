    //
//  MediaViewController.m
//  GigStand
//
//  Created by bill donner on 2/4/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "MediaViewController.h"
#import "DataManager.h"


@implementation MediaViewController

-(void) donePressed
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/
-(MediaViewController *) initWithURL: (NSURL *)urlx andWithTune: (NSString *)tunex;
{
	self=[super init];
	if (self)
	{
		self->url = [urlx copy];
		self->tune = [tunex copy];
	}
	return self;
}

// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	self->webView = [[[UIWebView alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame]] autorelease];
	webView.delegate = nil;
	NSURLRequest *request = [NSURLRequest requestWithURL:self->url];
	[webView loadRequest:request];
	NSString *tite = [NSString stringWithFormat:@"%C %@",0x266C,self->tune];
	self.navigationItem.titleView  = [[DataManager allocTitleView:tite] autorelease];	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"done" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	self.view = self->webView;
}



/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[self->url release];
	[self->tune release];
	if (self->webView) [self->webView release];
    [super dealloc];
}


@end
