//
//  MCSOAPController.h
//  MedCommons
//
//  Created by bill donner on 1/20/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//



@class DashboardPatient;
@interface MCSOAPController : UIViewController <UIActionSheetDelegate,UITextViewDelegate>
{
	
	NSString *theurl;
	NSString *panetitle;
	int alert_state;
	UIActionSheet *actionSheet;

	DashboardPatient *wrapper;
	UIView *outerView;
	UILabel *labelNotes;
	UITextView *textFieldNotesA,*textFieldNotesP,*textFieldNotesS,*textFieldNotesD,*textFieldNotesC;

	UIToolbar *rightTools,*leftTools;
		UITextView *savingTextView;
	
}

-(MCSOAPController *) init ;
@end