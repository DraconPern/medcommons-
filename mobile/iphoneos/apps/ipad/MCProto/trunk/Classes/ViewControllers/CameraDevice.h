//
//  CameraDevice.h
//  MedCommons
//
//  Created by bill donner on 9/4/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CameraDevice : UIImagePickerController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>
{
    BOOL takingSubjectPicture;
}

- (id) initWithSubject: (BOOL) _sp
           wantsCamera: (BOOL) wantsCamera
             wantsRoll: (BOOL) wantsRoll;

@end
