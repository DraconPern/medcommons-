//
//  GroupDetailController.m
//  MCProvider
//
//  Created by Bill Donner on 4/26/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "GroupDetailController.h"

#import "AppDelegate.h"
#import "DataManager.h"
#import "InboxGroupListController.h"
#import "SessionManager.h"
#import "Member.h"
#import "InfoViewController.h"

#pragma mark -
#pragma mark Public Class AppDelegate
#pragma mark -

@interface GroupDetailController ()

@property (nonatomic, retain, readwrite) InboxGroupListController *iglc;

- (void) showInfo;

@end

@implementation GroupDetailController

@synthesize detailItem = detailItem_;
@synthesize iglc       = iglc_;

#pragma mark Public Instance Methods

- (void) displayMemberList
{
    // this is signalled from the grouproot controller
    if (self.iglc)
    {
        [self.navigationController popViewControllerAnimated: NO];

        self.iglc = nil;    // !!!!
    }

    self.iglc = [[[InboxGroupListController alloc] init]
                 autorelease];

    [self.navigationController pushViewController: self.iglc
                                         animated: YES];
}

#pragma mark Private Instance Methods

- (void) showInfo
{
    InfoViewController *ivc = [[[InfoViewController alloc] initWithNoGroups]
                               autorelease];

    [self.navigationController pushViewController: ivc
                                         animated: YES];
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    self.view = [[[UIView alloc] initWithFrame: self.parentViewController.view.bounds]
                 autorelease];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidLoad
{
    [super viewDidLoad];

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;

    UIBarButtonItem *bbi = [[ UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemBookmarks
                                                                          target: self
                                                                          action: @selector (showInfo)];

    self.navigationItem.rightBarButtonItem = bbi;

    // nearly identical code in both GroupDetailController and DetailViewController -bill

    DataManager *dm = self.appDelegate.dataManager;

    self.navigationItem.title = dm.topLevelHomeTitle; //@"Home";

    // paint

    UIWebView *contentView = [[UIWebView alloc]
                              initWithFrame: self.parentViewController.view.bounds];

    contentView.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                    UIViewAutoresizingFlexibleWidth);
    contentView.backgroundColor = [UIColor blueColor];
    contentView.scalesPageToFit = YES;

    [contentView loadRequest: [NSURLRequest requestWithURL: dm.topLevelHomeURL]];

    self.view = contentView;

    [contentView release];

    if (!self.appDelegate.sessionManager.isLoggedIn)
    {
        InfoViewController *ivc = [[[InfoViewController alloc] init]
                                   autorelease];

        [self.navigationController pushViewController: ivc
                                             animated: YES];
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->detailItem_ release];
    [self->iglc_ release];

    [super dealloc];
}

#pragma mark Extended UIViewController Methods

- (void) hideMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: nil
                                     animated: YES];
}

- (void) showMasterPopoverBarButtonItem: (UIBarButtonItem *) bbi
{
    [self.navigationItem setLeftBarButtonItem: bbi
                                     animated: YES];
}

@end
