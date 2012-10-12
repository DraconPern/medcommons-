//
//  InfoViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface InfoViewController : UIViewController
{
@private

    UIActionSheet   *actionSheet_;
    UIBarButtonItem *backButton_;
    NSCalendar      *calendar_;
    NSDateFormatter *dateFormatter_;
    NSArray         *documents_;
    UIBarButtonItem *inboxButton_;
    UIBarButtonItem *logInButton_;
    UIBarButtonItem *logOutButton_;
    NSString        *pushIntoSoloURL_;
    NSDateFormatter *timeFormatter_;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

- (id) initWithNoGroups;

- (id) initWithSoloURL: (NSString *) URL;

@end
