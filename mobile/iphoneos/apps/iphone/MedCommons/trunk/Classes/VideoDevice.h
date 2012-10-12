//
//  VideoDevice.h
//  ForensicFoto
//
//  Created by bill donner on 9/23/09.
//  Copyright 2009 MedCommons,Inc. . All rights reserved.
//



@interface VideoDevice : UIImagePickerController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>
{

}- (id) initWithCamera: (BOOL) wantscamera;
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info;
@end

