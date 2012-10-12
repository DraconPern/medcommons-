    // this may no longer be needed now that search has been moved to its own tab

//  DecoratedListViewController.m
//  MusicStand
//
//  Created by bill donner on 11/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "DecoratedListViewController.h"
#import "SearchController.h"
#import "DataManager.h"
#import "DataStore.h"
#import "NSData+Base64.h"


@implementation DecoratedListViewController

- (void)mailComposeController:(MFMailComposeViewController*)controller  
          didFinishWithResult:(MFMailComposeResult)result 
                        error:(NSError*)error;
{
	if (result == MFMailComposeResultSent) {
		NSLog(@"It's away!");
	}
	[self dismissModalViewControllerAnimated:YES];
}
-(void) emailPressed
{
	MFMailComposeViewController* controller = [[[MFMailComposeViewController alloc] init] autorelease];
	controller.mailComposeDelegate = self;
	[controller setSubject:[NSString stringWithFormat:@"My Set List %d",self->tag]];

	
    NSString *plistPath;
	
	plistPath = [ [DataStore pathForSharedDocuments] stringByAppendingPathComponent:[NSString stringWithFormat:@"setlist%d.plist",self->tag]];
	
	
	if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
		NSLog(@"No plist today:=(");
		return;
	}
	NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
	[controller setMessageBody:[NSString stringWithFormat:@"\n\r%@\n\r%@\n\r%@\n\r%@\n\r\n\r%@\n\r%@\n\r%@\n\r",
@"Hello there.\r\nI'm sending you a setlist from MusicStand on my iPad so that we can play the same tunes.",
								@"\r\nThere are two ways to get this setlist into your own MusicStand:",
								@"\r\nTake the attachment and drag it thru iTunes along with music piles.",
@"Or you can cut and paste directly from this message into MusicStand.",
								@"--------BEGIN MUSICSTAND SETLIST-----",								
		[[NSData dataWithContentsOfFile:plistPath ] base64EncodedString],
								@"--------END MUSICSTAND SETLIST-----"
								] isHTML:NO]; 
	[controller addAttachmentData:plistXML mimeType:@"text/xml" fileName:[NSString stringWithFormat:@"SetList%d",self->tag]];
	[self presentModalViewController:controller animated:YES];

	
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


// If we have email, then put up a button
- (void)loadView {
	[super loadView];
	if ([MFMailComposeViewController canSendMail])
	{
	self.navigationItem.leftBarButtonItem =
	[[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemReply target:self action:@selector(emailPressed)] autorelease];
	}
	
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
 
    [super viewDidLoad];
 //
// self.navigationItem.leftBarButtonItem = 
// [[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSearch target:self action:@selector(searchpressed)] autorelease];
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
