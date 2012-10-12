//
//  CreateNewSetListController.h
//  MusicStand
//
//  Created by bill donner on 11/10/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
@class NewSetListView;

@interface CreateNewSetListController : UIViewController <UIPickerViewDataSource,UIPickerViewDelegate,UITextFieldDelegate
//,UIImagePickerControllerDelegate,UINavigationControllerDelegate
> {
	NewSetListView *nsv;
	NSMutableArray *pickerViewArray;
	//NSMutableString *savedTextField;
	BOOL validFieldEntered;
	UILabel *label;
	UIPopoverController		*popoverController;

}

-(NSMutableArray *) newListOfSetlists;

-(void) newSetListButtonTouched;

//-(void) newImageButtonTouched;
@end
