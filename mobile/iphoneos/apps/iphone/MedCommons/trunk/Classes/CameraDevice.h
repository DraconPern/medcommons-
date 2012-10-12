//
//  CameraDevice.h
//  ForensicFotoZeroPointThree
//
//  Created by bill donner on 9/4/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//
@class PatientStore;
@interface CameraDevice : UIImagePickerController <UIImagePickerControllerDelegate, UINavigationControllerDelegate>
{
	UIViewController *PC;   // set by App delegate needed by ImageController
PatientStore *patientStore ;
BOOL takingSubjectPicture;
}
- (id) initWithSubject: (BOOL) _sp wantsCamera: (BOOL) wantscamera 
			 wantsRoll: (BOOL) wantsroll  ;

//-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info;

-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingImage:(UIImage *)image editingInfo:(NSDictionary *)editingInfo;
@end