//
//  MCTextFieldTableViewCell.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/23/10.
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

#import "MCTextFieldTableViewCell.h"
#import "UITextField+MCToolbox.h"

#pragma mark -
#pragma mark Public Class MCTextFieldTableViewCell
#pragma mark -

#pragma mark Internal Constants

#define PADDING_LEFT_INSET  8.0f
#define PADDING_RIGHT_INSET 8.0f

@implementation MCTextFieldTableViewCell

@synthesize textField = textField_;

#pragma mark Overridden UITableViewCell Methods

- (id) initWithStyle: (UITableViewCellStyle) style
     reuseIdentifier: (NSString *) reuseIdentifier
{
    self = [super initWithStyle: style
                reuseIdentifier: reuseIdentifier];

    if (self)
    {
        self->textField_ = [[UITextField alloc] initWithFrame: CGRectZero];

        self.textField.adjustsFontSizeToFitWidth = YES;
        self.textField.backgroundColor = [UIColor whiteColor];
        self.textField.font = [UIFont systemFontOfSize: 17.0f];
        self.textField.minimumFontSize = 12.0f;
        self.textField.textColor = [UIColor colorWithRed: 0.22f
                                                   green: 0.33f
                                                    blue: 0.53f
                                                   alpha: 1.0f];

        [self.contentView addSubview: self.textField];

        self.textLabel.adjustsFontSizeToFitWidth = YES;
        self.textLabel.minimumFontSize = 12.0f;
    }

    return self;
}

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [super layoutSubviews]; // Apple docs say we MUST do this ...

    CGSize  cvSize = CGSizeMake (CGRectGetWidth (self.contentView.bounds),
                                 CGRectGetHeight (self.contentView.bounds));
    CGRect  tlFrame = CGRectStandardize (self.textLabel.frame);
    CGFloat tlHeight = CGRectGetHeight (tlFrame);
    CGRect  tfFrame;

    tfFrame.size.width = ((cvSize.width / 2.0f) -
                          PADDING_LEFT_INSET -
                          PADDING_RIGHT_INSET);
    tfFrame.size.height = [self.textField preferredHeight];

    if (tfFrame.size.height < tlHeight) // kluge alert!!!
        tfFrame.size.height = tlHeight;

    tfFrame.origin.x = (cvSize.width -
                        tfFrame.size.width -
                        PADDING_RIGHT_INSET);
    tfFrame.origin.y = ((cvSize.height -
                         tfFrame.size.height) / 2.0f);

    self.textField.frame = tfFrame;

    CGFloat tfMinX = CGRectGetMinX (tfFrame) - PADDING_LEFT_INSET;
    CGFloat tlMaxX = CGRectGetMaxX (tlFrame);

    if (tlMaxX > tfMinX)
    {
        tlFrame.size.width -= (tlMaxX - tfMinX);

        self.textLabel.frame = tlFrame;
    }
}

- (void) setBounds: (CGRect) bounds
{
    super.bounds = bounds;

    [self setNeedsLayout];
}

- (void) setCenter: (CGPoint) center
{
    super.center = center;

    [self setNeedsLayout];
}

- (void) setFrame: (CGRect) frame
{
    super.frame = frame;

    [self setNeedsLayout];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->textField_ release];

    [super dealloc];
}

@end
