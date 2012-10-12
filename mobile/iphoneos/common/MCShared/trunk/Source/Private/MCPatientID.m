//
//  MCPatientID.m
//  MCShared
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCPatientID.h"

#pragma mark -
#pragma mark Public Class MCPatientID
#pragma mark -

@implementation MCPatientID

@synthesize dateOfBirth = dateOfBirth_;
@synthesize firstName   = firstName_;
@synthesize gender      = gender_;
@synthesize lastName    = lastName_;
@dynamic    name;

#pragma mark Dynamic Property Methods

- (NSString *) name
{
    if (!name_)
    {
        NSString *fname = firstName_;
        NSString *lname = lastName_;

        if ([@"" isEqual: fname])
            fname = nil;

        if ([@"" isEqual: lname])
            lname = nil;

        if (fname && lname)
            name_ = [[NSString stringWithFormat: @"%@ %@", fname, lname] retain];
        else if (fname)
            name_ = [fname copy];
        else if (lname)
            name_ = [lname copy];
        else
            name_ = @"<unknown>";
    }

    return name_;
}

#pragma mark Public Class Methods

+ (id) patientIDWithFirstName: (NSString *) firstName
                     lastName: (NSString *) lastName
                       gender: (NSString *) gender
                  dateOfBirth: (NSString *) dateOfBirth
{
    return [[[MCPatientID alloc] initWithFirstName: firstName
                                          lastName: lastName
                                            gender: gender
                                       dateOfBirth: dateOfBirth]
            autorelease];
}

#pragma mark Public Instance Methods

- (id) initWithFirstName: (NSString *) firstName
                lastName: (NSString *) lastName
                  gender: (NSString *) gender
             dateOfBirth: (NSString *) dateOfBirth
{
    if (self = [super init])
    {
        dateOfBirth_ = [dateOfBirth copy];
        firstName_ = [firstName copy];
        gender_ = [gender copy];
        lastName_ = [lastName copy];
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [dateOfBirth_ release];
    [firstName_ release];
    [gender_ release];
    [lastName_ release];
    [name_ release];

    [super dealloc];
}

@end
