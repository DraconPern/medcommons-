//
//  MCMasterDetailViewController.m
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
//  Based on Jeffrey Beauchamp's CustomSplitViewController class as presented
//  in <https://devforums.apple.com/message/171831>. It has been extensively
//  modified to the point of unrecognizability.
//

#import "MCMasterDetailViewController.h"
#import "MCNavigationController.h"

#pragma mark -
#pragma mark Public Class MCMasterDetailViewController
#pragma mark -

#pragma mark Internal Constants

#define DEFAULT_GUTTER_WIDTH        1.0f
#define DEFAULT_MASTER_COLUMN_WIDTH 320.0f

@interface MCMasterDetailViewController () <UINavigationControllerDelegate, UIPopoverControllerDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem     *barButtonItem;
@property (nonatomic, assign, readwrite) BOOL                 isLandscape;
@property (nonatomic, retain, readwrite) UIPopoverController *popoverController;
@property (nonatomic, assign, readwrite) BOOL                 showingBarButtonItem;

- (void) layoutSplitViews;

- (void) presentMasterPopoverFromBarButtonItem: (UIBarButtonItem *) bbi;

- (BOOL) shouldHideMasterView;

- (void) toggleMasterPopoverBarButtonItemAnimated: (BOOL) animated;

@end

@implementation MCMasterDetailViewController

@synthesize barButtonItem              = barButtonItem_;
@synthesize detailNavigationController = detailNavigationController_;
@dynamic    detailViewController;
@synthesize gutterWidth                = gutterWidth_;
@synthesize hidesMasterViewInLandscape = hidesMasterViewInLandscape_;
@synthesize hidesMasterViewInPortrait  = hidesMasterViewInPortrait_;
@synthesize isLandscape                = isLandscape_;
@synthesize masterColumnWidth          = masterColumnWidth_;
@synthesize masterNavigationController = masterNavigationController_;
@dynamic    masterViewController;
@synthesize navigationBarStyle         = navigationBarStyle_;
@synthesize popoverController          = popoverController_;
@synthesize showingBarButtonItem       = showingBarButtonItem_;

#pragma mark Public Instance Methods

- (UIViewController *) detailViewController
{
    return self.detailNavigationController.rootViewController;
}

- (void) dismissMasterPopoverAnimated: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.dismissMasterPopoverAnimated: ***");

    if (self.popoverController)
    {
        if (self.popoverController.popoverVisible)
            [self.popoverController dismissPopoverAnimated: animated];

        self.popoverController = nil;

        //
        // When self.masterNavigationController was added as the content view
        // controller to self.popoverController in
        // presentMasterPopoverFromBarButtonItem, its view was removed from
        // self.view; restore it to its original position now:
        //
        if ([self isViewLoaded])
            [self.view insertSubview: self.masterNavigationController.view
                             atIndex: 0];

        //
        // Kluge to fix navigation bar weirdness upon dismissal of popover
        // controller:
        //
        UINavigationBar *navBar = self.masterNavigationController.navigationBar;

        if (!CGRectEqualToRect (navBar.frame, navBar.bounds))
            navBar.frame = navBar.bounds;

        [self.masterNavigationController.topViewController viewWillAppear: animated];
    }
}

- (id) initWithMasterViewController: (UIViewController *) mvc
               detailViewController: (UIViewController *) dvc
{
    self = [super init];

    if (self)
    {
        self->masterNavigationController_ = [[MCNavigationController alloc] initWithRootViewController: mvc
                                                                                  parentViewController: self];

        self->detailNavigationController_ = [[MCNavigationController alloc] initWithRootViewController: dvc
                                                                                  parentViewController: self];

        self->gutterWidth_ = DEFAULT_GUTTER_WIDTH;
        self->hidesMasterViewInLandscape_ = NO;
        self->hidesMasterViewInPortrait_ = YES;
        self->masterColumnWidth_ = DEFAULT_MASTER_COLUMN_WIDTH;
        self->navigationBarStyle_ = UIBarStyleDefault;

        self.masterNavigationController.delegate = self;
        self.detailNavigationController.delegate = self;
    }

    return self;
}

- (UIViewController *) masterViewController
{
    return self.masterNavigationController.rootViewController;
}

- (void) setDetailViewController: (UIViewController *) dvc
{
    [self setDetailViewController: dvc
                         animated: NO];
}

- (void) setDetailViewController: (UIViewController *) dvc
                        animated: (BOOL) animated
{
    if (self.detailViewController != dvc)
    {
        [self dismissMasterPopoverAnimated: animated];

        [self.detailNavigationController setRootViewController: dvc
                                                      animated: animated];

        if ([self isViewLoaded])
        {
            [self layoutSplitViews];
            [self toggleMasterPopoverBarButtonItemAnimated: animated];
        }
    }
}

- (void) setGutterWidth: (CGFloat) width
{
    [self setGutterWidth: width
                animated: NO];
}

- (void) setGutterWidth: (CGFloat) width
               animated: (BOOL) animated
{
    if (self->gutterWidth_ != width)
    {
        self->gutterWidth_ = width;

        if ([self isViewLoaded])
            [self layoutSplitViews];
    }
}

- (void) setHidesMasterViewInLandscape: (BOOL) hides
{
    [self setHidesMasterViewInLandscape: hides
                               animated: NO];
}

- (void) setHidesMasterViewInLandscape: (BOOL) hides
                              animated: (BOOL) animated
{
    if (self->hidesMasterViewInLandscape_ != hides)
    {
        self->hidesMasterViewInLandscape_ = hides;

        if ([self isViewLoaded])
        {
            [self layoutSplitViews];
            [self toggleMasterPopoverBarButtonItemAnimated: animated];
        }
    }
}

- (void) setHidesMasterViewInPortrait: (BOOL) hides
{
    [self setHidesMasterViewInPortrait: hides
                              animated: NO];
}

- (void) setHidesMasterViewInPortrait: (BOOL) hides
                             animated: (BOOL) animated
{
    if (self->hidesMasterViewInPortrait_ != hides)
    {
        self->hidesMasterViewInPortrait_ = hides;

        if ([self isViewLoaded])
        {
            [self layoutSplitViews];
            [self toggleMasterPopoverBarButtonItemAnimated: animated];
        }
    }
}

- (void) setMasterColumnWidth: (CGFloat) width
{
    [self setMasterColumnWidth: width
                      animated: NO];
}

- (void) setMasterColumnWidth: (CGFloat) width
                     animated: (BOOL) animated
{
    if (self->masterColumnWidth_ != width)
    {
        self->masterColumnWidth_ = width;

        if ([self isViewLoaded])
            [self layoutSplitViews];
    }
}

- (void) setMasterViewController: (UIViewController *) mvc
{
    [self setMasterViewController: mvc
                         animated: NO];
}

- (void) setMasterViewController: (UIViewController *) mvc
                        animated: (BOOL) animated
{
    if (self.masterViewController != mvc)
    {
        [self.masterNavigationController setRootViewController: mvc
                                                      animated: animated];

        if ([self isViewLoaded])
            [self layoutSplitViews];
    }
}

- (void) setNavigationBarStyle: (UIBarStyle) barStyle
{
    if (self->navigationBarStyle_ != barStyle)
    {
        self->navigationBarStyle_ = barStyle;

        self.detailNavigationController.navigationBar.barStyle = barStyle;
        self.masterNavigationController.navigationBar.barStyle = barStyle;
    }
}

#pragma mark Private Instance Methods

- (void) layoutSplitViews
{
    //NSLog (@"*** MCMasterDetailViewController.layoutSplitViews ***");

    CGRect bounds = CGRectStandardize (self.view.bounds);

    if ([self shouldHideMasterView])
    {
        self.detailNavigationController.view.frame = bounds;

        bounds.origin.x -= self.masterColumnWidth;
        bounds.size.width = self.masterColumnWidth;

        self.masterNavigationController.view.frame = bounds;
    }
    else
    {
        [self dismissMasterPopoverAnimated: NO];

        self.masterNavigationController.view.frame = CGRectMake (bounds.origin.x,
                                                                 bounds.origin.y,
                                                                 self.masterColumnWidth,
                                                                 bounds.size.height);

        CGFloat tmpWidth = self.masterColumnWidth + self.gutterWidth;

        bounds.origin.x += tmpWidth;
        bounds.size.width -= tmpWidth;

        self.detailNavigationController.view.frame = bounds;
    }

    //
    // These somehow get unset; jam them in again:
    //
    self.detailNavigationController.navigationBar.barStyle = self.navigationBarStyle;
    self.masterNavigationController.navigationBar.barStyle = self.navigationBarStyle;
}

- (void) presentMasterPopoverFromBarButtonItem: (UIBarButtonItem *) bbi
{
    //NSLog (@"*** MCMasterDetailViewController.presentMasterPopoverFromBarButtonItem: %@ ***", bbi);

    if (!self.popoverController)
    {
        self.popoverController = [[[UIPopoverController alloc]
                                   initWithContentViewController: self.masterNavigationController]
                                  autorelease];

        self.popoverController.delegate = self;

        [self.popoverController presentPopoverFromBarButtonItem: bbi
                                       permittedArrowDirections: UIPopoverArrowDirectionUp
                                                       animated: YES];
    }
}

- (BOOL) shouldHideMasterView
{
    //
    // If the detail view controller (at the top of the navigation stack) has
    // an explicit opinion on the matter (via the appropriate optional
    // protocol property method), honor it; otherwise, behave according to
    // the settings on the master-detail view controller:
    //
    UIViewController *dvc = self.detailNavigationController.topViewController;

    return (self.isLandscape ?
            ([dvc respondsToSelector: @selector (hidesMasterViewInLandscape)] ?
             dvc.hidesMasterViewInLandscape :
             self.hidesMasterViewInLandscape) :
            ([dvc respondsToSelector: @selector (hidesMasterViewInPortrait)] ?
             dvc.hidesMasterViewInPortrait :
             self.hidesMasterViewInPortrait));
}

- (void) toggleMasterPopoverBarButtonItemAnimated: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.toggleMasterPopoverBarButtonItem ***");

    //
    // The popover bar item should always be showing when the master view is
    // hidden and should be hidden when the master view is showing:
    //
    BOOL shouldShowBBI = [self shouldHideMasterView];

    //
    // If we should be showing the popover bar button item, but are not
    // currently showing it, we should do so; however, if the detail view
    // controller does not respond to the show selector, then we can't very
    // well show the popover bar button item, so forget about it:
    //
    if (shouldShowBBI &&
        !self.showingBarButtonItem &&
        [self.detailViewController respondsToSelector: @selector (showMasterPopoverBarButtonItem:animated:)])
    {
        //
        // Create the popover bar button item on demand:
        //
        if (!self.barButtonItem)
            self.barButtonItem = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"Menu", @"")
                                                                   style: UIBarButtonItemStylePlain
                                                                  target: self
                                                                  action: @selector (presentMasterPopoverFromBarButtonItem:)]
                                  autorelease];

        [self.detailViewController showMasterPopoverBarButtonItem: self.barButtonItem
                                                         animated: animated];

        self.showingBarButtonItem = YES;
    }

    //
    // If we should NOT be showing the popover bar button item, but we are
    // currently showing it, we should hide it; however, if the detail view
    // controller does not respond to the hide selector, then we can't very
    // well hide the popover bar button item, so forget about it:
    //
    if (!shouldShowBBI &&
        self.showingBarButtonItem &&
        [self.detailViewController respondsToSelector: @selector (hideMasterPopoverBarButtonItem:animated:)])
    {
        //
        // If there is no bar button item, then it cannot have been shown,
        // thus there is nothing to hide:
        //
        if (self.barButtonItem)
            [self.detailViewController hideMasterPopoverBarButtonItem: self.barButtonItem
                                                             animated: animated];

        self.showingBarButtonItem = NO;
    }
}

#pragma mark Overridden UIViewController Methods

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) fromOrient
{
    //NSLog (@"*** MCMasterDetailViewController.didRotateFromInterfaceOrientation ***");

    [self.masterNavigationController didRotateFromInterfaceOrientation: fromOrient];
    [self.detailNavigationController didRotateFromInterfaceOrientation: fromOrient];

    [self toggleMasterPopoverBarButtonItemAnimated: YES];
}

- (void) loadView
{
    //NSLog (@"*** MCMasterDetailViewController.loadView ***");

    CGRect tmpFrame = (self.parentViewController ?
                       self.parentViewController.view.bounds :
                       [UIScreen mainScreen].applicationFrame);

    self.view = [[[UIView alloc] initWithFrame: tmpFrame]
                 autorelease];

    self.view.backgroundColor = [UIColor darkGrayColor];
    self.view.clipsToBounds = YES;

    self.masterNavigationController.view.clipsToBounds = YES;
    self.detailNavigationController.view.clipsToBounds = YES;

    [self.view addSubview: self.masterNavigationController.view];
    [self.view addSubview: self.detailNavigationController.view];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
{
    //NSLog (@"*** MCMasterDetailViewController.shouldAutorotateToInterfaceOrientation ***");

    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.viewDidAppear ***");

    [super viewDidAppear: animated];

    [self.masterNavigationController viewDidAppear: animated];
    [self.detailNavigationController viewDidAppear: animated];

    [self toggleMasterPopoverBarButtonItemAnimated: animated];
}

- (void) viewDidDisappear: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.viewDidDisappear ***");

    [super viewDidDisappear: animated];

    [self.masterNavigationController viewDidDisappear: animated];
    [self.detailNavigationController viewDidDisappear: animated];
}

- (void) viewDidLoad
{
    //NSLog (@"*** MCMasterDetailViewController.viewDidLoad ***");

    [super viewDidLoad];

    [self layoutSplitViews];
}

- (void) viewDidUnload
{
    //NSLog (@"*** MCMasterDetailViewController.viewDidUnload ***");

    [super viewDidUnload];

    [self.masterNavigationController viewDidUnload];
    [self.detailNavigationController viewDidUnload];
}

- (void) viewWillAppear: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.viewWillAppear ***");

    [super viewWillAppear: animated];

    [self.masterNavigationController viewWillAppear: animated];
    [self.detailNavigationController viewWillAppear: animated];
}

- (void) viewWillDisappear: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.viewWillDisappear ***");

    [super viewWillDisappear: animated];

    [self.masterNavigationController viewWillDisappear: animated];
    [self.detailNavigationController viewWillDisappear: animated];
}

- (void) willAnimateRotationToInterfaceOrientation: (UIInterfaceOrientation) toOrient
                                          duration: (NSTimeInterval) duration
{
    //NSLog (@"*** MCMasterDetailViewController.willAnimateRotationToInterfaceOrientation ***");

    self.isLandscape = UIInterfaceOrientationIsLandscape (toOrient);

    [self.masterNavigationController willAnimateRotationToInterfaceOrientation: toOrient
                                                                      duration: duration];

    [self.detailNavigationController willAnimateRotationToInterfaceOrientation: toOrient
                                                                      duration: duration];

    [self layoutSplitViews];
}

- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
                                 duration: (NSTimeInterval) duration
{
    //NSLog (@"*** MCMasterDetailViewController.willRotateToInterfaceOrientation ***");

    [self.masterNavigationController willRotateToInterfaceOrientation: toOrient
                                                             duration: duration];

    [self.detailNavigationController willRotateToInterfaceOrientation: toOrient
                                                             duration: duration];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->barButtonItem_ release];
    [self->detailNavigationController_ release];
    [self->masterNavigationController_ release];
    [self->popoverController_ release];

    [super dealloc];
}

#pragma mark UINavigationControllerDelegate Methods

- (void) navigationController: (UINavigationController *) nc
        didShowViewController: (UIViewController *) vc
                     animated: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.navigationController: %@ didShowViewController: %@ ***", nc, vc);
}

- (void) navigationController: (UINavigationController *) nc
       willShowViewController: (UIViewController *) vc
                     animated: (BOOL) animated
{
    //NSLog (@"*** MCMasterDetailViewController.navigationController: %@ willShowViewController: %@ ***", nc, vc);

    [self layoutSplitViews];
    [self toggleMasterPopoverBarButtonItemAnimated: animated];
}

#pragma mark UIPopoverControllerDelegate Methods

- (BOOL) popoverControllerShouldDismissPopover: (UIPopoverController *) pc
{
    //NSLog (@"*** MCMasterDetailViewController.popoverControllerShouldDismissPopover: %@ ***", pc);

    return YES;
}

- (void) popoverControllerDidDismissPopover: (UIPopoverController *) pc
{
    //NSLog (@"*** MCMasterDetailViewController.popoverControllerDidDismissPopover: %@ ***", pc);

    [self dismissMasterPopoverAnimated: YES];
}

@end

#pragma mark -
#pragma mark Public Class UIViewController Additions
#pragma mark -

@implementation UIViewController (MCMasterDetailViewController)

@dynamic masterDetailViewController;

#pragma mark Public Instance Methods

- (MCMasterDetailViewController *) masterDetailViewController
{
    UIViewController *vc = self.parentViewController;

    while (vc)
    {
        if ([vc isKindOfClass: [MCMasterDetailViewController class]])
            break;

        vc = vc.parentViewController;
    }

    return (MCMasterDetailViewController *) vc;
}

@end
