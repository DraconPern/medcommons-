//
//  NoteViewController.m
//  MCProvider
//
//  Created by J. G. Pusey on 3/18/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "Member.h"
#import "NoteView.h"
#import "NoteViewController.h"

#pragma mark -
#pragma mark Public Class NoteViewController
#pragma mark -

@interface NoteViewController () <UITextViewDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem *doneButton;
@property (nonatomic, assign, readonly)  BOOL             hasUnsavedText;
@property (nonatomic, copy,   readonly)  NSString        *memberName;
@property (nonatomic, retain, readonly)  Note            *tempNote;

- (void) finishNote: (id) sender;

- (void) updateNavigationItemAnimated: (BOOL) animated;

- (void) updateToolbarAnimated: (BOOL) animated;

@end

@implementation NoteViewController

@synthesize delegate                   = delegate_;
@synthesize doneButton                 = doneButton_;
@synthesize editable                   = editable_;
@dynamic    hasUnsavedText;
@dynamic    hidesMasterViewInLandscape;
@synthesize memberName                 = memberName_;
@dynamic    tempNote;

#pragma mark Public Instance Methods

- (id) initWithMember: (Member *) member
             noteType: (NoteType) noteType
{
    self = [super init];

    if (self)
    {
        self->memberName_ = [member.name copy];
        self->tempNote_ = [[Note alloc] initWithIdentifier: nil
                                                      date: [NSDate date]
                                                      type: noteType
                                                      text: @""];
    }

    return self;
}

- (id) initWithMember: (Member *) member
                 note: (Note *) note
{
    self = [super init];

    if (self)
    {
        self->memberName_ = [member.name copy];
        self->tempNote_ = [[Note alloc] initWithIdentifier: nil // ??? or: note.identifier ???
                                                      date: [NSDate date]
                                                      type: note.type
                                                      text: note.text];
    }

    return self;
}

- (void) setEditable: (BOOL) editable
{
    if (self->editable_ != editable)
    {
        self->editable_ = editable;

        //
        // ONLY IF view is already loaded do we set its editable property in
        // sync; otherwise, we will prematurely load the view causing all kinds
        // of weirdness:
        //
        if (self.isViewLoaded)
        {
            NoteView *noteView = (NoteView *) self.view;

            if (noteView.isEditable != editable)
            {
                noteView.editable = editable;

                [self updateNavigationItemAnimated: NO];
            }
        }
    }
}

#pragma mark Private Instance Methods

- (void) finishNote: (id) sender
{
    NoteView *noteView = (NoteView *) self.view;

    [noteView.contentView resignFirstResponder];

    if ([self.delegate respondsToSelector: @selector (noteViewController:didFinishWithNote:)])
        [self.delegate noteViewController: self
                        didFinishWithNote: noteView.note];
}

- (BOOL) hasUnsavedText
{
    return ((NoteView *) self.view).hasUnsavedText;
}

- (Note *) tempNote
{
    Note *note = [self->tempNote_ autorelease];

    self->tempNote_ = nil;

    return note;
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    if (!self.doneButton)
        self.doneButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemDone
                                                                         target: self
                                                                         action: @selector (finishNote:)]
                           autorelease];

    self.navigationItem.title = [NSString stringWithFormat:
                                 (self.isEditable ?
                                  NSLocalizedString (@"New Note for %@", @"") :
                                  NSLocalizedString (@"Note for %@", @"")),
                                 self.memberName];

    [self.navigationItem setRightBarButtonItem: ((self.isEditable &&
                                                  self.hasUnsavedText) ?
                                                 self.doneButton :
                                                 nil)
                                      animated: animated];
}

- (void) updateToolbarAnimated: (BOOL) animated
{
    [(NoteView *) self.view updateToolbarAnimated: animated];
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    CGRect    tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
    NoteView *noteView = [[[NoteView alloc] initWithFrame: tmpFrame
                                                     note: self.tempNote]
                          autorelease];

    noteView.editable = self.isEditable;

    if (self.isEditable)
        noteView.contentView.delegate = self;

    self.view = noteView;

    self->undoManager_ = [[NSUndoManager alloc] init];

    [self->undoManager_ setLevelsOfUndo: 20];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) toOrient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];

    //
    // For some strange reason, the following call MUST go in viewDidAppear;
    // it does NOT work in viewWillAppear -- go figure:
    //
    [self updateToolbarAnimated: animated];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    [self updateNavigationItemAnimated: animated];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->doneButton_ release];
    [self->memberName_ release];
    [self->tempNote_ release];
    [self->undoManager_ release];

    [super dealloc];
}

#pragma mark Extended UIViewController Methods

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

#pragma mark UITextViewDelegate Methods

- (void) textViewDidChange: (UITextView *) textView
{
    if (textView == ((NoteView *) self.view).contentView)
    {
        [self updateNavigationItemAnimated: YES];
        [self updateToolbarAnimated: YES];
    }
}

@end
