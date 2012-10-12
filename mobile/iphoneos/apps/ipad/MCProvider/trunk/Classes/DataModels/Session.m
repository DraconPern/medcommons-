//
//  Session.m
//  MCProvider
//
//  Created by J. G. Pusey on 7/12/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DictionaryAdditions.h"
#import "Group.h"
#import "Member.h"
#import "Session.h"
#import "SessionManager.h"

//
// Kluge to make setGroupInFocus work ...
//
@interface SessionManager ()

- (void) updateGroupInfo: (NSTimer *) timer;

@end

#pragma mark -
#pragma mark Public Class Session
#pragma mark -

#pragma mark Internal Constants

//
// Keys for info provided on init:
//
#define INIT_APPLIANCE_KEY          @"servername"
#define INIT_LOGIN_DATE_TIME_KEY    @"servertime"
#define INIT_RESULT_KEY             @"result"

//
// Keys for info provided on update:
//
#define UPDATE_AUTH_TOKEN_KEY       @"auth"
#define UPDATE_GROUPS_KEY           @"groups"
#define UPDATE_IDENTIFIER_KEY       @"accid"
#define UPDATE_USER_FAMILY_NAME_KEY @"ln"
#define UPDATE_USER_GIVEN_NAME_KEY  @"fn"
#define UPDATE_USER_ID_KEY          @"email"
#define UPDATE_USER_PHOTO_URL_KEY   @"photoUrl"

@interface Session ()

@property (nonatomic, retain, readwrite) NSArray      *groups;
@property (nonatomic, retain, readonly)  NSDictionary *info;
@property (nonatomic, retain, readwrite) NSDictionary *result;

- (NSArray *) sortedGroups: (NSArray *) unsortedGroups;

@end

@implementation Session

@dynamic    appliance;
@dynamic    authToken;
@synthesize groupInFocus   = groupInFocus_;
@synthesize groups         = groups_;
@dynamic    identifier;
@synthesize info           = info_;
@dynamic    loginDateTime;
@synthesize memberInFocus  = memberInFocus_;
@synthesize password       = password_;
@synthesize result         = result_;
@dynamic    userFamilyName;
@dynamic    userGivenName;
@dynamic    userID;
@dynamic    userName;
@dynamic    userPhotoURL;

#pragma mark Public Class Methods

+ (id) sessionWithInfo: (NSDictionary *) info
              password: (NSString *) password
{
    return [[[Session alloc] initWithInfo: info
                                 password: password]
            autorelease];
}

#pragma mark Public Instance Methods

- (NSString *) appliance
{
    return [self.info stringForKey: INIT_APPLIANCE_KEY];
}

- (NSString *) authToken
{
    return [self.result stringForKey: UPDATE_AUTH_TOKEN_KEY];
}

- (NSArray *) groups
{
    if (!self->groups_)
    {
        NSArray        *inGroups = [self.result arrayForKey: UPDATE_GROUPS_KEY];
        NSMutableArray *outGroups = [NSMutableArray arrayWithCapacity: [inGroups count]];

        for (NSDictionary *info in inGroups)
            [outGroups addObject: [Group groupWithInfo: info]];

        self->groups_ = [[self sortedGroups: outGroups] retain];
    }

    return self->groups_;
}

- (Group *) groupWithIdentifier: (NSString *) ident
{
    for (Group *group in self.groups)
    {
        if ([ident isEqualToString: group.identifier])
            return group;
    }

    return nil;
}

- (NSString *) identifier
{
    return [self.result stringForKey: UPDATE_IDENTIFIER_KEY];
}

- (id) initWithInfo: (NSDictionary *) info
           password: (NSString *) password
{
    //NSLog (@"*** Session.initWithInfo: %@", info);

    self = [super init];

    if (self)
    {
        self->info_ = [info retain];
        self->password_ = [password copy];

        self.result = [self.info dictionaryForKey: INIT_RESULT_KEY];
    }

    return self;
}

- (NSString *) loginDateTime
{
    return [self.info stringForKey: INIT_LOGIN_DATE_TIME_KEY];
}

- (void) setGroupInFocus: (Group *) group
{
    if (self->groupInFocus_ != group)
    {
        self.memberInFocus = nil;   // new/no group; no member in focus ...

        [self->groupInFocus_ release];

        self->groupInFocus_ = [group retain];

        // Kluge for now ...
        [self.appDelegate.sessionManager updateGroupInfo: nil];
    }
}

- (void) updateInfo: (NSDictionary *) info
{
    //NSLog (@"*** Session.updateInfo: %@", info);

    NSAssert ([self.identifier isEqualToString: [info stringForKey: UPDATE_IDENTIFIER_KEY]],
              @"Wrong session ID for update!");

    self.result = info;
    self.groups = nil;
}

- (NSString *) userFamilyName
{
    return [self.result stringForKey: UPDATE_USER_FAMILY_NAME_KEY];
}

- (NSString *) userGivenName
{
    return [self.result stringForKey: UPDATE_USER_GIVEN_NAME_KEY];
}

- (NSString *) userID
{
    return [self.result stringForKey: UPDATE_USER_ID_KEY];
}

- (NSString *) userName
{
    BOOL      hasFamilyName = ([self.userFamilyName length] > 0);
    BOOL      hasGivenName = ([self.userGivenName length] > 0);
    NSString *tmpName;

    if (hasGivenName && hasFamilyName)
        tmpName = [NSString stringWithFormat:
                   @"%@ %@",
                   self.userGivenName,
                   self.userFamilyName];
    else if (hasGivenName)
        tmpName = self.userGivenName;
    else if (hasFamilyName)
        tmpName = self.userFamilyName;
    else
        tmpName = nil;

    return tmpName;
}

- (NSURL *) userPhotoURL
{
    return [self.result URLForKey: UPDATE_USER_PHOTO_URL_KEY];
}

#pragma mark Private Instance Methods

- (NSArray *) sortedGroups: (NSArray *) unsortedGroups
{
    NSSortDescriptor *sort = [[[NSSortDescriptor alloc] initWithKey: @"name"
                                                          ascending: YES
                                                           selector: @selector (caseInsensitiveCompare:)]
                              autorelease];
    NSArray          *sortDescs = [NSArray arrayWithObject: sort];

    return [unsortedGroups sortedArrayUsingDescriptors: sortDescs];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->info_ release];
    [self->groups_ release];
    [self->password_ release];
    [self->result_ release];
    [self->userName_ release];

    [super dealloc];
}

@end
