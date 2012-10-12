//
//  Photo.h
//  MCProvider
//
//  Created by J. G. Pusey on 8/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Photo : NSObject
{
@private

    NSDictionary *attributes_;
    NSString     *path_;
}

@property (nonatomic, copy, readonly) NSDictionary *attributes;
@property (nonatomic, copy, readonly) NSString     *path;

+ (id) photoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes;

+ (id) photoWithPath: (NSString *) path
          attributes: (NSDictionary *) attributes
         addLocation: (BOOL) addLocation;

+ (id) photoWithPropertyList: (NSDictionary *) plist;

- (id) initWithPath: (NSString *) path
         attributes: (NSDictionary *) attributes;

- (id) initWithPath: (NSString *) path
         attributes: (NSDictionary *) attributes
        addLocation: (BOOL) addLocation;

- (id) initWithPropertyList: (NSDictionary *) plist;

- (NSDictionary *) propertyList;

@end
