//
//  NSURL+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 7/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSURL+MCToolbox.h"

@implementation NSURL (MCToolbox)

+ (id) URLWithFormat: (NSString *) format, ...
{
    NSString *tmpString;
    va_list   args;

    va_start (args, format);

    tmpString = [[NSString alloc] initWithFormat: format
                                       arguments: args];

    va_end (args);

    return [NSURL URLWithString: [tmpString autorelease]];
}

@end
