//
//  AppDelegate.h
//  MCProvider
//
//  Created by Bill Donner on 3/4/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

@class DataManager;
@class DetailViewController;
@class DocumentsManager;
@class SessionManager;
@class MCNetworkReachability;
@class InfoViewController;
@class GroupListViewController;
@class GroupRootController;
@class SettingsManager;
//@class ScenarioManager;
@class StyleManager;

@interface AppDelegate : MCApplicationDelegate
{
@private

    GroupListViewController  *groupListViewController_;
    GroupRootController      *groupRootController_;
    MCNetworkReachability    *hostReachability_;
    InfoViewController       *infoViewController_;
    MCNetworkReachability    *internetReachability_;
    MCNetworkReachability    *localWiFiReachability_;
    NSString                 *sessionRandomID_;
    //
    // This is the UI idiom we are targeting; on iPhone/iPod touch device, only
    // UIUserInterfaceIdiomPhone is allowed; on iPad device, both
    // UIUserInterfaceIdiomPad and UIUserInterfaceIdiomPhone are allowed:
    //
    UIUserInterfaceIdiom      targetIdiom_;
}

@property (nonatomic, retain, readonly)  DetailViewController    *baseDetailViewController;
@property (nonatomic, retain, readonly)  DataManager             *dataManager;
@property (nonatomic, retain, readonly)  UINavigationController  *detailNavigationController;
@property (nonatomic, retain, readonly)  DocumentsManager        *documentsManager;
@property (nonatomic, retain, readwrite) GroupListViewController *groupListViewController;
@property (nonatomic, retain, readwrite) GroupRootController     *groupRootController;
@property (nonatomic, retain, readonly)  MCNetworkReachability   *hostReachability;
@property (nonatomic, retain, readonly)  MCNetworkReachability   *internetReachability;
@property (nonatomic, retain, readwrite) InfoViewController      *infoViewController;
@property (nonatomic, retain, readonly)  MCNetworkReachability   *localWiFiReachability;
@property (nonatomic, retain, readonly)  UINavigationController  *masterNavigationController;
@property (nonatomic, retain, readonly)  UINavigationController  *navigationController;
//@property (nonatomic, retain, readonly)  ScenarioManager         *scenarioManager;
@property (nonatomic, retain, readonly)  SessionManager          *sessionManager;
@property (nonatomic, retain, readonly)  NSString                *sessionRandomID;
@property (nonatomic, retain, readonly)  SettingsManager         *settingsManager;
@property (nonatomic, retain, readonly)  StyleManager            *styleManager;
@property (nonatomic, assign, readonly)  UIUserInterfaceIdiom     targetIdiom;

+ (AppDelegate *) sharedInstance;

- (void) dieFromMisconfiguration: (NSString *) msg;

- (void) dismissMasterPopoverAnimated: (BOOL) animated;

- (void) setupControllers;


@end

//
// NSObject convenience additions:
//
@interface NSObject (AppDelegate)

@property (nonatomic, assign, readonly) AppDelegate *appDelegate;

+ (AppDelegate *) appDelegate;

@end
