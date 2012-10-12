//
//  NoteView.h
//  MCProvider
//
//  Created by J. G. Pusey on 4/6/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "Note.h"

@class Member;

@interface NoteView : MCView
{
@private

    UIView             *backgroundView_;
    UITextView         *contentView_;
    NSDate             *date_;
    UIToolbar          *footerToolbar_;
    UIView             *footerView_;
    UILabel            *headerLabel_;
    UIView             *headerView_;
    NoteType            noteType_;
    UISegmentedControl *segmentedControl_;
    //
    // Flags:
    //
    BOOL                isShowingToolbar_;
}

@property (nonatomic, retain, readonly)                       UITextView *contentView;
@property (nonatomic, assign, readwrite, getter = isEditable) BOOL        editable;
@property (nonatomic, assign, readonly)                       BOOL        hasUnsavedText;
@property (nonatomic, retain, readonly)                       Note       *note;

- (id) initWithFrame: (CGRect) frame
                note: (Note *) note;

- (void) setEditable: (BOOL) editable
            animated: (BOOL) animated;

- (void) updateToolbarAnimated: (BOOL) animated;

@end
