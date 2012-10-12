    //
//  LoggingViewController.m
//  GigStand
//
//  Created by bill donner on 12/26/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//

#import "LoggingViewController.h"
#import "DataManager.h"
#import "LogManager.h"

@interface LoggingViewController ()
-(void) loadView;
@end

@implementation LoggingViewController
-(void) pressedClear
{
	[LogManager clearCurrentLog];

	
	[self loadView]; // build it again, check leakage
}

-(id) init;
{
	self = [super init];
	if (self) 
	{
		
	}
	return self;
}

-(void) loadView
{
	CGRect frame = self.parentViewController.view.bounds; //[[UIScreen mainScreen] applicationFrame];
//	frame.size.height -= [DataManager navBarHeight]; //NAV_BAR_HEIGHT;
//	frame.origin.y += [DataManager navBarHeight];
	self.view = [[UIView alloc] initWithFrame: frame] ;
	self.view.backgroundColor = [DataManager applicationColor];
	self.navigationItem.titleView = [DataManager makeTitleView:@"Trace"];
	self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Clear" 
																			  style:UIBarButtonItemStyleBordered
																			 target: self 
																			 action: @selector (pressedClear)];
	NSError *error;
	NSStringEncoding encoding;
	NSString *contents = [NSString stringWithContentsOfFile:[LogManager pathForCurrentLog]
											   usedEncoding:&encoding error: &error];

	UITextView *ltv = [[[UITextView alloc] initWithFrame: frame] autorelease];
	ltv.editable = NO;
//	if (![contents isEqualToString:ltv.text ])
//	{
	[ltv setText:contents];

		ltv.contentOffset = CGPointMake(0.0f, 
										MAX(ltv.contentSize.height - ltv.frame.size.height, 0.0f));
		
//	}
	
	[self.view 	addSubview: ltv];
}

-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	NSLog (@"TRC didRotateFromInterfaceOrientation");
	[self loadView]; // build it again, check leakage
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
	[self.view release]; // get rid of the outer UIView, but dont release the text view because we still write to it
    [super dealloc];
}


@end
