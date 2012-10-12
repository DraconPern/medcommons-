//
//  NSDictionary+MCShared.m
//  MCShared
//
//  Created by J. G. Pusey on 4/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "NSDictionary+MCShared.h"

@implementation NSDictionary (MCShared)

- (id) objectForKey: (id) key
     notFoundMarker: (id) obj
{
    id value = [self objectForKey: key];
    
    return (value ? value : obj);
}

@end
