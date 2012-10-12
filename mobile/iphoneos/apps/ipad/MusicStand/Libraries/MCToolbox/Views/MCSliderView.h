//
//  MCSliderView.h
//  MCToolbox
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCView.h"

@interface MCSliderView : MCView
{
@private

    SEL           action_;
    CGFloat       currentValue_;
    UISlider     *slider_;
    UIEdgeInsets  sliderEdgeInsets_;
    CGFloat       stepValue_;
    id            target_;
}

@property (nonatomic, assign, readwrite) SEL          action;
@property (nonatomic, assign, readwrite) CGFloat      currentValue;
@property (nonatomic, assign, readwrite) CGFloat      maximumValue;
@property (nonatomic, assign, readwrite) CGFloat      minimumValue;
@property (nonatomic, assign, readwrite) UIEdgeInsets sliderEdgeInsets;
@property (nonatomic, assign, readwrite) CGFloat      stepValue;
@property (nonatomic, assign, readwrite) id           target;

@end
