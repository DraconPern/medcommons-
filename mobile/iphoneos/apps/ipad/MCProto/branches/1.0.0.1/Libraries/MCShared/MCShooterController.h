//
//  MCShooterController.h
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CustomViews;
@class ShooterStorageConnector;

typedef enum
{
    MCShooterAlertStateXXX,
    MCShooterAlertStateYYY,
    MCShooterAlertStateZZZ
} MCShooterAlertState;

@interface MCShooterController : UIViewController
{
@private

    UIActionSheet           *actionSheet_;
    MCShooterAlertState      alertState_;
    CustomViews             *customViews_;
    int                      disableTouchesSemaphore_;
    UIImageView             *helpView_;
    CGRect                   iconFrame_;
    UIImageView             *imageView_;
    UILabel                 *labelTitle_;
    UIView                  *outerView_;
    ShooterStorageConnector *patientStore_;
    CGRect                   pictureframe_;
    UITextView              *savingTextView_;
    UIViewController        *shootingController_;
    UIButton                *subjectImageButton_;
    NSMutableArray          *tinyPics_;
    NSString                *title_;
    CGRect                   toggleFrame_;
    CGRect                   touchFrame_;
    UIActionSheet           *uploadActionSheet_;
    BOOL                     validFieldEntered_;
}

- (id) initWithShootingController: (UIViewController *) vc;

@end
