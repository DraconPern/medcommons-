//
//  Note.m
//  MCProvider
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "DictionaryAdditions.h"
#import "Note.h"

#pragma mark -
#pragma mark Public Class Note
#pragma mark -

@implementation Note

@synthesize date       = date_;
@synthesize identifier = identifier_;
@synthesize text       = text_;
@dynamic    title;
@synthesize type       = type_;

#pragma mark Public Class Methods

+ (id) noteWithIdentifier: (NSString *) identifier
                     date: (NSDate *) date
                     type: (NoteType) type
                     text: (NSString *) text
{
    return [[[self alloc] initWithIdentifier: identifier
                                        date: date
                                        type: type
                                        text: text]
            autorelease];
}

+ (id) noteWithPropertyList: (NSDictionary *) plist
{
    return [[[self alloc] initWithPropertyList: plist]
            autorelease];
}

#pragma mark Public Instance Methods

- (id) initWithIdentifier: (NSString *) identifier
                     date: (NSDate *) date
                     type: (NoteType) type
                     text: (NSString *) text
{
    self = [super init];

    if (self)
    {
        self->date_ = [date copy];
        self->identifier_ = [identifier copy];
        self->text_ = [text copy];
        self->type_ = type;
    }

    return self;
}

- (id) initWithPropertyList: (NSDictionary *) plist
{
    NSDate   *tmpDate = [NSDate dateWithTimeIntervalSince1970:
                         [plist doubleForKey: @"reqtime"]];
    NSString *tmpIdentifier = [plist stringForKey: @"id"];
    NSString *soapA = [plist stringForKey: @"soapA"];
    NSString *soapC = [plist stringForKey: @"soapC"];
    NSString *soapD = [plist stringForKey: @"soapD"];
    NSString *soapP = [plist stringForKey: @"soapP"];
    NSString *soapS = [plist stringForKey: @"soapS"];
    NSString *tmpText = nil;
    NoteType  tmpType = 0;

    if (soapC)
    {
        tmpText = soapC;
        tmpType = NoteTypeConsultation;
    }
    else if (soapD)
    {
        tmpText = soapD;
        tmpType = NoteTypeDischarge;
    }
    else if (soapS)
    {
        tmpText = soapS;
        tmpType = NoteTypeSignOut;
    }
    else if (soapP)
    {
        tmpText = soapP;
        tmpType = NoteTypeProgress;
    }
    else if (soapA)
    {
        tmpText = soapA;
        tmpType = NoteTypeAdmissions;
    }

    if (!tmpText)
        tmpText = @"";

    return [self initWithIdentifier: tmpIdentifier
                               date: tmpDate
                               type: tmpType
                               text: tmpText];
}

- (NSDictionary *) propertyList
{
    NSString *key = nil;

    switch (self.type)
    {
        case NoteTypeAdmissions :
            key = @"soapA";
            break;

        case NoteTypeConsultation :
            key = @"soapC";
            break;

        case NoteTypeDischarge :
            key = @"soapD";
            break;

        case NoteTypeProgress :
            key = @"soapP";
            break;

        case NoteTypeSignOut :
            key = @"soapS";
            break;

        default :
            break;
    }

    if (key)
        return (self.identifier ?
                [NSDictionary dictionaryWithObjectsAndKeys:
                 self.identifier, @"id",
                 self.text,       key,
                 nil] :
                [NSDictionary dictionaryWithObject: self.text
                                            forKey: key]);

    return nil;
}

- (NSString *) title
{
    if (!self->title_)
    {
        NSString *noteType;

        switch (self->type_)
        {
            case NoteTypeAdmissions :
                noteType = NSLocalizedString (@"Admissions Note", @"");
                break;

            case NoteTypeConsultation :
                noteType = NSLocalizedString (@"Consultation Note", @"");
                break;

            case NoteTypeDischarge :
                noteType = NSLocalizedString (@"Discharge Note", @"");
                break;

            case NoteTypeProgress :
                noteType = NSLocalizedString (@"Progress Note", @"");
                break;

            case NoteTypeSignOut :
                noteType = NSLocalizedString (@"Sign Out Note", @"");
                break;

            default :
                noteType = NSLocalizedString (@"Note", @"");
                break;
        }

        NSDateFormatter *dateFormatter = [[[NSDateFormatter alloc] init]
                                          autorelease];

        [dateFormatter setDateFormat: @"yyyy-MM-dd HH:mm"];

        self->title_ = [[NSString stringWithFormat: @"%@ %@",
                         [dateFormatter stringFromDate: self->date_],
                         noteType] retain];
    }

    return self->title_;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->date_ release];
    [self->identifier_ release];
    [self->text_ release];
    [self->title_ release];

    [super dealloc];
}

@end
