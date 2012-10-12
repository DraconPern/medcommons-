//
//  InboxGroupListController.h
//  MCProvider
//
//  Created by Bill Donner on 5/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AsyncImageView;
@class MCActionController;

@interface InboxGroupListController : UIViewController
{
@private

    UIView             *backgroundView_;
    UILabel            *bottomSectionDesc_;
    UITableView        *contentView_;
    UITextField        *familyNameTextField_;
    UIView             *footerView_;
    UITextField        *givenNameTextField_;
    MCActionController *groupsActionController_;
    AsyncImageView     *logoView_;
    NSMutableArray     *mugShots_;
    NSUInteger          opIndices_ [4];
    UISegmentedControl *segmentedControl_;
    UILabel            *topSectionDesc_;
    NSUInteger          viewingMode_;
	
	NSUInteger			stashedidx;
	NSUInteger			stashedvmode;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

@end
