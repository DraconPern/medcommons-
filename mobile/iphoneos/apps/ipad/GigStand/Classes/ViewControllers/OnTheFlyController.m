    //
//  OnTheFlyController.m
//  GigStand
//
//  Created by bill donner on 2/17/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import "OnTheFlyController.h"
#import "ArchivesManager.h"
#import "AddressBarWebViewController.h"

@implementation OnTheFlyController

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
	
	AddressBarWebViewController *zvc = [[[AddressBarWebViewController alloc] initWithMcid:@"!@#$%" ]	autorelease];
	
	zvc.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
	UINavigationController *nav = [[[UINavigationController alloc] initWithRootViewController: zvc] autorelease];
	
	[self presentModalViewController:nav animated: YES];
}
-(void) actionPressed
{
	[self->toass showInView:self.view];
}

-(id) init 
{
	self = [super initWithArchive:[ArchivesManager nameForOnTheFlyArchive]] ;
	if (self)
	{
	}
	return self;
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

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

- (void)viewDidLoad {
    [super viewDidLoad];

	NSString *cancel =  (UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad)?@"Cancel":nil; // if on the iPhone
	
		self->toass = [[UIActionSheet alloc] initWithTitle: NSLocalizedString (@"OnTheFly Options", @"")
												  delegate:self
										 cancelButtonTitle:cancel
									destructiveButtonTitle:nil
										 otherButtonTitles:
					   NSLocalizedString (@"Grab Content From Photo Roll", @""),
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
