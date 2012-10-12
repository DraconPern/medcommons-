//
//  GroupListViewController.h
//  MCProvider
//
//  Created by Bill Donner on 4/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class InfoViewController;

@interface GroupListViewController : UITableViewController
{
@private

    InfoViewController *ivc_;
}

- (void) reloadData;

@end
