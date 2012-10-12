//
//  NSDictionary+MCShared.h
//  MCShared
//
//  Created by J. G. Pusey on 4/8/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSDictionary (MCShared)

- (id) objectForKey: (id) key
     notFoundMarker: (id) obj;

@end
