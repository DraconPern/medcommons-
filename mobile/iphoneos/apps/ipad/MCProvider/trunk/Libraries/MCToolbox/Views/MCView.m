//
//  MCView.m
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

#import "MCView.h"

#pragma mark -
#pragma mark Public Class MCView
#pragma mark -

@interface MCView ()

- (void) addNotificationObservers;

- (void) removeNotificationObservers;

//
// Forward declarations:
//
- (void) keyboardWillHide: (NSNotification *) notification;

- (void) keyboardWillShow: (NSNotification *) notification;

@end

@implementation MCView

@synthesize keyboardHeight = keyboardHeight_;
@synthesize tracksKeyboard = tracksKeyboard_;
@synthesize userInfo       = userInfo_;

#pragma mark Public Instance Methods

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

- (void)dealloc
{
    if (self.tracksKeyboard)
        [self removeNotificationObservers];

    [self->userInfo_ release];

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

- (void) keyboardWillShow: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];
    NSValue      *tmpValue;

    tmpValue = [userInfo objectForKey: UIKeyboardFrameEndUserInfoKey];

    //
    // Convert keyboard frame from window coordinates to local (i.e.,
    // "self") coordinates:
    //
    CGRect keyboardBounds = [self convertRect: [tmpValue CGRectValue]
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
