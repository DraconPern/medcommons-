//
//  Video.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "Photo.h"

@interface Video : Photo

+ (id) videoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes;

+ (id) videoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes
         addLocation: (BOOL) addLocation;

+ (id) videoWithPropertyList: (NSDictionary *) plist;

@end
