//
//  MCSlideSorter.h
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ShooterStorageConnector,CustomViews;
@interface MCSlideSorter : UIViewController
{



    UIView *outerView;
    BOOL loaded;
    ShooterStorageConnector *patientStore;
    CGPoint startTouchPosition;

    int initialtouchcenter;
    UIImageView *initialtouchView;

    NSMutableArray *allviews; // so is this
    CustomViews *customViews;


}
// Private Methods
-(void)animateFirstTouchAtPoint:(CGPoint)touchPoint forView:(UIImageView *)theView;
-(void)animateView:(UIView *)theView toPosition:(CGPoint) thePosition;
-(void)dispatchFirstTouchAtPoint:(CGPoint)touchPoint forEvent:(UIEvent *)event;
-(void)dispatchTouchEvent:(UIView *)theView toPosition:(CGPoint)position;
-(void)dispatchTouchEndEvent:(UIView *)theView toPosition:(CGPoint)position;
- (void) screenUpdate: (NSTimer *) Timer;

-(MCSlideSorter *)   init;
@end
