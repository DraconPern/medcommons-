//
//  AppSettingsAppDelegate.m
//  AppSettings
//
//  Created by bill donner on 5/6/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import "AppSettingsAppDelegate.h"
#import "RootViewController.h"

@implementation AppSettingsAppDelegate


@synthesize window;
@synthesize rootViewController;


- (void)applicationDidFinishLaunching:(UIApplication *)application {
    
    [window addSubview:[rootViewController view]];
    [window makeKeyAndVisible];
}


- (void)dealloc {
    [rootViewController release];
    [window release];
    [super dealloc];
}

@end
