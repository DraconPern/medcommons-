//
//  CCRViewController.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CCRActionController;
@class CCRShareActionController;

@interface CCRViewController : UIViewController <UIGestureRecognizerDelegate>
{
@private

    UIBarButtonItem          *backButton_;
    CCRActionController      *episodesActionController_;
    CCRShareActionController *menuActionController_;
    UIBarButtonItem          *segmentedButton_;
    UISegmentedControl       *segmentedControl_;
    NSURL                    *URL_;
//    float                   panDeltaX;
//    float                   panDeltaY;
//    float                   pinchStart;
    CFTimeInterval          lastScrubTime;
    CGFloat                 lastScrubValue;
    NSTimer                 *scrubTimer;
    NSString                *lastScrubJS;
    BOOL                    scrubTimerSet;
}

@property (nonatomic, assign, readonly) BOOL hidesMasterViewInLandscape;

- (id) initWithURL: (NSURL *) URL;

@end
