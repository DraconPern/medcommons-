//
//  GroupsActionController.m
//  MCProvider
//
//  Created by Bill Donner on 9/30/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate.h"
#import "Group.h"
#import "GroupsActionController.h"
#import "Member.h"
#import "Session.h"
#import "SessionManager.h"
#import "SettingsManager.h"
#import "WebViewController.h"

#pragma mark -
#pragma mark Public Class GroupsActionController
#pragma mark -

@interface GroupsActionController ()

- (void) chooseGroup: (id) userInfo;

@end

@implementation GroupsActionController

#pragma mark Private Instance Methods

- (void) chooseGroup: (id) userInfo
{
    AppDelegate     *appDel = self.appDelegate;
    Session         *session = appDel.sessionManager.loginSession;
    SettingsManager *settings = appDel.settingsManager;

    session.groupInFocus = userInfo;

    settings.lastGroupID = session.groupInFocus.identifier;

    [settings synchronizeReadWriteSettings];
}

#pragma mark Overridden NSObject Methods

- (id) init
{
    self = [super initWithTitle: NSLocalizedString (@"Choose Group", @"")];

    if (self)
    {
        AppDelegate *appDel = self.appDelegate;

        if (appDel.targetIdiom != UIUserInterfaceIdiomPad)
            [self addCancelButtonWithTitle: NSLocalizedString (@"Cancel", @"")
                                    target: nil
                                    action: NULL
                                  userInfo: nil];

        SessionManager *sm = appDel.sessionManager;

        for (Group *group in sm.loginSession.groups)
            [self addOtherButtonWithTitle: group.name
                                   target: self
                                   action: @selector (chooseGroup:)
                                 userInfo: group];
    }

    return self;
}

@end
