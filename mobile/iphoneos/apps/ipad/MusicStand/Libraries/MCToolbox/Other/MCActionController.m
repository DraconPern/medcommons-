//
//  MCActionController.m
//  MCToolbox
//
//  Created by J. G. Pusey on 8/18/10.
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

#import "MCActionController.h"

#pragma mark -
#pragma mark Private Class MCActionButton
#pragma mark -

@interface MCActionButton : NSObject
{
@private

    SEL       action_;
    id        target_;
    NSString *title_;
    id        userInfo_;
}

@property (nonatomic, assign, readonly) SEL       action;
@property (nonatomic, assign, readonly) id        target;
@property (nonatomic, copy,   readonly) NSString *title;
@property (nonatomic, assign, readonly) id        userInfo;

+ (id) buttonWithTitle: (NSString *) title
                target: (id) target
                action: (SEL) action
              userInfo: (id) userInfo;

- (id) initWithTitle: (NSString *) title
              target: (id) target
              action: (SEL) action
            userInfo: (id) userInfo;

@end

@implementation MCActionButton

@synthesize action   = action_;
@synthesize target   = target_;
@synthesize title    = title_;
@synthesize userInfo = userInfo_;

#pragma mark Public Class Methods

+ (id) buttonWithTitle: (NSString *) title
                target: (id) target
                action: (SEL) action
              userInfo: (id) userInfo
{
    return [[[self alloc] initWithTitle: title
                                 target: target
                                 action: action
                               userInfo: userInfo]
            autorelease];
}

#pragma mark Public Instance Methods

- (id) initWithTitle: (NSString *) title
              target: (id) target
              action: (SEL) action
            userInfo: (id) userInfo
{
    self = [super init];

    if (self)
    {
        self->action_ = action;
        self->target_ = target;
        self->title_ = [title copy];
        self->userInfo_ = userInfo;
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->title_ release];

    [super dealloc];
}

@end


#pragma mark -
#pragma mark Public Class MCActionController
#pragma mark -

@interface MCActionController () <UIActionSheetDelegate>

@property (nonatomic, retain, readwrite) UIActionSheet  *actionSheet;
@property (nonatomic, retain, readwrite) MCActionButton *cancelButton;
@property (nonatomic, retain, readwrite) MCActionButton *destructiveButton;
@property (nonatomic, retain, readonly)  NSMutableArray *otherButtons;

@end

@implementation MCActionController

@synthesize actionSheet          = actionSheet_;
@synthesize actionSheetStyle     = actionSheetStyle_;
@synthesize cancelButton         = cancelButton_;
@synthesize destructiveButton    = destructiveButton_;
@dynamic    hasCancelButton;
@dynamic    hasDestructiveButton;
@dynamic    isVisible;
@dynamic    numberOfOtherButtons;
@synthesize otherButtons         = otherButtons_;
@synthesize title                = title_;

#pragma mark Public Instance Methods

- (void) addCancelButtonWithTitle: (NSString *) title
                           target: (id) target
                           action: (SEL) action
                         userInfo: (id) userInfo
{
    self.cancelButton = [MCActionButton buttonWithTitle: title
                                                 target: target
                                                 action: action
                                               userInfo: userInfo];

    self.actionSheet = nil;
}

- (void) addDestructiveButtonWithTitle: (NSString *) title
                                target: (id) target
                                action: (SEL) action
                              userInfo: (id) userInfo
{
    self.destructiveButton = [MCActionButton buttonWithTitle: title
                                                      target: target
                                                      action: action
                                                    userInfo: userInfo];

    self.actionSheet = nil;
}

- (void) addOtherButtonWithTitle: (NSString *) title
                          target: (id) target
                          action: (SEL) action
                        userInfo: (id) userInfo
{
    [self.otherButtons addObject: [MCActionButton buttonWithTitle: title
                                                           target: target
                                                           action: action
                                                         userInfo: userInfo]];

    self.actionSheet = nil;
}

- (void) dismissWithCancelButtonAnimated: (BOOL) animated
{
    [self.actionSheet dismissWithClickedButtonIndex: self.actionSheet.cancelButtonIndex
                                           animated: animated];
}

- (BOOL) hasCancelButton
{
    return (self.cancelButton != nil);
}

- (BOOL) hasDestructiveButton
{
    return (self.destructiveButton != nil);
}

- (id) initWithTitle: (NSString *) title
{
    self = [super init];

    if (self)
    {
        self->actionSheetStyle_ = UIActionSheetStyleDefault;
        self->otherButtons_ = [NSMutableArray new];
        self->title_ = [title copy];
    }

    return self;
}

- (BOOL) isVisible
{
    return (self.actionSheet ? self.actionSheet.isVisible : NO);
}

- (NSUInteger) numberOfOtherButtons
{
    return [self.otherButtons count];
}

- (void) removeAllOtherButtons
{
    if ([self.otherButtons count] > 0)
    {
        [self.otherButtons removeAllObjects];

        self.actionSheet = nil;
    }
}

- (void) removeCancelButton
{
    if (self.cancelButton)
    {
        self.cancelButton = nil;

        self.actionSheet = nil;
    }
}

- (void) removeDestructiveButton
{
    if (self.destructiveButton)
    {
        self.destructiveButton = nil;

        self.actionSheet = nil;
    }
}

- (void) removeOtherButtonWithTitle: (NSString *) title
                             target: (id) target
                             action: (SEL) action
                           userInfo: (id) userInfo
{
    //
    // Remove first button that matches criteria:
    //
    NSUInteger idx = 0;

    for (MCActionButton *btn in self.otherButtons)
    {
        if ((btn.action == action) &&
            (btn.target == target) &&
            ((btn.title == title) ||    // in case title is nil
             [btn.title isEqualToString: title]) &&
            (btn.userInfo == userInfo))
        {
            [self.otherButtons removeObjectAtIndex: idx];

            self.actionSheet = nil;

            break;
        }

        idx++;
    }
}

- (void) setActionSheetStyle: (UIActionSheetStyle) actionSheetStyle
{
    if (self->actionSheetStyle_ != actionSheetStyle)
    {
        self->actionSheetStyle_ = actionSheetStyle;

        if (self->actionSheet_)
            self->actionSheet_.actionSheetStyle = actionSheetStyle;
    }
}

- (void) setTitle: (NSString *) title
{
    if (self->title_ != title)
    {
        [self->title_ release];

        self->title_ = [title copy];

        if (self->actionSheet_)
            self->actionSheet_.title = title;
    }
}

- (void) showFromBarButtonItem: (UIBarButtonItem *) item
                      animated: (BOOL) animated
{
    [self.actionSheet showFromBarButtonItem: item
                                   animated: animated];
}

- (void) showFromRect: (CGRect) rect
               inView: (UIView *) view
             animated: (BOOL) animated
{
    [self.actionSheet showFromRect: rect
                            inView: view
                          animated: animated];
}

- (void) showFromTabBar: (UITabBar *) view
{
    [self.actionSheet showFromTabBar: view];
}

- (void) showFromToolbar: (UIToolbar *) view
{
    [self.actionSheet showFromToolbar: view];
}

- (void) showInView: (UIView *) view
{
    [self.actionSheet showInView: view];
}

#pragma mark Private Instance Methods

- (UIActionSheet *) actionSheet
{
    if (!self->actionSheet_)
    {
        self->actionSheet_ = [[UIActionSheet alloc] initWithTitle: self.title
                                                         delegate: self
                                                cancelButtonTitle: nil
                                           destructiveButtonTitle: nil
                                                otherButtonTitles: nil];

        self->actionSheet_.actionSheetStyle = self.actionSheetStyle;

        //
        // MUST add destructive button (if any) first:
        //
        if (self.destructiveButton)
            self->actionSheet_.destructiveButtonIndex = [self->actionSheet_ addButtonWithTitle: self.destructiveButton.title];

        //
        // Add any other buttons:
        //
        for (MCActionButton *btn in self.otherButtons)
            [self->actionSheet_ addButtonWithTitle: btn.title];

        //
        // MUST add cancel button (if any) last:
        //
        if (self.cancelButton)
            self->actionSheet_.cancelButtonIndex = [self->actionSheet_ addButtonWithTitle: self.cancelButton.title];
    }

    return self->actionSheet_;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->actionSheet_ release];
    [self->cancelButton_ release];
    [self->destructiveButton_ release];
    [self->otherButtons_ release];
    [self->title_ release];

    [super dealloc];
}

#pragma mark UIActionSheetDelegate Methods

- (void) actionSheet: (UIActionSheet *) actSheet
clickedButtonAtIndex: (NSInteger) buttonIdx
{
    NSAssert (self->actionSheet_ == actSheet,
              @"Bad action sheet!");

    MCActionButton *button = nil;

    if (buttonIdx == actSheet.cancelButtonIndex)
        button = self.cancelButton;
    else if (buttonIdx == actSheet.destructiveButtonIndex)
        button = self.destructiveButton;
    //
    // UIActionSheet firstOtherButtonIndex property is NOT reliable; use
    // alternate method (but only if button index is nonnegative):
    //
    else if (buttonIdx >= 0)
    {
        //
        // Assume if destructive button is defined that it comes first and
        // thus its index is either -1 or 0; first "other" button index
        // should therefore be one past that:
        //
        NSUInteger idx = buttonIdx - (actSheet.destructiveButtonIndex + 1);

        button = [self.otherButtons objectAtIndex: idx];
    }

    //
    // If button exists and has associated action and target, perform it:
    //
    if (button && button.action && button.target)
    {
        //
        // If selector name ends with colon, invoke with userInfo argument:
        //
        if ([NSStringFromSelector (button.action) hasSuffix: @":"])
            [button.target performSelector: button.action
                                withObject: button.userInfo];
        else
            [button.target performSelector: button.action];
    }
}

@end
