//
//  MCMasterDetailViewController.h
//  MCToolbox
//
//  Created by J. G. Pusey on 4/21/10.
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

//
// Informal protocol "MCDetailViewController" on UIViewController -- MUST
// come BEFORE MCMasterDetailViewController interface:
//
/**
 * The MCDetailViewController informal protocol ...
 */
@interface UIViewController (MCDetailViewController)

/**
 * @brief A Boolean value indicating whether the master view controller is
 *        hidden in landscape mode. (read-only)
 */
@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

/**
 * @brief A Boolean value indicating whether the master view controller is
 *        hidden in portrait mode. (read-only)
 */
@property (nonatomic, assign, readonly) BOOL hidesMasterViewInPortrait;

/**
 * @brief Hides ...
 *
 * @param bbi
 * @param animated
 */
- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
                               animated: (BOOL) animated;

/**
 * @brief Shows ...
 *
 * @param bbi
 * @param animated
 */
- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
                               animated: (BOOL) animated;

@end

/**
 * The MCMasterDetailViewController class is a container view controller that
 * manages the presentation of two side-by-side view controllers. It replaces
 * and enhances the functionality of the UISplitViewController class.
 */
@interface MCMasterDetailViewController : UIViewController
{
@private

    UIBarButtonItem        *barButtonItem_;
    UINavigationController *detailNavigationController_;
    CGFloat                 gutterWidth_;
    CGFloat                 masterColumnWidth_;
    UINavigationController *masterNavigationController_;
    UIBarStyle              navigationBarStyle_;
    UIPopoverController    *popoverController_;
    //
    // Flags:
    //
    BOOL                    hidesMasterViewInLandscape_;
    BOOL                    hidesMasterViewInPortrait_;
    BOOL                    isLandscape_;
    BOOL                    showingBarButtonItem_;
}

/**
 * @brief The navigation controller that manages the detail view controller.
 *        (read-only)
 */
@property (nonatomic, retain, readonly)  UINavigationController *detailNavigationController;

/**
 * @brief The detail view controller managed by the receiver.
 */
@property (nonatomic, retain, readwrite) UIViewController       *detailViewController;

/**
 * @brief The width of the gutter between the master and detail view
 *        controllers.
 */
@property (nonatomic, assign, readwrite) CGFloat                 gutterWidth;

/**
 * @brief A Boolean value indicating whether the master view controller is
 *        hidden in landscape mode.
 */
@property (nonatomic, assign, readwrite) BOOL                    hidesMasterViewInLandscape;

/**
 * @brief A Boolean value indicating whether the master view controller is
 *        hidden in portrait mode.
 */
@property (nonatomic, assign, readwrite) BOOL                    hidesMasterViewInPortrait;

///**
// * @brief Bogus property to test a couple Doxygen/DoxyClean things.
// */
//@property (assign, readwrite, getter=isLandscape, setter   =
//                slamPortrait:) BOOL landscape;

/**
 * @brief The width of the master view controller.
 */
@property (nonatomic, assign, readwrite) CGFloat                 masterColumnWidth;

/**
 * @brief The navigation controller that manages the master view controller.
 *        (read-only)
 */
@property (nonatomic, retain, readonly)  UINavigationController *masterNavigationController;

/**
 * @brief The master view controller managed by the receiver.
 */
@property (nonatomic, retain, readwrite) UIViewController       *masterViewController;

/**
 * @brief The appearance of the navigation bars managed by the receiver.
 */
@property (nonatomic, assign, readwrite) UIBarStyle              navigationBarStyle;

/**
 * @brief Dismisses the master popover programmatically.
 *
 * @param animated
 */
- (void) dismissMasterPopoverAnimated: (BOOL) animated;

/**
 * @brief Initializes and returns a newly created master-detail view controller.
 *
 * @param mvc
 * @param dvc
 */
- (id) initWithMasterViewController: (UIViewController *) mvc
               detailViewController: (UIViewController *) dvc;

/**
 * @brief Replaces the detail view controller currently managed by the receiver
 *        with the specified view controller.
 *
 * @param dvc      The new detail view controller
 * @param animated If @c YES, animate the pushing or popping of the top detail
 *                 view controller. If @c NO, replace the detail view
 *                 without any animations.
 */
- (void) setDetailViewController: (UIViewController *) dvc
                        animated: (BOOL) animated;

/**
 * @brief Sets the width of the gutter between the master and detail view
 *        controllers, optionally animating the transition.
 *
 * @param width
 * @param animated
 */
- (void) setGutterWidth: (CGFloat) width
               animated: (BOOL) animated;

/**
 * @brief Sets ...
 *
 * @param hides
 * @param animated
 */
- (void) setHidesMasterViewInLandscape: (BOOL) hides
                              animated: (BOOL) animated;

/**
 * @brief Sets ...
 *
 * @param hides
 * @param animated
 */
- (void) setHidesMasterViewInPortrait: (BOOL) hides
                             animated: (BOOL) animated;

/**
 * @brief Sets the width of the master view controller, optionally animating
 *        the transition.
 *
 * @param width
 * @param animated
 */
- (void) setMasterColumnWidth: (CGFloat) width
                     animated: (BOOL) animated;

/**
 * @brief Sets ...
 *
 * @param mvc
 * @param animated
 */
- (void) setMasterViewController: (UIViewController *) mvc
                        animated: (BOOL) animated;

@end

//
// UIViewController convenience addition:
//
/**
 * The MCMasterDetailViewController class adds programming interfaces to
 * UIViewController to facilitate ...
 */
@interface UIViewController (MCMasterDetailViewController)

/**
 * @brief A parent or ancestor that is a master-detail view controller.
 *        (read-only)
 */
@property (nonatomic, assign, readonly) MCMasterDetailViewController *masterDetailViewController;

@end
