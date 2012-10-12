//
//  NSError+MCToolbox.m
//  MCToolbox
//
//  Created by J. G. Pusey on 6/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSError+MCToolbox.h"

@implementation NSError (MCToolbox)

+ (id) errorWithDomain: (NSString *) domain
                 code: (NSInteger) code
 localizedDescription: (NSString *) locDesc
{
    NSDictionary *userInfo = [NSDictionary dictionaryWithObject: locDesc
                                                         forKey: NSLocalizedDescriptionKey];

    return [NSError errorWithDomain: domain
                               code: code
                           userInfo: userInfo];
}

@end
