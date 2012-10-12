//
//  UITextField+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 9/17/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "UITextField+MCToolbox.h"

@implementation UITextField (MCToolbox)

- (CGFloat) preferredHeight
{
    CGFloat height;

    //
    // If text has not been set, sizeThatFits does not return a useful
    // height; thus we must set it to some bogus text and then unset it:
    //
    if (!self.text)
    {
        self.text = @"XXX";

        height = [self sizeThatFits: CGSizeZero].height;

        self.text = nil;
    }
    else
        height = [self sizeThatFits: CGSizeZero].height;

    return height;
}

@end
