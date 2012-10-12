//
//  UINavigationController+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 4/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UINavigationController (MCToolbox)

@property (nonatomic, retain, readwrite) UIViewController *rootViewController;

- (void) setRootViewController: (UIViewController *) vc
                      animated: (BOOL) animated;

@end
