//
//  NSObject+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/17/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSObject+MCToolbox.h"

@implementation NSObject (MCToolbox)

@dynamic application;

+ (UIApplication *) application
{
    return [UIApplication sharedApplication];
}

- (UIApplication *) application
{
    return [UIApplication sharedApplication];
}

@end
