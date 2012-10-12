//
//  BasicListCell.h
//  MCProvider
//
//  Created by J G. Pusey on 8/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AsyncImageView;

@interface BasicListCell : UITableViewCell
{
@private

    AsyncImageView *asyncImageView_;
    UILabel        *info1Label_;
    UILabel        *info2Label_;
    UILabel        *subtitleLabel_;
    UILabel        *titleLabel_;
}

@property (nonatomic, retain, readonly) AsyncImageView *asyncImageView;
@property (nonatomic, assign, readonly) BOOL            hasAsyncImageView;
@property (nonatomic, assign, readonly) BOOL            hasInfo1Label;
@property (nonatomic, assign, readonly) BOOL            hasInfo2Label;
@property (nonatomic, assign, readonly) BOOL            hasSubtitleLabel;
@property (nonatomic, assign, readonly) BOOL            hasTitleLabel;
@property (nonatomic, retain, readonly) UILabel        *info1Label;
@property (nonatomic, retain, readonly) UILabel        *info2Label;
@property (nonatomic, retain, readonly) UILabel        *subtitleLabel;
@property (nonatomic, retain, readonly) UILabel        *titleLabel;

+ (CGFloat) defaultCellHeight;

+ (CGSize) defaultImageSize;

- (id) initWithReuseIdentifier: (NSString *) reuseIdentifier;

- (void) removeAsyncImageView;

- (void) removeInfo1Label;

- (void) removeInfo2Label;

- (void) removeSubtitleLabel;

- (void) removeTitleLabel;

@end
