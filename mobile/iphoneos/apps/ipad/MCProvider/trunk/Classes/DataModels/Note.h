//
//  Note.h
//  MCProvider
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum
{
    NoteTypeMin          = 1,
    NoteTypeAdmissions   = NoteTypeMin,
    NoteTypeProgress,
    NoteTypeSignOut,
    NoteTypeDischarge,
    NoteTypeConsultation,
    NoteTypeMax          = NoteTypeConsultation,
    NoteTypeDefault      = NoteTypeProgress
} NoteType;

@interface Note : NSObject
{
@private

    NSDate   *date_;
    NSString *identifier_;
    NSString *text_;
    NSString *title_;
    NoteType  type_;
}

@property (nonatomic, copy,   readonly) NSDate   *date;
@property (nonatomic, copy,   readonly) NSString *identifier;
@property (nonatomic, copy,   readonly) NSString *text;
@property (nonatomic, copy,   readonly) NSString *title;
@property (nonatomic, assign, readonly) NoteType  type;

+ (id) noteWithIdentifier: (NSString *) identifier
                     date: (NSDate *) date
                     type: (NoteType) type
                     text: (NSString *) text;

+ (id) noteWithPropertyList: (NSDictionary *) plist;

- (id) initWithIdentifier: (NSString *) identifier
                     date: (NSDate *) date
                     type: (NoteType) type
                     text: (NSString *) text;

- (id) initWithPropertyList: (NSDictionary *) plist;

- (NSDictionary *) propertyList;

@end
