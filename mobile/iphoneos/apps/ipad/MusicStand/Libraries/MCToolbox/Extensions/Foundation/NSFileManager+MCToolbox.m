//
//  NSFileManager+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 5/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSFileManager+MCToolbox.h"

@implementation NSFileManager (MCToolbox)

+ (NSDate *) fileModificationDateOfItemAtPath: (NSString *) path
{
    NSFileManager *fm = [NSFileManager defaultManager];

    if ([fm fileExistsAtPath: path])
    {
        NSDictionary *attrs = [fm attributesOfItemAtPath: path
                                                   error: NULL];

        if (attrs)
            return [attrs fileModificationDate];
    }

    return nil;
}

@end
