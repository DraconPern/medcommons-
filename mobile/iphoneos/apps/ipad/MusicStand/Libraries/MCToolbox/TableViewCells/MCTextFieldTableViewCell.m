//
//  MCTextFieldTableViewCell.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/23/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
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
