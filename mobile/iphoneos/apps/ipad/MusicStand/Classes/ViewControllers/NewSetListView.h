//
//  NewSetListView.h
//  MusicStand
//
//  Created by bill donner on 11/10/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CreateNewSetListController.h"

@interface NewSetListView : UIView
{
	UILabel *errorLabelText;
	UIPickerView *setListPickerView;
	UITextField *setListNameField;
	CreateNewSetListController *delegateController;
}


-(NSString *) allocNewSetListNameFieldFromText;

-(id) initWithDelegateController: (CreateNewSetListController *) dg;
-(void) reload ;

-(void) clear;
-(void) signalError:(NSString *)errorstring;
@end
