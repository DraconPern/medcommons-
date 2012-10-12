//
//  MCSOAPNote.m
//  MCShared
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCSOAPNote.h"

#pragma mark -
#pragma mark Public Class MCSOAPNote
#pragma mark -

@implementation MCSOAPNote

@synthesize date      = date_;
@synthesize patientID = patientID_;
@synthesize text      = text_;
@dynamic    title;
@synthesize type      = type_;

#pragma mark Dynamic Property Methods

- (NSString *) title
{
    if (!title_)
    {
        NSString *noteType;

        switch (type_)
        {
            case MCSOAPNoteTypeAdmissions :
                noteType = NSLocalizedString (@"Admissions Note", @"");
                break;

            case MCSOAPNoteTypeConsultation :
                noteType = NSLocalizedString (@"Consultation Note", @"");
                break;

            case MCSOAPNoteTypeDischarge :
                noteType = NSLocalizedString (@"Discharge Note", @"");
                break;

            case MCSOAPNoteTypeProgress :
                noteType = NSLocalizedString (@"Progress Note", @"");
                break;

            case MCSOAPNoteTypeSignOut :
                noteType = NSLocalizedString (@"Sign Out Note", @"");
                break;

            default :
                noteType = NSLocalizedString (@"Unknown SOAP Note", @"");
                break;
        }

        NSDateFormatter *dateFormatter = [[[NSDateFormatter alloc] init] autorelease];

        [dateFormatter setDateFormat: @"yyyy-MM-dd HH:mm"];

        title_ = [[NSString stringWithFormat: @"%@ %@",
                   [dateFormatter stringFromDate: date_],
                   noteType] retain];
    }

    return title_;
}

#pragma mark Public Class Methods

+ (id) noteWithPatientID: (MCPatientID *) patientID
                    date: (NSDate *) date
                    type: (MCSOAPNoteType) type
                    text: (NSString *) text
{
    return [[[MCSOAPNote alloc] initWithPatientID: patientID
                                             date: date
                                             type: type
                                             text: text] autorelease];
}

#pragma mark Public Instance Methods

- (id) initWithPatientID: (MCPatientID *) patientID
                    date: (NSDate *) date
                    type: (MCSOAPNoteType) type
                    text: (NSString *) text
{
    if (self = [super init])
    {
        date_ = [date copy];
        patientID_ = [patientID retain];
        text_ = [text copy];
        type_ = type;
    }

    return self;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [date_ release];
    [patientID_ release];
    [text_ release];
    [title_ release];

    [super dealloc];
}

@end
