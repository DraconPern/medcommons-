//
//  MCFaxController.h
//  MedCommons
//
//  Created by bill donner on 1/3/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//


@class DashboardPatient;

@interface MCFaxController : UITableViewController <UIActionSheetDelegate> {
	DashboardPatient *wrapper;

	int alert_state;
	UIActionSheet *actionSheet;
	UISwitch *showPinSwitcha, *showPinSwitchb;
	
}

- (id)init;
@end