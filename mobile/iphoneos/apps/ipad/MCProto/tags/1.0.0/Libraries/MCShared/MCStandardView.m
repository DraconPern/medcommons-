//
//  MCStandardView.m
//  MCShared
//
//  Created by J. G. Pusey on 4/13/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCStandardView.h"

#pragma mark -
#pragma mark Public Class MCSOAPNoteView
#pragma mark -

#pragma mark Internal Constants

@interface MCStandardView ()

- (void) addNotificationObservers;

- (void) removeNotificationObservers;

@end

@implementation MCStandardView

@synthesize backgroundView     = backgroundView_;
@synthesize contentView        = contentView_;
@synthesize footerView         = footerView_;
@synthesize headerView         = headerView_;
@synthesize keyboardHeight     = keyboardHeight_;
@dynamic    resizesForKeyboard;

#pragma mark Dynamic Property Methods

- (BOOL) resizesForKeyboard
{
    return (flags_.resizesForKeyboard ? YES : NO);
}

- (void) setResizesForKeyboard: (BOOL) resizesForKeyboard
{
    BOOL oldResizesForKeyboard = self.resizesForKeyboard;

    if (!oldResizesForKeyboard != !resizesForKeyboard)
    {
        flags_.resizesForKeyboard = resizesForKeyboard;

        if (resizesForKeyboard)
            [self addNotificationObservers];
        else
            [self removeNotificationObservers];
    }
}

#pragma mark Public Instance Methods

- (id) initWithFrame: (CGRect) frame
      backgroundView: (UIView *) backgroundView
         contentView: (UIView *) contentView
          headerView: (UIView *) headerView
          footerView: (UIView *) footerView
{
    assert (backgroundView != nil);
    assert (contentView != nil);
    
    if (self = [super initWithFrame: frame])
    {
        backgroundView_ = [backgroundView retain];
        contentView_ = [contentView retain];
        footerView_ = [footerView retain];
        headerView_ = [headerView retain];
        keyboardHeight_ = 0.0f;

        [backgroundView_ addSubview: headerView_];
        [backgroundView_ addSubview: footerView_];
        [backgroundView_ addSubview: contentView_];
        
        [self layoutSubviews];
        [self addSubview: backgroundView_];
    }
    
    return self;
}

- (void) layoutBackgroundView
{
    CGRect parentFrame = self.frame;
    
    parentFrame.size.height -= keyboardHeight_;
    
    backgroundView_.frame = parentFrame;
}

- (void) layoutContentView
{
    // let subclass handle ...
}

- (void) layoutFooterView
{
    // let subclass handle ...
}

- (void) layoutHeaderView
{
    // let subclass handle ...
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

#pragma mark Overridden UIView Methods

- (void) layoutSubviews
{
    [self layoutBackgroundView];
    
    if (headerView_)
        [self layoutHeaderView];
    
    if (footerView_)
        [self layoutFooterView];
    
    [self layoutContentView];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    if (self.resizesForKeyboard)
        [self removeNotificationObservers];
    
    [backgroundView_ release];
    [contentView_ release];
    [footerView_ release];
    [headerView_ release];
    
    [super dealloc];
}

#pragma mark UIKeyboard Notifications

- (void) keyboardWillHide: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];
    
    //
    // Restore original sizes of subviews; animate resizes so they will be in
    // sync with disappearance of keyboard:
    //
    NSValue        *animationDurationValue = [userInfo objectForKey: UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval  animationDuration;
    
    [animationDurationValue getValue: &animationDuration];
    
    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: animationDuration];
    
    keyboardHeight_ = 0.0f;
    
    [self layoutSubviews];
    
    [UIView commitAnimations];
}

- (void) keyboardWillShow: (NSNotification *) notification
{
    NSDictionary *userInfo = [notification userInfo];
    CGFloat       tmpKeyboardHeight;
    
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
    NSValue *frameValue = [userInfo objectForKey: UIKeyboardFrameEndUserInfoKey];
    CGSize   keyboardSize = [frameValue CGRectValue].size;
    CGSize   windowSize = self.window.frame.size;
    
    //
    // If keyboard width does not match window width, then assume we're in (or
    // rotating to) landscape mode (view controller's interfaceOrientation is
    // not reliable when about to rotate), else assume we're in (or rotating
    // to) portrait mode.
    //
    // Kinda klugey, but whaddya gonna do?
    //
    tmpKeyboardHeight = ((keyboardSize.width != windowSize.width) ?
                         keyboardSize.width :
                         keyboardSize.height);
#else
    NSValue *boundsValue = [userInfo objectForKey: UIKeyboardBoundsUserInfoKey];
    
    tmpKeyboardHeight = [boundsValue CGRectValue].size.height;
#endif
    
    //
    // Reduce sizes of subviews so they will not be obscured by keyboard;
    // animate resizes so they will be in sync with appearance of keyboard:
    //
    NSValue        *animationDurationValue = [userInfo objectForKey: UIKeyboardAnimationDurationUserInfoKey];
    NSTimeInterval  animationDuration;
    
    [animationDurationValue getValue: &animationDuration];
    
    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: animationDuration];
    
    keyboardHeight_ = tmpKeyboardHeight;
    
    [self layoutSubviews];
    
    [UIView commitAnimations];
}

@end
