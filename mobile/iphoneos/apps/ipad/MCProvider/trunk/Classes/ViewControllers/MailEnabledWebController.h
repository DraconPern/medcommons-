//
//  MailEnabledWebController.h
//  MCProvider
//
//  Created by bill donner on 12/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "WebViewController.h"

@interface MailEnabledWebController : WebViewController <MFMailComposeViewControllerDelegate> {
	
	MFMailComposeViewController *mailer;

}

@end
