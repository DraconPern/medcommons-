//
//  MCSOAPNoteView.h
//  MCShared
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "MCSOAPNote.h"

@interface MCSOAPNoteView : UIView
{
@private

    UIView                  *backgroundView_;
    UITextView              *contentView_;
    NSDate                  *date_;
    UIView                  *footerView_;
    UILabel                 *headerLabel_;
    UIView                  *headerView_;
    CGFloat                  keyboardHeight_;
    MCSOAPNoteType           noteType_;
    MCPatientID             *patientID_;
    UIActivityIndicatorView *uploadIndicator_;
    UILabel                 *uploadLabel_;
}

@property (nonatomic, readonly)             UITextView *contentView;
@property (nonatomic, getter = isEditable)  BOOL        editable;
@property (nonatomic, readonly)             MCSOAPNote *note;
@property (nonatomic, getter = isUploading) BOOL        uploading;

- (id) initWithFrame: (CGRect) frame
                note: (MCSOAPNote *) note;

@end
