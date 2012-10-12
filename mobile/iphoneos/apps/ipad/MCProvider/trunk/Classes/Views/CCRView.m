//
//  CCRView.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "CCRHeaderView.h"
#import "CCRMainView.h"
#import "CCRScrubberView.h"
#import "CCRThumbCell.h"
#import "CCRThumbListView.h"
#import "CCRToolCell.h"
#import "CCRToolListView.h"
#import "CCRView.h"

#define TO_BOOL(x) ((x) ? YES : NO)

#pragma mark -
#pragma mark Public Class CCRView
#pragma mark -

#pragma mark Internal Constants

#define ANIMATION_DURATION 0.3f

@interface CCRView ()

@property (nonatomic, assign, readwrite) CGRect boundsLandscape;
@property (nonatomic, assign, readwrite) CGRect boundsPortrait;
@property (nonatomic, assign, readonly)  CGRect headerViewFrame;
@property (nonatomic, assign, readonly)  BOOL   isLandscape;
@property (nonatomic, assign, readonly)  CGRect mainViewFrame;
@property (nonatomic, assign, readwrite) BOOL   mainViewZoomed;
@property (nonatomic, assign, readwrite) BOOL   rotating;
@property (nonatomic, assign, readonly)  CGRect scrubberViewFrame;
@property (nonatomic, assign, readwrite) BOOL   scrubberViewVisible;
@property (nonatomic, assign, readonly)  CGRect thumbListViewFrame;
@property (nonatomic, assign, readwrite) BOOL   thumbListViewVisible;
@property (nonatomic, assign, readwrite) BOOL   thumbListViewZoomed;
@property (nonatomic, assign, readonly)  CGRect toolListViewFrame;
@property (nonatomic, assign, readwrite) BOOL   toolListViewVisible;

- (void) computeSubviewFramesLandscape;

- (void) computeSubviewFramesPortrait;

- (void) loadSubviews;

- (void) recomputeSubviews;

@end

@implementation CCRView

@synthesize boundsLandscape      = boundsLandscape_;
@synthesize boundsPortrait       = boundsPortrait_;
@synthesize headerView           = headerView_;
@dynamic    headerViewFrame;
@dynamic    isLandscape;
@synthesize mainView             = mainView_;
@dynamic    mainViewFrame;
@dynamic    mainViewZoomed;
@dynamic    rotating;
@synthesize scrubberView         = scrubberView_;
@dynamic    scrubberViewFrame;
@dynamic    scrubberViewVisible;
@synthesize thumbListView        = thumbListView_;
@dynamic    thumbListViewFrame;
@dynamic    thumbListViewVisible;
@dynamic    thumbListViewZoomed;
@synthesize toolListView         = toolListView_;
@dynamic    toolListViewFrame;
@dynamic    toolListViewVisible;

#pragma mark Public Instance Methods

- (void) forceRecomputeSubviews
{
    self.boundsLandscape = CGRectZero;
    self.boundsPortrait = CGRectZero;

    [self recomputeSubviews];
}

- (void) hideScrubberViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.hideScrubberViewAnimated");

    if (self.scrubberViewVisible)
    {
        self.scrubberViewVisible = NO;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.scrubberView.frame = self.scrubberViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) hideSubviewsBeforeRotation
{
    //NSLog (@"*** CCRView.hideSubviewsBeforeRotation");

    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: ANIMATION_DURATION];

    self.rotating = YES;

    self.scrubberView.frame = self.scrubberViewFrame;
    self.thumbListView.frame = self.thumbListViewFrame;
    self.toolListView.frame = self.toolListViewFrame;

    [UIView commitAnimations];
}

- (void) hideThumbListViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.hideThumbListViewAnimated");

    if (self.thumbListViewVisible)
    {
        self.thumbListViewVisible = NO;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.thumbListView.frame = self.thumbListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) hideToolListViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.hideToolListViewAnimated");

    if (self.toolListViewVisible)
    {
        self.toolListViewVisible = NO;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.toolListView.frame = self.toolListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) maximizeMainViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.maximizeMainViewAnimated");

    if (!self.mainViewZoomed)
    {
        self.mainViewZoomed = YES;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.mainView.frame = self.mainViewFrame;
        self.scrubberView.frame = self.scrubberViewFrame;
        self.thumbListView.frame = self.thumbListViewFrame;
        self.toolListView.frame = self.toolListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) maximizeThumbListViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.maximizeThumbListViewAnimated");

    if (!self.thumbListViewZoomed)
    {
        self.thumbListViewZoomed = YES;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.thumbListView.alwaysScrollVertical = YES;
        self.thumbListView.layoutDirection = AQGridViewLayoutDirectionHorizontal;
        self.thumbListView.frame = self.thumbListViewFrame;
        self.scrubberView.frame = self.scrubberViewFrame;
        self.toolListView.frame = self.toolListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];

        [self.thumbListView reloadData];
    }
}

- (void) normalizeMainViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.normalizeMainViewAnimated");

    if (self.mainViewZoomed)
    {
        self.mainViewZoomed = NO;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.mainView.frame = self.mainViewFrame;
        self.scrubberView.frame = self.scrubberViewFrame;
        self.thumbListView.frame = self.thumbListViewFrame;
        self.toolListView.frame = self.toolListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) normalizeThumbListViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.normalizeThumbListViewAnimated");

    if (self.thumbListViewZoomed)
    {
        self.thumbListViewZoomed = NO;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        //
        // Layout direction may need to be adjusted:
        //
        self.thumbListView.alwaysScrollVertical = NO;
        self.thumbListView.layoutDirection = (self.isLandscape ?
                                              AQGridViewLayoutDirectionVertical :
                                              AQGridViewLayoutDirectionHorizontal);

        self.scrubberView.frame = self.scrubberViewFrame;
        self.thumbListView.frame = self.thumbListViewFrame;
        self.toolListView.frame = self.toolListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];

        [self.thumbListView reloadData];
    }
}

- (void) rotateSubviews
{
    //NSLog (@"*** CCRView.rotateSubviews");

    //
    // Set some subviews to hidden for avoid artifacts on rotation; restore
    // in showSubviewsAfterRotation:
    //
    self.scrubberView.hidden = YES;
    self.toolListView.hidden = YES;

    if (self.thumbListViewZoomed)
    {
        self.thumbListView.alwaysScrollVertical = YES;
        self.thumbListView.layoutDirection = AQGridViewLayoutDirectionHorizontal;
    }
    else
    {
        self.thumbListView.alwaysScrollVertical = NO;
        self.thumbListView.hidden = YES;
        self.thumbListView.layoutDirection = (self.isLandscape ?
                                              AQGridViewLayoutDirectionVertical :
                                              AQGridViewLayoutDirectionHorizontal);
    }

    self.toolListView.layoutDirection = (self.isLandscape ?
                                         AQGridViewLayoutDirectionVertical :
                                         AQGridViewLayoutDirectionHorizontal);

    self.headerView.frame = self.headerViewFrame;
    self.mainView.frame = self.mainViewFrame;
    self.scrubberView.frame = self.scrubberViewFrame;
    self.thumbListView.frame = self.thumbListViewFrame;
    self.toolListView.frame = self.toolListViewFrame;

    [self.thumbListView reloadData];
    [self.toolListView reloadData];
}

- (void) showScrubberViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.showScrubberViewAnimated");

    if (!self.scrubberViewVisible)
    {
        self.scrubberViewVisible = YES;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.scrubberView.frame = self.scrubberViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) showSubviewsAfterRotation
{
    //NSLog (@"*** CCRView.showSubviewsAfterRotation");

    //
    // Set some subviews to hidden for avoid artifacts on rotation; restore
    // in showSubviewsAfterRotation:
    //
    self.scrubberView.hidden = NO;
    self.toolListView.hidden = NO;

    if (!self.thumbListViewZoomed)
        self.thumbListView.hidden = NO;

    [UIView beginAnimations: nil
                    context: NULL];
    [UIView setAnimationDuration: ANIMATION_DURATION];

    self.rotating = NO;

    self.scrubberView.frame = self.scrubberViewFrame;
    self.thumbListView.frame = self.thumbListViewFrame;
    self.toolListView.frame = self.toolListViewFrame;

    [UIView commitAnimations];
}

- (void) showThumbListViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.showThumbListViewAnimated");

    if (!self.thumbListViewVisible)
    {
        self.thumbListViewVisible = YES;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.thumbListView.frame = self.thumbListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

- (void) showToolListViewAnimated: (BOOL) animated
{
    //NSLog (@"*** CCRView.showToolListViewAnimated");

    if (!self.toolListViewVisible)
    {
        self.toolListViewVisible = YES;

        //
        // Start of (possibly) animated actions:
        //
        if (animated)
        {
            [UIView beginAnimations: nil
                            context: NULL];
            [UIView setAnimationDuration: ANIMATION_DURATION];
        }

        self.toolListView.frame = self.toolListViewFrame;

        //
        // End of (possibly) animated actions:
        //
        if (animated)
            [UIView commitAnimations];
    }
}

#pragma mark Private Instance Methods

- (void) computeSubviewFramesLandscape
{
    //NSLog (@"*** CCRView.computeSubviewFramesLandscape");

    CGSize tlvSize = [self.toolListView sizeThatFitsVertical: CGSizeZero];
    CGSize hvSize = CGSizeZero; //[self.headerView sizeThatFits: CGSizeZero];
    CGSize bvSize = self.boundsLandscape.size;
    CGSize mvSizeMax;
    CGRect tmpFrame;
    CGSize thvSize;
    CGSize mvSize;
    CGSize svSize;

    thvSize.width = [self.thumbListView sizeThatFitsVertical: CGSizeZero].width;
    thvSize.height = (bvSize.height - hvSize.height);

    svSize.height = [self.scrubberView sizeThatFits: CGSizeZero].height;

    mvSizeMax.width = (bvSize.width - thvSize.width - tlvSize.width);
    mvSizeMax.height = (bvSize.height - svSize.height);

    mvSize.width = MIN (mvSizeMax.width,
                        mvSizeMax.height);
    mvSize.height = mvSize.width;

    svSize.width = mvSize.width;

    //
    // Header view:
    //
    tmpFrame.origin = CGPointZero;
    tmpFrame.size = hvSize;

    self->hvFrame_ = tmpFrame;

    //
    // Thumb list view:
    //
    tmpFrame.origin.x = CGRectGetMinX (self->hvFrame_);
    tmpFrame.origin.y = CGRectGetMaxY (self->hvFrame_);
    tmpFrame.size = thvSize;

    self->thvFrameLV_ = tmpFrame;

    tmpFrame.origin.x = -thvSize.width;

    self->thvFrameLH_ = tmpFrame;
    self->thvFrameLZ_ = self.boundsLandscape;

    //
    // Tool list view:
    //
    tmpFrame.origin.x = bvSize.width - tlvSize.width;
    tmpFrame.origin.y = floorf ((bvSize.height - tlvSize.height) / 2.0f);
    tmpFrame.size = tlvSize;

    self->tlvFrameLV_ = tmpFrame;

    tmpFrame.origin.x = bvSize.width;

    self->tlvFrameLH_ = tmpFrame;

    //
    // Scrubber view:
    //
    tmpFrame.origin.x = floorf ((CGRectGetMaxX (self->thvFrameLV_) +
                                 CGRectGetMinX (self->tlvFrameLV_) -
                                 svSize.width) / 2.0f);
    tmpFrame.origin.y = bvSize.height - svSize.height;
    tmpFrame.size = svSize;

    self->svFrameLV_ = tmpFrame;

    tmpFrame.origin.y = bvSize.height;

    self->svFrameLH_ = tmpFrame;

    //
    // Main view:
    //
    tmpFrame.origin.x = floorf ((CGRectGetMaxX (self->thvFrameLV_) +
                                 CGRectGetMinX (self->tlvFrameLV_) -
                                 mvSize.width) / 2.0f);
    tmpFrame.origin.y = floorf ((CGRectGetMinY (self->svFrameLV_) -
                                 mvSize.height) / 2.0f);
    tmpFrame.size = mvSize;

    self->mvFrameLN_ = tmpFrame;

    mvSizeMax.width = (bvSize.width - thvSize.width);
    mvSizeMax.height = bvSize.height;

    tmpFrame.size.width = MIN (mvSizeMax.width,
                               mvSizeMax.height);
    tmpFrame.size.height = tmpFrame.size.width;
    tmpFrame.origin.x = floorf ((CGRectGetMaxX (self->thvFrameLV_) +
                                 bvSize.width -
                                 tmpFrame.size.width) / 2.0f);
    tmpFrame.origin.y = floorf ((bvSize.height - tmpFrame.size.height) / 2.0f);

    self->mvFrameLZ_ = tmpFrame;

    //    NSLog (@"+++ boundsLandscape=%@"
    //           @"\n    hvFrame=%@"
    //           @"\n    mvFrameLN=%@"
    //           @"\n    mvFrameLZ=%@"
    //           @"\n    svFrameLH=%@"
    //           @"\n    svFrameLV=%@"
    //           @"\n    thvFrameLH=%@"
    //           @"\n    thvFrameLV=%@"
    //           @"\n    thvFrameLZ=%@"
    //           @"\n    tlvFrameLH=%@"
    //           @"\n    tlvFrameLV=%@",
    //           NSStringFromCGRect (self.boundsLandscape),
    //           NSStringFromCGRect (self.hvFrame),
    //           NSStringFromCGRect (self.mvFrameLN),
    //           NSStringFromCGRect (self.mvFrameLZ),
    //           NSStringFromCGRect (self.svFrameLH),
    //           NSStringFromCGRect (self.svFrameLV),
    //           NSStringFromCGRect (self.thvFrameLH),
    //           NSStringFromCGRect (self.thvFrameLV),
    //           NSStringFromCGRect (self.thvFrameLZ),
    //           NSStringFromCGRect (self.tlvFrameLH),
    //           NSStringFromCGRect (self.tlvFrameLV));
}

- (void) computeSubviewFramesPortrait
{
    //NSLog (@"*** CCRView.computeSubviewFramesPortrait");

    CGSize tlvSize = [self.toolListView sizeThatFitsHorizontal: CGSizeZero];
    CGSize hvSize = CGSizeZero; //[self.headerView sizeThatFits: CGSizeZero];
    CGSize bvSize = self.boundsPortrait.size;
    CGSize mvSizeMax;
    CGRect tmpFrame;
    CGSize thvSize;
    CGSize mvSize;
    CGSize svSize;

    thvSize.width = (bvSize.width - hvSize.width);
    thvSize.height = [self.thumbListView sizeThatFitsHorizontal: CGSizeZero].height;

    svSize.height = [self.scrubberView sizeThatFits: CGSizeZero].height;

    mvSizeMax.width = bvSize.width;
    mvSizeMax.height = (bvSize.height -
                        thvSize.height -
                        svSize.height -
                        tlvSize.height);

    mvSize.width = MIN (mvSizeMax.width,
                        mvSizeMax.height);
    mvSize.height = mvSize.width;

    svSize.width = mvSize.width;

    //
    // Header view:
    //
    tmpFrame.origin = CGPointZero;
    tmpFrame.size = hvSize;

    self->hvFrame_ = tmpFrame;

    //
    // Thumb list view:
    //
    tmpFrame.origin.x = CGRectGetMaxX (self->hvFrame_);
    tmpFrame.origin.y = CGRectGetMinY (self->hvFrame_);
    tmpFrame.size = thvSize;

    self->thvFramePV_ = tmpFrame;

    tmpFrame.origin.y = -thvSize.height;

    self->thvFramePH_ = tmpFrame;
    self->thvFramePZ_ = self.boundsPortrait;

    //
    // Tool list view:
    //
    tmpFrame.origin.x = floorf ((bvSize.width - tlvSize.width) / 2.0f);
    tmpFrame.origin.y = bvSize.height - tlvSize.height;
    tmpFrame.size = tlvSize;

    self->tlvFramePV_ = tmpFrame;

    tmpFrame.origin.y = bvSize.height;

    self->tlvFramePH_ = tmpFrame;

    //
    // Scrubber view:
    //
    tmpFrame.origin.x = floorf ((bvSize.width - svSize.width) / 2.0f);
    tmpFrame.origin.y = bvSize.height - tlvSize.height - svSize.height;
    tmpFrame.size = svSize;

    self->svFramePV_ = tmpFrame;

    tmpFrame.origin.y = bvSize.height;

    self->svFramePH_ = tmpFrame;

    //
    // Main view:
    //
    tmpFrame.origin.x = floorf ((bvSize.width - mvSize.width) / 2.0f);
    tmpFrame.origin.y = floorf ((CGRectGetMaxY (self->thvFramePV_) +
                                 CGRectGetMinY (self->svFramePV_) -
                                 mvSize.height) / 2.0f);
    tmpFrame.size = mvSize;

    self->mvFramePN_ = tmpFrame;

    mvSizeMax.width = bvSize.width;
    mvSizeMax.height = (bvSize.height - thvSize.height);

    tmpFrame.size.width = MIN (mvSizeMax.width,
                               mvSizeMax.height);
    tmpFrame.size.height = tmpFrame.size.width;
    tmpFrame.origin.x = floorf ((bvSize.width - tmpFrame.size.width) / 2.0f);
    tmpFrame.origin.y = floorf ((CGRectGetMaxY (self->thvFramePV_) +
                                 bvSize.height -
                                 tmpFrame.size.height) / 2.0f);

    self->mvFramePZ_ = tmpFrame;

    //    NSLog (@"+++ boundsPortrait=%@"
    //           @"\n    hvFrame=%@"
    //           @"\n    mvFramePN=%@"
    //           @"\n    mvFramePZ=%@"
    //           @"\n    svFramePH=%@"
    //           @"\n    svFramePV=%@"
    //           @"\n    thvFramePH=%@"
    //           @"\n    thvFramePV=%@"
    //           @"\n    thvFramePZ=%@"
    //           @"\n    tlvFramePH=%@"
    //           @"\n    tlvFramePV=%@",
    //           NSStringFromCGRect (self.boundsPortrait),
    //           NSStringFromCGRect (self.hvFrame),
    //           NSStringFromCGRect (self.mvFramePN),
    //           NSStringFromCGRect (self.mvFramePZ),
    //           NSStringFromCGRect (self.svFramePH),
    //           NSStringFromCGRect (self.svFramePV),
    //           NSStringFromCGRect (self.thvFramePH),
    //           NSStringFromCGRect (self.thvFramePV),
    //           NSStringFromCGRect (self.thvFramePZ),
    //           NSStringFromCGRect (self.tlvFramePH),
    //           NSStringFromCGRect (self.tlvFramePV));
}

- (CGRect) headerViewFrame
{
    return self->hvFrame_;
}

- (BOOL) isLandscape
{
    return (self.bounds.size.width > self.bounds.size.height);
}

- (void) loadSubviews
{
    self->headerView_ = [[CCRHeaderView alloc] initWithFrame: CGRectZero];
    self->mainView_ = [[CCRMainView alloc] initWithFrame: CGRectZero];
    self->scrubberView_ = [[CCRScrubberView alloc] initWithFrame: CGRectZero];
    self->thumbListView_ = [[CCRThumbListView alloc] initWithFrame: CGRectZero];
    self->toolListView_ = [[CCRToolListView alloc] initWithFrame: CGRectZero];

    self.thumbListView.alwaysScrollVertical = NO;
    self.thumbListView.layoutDirection = (self.isLandscape ?
                                          AQGridViewLayoutDirectionVertical :
                                          AQGridViewLayoutDirectionHorizontal);

    self.toolListView.layoutDirection = (self.isLandscape ?
                                         AQGridViewLayoutDirectionVertical :
                                         AQGridViewLayoutDirectionHorizontal);

    //
    // Order of subviews is VERY important to make animation look nice:
    //
    //    [self addSubview: self.headerView];
    [self addSubview: self.mainView];
    [self addSubview: self.thumbListView];
    [self addSubview: self.scrubberView];
    [self addSubview: self.toolListView];

    [self recomputeSubviews];
}

- (CGRect) mainViewFrame
{
    return (self.mainViewZoomed ?
            (self.isLandscape ?
             self->mvFrameLZ_ :
             self->mvFramePZ_) :
            (self.isLandscape ?
             self->mvFrameLN_ :
             self->mvFramePN_));
}

- (BOOL) mainViewZoomed
{
    return TO_BOOL (self->flags_.mainViewZoomed);
}

- (void) recomputeSubviews
{
    //NSLog (@"*** CCRView.recomputeSubviews");

    if (self.isLandscape)
    {
        if (!CGRectEqualToRect (self.boundsLandscape, self.bounds))
        {
            self.boundsLandscape = self.bounds;

            [self computeSubviewFramesLandscape];
        }
    }
    else if (!CGRectEqualToRect (self.boundsPortrait, self.bounds))
    {
        self.boundsPortrait = self.bounds;

        [self computeSubviewFramesPortrait];
    }

    self.headerView.frame = self.headerViewFrame;
    self.mainView.frame = self.mainViewFrame;
    self.scrubberView.frame = self.scrubberViewFrame;
    self.thumbListView.frame = self.thumbListViewFrame;
    self.toolListView.frame = self.toolListViewFrame;
}

- (BOOL) rotating
{
    return TO_BOOL (self->flags_.rotating);
}

- (CGRect) scrubberViewFrame
{
    return ((self.scrubberViewVisible &&
             !self.mainViewZoomed &&
             !self.rotating &&
             !self.thumbListViewZoomed) ?
            (self.isLandscape ?
             self->svFrameLV_ :
             self->svFramePV_) :
            (self.isLandscape ?
             self->svFrameLH_ :
             self->svFramePH_));
}

- (BOOL) scrubberViewVisible
{
    return TO_BOOL (self->flags_.scrubberViewVisible);
}

- (void) setMainViewZoomed: (BOOL) mainViewZoomed
{
    self->flags_.mainViewZoomed = TO_BOOL (mainViewZoomed);
}

- (void) setRotating: (BOOL) rotating
{
    self->flags_.rotating = TO_BOOL (rotating);
}

- (void) setScrubberViewVisible: (BOOL) scrubberViewVisible
{
    self->flags_.scrubberViewVisible = TO_BOOL (scrubberViewVisible);
}

- (void) setThumbListViewVisible: (BOOL) thumbListViewVisible
{
    self->flags_.thumbListViewVisible = TO_BOOL (thumbListViewVisible);
}

- (void) setThumbListViewZoomed: (BOOL) thumbListViewZoomed
{
    self->flags_.thumbListViewZoomed = TO_BOOL (thumbListViewZoomed);
}

- (void) setToolListViewVisible: (BOOL) toolListViewVisible
{
    self->flags_.toolListViewVisible = TO_BOOL (toolListViewVisible);
}

- (CGRect) thumbListViewFrame
{
    return (self.thumbListViewZoomed ?
            (self.isLandscape ?
             self->thvFrameLZ_ :
             self->thvFramePZ_) :
            ((self.thumbListViewVisible &&
              !self.mainViewZoomed &&
              !self.rotating) ?
             (self.isLandscape ?
              self->thvFrameLV_ :
              self->thvFramePV_) :
             (self.isLandscape ?
              self->thvFrameLH_ :
              self->thvFramePH_)));
}

- (BOOL) thumbListViewVisible
{
    return TO_BOOL (self->flags_.thumbListViewVisible);
}

- (BOOL) thumbListViewZoomed
{
    return TO_BOOL (self->flags_.thumbListViewZoomed);
}

- (CGRect) toolListViewFrame
{
    return ((self.toolListViewVisible &&
             !self.mainViewZoomed &&
             !self.rotating &&
             !self.thumbListViewZoomed) ?
            (self.isLandscape ?
             self->tlvFrameLV_ :
             self->tlvFramePV_) :
            (self.isLandscape ?
             self->tlvFrameLH_ :
             self->tlvFramePH_));
}

- (BOOL) toolListViewVisible
{
    return TO_BOOL (self->flags_.toolListViewVisible);
}

#pragma mark Overridden UIView Methods

- (id) initWithFrame: (CGRect) frame
{
    self = [super initWithFrame: frame];

    if (self)
    {
        self.backgroundColor = [UIColor yellowColor];

        [self loadSubviews];
    }

    return self;
}

- (void) setBounds: (CGRect) bounds
{
    //NSLog (@"*** CCRView.setBounds: %@", NSStringFromCGRect (bounds));

    super.bounds = bounds;

    [self recomputeSubviews];
}

- (void) setCenter: (CGPoint) center
{
    //NSLog (@"*** CCRView.setCenter: %@", NSStringFromCGPoint (center));

    super.center = center;

    [self recomputeSubviews];
}

- (void) setFrame: (CGRect) frame
{
    //NSLog (@"*** CCRView.setFrame: %@", NSStringFromCGRect (frame));

    super.frame = frame;

    [self recomputeSubviews];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    self.mainView.delegate = nil;    // Apple docs say this is required

    [self->headerView_ release];
    [self->mainView_ release];
    [self->scrubberView_ release];
    [self->thumbListView_ release];
    [self->toolListView_ release];

    [super dealloc];
}

@end
