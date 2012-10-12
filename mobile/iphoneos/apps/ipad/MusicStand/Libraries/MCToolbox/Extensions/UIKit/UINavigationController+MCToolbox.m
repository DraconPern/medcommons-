//
//  UINavigationController+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 4/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "UINavigationController+MCToolbox.h"

@implementation UINavigationController (MCToolbox)

@dynamic rootViewController;

- (UIViewController *) rootViewController
{
    return [self.viewControllers objectAtIndex: 0];
}

- (void) setRootViewController: (UIViewController *) vc
{
    [self setRootViewController: vc
                       animated: NO];
}

- (void) setRootViewController: (UIViewController *) vc
                      animated: (BOOL) animated
{
    if (self.rootViewController != vc)
        [self setViewControllers: [NSArray arrayWithObject: vc]
                        animated: animated];
}

@end
