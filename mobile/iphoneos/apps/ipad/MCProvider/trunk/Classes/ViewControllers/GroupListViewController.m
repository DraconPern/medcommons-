//
//  GroupListViewController.m
//  MCProvider
//
//  Created by Bill Donner on 4/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "AsyncImageView.h"
#import "DataStore.h"
#import "Group.h"
#import "GroupListViewController.h"
#import "InboxGroupListController.h"
#import "MasterViewController.h"
#import "SessionManager.h"
#import "InfoViewController.h"
#import "MemberListViewController.h"
#import "Session.h"
#import "SessionManager.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class GroupListViewController
#pragma mark -

#pragma mark Internal Constants

//
// Assorted view tags:
//
enum
{
    MAIN_LABEL_TAG       = 101,
    DETAIL_LABEL_TAG,
    PHOTO_IMAGE_VIEW_TAG
};

@interface GroupListViewController () <UITableViewDataSource, UITableViewDelegate>

- (void) showInfo;

- (void) tableView: (UITableView *) tabView
      didSelectRow: (NSUInteger) row;

@end

@implementation GroupListViewController

#pragma mark Public Instance Methods

- (void) reloadData
{
    [self.tableView reloadData];
}

#pragma mark Private Instance Methods

- (void) showInfo
{
    if (self->ivc_)
        [self->ivc_ release];

    self->ivc_ = [[[InfoViewController alloc] init]
                  retain]; // this makes it work correctly, autorelease does not

    [self.navigationController pushViewController: self->ivc_
                                         animated: YES];
}

- (void) tableView: (UITableView *) tabView
      didSelectRow: (NSUInteger) row
{
    Session *session = self.appDelegate.sessionManager.loginSession;

    session.groupInFocus = [session.groups objectAtIndex: row];

    // dont' push, just pop back in

    [self.navigationController popViewControllerAnimated:YES];
//    InboxGroupListController *iglc = [[[InboxGroupListController alloc] init]
//                                      autorelease];
//
//    [self.navigationController pushViewController: iglc
//                                         animated: YES];
}

#pragma mark Overridden UIViewController Methods

- (void) didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];

    // Release any cached data, images, etc that aren't in use.
    [AsyncImageView clearCache]; // take it back to zero entries
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidLoad
{
    [super viewDidLoad];

    self.navigationItem.hidesBackButton = NO;
    self.navigationItem.title = @"Groups Chooser";

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
    self.navigationController.toolbarHidden = YES;

 //   if (self.appDelegate.targetIdiom == UIUserInterfaceIdiomPhone)
//        self.navigationItem.leftBarButtonItem = [[[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemBookmarks
//                                                                                               target: self
//                                                                                               action: @selector (showInfo)]
//                                                 autorelease];
//
//    if (!self.appDelegate.sessionManager.isLoggedIn)
//        [self showInfo];
}

- (void) viewWillAppear: (BOOL) animated
{
    [super viewWillAppear: animated];

    [self.navigationController setToolbarHidden: YES
                                       animated: animated];

    //NSLog (@"refresh table in GroupListViewController");
    [self.tableView reloadData]; // refresh
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    self.appDelegate.groupListViewController = nil; // mark us gone

    [super dealloc];
}

- (id) init
{
    self = [super initWithStyle: UITableViewStylePlain];

    if (self)
        self.appDelegate.groupListViewController = self; // mark us present

    return self;
}

#pragma mark UITableViewDataSource Methods

- (NSInteger) numberOfSectionsInTableView: (UITableView *) tabView
{
    return 1;
}

- (UITableViewCell *) tableView: (UITableView *) tabView
          cellForRowAtIndexPath: (NSIndexPath *) idxPath
{
    static NSString *CellIdentifier = @"GroupListCell";

    AsyncImageView *photoImageView;
    UILabel        *detailLabel;
    UILabel        *mainLabel;

    UITableViewCell *cell = [tabView dequeueReusableCellWithIdentifier: CellIdentifier];

    if (!cell)
    {
        cell = [[[UITableViewCell alloc] initWithFrame: CGRectZero
                                       reuseIdentifier: CellIdentifier]
                autorelease];

        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
        cell.highlighted = NO;

        mainLabel = [[[UILabel alloc]
                      initWithFrame: CGRectMake (52.0f, 2.0f, 250.0f, 30.0f)]
                     autorelease];

        mainLabel.font = [UIFont systemFontOfSize: 16.0f];
        mainLabel.tag = MAIN_LABEL_TAG;
        mainLabel.textColor = [UIColor darkTextColor];

        [cell.contentView addSubview: mainLabel];

        detailLabel = [[[UILabel alloc]
                        initWithFrame: CGRectMake (52.0f, 32.0f, 250.0f, 20.0f)]
                       autorelease];

        detailLabel.font = [UIFont systemFontOfSize: 12.0f];
        detailLabel.tag = DETAIL_LABEL_TAG;
        detailLabel.textColor = [UIColor darkTextColor];

        [cell.contentView addSubview: detailLabel];

        photoImageView = [[[AsyncImageView alloc]
                           initWithFrame: CGRectMake (2.0f, 2.0f, 47.0f, 47.0f)]
                          autorelease];

        photoImageView.tag = PHOTO_IMAGE_VIEW_TAG;

        [cell.contentView addSubview: photoImageView];
    }
    else
    {
        mainLabel = (UILabel *) [cell.contentView viewWithTag: MAIN_LABEL_TAG];
        detailLabel = (UILabel *) [cell.contentView viewWithTag: DETAIL_LABEL_TAG];
        photoImageView = (AsyncImageView *) [cell.contentView viewWithTag: PHOTO_IMAGE_VIEW_TAG];
    }

    SessionManager *sm = self.appDelegate.sessionManager;
    Group          *group = [sm.loginSession.groups objectAtIndex: idxPath.row];
    StyleManager   *styles = self.appDelegate.styleManager;

    mainLabel.text = group.name;

    NSString *path = [DataStore pathForGroupWithIdentifier: group.identifier];
    NSDate   *modDate = [NSFileManager fileModificationDateOfItemAtPath: path];

    detailLabel.text = [modDate description];

    [photoImageView loadImageFromURL: group.logoURL
                       fallbackImage: styles.fallbackGroupLogoImageS];

    return cell;
}

- (CGFloat) tableView: (UITableView *) tabView
heightForRowAtIndexPath: (NSIndexPath *) idxPath
{
    return 52.0f;
}

- (NSInteger) tableView: (UITableView *) tabView
  numberOfRowsInSection: (NSInteger) section
{
    return [self.appDelegate.sessionManager.loginSession.groups count];
}

#pragma mark UITableViewDelegate Methods

- (void) tableView: (UITableView *) tabView
didSelectRowAtIndexPath: (NSIndexPath *) idxPath
{
    [self tableView: self.tableView
       didSelectRow: idxPath.row];
}

@end
