//
//  MCBorder.m
//  MCToolbox
//
//  Created by J. G. Pusey on 5/20/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>

#import "MCBorder.h"

#pragma mark -
#pragma mark Public Class MCBorder
#pragma mark -

@implementation MCBorder

@synthesize color        = color_;
@synthesize cornerRadius = cornerRadius_;
@synthesize width        = width_;

#pragma mark Public Class Methods

+ (MCBorder *) borderWithColor: (UIColor *) color
                         width: (CGFloat) width
                  cornerRadius: (CGFloat) cornerRadius
{
    return [[[MCBorder alloc] initWithColor: color
                                      width: width
                               cornerRadius: cornerRadius]
            autorelease];
}

#pragma mark Public Instance Methods

- (id) initWithColor: (UIColor *) color
               width: (CGFloat) width
        cornerRadius: (CGFloat) cornerRadius
{
    self = [super init];

    if (self)
    {
        self->color_ = [color retain];
        self->cornerRadius_ = cornerRadius;
        self->width_ = width;
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->color_ release];

    [super dealloc];
}

@end

#pragma mark -
#pragma mark Public Class UIView Additions
#pragma mark -

@implementation UIView (MCToolbox)

@dynamic border;

#pragma mark Public Instance Methods

- (MCBorder *) border
{
    return [MCBorder borderWithColor: [UIColor colorWithCGColor: self.layer.borderColor]
                               width: self.layer.borderWidth
                        cornerRadius: self.layer.cornerRadius];
}

- (void) setBorder: (MCBorder *) border
{
    [self setBorder: border
           animated: NO];
}

- (void) setBorder: (MCBorder *) border
          animated: (BOOL) animated
{
    if (border)
    {
        self.layer.borderColor = border.color.CGColor;
        self.layer.borderWidth = border.width;
        self.layer.cornerRadius = border.cornerRadius;
        self.layer.masksToBounds = YES;
    }
    else
    {
        self.layer.borderColor = [UIColor blackColor].CGColor;
        self.layer.borderWidth = 0.0f;
        self.layer.cornerRadius = 0.0f;
        self.layer.masksToBounds = NO;
    }
}

@end
