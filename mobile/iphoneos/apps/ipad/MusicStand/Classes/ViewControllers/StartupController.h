//
//  StartupController.h
//  MusicStand
//
//  Created by bill donner on 10/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class FrontSearchController;

@interface StartupController : UIViewController {
	NSUInteger countup;
	UIView *tv;
	UIWindow *window;
	UIAlertView *zipalert;
	UITabBarController			*tabBarController;
	FrontSearchController *fsc;
	
	UINavigationController *fscn;
}

-(id) initWithWindow: (UIWindow *) win ;
@end
