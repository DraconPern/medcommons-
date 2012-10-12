    //
//  ViewSourceController.m
//  MusicStand
//
//  Created by bill donner on 10/12/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "ViewSourceController.h"
#import "DataStore.h"


@implementation ViewSourceController

-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

-(id) initWithPath:(NSString *)path
{
	self = [super init];
	if (self)
	{
	self->path_ = path;
	}
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
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
		self.navigationController.navigationBar.translucent = YES;
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemDone
											  target:self 
											  action:@selector(donePressed)] autorelease];
	NSError *error;
	NSStringEncoding encoding;
	NSString *filespec = [NSString stringWithFormat:@"%@/%@",[DataStore pathForSharedDocuments],self->path_];
	NSString *filecontents = [NSString stringWithContentsOfFile:filespec usedEncoding:&encoding error: &error];
	//NSLog(@"viewsource encoding %d error %@",encoding, error);
	
	UITextView *tv = [[[UITextView alloc] initWithFrame:[[UIScreen mainScreen] applicationFrame]] autorelease];
	self.navigationItem.title = [NSString stringWithFormat: @"%@ - %d bytes", self->path_,[filecontents length]]; 
	tv.delegate = nil;
	tv.text = filecontents;
	tv.editable = NO;
	
	self.view = tv;
	
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
    
    // Release any cached data, images, etc that aren't in use.
}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	[self->path_ release];
    [super dealloc];
}


@end
