//
//  MCBorder.m
//  MCToolbox
//
//  Created by J. G. Pusey on 5/20/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//  * Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of MedCommons, Inc. nor the names of its contributors
//    may be used to endorse or promote products derived from this software
//    without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
