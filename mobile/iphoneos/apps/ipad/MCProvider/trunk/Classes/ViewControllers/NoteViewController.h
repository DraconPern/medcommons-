//
//  NoteViewController.h
//  MCProvider
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "Note.h"

@protocol NoteViewControllerDelegate;

//
// IMPORTANT: note view controllers _must_ be used inside of a navigation
// controller in order to function properly.
//
@interface NoteViewController : UIViewController
{
@private

    id <NoteViewControllerDelegate>  delegate_;
    UIBarButtonItem                 *doneButton_;
    NSString                        *memberName_;
    Note                            *tempNote_;
    NSUndoManager                   *undoManager_;
    //
    // Flags:
    //
    BOOL                             editable_;
}

@property (nonatomic, assign, readwrite)                      id <NoteViewControllerDelegate> delegate;
@property (nonatomic, assign, readwrite, getter = isEditable) BOOL                            editable;
@property (nonatomic, assign, readonly)                       BOOL                            hidesMasterViewInLandscape;

- (id) initWithMember: (Member *) member
             noteType: (NoteType) noteType;

- (id) initWithMember: (Member *) member
                 note: (Note *) note;

@end

@protocol NoteViewControllerDelegate <NSObject>

@optional

- (void) noteViewController: (NoteViewController *) controller
          didFinishWithNote: (Note *) note;

@end
