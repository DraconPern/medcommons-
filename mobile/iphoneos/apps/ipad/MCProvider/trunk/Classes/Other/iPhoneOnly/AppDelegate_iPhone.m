//
//  AppDelegate_iPhone.m
//  MCProvider
//
//  Created by J. G. Pusey on 4/29/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "AppDelegate_iPhone.h"
#import "GroupListViewController.h"

#pragma mark -
#pragma mark Public Class AppDelegate_iPhone
#pragma mark -

@implementation AppDelegate_iPhone

#pragma mark Public Instance Methods

- (void) setupMono: (NSString *) urlString
{
    GroupListViewController *glvc = [[[GroupListViewController alloc] init]
                                     autorelease];

    self->navigationController_ = [[UINavigationController alloc]
                                   initWithRootViewController: glvc];

    [self setInitialRootViewController: self->navigationController_];
    [self setupControllers];
}

#pragma mark Overridden AppDelegate Methods

- (DetailViewController *) baseDetailViewController
{
    return nil;
}

- (UINavigationController *) detailNavigationController
{
    return nil;
}

- (UINavigationController *) masterNavigationController
{
    return nil;
}

- (UINavigationController *) navigationController
{
    return self->navigationController_;
}

#pragma mark Overridden NSObject Methods

- (void) dealloc
{
    [self->navigationController_ release];

    [super dealloc];
}

@end
