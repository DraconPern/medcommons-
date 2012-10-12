//
//  MCStandardView.m
//  MCToolbox
//
//  Created by J. G. Pusey on 4/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCStandardView.h"

#pragma mark -
#pragma mark Public Class MCStandardView
#pragma mark -

@interface MCStandardView ()

- (void) addNotificationObservers;

- (void) removeNotificationObservers;

//
// Forward declarations:
//
- (void) keyboardWillHide: (NSNotification *) notification;

- (void) keyboardWillShow: (NSNotification *) notification;

@end

@implementation MCStandardView

@synthesize backgroundView            = backgroundView_;
@synthesize contentView               = contentView_;
@synthesize footerView                = footerView_;
@synthesize headerView                = headerView_;
@synthesize keyboardHeight            = keyboardHeight_;
@dynamic    standardGapThickness;
@dynamic    standardMarginEdgeInsets;
@dynamic    standardPaddingEdgeInsets;
@synthesize tracksKeyboard            = tracksKeyboard_;

#pragma mark Public Instance Methods

- (void) setBackgroundView: (UIView *) backgroundView
{
    if (self->backgroundView_ != backgroundView)
    {
        if (self->backgroundView_)
        {
            [self->backgroundView_ removeFromSuperview];
            [self->backgroundView_ release];
        }

        self->backgroundView_ = [backgroundView retain];

        if (self->backgroundView_)
        {
            if (self->headerView_)
                [self->backgroundView_ addSubview: self->headerView_];

            if (self->contentView_)
                [self->backgroundView_ addSubview: self->contentView_];

            if (self->footerView_)
                [self->backgroundView_ addSubview: self->footerView_];

            [self addSubview: self->backgroundView_];
        }
    }
}

- (void) setContentView: (UIView *) contentView
{
    if (self->contentView_ != contentView)
    {
        if (self->contentView_)
        {
            [self->contentView_ removeFromSuperview];
            [self->contentView_ release];
        }

        self->contentView_ = [contentView retain];

        if (self->contentView_ && self->backgroundView_)
            [self->backgroundView_ addSubview: self->contentView_];
    }
}

- (void) setFooterView: (UIView *) footerView
{
    if (self->footerView_ != footerView)
    {
        if (self->footerView_)
        {
            [self->footerView_ removeFromSuperview];
            [self->footerView_ release];
        }

        self->footerView_ = [footerView retain];

        if (self->footerView_ && self->backgroundView_)
            [self->backgroundView_ addSubview: self->footerView_];
    }
}

- (void) setHeaderView: (UIView *) headerView
{
    if (self->headerView_ != headerView)
    {
        if (self->headerView_)
        {
            [self->headerView_ removeFromSuperview];
            [self->headerView_ release];
        }

        self->headerView_ = [headerView retain];

        if (self->headerView_ && self->backgroundView_)
            [self->backgroundView_ addSubview: self->headerView_];
    }
}

- (void) setTracksKeyboard: (BOOL) tracksKeyboard
{
    if (self->tracksKeyboard_ != tracksKeyboard)
    {
        self->tracksKeyboard_ = tracksKeyboard;

        if (tracksKeyboard)
            [self addNotificationObservers];
        else
            [self removeNotificationObservers];
    }
}

- (CGFloat) standardGapThickness
{
    return 8.0f;
}

- (UIEdgeInsets) standardMarginEdgeInsets
{
    return UIEdgeInsetsZero;
}

- (UIEdgeInsets) standardPaddingEdgeInsets
{
    return UIEdgeInsetsMake (20.0f, 20.0f, 20.0f, 20.0f);
}

#pragma mark Private Instance Methods

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc addObserver: self
           selector: @selector (keyboardWillShow:)
               name: UIKeyboardWillShowNotification
             object: nil];

    [nc addObserver: self
           selector: @selector (keyboardWillHide:)
               name: UIKeyboardWillHideNotification
             object: nil];
}

- (void) removeNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc removeObserver: self
                  name: UIKeyboardWillShowNotification
                object: nil];

    [nc removeObserver: self
                  name: UIKeyboardWillHideNotification
                object: nil];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    if (self.tracksKeyboard)
        [self removeNotificationObservers];

    [self->backgroundView_ release];
    [self->contentView_ release];
    [self->footerView_ release];
    [self->headerView_ release];

    [super dealloc];
}

#pragma mark UIKeyboard Notification Methods

- (void) keyboardWillHide: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];

    //
    // Restore original sizes of subviews; animate resizes so they will be in
    // sync with disappearance of keyboard:
    //
    NSValue        *tmpValue = [userInfo objectForKey: UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval  animationDuration;

    [tmpValue getValue: &animationDuration];

    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: animationDuration];

    self->keyboardHeight_ = 0.0f;

    [self layoutSubviews];

    [UIView commitAnimations];
}

//
// UIKeyboardBoundsUserInfoKey is deprecated in SDK 3.2; the following pragma
// switch keeps the compiler from bitching and moaning about it when we build
// universal:
//
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"

//
// UIKeyboardFrameEndUserInfoKey is apparently not declared with the
// "weak_import" attribute; therefore, we cannot check it against NULL when
// running on a 3.1.3 device/simulator without blowing up; thus, we must
// check for the equivalent string literal:
//
#define KLUGE_UIKeyboardFrameEndUserInfoKey @"UIKeyboardFrameEndUserInfoKey"

- (void) keyboardWillShow: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];
    CGRect        keyboardBounds;
    NSValue      *tmpValue;

    //
    // THIS IS BROKEN (see explanation above):
    //
    //    tmpValue = ((UIKeyboardFrameEndUserInfoKey != NULL) ?
    //                [userInfo objectForKey: UIKeyboardFrameEndUserInfoKey] :
    //                nil);
    //
    // SO DO THIS INSTEAD:
    //
    tmpValue = [userInfo objectForKey: KLUGE_UIKeyboardFrameEndUserInfoKey];

    if (!tmpValue)
    {
        //
        // Use "deprecated" key if necessary:
        //
        tmpValue = [userInfo objectForKey: UIKeyboardBoundsUserInfoKey];

        keyboardBounds = [tmpValue CGRectValue];
    }
    else
        //
        // Convert keyboard frame from window coordinates to local (i.e.,
        // "self") coordinates:
        //
        keyboardBounds = [self convertRect: [tmpValue CGRectValue]
                                    toView: nil];

    //
    // Reduce sizes of subviews so they will not be obscured by keyboard;
    // animate resizes so they will be in sync with appearance of keyboard:
    //
    tmpValue = [userInfo objectForKey: UIKeyboardAnimationDurationUserInfoKey];

    NSTimeInterval animationDuration;

    [tmpValue getValue: &animationDuration];

    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: animationDuration];

    self->keyboardHeight_ = CGRectGetHeight (keyboardBounds);

    [self layoutSubviews];

    [UIView commitAnimations];
}

@end
