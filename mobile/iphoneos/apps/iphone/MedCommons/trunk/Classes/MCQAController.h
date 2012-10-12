//
//  MCQAController.h
//  MedCommons
//
//  Created by bill donner on 1/17/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
@class DashboardPatient;
@interface MCQAController : UIViewController <UIActionSheetDelegate>{
	NSString *qastate; 
	NSString *theurl;
		int alert_state;
	UIActionSheet *actionSheet;
	DashboardPatient *wrapper;
	UIView *outerView;
			UIWebView *webView;

}

-(MCQAController *) init;
@end