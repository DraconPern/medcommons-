//
//  MoreController.h
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//


@class DashboardPatient,CustomViews,PatientStore;

@interface MoreController : UITableViewController <UIActionSheetDelegate> {

	DashboardPatient *wrapper;
	int alert_state;
	UIActionSheet *actionSheet;
	PatientStore *patientStore;
	CustomViews *customViews;
	
}



- (id)init;

@end
