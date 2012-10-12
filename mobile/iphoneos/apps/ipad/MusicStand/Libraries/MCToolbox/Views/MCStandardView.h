//
//  MCStandardView.h
//  MCToolbox
//
//  Created by J. G. Pusey on 4/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MCStandardView : UIView
{
@private

    UIView     *backgroundView_;
    UIView     *contentView_;
    UIView     *footerView_;
    UIView     *headerView_;
    CGFloat     keyboardHeight_;
    //
    // Flags:
    //
    BOOL        tracksKeyboard_;
}

@property (nonatomic, retain, readwrite) UIView       *backgroundView;
@property (nonatomic, retain, readwrite) UIView       *contentView;
@property (nonatomic, retain, readwrite) UIView       *footerView;
@property (nonatomic, retain, readwrite) UIView       *headerView;
@property (nonatomic, assign, readonly)  CGFloat       keyboardHeight;
@property (nonatomic, assign, readonly)  CGFloat       standardGapThickness;
@property (nonatomic, assign, readonly)  UIEdgeInsets  standardMarginEdgeInsets;
@property (nonatomic, assign, readonly)  UIEdgeInsets  standardPaddingEdgeInsets;
@property (nonatomic, assign, readwrite) BOOL          tracksKeyboard;

@end
