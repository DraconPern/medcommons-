//
//  MCApplicationDelegate.h
//  MCToolbox
//
//  Created by J. G. Pusey on 4/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MCApplicationDelegate : NSObject <UIApplicationDelegate>
{
@private

    NSUInteger        networkActivityCount_;
    UIViewController *rootViewController_;
    UIWindow         *window_;
}

@property (nonatomic, retain, readonly) UIViewController *rootViewController;
@property (nonatomic, retain, readonly) UIWindow         *window;

- (void) didStartNetworkActivity;

- (void) didStopNetworkActivity;

- (void) setInitialRootViewController: (UIViewController *) vc;

@end
