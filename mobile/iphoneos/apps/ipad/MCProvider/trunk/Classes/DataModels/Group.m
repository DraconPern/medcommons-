//
//  Group.m
//  MCProvider
//
//  Created by Bill Donner on 4/22/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "DictionaryAdditions.h"
#import "Group.h"
#import "Member.h"

#pragma mark -
#pragma mark Public Class Group
#pragma mark -

#pragma mark Internal Constants

//
// Keys for info provided on init:
//
#define INIT_IDENTIFIER_KEY   @"accid"
#define INIT_LOGO_URL_KEY     @"logo_url"
#define INIT_NAME_KEY         @"name"

//
// Keys for info provided on update:
//
#define UPDATE_FILTER_A_KEY   @"patients"
#define UPDATE_FILTER_B_KEY   @"members"
#define UPDATE_IDENTIFIER_KEY @"groupidx"

@interface Group ()

@property (nonatomic, retain, readonly)  NSDictionary *info;
@property (nonatomic, retain, readwrite) NSArray      *members;
@property (nonatomic, retain, readwrite) NSArray      *membersFilteredA;
@property (nonatomic, retain, readwrite) NSArray      *membersFilteredB;
@property (nonatomic, retain, readwrite) NSDictionary *result;

- (NSArray *) sortedMembers: (NSArray *) unsortedMembers;

@end

@implementation Group

@dynamic    identifier;
@synthesize info             = info_;
@dynamic    logoURL;
@synthesize members          = members_;
@synthesize membersFilteredA = membersFilteredA_;
@synthesize membersFilteredB = membersFilteredB_;
@dynamic    name;
@synthesize result           = result_;

#pragma mark Public Class Methods

+ (id) groupWithInfo: (NSDictionary *) info
{
    return [[[Group alloc] initWithInfo: info]
            autorelease];
}

#pragma mark Public Instance Methods

- (void) dump
{
    NSLog (@"Group identifier: %@, name: %@, logoURL: <%@>",
           self.identifier,
           self.name,
           self.logoURL);
}

- (NSString *) identifier
{
    return [self.info stringForKey: INIT_IDENTIFIER_KEY];
}

- (id) initWithInfo: (NSDictionary *) info
{
    //NSLog (@"*** Group.initWithInfo: %@", info);

    self = [super init];

    if (self)
        self->info_ = [info retain];

    return self;
}

- (NSURL *) logoURL
{
    return [self.info URLForKey: INIT_LOGO_URL_KEY];
}

- (NSArray *) members   // same as membersFilteredA for now ...
{
    if (!self->members_)
    {
        NSArray        *inMembers = [self.result arrayForKey: UPDATE_FILTER_A_KEY];
        NSMutableArray *outMembers = [NSMutableArray arrayWithCapacity: [inMembers count]];

        for (NSDictionary *info in inMembers)
        {
            Member *tmpMember = [Member memberWithInfo: info];

            if (tmpMember)
                [outMembers addObject: tmpMember];
        }

        self->members_ = [[self sortedMembers: outMembers] retain];
    }

    return self->members_;
}

- (NSArray *) membersFilteredA
{
    if (!self->membersFilteredA_)
    {
        NSArray        *inMembers = [self.result arrayForKey: UPDATE_FILTER_A_KEY];
        NSMutableArray *outMembers = [NSMutableArray arrayWithCapacity: [inMembers count]];

        for (NSDictionary *info in inMembers)
        {
            Member *tmpMember = [Member memberWithInfo: info];

            if (tmpMember)
                [outMembers addObject: tmpMember];
        }

        self->membersFilteredA_ = [[self sortedMembers: outMembers] retain];
    }

    return self->membersFilteredA_;
}

- (NSArray *) membersFilteredB
{
    if (!self->membersFilteredB_)
    {
        NSArray        *inMembers = [self.result arrayForKey: UPDATE_FILTER_B_KEY];
        NSMutableArray *outMembers = [NSMutableArray arrayWithCapacity: [inMembers count]];

        for (NSDictionary *info in inMembers)
        {
            Member *tmpMember = [Member memberWithInfo: info];

            if (tmpMember)
                [outMembers addObject: tmpMember];
        }

        self->membersFilteredB_ = [[self sortedMembers: outMembers] retain];
    }

    return self->membersFilteredB_;
}

- (NSString *) name
{
    return [self.info stringForKey: INIT_NAME_KEY];
}

- (void) updateInfo: (NSDictionary *) info
{
    //NSLog (@"*** Group.updateInfo: %@", info);

    NSAssert ([self.identifier isEqualToString: [info stringForKey: UPDATE_IDENTIFIER_KEY]],
              @"Wrong group ID for update!");

    self.result = info;
    self.members = nil;
    self.membersFilteredA = nil;
    self.membersFilteredB = nil;
}

#pragma mark Private Instance Methods

- (NSArray *) sortedMembers: (NSArray *) unsortedMembers
{
    NSSortDescriptor *sort1 = [[[NSSortDescriptor alloc] initWithKey: @"familyName"
                                                           ascending: YES
                                                            selector: @selector (caseInsensitiveCompare:)]
                               autorelease];
    NSSortDescriptor *sort2 = [[[NSSortDescriptor alloc] initWithKey: @"givenName"
                                                           ascending: YES
                                                            selector: @selector (caseInsensitiveCompare:)]
                               autorelease];
    NSArray          *sortDescs = [NSArray arrayWithObjects: sort1, sort2, nil];

    return [unsortedMembers sortedArrayUsingDescriptors: sortDescs];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->info_ release];
    [self->members_ release];
    [self->membersFilteredA_ release];
    [self->membersFilteredB_ release];
    [self->result_ release];

    [super dealloc];
}

@end
