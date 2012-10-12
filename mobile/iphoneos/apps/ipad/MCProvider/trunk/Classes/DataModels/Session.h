//
//  Session.h
//  MCProvider
//
//  Created by J. G. Pusey on 7/12/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Group;
@class Member;

@interface Session : NSObject
{
@private

    NSDictionary *info_;
    Group        *groupInFocus_;
    NSArray      *groups_;
    Member       *memberInFocus_;
    NSString     *password_;
    NSDictionary *result_;
    NSString     *userName_;
}

@property (nonatomic, copy,   readonly)  NSString *appliance;
@property (nonatomic, copy,   readonly)  NSString *authToken;
@property (nonatomic, retain, readwrite) Group    *groupInFocus;
@property (nonatomic, retain, readonly)  NSArray  *groups;
@property (nonatomic, copy,   readonly)  NSString *identifier;
@property (nonatomic, copy,   readonly)  NSString *loginDateTime;
@property (nonatomic, retain, readwrite) Member   *memberInFocus;
@property (nonatomic, copy,   readonly)  NSString *password;
@property (nonatomic, copy,   readonly)  NSString *userFamilyName;
@property (nonatomic, copy,   readonly)  NSString *userGivenName;
@property (nonatomic, copy,   readonly)  NSString *userID;
@property (nonatomic, copy,   readonly)  NSString *userName;
@property (nonatomic, retain, readonly)  NSURL    *userPhotoURL;

+ (id) sessionWithInfo: (NSDictionary *) info
              password: (NSString *) password;

- (Group *) groupWithIdentifier: (NSString *) ident;

- (id) initWithInfo: (NSDictionary *) info
           password: (NSString *) password;

- (void) updateInfo: (NSDictionary *) info;

@end
