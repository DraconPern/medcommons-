//
//  MCSOAPNoteViewController.m
//  MCShared
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCPatientID.h"
#import "MCSOAPNoteView.h"
#import "MCSOAPNoteViewController.h"

#pragma mark -
#pragma mark Public Class MCSOAPNoteViewController
#pragma mark -

@interface MCSOAPNoteViewController ()

- (void) cancelNoteUpdates: (id) sender;

- (void) saveNoteUpdates: (id) sender;

- (void) updateTitleAndNavigationItem;

@end

@implementation MCSOAPNoteViewController

@synthesize delegate          = delegate_;
@dynamic    editable;
@dynamic    showsCancelButton;

#pragma mark Dynamic Property Methods

- (BOOL) isEditable
{
    return ((MCSOAPNoteView *) self.view).isEditable;
}

- (void) setEditable: (BOOL) editable
{
    BOOL oldEditable = ((MCSOAPNoteView *) self.view).isEditable;

    if (!oldEditable != !editable)
    {
        ((MCSOAPNoteView *) self.view).editable = editable;

        [self updateTitleAndNavigationItem];
    }

}

- (void) setShowsCancelButton: (BOOL) showsCancelButton
{
    flags_.showsCancelButton = showsCancelButton;
}

- (BOOL) showsCancelButton
{
    return (flags_.showsCancelButton ? YES : NO);
}

#pragma mark Public Instance Methods

- (id) initWithNote: (MCSOAPNote *) note
{
    if (self = [super init])
    {
        patientName_ = [note.patientID.name copy];
        tempNote_ = [[MCSOAPNote alloc] initWithPatientID: note.patientID
                                                     date: [NSDate date]
                                                     type: note.type
                                                     text: note.text];
    }

    return self;
}

- (id) initWithPatientID: (MCPatientID *) patientID
                noteType: (MCSOAPNoteType) noteType
{
    if (self = [super init])
    {
        patientName_ = [patientID.name copy];
        tempNote_ = [[MCSOAPNote alloc] initWithPatientID: patientID
                                                     date: [NSDate date]
                                                     type: noteType
                                                     text: @""];
    }

    return self;
}

#pragma mark Private Instance Methods

- (void) cancelNoteUpdates: (id) sender
{
    MCSOAPNoteView *noteView = (MCSOAPNoteView *) self.view;

    [noteView.contentView resignFirstResponder];

    if ([self.delegate respondsToSelector: @selector (soapNoteViewController:didFinishWithNote:result:)])
        [self.delegate soapNoteViewController: self
                            didFinishWithNote: nil
                                       result: MCSOAPNoteEditResultCanceled];
}

- (void) saveNoteUpdates: (id) sender
{
    MCSOAPNoteView *noteView = (MCSOAPNoteView *) self.view;

    [noteView.contentView resignFirstResponder];

    noteView.uploading = YES;

    if ([self.delegate respondsToSelector: @selector (soapNoteViewController:didFinishWithNote:result:)])
        [self.delegate soapNoteViewController: self
                            didFinishWithNote: noteView.note
                                       result: MCSOAPNoteEditResultSaved];
}

- (void) updateTitleAndNavigationItem
{
    self.title = [NSString stringWithFormat:
                  (self.isEditable ?
                   NSLocalizedString (@"New Note for %@", @"") :
                   NSLocalizedString (@"Note for %@", @"")),
                  patientName_];

    if (!cancelButton_)
        cancelButton_ = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemCancel
                                                                      target: self
                                                                      action: @selector (cancelNoteUpdates:)];

    if (!saveButton_)
        saveButton_ = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemSave
                                                                    target: self
                                                                    action: @selector (saveNoteUpdates:)];

    self.navigationItem.leftBarButtonItem = (self.showsCancelButton ?
                                             cancelButton_ :
                                             nil);

    self.navigationItem.rightBarButtonItem = (self.isEditable ?
                                              saveButton_ :
                                              nil);
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    self.view = [[[MCSOAPNoteView alloc] initWithFrame: self.parentViewController.view.frame
                                                  note: tempNote_]
                 autorelease];

    [tempNote_ release];

    tempNote_ = nil;

    undoManager_ = [[NSUndoManager alloc] init];

    [undoManager_ setLevelsOfUndo: 20];

    [self updateTitleAndNavigationItem];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation
{
    return YES;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [cancelButton_ release];
    [patientName_ release];
    [saveButton_ release];
    [tempNote_ release];
    [undoManager_ release];

    [super dealloc];
}

@end
