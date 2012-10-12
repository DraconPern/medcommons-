//
//  UISegmentedControl+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "UISegmentedControl+MCToolbox.h"

@implementation UISegmentedControl (MCToolbox)

- (CGRect) rectForSegmentAtIndex: (NSUInteger) segment
{
    CGRect rect = CGRectStandardize (self.bounds);

    rect.size.width /= self.numberOfSegments;
    rect.origin.x += (rect.size.width * segment);

    return CGRectIntegral (rect);
}

@end
