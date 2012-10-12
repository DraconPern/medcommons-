//
//  Member.h
//  MCProvider
//
//  Created by Bill Donner on 12/27/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class MemberStore;

@interface Member : NSObject
{
@private

    NSDateFormatter *dateFormatter_;
    NSString        *dateTime_;
    NSDictionary    *info_;
    NSDictionary    *noteInfo_;
    NSArray         *notes_;
    MemberStore     *store_;
}

@property (nonatomic, copy,   readonly) NSString    *comment;
@property (nonatomic, copy,   readonly) NSString    *custom0;
@property (nonatomic, copy,   readonly) NSString    *custom1;
@property (nonatomic, copy,   readonly) NSString    *dateOfBirth;
@property (nonatomic, copy,   readonly) NSString    *dateTime;
@property (nonatomic, copy,   readonly) NSString    *familyName;
@property (nonatomic, copy,   readonly) NSString    *gender;
@property (nonatomic, copy,   readonly) NSString    *givenName;
@property (nonatomic, copy,   readonly) NSString    *identifier;
@property (nonatomic, assign, readonly) BOOL         isVisible;
@property (nonatomic, copy,   readonly) NSString    *name;
@property (nonatomic, retain, readonly) NSArray     *notes;
@property (nonatomic, retain, readonly) NSURL       *photoURL;
@property (nonatomic, copy,   readonly) NSString    *purpose;
@property (nonatomic, copy,   readonly) NSString    *status;
@property (nonatomic, retain, readonly) MemberStore *store;

+ (id) memberWithIdentifier: (NSString *) identifier
                  givenName: (NSString *) givenName
                 familyName: (NSString *) familyName
                   dateTime: (NSString *) dateTime;

+ (id) memberWithInfo: (NSDictionary *) info;

- (void) dump;

- (id) initWithIdentifier: (NSString *) identifier
                givenName: (NSString *) givenName
               familyName: (NSString *) familyName
                 dateTime: (NSString *) dateTime;

- (id) initWithInfo: (NSDictionary *) info;

- (void) updateNoteInfo: (NSDictionary *) noteInfo;

@end
