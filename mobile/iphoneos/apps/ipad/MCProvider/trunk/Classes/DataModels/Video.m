//
//  Video.m
//  MCProvider
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "Video.h"

#pragma mark -
#pragma mark Public Class Video
#pragma mark -

@implementation Video

#pragma mark Public Class Methods

+ (id) videoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes
{
    return [self videoWithPath: path
                    attributes: attributes
                   addLocation: YES];
}

+ (id) videoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes
         addLocation: (BOOL) addLocation
{
    return [[[self alloc] initWithPath: path
                            attributes: attributes
                           addLocation: addLocation]
            autorelease];
}

+ (id) videoWithPropertyList: (NSDictionary *) plist
{
    return [[[self alloc] initWithPropertyList: plist]
            autorelease];
}

@end
