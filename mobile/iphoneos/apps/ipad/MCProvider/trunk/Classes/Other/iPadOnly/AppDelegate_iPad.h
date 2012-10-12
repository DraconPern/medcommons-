//
//  AppDelegate_iPad.h
//  MCProvider
//
//  Created by J. G. Pusey on 4/29/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"

@class MCMasterDetailViewController;

@interface AppDelegate_iPad : AppDelegate
{
@private

    DetailViewController         *baseDetailViewController_;
    MCMasterDetailViewController *masterDetailViewController_;
    UINavigationController       *navigationController_;
}

@end
