//
//  VideoDevice.m
//  ForensicFoto
//
//  Created by bill donner on 9/23/09.
//  Copyright 2009 MedCommons,Inc. . All rights reserved.
//

#import "VideoDevice.h"
#import "MedCommons.h"
#import "PatientStore.h"
#import "DataManager.h"
@implementation VideoDevice
PatientStore *patientStore ;

- (void)video:(NSString *)videoPath didFinishSavingWithError:(NSError *)error contextInfo:(NSString *)contextInfo
{
	
	if (error) 
		
		CFShow([error localizedDescription]);
	else
	{
		NSLog(@"didFinishSavingWithError--videoPath in camera roll:%@",videoPath);
		NSLog(@"didFinishSavingWithError--videoPath in temp directory:%@",contextInfo);
		if (contextInfo)
		{
			// The thumbnail jpg should located in this directory.
	//		NSString *thumbnailDirectory = [[contextInfo stringByDeletingLastPathComponent] stringByDeletingLastPathComponent];
			
			// Debug info. list all files in the directory of the video file.
			// e.g. /private/var/mobile/Applications/D1E784A4-EC1A-402B-81BF-F36D3A08A332/tmp/capture
	//		NSLog([contextInfo stringByDeletingLastPathComponent]);
	//		NSLog([[[NSFileManager defaultManager] contentsOfDirectoryAtPath:[contextInfo stringByDeletingLastPathComponent] error:nil] description]);
			// Debug info. list all files in the parent directory of the video file, i.e. the "~/tmp" directory.
			// e.g. /private/var/mobile/Applications/D1E784A4-EC1A-402B-81BF-F36D3A08A332/tmp
	//		NSLog(thumbnailDirectory);
	//		NSLog([[[NSFileManager defaultManager] contentsOfDirectoryAtPath:thumbnailDirectory error:nil] description]);
			///////////////////
			
			// Find the thumbnail for the video just recorded.
			NSString *file,*latestFile;
			NSDate *latestDate = [NSDate distantPast];
			NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager] 
											  enumeratorAtPath:[[contextInfo stringByDeletingLastPathComponent]stringByDeletingLastPathComponent]];
			// Enumerate all files in the ~/tmp directory
			while (file = [dirEnum nextObject]) {
				// Only check files with jpg extension.
				if ([[file pathExtension] isEqualToString: @"jpg"]) {
					NSLog(@"***latestDate:%@",latestDate);
					NSLog(@"***file name:%@",file);
					NSLog(@"***NSFileSize:%@", [[dirEnum fileAttributes] valueForKey:@"NSFileSize"]);
					NSLog(@"***NSFileModificationDate:%@", [[dirEnum fileAttributes] valueForKey:@"NSFileModificationDate"]);
					// Check if current jpg file is the latest one.
					if ([(NSDate *)[[dirEnum fileAttributes] valueForKey:@"NSFileModificationDate"] compare:latestDate] == NSOrderedDescending){
						latestDate = [[dirEnum fileAttributes] valueForKey:@"NSFileModificationDate"];
						latestFile = file;
						NSLog(@"***latestFile changed:%@",latestFile);
					}
				}
			}
			// The thumbnail path.
			
			latestFile = [NSTemporaryDirectory() stringByAppendingPathComponent:latestFile];
			NSLog(@"****** The thumbnail file should be this one:%@",latestFile);
		}
		// Your code ...
		// Your code ...
		// Your code ...
	}
}


/*
 
 Simulator  - UIImagePickerControllerSourceTypeSavedPhotosAlbum
 
 iPod Touch - UIImagePickerControllerSourceTypeSavedPhotosAlbum
 
 iPhone 3G -  UIImagePickerControllerSourceTypeSavedPhotosAlbum, UIImagePickerControllerSourceTypePhotoLibrary, UIImagePickerControllerSourceTypeCamera - Photos Only
 
 iPhone 3GS - UIImagePickerControllerSourceTypeSavedPhotosAlbum, UIImagePickerControllerSourceTypePhotoLibrary, UIImagePickerControllerSourceTypeCamera - Photos and Video 
 
 
 
 */

- (BOOL) videoRecordingAvailable
{
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) return NO;
	return [[UIImagePickerController availableMediaTypesForSourceType:UIImagePickerControllerSourceTypeCamera] containsObject:@"public.movie"];
}

- (id) initWithCamera: (BOOL) wantscamera
{
	if (!(self = [super init])) return self;
	
	// if we have a camera then use it, otherwise, lets make sure this runs in the simulator
	//if ([UIImagePickerController isSourceTypeAvailable:SOURCETYPE])	
	if ([self videoRecordingAvailable])		self.sourceType = UIImagePickerControllerSourceTypeCamera; 
	    else self.sourceType =UIImagePickerControllerSourceTypePhotoLibrary;
	if (wantscamera == NO) self.sourceType =  UIImagePickerControllerSourceTypeSavedPhotosAlbum; 
	self.allowsEditing  = YES;// 
	self.delegate = self;
	self.videoQuality = UIImagePickerControllerQualityTypeMedium;
	self.videoMaximumDuration = 30.0f; // 30 seconds
	self.mediaTypes = [NSArray arrayWithObject:@"public.movie"];
	// ipc.mediaTypes = [NSArray arrayWithObjects:@"public.movie", @"public.image", nil];
	return self;
}
#pragma mark UIImagePickerControllerDelegate

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
	// recover video URL
	NSURL *url = [info objectForKey:UIImagePickerControllerMediaURL];
	
	NSDate *today = [NSDate date]; // get precise time of picture
	// check if video is compatible with album
	BOOL compatible = UIVideoAtPathIsCompatibleWithSavedPhotosAlbum([url path]);
	
    int nextind = [patientStore nextFreeIndex];
	NSInteger slot =  nextind + 1;
	// store thumbnail 
	
	//UIImage *image = [info objectForKey:UIImagePickerControllerEditedImage];
	UIImage *image = [UIImage imageNamed:@"qt.gif"];
	
	NSString *uniquePath;	
	NSString *photopath;
	photopath = [[[DataManager sharedInstance] ffPatientStore] findFreePatientStorePath];
	uniquePath = [NSHomeDirectory() stringByAppendingPathComponent:photopath];

	
	
	
	//CAM_LOG(@"********Saved thumbnail to %@ index %d***************",photoPath,nextind);
	NSData *png = (NSData *) UIImagePNGRepresentation(image);
	[png  writeToFile: uniquePath atomically:YES ];
	[[[DataManager sharedInstance] ffPatientStore] writePatientStore];
	NSInteger size = [png length];
	NSMutableDictionary *dict = (NSMutableDictionary *)[NSMutableDictionary dictionaryWithObjectsAndKeys:today,@"shoot-time",uniquePath,@"local-file",
														[NSString stringWithFormat:@"%d",size],@"size",
														[NSString stringWithFormat:@"%d",slot],@"slot",nil];
	//NSLog(@"-- %d Video Controller Dictionary",[dict retainCount]);
	[dict setValue:@"thumbnail" forKey:@"media-type"];
	[[[DataManager sharedInstance] ffPatientStore] setPhotoSpec: photopath atIndex: nextind withPhotoAttrs:dict ];
	
	// save
	if (compatible)
	{
		[dict setValue:@"video" forKey:@"media-type"];
		UISaveVideoAtPathToSavedPhotosAlbum([url path], self, @selector(video:didFinishSavingWithError:contextInfo:), NULL);
		[[[DataManager sharedInstance] ffPatientStore] setVideoSpec:   [NSString stringWithString:[url path]]
						  atIndex: (int) nextind 
				           withVideoAttrs:dict
		  ] ;
	}
//	[PC performSelector:@selector(CameraDone:) withObject:nil];
	CAM_LOG(@"**********************Saved video to %@***************",[url path]);
	
	[self dismissModalViewControllerAnimated:YES];
	[picker release];
}
@end


