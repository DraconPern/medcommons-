//
//  DashboardMainController.h
//  ForensicFoto
//
//  Created by bill donner on 9/13/09.
//  Copyright 2009 MedCommons,Inc. . All rights reserved.
//

@class MedCommonsAppDelegate,MCDashboardOuterController,
               PatientStore;

@interface DashboardMainController : UIViewController
{
	MCDashboardOuterController *mcDashboardOuterController;
	
	PatientStore *patientStore;
	NSDictionary *mc_json_response;
	NSString *lastValidMetaBoard;
	UIView *modalView;
		UITableViewController *realtablecontroller;
  
}
-(void)modalOn:(NSString *)s withDismissalAfter:(float_t)delta;
-(void)modalOff:(id)foo;
-(void) loadNextDashBoard;
-(int) howManyDashBoards;
-(DashboardMainController *) init;
@end
