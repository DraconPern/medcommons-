//
//  Group.h
//  MCProvider
//
//  Created by Bill Donner on 4/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//
#import <Foundation/Foundation.h>

@interface Group : NSObject
{
@private

    NSDictionary *info_;
    NSArray      *members_;
    NSArray      *membersFilteredA_;
    NSArray      *membersFilteredB_;
    NSDictionary *result_;
}

@property (nonatomic, copy,   readonly) NSString *identifier;
@property (nonatomic, retain, readonly) NSURL    *logoURL;
@property (nonatomic, retain, readonly) NSArray  *members;
@property (nonatomic, retain, readonly) NSArray  *membersFilteredA;
@property (nonatomic, retain, readonly) NSArray  *membersFilteredB;
@property (nonatomic, copy,   readonly) NSString *name;

+ (id) groupWithInfo: (NSDictionary *) info;

- (void) dump;

- (id) initWithInfo: (NSDictionary *) info;

- (void) updateInfo: (NSDictionary *) info;

@end
