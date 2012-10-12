    //
//  SongsViewController.m
//  GigStand
//
//  Created by bill donner on 12/25/10.
//  Copyright 2010 gigstand.net. All rights reserved.
//
#import "ArchiveViewController.h"
#import "DataManager.h"
#import "ArchivesManager.h"

#import "TunesManager.h"
@implementation ArchiveViewController
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}

-(id) initWithArchive :(NSString *)archive;
{	
	NSLog (@"AVC large archive %@ initialization starting",archive);
	
	NSMutableArray *archiveItems =   [[[NSMutableArray alloc ] init ] autorelease];
	
	// poor man's cache - check cache dictionary for existence of archive entry and with valid timestamp - use that in preference to
	// this: which reloads everything thru sqlite
	[archiveItems	addObjectsFromArray:[TunesManager allTitlesFromArchive: archive]];
	[archiveItems sortUsingSelector:@selector(compare:)];
	
	self = [super  initWithArray:archiveItems  andTitle:[NSString stringWithFormat:@":%@",[ArchivesManager shortName:archive]] andArchive:archive];		
	
	NSLog (@"AVC large archive %@ initialization complete",archive);
	
	return self;
	
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"Home" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	NSLog (@"AVC finished viewDidLoad");
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
