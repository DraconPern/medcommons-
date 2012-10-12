//
//  MCStandardView.h
//  MCShared
//
//  Created by J. G. Pusey on 4/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MCStandardView : UIView
{
@private
    
    UIView  *backgroundView_;
    UIView  *contentView_;
    UIView  *footerView_;
    UIView  *headerView_;
    CGFloat  keyboardHeight_;
    struct
    {
        unsigned int resizesForKeyboard : 1;
    } flags_;
}

@property (nonatomic, retain, readonly) UIView  *backgroundView;
@property (nonatomic, retain, readonly) UIView  *contentView;
@property (nonatomic, retain, readonly) UIView  *footerView;
@property (nonatomic, retain, readonly) UIView  *headerView;
@property (nonatomic, readonly)         CGFloat  keyboardHeight;
@property (nonatomic, readwrite)        BOOL     resizesForKeyboard;

- (id) initWithFrame: (CGRect) frame
      backgroundView: (UIView *) backgroundView
         contentView: (UIView *) contentView
          headerView: (UIView *) headerView
          footerView: (UIView *) footerView;

- (void) layoutBackgroundView;

- (void) layoutContentView;

- (void) layoutFooterView;

- (void) layoutHeaderView;

@end
