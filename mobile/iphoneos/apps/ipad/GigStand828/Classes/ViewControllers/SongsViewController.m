    //
//  SongsViewController.m
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//
#import "SongsViewController.h"
#import "DataManager.h"
#import "TunesManager.h"

#import "WebViewController.h"


@implementation SongsViewController
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}
-(void) infoPressed;
{
	NSURL *splashURL = [NSURL URLWithString:@"http://gigstand.net"];
	
	if (splashURL)
	{
		WebViewController *wvc = [[[WebViewController alloc]
								   initWithURL: splashURL]
								  autorelease];
		
		//wvc.navigationItem.hidesBackButton = NO;
		
		// take title from environment variable
		wvc.title = NSLocalizedString (@"gigstand.net", @"");
		
		UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: wvc] autorelease];
		
		[self presentModalViewController:nav animated: YES];
		
	}
}
-(id) init 
{
	
	// dynamically build the array of items  of RefNodesin this archiv
	
	NSLog (@"SVC all tunes initialization starting");
	
	self = [super  initWithArray:[TunesManager allTitles] andTitle:@"All Songs" andArchive:nil];
	
	
	NSLog (@"SVC all tunes initialization complete");

	
	return self;
	
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	self.navigationItem.titleView  = [[DataManager allocTitleView:@"All Songs"] autorelease];	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"Home" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"www" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(infoPressed)] autorelease];
	
	
	NSLog (@"SVC finished viewDidLoad");
}



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
    [super dealloc];
}


@end
