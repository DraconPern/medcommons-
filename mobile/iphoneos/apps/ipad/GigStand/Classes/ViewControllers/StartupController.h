//
//  StartupController.h
//  MusicStand
//
//  Created by bill donner on 10/15/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>



@interface StartupController : UIViewController {

	UIView *tv;
	UIWindow *window;
	UIAlertView *zipalert;
	UIViewController *fsc;	
	UINavigationController *fscn;
}

-(id) initWithWindow: (UIWindow *) win ;
@end
