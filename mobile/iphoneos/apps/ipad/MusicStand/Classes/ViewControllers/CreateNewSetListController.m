//
//  CreateNewSetListController.m
//  MusicStand
//
//  Created by bill donner on 11/10/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CreateNewSetListController.h"
#import "NewSetListView.h"
#import "DataStore.h"
#import "DataManager.h"

#define SETIMAGE(X) [(UIImageView *)self.view setImage:X];
@implementation CreateNewSetListController
- (void) dismissImagePickerController
{
	[self->popoverController dismissPopoverAnimated: YES];
	//[self->popoverController release];
}
- (NSString *) findUniqueSavePath
{
	int i = 1;
	NSString *path; 
	do {
		// iterate until a name does not match an existing file
	    path = [NSString stringWithFormat:@"%@/Documents/IMAGE_%04d.PNG", NSHomeDirectory(), i++];
	} while ([[NSFileManager defaultManager] fileExistsAtPath:path]);
	
	return path;
}
//
//- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
//{
//	UIImage *image = [info objectForKey:@"UIImagePickerControllerOriginalImage"];
//	//[self dismissModalViewControllerAnimated:YES];
//	[picker release];
//	
//	// Write to file
//	// [UIImageJPEGRepresentation(image, 1.0f) writeToFile:[self findUniqueSavePath] atomically:YES];
//	[UIImagePNGRepresentation(image) writeToFile:[self findUniqueSavePath] atomically:YES];
//	
//	// Set the background
////	SETIMAGE(image);
////	
////	// Show the current contents of the documents folder
////	CFShow([[NSFileManager defaultManager] directoryContentsAtPath:[NSHomeDirectory() stringByAppendingString:@"/Documents"]]);
//}
//
//// Provide 2.x compliance
//- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingImage:(UIImage *)image editingInfo:(NSDictionary *)editingInfo
//{
//	NSDictionary *dict = [NSDictionary dictionaryWithObject:image forKey:@"UIImagePickerControllerOriginalImage"];
//	[self imagePickerController:picker didFinishPickingMediaWithInfo:dict];
//}
//
//- (void) pickImage: (id) sender
//{
//	UIImagePickerController *ipc = [[[UIImagePickerController alloc] init] autorelease];
//	ipc.sourceType =  UIImagePickerControllerSourceTypePhotoLibrary;
//	ipc.delegate = self;
////	ipc.allowsImageEditing = NO;
//	//[self presentModalViewController:ipc animated:YES];	
//	self->popoverController = [[UIPopoverController alloc] initWithContentViewController: ipc];
//	[self->popoverController presentPopoverFromRect:self.view.frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
//}

-(id) init 
{
	self=[super init];
	if (self)
	{
	}
	return self;
}
-(void) newSetListButtonTouched
{
	NSString *stashed_string = [[nsv allocNewSetListNameFieldFromText] autorelease] ;
	
	if ([stashed_string length]<1) return;
	
	NSLog(@"Touched new Setlistbutton for %@", stashed_string);
	
	// check for duplicates
	for (NSString *s in self->pickerViewArray)
	{
		if ([s isEqualToString: stashed_string])
		{
			[nsv signalError:@"The list name is not unique, please re-enter"];
			validFieldEntered = NO;	
			return;
		}
	}
	
	
	validFieldEntered = YES;		
	[DataManager writeRefNodeItems:[NSArray array]  toPropertyList:stashed_string];  // empty plist is fine
	
	[self->pickerViewArray release];
	self->pickerViewArray = [self newListOfSetlists]; // should have new one now
	UIAlertView *av = [[[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%@ was created",stashed_string]
												  message:@"you can add more setlists now"
												 delegate: self
										cancelButtonTitle: @"OK"
										otherButtonTitles: nil] 
					   autorelease];
	[av show];
	[nsv clear];
	[nsv reload];
	
}
//-(void) newImageButtonTouched
//{
//	NSLog(@"Touched new Imagebutton");
//	[self pickImage: nil];
//	
//}

-(void) donePressed;
{
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}


-(void) prepareNavBar
{
	self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] 
											  initWithBarButtonSystemItem: UIBarButtonSystemItemDone
											  target:self 
											  action:@selector(donePressed)] autorelease];
	
	CGRect frame = CGRectMake(0, 0, 400, 44);
	if(label) [label release]; // can get called a bunch
	label = [[UILabel alloc] initWithFrame:frame] ;
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0f];
	label.shadowColor = [UIColor colorWithWhite:0.0f alpha:0.5f];
	label.textAlignment = UITextAlignmentCenter;
	label.textColor = [UIColor whiteColor];
	self.navigationItem.titleView = label;
	label.text = NSLocalizedString(@"Add More Lists", @"");
	
}
-(void) loadView
{
	
	[self prepareNavBar];
	self->pickerViewArray = [self newListOfSetlists];
	nsv = [[NewSetListView alloc] initWithDelegateController:self];
	self.view = nsv;
	
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
	[label release];
	[nsv release];
	//	[pickerViewArray release]; // might be leaking.... actually crashes if this is released
    [super dealloc];
}

// pickerview stuff
-(NSMutableArray *) newListOfSetlists
{
	NSMutableArray *alllists = [[NSMutableArray alloc] init];
	
	NSString *file;
	
	//NSLog (@"scanning for setlists in %@",[DataStore pathForTuneLists]);
	
	NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
									  enumeratorAtPath: [DataStore pathForTuneLists]];
	while ((file = [dirEnum nextObject]))
	{
		NSDictionary *attrs = [dirEnum fileAttributes];
		
		NSString *ftype = [attrs objectForKey:@"NSFileType"];
		if ([ftype isEqualToString:NSFileTypeRegular])
		{
			NSString *shortie = [file stringByDeletingPathExtension];
			if (!([shortie isEqualToString:@"alltunes"] ||[shortie isEqualToString:@"archives"]))
				[alllists addObject:shortie];
			//NSLog (@"added setlist %@",shortie);
		}
	}
	return alllists;
}
- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
	NSLog (@" in createnewsetlistcontroller pickerview didselectrow ");
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
	//	NSLog(@"picker row %d is %@",row,[pickerViewArray objectAtIndex:row]);
	
	return [pickerViewArray objectAtIndex:row];
	
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component
{
	CGFloat componentWidth = 768.0f;
	
	return componentWidth;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component
{
	return 50.0f;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
	
	//	
	//	NSLog(@"picker rows is %d",[pickerViewArray count]);
	return [pickerViewArray count];
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
	return 1;
}

#pragma mark UITextFieldDelegates
- (void)textFieldDidBeginEditing:(UITextField *)textField
{
	validFieldEntered = NO;
	
	[nsv signalError:@""];
	
	
	textField.text = @"";
	//if(savedTextField) [savedTextField release];
//	
//    savedTextField = [[NSMutableString alloc] initWithString:textField.text];
}

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
	
	return YES;
}
- (BOOL)textFieldShouldEndEditing:(UITextField *)textField
{
	return YES;
}
- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
	[textField resignFirstResponder]; // this gets rid of the keyboard	
	return YES;
}
- (BOOL)textFieldShouldClear:(UITextField *)textField
{
	//	NSLog (@"textFieldShouldClear tag %d text %@",[textField tag],[textField text]);for
	return YES;
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{    
	// check for duplicates
	for (NSString *s in self->pickerViewArray)
	{
		if ([s isEqualToString: textField.text])
		{
			[nsv signalError:@"That name is not unique, please re-enter"];
			
			[textField resignFirstResponder]; // this gets rid of the keyboard
			validFieldEntered = NO;	
			return;
		}
	}
	
	[textField resignFirstResponder]; // this gets rid of the keyboard
// FINISH UP WHEN THE BIG DOIT BUTTON IS PRESSED
	
//	savedTextField = [[NSMutableString alloc] initWithString:textField.text];
	
//	validFieldEntered = YES;	
}
#pragma mark -
//@optional
//
//// Called when the navigation controller shows a new top view controller via a push, pop or setting of the view controller stack.
//- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated;
//- (void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated;

@end
