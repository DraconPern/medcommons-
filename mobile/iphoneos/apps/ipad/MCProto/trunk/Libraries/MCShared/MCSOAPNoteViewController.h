//
//  MCSOAPNoteViewController.h
//  MCShared
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "MCSOAPNote.h"

@protocol MCSOAPNoteViewControllerDelegate;

typedef enum
{
    MCSOAPNoteEditResultCanceled,
    MCSOAPNoteEditResultSaved,      // + ...SavedAsDraft ???
    MCSOAPNoteEditResultFailed
} MCSOAPNoteEditResult;

//
// IMPORTANT: SOAP note view controllers _must_ be used inside of a navigation
// controller in order to function properly.
//
@interface MCSOAPNoteViewController : UIViewController
{
@private

    UIBarButtonItem                       *cancelButton_;
    id <MCSOAPNoteViewControllerDelegate>  delegate_;
    NSString                              *patientName_;
    UIBarButtonItem                       *saveButton_;     // uploadButton_ ???
    MCSOAPNote                            *tempNote_;
    NSUndoManager                         *undoManager_;    // make public property?
    struct
    {
        unsigned int showsCancelButton : 1;
    } flags_;
}

@property (nonatomic, assign)              id <MCSOAPNoteViewControllerDelegate> delegate;
@property (nonatomic, getter = isEditable) BOOL                                  editable;
@property (nonatomic)                      BOOL                                  showsCancelButton;

- (id) initWithNote: (MCSOAPNote *) note;

- (id) initWithPatientID: (MCPatientID *) patientID
                noteType: (MCSOAPNoteType) noteType;

@end

@protocol MCSOAPNoteViewControllerDelegate <NSObject>

@optional

- (void) soapNoteViewController: (MCSOAPNoteViewController *) controller
              didFinishWithNote: (MCSOAPNote *) note
                         result: (MCSOAPNoteEditResult) result;

@end
