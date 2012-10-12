//
//  MCSliderView.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCSliderView.h"

#pragma mark -
#pragma mark Public Class MCSliderView
#pragma mark -

@interface MCSliderView ()

@property (nonatomic, retain, readonly) UISlider *slider;

- (void) performAction;

- (CGFloat) roundValue: (CGFloat) value;

@end

@implementation MCSliderView

@synthesize action           = action_;
@synthesize currentValue     = currentValue_;
@dynamic    maximumValue;
@dynamic    minimumValue;
@synthesize slider           = slider_;
@synthesize sliderEdgeInsets = sliderEdgeInsets_;
@synthesize stepValue        = stepValue_;
@synthesize target           = target_;

#pragma mark Public Instance Methods

- (CGFloat) maximumValue
{
    return (CGFloat) self.slider.maximumValue;
}

- (CGFloat) minimumValue
{
    return (CGFloat) self.slider.minimumValue;
}

- (void) setCurrentValue: (CGFloat) value
{
    self.slider.value = (float) value;

    self->currentValue_ = [self roundValue: (CGFloat) self.slider.value];   // may have changed
}

- (void) setMaximumValue: (CGFloat) value
{
    self.slider.maximumValue = (float) [self roundValue: value];

    self->currentValue_ = [self roundValue: (CGFloat) self.slider.value];   // may have changed
}

- (void) setMinimumValue: (CGFloat) value
{
    self.slider.minimumValue = (float) value;

    if (self.stepValue > 0.0f)
        self.slider.maximumValue = (float) [self roundValue: (CGFloat) self.slider.maximumValue];

    self->currentValue_ = [self roundValue: (CGFloat) self.slider.value];   // may have changed
}

- (void) setStepValue: (CGFloat) value
{
    CGFloat maxStepValue = self.maximumValue - self.minimumValue;
    CGFloat tmpStepValue = ((value < 0.0f) ?
                            0.0f :
                            ((value > maxStepValue) ?
                             maxStepValue :
                             value));

    if (self->stepValue_ != tmpStepValue)
    {
        self->stepValue_ = tmpStepValue;

        if (self.stepValue > 0.0f)
            self.slider.maximumValue = (float) [self roundValue: (CGFloat) self.slider.maximumValue];

        self->currentValue_ = [self roundValue: (CGFloat) self.slider.value];   // may have changed
    }
}

#pragma mark Private Instance Methods

- (void) performAction
{
    CGFloat rndValue = [self roundValue: (CGFloat) self.slider.value];

    if (self->currentValue_ != rndValue)
    {
        self->currentValue_ = rndValue;

        [self.target performSelector: self.action
                          withObject: self];
    }
}

- (CGFloat) roundValue: (CGFloat) value
{
    return ((self.stepValue > 0.0f) ?
            ((roundf ((value - self.minimumValue) /
                      self.stepValue) *
              self.stepValue) +
             self.minimumValue) :
            value);
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self->slider_ = [[UISlider alloc] initWithFrame: frame];

        self.slider.autoresizingMask = UIViewAutoresizingFlexibleWidth;

        [self.slider addTarget: self
                        action: @selector (performAction)
              forControlEvents: UIControlEventValueChanged];

        [self addSubview: self.slider];
        [self sizeToFit];
    }

    return self;
}

- (void) layoutSubviews
{
    [super layoutSubviews]; // Apple docs say we MUST do this ...

    CGRect tmpFrame = UIEdgeInsetsInsetRect (CGRectStandardize (self.bounds),
                                             self.sliderEdgeInsets);
    CGRect tmpBounds = CGRectZero;

    tmpBounds.size = [self.slider sizeThatFits: tmpFrame.size];

    self.slider.bounds = tmpBounds;
    self.slider.center = CGPointMake (CGRectGetMidX (tmpFrame),
                                      CGRectGetMidY (tmpFrame));
}

- (CGSize) sizeThatFits: (CGSize) size
{
    CGSize minSize = CGSizeMake ((self.sliderEdgeInsets.left +
                                  CGRectGetWidth (self.slider.bounds) +
                                  self.sliderEdgeInsets.right),
                                 (self.sliderEdgeInsets.top +
                                  CGRectGetHeight (self.slider.bounds) +
                                  self.sliderEdgeInsets.bottom));

    if (size.width < minSize.width)
        size.width = minSize.width;

    if (size.height < minSize.height)
        size.height = minSize.height;

    return size;
}

#pragma mark Overridden NSObject Methods

- (void)dealloc
{
    [self->slider_ release];

    [super dealloc];
}

@end
