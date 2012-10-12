//
//  DashboardTableViewController.h
//  MedCommons
//
//  Created by bill donner on 12/27/09.
//  Copyright 2009 MedCommons,Inc. All rights reserved.
//

@class RESTComms,CustomViews,DashboardMainController,PatientStore;

@interface DashboardTableViewController : UITableViewController<UIActionSheetDelegate>  {

	NSMutableArray *patientsArray;
	NSMutableArray *sectionsArray;
	UILocalizedIndexedCollation *collation;
	BOOL everLoaded;
	RESTComms *llc;
	NSString *myAuth;
	NSString *practiceGroupName;
	int alert_state;
	UIActionSheet *actionSheet;
	CustomViews *customViews;
	PatientStore *patientStore;
	DashboardMainController *mainOuterController;
}
@property (nonatomic, retain) NSString *myAuth;

@property (nonatomic, retain) 	NSString *practiceGroupName;

@property (nonatomic, retain) NSMutableArray *patientsArray;

@property (nonatomic, retain) NSMutableArray *sectionsArray;

@property (nonatomic, retain) UILocalizedIndexedCollation *collation;

-(DashboardTableViewController *) init;


@end