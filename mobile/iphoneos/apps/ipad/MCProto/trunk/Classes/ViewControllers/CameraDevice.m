//
//  CameraDevice.m
//  MedCommons
//
//  Created by bill donner on 9/4/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

#import "CameraDevice.h"
#import "DataManager.h"
#import "ShooterStorageConnector.h"

/*

 Simulator  - UIImagePickerControllerSourceTypeSavedPhotosAlbum

 iPod Touch, iPad - UIImagePickerControllerSourceTypeSavedPhotosAlbum

 iPhone 3G -  UIImagePickerControllerSourceTypeSavedPhotosAlbum, UIImagePickerControllerSourceTypePhotoLibrary, UIImagePickerControllerSourceTypeCamera - Photos Only

 iPhone 3GS - UIImagePickerControllerSourceTypeSavedPhotosAlbum, UIImagePickerControllerSourceTypePhotoLibrary, UIImagePickerControllerSourceTypeCamera - Photos and Video

 */

@implementation CameraDevice

- (id) initWithSubject: (BOOL) _sp
           wantsCamera: (BOOL) wantsCamera
             wantsRoll: (BOOL) wantsRoll
{
    if (self = [super init])
    {
        takingSubjectPicture = _sp; //if YES, always store into subjects photo slot

        //
        // If we have a camera then use it; otherwise, let's make sure this
        // runs in the simulator:
        //
        if (wantsCamera &&
            [UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera])
            self.sourceType = UIImagePickerControllerSourceTypeCamera;
        else if (wantsRoll)
            self.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
        else
            self.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;

        self.allowsEditing = YES;
        self.delegate = self;
    }

    return self;
}

#pragma mark UIImagePickerControllerDelegate

//-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
//{
// recover final image after editing or compression - the original image is very large
//  UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];
//      UIImage *imageedit = [info objectForKey:UIImagePickerControllerEditedImage];

- (void) imagePickerController: (UIImagePickerController *) picker  // DEPRECATED !!!
         didFinishPickingImage: (UIImage *) image
                   editingInfo: (NSDictionary *) editingInfo
{
    ShooterStorageConnector *storage = [[DataManager sharedInstance] ffPatientStore];
    NSInteger                nextIdx = [storage nextFreeIndex]; // where's this going?
    NSDate                  *today = [NSDate date]; // get precise time of picture
    NSString                *uniquePath;
    NSString                *photoPath;
    NSInteger                slot;

    if (takingSubjectPicture)
    {
        photoPath = [storage newSubjectPhotoSpec];
        uniquePath = [NSHomeDirectory () stringByAppendingPathComponent: photoPath];
        slot = 0;
    }
    else
    {
        photoPath = [storage findFreePatientStorePath];
        uniquePath = [NSHomeDirectory () stringByAppendingPathComponent: photoPath];
        slot = nextIdx + 1;
    }

    NSData              *png = (NSData *) UIImagePNGRepresentation (image);
    NSInteger            size = [png length];
    NSMutableDictionary *dict = (NSMutableDictionary *) [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                                         today, @"shoot-time",
                                                         uniquePath, @"local-file",
                                                         [NSString stringWithFormat: @"%d", size], @"size",
                                                         [NSString stringWithFormat: @"%d", slot], @"slot",
                                                         nil];

    [dict setValue: @"photo"
            forKey: @"media-type"];

    [png writeToFile: uniquePath
          atomically: YES];

    [storage writePersonStore];

    CAM_LOG (@"****Saved %d bytes of photo to %@ index %d***************", png.length, photoPath, nextIdx);

    if (takingSubjectPicture)
        [storage setSubjectPhotoSpec: photoPath
                      withPhotoAttrs: dict];
    else
        [storage setPhotoSpec: photoPath
                      atIndex: nextIdx
               withPhotoAttrs: dict];

    [[self parentViewController] dismissModalViewControllerAnimated: YES];
}

- (void)imagePickerControllerDidCancel: (UIImagePickerController *) picker
{
    [[self parentViewController] dismissModalViewControllerAnimated: YES];
}

@end
