//
//  CCRGestures.m
//  MCProvider
//
//  Created by J. G. Pusey on 10/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRGestures.h"

#pragma mark -
#pragma mark Public Class CCRGestures
#pragma mark -

@interface CCRGestures () <UIGestureRecognizerDelegate>

@property (nonatomic, retain, readonly)  UIPanGestureRecognizer   *panGesture;
@property (nonatomic, retain, readonly)  UIPinchGestureRecognizer *pinchGesture;
@property (nonatomic, retain, readonly)  UISwipeGestureRecognizer *swipe2DownGesture;
@property (nonatomic, retain, readonly)  UISwipeGestureRecognizer *swipe2LeftGesture;
@property (nonatomic, retain, readonly)  UISwipeGestureRecognizer *swipe2RightGesture;
@property (nonatomic, retain, readonly)  UISwipeGestureRecognizer *swipe2UpGesture;
@property (nonatomic, retain, readonly)  UITapGestureRecognizer   *tap1DoubleGesture;
@property (nonatomic, retain, readonly)  UITapGestureRecognizer   *tap1SingleGesture;
@property (nonatomic, retain, readonly)  UITapGestureRecognizer   *tap1TripleGesture;

- (void) handlePan: (UIPanGestureRecognizer *) pgr;

- (void) handlePinch: (UIPinchGestureRecognizer *) pgr;

- (void) handleSwipe: (UISwipeGestureRecognizer *) sgr;

- (void) handleTap: (UITapGestureRecognizer *) tgr;

- (void) setupGestureRecognizers;

@end

@implementation CCRGestures

@synthesize pan1DownAction     = pan1DownAction_;
@synthesize pan1LeftAction     = pan1LeftAction_;
@synthesize pan1RightAction    = pan1RightAction_;
@synthesize pan1UpAction       = pan1UpAction_;
@synthesize panGesture         = panGesture_;
@synthesize pinchGesture       = pinchGesture_;
@synthesize pinchInAction      = pinchInAction_;
@synthesize pinchOutAction     = pinchOutAction_;
@synthesize swipe2DownAction   = swipe2DownAction_;
@synthesize swipe2DownGesture  = swipe2DownGesture_;
@synthesize swipe2LeftAction   = swipe2LeftAction_;
@synthesize swipe2LeftGesture  = swipe2LeftGesture_;
@synthesize swipe2RightAction  = swipe2RightAction_;
@synthesize swipe2RightGesture = swipe2RightGesture_;
@synthesize swipe2UpAction     = swipe2UpAction_;
@synthesize swipe2UpGesture    = swipe2UpGesture_;
@synthesize tap1DoubleAction   = tap1DoubleAction_;
@synthesize tap1DoubleGesture  = tap1DoubleGesture_;
@synthesize tap1SingleAction   = tap1SingleAction_;
@synthesize tap1SingleGesture  = tap1SingleGesture_;
@synthesize tap1TripleAction   = tap1TripleAction_;
@synthesize tap1TripleGesture  = tap1TripleGesture_;
@synthesize target             = target_;
@synthesize view               = view_;

#pragma mark Public Instance Methods

- (void) setView: (UIView *) view
{
    if (self->view_ != view)
    {
        [self.view removeGestureRecognizer: self.tap1TripleGesture];
        [self.view removeGestureRecognizer: self.tap1SingleGesture];
        [self.view removeGestureRecognizer: self.tap1DoubleGesture];
        [self.view removeGestureRecognizer: self.swipe2UpGesture];
        [self.view removeGestureRecognizer: self.swipe2RightGesture];
        [self.view removeGestureRecognizer: self.swipe2LeftGesture];
        [self.view removeGestureRecognizer: self.swipe2DownGesture];
        [self.view removeGestureRecognizer: self.pinchGesture];
        [self.view removeGestureRecognizer: self.panGesture];

        [self->view_ release];

        self->view_ = [view retain];

        [self.view addGestureRecognizer: self.panGesture];
        [self.view addGestureRecognizer: self.pinchGesture];
        [self.view addGestureRecognizer: self.swipe2DownGesture];
        [self.view addGestureRecognizer: self.swipe2LeftGesture];
        [self.view addGestureRecognizer: self.swipe2RightGesture];
        [self.view addGestureRecognizer: self.swipe2UpGesture];
        [self.view addGestureRecognizer: self.tap1DoubleGesture];
        [self.view addGestureRecognizer: self.tap1SingleGesture];
        [self.view addGestureRecognizer: self.tap1TripleGesture];
    }
}

#pragma mark Private Instance Methods

- (void) handlePan: (UIPanGestureRecognizer *) pgr
{
    if (pgr.view != self.view)
        return;

    SEL action = NULL;

    if (pgr == self.panGesture)
    {
        switch (pgr.state)
        {
            case UIGestureRecognizerStateBegan :
                [pgr setTranslation: CGPointZero
                             inView: pgr.view];
                break;

            case UIGestureRecognizerStateEnded :
            {
                CGPoint translation = [pgr translationInView: pgr.view];

                if (fabsf (translation.x) < fabsf (translation.y))
                {
                    if (translation.y < 0.0f)
                    {
                        NSLog (@">>> pan1_up_gesture <<<");

                        action = self.pan1UpAction;
                    }
                    else
                    {
                        NSLog (@">>> pan1_down_gesture <<<");

                        action = self.pan1DownAction;
                    }
                }
                else
                {
                    if (translation.x < 0.0f)
                    {
                        NSLog (@">>> pan1_left_gesture <<<");

                        action = self.pan1LeftAction;
                    }
                    else
                    {
                        NSLog (@">>> pan1_right_gesture <<<");

                        action = self.pan1RightAction;
                    }
                }

                break;
            }

            default :
                break;
        }
    }

    if (action)
        [self.target performSelector: action];
}

- (void) handlePinch: (UIPinchGestureRecognizer *) pgr
{
    if (pgr.view != self.view)
        return;

    SEL action = NULL;

    if (pgr == self.pinchGesture)
    {
        switch (pgr.state)
        {
            case UIGestureRecognizerStateBegan :
                pgr.scale = 1.0f;
                break;

            case UIGestureRecognizerStateEnded :
                if (pgr.scale < 1.0f)
                {
                    NSLog (@">>> pinch_in_gesture <<<");

                    action = self.pinchInAction;
                }
                else if (pgr.scale > 1.0f)
                {
                    NSLog (@">>> pinch_out_gesture <<<");

                    action = self.pinchOutAction;
                }
                break;

            default :
                break;
        }
    }

    if (action)
        [self.target performSelector: action];
}

- (void) handleSwipe: (UISwipeGestureRecognizer *) sgr
{
    if (sgr.view != self.view)
        return;

    SEL action = NULL;

    if (sgr == self.swipe2LeftGesture)
    {
        NSLog (@">>> swipe2_left_gesture <<<");

        action = self.swipe2LeftAction;
    }
    else if (sgr == self.swipe2RightGesture)
    {
        NSLog (@">>> swipe2_right_gesture <<<");

        action = self.swipe2LeftAction;
    }
    else if (sgr == self.swipe2LeftGesture)
    {
        NSLog (@">>> swipe2_up_gesture <<<");

        action = self.swipe2UpAction;
    }
    else if (sgr == self.swipe2DownGesture)
    {
        NSLog (@">>> swipe2_down_gesture <<<");

        action = self.swipe2DownAction;
    }

    if (action)
        [self.target performSelector: action];
}

- (void) handleTap: (UITapGestureRecognizer *) tgr
{
    if (tgr.view != self.view)
        return;

    SEL action = NULL;

    if (tgr == self.tap1SingleGesture)
    {
        NSLog (@">>> tap1_single_gesture <<<");

        action = self.tap1SingleAction;
    }
    else if (tgr == self.tap1DoubleGesture)
    {
        NSLog (@">>> tap1_double_gesture <<<");

        action = self.tap1DoubleAction;
    }
    else if (tgr == self.tap1TripleGesture)
    {
        NSLog (@">>> tap1_triple_gesture <<<");

        action = self.tap1TripleAction;
    }

    if (action)
        [self.target performSelector: action];
}

- (void) setupGestureRecognizers
{
    //
    // Pan (one finger only):
    //
    self->panGesture_ = [[UIPanGestureRecognizer alloc] initWithTarget: self
                                                                action: @selector (handlePan:)];

    self.panGesture.delegate = self;
    self.panGesture.maximumNumberOfTouches = 1;
    self.panGesture.minimumNumberOfTouches = 1;

    //
    // Two-fingered down swipe:
    //
    self->swipe2DownGesture_ = [[UISwipeGestureRecognizer alloc] initWithTarget: self
                                                                         action: @selector (handleSwipe:)];

    self.swipe2DownGesture.delegate = self;
    self.swipe2DownGesture.direction = UISwipeGestureRecognizerDirectionDown;
    self.swipe2DownGesture.numberOfTouchesRequired = 2;

    //
    // Two-fingered left swipe:
    //
    self->swipe2LeftGesture_ = [[UISwipeGestureRecognizer alloc] initWithTarget: self
                                                                         action: @selector (handleSwipe:)];

    self.swipe2LeftGesture.delegate = self;
    self.swipe2LeftGesture.direction = UISwipeGestureRecognizerDirectionLeft;
    self.swipe2LeftGesture.numberOfTouchesRequired = 2;

    //
    // Two-fingered right swipe:
    //
    self->swipe2RightGesture_ = [[UISwipeGestureRecognizer alloc] initWithTarget: self
                                                                          action: @selector (handleSwipe:)];

    self.swipe2RightGesture.delegate = self;
    self.swipe2RightGesture.direction = UISwipeGestureRecognizerDirectionRight;
    self.swipe2RightGesture.numberOfTouchesRequired = 2;

    //
    // Two-fingered up swipe:
    //
    self->swipe2UpGesture_ = [[UISwipeGestureRecognizer alloc] initWithTarget: self
                                                                       action: @selector (handleSwipe:)];

    self.swipe2UpGesture.delegate = self;
    self.swipe2UpGesture.direction = UISwipeGestureRecognizerDirectionUp;
    self.swipe2UpGesture.numberOfTouchesRequired = 2;

    //
    // Pinch (all two-fingered swipes MUST fail):
    //
    self->pinchGesture_ = [[UIPinchGestureRecognizer alloc] initWithTarget: self
                                                                    action: @selector (handlePinch:)];

    self.pinchGesture.delegate = self;
    self.pinchGesture.scale = 1.0f;

    [self.pinchGesture requireGestureRecognizerToFail: self.swipe2DownGesture];
    [self.pinchGesture requireGestureRecognizerToFail: self.swipe2LeftGesture];
    [self.pinchGesture requireGestureRecognizerToFail: self.swipe2RightGesture];
    [self.pinchGesture requireGestureRecognizerToFail: self.swipe2UpGesture];

    //
    // One-fingered triple tap:
    //
    self->tap1TripleGesture_ = [[UITapGestureRecognizer alloc] initWithTarget: self
                                                                       action: @selector (handleTap:)];

    self.tap1TripleGesture.delegate = self;
    self.tap1TripleGesture.numberOfTapsRequired = 3;
    self.tap1TripleGesture.numberOfTouchesRequired = 1;

    //
    // One-fingered double tap (one-fingered triple tap MUST fail):
    //
    self->tap1DoubleGesture_ = [[UITapGestureRecognizer alloc] initWithTarget: self
                                                                       action: @selector (handleTap:)];

    self.tap1DoubleGesture.delegate = self;
    self.tap1DoubleGesture.numberOfTapsRequired = 2;
    self.tap1DoubleGesture.numberOfTouchesRequired = 1;

    [self.tap1DoubleGesture requireGestureRecognizerToFail: self.tap1TripleGesture];

    //
    // One-fingered single tap (one-fingered double and triple taps MUST fail):
    //
    self->tap1SingleGesture_ = [[UITapGestureRecognizer alloc] initWithTarget: self
                                                                       action: @selector (handleTap:)];

    self.tap1SingleGesture.delegate = self;
    self.tap1SingleGesture.numberOfTapsRequired = 1;
    self.tap1SingleGesture.numberOfTouchesRequired = 1;

    [self.tap1SingleGesture requireGestureRecognizerToFail: self.tap1DoubleGesture];
    [self.tap1SingleGesture requireGestureRecognizerToFail: self.tap1TripleGesture];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    self.view = nil;    // very important!!!

    [self->panGesture_ release];
    [self->pinchGesture_ release];
    [self->swipe2DownGesture_ release];
    [self->swipe2LeftGesture_ release];
    [self->swipe2RightGesture_ release];
    [self->swipe2UpGesture_ release];
    [self->tap1DoubleGesture_ release];
    [self->tap1SingleGesture_ release];
    [self->tap1TripleGesture_ release];

    [super dealloc];
}

- (id) init
{
    self = [super init];

    if (self)
        [self setupGestureRecognizers];

    return self;
}

#pragma mark UIGestureRecognizerDelegate Methods

- (BOOL) gestureRecognizer: (UIGestureRecognizer *) gr
shouldRecognizeSimultaneouslyWithGestureRecognizer: (UIGestureRecognizer *) gr2
{
//    NSLog (@"*** CCRGestures.gestureRecognizer: %@ shouldRecognizeSimultaneouslyWithGestureRecognizer: %@",
//           gr,
//           gr2);

//    //
//    // Both gesture recognizers must be on same (specified) view:
//    //
//    return ((gr.view == self.view) &&
//            (gr2.view == self.view));

    return YES;
}

@end
