//
//  DetailViewController.m
//  MCProvider
//
//  Created by Bill Donner on 2/24/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "AppDelegate.h"
#import "DataManager.h"
#import "DetailViewController.h"
#import "InfoViewController.h"
#import "MasterViewController.h"
#import "SessionManager.h"
#import "ScenarioManager.h"
#import "SegmentMap.h"
//#import "SettingsManager.h"
#import "ShooterController.h"

#pragma mark -
#pragma mark Public Class AppDelegate
#pragma mark -

@interface DetailViewController ()

- (void) showInfo;

@end

@implementation DetailViewController

@dynamic detailItem;

#pragma mark Public Instance Methods

- (NSString *) detailItem
{
    return self->detailItem_;
}

- (void) setDetailItem: (NSString *) detailItem
{
    if (self->detailItem_ != detailItem)
    {
        [self->detailItem_ release];

        self->detailItem_ = [detailItem retain];
//
//        self.appDelegate.settingsManager.lastDetailItem = self->detailItem_;
//
//        NSDictionary *dict = [self.appDelegate.scenarioManager
//                              configureDetailViewFromSceneBlock: [self->detailItem_ intValue]];
//
//        [self displayDetailWebView: [dict objectForKey: @"URL"]
//                   backgroundColor: [UIColor redColor]
//                             title: [dict objectForKey: @"Title"]];
    }

    //self->fullScreenWebView_ = NO; // never get full screen this way
}

- (void) viewInSafari
{
    DataManager  *dm = self.appDelegate.dataManager;
    NSDictionary *scene = [dm currentSceneContext];

    if (!scene)
        return;

    NSDictionary *block = [dm currentBlock: [detailItem_ intValue]
                                  forScene: scene];

    if (!block)
        return;

    if (![block objectForKey: @"URL"])
    {
        [dm dieFromMisconfiguration: @"Missing <url/> in block"];

        return;
    }

    NSString *urlString = [[block objectForKey: @"URL"] stringByTrimmingWhitespace];

    [self.application openURL: [NSURL URLWithString: urlString]];
}

#pragma mark Private Instance Methods

- (void) showInfo
{
    InfoViewController *ivc = [[[InfoViewController alloc] init]
                               autorelease];

    [self.appDelegate.detailNavigationController setRootViewController: ivc
                                                              animated: YES];
}

#pragma mark Overridden UIViewController Methods

- (void) loadView
{
    self.view = [[[UIView alloc]
                  initWithFrame: self.parentViewController.view.bounds]
                 autorelease];
}

- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
{
    return YES;
}

- (void) viewDidLoad
{
    [super viewDidLoad];

    AppDelegate  *appDel = self.appDelegate;
    DataManager  *dm = appDel.dataManager;
    NSDictionary *scene = [dm currentSceneContext]; // deep error checking in here

    if (!scene)
        return;

    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;

    if (![scene objectForKey: @"largetitle"])
        [dm dieFromMisconfiguration: @"No largetitle for current scene"];

    self.navigationItem.title = dm.topLevelHomeTitle ; //@"Home";
    self.navigationItem.title = [scene objectForKey: @"largetitle"];
    //self.navigationItem.rightBarButtonItem = dm.rightDetailSM.segmentBarItem;
    UIBarButtonItem *bbi = [[UIBarButtonItem alloc] initWithBarButtonSystemItem: UIBarButtonSystemItemBookmarks
                                                                         target: self
                                                                         action: @selector (showInfo)];
    self.navigationItem.rightBarButtonItem = bbi;

    NSString *onceURL = [dm.masterPlist objectForKey: @"once"];

    if (onceURL)
    {
        [appDel didStartNetworkActivity];

        CGRect bounds = [[UIScreen mainScreen] bounds];

        //put up something other than the white background
        UIImageView *imageView = [[[UIImageView alloc] initWithImage: [UIImage imageNamed: @"nAppleIcon_512x512.png"]]
                                  autorelease];

        imageView.center = CGPointMake (bounds.size.width / 2.0f,
                                        bounds.size.height / 2.0f);

        [self.view addSubview: imageView];

        // just call up this url, dont even display it, so cookies can get set, etc
        NSURL     *url = [NSURL URLWithString: [onceURL stringByTrimmingWhitespace]];
        UIWebView *wv = [[[UIWebView alloc] init] autorelease];

        [wv loadRequest: [NSURLRequest requestWithURL: url]];

        [appDel didStopNetworkActivity];
    }

    //   NSString *lastDetailItem = [self.appDelegate.settingsManager.lastDetailItem;
    //
    //    if(lastDetailItem && dm.userHasConsented)
    //        [self setDetailItem: lastDetailItem];
    //    else
    {
        // first time up, put up homepage
        // nearly identical code in both GroupDetailController and DetailViewController -bill
        self.navigationItem.title = dm.topLevelHomeTitle ; //@"Home";

        NSURL     *contentURL_ = dm.topLevelHomeURL;
        UIWebView *contentView_ = [[UIWebView alloc]
                                   initWithFrame:self.parentViewController.view.bounds];

        contentView_.autoresizingMask = (UIViewAutoresizingFlexibleHeight |
                                         UIViewAutoresizingFlexibleWidth);
        contentView_.backgroundColor = [UIColor blueColor];
        contentView_.scalesPageToFit = YES;
        [contentView_ loadRequest: [NSURLRequest requestWithURL: contentURL_]];
        self.view = contentView_;
        [contentView_ release];
    }
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [detailItem_ release];
    [lastURL_ release];

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
