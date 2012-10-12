//
//  Member.m
//  MCProvider
//
//  Created by Bill Donner on 12/27/09.
//  Copyright 2009 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "DictionaryAdditions.h"
#import "Member.h"
#import "MemberStore.h"
#import "Note.h"

#pragma mark -
#pragma mark Public Class Member
#pragma mark -

#pragma mark Internal Constants

#define DATE_FORMAT       @"yyyy-MM-dd HH:mm"

#define COMMENT_KEY       @"Comment"
#define CUSTOM0_KEY       @"custom_00"
#define CUSTOM1_KEY       @"custom_01"
#define DATE_OF_BIRTH_KEY @"DOB"
#define DATE_TIME_KEY     @"CreationDateTime"
#define FAMILY_NAME_KEY   @"PatientFamilyName"
#define GENDER_KEY        @"PatientSex"
#define GIVEN_NAME_KEY    @"PatientGivenName"
#define IDENTIFIER_KEY    @"PatientIdentifier"
#define PHOTO_URL_KEY     @"photoUrl"
#define PURPOSE_KEY       @"Purpose"
#define STATUS_KEY        @"Status"
#define VIEW_STATUS_KEY   @"ViewStatus"

#pragma mark Internal Functions

static NSComparisonResult compareNotes (Note *note1,
                                        Note *note2,
                                        void *context);

static NSComparisonResult compareNotes (Note *note1,
                                        Note *note2,
                                        void *context)
{
    //
    // Reverse time order:
    //
    NSComparisonResult cmp = [note2.date compare: note1.date];

    //
    // Reverse enum order:
    //
    if (cmp == NSOrderedSame)
    {
        if (note2.type < note1.type)
            cmp = NSOrderedAscending;
        else if (note2.type > note1.type)
            cmp = NSOrderedDescending;
    }

    return cmp;
}

@interface Member ()

@property (nonatomic, retain, readonly)  NSDateFormatter *dateFormatter;
@property (nonatomic, retain, readonly)  NSDictionary    *info;
@property (nonatomic, retain, readwrite) NSDictionary    *noteInfo;
@property (nonatomic, retain, readwrite) NSArray         *notes;

- (NSArray *) sortedNotes: (NSArray *) unsortedNotes;

@end

@implementation Member

@dynamic    comment;
@dynamic    custom0;
@dynamic    custom1;
@dynamic    dateOfBirth;
@dynamic    dateTime;
@dynamic    familyName;
@dynamic    gender;
@dynamic    givenName;
@dynamic    identifier;
@synthesize info        = info_;
@dynamic    isVisible;
@dynamic    name;
@synthesize noteInfo    = noteInfo_;
@synthesize notes       = notes_;
@dynamic    photoURL;
@dynamic    purpose;
@dynamic    status;
@dynamic    store;

#pragma mark Public Class Methods

+ (id) memberWithIdentifier: (NSString *) identifier
                  givenName: (NSString *) givenName
                 familyName: (NSString *) familyName
                   dateTime: (NSString *) dateTime
{
    return [[[Member alloc] initWithIdentifier: identifier
                                     givenName: givenName
                                    familyName: familyName
                                      dateTime: dateTime]
            autorelease];
}

+ (id) memberWithInfo: (NSDictionary *) info
{
    return [[[Member alloc] initWithInfo: info]
            autorelease];
}

#pragma mark Public Instance Methods

- (NSString *) comment
{
    return [self.info stringForKey: COMMENT_KEY];
}

- (NSString *) custom0
{
    return [self.info stringForKey: CUSTOM0_KEY];
}

- (NSString *) custom1
{
    return [self.info stringForKey: CUSTOM1_KEY];
}

- (NSString *) dateOfBirth
{
    return [self.info stringForKey: DATE_OF_BIRTH_KEY];
}

- (NSString *) dateTime
{
    if (!self->dateTime_)
        self->dateTime_ = [[self.dateFormatter stringFromDate:
                            [self.info dateForKey: DATE_TIME_KEY]]
                           copy];

    return self->dateTime_;
}

- (void) dump
{
    //    NSLog  (@"Member identifier: %@, name: %@, DOB: %@, status: %@",
    //            self.identifier,
    //            self.name,
    //            self.dateOfBirth,
    //            self.status);
}

- (NSString *) familyName
{
    return [self.info stringForKey: FAMILY_NAME_KEY];
}

- (NSString *) gender
{
    return [self.info stringForKey: GENDER_KEY];
}

- (NSString *) givenName
{
    return [self.info stringForKey: GIVEN_NAME_KEY];
}

- (NSString *) identifier
{
    return [self.info stringForKey: IDENTIFIER_KEY];
}

- (id) initWithIdentifier: (NSString *) identifier
                givenName: (NSString *) givenName
               familyName: (NSString *) familyName
                 dateTime: (NSString *) dateTime
{
    self = [super init];

    if (self)
    {
        self->dateTime_ = [dateTime copy];
        self->info_ = [[NSDictionary alloc] initWithObjectsAndKeys:
                       familyName, FAMILY_NAME_KEY,
                       givenName,  GIVEN_NAME_KEY,
                       identifier, IDENTIFIER_KEY,
                       nil];
        self->noteInfo_ = [[NSDictionary alloc] init];
    }

    return self;
}

- (id) initWithInfo: (NSDictionary *) info
{
    self = [super init];

    if (self)
        self->info_ = [info retain];

    return self;
}

- (BOOL) isVisible
{
    return [[self.info stringForKey: VIEW_STATUS_KEY] isEqualToString: @"Visible"];
}

- (NSString *) name
{
    BOOL      hasFamilyName = ([self.familyName length] > 0);
    BOOL      hasGivenName = ([self.givenName length] > 0);
    NSString *tmpName;

    if (hasGivenName && hasFamilyName)
        tmpName = [NSString stringWithFormat:
                   @"%@ %@",
                   self.givenName,
                   self.familyName];
    else if (hasGivenName)
        tmpName = self.givenName;
    else if (hasFamilyName)
        tmpName = self.familyName;
    else
        tmpName = nil;

    return tmpName;
}

- (NSArray *) notes
{
    if (!self->notes_)
    {
        NSArray        *inNotes = [self.noteInfo arrayForKey: @"blurts"];
        NSMutableArray *outNotes = [NSMutableArray arrayWithCapacity: [inNotes count]];

        for (NSDictionary *plist in inNotes)
        {
            Note *tmpNote = [Note noteWithPropertyList: plist];

            if (tmpNote)
                [outNotes addObject: tmpNote];
        }

        self->notes_ = [[self sortedNotes: outNotes] retain];
    }

    return self->notes_;
}

- (NSURL *) photoURL
{
    return [self.info URLForKey: PHOTO_URL_KEY];
}

- (NSString *) purpose
{
    return [self.info stringForKey: PURPOSE_KEY];
}

- (NSString *) status
{
    return [self.info stringForKey: STATUS_KEY];
}

- (MemberStore *) store
{
    if (!self->store_)
        self->store_ = [[MemberStore alloc] initWithMember: self];

    return self->store_;
}

- (void) updateNoteInfo: (NSDictionary *) noteInfo
{
    self.noteInfo = noteInfo;
    self.notes = nil;
}

#pragma mark Private Instance Methods

- (NSDateFormatter *) dateFormatter
{
    if (!self->dateFormatter_)
    {
        self->dateFormatter_ = [[NSDateFormatter alloc] init];

        [self->dateFormatter_ setDateFormat: DATE_FORMAT];
    }

    return self->dateFormatter_;
}

- (NSArray *) sortedNotes: (NSArray *) unsortedNotes
{
    return [unsortedNotes sortedArrayUsingFunction: compareNotes
                                           context: NULL];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->dateFormatter_ release];
    [self->dateTime_ release];
    [self->info_ release];
    [self->noteInfo_ release];
    [self->notes_ release];
    [self->store_ release];

    [super dealloc];
}

@end
