//
//  RadioButtonChooserControl.h
//  GigStand
//
//  Created by bill donner on 1/2/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
@class InstanceInfo;

// lays out an array of names and actions as a segmented control, but if its too large for the device
// it puts up a "+N" button as the last choice and moves to an actionsheet

@interface RadioButtonChooserControl : UIView <UIActionSheetDelegate>
{
	UISegmentedControl *segmentedControl; // used for elements 1-max
	
	UIActionSheet *actionSheet; // all elements enumberated here
	
	SEL completionAction;
	
	NSMutableArray *myvariants; // holds all the archive choices as a list of names
	
	UIViewController *viewController; // who started this, so we can signal back thru completion action
	
	NSUInteger maxbuttons ; // when we hit maxbuttons we move to actionsheet
}


- (id) initWithTitle: (NSString *) title andInstance:(InstanceInfo *)iincoming
		   andAction: (SEL) action andController: (UIViewController *) controller;


@end
