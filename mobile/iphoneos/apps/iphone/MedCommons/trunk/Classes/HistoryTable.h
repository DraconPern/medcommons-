//
//  HistoryTable.h
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//


@class HistoryDetailsViewController,PatientStore;


@interface HistoryTable : UITableViewController {

	PatientStore *patientStore;
	NSMutableDictionary *imageCache;
}
-(void)  screenUpdate: (id) obj;
-(HistoryTable *) init;
@end