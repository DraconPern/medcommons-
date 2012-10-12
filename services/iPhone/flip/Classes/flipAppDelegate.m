//
//  flipAppDelegate.m
//  flip
//
//  Created by bill donner on 5/16/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import "flipAppDelegate.h"
#import "MainViewController.h"

@implementation flipAppDelegate


@synthesize window;
@synthesize mainViewController;


- (void)applicationDidFinishLaunching:(UIApplication *)application {
    
	MainViewController *aController = [[MainViewController alloc] initWithNibName:@"MainView" bundle:nil];
	self.mainViewController = aController;
	[aController release];
	
    mainViewController.view.frame = [UIScreen mainScreen].applicationFrame;
	[window addSubview:[mainViewController view]];
    [window makeKeyAndVisible];
}


- (void)dealloc {
    [mainViewController release];
    [window release];
    [super dealloc];
}

@end
