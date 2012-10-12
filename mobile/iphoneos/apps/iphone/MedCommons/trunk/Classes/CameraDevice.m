//
//  CameraDevice.m
//  ForensicFotoZeroPointThree
//
//  Created by bill donner on 9/4/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//

#import "CameraDevice.h"
#import "MedCommons.h"
#import "PatientStore.h"
#import "DataManager.h"


/*
 
 Simulator  - UIImagePickerControllerSourceTypeSavedPhotosAlbum
 
 iPod Touch - UIImagePickerControllerSourceTypeSavedPhotosAlbum
 
 iPhone 3G -  UIImagePickerControllerSourceTypeSavedPhotosAlbum, UIImagePickerControllerSourceTypePhotoLibrary, UIImagePickerControllerSourceTypeCamera - Photos Only
 
 iPhone 3GS - UIImagePickerControllerSourceTypeSavedPhotosAlbum, UIImagePickerControllerSourceTypePhotoLibrary, UIImagePickerControllerSourceTypeCamera - Photos and Video 
 
 
 
 */


@implementation CameraDevice

- (id) initWithSubject: (BOOL) _sp wantsCamera: (BOOL) wantscamera 
		wantsRoll: (BOOL) wantsroll {
	if (!(self = [super init])) return self;
	
	// if we have a camera then use it, otherwise, lets make sure this runs in the simulator
	if ((wantscamera ==YES) &&([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]))
	self.sourceType = UIImagePickerControllerSourceTypeCamera; 
	else if (wantsroll == YES) 
		self.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum; 
	else self.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;

	self.allowsEditing  = YES; 	self.delegate = self;
	takingSubjectPicture = _sp; //if YES, always store into subjects photo slot
	return self;
}
#pragma mark UIImagePickerControllerDelegate//
//-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
//{
	// recover final image after editing or compression - the original image is very large
//	UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];
//		UIImage *imageedit = [info objectForKey:UIImagePickerControllerEditedImage];
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingImage:(UIImage *)image editingInfo:(NSDictionary *)editingInfo 
{
	NSDate *today = [NSDate date]; // get precise time of picture
	int nextind = [[[DataManager sharedInstance] ffPatientStore] nextFreeIndex]; // where's this going?
	NSInteger slot;
	NSString *uniquePath;
	NSString *photopath;
	if (takingSubjectPicture == YES)  
	{
		photopath = [[[DataManager sharedInstance] ffPatientStore] newSubjectPhotoSpec];
		uniquePath = [NSHomeDirectory() stringByAppendingPathComponent:photopath];
		slot=0;
	}
	else
	{
		
		photopath = [[[DataManager sharedInstance] ffPatientStore] findFreePatientStorePath];
		uniquePath = [NSHomeDirectory() stringByAppendingPathComponent:photopath];
		slot=nextind+1;
	}	
	
	NSData *png = (NSData *) UIImagePNGRepresentation(image);
	NSInteger size = [png length];	
	NSMutableDictionary *dict = (NSMutableDictionary *)[NSMutableDictionary dictionaryWithObjectsAndKeys:today,@"shoot-time",uniquePath,@"local-file",
														[NSString stringWithFormat:@"%d",size],@"size",[NSString stringWithFormat:@"%d",slot],@"slot",nil];
    [dict setValue:@"photo" forKey:@"media-type"];
	
	//NSLog(@"-- %d Camera Controller Dictionary",[dict retainCount]);
	[png  writeToFile: uniquePath atomically:YES ];	
	[[[DataManager sharedInstance] ffPatientStore] writePatientStore];// snap it to disk
	CAM_LOG(@"****Saved %d bytes of photo to %@ index %d***************",png.length, photopath,nextind);
	if (takingSubjectPicture==YES) [[[DataManager sharedInstance] ffPatientStore] setSubjectPhotoSpec: photopath  withPhotoAttrs:dict];
		
	else [[[DataManager sharedInstance] ffPatientStore] setPhotoSpec:photopath atIndex: (int) nextind withPhotoAttrs:dict];
	
	[[self parentViewController] dismissModalViewControllerAnimated:YES];   // delay
	[picker release];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
	[[self parentViewController] dismissModalViewControllerAnimated:YES];
	[picker release];
	
}
@end

