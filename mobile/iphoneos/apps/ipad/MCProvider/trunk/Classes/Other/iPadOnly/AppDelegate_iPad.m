//
//  AppDelegate_iPad.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/29/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate_iPad.h"
#import "DataManager.h"
#import "DetailViewController.h"
#import "GroupDetailController.h"
#import "GroupRootController.h"
#import "MasterViewController.h"
#import "InfoViewController.h"
//#import "ScenarioManager.h"
#import "SettingsManager.h"

#pragma mark -
#pragma mark Public Class AppDelegate_iPad
#pragma mark -

@implementation AppDelegate_iPad

#pragma mark Public Instance Methods

- (void) dismissMasterPopoverAnimated: (BOOL) animated
{
    [self->masterDetailViewController_ dismissMasterPopoverAnimated: animated];
}

- (void) setupMono: (NSString *) urlString
{
    // no plist, so lets run this old school
    InfoViewController *ivc = [(!urlString ?
                                [[InfoViewController alloc] init] :
                                [[InfoViewController alloc] initWithSoloURL: urlString])
                               autorelease];

    self->navigationController_ = [[UINavigationController alloc]
                                   initWithRootViewController: ivc];

    [self setInitialRootViewController: self->navigationController_];
    [self setupControllers];
}

- (void) setupStereoWithGroup: (BOOL) group
{
  //  DataManager      *dm = self.dataManager;
    UIViewController *dvc;
    UIViewController *mvc;

    if (group)
    {
        self->baseDetailViewController_ = nil;

        dvc = [[[GroupDetailController alloc] init] autorelease];
        mvc = [[[GroupRootController alloc] init] autorelease];
    }
    else
    {
        self->baseDetailViewController_ = [[DetailViewController alloc] init];

        dvc = self->baseDetailViewController_;
        mvc = [[[MasterViewController alloc] init] autorelease];
    }

    self->masterDetailViewController_ = [[MCMasterDetailViewController alloc] initWithMasterViewController: mvc
                                                                                      detailViewController: dvc];

    self->masterDetailViewController_.navigationBarStyle = UIBarStyleBlack;

    [self setInitialRootViewController: self->masterDetailViewController_];
//
//    if (!group && [self.scenarioManager setupScenarioButtons])
//    {
//        NSUInteger lastScene = self.settingsManager.lastScene;
//
//        if (!dm.userHasConsented)
//            lastScene = 0;
//
//        dm.currentScene = lastScene;
//    }

    [self setupControllers];
}

#pragma mark Overridden AppDelegate Methods

- (DetailViewController *) baseDetailViewController
{
    return self->baseDetailViewController_;
}

- (UINavigationController *) detailNavigationController
{
    return self->masterDetailViewController_.detailNavigationController;
}

- (UINavigationController *) masterNavigationController
{
    return self->masterDetailViewController_.masterNavigationController;
}

- (UINavigationController *) navigationController
{
    return self->navigationController_;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->baseDetailViewController_ release];
    [self->masterDetailViewController_ release];
    [self->navigationController_ release];

    [super dealloc];
}

@end
