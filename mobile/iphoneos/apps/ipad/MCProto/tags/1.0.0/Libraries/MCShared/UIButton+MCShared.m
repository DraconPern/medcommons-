//
//  UIButton+MCShared.m
//  MCShared
//
//  Created by J. G. Pusey on 4/2/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "UIButton+MCShared.h"

@implementation UIButton (MCShared)

+ (id) buttonWithTitle: (NSString *) title
                target: (id) target
              selector: (SEL) selector
                 frame: (CGRect) frame
                   tag: (NSInteger) tag
{
    UIButton *button = [UIButton buttonWithType: UIButtonTypeRoundedRect];

    button.frame = frame;
    button.tag = tag;

    [button setTitle: title
            forState: UIControlStateNormal];
    [button addTarget: target
               action: selector
     forControlEvents: UIControlEventTouchUpInside];

    return button;
}

@end
