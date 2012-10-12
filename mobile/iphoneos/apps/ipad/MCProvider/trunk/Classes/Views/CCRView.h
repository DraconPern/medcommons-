//
//  CCRView.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@class CCRHeaderView;
@class CCRMainView;
@class CCRScrubberView;
@class CCRThumbListView;
@class CCRToolListView;

@interface CCRView : MCView
{
@private

    CCRHeaderView    *headerView_;
    CCRMainView      *mainView_;
    CCRScrubberView  *scrubberView_;
    CCRThumbListView *thumbListView_;
    CCRToolListView  *toolListView_;
    //
    // Rects:
    //
    CGRect            boundsLandscape_; // last view bounds used for landscape calculation
    CGRect            boundsPortrait_;  // last view bounds used for portrait calculation
    CGRect            hvFrame_;         // headerView frame
    CGRect            mvFrameLN_;       // mainView frame landscape normal
    CGRect            mvFrameLZ_;       // mainView frame landscape zoomed
    CGRect            mvFramePN_;       // mainView frame portrait normal
    CGRect            mvFramePZ_;       // mainView frame portrait zoomed
    CGRect            svFrameLH_;       // scrubberView frame landscape hidden
    CGRect            svFrameLV_;       // scrubberView frame landscape visible
    CGRect            svFramePH_;       // scrubberView frame portrait hidden
    CGRect            svFramePV_;       // scrubberView frame portrait visible
    CGRect            thvFrameLH_;      // thumbListView frame landscape hidden
    CGRect            thvFrameLV_;      // thumbListView frame landscape visible
    CGRect            thvFrameLZ_;      // thumbListView frame landscape zoomed (and visible)
    CGRect            thvFramePH_;      // thumbListView frame portrait hidden
    CGRect            thvFramePV_;      // thumbListView frame portrait visible
    CGRect            thvFramePZ_;      // thumbListView frame portrait zoomed (and visible)
    CGRect            tlvFrameLH_;      // toolListView frame landscape hidden
    CGRect            tlvFrameLV_;      // toolListView frame landscape visible
    CGRect            tlvFramePH_;      // toolListView frame portrait hidden
    CGRect            tlvFramePV_;      // toolListView frame portrait visible
    //
    // Flags:
    //
    struct
    {
        NSUInteger    mainViewZoomed : 1;
        NSUInteger    rotating : 1;
        NSUInteger    scrubberViewVisible : 1;
        NSUInteger    thumbListViewVisible : 1;
        NSUInteger    thumbListViewZoomed : 1;
        NSUInteger    toolListViewVisible : 1;
        NSUInteger    reserved : 26;
    } flags_;
}

@property (nonatomic, retain, readonly)  CCRHeaderView    *headerView;
@property (nonatomic, retain, readonly)  CCRMainView      *mainView;
@property (nonatomic, retain, readonly)  CCRScrubberView  *scrubberView;
@property (nonatomic, retain, readonly)  CCRThumbListView *thumbListView;
@property (nonatomic, retain, readonly)  CCRToolListView  *toolListView;

- (void) forceRecomputeSubviews;

- (void) hideScrubberViewAnimated: (BOOL) animated;

- (void) hideSubviewsBeforeRotation;

- (void) hideThumbListViewAnimated: (BOOL) animated;

- (void) hideToolListViewAnimated: (BOOL) animated;

- (void) maximizeMainViewAnimated: (BOOL) animated;

- (void) maximizeThumbListViewAnimated: (BOOL) animated;

- (void) normalizeMainViewAnimated: (BOOL) animated;

- (void) normalizeThumbListViewAnimated: (BOOL) animated;

- (void) rotateSubviews;

- (void) showScrubberViewAnimated: (BOOL) animated;

- (void) showSubviewsAfterRotation;

- (void) showThumbListViewAnimated: (BOOL) animated;

- (void) showToolListViewAnimated: (BOOL) animated;

@end
