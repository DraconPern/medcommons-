//
//  ShooterController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ShooterController : UIViewController
{
@private

    UIActionSheet       *actionSheet_;
    UIBarButtonItem     *backButton_;
    NSUInteger           disableTouchesSemaphore_;
    UIBarButtonItem     *doneButton_;
    NSUInteger           partPhotoIndex_;
    UIPopoverController *popoverController_;
    CGRect               tappedRect_;
    //
    // Flags:
    //
    BOOL                 replacePhoto_;
    BOOL                 subjectPhoto_;
    BOOL                 usePopover_;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

@end
