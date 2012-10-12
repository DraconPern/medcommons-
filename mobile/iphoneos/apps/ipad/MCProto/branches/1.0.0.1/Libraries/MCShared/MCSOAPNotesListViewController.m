//
//  MCSOAPNotesListViewController.m
//  MCShared
//
//  Created by J. G. Pusey on 4/2/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCPatientID.h"
#import "MCSOAPNotesListView.h"
#import "MCSOAPNotesListViewController.h"
#import "MCSOAPNoteView.h"

#pragma mark -
#pragma mark Public Class MCSOAPNotesListViewController
#pragma mark -

#define NARROW_LANDSCAPE_WIDTH 703.0f
#define WIDE_LANDSCAPE_WIDTH   1024.0f

@interface MCSOAPNotesListViewController () <MCSOAPNoteViewControllerDelegate, UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, retain) UIPopoverController *popoverController;

- (void) addSOAPNote: (id) sender;

//- (void) refreshSOAPNotesList: (id) sender;

- (void) toggleLandscapeView: (id) sender;

- (void) updateTitleAndNavigationItem;

@end


@implementation MCSOAPNotesListViewController

@synthesize delegate          = delegate_;
@synthesize popoverController = popoverController_;
@synthesize provider          = provider_;

#pragma mark Public Instance Methods

- (id) initWithProvider: (MCSOAPNotesProvider *) provider
{
    if (self = [super init])
        provider_ = [provider retain];

    return self;
}

#pragma mark Private Instance Methods

- (void) addSOAPNote: (id) sender
{
    MCSOAPNoteViewController *snvc = [[MCSOAPNoteViewController alloc] initWithPatientID: [provider_ patientID]
                                                                                noteType: MCSOAPNoteTypeDefault];

    snvc.delegate = self;
    snvc.editable = YES;

    [self.navigationController pushViewController: snvc
                                         animated: YES];

    [snvc release];
}

#if 0
- (void) refreshSOAPNotesList: (id) sender
{
    [provider_ setNeedsUpdate];

    MCSOAPNotesListView *listView = (MCSOAPNotesListView *) self.view;

    [listView.contentView reloadData];
}
#endif

// rework this into something less hard-coded ...
- (void) toggleLandscapeView: (id) sender
{
    if (!UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))
        return;

    UIView *tmpView = self.navigationController.view;

    if (!tmpView)
        tmpView = self.view;

    CGRect tmpFrame = tmpView.frame;

    if (tmpFrame.size.width != WIDE_LANDSCAPE_WIDTH)
    {
        tmpFrame.origin.x   = 0.0f;
        tmpFrame.size.width = WIDE_LANDSCAPE_WIDTH;

        [self.navigationItem setLeftBarButtonItem: unwidenButton_
                                         animated: YES];
    }
    else
    {
        tmpFrame.origin.x   = WIDE_LANDSCAPE_WIDTH - NARROW_LANDSCAPE_WIDTH;
        tmpFrame.size.width = NARROW_LANDSCAPE_WIDTH;

        [self.navigationItem setLeftBarButtonItem: widenButton_
                                         animated: YES];
    }

    tmpView.frame = tmpFrame;
}

- (void) updateTitleAndNavigationItem
{
    self.title = [NSString stringWithFormat:
                  NSLocalizedString (@"All Notes for %@", @""),
                  [provider_ patientID].name];

    if (!addButton_)
        addButton_ = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemAdd
                                                                   target: self
                                                                   action: @selector (addSOAPNote:)];

    if (!backButton_)
        backButton_ = [[UIBarButtonItem alloc] initWithTitle: NSLocalizedString (@"All Notes", @"")
                                                       style: UIBarButtonItemStylePlain
                                                      target: nil
                                                      action: NULL];

#if 0
    if (!refreshButton_)
        refreshButton_ = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemRefresh
                                                                       target: self
                                                                       action: @selector (refreshSOAPNotesList:)];
#endif

    if (!unwidenButton_)
        unwidenButton_ = [[UIBarButtonItem alloc] initWithTitle: @">>>>"
                                                          style: UIBarButtonItemStylePlain
                                                         target: self
                                                         action: @selector (toggleLandscapeView:)];

    if (!widenButton_)
        widenButton_ = [[UIBarButtonItem alloc] initWithTitle: @"<<<<"
                                                        style: UIBarButtonItemStylePlain
                                                       target: self
                                                       action: @selector (toggleLandscapeView:)];

    self.navigationItem.backBarButtonItem = backButton_;
    self.navigationItem.rightBarButtonItem = addButton_;

    if (UIDeviceOrientationIsLandscape ([UIDevice currentDevice].orientation))
    {
        UIView *tmpView = self.navigationController.view;

        if (!tmpView)
            tmpView = self.view;

        CGRect tmpFrame = tmpView.frame;

        self.navigationItem.leftBarButtonItem = ((tmpFrame.size.width != WIDE_LANDSCAPE_WIDTH) ?
                                                 widenButton_ :
                                                 unwidenButton_);
    }
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    MCSOAPNotesListView *listView = [[[MCSOAPNotesListView alloc] initWithFrame: self.parentViewController.view.frame]
                                     autorelease];

    UITableView *tabView = (UITableView *) listView.contentView;

    tabView.dataSource = self;
    tabView.delegate = self;

    self.view = listView;

    [self updateTitleAndNavigationItem];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) interfaceOrientation
{
    return YES;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [addButton_ release];
    [backButton_ release];
    [popoverController_ release];
    [provider_ release];
    //    [refreshButton_ release];
    [widenButton_ release];

    [super dealloc];
}

#pragma mark MCSOAPNoteViewControllerDelegate Methods

- (void) soapNoteViewController: (MCSOAPNoteViewController *) controller
              didFinishWithNote: (MCSOAPNote *) note
                         result: (MCSOAPNoteEditResult) result
{
    if (note && (result == MCSOAPNoteEditResultSaved))
    {
        if ([provider_ uploadNote: note])
        {
            MCSOAPNotesListView *listView = (MCSOAPNotesListView *) self.view;
            UITableView         *tabView = (UITableView *) listView.contentView;

            [tabView reloadData];
        }
    }

    [self.navigationController popViewControllerAnimated: YES];
}

#pragma mark UISplitViewControllerDelegate Methods

- (void) splitViewController: (UISplitViewController *) svc
      willHideViewController: (UIViewController *) vc
           withBarButtonItem: (UIBarButtonItem *) bbi
        forPopoverController: (UIPopoverController *) pc
{
    bbi.title = NSLocalizedString (@"Menu", @"");

    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];

    self.popoverController = pc;
}

- (void) splitViewController: (UISplitViewController *) svc
      willShowViewController: (UIViewController *) vc
   invalidatingBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: widenButton_
                                     animated: YES];

    self.popoverController = nil;
}

#pragma mark UITableViewDataSource Methods

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: @"cell"];

    if (!cell)
        cell = [[[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle
                                       reuseIdentifier: @"cell"]
                autorelease];

    MCSOAPNote *note = [provider_.notes objectAtIndex: idxPath.row];

    cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
    cell.detailTextLabel.text = note.text;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.textLabel.text = note.title;

    return cell;
}

- (NSInteger) tableView: (UITableView *) tableView
  numberOfRowsInSection: (NSInteger) section
{
    return [provider_.notes count];
}

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
accessoryButtonTappedForRowWithIndexPath: (NSIndexPath *) idxPath
{
    MCSOAPNote               *note = [provider_.notes objectAtIndex: idxPath.row];
    MCSOAPNoteViewController *snvc = [[MCSOAPNoteViewController alloc] initWithNote: note];

    snvc.delegate = self;
    snvc.editable = NO;

    [self.navigationController pushViewController: snvc
                                         animated: YES];

    [snvc release];
}

@end
