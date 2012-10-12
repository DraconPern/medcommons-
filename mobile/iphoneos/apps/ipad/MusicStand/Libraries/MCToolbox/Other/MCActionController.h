//
//  MCActionController.h
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

#import <UIKit/UIKit.h>

@class MCActionButton;

/**
 * The MCActionController class ...
 */
@interface MCActionController : NSObject
{
@private

    UIActionSheet      *actionSheet_;
    UIActionSheetStyle  actionSheetStyle_;
    MCActionButton     *cancelButton_;
    MCActionButton     *destructiveButton_;
    NSMutableArray     *otherButtons_;
    NSString           *title_;
}

@property (nonatomic, assign, readwrite) UIActionSheetStyle  actionSheetStyle;
@property (nonatomic, assign, readonly)  BOOL                hasCancelButton;
@property (nonatomic, assign, readonly)  BOOL                hasDestructiveButton;
@property (nonatomic, assign, readonly)  BOOL                isVisible;
@property (nonatomic, assign, readonly)  NSUInteger          numberOfOtherButtons;
@property (nonatomic, copy,   readwrite) NSString           *title;

- (void) addCancelButtonWithTitle: (NSString *) title
                           target: (id) target
                           action: (SEL) action
                         userInfo: (id) userInfo;

- (void) addDestructiveButtonWithTitle: (NSString *) title
                                target: (id) target
                                action: (SEL) action
                              userInfo: (id) userInfo;

- (void) addOtherButtonWithTitle: (NSString *) title
                          target: (id) target
                          action: (SEL) action
                        userInfo: (id) userInfo;

- (void) dismissWithCancelButtonAnimated: (BOOL) animated;

- (id) initWithTitle: (NSString *) title;

- (void) removeAllOtherButtons;

- (void) removeCancelButton;

- (void) removeDestructiveButton;

- (void) removeOtherButtonWithTitle: (NSString *) title
                             target: (id) target
                             action: (SEL) action
                           userInfo: (id) userInfo;

- (void) showFromBarButtonItem: (UIBarButtonItem *) item
                      animated: (BOOL) animated;

- (void) showFromRect: (CGRect) rect
               inView: (UIView *) view
             animated: (BOOL) animated;

- (void) showFromTabBar: (UITabBar *) view;

- (void) showFromToolbar: (UIToolbar *) view;

- (void) showInView: (UIView *) view;

@end
