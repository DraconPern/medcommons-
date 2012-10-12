//
//  InfoViewController.h
//  MCProvider
//
//  Created by Bill Donner on 1/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZipViewController : UIViewController
{
@private

    UIBarButtonItem *backButton_;
        NSArray         *documents_;
    NSString *base_;
}

- (id) initWithBase:(NSString *) str;
@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

@end
