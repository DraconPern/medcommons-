//
//  AppDelegate.h
//  MedPad
//
//  Created by bill donner on 3/4/10.
//  Copyright Apple Inc 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#if (!ENABLE_APD_LOGGING)
#define APD_LOG(format,...)
#else
#define APD_LOG(format,...)  CONSOLE_LOG(format ,## __VA_ARGS__)
#endif

@class RootViewController;
@class DetailViewController;
@class AppDelegate;

//

@interface AppDelegate : NSObject <UIApplicationDelegate>
{
    //JGP   DetailViewController  *detailViewController;
    //JGP   RootViewController    *rootViewController;
    UISplitViewController *splitViewController;
    UIWindow              *window;
}

//JGP   @property (nonatomic, retain) IBOutlet DetailViewController  *detailViewController;
//JGP   @property (nonatomic, retain) IBOutlet RootViewController    *rootViewController;
@property (nonatomic, retain) IBOutlet UISplitViewController *splitViewController;
@property (nonatomic, retain) IBOutlet UIWindow              *window;

#pragma mark -
#pragma mark JGP Hacks

- (UIViewController *) currentDetailViewController;

- (DetailViewController *) originalDetailViewController;

- (void) replaceDetailViewController: (UIViewController *) vc
         splitViewControllerDelegate: (id <UISplitViewControllerDelegate>) svcd;

- (void) restoreDetailViewController;

#pragma mark -

@end




