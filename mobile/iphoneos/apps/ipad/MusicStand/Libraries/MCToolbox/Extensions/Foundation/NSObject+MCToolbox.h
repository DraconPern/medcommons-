//
//  NSObject+MCToolbox.h
//  MCToolbox
//
//  Created by J. G. Pusey on 6/17/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NSObject (MCToolbox)

@property (nonatomic, assign, readonly) UIApplication *application;

+ (UIApplication *) application;

@end
