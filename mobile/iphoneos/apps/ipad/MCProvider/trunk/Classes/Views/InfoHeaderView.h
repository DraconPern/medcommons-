//
//  InfoHeaderView.h
//  MCProvider
//
//  Created by J. G. Pusey on 6/25/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AsyncImageView;
@class InfoHeaderViewCell;

@interface InfoHeaderView : UIView
{
@private

    UIActivityIndicatorView *activityIndicator_;
    UIView                  *backgroundView_;
    UIView                  *fauxSectionView_;
    UIImageView             *secretTouchImageView_;
    InfoHeaderViewCell      *userLoginApplianceCell_;
    InfoHeaderViewCell      *userLoginTimeCell_;
    InfoHeaderViewCell      *userLoginUserIDCell_;
    AsyncImageView          *userPhotoImageView_;
    InfoHeaderViewCell      *userRealNameCell_;
}

@property (nonatomic, retain, readonly) UIView *secretView;

@property (nonatomic, retain, readonly) AsyncImageView *userPhotoImageView;

- (void) startActivityIndicator;

- (void) stopActivityIndicator;

- (void) update;

@end
