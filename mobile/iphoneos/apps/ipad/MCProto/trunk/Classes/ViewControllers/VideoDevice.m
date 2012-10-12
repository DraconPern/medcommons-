//
//  VideoDevice.m
//  MedCommons
//
//  Created by bill donner on 9/23/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

#import "DataManager.h"
#import "ShooterStorageConnector.h"
#import "VideoDevice.h"

@interface VideoDevice ()

- (BOOL) videoRecordingAvailable;

@end

@implementation VideoDevice

- (id) initWithCamera: (BOOL) wantsCamera
{
    if (self = [super init])
    {
        //
        // If we have a camera then use it; otherwise, let's make sure this
        // runs in the simulator:
        //
        if ([self videoRecordingAvailable])
            self.sourceType = UIImagePickerControllerSourceTypeCamera;
        else
            self.sourceType =UIImagePickerControllerSourceTypePhotoLibrary;

        if (!wantsCamera)
            self.sourceType =  UIImagePickerControllerSourceTypeSavedPhotosAlbum;

        self.allowsEditing  = YES;
        self.delegate = self;
        self.mediaTypes = [NSArray arrayWithObject: @"public.movie"];
        self.videoMaximumDuration = 30.0f;  // 30 seconds
        self.videoQuality = UIImagePickerControllerQualityTypeMedium;
    }

    return self;
}

- (void) video: (NSString *) videoPath
didFinishSavingWithError: (NSError *) error
   contextInfo: (NSString *) contextInfo
{
    if (error)
        CFShow ([error localizedDescription]);
    else
    {
        NSLog(@"didFinishSavingWithError--videoPath in camera roll: %@", videoPath);
        NSLog(@"didFinishSavingWithError--videoPath in temp directory: %@", contextInfo);

        if (contextInfo)
        {
            // The thumbnail jpg should located in this directory.
            //      NSString *thumbnailDirectory = [[contextInfo stringByDeletingLastPathComponent] stringByDeletingLastPathComponent];

            // Debug info. list all files in the directory of the video file.
            // e.g. /private/var/mobile/Applications/D1E784A4-EC1A-402B-81BF-F36D3A08A332/tmp/capture
            //      NSLog([contextInfo stringByDeletingLastPathComponent]);
            //      NSLog([[[NSFileManager defaultManager] contentsOfDirectoryAtPath:[contextInfo stringByDeletingLastPathComponent] error:nil] description]);
            // Debug info. list all files in the parent directory of the video file, i.e. the "~/tmp" directory.
            // e.g. /private/var/mobile/Applications/D1E784A4-EC1A-402B-81BF-F36D3A08A332/tmp
            //      NSLog(thumbnailDirectory);
            //      NSLog([[[NSFileManager defaultManager] contentsOfDirectoryAtPath:thumbnailDirectory error:nil] description]);
            ///////////////////

            // Find the thumbnail for the video just recorded.
            NSDate   *latestDate = [NSDate distantPast];
            NSString *latestFile;
            NSString *file;
            NSDirectoryEnumerator *dirEnum = [[NSFileManager defaultManager]
                                              enumeratorAtPath: [[contextInfo stringByDeletingLastPathComponent] stringByDeletingLastPathComponent]];

            // Enumerate all files in the ~/tmp directory
            while (file = [dirEnum nextObject])
            {
                // Only check files with jpg extension.
                if ([[file pathExtension] isEqualToString: @"jpg"])
                {
                    NSDictionary *attrs = [dirEnum fileAttributes];

                    NSLog (@"***latestDate:%@", latestDate);
                    NSLog (@"***file name:%@", file);
                    NSLog (@"***NSFileSize:%@", [attrs valueForKey: @"NSFileSize"]);
                    NSLog (@"***NSFileModificationDate:%@", [attrs valueForKey: @"NSFileModificationDate"]);

                    // Check if current jpg file is the latest one.
                    if ([(NSDate *) [attrs valueForKey:@"NSFileModificationDate"] compare: latestDate] == NSOrderedDescending)
                    {
                        latestDate = [attrs valueForKey: @"NSFileModificationDate"];
                        latestFile = file;

                        NSLog(@"***latestFile changed:%@", latestFile);
                    }
                }
            }
            // The thumbnail path.

            latestFile = [NSTemporaryDirectory() stringByAppendingPathComponent: latestFile];

            NSLog(@"****** The thumbnail file should be this one:%@", latestFile);
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
    if (![UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera])
        return NO;

    return [[UIImagePickerController availableMediaTypesForSourceType: UIImagePickerControllerSourceTypeCamera] containsObject: @"public.movie"];
}

#pragma mark UIImagePickerControllerDelegate

- (void) imagePickerController: (UIImagePickerController *) picker
 didFinishPickingMediaWithInfo: (NSDictionary *) info
{
    ShooterStorageConnector *storage = [[DataManager sharedInstance] ffPatientStore];
    // recover video URL
    NSURL     *url = [info objectForKey: UIImagePickerControllerMediaURL];
    NSDate    *today = [NSDate date]; // get precise time of picture
                                      // check if video is compatible with album
    BOOL       compatible = UIVideoAtPathIsCompatibleWithSavedPhotosAlbum ([url path]);
    NSInteger  nextIdx = [storage nextFreeIndex];
    NSInteger  slot =  nextIdx + 1;

    //
    // Create thumbnail:
    //
    //  UIImage *image = [info objectForKey: UIImagePickerControllerEditedImage];
    UIImage  *image = [UIImage imageNamed: @"qt.gif"];
    NSString *uniquePath;
    NSString *photoPath;

    photoPath = [storage findFreePatientStorePath];
    uniquePath = [NSHomeDirectory () stringByAppendingPathComponent: photoPath];

    NSData *png = (NSData *) UIImagePNGRepresentation (image);

    [png writeToFile: uniquePath
          atomically: YES ];

    [storage writePersonStore];

    NSInteger            size = [png length];
    NSMutableDictionary *dict = (NSMutableDictionary *) [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                                         today, @"shoot-time",
                                                         uniquePath, @"local-file",
                                                         [NSString stringWithFormat: @"%d", size], @"size",
                                                         [NSString stringWithFormat: @"%d", slot], @"slot",
                                                         nil];

    //
    // Save thumbnail:
    //
    [dict setValue: @"thumbnail"
            forKey: @"media-type"];

    [storage setPhotoSpec: photoPath
                  atIndex: nextIdx
           withPhotoAttrs: dict];

    //
    // Save video:
    //
    if (compatible)
    {
        [dict setValue: @"video"
                forKey: @"media-type"];

        UISaveVideoAtPathToSavedPhotosAlbum ([url path],
                                             self,
                                             @selector (video:didFinishSavingWithError:contextInfo:),
                                             NULL);

        [storage setVideoSpec: [NSString stringWithString: [url path]]
                      atIndex: nextIdx
               withVideoAttrs: dict];
    }

    CAM_LOG (@"**********************Saved video to %@***************", [url path]);

    [self dismissModalViewControllerAnimated: YES];
}

@end
