//
//  MCPatientController.h
//  MedCommons
//
//  Created by bill donner on 12/28/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

@class DashboardPatient;

@interface MCPatientController : UITableViewController <UIActionSheetDelegate> {

	DashboardPatient *wrapper;

	int alert_state;
	UIActionSheet *actionSheet;
	
}

- (id)init;
@end
