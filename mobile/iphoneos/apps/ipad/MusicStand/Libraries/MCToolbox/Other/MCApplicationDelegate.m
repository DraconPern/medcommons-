//
//  MCApplicationDelegate.m
//  MCToolbox
//
//  Created by J. G. Pusey on 4/19/10.
//  Copyright 2010 MedCommons, Inc. All rights reserved.
//

#import "MCToolbox.h"   // <MCToolbox/MCToolbox.h>

#import "MCApplicationDelegate.h"

@interface MCApplicationDelegate ()

@property (nonatomic, assign, readwrite) NSUInteger networkActivityCount;

@end

@implementation MCApplicationDelegate

@synthesize networkActivityCount = networkActivityCount_;
@synthesize rootViewController   = rootViewController_;
@synthesize window               = window_;

#pragma mark Public Instance Methods

- (void) didStartNetworkActivity
{
    if (self.networkActivityCount < NSUIntegerMax)
        self.networkActivityCount++;

    self.application.networkActivityIndicatorVisible = YES;
}

- (void) didStopNetworkActivity
{
    if (self.networkActivityCount > 0)
        self.networkActivityCount--;

    self.application.networkActivityIndicatorVisible = (self.networkActivityCount > 0);
}

- (void) setInitialRootViewController: (UIViewController *) vc
{
    self->rootViewController_ = [vc retain];

    self->window_ = [[UIWindow alloc] initWithFrame: [UIScreen mainScreen].bounds];

    [self->window_ addSubview: self->rootViewController_.view];
    [self->window_ makeKeyAndVisible];
}

#pragma mark Overridden NSObject Instance Methods

- (void) dealloc
{
    [self->rootViewController_ release];
    [self->window_ release];

    [super dealloc];
}

@end
