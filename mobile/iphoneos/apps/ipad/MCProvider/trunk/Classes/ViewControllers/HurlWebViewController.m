//
//  HurlWebViewController.m
//  MCProvider
//
//  Created by Bill Donner on 4/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DetailViewController+Bill.h"
#import "Group.h"
#import "HurlWebViewController.h"
#import "Member.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "ShareActionController.h"

#pragma mark -
#pragma mark Public Class HurlWebViewController
#pragma mark -

#pragma mark Internal Constants

@interface HurlWebViewController ()

@property (nonatomic, retain, readwrite) UIBarButtonItem    *actionButton;
@property (nonatomic, retain, readwrite) MCActionController *actionController;
@property (nonatomic, retain, readwrite) UIBarButtonItem    *backButton;

- (void) chooseAction: (id) sender;

- (void) showActionController;

- (void) updateNavigationItemAnimated: (BOOL) animated;

@end

@implementation HurlWebViewController

@synthesize actionButton     = actionButton_;
@synthesize actionController = actionController_;
@synthesize backButton       = backButton_;

#pragma mark Private Instance Methods

- (void) chooseAction: (id) sender
{
    SettingsManager *settings = self.appDelegate.settingsManager;

    if (!self.actionController.isVisible && (settings.featureLevel > 0))
    {
        self.actionController = [[[ShareActionController alloc] initWithViewController: self
                                                                               webView: self.contentView]
                                 autorelease];

        if (self.actionController)
            [self showActionController];
    }
}

- (void) showActionController
{
    if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPad)
        [self.actionController showFromBarButtonItem: self.navigationItem.rightBarButtonItem
                                            animated: YES];
    else
        [self.actionController showInView: self.view];
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    if (!self.actionButton)
        self.actionButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemAction
                                                                           target: self
                                                                           action: @selector (chooseAction:)]
                             autorelease];

    if (!self.backButton)
        self.backButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"CCR", @"")
                                                            style: UIBarButtonItemStylePlain
                                                           target: nil
                                                           action: NULL]
                           autorelease];

    AppDelegate    *appDel = self.appDelegate;
    SessionManager *sm = appDel.sessionManager;
    Member         *member = sm.loginSession.memberInFocus;

    self.navigationItem.title = [NSString stringWithFormat:
                                 NSLocalizedString (@"CCR for %@", @""),
                                 member.name];

    self.navigationItem.backBarButtonItem = self.backButton;

    //
    // Determine if there are any action buttons to display:
    //
    SettingsManager *settings = appDel.settingsManager;
    BOOL             showActionButton;

    showActionButton = (((settings.featureLevel == 1) && settings.useNotes) ||
                        (settings.featureLevel >= 2));

    [self.navigationItem setRightBarButtonItem: (showActionButton ?
                                                 self.actionButton :
                                                 nil)
                                      animated: animated];
}

#pragma mark -
#pragma mark Responding to gestures


- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {

    return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}

- (void)handleTapFrom:(UITapGestureRecognizer *)recognizer {

    CGPoint location = [recognizer locationInView:self.view];

    NSLog (@"handleTapFrom x %f y %f", location.x, location.y);

}
- (void)handleDublTapFrom:(UITapGestureRecognizer *)recognizer {

    CGPoint location = [recognizer locationInView:self.view];
    NSLog (@"handleDublTapFrom x %f y %f", location.x, location.y);
}
- (void)handleTwoFingerTapFrom:(UITapGestureRecognizer *)recognizer {

    CGPoint location = [recognizer locationInView:self.view];

    NSLog (@"handleTwoFingerFrom x %f y %f", location.x, location.y);

}

- (void)handleSwipeFrom:(UISwipeGestureRecognizer *)recognizer {

    CGPoint location = [recognizer locationInView:self.view];


    if (recognizer.direction == UISwipeGestureRecognizerDirectionLeft) {
        location.x -= 220.0f;
        NSLog (@"UISwipeGestureRecognizerDirectionLeft");
    }
    else {
        location.x += 220.0f;
        NSLog (@"UISwipeGestureRecognizerDirectionRight");
    }

}


- (void)handleRotationFrom:(UIRotationGestureRecognizer *)recognizer {

    CGPoint location = [recognizer locationInView:self.view];
    NSLog (@"handleRotationFrom x %f y %f", location.x, location.y);

}

#pragma mark Overridden UIViewController Methods

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    // re-request if we pop back from the camera
    [self refresh];
    [self updateNavigationItemAnimated: animated];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];

    if (self.actionController.isVisible)
        [self.actionController dismissWithCancelButtonAnimated: YES];
}


- (void)viewDidLoad {
    [super viewDidLoad];

    /*
     Create and configure the four recognizers. Add each to the view as a gesture recognizer.
     */
    UIGestureRecognizer *recognizer;

    /*
     Create a tap recognizer and add it to the view.
     */
    UITapGestureRecognizer *tecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapFrom:)];
    tecognizer.delegate = self;
    [self.view addGestureRecognizer:tecognizer];
    [tecognizer release];

    /*
     Create a dubl tap recognizer and add it to the view.
     */
    tecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleDublTapFrom:)];
    tecognizer.delegate = self;

    tecognizer.numberOfTouchesRequired = 1;
    tecognizer.numberOfTapsRequired = 2;
    [self.view addGestureRecognizer:tecognizer];
    [tecognizer release];

    /*
     Two finger tap
     */
    tecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTwoFingerTapFrom:)];
    tecognizer.delegate = self;
    tecognizer.numberOfTouchesRequired = 2;
    tecognizer.numberOfTapsRequired = 1;
    [self.view addGestureRecognizer:tecognizer];
    [tecognizer release];

    /*
     Create a swipe gesture recognizer to recognize right swipes (the default).
     */
    recognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeFrom:)];
    recognizer.delegate = self;
    [self.view addGestureRecognizer:recognizer];
    [recognizer release];

    /*
     Create a swipe gesture recognizer to recognize left swipes.
     */
    UISwipeGestureRecognizer *srecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeFrom:)];
    srecognizer.delegate = self;
    srecognizer.direction = UISwipeGestureRecognizerDirectionLeft;

    [self.view addGestureRecognizer:srecognizer];
    [srecognizer release];

    /*
     Create a rotation gesture recognizer.
     We're only interested in receiving messages from this recognizer, and the view will take ownership of it, so we don't need to keep a reference to it.
     */
    recognizer = [[UIRotationGestureRecognizer alloc] initWithTarget:self action:@selector(handleRotationFrom:)];
    recognizer.delegate = self;
    [self.view addGestureRecognizer:recognizer];
    [recognizer release];


}


#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->actionButton_ release];
    [self->actionController_ release];
    [self->backButton_ release];

    [super dealloc];
}

@end
