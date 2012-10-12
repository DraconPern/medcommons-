//
//  AddTuneToSetListController.h
//  MusicStand
//
//  Created by bill donner on 11/9/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//


#import <UIKit/UIKit.h>

@interface AddTuneToSetListController : UIViewController <UIPickerViewDelegate, UIPickerViewDataSource, UITextViewDelegate>
{
	UIPickerView		*myPickerView;
		NSMutableArray				*pickerViewArray;
	NSArray *incomingArray;
	
	UILabel				*label;
	NSMutableString		*chosenList;
		
	UIView				*currentPicker;
	NSString			*maintitle;
}

@property (nonatomic, retain) UIPickerView *myPickerView;
@property (nonatomic, retain) NSArray *pickerViewArray;

@property (nonatomic, retain) UILabel *label;

@property (nonatomic, retain) UIView *currentPicker;


@end
