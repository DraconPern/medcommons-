//
//  NSString+MCShared.m
//  MCShared
//
//  Created by J. G. Pusey on 4/7/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSString+MCShared.h"

@implementation NSString (MCShared)

- (NSString *) stringByURLDecoding
{
    return [self stringByReplacingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
}

- (NSString *) stringByURLEncoding
{
    return [self stringByAddingPercentEscapesUsingEncoding: NSUTF8StringEncoding];
}

@end
