//
//  NoteListViewController.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/2/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "NoteListViewController.h"
#import "NoteViewController.h"
#import "SessionManager.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class NoteListViewController
#pragma mark -

@interface NoteListViewController () <NoteViewControllerDelegate, UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain, readwrite) UIBarButtonItem   *addButton;
@property (nonatomic, retain, readwrite) UIBarButtonItem   *backButton;
@property (nonatomic, retain, readwrite) NSMutableArray    *editableNotes;
@property (nonatomic, retain, readwrite) NSMutableIndexSet *pendingDeletes;
@property (nonatomic, assign, readwrite) NSUInteger         pendingInsertCount;

- (void) addNote: (id) sender;

- (void) addNotificationObservers;

- (Note *) noteAtIndex: (NSUInteger) idx;

- (BOOL) noteIsPendingDeleteAtIndex: (NSUInteger) idx;

- (BOOL) noteIsPendingInsertAtIndex: (NSUInteger) idx;

- (NSUInteger) numberOfNotes;

- (void) removeNotificationObservers;

- (void) updateNavigationItemAnimated: (BOOL) animated;

- (BOOL) uploadNote: (Note *) note;

//
// Forward declarations:
//
- (void) deleteNoteDidFinish: (NSNotification *) notification;

- (void) insertNoteDidFinish: (NSNotification *) notification;

- (void) listNotesDidFinish: (NSNotification *) notification;

@end

@implementation NoteListViewController

@synthesize addButton                  = addButton_;
@synthesize backButton                 = backButton_;
@synthesize delegate                   = delegate_;
@synthesize editableNotes              = editableNotes_;
@dynamic    hidesMasterViewInLandscape;
@synthesize member                     = member_;
@synthesize pendingDeletes             = pendingDeletes_;
@synthesize pendingInsertCount         = pendingInsertCount_;

#pragma mark Public Instance Methods

- (id) initWithMember: (Member *) member
{
    self = [super init];

    if (self)
        self->member_ = [member retain];

    return self;
}

#pragma mark Private Instance Methods

- (void) addNote: (id) sender
{
    NoteViewController *nvc = [[NoteViewController alloc] initWithMember: self.member
                                                                noteType: NoteTypeDefault];

    nvc.delegate = self;
    nvc.editable = YES;

    [self.navigationController pushViewController: nvc
                                         animated: YES];

    [nvc release];
}

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc addObserver: self
           selector: @selector (deleteNoteDidFinish:)
               name: SessionManagerDeleteNoteDidFinishNotification
             object: nil];

    [nc addObserver: self
           selector: @selector (insertNoteDidFinish:)
               name: SessionManagerInsertNoteDidFinishNotification
             object: nil];

    [nc addObserver: self
           selector: @selector (listNotesDidFinish:)
               name: SessionManagerListNotesDidFinishNotification
             object: nil];
}

- (Note *) noteAtIndex: (NSUInteger) idx
{
    return [(self.editing ?
             self.editableNotes :
             self.member.notes) objectAtIndex: idx];
}

- (BOOL) noteIsPendingDeleteAtIndex: (NSUInteger) idx
{
    return (self.editing &&
            [self.pendingDeletes containsIndex: idx - self.pendingInsertCount]);
}

- (BOOL) noteIsPendingInsertAtIndex: (NSUInteger) idx
{
    return (self.editing && (idx < self.pendingInsertCount));
}

- (NSUInteger) numberOfNotes
{
    return (self.editing ?
            [self.editableNotes count] :
            [self.member.notes count]);
}

- (void) removeNotificationObservers
{
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

- (void) updateNavigationItemAnimated: (BOOL) animated
{
    if (self.editing && !self.addButton)
        self.addButton = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemAdd
                                                                        target: self
                                                                        action: @selector (addNote:)]
                          autorelease];

    if (!self.backButton)
        self.backButton = [[[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"All Notes", @"")
                                                            style: UIBarButtonItemStylePlain
                                                           target: nil
                                                           action: NULL]
                           autorelease];

    self.navigationItem.title = [NSString stringWithFormat:
                                 NSLocalizedString (@"All Notes for %@", @""),
                                 self.member.name];

    self.navigationItem.backBarButtonItem = self.backButton;

    [self.navigationItem setHidesBackButton: self.editing
                                   animated: animated];

    [self.navigationItem setLeftBarButtonItem: (self.editing ?
                                                self.addButton :
                                                nil)
                                     animated: animated];

    [self.navigationItem setRightBarButtonItem: [self editButtonItem]
                                      animated: animated];
}

- (BOOL) uploadNote: (Note *) note
{
    SessionManager *sm = self.appDelegate.sessionManager;

    return [sm insertNote: note
               intoMember: self.member
                  options: SessionManagerOptionNone];
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    CGRect       tmpFrame = CGRectStandardize (self.parentViewController.view.bounds);
    UITableView *tabView = [[[UITableView alloc] initWithFrame: tmpFrame
                                                         style: UITableViewStylePlain]
                            autorelease];

    tabView.backgroundColor = self.appDelegate.styleManager.backgroundColorLight;
    tabView.dataSource = self;
    tabView.delegate = self;

    self.view = tabView;

    //
    // Not sure if this is the right place for this ...
    //
    SessionManager *sm = self.appDelegate.sessionManager;

    [sm listNotesForMember: self.member
                   options: SessionManagerOptionNone];
}

- (void) setEditing: (BOOL) editing
           animated: (BOOL) animated
{
    [super setEditing: editing
             animated: animated];

    if (editing)
    {
        self.editableNotes = [NSMutableArray arrayWithArray: self.member.notes];
        self.pendingDeletes = [NSMutableIndexSet indexSet];
        self.pendingInsertCount = 0;
    }
    else
    {
        SessionManager *sm = self.appDelegate.sessionManager;
        MemberStore    *mstore = self.member.store;
        Note           *note;

        //
        // First, snap any pending inserts to disk:
        //
        if (self.pendingInsertCount > 0)
        {
            [mstore beginUpdates];

            for (NSUInteger idx = 0; idx < self.pendingInsertCount; idx++)
                [mstore addNote: [self.editableNotes objectAtIndex: idx]];

            [mstore endUpdates];
        }

        //
        // Post insert and/or delete requests to server:
        //
        for (NSUInteger idx = 0; idx < [self.editableNotes count]; idx++)
        {
            note = [self.editableNotes objectAtIndex: idx];

            if (idx < self.pendingInsertCount)
                [sm insertNote: note
                    intoMember: self.member
                       options: SessionManagerOptionNone];
            else if ([self.pendingDeletes containsIndex: idx - self.pendingInsertCount])
                [sm deleteNote: note
                       options: SessionManagerOptionNone];
        }

        self.editableNotes = nil;
        self.pendingDeletes = nil;
        self.pendingInsertCount = 0;
    }

    [(UITableView *) self.view setEditing: editing
                                 animated: animated];

    [self updateNavigationItemAnimated: animated];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidAppear: (BOOL) animated
{
    [super viewDidAppear: animated];

    [(UITableView *) self.view flashScrollIndicators];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    [self addNotificationObservers];
    [self updateNavigationItemAnimated: animated];

    NSIndexPath *idxPath = [(UITableView *) self.view indexPathForSelectedRow];

    if (idxPath)
        [(UITableView *) self.view deselectRowAtIndexPath: idxPath
                                                 animated: NO];
}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];

    [self removeNotificationObservers];
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->addButton_ release];
    [self->backButton_ release];
    [self->editableNotes_ release];
    [self->member_ release];
    [self->pendingDeletes_ release];

    [super dealloc];
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (BOOL) hidesMasterViewInLandscape
{
    return YES;
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}

#pragma mark NoteViewControllerDelegate Methods

- (void) noteViewController: (NoteViewController *) controller
          didFinishWithNote: (Note *) note
{
    if (note)
    {
        [self.editableNotes insertObject: note
                                 atIndex: 0];

        self.pendingInsertCount++;

        NSIndexPath *idxPath = [NSIndexPath indexPathForRow: 0
                                                  inSection: 0];
        UITableView *tabView = (UITableView *) self.view;

        [tabView beginUpdates];
        [tabView insertRowsAtIndexPaths: [NSArray arrayWithObject: idxPath]
                       withRowAnimation: UITableViewRowAnimationLeft];
        [tabView endUpdates];
    }

    [self.navigationController popViewControllerAnimated: YES];
}

#pragma mark SessionManager Notification Methods

- (void) deleteNoteDidFinish: (NSNotification *) notification
{
    SessionManager *sm = self.appDelegate.sessionManager;

    [sm listNotesForMember: self.member
                   options: SessionManagerOptionNone];
}

- (void) insertNoteDidFinish: (NSNotification *) notification
{
    SessionManager *sm = self.appDelegate.sessionManager;

    [sm listNotesForMember: self.member
                   options: SessionManagerOptionNone];
}

- (void) listNotesDidFinish: (NSNotification *) notification
{
    [(UITableView *) self.view reloadData];
}

#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier = @"NoteListCell";

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier];

    if (!cell)
        cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle
                                       reuseIdentifier: CellIdentifier]
                autorelease];

    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.highlighted = NO;

    Note *note = [self noteAtIndex: idxPath.row];

    cell.detailTextLabel.text = note.text;
    cell.textLabel.text = note.title;

    StyleManager *styles = self.appDelegate.styleManager;

    if ([self noteIsPendingDeleteAtIndex: idxPath.row])
    {
        cell.textLabel.font = styles.labelFontBoldXL;
        cell.textLabel.textColor = [UIColor grayColor];
    }
    else if ([self noteIsPendingInsertAtIndex: idxPath.row])
    {
        cell.textLabel.font = styles.labelFontItalicXL;
        cell.textLabel.textColor = [UIColor darkTextColor];
    }
    else
    {
        cell.textLabel.font = styles.labelFontBoldXL;
        cell.textLabel.textColor = [UIColor darkTextColor];
    }

    return cell;
}

- (void) tableView: (UITableView *) tabView
commitEditingStyle: (UITableViewCellEditingStyle) style
 forRowAtIndexPath: (NSIndexPath *) idxPath
{
    if (style == UITableViewCellEditingStyleDelete)
    {
        if ([self noteIsPendingInsertAtIndex: idxPath.row])
        {
            [self.editableNotes removeObjectAtIndex: idxPath.row];

            self.pendingInsertCount--;

            [tabView beginUpdates];
            [tabView deleteRowsAtIndexPaths: [NSArray arrayWithObject: idxPath]
                           withRowAnimation: UITableViewRowAnimationRight];
            [tabView endUpdates];
        }
        else
        {
            [self.pendingDeletes addIndex: idxPath.row - self.pendingInsertCount];

            [tabView beginUpdates];
            [tabView reloadRowsAtIndexPaths: [NSArray arrayWithObject: idxPath]
                           withRowAnimation: UITableViewRowAnimationFade];
            [tabView endUpdates];
        }
    }
}

- (NSInteger) tableView: (UITableView *) tableView
  numberOfRowsInSection: (NSInteger) section
{
    return [self numberOfNotes];
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    Note               *note = [self noteAtIndex: idxPath.row];
    NoteViewController *nvc = [[[NoteViewController alloc] initWithMember: self.member
                                                                     note: note]
                               autorelease];

    nvc.delegate = self;
    nvc.editable = [self noteIsPendingInsertAtIndex: idxPath.row];

    [self.navigationController pushViewController: nvc
                                         animated: YES];
}

- (UITableViewCellEditingStyle) tableView: (UITableView *) tabView
            editingStyleForRowAtIndexPath: (NSIndexPath *) idxPath
{
    return ([self noteIsPendingDeleteAtIndex: idxPath.row] ?
            UITableViewCellEditingStyleNone :
            UITableViewCellEditingStyleDelete);
}

- (NSIndexPath *) tableView: (UITableView *) tabView
   willSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    if ([self noteIsPendingDeleteAtIndex: idxPath.row]) // ???
    {
        [tabView deselectRowAtIndexPath: idxPath
                               animated: YES];

        return nil;
    }

    return idxPath;
}

@end
