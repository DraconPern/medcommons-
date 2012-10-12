//
//  CCRToolCell.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/31/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "CCRButton.h"
#import "CCRToolCell.h"

#pragma mark Internal Constants

#define HAIR_GAP_THICKNESS  2.0f
#define THIN_GAP_THICKNESS  8.0f
#define WIDE_GAP_THICKNESS  20.0f

#define BUTTON_HEIGHT       BUTTON_WIDTH
#define BUTTON_WIDTH        48.0f

#define BUTTON_BOTTOM_INSET (THIN_GAP_THICKNESS / 2.0f)
#define BUTTON_LEFT_INSET   THIN_GAP_THICKNESS
#define BUTTON_RIGHT_INSET  0.0f
#define BUTTON_TOP_INSET    (THIN_GAP_THICKNESS / 2.0f)

#define PREFERRED_HEIGHT    (BUTTON_TOP_INSET + BUTTON_HEIGHT + BUTTON_BOTTOM_INSET)
#define PREFERRED_WIDTH     (BUTTON_LEFT_INSET + BUTTON_WIDTH + BUTTON_RIGHT_INSET)

#pragma mark -
#pragma mark Public Class CCRToolCell
#pragma mark -

//@interface CCRToolCell ()
//
//@end

@implementation CCRToolCell

@synthesize action      = action_;
@synthesize button      = button_;
@dynamic    identifier;
@dynamic    image;
@dynamic    jsEvent;
@dynamic    jsParameter;
@synthesize target      = target_;

#pragma mark Public Class Methods

+ (CGSize) preferredSize
{
    return CGSizeMake (PREFERRED_WIDTH,
                       PREFERRED_HEIGHT);
}

#pragma mark Public Instance Methods

- (NSString *) identifier
{
    return self.button.identifier;
}

- (UIImage *) image
{
    return [self.button imageForState: UIControlStateNormal];
}

- (id) initWithIdentifier: (NSString *) identifier
{
    CGRect tmpFrame = CGRectZero;

    tmpFrame.size = [[self class] preferredSize];

    self = [super initWithFrame: tmpFrame
                reuseIdentifier: nil];

    if (self)
    {
        self->button_ = [[CCRButton alloc] initWithIdentifier: identifier];

        tmpFrame.origin.x = BUTTON_LEFT_INSET;
        tmpFrame.origin.y = BUTTON_TOP_INSET;
        tmpFrame.size.width = BUTTON_WIDTH;
        tmpFrame.size.height = BUTTON_HEIGHT;

        self.button.frame = tmpFrame;

        [self.contentView addSubview: self.button];

        self.selectionStyle = AQGridViewCellSelectionStyleNone;

        //self.contentView.backgroundColor = [UIColor blueColor];
    }

    return self;
}

- (NSString *) jsEvent
{
    return self.button.jsEvent;
}

- (NSString *) jsParameter
{
    return self.button.jsParameter;
}

- (void) setAction: (SEL) action
{
    if (self->action_ != action)
    {
        if (self->action_ && self->target_)
            [self.button removeTarget: self->target_
                               action: self->action_
                     forControlEvents: UIControlEventTouchUpInside];

        self->action_ = action;

        if (self->action_ && self->target_)
            [self.button addTarget: self->target_
                            action: self->action_
                  forControlEvents: UIControlEventTouchUpInside];
    }
}

- (void) setImage: (UIImage *) image
{
    [self.button setImage: image
                 forState: UIControlStateNormal];
}

- (void) setJsEvent: (NSString *) jsEvent
{
    self.button.jsEvent = jsEvent;
}

- (void) setJsParameter: (NSString *) jsParameter
{
    self.button.jsParameter = jsParameter;
}

- (void) setTarget: (id) target
{
    if (self->target_ != target)
    {
        if (self->action_ && self->target_)
            [self.button removeTarget: self->target_
                               action: self->action_
                     forControlEvents: UIControlEventTouchUpInside];

        self->target_ = target;

        if (self->action_ && self->target_)
            [self.button addTarget: self->target_
                            action: self->action_
                  forControlEvents: UIControlEventTouchUpInside];
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->button_ release];

    [super dealloc];
}

@end
