//
//  MCSwitchTableViewCell.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/16/10.
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

#import "MCSwitchTableViewCell.h"

#pragma mark -
#pragma mark Public Class MCSwitchTableViewCell
#pragma mark -

#pragma mark Internal Constants

#define PADDING_LEFT_INSET  8.0f
#define PADDING_RIGHT_INSET 8.0f

@implementation MCSwitchTableViewCell

@synthesize switchControl = switchControl_;

#pragma mark Overridden UITableViewCell Methods

- (id) initWithStyle: (UITableViewCellStyle) style
     reuseIdentifier: (NSString *) reuseIdentifier
{
    self = [super initWithStyle: style
                reuseIdentifier: reuseIdentifier];

    if (self)
    {
        self->switchControl_ = [[UISwitch alloc] initWithFrame: CGRectZero];

        self.switchControl.backgroundColor = [UIColor clearColor];

        [self.contentView addSubview: self.switchControl];

        self.textLabel.adjustsFontSizeToFitWidth = YES;
        self.textLabel.minimumFontSize = 12.0f;
    }

    return self;
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [super layoutSubviews]; // Apple docs say we MUST do this ...

    CGRect scFrame = CGRectStandardize (self.switchControl.frame);

    scFrame.origin.x = (CGRectGetWidth (self.contentView.bounds) -
                        CGRectGetWidth (scFrame) -
                        PADDING_RIGHT_INSET);
    scFrame.origin.y = ((CGRectGetHeight (self.contentView.bounds) -
                         CGRectGetHeight (scFrame)) / 2.0f);

    self.switchControl.frame = scFrame;

    CGFloat scMinX = CGRectGetMinX (scFrame) - PADDING_LEFT_INSET;
    CGRect  tlFrame = CGRectStandardize (self.textLabel.frame);
    CGFloat tlMaxX = CGRectGetMaxX (tlFrame);

    if (tlMaxX > scMinX)
    {
        tlFrame.size.width -= (tlMaxX - scMinX);

        self.textLabel.frame = tlFrame;
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->switchControl_ release];

    [super dealloc];
}

@end
