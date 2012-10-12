//
//  MainInfoController.h
//  ForensicFoto
//
//  Created by bill donner on 9/9/09.
//  Copyright 2009 MEDCOMMONS, INC.. All rights reserved.
//

@class PatientStore,CustomViews,RESTComms;
@interface MCUploadController : UIViewController <UIActionSheetDelegate>
{
	
	UIProgressView *progressView;
	NSTimer *ticktimer;

	float progresspct;
	BOOL isUploading;
	BOOL showDashBoardLink;	
	int alert_state;
	NSString *lastpostresponseline;	
	UIActionSheet *actionSheet,*uploadActionSheet;
	UIButton *cButton;
	UIButton *subjectImageButton;
CustomViews *customViews;
	
	NSMutableDictionary *generalAttrs;
	NSString *successImage,*failureImage;
	
	NSString *title;
	UILabel *labelTitle;
	PatientStore *patientStore;

	UILabel *topSplash,*middleSplash;
	UIImageView *imageView;
	NSString *ffInfoTitle,*ffInfoMsgTop,*ffInfoMsgMiddle;
	
	UIView *outerView;


	UITextView *textview;
	UIImageView *imageview;
	NSString *textToDisplay;
	CGRect  touchFrame;
	CGRect pictureframe;
	int disableTouchesSemaphore;
	BOOL validFieldEntered;
	
	UILabel *tLabel;	UILabel *aLabel;UILabel *bLabel;UILabel *nLabel;	UILabel *cLabel;	UIButton *aButton ;	UIButton *bButton ;
}
-(void)incrementProgressBar:(float)_progress;
-(MCUploadController *) initWithTitle:(NSString *)_titl
andWithTop:(NSString *)_top
andWithMiddle:(NSString *)_middle
andWithSuccess:(NSString *)_success
andWithFailure:(NSString *)_failure;




@end