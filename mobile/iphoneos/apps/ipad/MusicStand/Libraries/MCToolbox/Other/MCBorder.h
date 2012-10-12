//
//  MCBorder.h
//  MCToolbox
//
//  Created by J. G. Pusey on 5/20/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MCBorder : NSObject
{
@private

    UIColor *color_;
    CGFloat  cornerRadius_;
    CGFloat  width_;
}

@property (nonatomic, retain, readonly) UIColor *color;
@property (nonatomic, assign, readonly) CGFloat  cornerRadius;
@property (nonatomic, assign, readonly) CGFloat  width;

+ (MCBorder *) borderWithColor: (UIColor *) color
                         width: (CGFloat) width
                  cornerRadius: (CGFloat) cornerRadius;

- (id) initWithColor: (UIColor *) color
               width: (CGFloat) width
        cornerRadius: (CGFloat) cornerRadius;

@end

//
// UIView convenience additions:
//
@interface UIView (MCToolbox)

@property (nonatomic, retain, readwrite) MCBorder *border;

- (void) setBorder: (MCBorder *) border
          animated: (BOOL) animated;

@end
