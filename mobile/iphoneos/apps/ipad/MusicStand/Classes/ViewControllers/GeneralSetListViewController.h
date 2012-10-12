//
//  GeneralSetListViewController.h
//  MusicStand
//
//  Created by bill donner on 11/5/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "GeneralListViewController.h"

#import <MessageUI/MFMailComposeViewController.h>

@interface GeneralSetListViewController : GeneralListViewController <MFMailComposeViewControllerDelegate,UIActionSheetDelegate,UIPrintInteractionControllerDelegate> {
	BOOL canShare;
	UIActionSheet *toass;
	MFMailComposeViewController* controller;


}
-(void) emailPressed;
@end
