//
//  MasterViewController.h
//  MCProvider
//
//  Created by Bill Donner on 2/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MasterViewController : UITableViewController
{
@private

    UIButton *safariButton_;
    BOOL      withGroup;
}

@property (nonatomic, retain, readwrite) UIButton *safariButton;

- (id) initWithGroup;

- (void) remotePoke: (NSString *) title;

@end
