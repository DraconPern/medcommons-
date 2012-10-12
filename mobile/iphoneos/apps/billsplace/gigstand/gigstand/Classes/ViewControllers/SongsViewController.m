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
#import "ArchivesManager.h"
#import "WebViewController.h"
#import "WebCaptureController.h"

@implementation SongsViewController
-(void) actionPressed
{        
    [self->toass showFromBarButtonItem: self.navigationItem.rightBarButtonItem animated: YES];
}
-(void) newPhotoContent:(UIActionSheet *) actionSheet
{
	
	UIImagePickerController *zvc = [[[UIImagePickerController alloc] init] autorelease];
	zvc.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
	zvc.delegate = self; // had to make this a UINavigationController delegate
	
	
	if (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad) 
		
	{
		[self presentModalViewController:zvc animated: YES];
	}
	else {
		
		popoverController = [[UIPopoverController alloc] initWithContentViewController: zvc] ;
		
		[popoverController presentPopoverFromRect: self.parentViewController.view.bounds
										   inView: self.view
						 permittedArrowDirections: UIPopoverArrowDirectionAny
										 animated: YES];
	}
	
}
-(void) newWebContent:(UIActionSheet *) actionSheet
{
	
	WebCaptureController *zvc = [[[WebCaptureController alloc] initWithMcid:@"!@#$%" ]	autorelease];
	
	zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
	
	[self presentModalViewController:nav animated: YES];
}
-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}
-(void) infoPressed;
{
	NSURL *splashURL = [NSURL URLWithString:@"http://s354932748.onlinehome.us/content-sites.html"];
	
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
	
	//NSLog (@"SVC all tunes initialization starting");
	
	self = [super  initWithArray:[TunesManager allTitles] 
                        andTitle:@"All Tunes" andArchive:nil];
	
	
	//NSLog (@"SVC all tunes initialization complete");

	
	return self;
	
}



// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	self.navigationItem.titleView  = [DataManager makeTitleView:@"All Songs"] ;	
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithTitle:@"done" style:UIBarButtonItemStyleBordered 
											  target:self 
											  action:@selector(donePressed)] autorelease];
	

	
	NSString *cancel =  (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)?@"Cancel":nil; // if on the iPhone
	
	self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"Grab Options", @"")
											  delegate:self
									 cancelButtonTitle:cancel
								destructiveButtonTitle:nil
									 otherButtonTitles:
				   NSLocalizedString (@"Grab Content From Photos", @""),
				   NSLocalizedString (@"Grab Content From Web", @""),
				   nil];
	
	
	
	self->toass.actionSheetStyle = UIActionSheetStyleBlackOpaque;
	self->toass.tag = 1;
	
	self.navigationItem.rightBarButtonItem = [[[UIBarButtonItem alloc]
											   initWithBarButtonSystemItem:UIBarButtonSystemItemAction
											   target:self
											   action:@selector(actionPressed)] autorelease];
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
    [toass release];
    [popoverController release];
    [super dealloc];
}

#pragma mark UIActionSheet delegate

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex 
{ 
	//	NSLog (@"OTV tag is %d button is %d",actionSheet.tag, buttonIndex);
	
	//	if (actionSheet.tag == 1) // this is invoked from the upper right corner
	//	{
	//		
	if (buttonIndex==1)
		
	{
		[self newWebContent:actionSheet];            
	}
	//		
	if (buttonIndex==0)
		//			
	{
		[self newPhotoContent:actionSheet];            
	}
	
}
#pragma mark UIImagePickerDelegate


-(void) imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
	UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];
	[ArchivesManager saveImageToOnTheFlyArchive:image];
	[self dismissModalViewControllerAnimated:YES];
}

@end
