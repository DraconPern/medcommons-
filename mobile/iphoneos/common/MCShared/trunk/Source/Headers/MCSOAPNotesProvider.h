//
//  MCSOAPNotesProvider.h
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "MCPatientID.h";
#import "MCSOAPNote.h"

@interface MCSOAPNotesProvider : NSObject
{
@private

    NSArray     *notes_;
    MCPatientID *patientID_;
}

@property (nonatomic, readonly) NSArray     *notes;
@property (nonatomic, readonly) MCPatientID *patientID;

+ (id) providerWithPatientID: (MCPatientID *) patientID;

- (id) initWithPatientID: (MCPatientID *) patientID;

- (void) setNeedsUpdate;    // ???

- (BOOL) uploadNote: (MCSOAPNote *) note;

@end
