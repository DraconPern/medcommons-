//
//  StartupController.m
//  MusicStand
//
//  Created by bill donner on 10/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//




//#import "DocumentsManager.h"
#import "DataManager.h"
#import "SettingsManager.h"
#import "SettingsViewController.h"
#import "StartupController.h"
#import "AllSetListsController.h"
#import "AllTunesViewController.h"
#import "FrontSearchController.h"
#import "Product.h"

// shud figure out how to commit suicide 
@interface UITabBarController (private)
- (UITabBar *)tabBar;
@end
@interface CustomUITabBarController: UITabBarController;
@end

@implementation CustomUITabBarController


- (void)viewDidLoad {
    [super viewDidLoad];
	
    CGRect frame = CGRectMake(0.0f, 0.0f, 1024.f, 49.f); // make it big enough so it can rotate safely
    UIView *v = [[UIView alloc] initWithFrame:frame];
    [v setBackgroundColor: [DataManager sharedInstance].appColor ] ;
    //[v setAlpha:0.5f];
    [[self tabBar] insertSubview: v atIndex:0];
    [v release];
	
}
@end

@implementation StartupController


-(void) finishSetup
{
	
	[DataManager onceOnlyMasterIndexInitialization];
	
	NSInteger countFiles = [DataManager recoverDB];
	NSLog(@"recoverDB counted %d files",countFiles);
	
	if (countFiles==0 )
		
	{
		[[DataManager sharedInstance ] buildNewDB];	
		
	}
	[DataManager finishDBSetup];
	

	
	
	//tabBarController = [[CustomUITabBarController alloc] init];
	
	
	self->fsc= [[[FrontSearchController alloc] init ] retain];  // if these are released, the program hangs in startup
	
	self->fscn = [[[UINavigationController alloc] initWithRootViewController:fsc] retain]	;
	
	
	
	//[self.view removeFromSuperview];//?  leave this in the background for interesting effects
	//
	
	[self->zipalert dismissWithClickedButtonIndex:-1 animated:NO];// TALK TO JOHN ABOUT THIS
	[self->zipalert release];
	[self->window addSubview:self->fscn.view];	
	[self->window makeKeyAndVisible];
}

- (void)loadView {

	
	
    SettingsManager *settings = [SettingsManager sharedInstance];
    NSLog (@"> %@ %@",
           settings.applicationName,
           settings.applicationVersion,
           settings.appliance);
	
	tv = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame]];
	
    tv.backgroundColor =  [DataManager sharedInstance].appColor;
	
	UIImageView *iv = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"MusicStand_512x512.png"]] autorelease];
	iv.center = tv.center;
	[tv addSubview:iv];
	
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	self.navigationController.navigationBar.translucent = YES;
	self.navigationItem.title = @"Gig Stand"; 
	self->countup = 0;
	self.view = tv;
	
	//[[DataManager sharedInstance] dismissZipExpansionWaitIndicators]; // turn off signalling
	self->zipalert =  [[UIAlertView alloc] initWithTitle:@"Restoring Gig Stand ...."
												 message:nil delegate:nil 
									   cancelButtonTitle:nil
									   otherButtonTitles:nil] ;
	UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
	
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
/*
 // Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
 - (void)viewDidLoad {
 [super viewDidLoad];
 }
 */


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
