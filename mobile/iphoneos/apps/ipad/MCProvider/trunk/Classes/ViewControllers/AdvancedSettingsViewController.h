//
//  AdvancedSettingsViewController.h
//  MCProvider
//
//  Created by Bill Donner on 5/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AdvancedSettingsViewController : UIViewController
{
@private

    UIBarButtonItem *backButton_;
	
	UIActionSheet   *actionSheet_;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

@end
