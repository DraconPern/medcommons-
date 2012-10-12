//
//  LoginViewController.h
//  MCProvider
//
//  Created by J. G. Pusey on 7/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol LoginViewControllerDelegate;

@interface LoginViewController : UIViewController
{
@private

    UIButton                         *cancelButton_;
    id <LoginViewControllerDelegate>  delegate_;
    UIButton                         *demoButton_;
    NSString                         *demoPassword_;
    NSString                         *demoUserID_;
    UIButton                         *logInButton_;
    //
    // Flags:
    //
    BOOL                              isLandscape_;
    BOOL                              isPhone_;
}

@property (nonatomic, assign, readwrite) id <LoginViewControllerDelegate> delegate;

@end

@protocol LoginViewControllerDelegate <NSObject>

@optional

- (void) loginViewController: (LoginViewController *) lvc
         didFinishWithUserID: (NSString *) userID
                    password: (NSString *) password;

- (void) loginViewControllerDidCancel: (LoginViewController *) lvc;

@end
