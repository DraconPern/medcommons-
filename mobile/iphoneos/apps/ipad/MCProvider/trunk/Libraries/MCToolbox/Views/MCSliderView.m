//
//  MCSliderView.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/30/10.
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

#import "MCSliderView.h"

#pragma mark -
#pragma mark Public Class MCSliderView
#pragma mark -

@interface MCSliderView ()

@property (nonatomic, retain, readonly) UISlider *slider;

- (CGFloat) roundValue: (CGFloat) value;

- (void) sliderAction: (id) sender;

- (void) updateCurrentValue;

- (void) updateMaximumValue;

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

    [self updateCurrentValue];
}

- (void) setMaximumValue: (CGFloat) value
{
    CGFloat rndValue = [self roundValue: value];

    if (self.slider.maximumValue != (float) rndValue)
    {
        [self willChangeValueForKey: @"maximumValue"];

        self.slider.maximumValue = (float) rndValue;

        [self didChangeValueForKey: @"maximumValue"];

        [self updateCurrentValue];
    }
}

- (void) setMinimumValue: (CGFloat) value
{
    if (self.slider.maximumValue != (float) value)
    {
        [self willChangeValueForKey: @"minimumValue"];

        self.slider.minimumValue = (float) value;

        [self didChangeValueForKey: @"minimumValue"];

        if (self.stepValue > 0.0f)
            [self updateMaximumValue];

        [self updateCurrentValue];
    }
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
        [self willChangeValueForKey: @"stepValue"];

        self->stepValue_ = tmpStepValue;

        [self didChangeValueForKey: @"stepValue"];

        if (self.stepValue > 0.0f)
            [self updateMaximumValue];

        [self updateCurrentValue];
    }
}

#pragma mark Private Instance Methods

- (CGFloat) roundValue: (CGFloat) value
{
    return ((self.stepValue > 0.0f) ?
            ((roundf ((value - self.minimumValue) /
                      self.stepValue) *
              self.stepValue) +
             self.minimumValue) :
            value);
}

- (void) sliderAction: (id) sender
{
    NSAssert (self.slider == sender,
              @"Bad slider!");

    [self updateCurrentValue];
}

- (void) updateCurrentValue
{
    CGFloat rndValue = [self roundValue: (CGFloat) self.slider.value];

    if (self->currentValue_ != rndValue)
    {
        [self willChangeValueForKey: @"currentValue"];

        self->currentValue_ = rndValue;

        [self didChangeValueForKey: @"currentValue"];

        [self.target performSelector: self.action
                          withObject: self];
    }
}

- (void) updateMaximumValue
{
    CGFloat rndValue = [self roundValue: (CGFloat) self.slider.maximumValue];

    if (self.slider.maximumValue != (float) rndValue)
    {
        [self willChangeValueForKey: @"maximumValue"];

        self.slider.maximumValue = (float) rndValue;

        [self didChangeValueForKey: @"maximumValue"];
    }
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
                        action: @selector (sliderAction:)
              forControlEvents: UIControlEventValueChanged];

        [self addSubview: self.slider];
        [self sizeToFit];
    }

    return self;
}

- (void) layoutSubviews
{
    [super layoutSubviews];

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
