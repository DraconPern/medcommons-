//
//  MemberListViewController.h
//  MCProvider
//
//  Created by Bill Donner on 4/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AsyncImageView;
@class MCActionController;

@interface MemberListViewController : UIViewController
{
@private

    MCActionController *groupsActionController_;
    AsyncImageView     *logoView_;
    UISegmentedControl *segmentedControl_;
    NSUInteger          viewingMode_;
	NSUInteger			stashedidx;
//	NSUInteger			stashedvmode;
}

@end
