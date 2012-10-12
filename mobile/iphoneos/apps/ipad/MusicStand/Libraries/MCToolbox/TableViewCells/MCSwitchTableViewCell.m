//
//  MCSwitchTableViewCell.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/16/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
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
