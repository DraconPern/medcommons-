//
//  flipAppDelegate.h
//  flip
//
//  Created by bill donner on 5/16/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

@class MainViewController;

@interface flipAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    MainViewController *mainViewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) MainViewController *mainViewController;

@end

