//
//  StartupController.m
//  MusicStand
//
//  Created by bill donner on 10/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "DataManager.h"
#import "StartupController.h"
#import "GigStandHomeController.h"
#import "ArchivesManager.h"
#import "TunesManager.h"
#import "ArchivesManager.h"

@implementation StartupController

-(void) finishSetup
{
	
	NSInteger acount = [ArchivesManager archivesCount];
	NSInteger tcount = [TunesManager tuneCount];
	NSInteger icount = [TunesManager instancesCount];
	
	NSLog(@"CoreData SQLite DB has %d archives with %d tunes from %d files",acount, tcount,icount);

	
		
	if (acount==0 )

		// if we've got nothing in the db then build a new one
		[ArchivesManager buildNewDB];
	
	else 
		
		[TunesManager updateGigBaseInfo]; // otherwise just flip around some times
	

	
	
	// finally lets start the main controller while dismissing anything that may be running
	
	
	self->fsc= [[[GigStandHomeController alloc] init ] retain];  // if these are released, the program hangs in startup
	
	self->fscn = [[[UINavigationController alloc] initWithRootViewController:fsc] retain]	;
	
	[self->zipalert dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->zipalert release];
	[self->window addSubview:self->fscn.view];	
	[self->window makeKeyAndVisible];
}

- (void)loadView {
 
	
	tv = [[UIView alloc] initWithFrame:self.parentViewController.view.bounds];//[[UIScreen mainScreen] applicationFrame]];
	
    tv.backgroundColor = [DataManager applicationColor];// [UIColor lightGrayColor] ; //
	
	UIImageView *iv = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"MusicStand_512x512.png"]] autorelease];

	iv.center = tv.center;

	[tv addSubview:iv];
	
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	self.navigationItem.titleView = [[DataManager allocAppTitleView:@"Starting..."] autorelease]; 

	self.view = tv;
	self.navigationController.navigationBar.hidden = YES;
	
	//return; // just quit here to take snapshots
	
	
	self->zipalert =  [[UIAlertView alloc] initWithTitle:@"Restoring Gig Stand ...."
												 message:nil delegate:nil 
									   cancelButtonTitle:nil
									   otherButtonTitles:nil] ;
	UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] 
										  initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
	
	// Adjust the indicator so it is up a few pixels from the bottom of the alert
	indicator.center = CGPointMake(zipalert.bounds.size.width / 2 +140 , zipalert.bounds.size.height /2 +70);
	[indicator startAnimating];
	[self->zipalert addSubview:indicator];
	[indicator release];
	[self->zipalert show];
	[NSTimer scheduledTimerWithTimeInterval: 0.01f target:self selector:@selector(finishSetup) userInfo:nil repeats:NO];
	
	
}

-(id) initWithWindow: (UIWindow *) win ;
{
	self = [super init];
	if (self)
	{
		self->window = win;
	}
	return self;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return NO;
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
	[tv release];
    [super dealloc];
}


@end
