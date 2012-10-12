//
//  MCShooterController.h
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//


	@class CustomViews,DashboardPatient,PatientStore;
	@interface MCShooterController : UIViewController <UITextFieldDelegate,UITextViewDelegate,UIActionSheetDelegate>
	{	
		UITextView *savingTextView;
		NSMutableArray *tinyPics;
		UIImageView *imageView;
		int alert_state;
		DashboardPatient *wrapper;
		UIActionSheet *actionSheet,*uploadActionSheet;
		UIView *outerView;
		UIImageView *helpView;
		CustomViews *customViews;
		UIButton *subjectImageButton;
		NSString *title;
		UILabel *labelTitle;
		PatientStore *patientStore;
		CGRect  touchFrame;
		CGRect pictureframe;
		CGRect toggleFrame ;
		CGRect iconFrame;
		int disableTouchesSemaphore;
		BOOL validFieldEntered;
	
	}
	- (void) subjectScreenUpdate: (NSTimer *) Timer;
	-(BOOL)dispatchTouchEndEvent:(UIView *)theView toPosition:(CGPoint)position;
	
-(void) switchToWideview :(int) partnum;
-(MCShooterController *) init;
	@end
