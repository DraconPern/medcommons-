//
//  MCSOAPNoteView.h
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCSOAPNote.h"
#import "MCStandardView.h"

@interface MCSOAPNoteView : MCStandardView
{
@private

    NSDate                  *date_;
    UILabel                 *headerLabel_;
    MCSOAPNoteType           noteType_;
    MCPatientID             *patientID_;
    UIActivityIndicatorView *uploadIndicator_;
    UILabel                 *uploadLabel_;
}

@property (nonatomic, getter = isEditable)  BOOL        editable;
@property (nonatomic, readonly)             MCSOAPNote *note;
@property (nonatomic, getter = isUploading) BOOL        uploading;

- (id) initWithFrame: (CGRect) frame
                note: (MCSOAPNote *) note;

@end
