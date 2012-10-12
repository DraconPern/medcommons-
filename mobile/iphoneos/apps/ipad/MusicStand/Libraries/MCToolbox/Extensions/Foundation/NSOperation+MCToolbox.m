//
//  NSOperation+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/21/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSOperation+MCToolbox.h"

@implementation NSOperation (MCToolbox)

- (BOOL) perform
{
    if ([self isReady] &&
        ![self isCancelled] &&
        ![self isExecuting] &&
        ![self isFinished])
    {
        if (![self isConcurrent])
            [NSThread detachNewThreadSelector: @selector (start)
                                     toTarget: self
                                   withObject: nil];
        else
            [self start];

        return YES;
    }

    return NO;
}

@end
