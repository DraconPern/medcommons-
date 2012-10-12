//
//  GroupRootController.m
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
#import "GroupDetailController.h"
#import "GroupRootController.h"
#import "InboxGroupListController.h"
#import "MasterViewController.h"
#import "MemberListViewController.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "StyleManager.h"

#pragma mark -
#pragma mark Public Class GroupRootController
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

@interface GroupRootController ()

- (void) tableView: (UITableView *) tabView
      didSelectRow: (NSUInteger) row;

@end

@implementation GroupRootController

#pragma mark Public Instance Methods

- (void) reloadData
{
    [self.tableView reloadData];
}
#pragma mark SessionManager Notification Methods

- (void) sessionInfoDidChange: (NSNotification *) notification
{
    [self.tableView reloadData];
}
#pragma mark Private Instance Methods

- (void) addNotificationObservers
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

    [nc addObserver: self
           selector: @selector (sessionInfoDidChange:)
               name: SessionManagerSessionInfoDidChangeNotification
             object: nil];
}

- (void) removeNotificationObservers
{
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

- (void) tableView: (UITableView *) tabView
      didSelectRow: (NSUInteger) row
{
    AppDelegate *appDel = self.appDelegate;

    [appDel dismissMasterPopoverAnimated: YES];

    Session *session = appDel.sessionManager.loginSession;

    session.groupInFocus = [session.groups objectAtIndex: row];

    if (appDel.settingsManager.simulatePhoneUI)
    {
        InboxGroupListController *iglc = [[[InboxGroupListController alloc] init]
                                          autorelease];

        [self.navigationController pushViewController: iglc
                                             animated: YES];
    }
    else
    {
        UIViewController *vc = appDel.detailNavigationController.topViewController;

        if ([vc isKindOfClass: [GroupDetailController class]])
        {
            GroupDetailController *gdc = (GroupDetailController *) vc;

            [gdc displayMemberList];
        }
        else
        {
            [appDel.detailNavigationController popViewControllerAnimated: NO];  // throw off the top

            vc = appDel.detailNavigationController.topViewController;    // note this will have changed

            if ([vc isKindOfClass: [GroupDetailController class]])
            {
                GroupDetailController *gdc = (GroupDetailController *) vc;

                [gdc displayMemberList];
            }
        }
    }
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

    SettingsManager *settings = self.appDelegate.settingsManager;

    self.navigationItem.title = settings.topLevelGroupTitle;

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;

}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    //NSLog (@"viewWillAppear table in GroupRootController");
    [self.tableView reloadData];


    [self addNotificationObservers];

}

- (void) viewWillDisappear: (BOOL) animated
{
    [super viewWillDisappear: animated];

    [self removeNotificationObservers];
}
- (void) viewDidUnload
{
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{

    self.appDelegate.groupRootController = nil; // mark us gone
    [super dealloc];
}

- (id) init
{



    self = [super initWithStyle: UITableViewStylePlain];

    self.appDelegate.groupRootController = self; // mark us present
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
    static NSString *CellIdentifier = @"GroupRootCell";

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
