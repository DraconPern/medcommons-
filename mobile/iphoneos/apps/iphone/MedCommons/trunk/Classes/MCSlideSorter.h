//
//  MCSlideSorter.h
//  MedCommons
//
//  Created by bill donner on 1/22/10.
//  Copyright 2010 MedCommons,Inc. All rights reserved.
//

@class PatientStore,DashboardPatient,CustomViews;
@interface MCSlideSorter : UIViewController
{
	
	
	CustomViews *customViews;
	
	UIView *outerView;
	BOOL loaded;
	PatientStore *patientStore;
	CGPoint startTouchPosition; 
	DashboardPatient *wrapper;
	int initialtouchcenter;
	UIImageView *initialtouchView;
	
	NSMutableArray *allviews; // so is this
	
	
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
