//
//  MCSOAPNote.h
//  MCShared
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "MCPatientID.h";

typedef enum
{
    MCSOAPNoteTypeMin          = 1,
    MCSOAPNoteTypeAdmissions   = MCSOAPNoteTypeMin,
    MCSOAPNoteTypeProgress,
    MCSOAPNoteTypeSignOut,
    MCSOAPNoteTypeDischarge,
    MCSOAPNoteTypeConsultation,
    MCSOAPNoteTypeMax          = MCSOAPNoteTypeConsultation,
    MCSOAPNoteTypeDefault      = MCSOAPNoteTypeProgress
} MCSOAPNoteType;

@interface MCSOAPNote : NSObject
{
@private

    NSDate         *date_;
    MCPatientID    *patientID_;
    NSString       *text_;
    NSString       *title_;
    MCSOAPNoteType  type_;
}

@property (nonatomic, readonly) NSDate         *date;
@property (nonatomic, readonly) MCPatientID    *patientID;
@property (nonatomic, readonly) NSString       *text;
@property (nonatomic, readonly) NSString       *title;
@property (nonatomic, readonly) MCSOAPNoteType  type;

+ (id) noteWithPatientID: (MCPatientID *) patientID
                    date: (NSDate *) date
                    type: (MCSOAPNoteType) type
                    text: (NSString *) text;

- (id) initWithPatientID: (MCPatientID *) patientID
                    date: (NSDate *) date
                    type: (MCSOAPNoteType) type
                    text: (NSString *) text;

@end
