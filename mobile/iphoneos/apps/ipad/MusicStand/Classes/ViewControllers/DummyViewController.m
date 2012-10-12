    //
//  DummyViewController.m
//  MusicStand
//
//  Created by bill donner on 10/12/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "DummyViewController.h"


@implementation DummyViewController
-(id) init
{
	self = [super init];
	if (self)
	{
		self->tv = [[UITextView alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame]];
	}
	self.navigationItem.title = @"Coming Soon"; 
	return self;
}

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
 - (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
 if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
 // Custom initialization
 }
 return self;
 }
 */


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
	
	tv.delegate = nil;
	tv.text = @"Sorry, this is not yet implemented";
	tv.editable = NO;
	
	self.view = tv;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Overriden to allow any orientation.
    return YES;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
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
