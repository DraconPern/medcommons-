//
//  CCRHeaderView.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "CCRButton.h"
#import "CCRHeaderView.h"
#import "CCRThumbCell.h"
#import "StyleManager.h"

#pragma mark Internal Constants

#define HAIR_GAP_THICKNESS 2.0f
#define THIN_GAP_THICKNESS 8.0f
#define WIDE_GAP_THICKNESS 20.0f

#define BOTTOM_INSET       THIN_GAP_THICKNESS
#define LEFT_INSET         THIN_GAP_THICKNESS
#define RIGHT_INSET        THIN_GAP_THICKNESS
#define TOP_INSET          THIN_GAP_THICKNESS

#define SPACING            WIDE_GAP_THICKNESS

#pragma mark -
#pragma mark Public Class CCRHeaderView
#pragma mark -

@interface CCRHeaderView ()

- (void) loadSubviews;

@end

@implementation CCRHeaderView

@synthesize documentsButton = documentsButton_;
@synthesize episodesButton  = episodesButton_;
@synthesize subtitleLabel   = subtitleLabel_;
@synthesize subtitleText    = subtitleText_;
@synthesize titleButton     = titleButton_;
@synthesize titleText       = titleText_;

#pragma mark Private Instance Methods

- (void) loadSubviews
{
    MCBorder     *border = [MCBorder borderWithColor: [UIColor lightGrayColor]
                                               width: 1.0f
                                        cornerRadius: 0.0f];
    UIEdgeInsets  titleInsets = UIEdgeInsetsMake (HAIR_GAP_THICKNESS,
                                                  HAIR_GAP_THICKNESS,
                                                  HAIR_GAP_THICKNESS,
                                                  HAIR_GAP_THICKNESS);
    StyleManager *styles = self.appDelegate.styleManager;

    //
    // Episodes button:
    //
    self->episodesButton_ = [[CCRButton alloc] initWithIdentifier: @"episodes"];

    self.episodesButton.border = border;
    self.episodesButton.showsTouchWhenHighlighted = YES;
    self.episodesButton.titleEdgeInsets = titleInsets;

    self.episodesButton.titleLabel.adjustsFontSizeToFitWidth = YES;
    self.episodesButton.titleLabel.backgroundColor = [UIColor clearColor];
    self.episodesButton.titleLabel.font = styles.labelFontM;
    self.episodesButton.titleLabel.minimumFontSize = styles.labelFontS.pointSize;
    self.episodesButton.titleLabel.textAlignment = UITextAlignmentLeft;
    self.episodesButton.titleLabel.textColor = [UIColor whiteColor];

    [self addSubview: self.episodesButton];

    //
    // Title button:
    //
    self->titleButton_ = [[CCRButton alloc] initWithIdentifier: @"title"];

    self.titleButton.border = border;
    self.titleButton.showsTouchWhenHighlighted = YES;
    self.titleButton.titleEdgeInsets = titleInsets;

    self.titleButton.titleLabel.adjustsFontSizeToFitWidth = YES;
    self.titleButton.titleLabel.backgroundColor = [UIColor clearColor];
    self.titleButton.titleLabel.font = styles.labelFontBoldXL;
    self.titleButton.titleLabel.minimumFontSize = styles.labelFontBoldS.pointSize;
    self.titleButton.titleLabel.textAlignment = UITextAlignmentCenter;
    self.titleButton.titleLabel.textColor = [UIColor whiteColor];

    [self addSubview: self.titleButton];

    //
    // Subtitle label:
    //
    self->subtitleLabel_ = [[UILabel alloc] initWithFrame: CGRectZero];

    self.subtitleLabel.adjustsFontSizeToFitWidth = YES;
    self.subtitleLabel.backgroundColor = [UIColor clearColor];
    self.subtitleLabel.font = styles.labelFontL;
    self.subtitleLabel.minimumFontSize = styles.labelFontS.pointSize;
    self.subtitleLabel.textAlignment = UITextAlignmentCenter;
    self.subtitleLabel.textColor = [UIColor whiteColor];

    [self addSubview: self.subtitleLabel];

    //
    // Documents button:
    //
    self->documentsButton_ = [[CCRButton alloc] initWithIdentifier: @"documents"];

    self.documentsButton.border = border;
    self.documentsButton.showsTouchWhenHighlighted = YES;
    self.documentsButton.titleEdgeInsets = titleInsets;

    self.documentsButton.titleLabel.adjustsFontSizeToFitWidth = YES;
    self.documentsButton.titleLabel.backgroundColor = [UIColor clearColor];
    self.documentsButton.titleLabel.font = styles.labelFontM;
    self.documentsButton.titleLabel.minimumFontSize = styles.labelFontS.pointSize;
    self.documentsButton.titleLabel.textAlignment = UITextAlignmentRight;
    self.documentsButton.titleLabel.textColor = [UIColor whiteColor];

    [self addSubview: self.documentsButton];
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self.backgroundColor = [UIColor purpleColor];

        [self loadSubviews];
        [self sizeToFit];
    }

    return self;
}

- (void) layoutSubviews
{
    CGSize hvSize = CGSizeMake (CGRectGetWidth (self.bounds),
                                CGRectGetHeight (self.bounds));

    hvSize.width -= (LEFT_INSET + RIGHT_INSET);
    hvSize.height -= (TOP_INSET + BOTTOM_INSET);

    CGRect tmpFrame;

    tmpFrame.size = [self.episodesButton sizeThatFits: hvSize];
    tmpFrame.origin.x = LEFT_INSET;
    tmpFrame.origin.y = TOP_INSET;

    self.episodesButton.frame = tmpFrame;

    tmpFrame.size = [self.titleButton sizeThatFits: hvSize];
    tmpFrame.origin.x = (hvSize.width - tmpFrame.size.width) / 2.0f;
    tmpFrame.origin.y = CGRectGetMaxY (tmpFrame) + SPACING;

    self.titleButton.frame = tmpFrame;

    tmpFrame.size = [self.subtitleLabel sizeThatFits: hvSize];
    tmpFrame.origin.x = (hvSize.width - tmpFrame.size.width) / 2.0f;
    tmpFrame.origin.y = CGRectGetMaxY (tmpFrame);

    self.subtitleLabel.frame = tmpFrame;

    tmpFrame.size = [self.documentsButton sizeThatFits: hvSize];
    tmpFrame.origin.x = LEFT_INSET + hvSize.width - tmpFrame.size.width;
    tmpFrame.origin.y = CGRectGetMaxY (tmpFrame) + SPACING;

    self.documentsButton.frame = tmpFrame;
}

- (CGSize) sizeThatFits: (CGSize) size
{
    return [CCRThumbCell preferredSize];
}

#pragma mark Overridden NSObject Methods

- (void)dealloc
{
    [self->documentsButton_ release];
    [self->episodesButton_ release];
    [self->subtitleLabel_ release];
    [self->subtitleText_ release];
    [self->titleButton_ release];
    [self->titleText_ release];

    [super dealloc];
}

@end
