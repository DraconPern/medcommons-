//
//  MCSOAPNotesProvider.m
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCComms.h"
#import "MCSOAPNote.h"
#import "MCSOAPNotesProvider.h"

#pragma mark -
#pragma mark "Package-private" Class MCSOAPNotesProvider
#pragma mark -

@interface MCSOAPNotesProvider ()

+ (NSArray *) extractSOAPNotes: (NSArray *) blurts
                     patientID: (MCPatientID *) patientID;

+ (NSDictionary *) formatSOAPNote: (MCSOAPNote *) note;

- (MCComms *) connection;

@end

@implementation MCSOAPNotesProvider

@synthesize patientID = patientID_;
@dynamic    notes;

#pragma mark Dynamic Property Methods

- (NSArray *) notes
{
    if (!notes_)
    {
        NSDictionary *response = [[self connection] getBlurts];
        NSString     *status = [response objectForKey: @"status"];

        if ([@"ok" isEqualToString: status])
        {
            [notes_ release];

            notes_ = [[MCSOAPNotesProvider extractSOAPNotes: [response objectForKey: @"blurts"]
                                                  patientID: patientID_]
                      retain];
        }
        else
            notes_ = [[NSArray alloc] initWithObjects: nil];
    }

    return notes_;
}

#pragma mark Public Class Methods

+ (id) providerWithPatientID: (MCPatientID *) patientID
{
    return [[[MCSOAPNotesProvider alloc] initWithPatientID: patientID]
            autorelease];
}

#pragma mark Public Instance Methods

- (id) initWithPatientID: (MCPatientID *) patientID
{
    if (self = [super init])
        patientID_ = [patientID retain];

    return self;
}

- (void) setNeedsUpdate
{
    [notes_ release];

    notes_ = nil;
}

- (BOOL) uploadNote: (MCSOAPNote *) note
{
    NSDictionary *blurt = [MCSOAPNotesProvider formatSOAPNote: note];

    if (blurt)
    {
        NSDictionary *response = [[self connection] addBlurt: blurt];
        NSString     *status = [response objectForKey: @"status"];

        if ([@"ok" isEqualToString: status])
        {
            [self setNeedsUpdate];

            return YES;
        }
    }

    return NO;
}

#pragma mark Private Class Methods

+ (NSArray *) extractSOAPNotes: (NSArray *) blurts
                     patientID: (MCPatientID *) patientID
{
    NSMutableArray *tmpNotes = [NSMutableArray arrayWithCapacity: [blurts count] * 5];

    for (NSDictionary *blurt in [blurts reverseObjectEnumerator])
    {
        NSNumber *reqTime = [blurt objectForKey: @"reqtime"];
        NSDate   *date = [NSDate dateWithTimeIntervalSince1970: [reqTime doubleValue]];
        NSString *soapA = [blurt objectForKey: @"soapA"];
        NSString *soapC = [blurt objectForKey: @"soapC"];
        NSString *soapD = [blurt objectForKey: @"soapD"];
        NSString *soapP = [blurt objectForKey: @"soapP"];
        NSString *soapS = [blurt objectForKey: @"soapS"];

        if (soapC && ([soapC length] > 0))
            [tmpNotes addObject: [MCSOAPNote noteWithPatientID: patientID
                                                          date: date
                                                          type: MCSOAPNoteTypeConsultation
                                                          text: soapC]];
        if (soapD && ([soapD length] > 0))
            [tmpNotes addObject: [MCSOAPNote noteWithPatientID: patientID
                                                          date: date
                                                          type: MCSOAPNoteTypeDischarge
                                                          text: soapD]];

        if (soapS && ([soapS length] > 0))
            [tmpNotes addObject: [MCSOAPNote noteWithPatientID: patientID
                                                          date: date
                                                          type: MCSOAPNoteTypeSignOut
                                                          text: soapS]];

        if (soapP && ([soapP length] > 0))
            [tmpNotes addObject: [MCSOAPNote noteWithPatientID: patientID
                                                          date: date
                                                          type: MCSOAPNoteTypeProgress
                                                          text: soapP]];

        if (soapA && ([soapA length] > 0))
            [tmpNotes addObject: [MCSOAPNote noteWithPatientID: patientID
                                                          date: date
                                                          type: MCSOAPNoteTypeAdmissions
                                                          text: soapA]];
    }

    return tmpNotes;
}

+ (NSDictionary *) formatSOAPNote: (MCSOAPNote *) note
{
    NSString *key = nil;

    switch (note.type)
    {
        case MCSOAPNoteTypeAdmissions :
            key = @"soapA";
            break;

        case MCSOAPNoteTypeConsultation :
            key = @"soapC";
            break;

        case MCSOAPNoteTypeDischarge :
            key = @"soapD";
            break;

        case MCSOAPNoteTypeProgress :
            key = @"soapP";
            break;

        case MCSOAPNoteTypeSignOut :
            key = @"soapS";
            break;

        default:
            break;
    }

    if (key)
        return [NSDictionary dictionaryWithObjectsAndKeys:
                note.text,
                key,
                nil];

    return nil;
}

#pragma mark Private Instance Methods

- (MCComms *) connection
{
    MCComms *comms = [MCComms sharedInstance];

    if (![comms isLoggedIn])    // for now ...
        [comms makeSecureConnectionWithAppliance: @"portal.medcommons.net"
                                           email: @"billdonner@gmail.com"
                                        password: @"tester"];

    return comms;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [notes_ release];
    [patientID_ release];

    [super dealloc];
}

@end
