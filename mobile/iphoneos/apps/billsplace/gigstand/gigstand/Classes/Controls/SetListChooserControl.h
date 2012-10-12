//
//  SetListChooserControl.h
//  GigStand
//
//  Created by bill donner on 1/31/11.
//  Copyright 2011 gigstand.net. All rights reserved.
//

#import <UIKit/UIKit.h>
@interface SetListChooserControl: NSObject <UIActionSheetDelegate>
{
@private
    
    NSMutableArray *names;
    NSString				*tune;
    UIActionSheet          *mysheet;
    
    
    SEL completionAction;
	
    
    UIViewController *viewController; // who started this, so we can signal back thru completion action
    
}

- (id) initWithTune:(NSString *)tune
		  andAction: (SEL) action andController: (UIViewController *) controller;
-(void) show;
@end
