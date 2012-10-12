//
//  CCRScrubberView.m
//  MCProvider
//
//  Created by J. G. Pusey on 9/1/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRScrubberView.h"

#pragma mark -
#pragma mark Public Class CCRScrubberView
#pragma mark -

#pragma mark Internal Constants

#define HAIR_GAP_THICKNESS 2.0f
#define THIN_GAP_THICKNESS 8.0f
#define WIDE_GAP_THICKNESS 20.0f

#define BOTTOM_INSET       THIN_GAP_THICKNESS
#define LEFT_INSET         HAIR_GAP_THICKNESS
#define RIGHT_INSET        HAIR_GAP_THICKNESS
#define TOP_INSET          THIN_GAP_THICKNESS

@interface CCRScrubberView ()

@property (nonatomic, retain, readonly) UILabel *label;

- (void) addKeyValueObservers;

- (void) removeKeyValueObservers;

- (void) updateLabel;

@end

@implementation CCRScrubberView
@dynamic label;

#pragma mark Public Instance Methods

- (void) resetValues
{
    self.maximumValue = 1.0f;
    self.minimumValue = 0.0f;
    self.stepValue = 1.0f;
}

#pragma mark Private Instance Methods

- (void) addKeyValueObservers
{
    [self addObserver: self
           forKeyPath: @"currentValue"
              options: NSKeyValueObservingOptionInitial
              context: NULL];

    [self addObserver: self
           forKeyPath: @"maximumValue"
              options: NSKeyValueObservingOptionInitial
              context: NULL];
}

- (UILabel *) label
{
    if (!self->label_)
    {
        self->label_ = [[UILabel alloc] initWithFrame: CGRectZero];

        self->label_.backgroundColor = [UIColor clearColor];
        self->label_.font = [UIFont boldSystemFontOfSize: 10.0f];
        self->label_.text = @"9999999999 / 9999999999";
        self->label_.textAlignment = UITextAlignmentCenter;
        self->label_.textColor = [UIColor whiteColor];

        CGRect tmpFrame = CGRectZero;

        tmpFrame.size = [self->label_ sizeThatFits: self.bounds.size];

        self->label_.frame = tmpFrame;
        self->label_.text = nil;

        [self addSubview: self->label_];
    }

    return self->label_;
}

- (void) removeKeyValueObservers
{
    [self removeObserver: self
              forKeyPath: @"currentValue"];

    [self removeObserver: self
              forKeyPath: @"maximumValue"];
}

- (void) updateLabel
{
    self.label.text = [NSString stringWithFormat: @"%lu / %lu",
                       (NSUInteger) self.currentValue+1,
                       (NSUInteger) self.maximumValue+1];
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self.backgroundColor = [UIColor greenColor];
        self.sliderEdgeInsets = UIEdgeInsetsMake (TOP_INSET,
                                                  LEFT_INSET,
                                                  BOTTOM_INSET,
                                                  RIGHT_INSET);
        self.stepValue = 1.0f;

        [self addKeyValueObservers];
    }

    return self;
}

- (void) layoutSubviews
{
    [super layoutSubviews];

    CGRect tmpFrame = CGRectStandardize (self.label.frame);

    tmpFrame.origin.x = (CGRectGetMidX (self.bounds) -
                         (tmpFrame.size.width / 2.0f));
    tmpFrame.origin.y = (CGRectGetMaxY (self.bounds) -
                         tmpFrame.size.height);

    self.label.frame = tmpFrame;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self removeKeyValueObservers];

    [self->label_ release];

    [super dealloc];
}

#pragma mark NSKeyValueObserving Methods

- (void) observeValueForKeyPath: (NSString *) keyPath
                       ofObject: (id) object
                         change: (NSDictionary *) change
                        context: (void *) context
{
    if (object == self)
        [self updateLabel];
}

@end
